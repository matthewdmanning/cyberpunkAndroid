package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/** Progress state of one step in a [CyberStepper]. */
enum class CyberStepState {
    /** The step is done. */
    Completed,

    /** The user is on this step. */
    Current,

    /** The step is not reached yet. */
    Upcoming,
}

/**
 * Horizontal step indicator for multi-step flows (setup, checkout, a mission briefing).
 *
 * ### Visual behavior
 * - Each step is a circular node with a short label under it. Nodes are true circles ([CircleShape]).
 *   - Completed: filled with [activeColor] and marked with a check icon.
 *   - Current: outlined in [activeColor] on [nodeContainerColor], showing the step number.
 *   - Upcoming: outlined in [inactiveColor], showing the step number in [inactiveContentColor].
 * - A straight connector runs between node centers. The part behind the current step is drawn in [activeColor];
 *   the rest is drawn in [inactiveColor].
 * - When [currentStep] changes, the active part of the connector extends (or retracts) with
 *   [connectorAnimationSpec]. This is the only motion, and it shows the transition from one step to the next.
 *   The connector reads the animated value only in the draw phase.
 *
 * ### Interaction
 * When [onStepClick] is not `null`, completed steps become buttons, so the user can go back to them. Current and
 * upcoming steps never react to taps.
 *
 * @param steps Step labels, in order.
 * @param currentStep Index of the current step. Values past the last index mark every step completed.
 * @param modifier Modifier applied to the stepper row.
 * @param nodeSize Diameter of each node.
 * @param connectorWidth Stroke width of the connector.
 * @param nodeBorderWidth Outline width of current and upcoming nodes.
 * @param connectorAnimationSpec Motion of the active connector between steps.
 * @param activeColor Fill of completed nodes, outline of the current node, and the active connector.
 * @param onActiveColor Check icon color on completed nodes.
 * @param inactiveColor Outline of upcoming nodes and the inactive connector.
 * @param nodeContainerColor Fill of current and upcoming nodes.
 * @param activeContentColor Number and label color of the current step, and label color of completed steps.
 * @param inactiveContentColor Number and label color of upcoming steps.
 * @param stateDescriptions Spoken state for each [CyberStepState].
 * @param appendedA11y Optional accessibility label for the whole stepper. Without it, each step is read on its own.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 * @param onStepClick Called with the index of a tapped completed step. `null` makes the stepper display-only.
 */
@Composable
fun CyberStepper(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier,
    nodeSize: Dp = CyberPrimitives.IconSizes.dp24,
    connectorWidth: Dp = CyberPrimitives.BorderWidths.dp2,
    nodeBorderWidth: Dp = CyberPrimitives.BorderWidths.dp2,
    connectorAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = CyberPrimitives.Durations.ms300,
        easing = CyberConfig.Easings.CyberEasing
    ),
    activeColor: Color = CyberTheme.colors.primary,
    onActiveColor: Color = CyberTheme.colors.background,
    inactiveColor: Color = CyberTheme.colors.border,
    nodeContainerColor: Color = CyberTheme.colors.surfaceSecondary,
    activeContentColor: Color = CyberTheme.colors.textPrimary,
    inactiveContentColor: Color = CyberTheme.colors.textSecondary,
    stateDescriptions: Map<CyberStepState, String> = DefaultStepStateDescriptions,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onStepClick: ((index: Int) -> Unit)? = null,
) {
    if (steps.isEmpty()) return
    val progress = animateFloatAsState(
        targetValue = currentStep.coerceIn(0, steps.size).toFloat(),
        animationSpec = connectorAnimationSpec,
        label = "CyberStepperConnector"
    )

    Row(
        modifier = modifier
            .cyberComponentSemantics("CyberStepper", appendedA11y, customA11y)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        steps.forEachIndexed { index, label ->
            val state = stepState(index, currentStep)
            val clickable = onStepClick != null && state == CyberStepState.Completed
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        stateDescriptions[state]?.let { stateDescription = it }
                    }
                    .then(
                        if (clickable) Modifier.clickable(role = Role.Button) { onStepClick?.invoke(index) }
                        else Modifier
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(nodeSize)
                        .stepConnector(
                            index = index,
                            lastIndex = steps.lastIndex,
                            progress = progress,
                            strokeWidth = connectorWidth,
                            activeColor = activeColor,
                            inactiveColor = inactiveColor
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    StepNode(
                        number = index + 1,
                        state = state,
                        size = nodeSize,
                        borderWidth = nodeBorderWidth,
                        activeColor = activeColor,
                        onActiveColor = onActiveColor,
                        inactiveColor = inactiveColor,
                        containerColor = nodeContainerColor,
                        activeContentColor = activeContentColor,
                        inactiveContentColor = inactiveContentColor
                    )
                }
                Text(
                    text = label.uppercase(),
                    color = if (state == CyberStepState.Upcoming) inactiveContentColor else activeContentColor,
                    style = CyberTheme.typography.terminal,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Default spoken states of [CyberStepper] steps. */
val DefaultStepStateDescriptions: Map<CyberStepState, String> = mapOf(
    CyberStepState.Completed to "Completed",
    CyberStepState.Current to "Current step",
    CyberStepState.Upcoming to "Not started",
)

/**
 * Returns the progress state of a step.
 *
 * @param index Step index.
 * @param currentStep Index of the current step.
 */
internal fun stepState(index: Int, currentStep: Int): CyberStepState = when {
    index < currentStep -> CyberStepState.Completed
    index == currentStep -> CyberStepState.Current
    else -> CyberStepState.Upcoming
}

/**
 * Returns how much of the connector segment that starts at node [segment] is active, from `0f` to `1f`.
 *
 * @param segment Index of the node where the segment starts. The segment ends at node `segment + 1`.
 * @param progress Animated current-step value.
 */
internal fun connectorFill(segment: Int, progress: Float): Float = (progress - segment).coerceIn(0f, 1f)

/**
 * Draws the two connector halves that pass through one step cell: from the cell's start edge to its center
 * (second half of the previous segment) and from the center to its end edge (first half of the next segment).
 *
 * @param index Step index of this cell.
 * @param lastIndex Index of the last step. The first cell has no start half and the last cell has no end half.
 * @param progress Animated current-step value, read only while drawing.
 * @param strokeWidth Connector stroke width.
 * @param activeColor Color of the active part.
 * @param inactiveColor Color of the inactive part.
 */
private fun Modifier.stepConnector(
    index: Int,
    lastIndex: Int,
    progress: State<Float>,
    strokeWidth: Dp,
    activeColor: Color,
    inactiveColor: Color,
): Modifier = drawBehind {
    val y = size.height / 2f
    val center = size.width / 2f
    val stroke = strokeWidth.toPx()
    // Each half-cell covers half a segment, so a segment fill of f maps to 2f on its first half and 2f - 1 on its second.
    fun half(fromX: Float, toX: Float, fill: Float) {
        val split = fromX + (toX - fromX) * fill
        drawLine(inactiveColor, Offset(fromX, y), Offset(toX, y), stroke)
        if (fill > 0f) drawLine(activeColor, Offset(fromX, y), Offset(split, y), stroke)
    }
    val value = progress.value
    if (index > 0) half(0f, center, (connectorFill(index - 1, value) * 2f - 1f).coerceIn(0f, 1f))
    if (index < lastIndex) half(center, size.width, (connectorFill(index, value) * 2f).coerceIn(0f, 1f))
}

/**
 * Circular node of one step.
 *
 * @param number One-based step number shown on current and upcoming nodes.
 * @param state Progress state of the step.
 * @param size Node diameter.
 * @param borderWidth Outline width of current and upcoming nodes.
 * @param activeColor Completed fill and current outline.
 * @param onActiveColor Check icon color.
 * @param inactiveColor Upcoming outline.
 * @param containerColor Current and upcoming fill.
 * @param activeContentColor Current number color.
 * @param inactiveContentColor Upcoming number color.
 */
@Composable
private fun StepNode(
    number: Int,
    state: CyberStepState,
    size: Dp,
    borderWidth: Dp,
    activeColor: Color,
    onActiveColor: Color,
    inactiveColor: Color,
    containerColor: Color,
    activeContentColor: Color,
    inactiveContentColor: Color,
) {
    val base = Modifier.size(size)
    when (state) {
        CyberStepState.Completed -> Box(
            modifier = base.background(activeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CyberIcon(
                iconRes = CyberIcons.Check,
                contentDescription = null,
                size = CyberPrimitives.IconSizes.dp16,
                tint = onActiveColor
            )
        }
        else -> {
            val current = state == CyberStepState.Current
            Box(
                modifier = base
                    .background(containerColor, CircleShape)
                    .border(borderWidth, if (current) activeColor else inactiveColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = if (current) activeContentColor else inactiveContentColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
