package com.example.cyberpunkandroid.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberHoloBrushSource
import com.example.cyberpunkandroid.effects.CyberHoloPalette
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.effects.cyberHoloPalette
import com.example.cyberpunkandroid.effects.rememberCyberDriftPhase
import com.example.cyberpunkandroid.effects.rememberCyberEcho
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Bottom navigation bar where the selection is a projector beam: the same job as [CyberNavRail], laid out for
 * phone portrait screens.
 *
 * ### Visual behavior
 * - Destinations share the width equally. Each is an icon above a short uppercase label.
 * - Above the selected destination, a short emitter line in a holographic brush lights the top edge, and a soft
 *   [beamColor] gradient falls from it behind the icon, like a hologram projected down from the edge.
 * - When the selection changes, the beam slides to the new destination ([indicatorAnimationSpec]) and leaves an
 *   afterimage at the old one that fades out ([rememberCyberEcho], [echoFadeSpec]). The echo shows where the user
 *   came from. Both run in the draw phase only.
 * - The emitter colors drift slowly ([driftCyclesPerSecond]); `0f` makes them static.
 * - A hairline in [dividerColor] separates the bar from the content above.
 *
 * Destinations form a selectable group with [Role.Tab] semantics.
 *
 * @param items Destinations from start to end. [CyberNavRailItem.badgeCount] shows a [CyberCountBadge].
 * @param selectedIndex Index of the current destination, or `-1` for none.
 * @param modifier Modifier applied to the bar.
 * @param height Bar height.
 * @param iconSize Destination icon size.
 * @param emitterWidth Thickness of the top emitter line.
 * @param emitterFraction Width of the emitter and beam as a fraction of one destination's width.
 * @param driftCyclesPerSecond Holographic drift speed of the emitter.
 * @param indicatorAnimationSpec Slide of the beam between destinations.
 * @param echoFadeSpec Fade of the afterimage.
 * @param colorAnimationSpec Cross-fade of icon and label colors.
 * @param palette Emitter colors.
 * @param beamColor Beam gradient color.
 * @param containerColor Bar background.
 * @param dividerColor Top hairline color.
 * @param selectedContentColor Icon and label color of the selected destination.
 * @param contentColor Icon and label color of the other destinations.
 * @param appendedA11y Optional accessibility label for the bar.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 * @param onItemSelected Called with the index of the tapped destination.
 */
@Composable
fun CyberBeamNavBar(
    items: List<CyberNavRailItem>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    height: Dp = CyberTheme.spacing.dp64,
    iconSize: Dp = CyberPrimitives.IconSizes.dp24,
    emitterWidth: Dp = CyberPrimitives.BorderWidths.dp2,
    emitterFraction: Float = 0.6f, // Narrower than the slot, so the beam reads as a projector, not a tab fill.
    driftCyclesPerSecond: Float = CyberConfig.Effects.HoloDriftCyclesPerSecond,
    indicatorAnimationSpec: AnimationSpec<Float> = spring(),
    echoFadeSpec: AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms500, easing = CyberConfig.Easings.OutExpoEasing),
    colorAnimationSpec: AnimationSpec<Color> = tween(CyberPrimitives.Durations.ms150, easing = CyberConfig.Easings.OutExpoEasing),
    palette: CyberHoloPalette = cyberHoloPalette(),
    beamColor: Color = CyberTheme.colors.primary,
    containerColor: Color = CyberTheme.colors.surfaceSecondary,
    dividerColor: Color = CyberTheme.colors.border,
    selectedContentColor: Color = CyberTheme.colors.primary,
    contentColor: Color = CyberTheme.colors.textSecondary,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onItemSelected: (index: Int) -> Unit,
) {
    if (items.isEmpty()) return
    val hasSelection = selectedIndex in items.indices
    val target = if (hasSelection) selectedIndex.toFloat() else 0f
    val position by animateFloatAsState(target, indicatorAnimationSpec, label = "CyberBeamNavBarIndicator")
    val echo = rememberCyberEcho(target, echoFadeSpec)
    val phase by rememberCyberDriftPhase(driftCyclesPerSecond)
    val beamAlpha = CyberConfig.Effects.BeamAlpha

    Row(
        modifier = modifier
            .cyberComponentSemantics("CyberBeamNavBar", appendedA11y, customA11y)
            .fillMaxWidth()
            .height(height)
            .background(containerColor)
            .drawWithCache {
                val source = CyberHoloBrushSource()
                val slot = size.width / items.size
                val beamWidth = slot * emitterFraction.coerceIn(0f, 1f)
                val emitterPx = emitterWidth.toPx()
                val hairline = CyberPrimitives.BorderWidths.dp1.toPx()

                /** Draws a beam and its emitter centered on destination [at], at opacity [alpha]. */
                fun drawBeam(scope: DrawScope, at: Float, alpha: Float) = with(scope) {
                    val left = slot * at + (slot - beamWidth) / 2f
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(beamColor.copy(alpha = beamAlpha * alpha), Color.Transparent),
                            startY = 0f,
                            endY = size.height
                        ),
                        topLeft = Offset(left, 0f),
                        size = Size(beamWidth, size.height)
                    )
                    drawRect(
                        brush = source.brush(size, phase, palette, alpha = alpha),
                        topLeft = Offset(left, 0f),
                        size = Size(beamWidth, emitterPx)
                    )
                }
                onDrawBehind {
                    drawRect(dividerColor, size = Size(size.width, hairline))
                    if (!hasSelection) return@onDrawBehind
                    if (echo.alpha > 0f) drawBeam(this, echo.position, echo.alpha)
                    drawBeam(this, position, 1f)
                }
            }
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            val tint by animateColorAsState(
                targetValue = if (selected) selectedContentColor else contentColor,
                animationSpec = colorAnimationSpec,
                label = "CyberBeamNavBarContent"
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onItemSelected(index) }),
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
    }
}
