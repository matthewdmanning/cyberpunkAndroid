package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberSemantics
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A circular interface ring serving as a radial progress bar.
 * Unadorned base item that exposes styling slots and uses core semantic traits.
 *
 * @param progress Normalized radial progress value between `0.0f` and `1.0f`.
 * @param modifier Modifier applied to the rim graphics.
 * @param strokeWidth Width of the rim ring strokes.
 * @param color Color tint applied to the active progress sweep arc.
 * @param backgroundColor Color tint applied to the background track ring.
 * @param appendedA11y Optional text to append to the default component accessibility name.
 * @param customA11y Optional custom accessibility description that completely overrides the default name.
 */
@Composable
fun CyberRim(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Float = CyberPrimitives.BorderWidths.dp2.value,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Float> = androidx.compose.animation.core.spring(),
    color: Color = CyberTheme.colors.primary,
    backgroundColor: Color = CyberTheme.colors.surface,
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = animationSpec,
        label = "RimProgress"
    )

    Spacer(
        modifier = modifier
            .cyberSemantics("CyberRim", appendedA11y, customA11y)
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = progress.coerceIn(0f, 1f),
                    range = 0f..1f
                )
            }
            .drawBehind {
                val sweepAngle = animatedProgress * 360f
                val diameter = minOf(size.width, size.height)
                val topLeftOffset = Offset(
                    x = (size.width - diameter) / 2f,
                    y = (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)
                
                // Background Track
                drawArc(
                    color = backgroundColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeftOffset,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
                )
                
                // Foreground Progress
                if (sweepAngle > 0f) {
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeftOffset,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
                    )
                }
            }
    )
}

/**
 * Modifier for tactical long-press interactions.
 * Incrementally fills over [durationMillis] while held down.
 * Reverts to 0f if released early. Triggers [onComplete] when fully charged.
 */
fun Modifier.cyberLongPressFill(
    durationMillis: Long = 1500L,
    pollingDelayMillis: Long = 16L,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onProgressUpdate: (Float) -> Unit,
    onComplete: () -> Unit
): Modifier = this.pointerInput(Unit) {
    detectTapGestures(
        onPress = {
            var isPressed = true
            val startTime = System.currentTimeMillis()
            var completed = false

            kotlinx.coroutines.coroutineScope {
                val timerJob = launch {
                    while (isPressed && !completed) {
                        val elapsed = System.currentTimeMillis() - startTime
                        val progress = (elapsed.toFloat() / durationMillis).coerceIn(0f, 1f)
                        onProgressUpdate(progress)

                        if (progress >= 1f) {
                            completed = true
                            onComplete()
                        } else {
                            delay(pollingDelayMillis)
                        }
                    }
                }

                try {
                    awaitRelease()
                } catch (e: Exception) {
                    // Cancelled
                } finally {
                    isPressed = false
                    timerJob.cancel()
                    if (!completed) {
                        onProgressUpdate(0f)
                    }
                }
            }
        }
    )
}
