package com.example.cyberpunkandroid.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberSlider
import com.example.cyberpunkandroid.components.CyberSwitch
import com.example.cyberpunkandroid.effects.CyberCometTracer
import com.example.cyberpunkandroid.effects.cyberPerimeter
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Sample centerlines: closed, concave, and self-crossing, open and closed. */
internal object StrokeIconSamples {
    val ring = cyberCenterline(24f, 24f) { moveTo(12f, 3f); arcTo(12f, 12f, 360f); close() }

    val star = cyberCenterline(24f, 24f) {
        for (k in 0 until 10) {
            val radius = if (k % 2 == 0) 10.0 else 4.0
            val a = -PI / 2 + k * PI / 5
            val x = (12 + radius * cos(a)).toFloat()
            val y = (12 + radius * sin(a)).toFloat()
            if (k == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    /** Open, and it crosses itself: the loop's eye is a hole until the stroke closes it. */
    val cursiveL = cyberCenterline(24f, 24f) {
        moveTo(4f, 21f)
        cubicTo(9f, 19f, 17f, 10f, 17f, 5f)
        cubicTo(17f, 2f, 13f, 2f, 12f, 5f)
        cubicTo(10f, 11f, 9f, 17f, 10f, 20f)
        cubicTo(11f, 22f, 14f, 21f, 16f, 19f)
    }

    /** Closed, and it crosses itself: each lobe thickens outward. */
    val figureEight = cyberCenterline(24f, 12f) {
        moveTo(12f, 6f)
        cubicTo(18f, 0f, 24f, 0f, 24f, 6f)
        cubicTo(24f, 12f, 18f, 12f, 12f, 6f)
        cubicTo(6f, 0f, 0f, 0f, 0f, 6f)
        cubicTo(0f, 12f, 6f, 12f, 12f, 6f)
        close()
    }

    /** Closed and concave, with a narrow notch that a thick outside stroke closes over. */
    val notchedU = cyberCenterline(24f, 24f) {
        moveTo(2f, 2f); lineTo(10f, 2f); lineTo(10f, 14f); lineTo(14f, 14f); lineTo(14f, 2f); lineTo(22f, 2f)
        lineTo(22f, 14f); arcTo(12f, 14f, 180f); close()
    }

    val all = listOf("Ring" to ring, "Star" to star, "Cursive l" to cursiveL, "Figure 8" to figureEight, "Notched U" to notchedU)
}

/**
 * Debug-only playground for [CyberStrokeIcon]: pick a sample, then use the sliders for width
 * and thickness. Switches set the closed-loop alignment and a comet tracer that runs along the
 * outside edge of the thickened icon. The readout shows how close the stroke is to the stage's
 * space, the only limit. Open the preview in interactive mode, or run it on a device.
 */
@Composable
internal fun CyberStrokeIconPlayground() {
    var sample by remember { mutableStateOf(0) }
    var widthDp by remember { mutableFloatStateOf(160f) }
    var thicknessDp by remember { mutableFloatStateOf(8f) }
    var alignment by remember { mutableStateOf(CyberStrokeAlignment.Outside) }
    var tracer by remember { mutableStateOf(true) }
    var holes by remember { mutableStateOf(false) }

    val (name, centerline) = StrokeIconSamples.all[sample]
    val shape = remember(centerline, widthDp, thicknessDp, alignment) {
        CyberStrokeShape(centerline, widthDp.dp, thicknessDp.dp, alignment)
    }
    val status = CyberTheme.status

    Column(
        modifier = Modifier.background(CyberTheme.colors.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StrokeIconSamples.all.forEachIndexed { index, (label, _) ->
                Label(
                    text = if (index == sample) "[$label]" else label,
                    color = if (index == sample) CyberTheme.colors.primary else CyberTheme.colors.textSecondary,
                    modifier = Modifier.clickable(role = Role.Button) { sample = index }
                )
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val fit = with(LocalDensity.current) { shape.fit(this, maxWidth.toPx().toDouble(), StageHeight.toPx().toDouble()) }
            val load = fit?.let { StrokeLoad.of(it.load.toFloat()) }
            val loadColor = when (load) {
                StrokeLoad.Ok -> status.success.color
                StrokeLoad.Notice -> status.info.color
                StrokeLoad.Warning -> status.warning.color
                StrokeLoad.Limit, null -> status.danger.color
            }
            val maxDp = fit?.let { with(LocalDensity.current) { it.maxStrokeWidth.toFloat().toDp().value } }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.fillMaxWidth().height(StageHeight), contentAlignment = Alignment.Center) {
                    CyberStrokeIcon(
                        shape = shape,
                        modifier = Modifier.border(1.dp, CyberTheme.colors.border).let {
                            if (tracer) it.cyberPerimeter(CyberCometTracer(), shape, color = CyberTheme.colors.secondary, includeHoles = holes)
                            else it
                        },
                        contentDescription = name
                    )
                }
                Label(
                    text = when {
                        fit == null -> "Centerline doesn't fit the stage"
                        load == StrokeLoad.Limit -> "Thickness held at %.1f dp (stage limit)".format(maxDp)
                        else -> "Stroke load %s: %.0f%% of the stage".format(load, fit.load * 100)
                    },
                    color = loadColor
                )
            }
        }
        Setting("Centerline width", widthDp, 24f..320f, "%.0f dp") { widthDp = it }
        // The outline warns as the stroke passes reasonable thickness for the icon's width.
        val thicknessRatio = thicknessDp / widthDp
        val thicknessOutline = when {
            thicknessRatio >= ExcessiveThickness -> status.danger.color
            thicknessRatio >= HeavyThickness -> status.warning.color
            else -> CyberTheme.colors.border
        }
        Setting("Thickness", thicknessDp, 0.5f..96f, "%.1f dp", outline = thicknessOutline) { thicknessDp = it }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CyberStrokeAlignment.entries.forEach { option ->
                Label(
                    text = if (option == alignment) "[$option]" else option.name,
                    color = if (option == alignment) CyberTheme.colors.primary else CyberTheme.colors.textSecondary,
                    modifier = Modifier.clickable(role = Role.Button) { alignment = option }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Label("Tracer", CyberTheme.colors.textSecondary)
            CyberSwitch(checked = tracer, customA11y = "Tracer", onCheckedChange = { tracer = it })
            Label("Hole edges", CyberTheme.colors.textSecondary)
            CyberSwitch(checked = holes, customA11y = "Hole edges", onCheckedChange = { holes = it })
        }
    }
}

@Composable
private fun Setting(
    name: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    format: String,
    outline: Color? = null,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Label("$name: ${format.format(value)}", CyberTheme.colors.textSecondary)
        CyberSlider(
            value = value,
            valueRange = range,
            modifier = Modifier.fillMaxWidth().let { if (outline != null) it.border(1.dp, outline) else it },
            customA11y = name,
            onValueChange = onValueChange
        )
    }
}

@Composable
private fun Label(text: String, color: Color, modifier: Modifier = Modifier) {
    BasicText(text, modifier, style = CyberTheme.typography.terminal.copy(color = color))
}

// Stroke width ÷ centerline width where the thickness slider's outline turns yellow, then red.
private const val HeavyThickness = 0.10f
private const val ExcessiveThickness = 0.20f

// Tall enough to watch the icon grow before it reaches the stage edge.
private val StageHeight = 280.dp

@Preview(widthDp = 360, heightDp = 780)
@Composable
private fun CyberStrokeIconPlaygroundPreview() {
    CyberTheme { CyberStrokeIconPlayground() }
}
