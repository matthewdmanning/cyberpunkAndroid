package com.example.cyberpunkandroid.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * An uncontained, continuous waveform monitor (like an EKG or oscilloscope).
 * Renders an unadorned list of raw [data] points as a line graph.
 *
 * Exposes core semantic descriptions for accessibility and focuses strictly on
 * rendering the waveform, leaving the background and container decisions to the caller.
 */
@Composable
fun CyberBiometrics(
    data: List<Float>, // Values mapped between 0f and 1f
    modifier: Modifier = Modifier,
    strokeWidth: Float = CyberPrimitives.BorderWidths.dp2.value,
    contentDescriptionText: String = "Biometric data waveform",
    color: Color = CyberTheme.colors.primary,
    appendedA11y: String? = null,
    customA11y: String? = null
) {
    Spacer(
        modifier = modifier.cyberComponentSemantics("CyberBiometrics", appendedA11y, customA11y)
            .semantics {
                contentDescription = contentDescriptionText
            }
            .drawBehind {
                if (data.isEmpty()) return@drawBehind

                val path = Path()
                val stepX = size.width / (data.size - 1).coerceAtLeast(1).toFloat()

                data.forEachIndexed { index, value ->
                    // Normalize value to height. value = 1.0 -> y = 0, value = 0.0 -> y = height
                    val x = index * stepX
                    val y = size.height - (value.coerceIn(0f, 1f) * size.height)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
    )
}

internal fun biometricValueAt(data: List<Float>, position: Float): Float {
    if (data.isEmpty()) return 0f
    val wrapped = ((position % data.size) + data.size) % data.size
    val first = kotlin.math.floor(wrapped).toInt()
    val fraction = wrapped - first
    val a = data[first].takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 0f
    val b = data[(first + 1) % data.size].takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 0f
    return a + (b - a) * fraction
}
