package com.example.cyberpunkandroid.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberHoloBrushSource
import com.example.cyberpunkandroid.effects.CyberHoloPalette
import com.example.cyberpunkandroid.effects.LocalCyberHoloUseShader
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.effects.cyberHoloPalette
import com.example.cyberpunkandroid.effects.rememberCyberDriftPhase
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Location trail drawn as a circuit trace: the same job as [CyberBreadcrumbs], in a holographic style.
 *
 * ### Visual behavior
 * - Segment labels sit side by side with no separators. Under them runs one continuous trace line, with a node
 *   under the start of each segment, like contacts on a circuit board.
 * - The trace is drawn with a holographic brush whose colors drift slowly ([rememberCyberDriftPhase]). Because
 *   one brush spans the whole row, the colors flow across segments without seams.
 * - Ancestor nodes are small filled dots in [nodeColor]. The current node is a holographic ring, and the trace
 *   ends there. This marks "you are here" by geometry, not by color alone.
 * - Ancestor labels are [linkColor]; the current label is [currentColor].
 *
 * ### Behavior
 * Same as [CyberBreadcrumbs]: ancestors are buttons, the current location is not, and the middle collapses to
 * `…` past [maxVisibleSegments].
 *
 * @param segments Labels from the root (index 0) to the current location (last index).
 * @param modifier Modifier applied to the trail.
 * @param maxVisibleSegments Largest number of segments shown before the middle collapses (at least 2).
 * @param traceWidth Stroke width of the trace and of the current node ring.
 * @param nodeSize Diameter of the current node ring. Ancestor dots use half of it.
 * @param driftCyclesPerSecond Holographic drift speed. `0f` makes the trace static.
 * @param palette Holographic colors of the trace and current node.
 * @param linkColor Ancestor label color.
 * @param currentColor Current label color.
 * @param nodeColor Ancestor dot color.
 * @param currentStateDescription Spoken state of the current location.
 * @param expandLabel Spoken action label of the `…` marker.
 * @param appendedA11y Optional accessibility label for the whole trail.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 * @param onSegmentClick Called with the index of the tapped ancestor segment.
 */
@Composable
fun CyberTraceBreadcrumbs(
    segments: List<String>,
    modifier: Modifier = Modifier,
    maxVisibleSegments: Int = 4,
    traceWidth: Dp = CyberPrimitives.BorderWidths.dp1,
    nodeSize: Dp = CyberTheme.spacing.dp8,
    driftCyclesPerSecond: Float = CyberConfig.Effects.HoloDriftCyclesPerSecond,
    palette: CyberHoloPalette = cyberHoloPalette(),
    linkColor: Color = CyberTheme.colors.textSecondary,
    currentColor: Color = CyberTheme.colors.textPrimary,
    nodeColor: Color = CyberTheme.colors.border,
    currentStateDescription: String = "Current location",
    expandLabel: String = "Show full path",
    appendedA11y: String? = null,
    customA11y: String? = null,
    onSegmentClick: (index: Int) -> Unit,
) {
    if (segments.isEmpty()) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    val slots = breadcrumbSlots(segments.size, maxVisibleSegments.coerceAtLeast(2), expanded)
    val phase by rememberCyberDriftPhase(driftCyclesPerSecond)
    val useShader = LocalCyberHoloUseShader.current
    // Start x of each visible slot, written in placement and read only while drawing the trace.
    val slotStarts = remember(slots) { mutableStateMapOf<Int, Float>() }
    val cellPadding = CyberTheme.spacing.dp4
    val traceGap = CyberTheme.spacing.dp4

    Row(
        modifier = modifier
            .cyberComponentSemantics("CyberTraceBreadcrumbs", appendedA11y, customA11y)
            .drawWithCache {
                val source = CyberHoloBrushSource(useShader)
                val ringRadius = nodeSize.toPx() / 2f
                val dotRadius = ringRadius / 2f
                val stroke = traceWidth.toPx()
                val y = size.height - ringRadius - stroke
                val inset = cellPadding.toPx() + ringRadius
                onDrawBehind {
                    val xs = slots.indices.mapNotNull { slot -> slotStarts[slot]?.plus(inset) }
                    if (xs.size != slots.size) return@onDrawBehind
                    val brush = source.brush(size, phase, palette)
                    val endX = xs.last()
                    drawLine(brush, Offset(xs.first(), y), Offset(endX - ringRadius, y), stroke)
                    xs.dropLast(1).forEach { x -> drawCircle(nodeColor, dotRadius, Offset(x, y)) }
                    drawCircle(brush, ringRadius - stroke / 2f, Offset(endX, y), style = Stroke(stroke))
                }
            },
        verticalAlignment = Alignment.Top
    ) {
        slots.forEachIndexed { slot, index ->
            val isCurrent = index == segments.lastIndex
            val cellModifier = when {
                index == null -> Modifier.clickable(onClickLabel = expandLabel, role = Role.Button) { expanded = true }
                isCurrent -> Modifier.semantics(mergeDescendants = true) { stateDescription = currentStateDescription }
                else -> Modifier.clickable(role = Role.Button) { onSegmentClick(index) }
            }
            Box(
                modifier = cellModifier
                    .onPlaced { slotStarts[slot] = it.positionInParent().x }
                    .minimumInteractiveComponentSize()
                    // Leave room under the label for the trace: the node diameter plus a gap.
                    .padding(start = cellPadding, end = cellPadding, bottom = nodeSize + traceGap),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (index == null) "…" else segments[index].uppercase(),
                    color = if (isCurrent) currentColor else linkColor,
                    style = CyberTheme.typography.terminal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
