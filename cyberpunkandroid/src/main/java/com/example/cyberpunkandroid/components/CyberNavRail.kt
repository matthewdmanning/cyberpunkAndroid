package com.example.cyberpunkandroid.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * One destination of a [CyberNavRail].
 *
 * @param label Short destination name shown under the icon.
 * @param iconRes Drawable resource of the destination icon (for example from
 *   [com.example.cyberpunkandroid.icons.CyberIcons]).
 * @param badgeCount Optional pending count shown with a [CyberCountBadge]. `0` or `null` shows no badge.
 */
@Immutable
data class CyberNavRailItem(
    val label: String,
    @param:DrawableRes val iconRes: Int,
    val badgeCount: Int? = null,
)

/**
 * Vertical navigation rail for the start edge of tablet, foldable, or landscape layouts.
 *
 * ### Visual and interactive behavior
 * - Each destination is an icon above a short uppercase label, in a slot of height [itemHeight].
 * - The selected destination is marked twice, so the selection does not depend on color alone: its slot gets the
 *   [selectedContainerColor] fill, and a straight indicator bar on the start edge slides to it.
 * - The indicator slide is the only motion. It moves with [indicatorAnimationSpec] when the selection changes, so
 *   the eye follows where the user went. It draws through `graphicsLayer`, so the slide does not recompose items.
 * - Icon and label colors cross-fade with [colorAnimationSpec].
 * - Destinations form a selectable group with [Role.Tab] semantics.
 *
 * @param items Destinations from top to bottom.
 * @param selectedIndex Index of the current destination, or `-1` for none.
 * @param modifier Modifier applied to the rail.
 * @param itemHeight Height of one destination slot. It is also the indicator's travel step.
 * @param indicatorWidth Thickness of the start-edge indicator bar.
 * @param iconSize Destination icon size.
 * @param indicatorAnimationSpec Motion of the indicator between slots.
 * @param colorAnimationSpec Cross-fade of icon and label colors.
 * @param containerColor Rail background.
 * @param selectedContainerColor Fill of the selected slot.
 * @param indicatorColor Indicator bar color.
 * @param selectedContentColor Icon and label color of the selected destination.
 * @param contentColor Icon and label color of the other destinations.
 * @param indicatorShape Outline of the indicator bar.
 * @param appendedA11y Optional accessibility label for the rail. Without it, each destination is read on its own.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 * @param onItemSelected Called with the index of the tapped destination.
 * @param header Optional content above the destinations, such as a menu button or a logo.
 */
@Composable
fun CyberNavRail(
    items: List<CyberNavRailItem>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    itemHeight: Dp = CyberTheme.spacing.dp64,
    indicatorWidth: Dp = CyberPrimitives.BorderWidths.dp4,
    iconSize: Dp = CyberPrimitives.IconSizes.dp24,
    indicatorAnimationSpec: AnimationSpec<Float> = spring(),
    colorAnimationSpec: AnimationSpec<Color> = tween(
        durationMillis = CyberPrimitives.Durations.ms150,
        easing = CyberConfig.Easings.OutExpoEasing
    ),
    containerColor: Color = CyberTheme.colors.surfaceSecondary,
    selectedContainerColor: Color = CyberTheme.colors.surfaceElevated,
    indicatorColor: Color = CyberTheme.colors.primary,
    selectedContentColor: Color = CyberTheme.colors.primary,
    contentColor: Color = CyberTheme.colors.textSecondary,
    indicatorShape: Shape = CyberTheme.shapes.roundedSm,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onItemSelected: (index: Int) -> Unit,
    header: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val hasSelection = selectedIndex in items.indices
    val animatedIndex by animateFloatAsState(
        targetValue = if (hasSelection) selectedIndex.toFloat() else 0f,
        animationSpec = indicatorAnimationSpec,
        label = "CyberNavRailIndicator"
    )

    Column(
        modifier = modifier
            .cyberComponentSemantics("CyberNavRail", appendedA11y, customA11y)
            .fillMaxHeight()
            // Every slot takes the width of the widest destination, so labels never wrap and slots line up.
            .width(IntrinsicSize.Max)
            .background(containerColor)
            .padding(vertical = CyberTheme.spacing.dp8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
    ) {
        header?.invoke(this)
        Box(modifier = Modifier.selectableGroup()) {
            Column {
                items.forEachIndexed { index, item ->
                    NavRailSlot(
                        item = item,
                        selected = index == selectedIndex,
                        itemHeight = itemHeight,
                        iconSize = iconSize,
                        colorAnimationSpec = colorAnimationSpec,
                        selectedContainerColor = selectedContainerColor,
                        selectedContentColor = selectedContentColor,
                        contentColor = contentColor,
                        onClick = { onItemSelected(index) }
                    )
                }
            }
            if (hasSelection) {
                Box(
                    modifier = Modifier
                        .width(indicatorWidth)
                        .height(itemHeight)
                        .graphicsLayer { translationY = itemHeight.toPx() * animatedIndex }
                        .background(indicatorColor, indicatorShape)
                )
            }
        }
    }
}

/**
 * One destination slot of a [CyberNavRail].
 *
 * @param item Destination data.
 * @param selected `true` when this is the current destination.
 * @param itemHeight Slot height.
 * @param iconSize Icon size.
 * @param colorAnimationSpec Cross-fade of icon and label colors.
 * @param selectedContainerColor Slot fill while selected.
 * @param selectedContentColor Icon and label color while selected.
 * @param contentColor Icon and label color while not selected.
 * @param onClick Called when the slot is tapped.
 */
@Composable
private fun NavRailSlot(
    item: CyberNavRailItem,
    selected: Boolean,
    itemHeight: Dp,
    iconSize: Dp,
    colorAnimationSpec: AnimationSpec<Color>,
    selectedContainerColor: Color,
    selectedContentColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = if (selected) selectedContentColor else contentColor,
        animationSpec = colorAnimationSpec,
        label = "CyberNavRailContent"
    )
    Column(
        modifier = Modifier
            .height(itemHeight)
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .background(if (selected) selectedContainerColor else Color.Transparent)
            .padding(horizontal = CyberTheme.spacing.dp12),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4, Alignment.CenterVertically)
    ) {
        Box {
            CyberIcon(iconRes = item.iconRes, contentDescription = null, size = iconSize, tint = tint)
            val count = item.badgeCount ?: 0
            if (count > 0) {
                // Center the badge on the icon's top-end corner, as Material badges do.
                CyberCountBadge(
                    count = count,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = CyberTheme.spacing.dp8, y = -CyberTheme.spacing.dp8)
                )
            }
        }
        Text(
            text = item.label.uppercase(),
            color = tint,
            style = CyberTheme.typography.terminal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
