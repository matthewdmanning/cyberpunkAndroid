package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberChangeBump
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.effects.rememberCyberChangePulse
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Small numeric badge for unread or pending counts, placed on a navigation item or icon.
 *
 * ### Behavior
 * - A [count] of `0` or less draws nothing, so callers can always place the badge.
 * - Counts above [maxCount] show as `"<maxCount>+"`.
 * - A single digit gives a circle; more digits stretch it into a pill. The circle comes from the fully
 *   rounded [shape] and the square minimum size, not from a polygon.
 * - When the count **increases**, the badge bumps once ([cyberChangeBump]) to show that something new arrived.
 *   Decreases (the user read an item) do not animate. Set [bumpOnIncrease] to `false` to turn the bump off.
 *
 * @param count Number to show.
 * @param modifier Modifier applied to the badge.
 * @param maxCount Largest number shown in full.
 * @param minSize Smallest width and height. The default is the small inline icon size, so the badge lines up
 *   with icon grids.
 * @param bumpOnIncrease `true` to bump the badge once when [count] increases.
 * @param bumpScale Extra scale at the peak of the bump.
 * @param bumpAnimationSpec Decay of the bump.
 * @param containerColor Badge fill. Defaults to the danger status color, the Material convention for counts
 *   that need attention.
 * @param contentColor Number color, readable on [containerColor].
 * @param shape Badge outline.
 * @param textStyle Number style.
 * @param appendedA11y Optional accessibility label, for example `"3 unread messages"`. Without it, the number
 *   itself is read.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
@Composable
fun CyberCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    maxCount: Int = 99,
    minSize: Dp = CyberPrimitives.IconSizes.dp16,
    bumpOnIncrease: Boolean = true,
    bumpScale: Float = CyberConfig.Effects.ChangeBumpScale,
    bumpAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = CyberPrimitives.Durations.ms300,
        easing = CyberConfig.Easings.OutExpoEasing
    ),
    containerColor: Color = CyberTheme.status.danger.color,
    contentColor: Color = CyberTheme.status.danger.onColor,
    shape: Shape = CyberTheme.shapes.roundedFull,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val pulse = rememberCyberChangePulse(
        key = count,
        animationSpec = bumpAnimationSpec,
        shouldPulse = { old, new -> bumpOnIncrease && new > old }
    )
    if (count <= 0) return

    Box(
        modifier = modifier
            .cyberComponentSemantics("CyberCountBadge", appendedA11y, customA11y)
            .cyberChangeBump(pulse = { pulse.value }, scaleDelta = bumpScale)
            .defaultMinSize(minWidth = minSize, minHeight = minSize)
            .background(containerColor, shape)
            .padding(horizontal = CyberTheme.spacing.dp4),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = countBadgeText(count, maxCount),
            color = contentColor,
            style = textStyle,
            maxLines = 1
        )
    }
}

/**
 * Formats a badge count, capping it at [maxCount] with a trailing `+`.
 *
 * @param count Number to format.
 * @param maxCount Largest number shown in full.
 */
internal fun countBadgeText(count: Int, maxCount: Int): String =
    if (count > maxCount) "$maxCount+" else count.toString()
