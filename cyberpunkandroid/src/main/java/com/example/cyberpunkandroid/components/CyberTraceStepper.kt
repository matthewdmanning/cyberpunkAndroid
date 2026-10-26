package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberHoloBrushSource
import com.example.cyberpunkandroid.effects.CyberHoloPalette
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.effects.cyberHoloPalette
import com.example.cyberpunkandroid.effects.rememberCyberDriftPhase
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Compact step indicator drawn as one signal trace: the same job as [CyberStepper], with less text.
 *
 * ### Visual behavior
 * - A header row shows only the current step label (start) and a `2/4` counter (end). The other labels are not
 *   shown, so the indicator fits a narrow toolbar or card header.
 * - Under it runs one straight trace with a node per step, evenly spread across the width. Nodes are circles:
 *   - Completed: a small filled dot in [activeColor].
 *   - Current: a ring in [activeColor] inside a thinner outer reticle ring, like a lock-on marker.
 *   - Upcoming: a hollow ring in [inactiveColor].
 * - The completed part of the trace uses a holographic brush whose colors drift slowly; the rest is a
 *   [inactiveColor] line. When the step changes, the holographic part extends to the new node
 *   ([traceAnimationSpec]). Both are drawn in the draw phase only.
 *
 * ### Accessibility
 * One node: it reads as a progress bar with state "Step 2 of 4, Verify" (from [stepStateDescription]).
 *
 * @param steps Step labels, in order.
 * @param currentStep Index of the current step.
 * @param modifier Modifier applied to the indicator.
 * @param nodeSize Diameter of the current node's inner ring. The reticle ring is twice this size.
 * @param traceWidth Stroke width of the trace and rings.
 * @param driftCyclesPerSecond Holographic drift speed. `0f` makes the trace static.
 * @param traceAnimationSpec Extension of the holographic trace to a new step.
 * @param palette Holographic colors of the completed trace.
 * @param activeColor Completed and current node color.
 * @param inactiveColor Upcoming node and remaining trace color.
 * @param labelColor Current step label color.
 * @param counterColor Counter color.
 * @param stepStateDescription Builds the spoken state from the 1-based step number, the step count, and the label.
 * @param appendedA11y Optional accessibility label.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
@Composable
fun CyberTraceStepper(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier,
    nodeSize: Dp = CyberTheme.spacing.dp8,
    traceWidth: Dp = CyberPrimitives.BorderWidths.dp1,
    driftCyclesPerSecond: Float = CyberConfig.Effects.HoloDriftCyclesPerSecond,
    traceAnimationSpec: AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms300, easing = CyberConfig.Easings.CyberEasing),
    palette: CyberHoloPalette = cyberHoloPalette(),
    activeColor: Color = CyberTheme.colors.primary,
    inactiveColor: Color = CyberTheme.colors.border,
    labelColor: Color = CyberTheme.colors.textPrimary,
    counterColor: Color = CyberTheme.colors.textSecondary,
    stepStateDescription: (number: Int, count: Int, label: String) -> String = { n, c, l -> "Step $n of $c, $l" },
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    if (steps.isEmpty()) return
    val current = currentStep.coerceIn(0, steps.lastIndex)
    val progress by animateFloatAsState(current.toFloat(), traceAnimationSpec, label = "CyberTraceStepperTrace")
    val phase by rememberCyberDriftPhase(driftCyclesPerSecond)
    val count = steps.size

    Column(
        modifier = modifier
            .cyberComponentSemantics("CyberTraceStepper", appendedA11y, customA11y)
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo(current.toFloat(), 0f..(count - 1).toFloat(), steps = (count - 2).coerceAtLeast(0))
                stateDescription = stepStateDescription(current + 1, count, steps[current])
            }
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = steps[current].uppercase(), color = labelColor, style = CyberTheme.typography.terminal)
            Spacer(Modifier.weight(1f))
            Text(text = "${current + 1}/$count", color = counterColor, style = CyberTheme.typography.terminal)
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(nodeSize * 2) // The reticle ring is twice the inner ring.
                .drawWithCache {
                    val source = CyberHoloBrushSource()
                    val stroke = traceWidth.toPx()
                    val inner = nodeSize.toPx() / 2f
                    val reticle = inner * 2f
                    val y = size.height / 2f
                    val first = reticle
                    val last = size.width - reticle
                    val pitch = if (count > 1) (last - first) / (count - 1) else 0f
                    fun x(i: Float) = if (count > 1) first + pitch * i else size.width / 2f
                    onDrawBehind {
                        val headX = x(progress)
                        drawLine(inactiveColor, Offset(headX, y), Offset(x((count - 1).toFloat()), y), stroke)
                        if (progress > 0f) drawLine(source.brush(size, phase, palette), Offset(first, y), Offset(headX, y), stroke)
                        for (i in 0 until count) {
                            val center = Offset(x(i.toFloat()), y)
                            when {
                                i < current -> drawCircle(activeColor, inner / 2f, center)
                                i == current -> {
                                    drawCircle(activeColor, inner, center, style = Stroke(stroke * 2f))
                                    drawCircle(activeColor.copy(alpha = activeColor.alpha / 2f), reticle - stroke, center, style = Stroke(stroke))
                                }
                                else -> drawCircle(inactiveColor, inner, center, style = Stroke(stroke))
                            }
                        }
                    }
                }
        )
    }
}
