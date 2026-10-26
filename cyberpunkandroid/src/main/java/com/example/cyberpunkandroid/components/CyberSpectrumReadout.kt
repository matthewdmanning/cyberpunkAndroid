package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberChromaEchoText
import com.example.cyberpunkandroid.effects.CyberHoloBrushSource
import com.example.cyberpunkandroid.effects.CyberHoloPalette
import com.example.cyberpunkandroid.effects.CyberTickSide
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.effects.cyberHoloPalette
import com.example.cyberpunkandroid.effects.cyberTickEdge
import com.example.cyberpunkandroid.effects.rememberCyberChangePulse
import com.example.cyberpunkandroid.effects.rememberCyberDriftPhase
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Key metric shown as an instrument reading: the same job as [CyberReadout], without a panel.
 *
 * ### Visual behavior
 * - No container. The label sits above the value; the unit sits on the value's baseline.
 * - Under the value runs a holographic baseline (drifting colors) with a tick ruler hanging from it
 *   ([cyberTickEdge]), like the scale of a measuring instrument. The baseline is as wide as the reading.
 * - When [value] changes, cyan and magenta echoes of the digits split away and converge back into them
 *   ([CyberChromaEchoText]), like a display re-locking its signal. The first value does not echo.
 * - The optional delta row uses the same sentiment colors as [CyberReadout].
 *
 * ### Accessibility
 * One merged node. [announceChanges] adds a polite live region.
 *
 * @param label Metric name.
 * @param value Current value, already formatted.
 * @param modifier Modifier applied to the readout.
 * @param unit Optional unit.
 * @param delta Optional formatted change.
 * @param trend Direction of [delta].
 * @param deltaSentiment Whether [delta] is good, bad, or neither.
 * @param echoOnChange `true` to play the chromatic echo when [value] changes.
 * @param announceChanges `true` to announce new values with a polite live region.
 * @param baselineWidth Stroke width of the holographic baseline.
 * @param driftCyclesPerSecond Holographic drift speed. `0f` makes the baseline static.
 * @param echoAnimationSpec Decay of the chromatic echo.
 * @param palette Holographic colors of the baseline.
 * @param labelColor Label, unit, and tick color.
 * @param valueColor Value color.
 * @param appendedA11y Optional accessibility label.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
@Composable
fun CyberSpectrumReadout(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    delta: String? = null,
    trend: CyberTrend = CyberTrend.Flat,
    deltaSentiment: CyberDeltaSentiment = CyberDeltaSentiment.Neutral,
    echoOnChange: Boolean = true,
    announceChanges: Boolean = false,
    baselineWidth: Dp = CyberPrimitives.BorderWidths.dp1,
    driftCyclesPerSecond: Float = CyberConfig.Effects.HoloDriftCyclesPerSecond,
    echoAnimationSpec: AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms300, easing = CyberConfig.Easings.OutExpoEasing),
    palette: CyberHoloPalette = cyberHoloPalette(),
    labelColor: Color = CyberTheme.colors.textSecondary,
    valueColor: Color = CyberTheme.colors.textPrimary,
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val pulse by rememberCyberChangePulse(
        key = value,
        animationSpec = echoAnimationSpec,
        shouldPulse = { old, new -> echoOnChange && old != new }
    )
    val phase by rememberCyberDriftPhase(driftCyclesPerSecond)

    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) { if (announceChanges) liveRegion = LiveRegionMode.Polite }
            .cyberComponentSemantics("CyberSpectrumReadout", appendedA11y, customA11y)
            .width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)
    ) {
        Text(text = label.uppercase(), color = labelColor, style = CyberTheme.typography.terminal)
        Row(horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)) {
            CyberChromaEchoText(
                text = value,
                pulse = { pulse },
                color = valueColor,
                modifier = Modifier.alignByBaseline()
            )
            if (unit != null) {
                Text(text = unit, color = labelColor, style = CyberTheme.typography.terminal, modifier = Modifier.alignByBaseline())
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(CyberTheme.spacing.dp8) // Room for the major ticks.
                .cyberTickEdge(color = labelColor, side = CyberTickSide.Top)
                .drawWithCache {
                    val source = CyberHoloBrushSource()
                    val stroke = baselineWidth.toPx()
                    onDrawBehind {
                        drawLine(source.brush(size, phase, palette), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                    }
                }
        )
        if (delta != null) {
            val deltaColor = when (deltaSentiment) {
                CyberDeltaSentiment.Positive -> CyberTheme.status.success.color
                CyberDeltaSentiment.Negative -> CyberTheme.status.danger.color
                CyberDeltaSentiment.Neutral -> CyberTheme.colors.textSecondary
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)) {
                when (trend) {
                    CyberTrend.Up -> CyberIcons.ArrowUp
                    CyberTrend.Down -> CyberIcons.ArrowDown
                    CyberTrend.Flat -> null
                }?.let { CyberIcon(iconRes = it, contentDescription = null, size = CyberPrimitives.IconSizes.dp16, tint = deltaColor) }
                Text(text = delta, color = deltaColor, style = CyberTheme.typography.terminal)
            }
        }
    }
}
