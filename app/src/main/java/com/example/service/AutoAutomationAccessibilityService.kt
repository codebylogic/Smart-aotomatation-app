package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

class AutoAutomationAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AutoAccessibility"
        private var instanceRef: WeakReference<AutoAutomationAccessibilityService>? = null

        fun isServiceRunning(): Boolean {
            return instanceRef?.get() != null
        }

        fun getInstance(): AutoAutomationAccessibilityService? {
            return instanceRef?.get()
        }

        var onEventHookReceived: ((eventType: String, details: String) -> Unit)? = null

        fun performClickAt(x: Float, y: Float): Boolean {
            val service = getInstance() ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val path = Path().apply {
                    moveTo(x, y)
                }
                val gesture = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                    .build()
                return service.dispatchGesture(gesture, null, null)
            }
            return false
        }

        fun performGesturePath(path: Path, durationMs: Long): Boolean {
            val service = getInstance() ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val gesture = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
                    .build()
                return service.dispatchGesture(gesture, null, null)
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
        Log.i(TAG, "AutoAutomationAccessibilityService connected")
        onEventHookReceived?.invoke("SERVICE_CONNECTED", "Accessibility hook initialized")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventTypeName = AccessibilityEvent.eventTypeToString(event.eventType)
        val pkg = event.packageName?.toString() ?: "system"
        val text = event.text?.joinToString() ?: ""

        // Broadcast to live hook stream
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            onEventHookReceived?.invoke("WINDOW_STATE", "$pkg : $text")
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "AutoAutomationAccessibilityService interrupted")
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event != null && event.action == KeyEvent.ACTION_DOWN) {
            val keyCode = event.keyCode
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                onEventHookReceived?.invoke(
                    "KEY_HOOK",
                    "KeyHook: ${KeyEvent.keyCodeToString(keyCode)} pressed"
                )
            }
        }
        return super.onKeyEvent(event)
    }

    override fun onDestroy() {
        instanceRef = null
        super.onDestroy()
    }
}
