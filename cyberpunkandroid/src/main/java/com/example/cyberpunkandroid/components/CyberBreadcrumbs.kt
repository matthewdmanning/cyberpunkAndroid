package com.example.cyberpunkandroid.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/** Text shown in place of the hidden middle segments of a collapsed [CyberBreadcrumbs] trail. */
private const val CollapsedMarker = "…"

/**
 * Horizontal location trail that shows where the current screen sits in a hierarchy, for example
 * `ROOT › SECTOR 7 › NODE 42`.
 *
 * ### Behavior
 * - The last segment is the current location. It is drawn in [currentColor] and is not clickable. Screen
 *   readers announce it with [currentStateDescription].
 * - Every earlier segment is a link drawn in [linkColor]. A tap calls [onSegmentClick] with its index, so the
 *   caller can navigate up to that level.
 * - When there are more than [maxVisibleSegments] segments, the trail keeps the first segment and the last
 *   `maxVisibleSegments - 1` segments and replaces the middle with a `…` marker. Tapping the marker expands the
 *   full trail.
 * - Each link keeps the minimum interactive touch size from Material 3, even though the text is small.
 *
 * The component has no animation and no effect: a location trail must be read at a glance.
 *
 * @param segments Labels from the root (index 0) to the current location (last index).
 * @param modifier Modifier applied to the trail row.
 * @param maxVisibleSegments Largest number of segments shown before the middle collapses. Values below 2 are
 *   treated as 2 (the root and the current location).
 * @param separatorSize Size of the default chevron separator.
 * @param linkColor Text color of the clickable ancestor segments.
 * @param currentColor Text color of the current location.
 * @param separatorColor Tint of the default separator.
 * @param currentStateDescription Spoken state of the current location.
 * @param expandLabel Spoken action label of the collapsed `…` marker.
 * @param appendedA11y Optional accessibility label for the whole trail. Without it, each segment is read on its own.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 * @param onSegmentClick Called with the index of the tapped ancestor segment.
 * @param separator Content drawn between two segments. Defaults to a right chevron icon.
 */
@Composable
fun CyberBreadcrumbs(
    segments: List<String>,
    modifier: Modifier = Modifier,
    maxVisibleSegments: Int = 4,
    separatorSize: Dp = CyberPrimitives.IconSizes.dp16,
    linkColor: Color = CyberTheme.colors.textSecondary,
    currentColor: Color = CyberTheme.colors.primary,
    separatorColor: Color = CyberTheme.colors.border,
    currentStateDescription: String = "Current location",
    expandLabel: String = "Show full path",
    appendedA11y: String? = null,
    customA11y: String? = null,
    onSegmentClick: (index: Int) -> Unit,
    separator: @Composable () -> Unit = {
        CyberIcon(
            iconRes = CyberIcons.ChevronRight,
            contentDescription = null,
            size = separatorSize,
            tint = separatorColor
        )
    },
) {
    if (segments.isEmpty()) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    val visible = breadcrumbSlots(segments.size, maxVisibleSegments.coerceAtLeast(2), expanded)
    val lastIndex = segments.lastIndex

    Row(
        modifier = modifier.cyberComponentSemantics("CyberBreadcrumbs", appendedA11y, customA11y),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)
    ) {
        visible.forEachIndexed { slot, index ->
            if (slot > 0) separator()
            when {
                index == null -> BreadcrumbText(
                    text = CollapsedMarker,
                    color = linkColor,
                    modifier = Modifier.clickable(
                        onClickLabel = expandLabel,
                        role = Role.Button,
                        onClick = { expanded = true }
                    )
                )
                index == lastIndex -> BreadcrumbText(
                    text = segments[index],
                    color = currentColor,
                    modifier = Modifier.semantics { stateDescription = currentStateDescription }
                )
                else -> BreadcrumbText(
                    text = segments[index],
                    color = linkColor,
                    modifier = Modifier.clickable(role = Role.Button, onClick = { onSegmentClick(index) })
                )
            }
        }
    }
}

/**
 * Text cell of one breadcrumb, padded to the minimum touch size.
 *
 * @param text Segment label.
 * @param color Label color.
 * @param modifier Modifier that carries the click or state semantics of this cell.
 */
@Composable
private fun BreadcrumbText(text: String, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .padding(horizontal = CyberTheme.spacing.dp2),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            style = CyberTheme.typography.terminal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Returns the segment index shown in each visible slot, with `null` for the collapsed marker.
 *
 * @param count Total number of segments.
 * @param maxVisible Largest number of segments to show while collapsed (at least 2).
 * @param expanded `true` to show every segment.
 */
internal fun breadcrumbSlots(count: Int, maxVisible: Int, expanded: Boolean): List<Int?> {
    if (expanded || count <= maxVisible) return (0 until count).toList()
    val tail = maxVisible - 1
    return listOf<Int?>(0, null) + ((count - tail) until count).toList()
}
