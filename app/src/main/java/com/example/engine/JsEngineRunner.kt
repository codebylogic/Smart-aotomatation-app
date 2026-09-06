package com.example.engine

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.example.data.model.LogLevel

class JsEngineRunner(private val context: Context, val deviceAPI: DeviceAPI) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var webView: WebView? = null

    init {
        mainHandler.post {
            setupWebView()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        if (webView != null) return
        try {
            webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                addJavascriptInterface(deviceAPI, "DeviceNative")

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        if (consoleMessage != null) {
                            val msg = consoleMessage.message()
                            when (consoleMessage.messageLevel()) {
                                ConsoleMessage.MessageLevel.ERROR -> deviceAPI.emitLog(LogLevel.ERROR, msg)
                                ConsoleMessage.MessageLevel.WARNING -> deviceAPI.emitLog(LogLevel.WARN, msg)
                                ConsoleMessage.MessageLevel.DEBUG -> deviceAPI.emitLog(LogLevel.DEBUG, msg)
                                else -> deviceAPI.emitLog(LogLevel.INFO, msg)
                            }
                        }
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            deviceAPI.emitLog(LogLevel.ERROR, "Failed to initialize WebView engine: ${e.message}")
        }
    }

    fun executeScript(
        rawScript: String,
        triggerSource: String = "AccessibilityService hook (Gesture: DOUBLE_TAP_BACK)",
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        val startTime = System.currentTimeMillis()
        deviceAPI.emitLog(LogLevel.INFO, "Triggered via $triggerSource")

        mainHandler.post {
            try {
                if (webView == null) {
                    setupWebView()
                }

                val bridgePreamble = """
                    (function() {
                        window.Device = {
                            setVolume: function(stream, level) { return window.DeviceNative.setVolume(stream, level); },
                            toggleWifi: function(state) { return window.DeviceNative.toggleWifi(state); },
                            toggleTorch: function(state) { return window.DeviceNative.toggleTorch(state); },
                            performClick: function(x, y) { return window.DeviceNative.performClick(x, y); },
                            performGesture: function(pts, dur) { return window.DeviceNative.performGesture(JSON.stringify(pts), dur || 100); },
                            showFloatingButton: function(cfg) { return window.DeviceNative.showFloatingButton(JSON.stringify(cfg)); },
                            sendNotification: function(t, m) { return window.DeviceNative.sendNotification(t, m); },
                            vibrate: function(pat) { return window.DeviceNative.vibrate(JSON.stringify(pat)); },
                            setRingerMode: function(m) { return window.DeviceNative.setRingerMode(m); }
                        };
                        window.WiFi = {
                            getConnectedSSID: async function() { return window.DeviceNative.getConnectedSSID(); }
                        };
                        window.Logger = {
                            info: function(m) { window.DeviceNative.log('INFO', String(m)); },
                            debug: function(m) { window.DeviceNative.log('DEBUG', String(m)); },
                            warn: function(m) { window.DeviceNative.log('WARN', String(m)); },
                            error: function(m) { window.DeviceNative.log('ERROR', String(m)); },
                            success: function(m) { window.DeviceNative.log('SUCCESS', String(m)); }
                        };
                        window.System = {
                            timestamp: function() { return Date.now(); }
                        };
                    })();
                """.trimIndent()

                // Sanitize script by stripping ES module import/export if present so it evaluates cleanly
                var runnableCode = rawScript
                    .replace(Regex("""import\s+[^;]+;"""), "")
                    .replace("export default async function run(event)", "async function userRun(event)")
                    .replace("export default function run(event)", "function userRun(event)")

                val invocation = """
                    $bridgePreamble
                    (async function() {
                        try {
                            $runnableCode
                            var res = await userRun({ source: '$triggerSource' });
                            return JSON.stringify({ success: true, result: res });
                        } catch(err) {
                            window.Logger.error(err && err.stack ? err.stack : String(err));
                            return JSON.stringify({ success: false, error: String(err) });
                        }
                    })();
                """.trimIndent()

                webView?.evaluateJavascript(invocation) { result ->
                    val elapsed = System.currentTimeMillis() - startTime
                    deviceAPI.emitLog(LogLevel.STATUS, "Process finished with exit code 0 (Elapsed: ${elapsed}ms)")
                    onComplete?.invoke(true, result ?: "")
                }
            } catch (e: Exception) {
                deviceAPI.emitLog(LogLevel.ERROR, "JS Engine Execution Crash: ${e.message}")
                onComplete?.invoke(false, e.message ?: "Unknown error")
            }
        }
    }
}
