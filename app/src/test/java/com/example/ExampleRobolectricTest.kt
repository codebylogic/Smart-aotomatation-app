package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.WorkflowGraph
import com.example.sync.BiDirectionalWorkflowSyncer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NexusFlow", appName)
  }

  @Test
  fun `test graph to js code generation`() {
    val defaultGraph = WorkflowGraph.createDefault()
    val js = BiDirectionalWorkflowSyncer.generateJsFromGraph(defaultGraph)

    assertTrue(js.contains("export default async function run(event)"))
    assertTrue(js.contains("WiFi.getConnectedSSID()"))
    assertTrue(js.contains("Device.setRingerMode('SILENT')"))
    assertTrue(js.contains("Device.toggleTorch(true)"))
  }

  @Test
  fun `test js code to graph parsing`() {
    val sampleCode = """
      export default async function run(event) {
          const ssid = await WiFi.getConnectedSSID();
          if (ssid === 'Office_WiFi_Ext') {
              await Device.setRingerMode('VIBRATE');
              await Device.toggleTorch(false);
          }
          return { success: true };
      }
    """.trimIndent()

    val baseGraph = WorkflowGraph.createDefault()
    val parsedGraph = BiDirectionalWorkflowSyncer.parseJsToGraph(sampleCode, baseGraph)

    val condNode = parsedGraph.nodes.find { it.type == com.example.data.model.NodeType.CONDITION }
    assertEquals("Office_WiFi_Ext", condNode?.config?.get("ssid"))

    val actionNode = parsedGraph.nodes.find { it.type == com.example.data.model.NodeType.ACTION }
    assertEquals("VIBRATE", actionNode?.config?.get("ringer_mode"))
  }
}

