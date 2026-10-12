package com.example.cyberpunkandroid.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberSlider
import com.example.cyberpunkandroid.components.CyberSwitch
import com.example.cyberpunkandroid.effects.CyberCometTracer
import com.example.cyberpunkandroid.effects.cyberPathBorder
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Debug-only playground for [CyberTripleChevron]: sliders for every geometry parameter,
 * a live readout of how close the stroke is to its natural limit, the component's bounds
 * (which grow with the stroke, up to the stage), and a comet tracer running along each
 * chevron's outline. Open the preview in interactive mode,
 * or run it on a device from the preview gutter.
 */
@Composable
internal fun CyberTripleChevronPlayground() {
    var widthDp by remember { mutableFloatStateOf(240f) }
    var aspectRatio by remember { mutableFloatStateOf(2.5f) }
    var spacingDp by remember { mutableFloatStateOf(12f) }
    var thicknessDp by remember { mutableFloatStateOf(12f) }
    var shear by remember { mutableFloatStateOf(0.2f) }
    var tracer by remember { mutableStateOf(true) }

    val width = widthDp.dp
    val spacing = spacingDp.dp
    val thickness = thicknessDp.dp
    val status = CyberTheme.status

    // Each chevron is its own contour, and path borders trace one contour, so one tracer each.
    val traced = (0..2).fold(Modifier.border(1.dp, CyberTheme.colors.border) as Modifier) { m, index ->
        if (!tracer) m
        else m.cyberPathBorder(
            CyberCometTracer(),
            color = CyberTheme.colors.secondary,
            shape = CyberTripleChevronShape(width, aspectRatio, spacing, thickness, shear, chevron = index),
            inset = 0.dp
        )
    }

    Column(
        modifier = Modifier
            .background(CyberTheme.colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // The stage is the space the chevrons may grow into: their natural limit.
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val geometry = with(LocalDensity.current) {
                CyberTripleChevronShape(width, aspectRatio, spacing, thickness, shear)
                    .geometry(this, maxWidth.toPx(), StageHeight.toPx())
            }
            val maxThickness = geometry?.let { with(LocalDensity.current) { it.maxStrokeWidth.toDp() } }
            val load = geometry?.let { StrokeLoad.of(it.load) }
            val loadColor = when (load) {
                StrokeLoad.Ok -> status.success.color
                StrokeLoad.Notice -> status.info.color
                StrokeLoad.Warning -> status.warning.color
                StrokeLoad.Limit, null -> status.danger.color
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth().height(StageHeight), contentAlignment = Alignment.Center) {
                    CyberTripleChevron(
                        modifier = traced,
                        width = width,
                        aspectRatio = aspectRatio,
                        spacing = spacing,
                        thickness = thickness,
                        shear = shear
                    )
                }
                Label(
                    text = when {
                        geometry == null -> "Too small to draw"
                        load == StrokeLoad.Limit -> "Thickness held at %.1f dp (limit)".format(maxThickness?.value)
                        else -> "Stroke load %s: %.0f%% of %.1f dp max".format(load, geometry.load * 100f, maxThickness?.value)
                    },
                    color = loadColor
                )
            }
        }
        Setting("Centerline width", widthDp, 32f..320f, "%.0f dp") { widthDp = it }
        Setting("Aspect ratio", aspectRatio, 0.5f..6f, "%.2f") { aspectRatio = it }
        Setting("Spacing", spacingDp, 0f..32f, "%.1f dp") { spacingDp = it }
        Setting("Thickness", thicknessDp, 0.5f..48f, "%.1f dp") { thicknessDp = it }
        Setting("Shear", shear, -1f..1f, "%.2f") { shear = it }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Label("Tracer", CyberTheme.colors.textSecondary)
            CyberSwitch(checked = tracer, customA11y = "Tracer", onCheckedChange = { tracer = it })
        }
    }
}

@Composable
private fun Setting(
    name: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    format: String,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Label("$name: ${format.format(value)}", CyberTheme.colors.textSecondary)
        CyberSlider(
            value = value,
            valueRange = range,
            modifier = Modifier.fillMaxWidth(),
            customA11y = name,
            onValueChange = onValueChange
        )
    }
}

@Composable
private fun Label(text: String, color: Color) {
    BasicText(text, style = CyberTheme.typography.terminal.copy(color = color))
}

// Tall enough to show the stroke growing the component before it reaches the stage edge.
private val StageHeight = 260.dp

@Preview(widthDp = 360, heightDp = 760)
@Composable
private fun CyberTripleChevronPlaygroundPreview() {
    CyberTheme { CyberTripleChevronPlayground() }
}
