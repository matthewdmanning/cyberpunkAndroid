package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberOverload
import com.example.cyberpunkandroid.effects.cyberTextGlow
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.floor

/**
 * Uncontained, continuously scrolling EKG-style line. Values are normalized to 0..1;
 * only segments crossing [criticalThreshold] glow yellow or red and overload.
 * The caller supplies size and any background through [modifier].
 */
@Composable
fun CyberBiometrics(
    data: List<Float>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = CyberPrimitives.BorderWidths.dp2.value,
    contentDescriptionText: String = "Biometric data waveform",
    // Peaks above this normalized value enter the warning band.
    criticalThreshold: Float = 0.85f,
    glowRadius: Dp = CyberPrimitives.Spacing.dp8,
    // One sweep through the supplied samples per 1.5 seconds keeps the waveform visibly moving.
    scrollAnimationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(1500, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    overloadAnimationSpec: AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms300),
    color: Color = CyberTheme.colors.primary,
    warningColor: Color = CyberTheme.semantics.colors.warning,
    criticalColor: Color = CyberTheme.semantics.colors.danger,
    appendedA11y: String? = null,
    customA11y: String? = null
) {
    val phase by rememberInfiniteTransition(label = "BiometricScroll").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = scrollAnimationSpec,
        label = "BiometricPhase"
    )
    val threshold = criticalThreshold.coerceIn(0f, 1f)
    val hasSpike = data.any { it.isFinite() && it >= threshold }
    val description = customA11y ?: listOfNotNull(contentDescriptionText, appendedA11y)
        .joinToString(" ")

    // A doubled contour glow keeps the uncontained line legible over busy backgrounds.
    val glowIntensity = 2f
    Box(modifier = modifier.clearAndSetSemantics { contentDescription = description }) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .cyberTextGlow(color = color, radius = glowRadius, intensity = glowIntensity)
                .drawBehind {
                    if (data.size < 2) return@drawBehind
                    val dx = size.width / (data.size - 1)
                    for (index in 0 until data.lastIndex) {
                        val start = biometricValueAt(data, index + phase * data.size)
                        val end = biometricValueAt(data, index + 1 + phase * data.size)
                        drawLine(
                            color = color,
                            start = Offset(index * dx, size.height * (1f - start)),
                            end = Offset((index + 1) * dx, size.height * (1f - end)),
                            strokeWidth = strokeWidth.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
        )
        if (hasSpike) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .cyberOverload(enabled = true, animationSpec = overloadAnimationSpec)
                    .cyberTextGlow(color = warningColor, radius = glowRadius, intensity = glowIntensity)
                    .drawBehind {
                        if (data.size < 2) return@drawBehind
                        val dx = size.width / (data.size - 1)
                        for (index in 0 until data.lastIndex) {
                            val start = biometricValueAt(data, index + phase * data.size)
                            val end = biometricValueAt(data, index + 1 + phase * data.size)
                            val peak = maxOf(start, end)
                            if (peak >= threshold) {
                                drawLine(
                                    color = if (peak >= (1f + threshold) / 2f) criticalColor else warningColor,
                                    start = Offset(index * dx, size.height * (1f - start)),
                                    end = Offset((index + 1) * dx, size.height * (1f - end)),
                                    strokeWidth = strokeWidth.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
            )
        }
    }
}

internal fun biometricValueAt(data: List<Float>, position: Float): Float {
    if (data.isEmpty()) return 0f
    val wrapped = ((position % data.size) + data.size) % data.size
    val first = floor(wrapped).toInt()
    val fraction = wrapped - first
    val a = data[first].takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 0f
    val b = data[(first + 1) % data.size].takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 0f
    return a + (b - a) * fraction
}
