package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberSemantics
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Dual-ring counter-rotating sci-fi loading spinner with segmented arcs.
 *
 * Implements the Cybercore CSS `.cyber-spinner` specification with dual concentric arc tracks rotating in opposite directions.
 *
 * @param modifier Composable modifier applied to the spinner graphics.
 * @param color Neon accent tint applied to the outer spinning segments. Defaults to [CyberTheme.colors.primary].
 * @param segments Number of discrete arc segments rendered per ring track. Defaults to `3`.
 * @param appendedA11y Optional text to append to the default component accessibility name.
 * @param customA11y Optional custom accessibility description that completely overrides the default name.
 */
@Composable
fun CyberSpinner(
    modifier: Modifier = Modifier,
    segments: Int = 3,
    size: androidx.compose.ui.unit.Dp = CyberPrimitives.IconSizes.dp32,
    animationSpec: androidx.compose.animation.core.InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(1500, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    color: Color = CyberTheme.colors.primary,
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpinnerTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = animationSpec,
        label = "RotationAnimation"
    )

    Spacer(
        modifier = modifier
            .cyberSemantics("CyberSpinner", appendedA11y, customA11y)
            .size(size)
            .drawBehind {
                val strokeWidth = CyberPrimitives.BorderWidths.dp2.toPx()
                val sweepAngle = (360f / segments) * 0.6f

                withTransform(
                    transformBlock = { rotate(rotation) }
                ) {
                    for (i in 0 until segments) {
                        val startAngle = i * (360f / segments)
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
                        )
                    }
                }
                
                // Inner segmented ring
                withTransform(
                    transformBlock = { rotate(-rotation * 1.5f) }
                ) {
                    val innerSegments = 4
                    val innerSweepAngle = (360f / innerSegments) * 0.4f
                    val graphicsSize = this.size
                    for (i in 0 until innerSegments) {
                        val startAngle = i * (360f / innerSegments)
                        drawArc(
                            color = color.copy(alpha = 0.5f),
                            startAngle = startAngle,
                            sweepAngle = innerSweepAngle,
                            useCenter = false,
                            style = Stroke(width = CyberPrimitives.BorderWidths.dp1.toPx(), cap = StrokeCap.Square),
                            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth * 2, strokeWidth * 2),
                            size = Size(graphicsSize.width - (strokeWidth * 4), graphicsSize.height - (strokeWidth * 4))
                        )
                    }
                }
            }
    )
}
