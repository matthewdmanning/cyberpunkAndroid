package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberChangeFlash
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.effects.cyberCornerBrackets
import com.example.cyberpunkandroid.effects.rememberCyberChangePulse
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/** Direction of the change shown by a [CyberReadout] delta. */
enum class CyberTrend {
    /** The value went up. */
    Up,

    /** The value went down. */
    Down,

    /** The value did not change. */
    Flat,
}

/**
 * Whether a [CyberReadout] delta is good, bad, or neither. It is separate from [CyberTrend], because "up" is good
 * for throughput and bad for latency.
 */
enum class CyberDeltaSentiment {
    /** The change is good. Drawn in the success color. */
    Positive,

    /** The change is bad. Drawn in the danger color. */
    Negative,

    /** The change is neither good nor bad. Drawn in the secondary text color. */
    Neutral,
}

/**
 * Single key metric: a label, a large value with an optional unit, and an optional change from the previous
 * period. Use it for dashboard tiles and HUD readouts (signal strength, credits, latency).
 *
 * ### Visual behavior
 * - A rectangular panel in [containerColor], framed by four corner brackets ([cyberCornerBrackets]) in
 *   [frameColor]. The brackets mark the readout without a full box.
 * - [label] in uppercase terminal type, [value] in display type, [unit] in terminal type on the value's baseline.
 * - The optional delta row shows an arrow for [trend] and the [delta] text, colored by [deltaSentiment].
 * - When [value] changes, the value flashes once in [flashColor] ([cyberChangeFlash]), so the user sees which
 *   number updated. The first value does not flash. Set [highlightChanges] to `false` to turn this off.
 *
 * ### Accessibility
 * The readout merges into one node, read as label, value, unit, and delta. Set [announceChanges] to `true` to
 * make it a polite live region, so screen readers announce new values.
 *
 * @param label Metric name.
 * @param value Current value, already formatted.
 * @param modifier Modifier applied to the readout.
 * @param unit Optional unit shown after the value.
 * @param delta Optional formatted change, for example `"+3.2 %"`.
 * @param trend Direction of [delta]. Ignored when [delta] is `null`.
 * @param deltaSentiment Whether [delta] is good, bad, or neither.
 * @param highlightChanges `true` to flash the value once when it changes.
 * @param announceChanges `true` to announce new values with a polite live region.
 * @param frameStrokeWidth Stroke width of the corner brackets.
 * @param frameArmLength Arm length of each corner bracket.
 * @param trendIconSize Size of the trend arrow.
 * @param flashAnimationSpec Decay of the change flash.
 * @param containerColor Panel background.
 * @param frameColor Corner bracket color.
 * @param labelColor Label and unit color.
 * @param valueColor Value color.
 * @param flashColor Change flash color.
 * @param shape Panel outline. The default is a plain rectangle, so the corner brackets fit its corners.
 * @param appendedA11y Optional accessibility label. Without it, the visible text is read.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
@Composable
fun CyberReadout(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    delta: String? = null,
    trend: CyberTrend = CyberTrend.Flat,
    deltaSentiment: CyberDeltaSentiment = CyberDeltaSentiment.Neutral,
    highlightChanges: Boolean = true,
    announceChanges: Boolean = false,
    frameStrokeWidth: Dp = CyberPrimitives.BorderWidths.dp2,
    frameArmLength: Dp = CyberTheme.spacing.dp12,
    trendIconSize: Dp = CyberPrimitives.IconSizes.dp16,
    flashAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = CyberPrimitives.Durations.ms500,
        easing = CyberConfig.Easings.OutExpoEasing
    ),
    containerColor: Color = CyberTheme.colors.surfaceSecondary,
    frameColor: Color = CyberTheme.colors.border,
    labelColor: Color = CyberTheme.colors.textSecondary,
    valueColor: Color = CyberTheme.colors.textPrimary,
    flashColor: Color = CyberTheme.colors.primary,
    shape: Shape = CyberTheme.shapes.roundedSm,
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val flash by rememberCyberChangePulse(
        key = value,
        animationSpec = flashAnimationSpec,
        shouldPulse = { old, new -> highlightChanges && old != new }
    )

    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                if (announceChanges) liveRegion = LiveRegionMode.Polite
            }
            .cyberComponentSemantics("CyberReadout", appendedA11y, customA11y)
            .background(containerColor, shape)
            .cyberCornerBrackets(color = frameColor, strokeWidth = frameStrokeWidth, armLength = frameArmLength)
            .padding(CyberTheme.spacing.dp16),
        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)
    ) {
        Text(text = label.uppercase(), color = labelColor, style = CyberTheme.typography.terminal)
        Row(horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)) {
            Text(
                text = value,
                color = valueColor,
                style = CyberTheme.typography.display,
                maxLines = 1,
                modifier = Modifier
                    .alignByBaseline()
                    .cyberChangeFlash(pulse = { flash }, color = flashColor, shape = CyberTheme.shapes.roundedSm)
            )
            if (unit != null) {
                Text(
                    text = unit,
                    color = labelColor,
                    style = CyberTheme.typography.terminal,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
        if (delta != null) {
            val deltaColor = when (deltaSentiment) {
                CyberDeltaSentiment.Positive -> CyberTheme.status.success.color
                CyberDeltaSentiment.Negative -> CyberTheme.status.danger.color
                CyberDeltaSentiment.Neutral -> CyberTheme.colors.textSecondary
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)
            ) {
                trendIcon(trend)?.let { icon ->
                    CyberIcon(iconRes = icon, contentDescription = null, size = trendIconSize, tint = deltaColor)
                }
                Text(text = delta, color = deltaColor, style = CyberTheme.typography.terminal)
            }
        }
    }
}

/**
 * Returns the arrow icon for a trend, or `null` for a flat trend.
 *
 * @param trend Direction of the change.
 */
private fun trendIcon(trend: CyberTrend): Int? = when (trend) {
    CyberTrend.Up -> CyberIcons.ArrowUp
    CyberTrend.Down -> CyberIcons.ArrowDown
    CyberTrend.Flat -> null
}
