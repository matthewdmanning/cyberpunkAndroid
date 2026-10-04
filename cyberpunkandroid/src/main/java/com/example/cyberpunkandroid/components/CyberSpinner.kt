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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

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


// --- Merged from source ---

/**
 * Full-screen or container-filling blocking loading overlay with frosted glass styling and centered [CyberSpinner].
 *
 * Imitates the Cybercore CSS `.cyber-spinner-overlay` specification.
 *
 * ### Interactive & Layout Behaviors:
 * - Fills parent bounds and intercepts all touch/pointer input, preventing interactions with underlying UI elements.
 * - Renders a high-opacity translucent backdrop tint with frosted glass aesthetics.
 * - Features a centered dual-ring counter-rotating [CyberSpinner] accompanied by optional uppercase monospace status text.
 * - Supports optional [onDismissRequest] callback triggered when tapping the overlay backdrop.
 *
 * @param modifier Composable modifier applied to the overlay container.
 * @param text Optional loading or progress status copy (e.g. `"INITIALIZING..."`, `"DECRYPTING DATA"`).
 * @param color Primary neon tint color for the centered [CyberSpinner]. Defaults to [CyberTheme.colors.primary].
 * @param onDismissRequest Optional callback invoked when the user taps the overlay backdrop.
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
@Composable
fun CyberSpinnerOverlay(
    modifier: Modifier = Modifier,
    text: String? = null,
    spinnerSize: androidx.compose.ui.unit.Dp = CyberPrimitives.IconSizes.dp32,
    color: Color = CyberTheme.colors.primary,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onDismissRequest: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.cyberComponentSemantics("CyberSpinnerOverlay", appendedA11y, customA11y)
            .fillMaxSize()
            .background(CyberPrimitives.Colors.Void500.copy(alpha = 0.90f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = if (onDismissRequest != null) Role.Button else null,
                onClick = { onDismissRequest?.invoke() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(CyberPrimitives.Spacing.dp16)
        ) {
            CyberSpinner(color = color, size = spinnerSize)

            if (text != null) {
                Spacer(modifier = Modifier.height(CyberPrimitives.Spacing.dp16))
                Text(
                    text = text.uppercase(),
                    style = CyberTheme.typography.terminal.copy(
                        color = CyberTheme.colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Layout wrapper that conditionally displays a blocking frosted glass [CyberSpinnerOverlay] over child [content].
 *
 * ### Interactive Behaviors:
 * - Wraps underlying composable [content] in a relative layout container.
 * - Smoothly fades [CyberSpinnerOverlay] in and out using [AnimatedVisibility] based on [visible].
 * - When [visible] is `true`, all touch/pointer gestures to [content] are blocked.
 *
 * @param visible Controls whether the loading overlay is currently active and visible.
 * @param modifier Modifier applied to the outer layout container.
 * @param text Optional monospace status description displayed beneath the spinner.
 * @param color Primary neon tint color for the centered spinner. Defaults to [CyberTheme.colors.primary].
 * @param onDismissRequest Optional callback invoked when tapping the backdrop.
 * @param content Base composable content obscured by the loading overlay while active.
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
@Composable
fun CyberSpinnerOverlay(
    visible: Boolean,
    text: String? = null,
    modifier: Modifier = Modifier,
    spinnerSize: androidx.compose.ui.unit.Dp = CyberPrimitives.IconSizes.dp32,
    enter: androidx.compose.animation.EnterTransition = fadeIn(),
    exit: androidx.compose.animation.ExitTransition = fadeOut(),
    color: Color = CyberTheme.colors.primary,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.cyberComponentSemantics("CyberSpinnerOverlay", appendedA11y, customA11y)) {
        content()

        AnimatedVisibility(
            visible = visible,
            enter = enter,
            exit = exit
        ) {
            CyberSpinnerOverlay(
                text = text,
                color = color,
                spinnerSize = spinnerSize,
                onDismissRequest = onDismissRequest,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}