package com.example.cyberpunkandroid.sliderpanel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyberpunkandroid.sliderpanel.model.SpecParseResult
import com.example.cyberpunkandroid.sliderpanel.model.parseSliderPanelSpec
import com.example.cyberpunkandroid.sliderpanel.state.initialSliderPanelState
import com.example.cyberpunkandroid.sliderpanel.state.reduce
import com.example.cyberpunkandroid.theme.CyberTheme

/** A spec that uses every scale and mark style. */
internal val SamplePanelJson = """
{
  "title": "STROKE LAB",
  "mode": "floating",
  "pinEdge": "bottom",
  "scrollable": true,
  "maxHeightDp": 300,
  "widthDp": 320,
  "sliders": [
    { "id": "thickness", "label": "Thickness", "unit": "dp", "start": 0.5, "stop": 64, "initial": 4,
      "step": 0.5, "decimals": 1, "scale": { "type": "log" },
      "marks": { "values": [{ "value": 1 }, { "value": 4 }, { "value": 16 }, { "value": 64, "label": "MAX" }], "minor": 1 } },
    { "id": "width", "label": "Width", "unit": "dp", "start": 24, "stop": 320, "initial": 160, "step": 1, "decimals": 0,
      "marks": { "every": 50, "minor": 4 } },
    { "id": "gain", "label": "Gain", "unit": "dB", "start": -60, "stop": 12, "initial": 0, "decimals": 1,
      "scale": { "type": "piecewise", "points": [
        { "position": 0, "value": -60 }, { "position": 0.5, "value": -12 }, { "position": 0.85, "value": 0 }, { "position": 1, "value": 12 } ] },
      "marks": { "values": [{ "value": -60 }, { "value": -12 }, { "value": 0, "label": "0 dB" }, { "value": 12 }] } },
    { "id": "detail", "label": "Detail", "start": 0, "stop": 1, "initial": 0.25, "decimals": 3,
      "scale": { "type": "power", "exponent": 2.5 }, "marks": { "every": 0.25, "minor": 4 } }
  ]
}
""".trimIndent()

@Preview(widthDp = 380, heightDp = 640)
@Composable
private fun CyberSliderPanelPreview() {
    val spec = (parseSliderPanelSpec(SamplePanelJson) as SpecParseResult.Success).spec
    var state by remember { mutableStateOf(initialSliderPanelState(spec)) }
    CyberTheme {
        Box(Modifier.fillMaxSize().background(CyberTheme.colors.background)) {
            CyberSliderPanel(state, onEvent = { state = state.reduce(it) })
        }
    }
}
