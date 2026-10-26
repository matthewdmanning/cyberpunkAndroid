package com.example.cyberpunkandroid.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberHoloBrushSource
import com.example.cyberpunkandroid.effects.CyberHoloPalette
import com.example.cyberpunkandroid.effects.LocalCyberHoloUseShader
import com.example.cyberpunkandroid.effects.cyberHoloPalette
import com.example.cyberpunkandroid.effects.rememberCyberDriftPhase
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.PI
import kotlin.math.min

/** Degrees in a full turn. */
private const val FullTurn = 360f

/** Start angle of the ring: 12 o'clock, because Compose arcs start at 3 o'clock. */
private const val TwelveOClock = -90f

/**
 * Circular countdown ring: the same job as [CyberTimeoutBar], for a compact spot (a code expiry, a hold timer)
 * where a centered readout fits.
 *
 * ### Visual behavior
 * - A true circle split into [segments] equal ticks, like the bezel of a dial. The track ticks are [trackColor].
 * - The remaining time lights the ticks clockwise from 12 o'clock with a holographic brush whose colors drift
 *   slowly. As time runs out, the lit arc retreats toward 12 o'clock.
 * - Below [warningFraction], the lit arc switches to [warningColor].
 * - [content] sits at the center, for example the seconds left.
 * - [remainingFraction] is read only in the draw phase.
 *
 * Drive it with [com.example.cyberpunkandroid.effects.rememberCyberCountdown].
 *
 * ### Accessibility
 * The ring is hidden from screen readers unless [customA11y] is supplied; [content] is still read.
 *
 * @param remainingFraction Supplies the remaining fraction, `1f` (full) to `0f` (expired).
 * @param modifier Modifier applied to the ring.
 * @param size Ring diameter.
 * @param strokeWidth Tick thickness.
 * @param segments Number of ticks around the ring (at least 1).
 * @param warningFraction Remaining fraction below which the lit arc uses [warningColor].
 * @param driftCyclesPerSecond Holographic drift speed. `0f` makes the arc static.
 * @param palette Holographic colors of the lit arc.
 * @param warningColor Lit arc color when time is short.
 * @param trackColor Unlit tick color.
 * @param customA11y Optional label that makes the ring visible to screen readers.
 * @param content Optional center content.
 */
@Composable
fun CyberRingTimer(
    remainingFraction: () -> Float,
    modifier: Modifier = Modifier,
    size: Dp = CyberPrimitives.IconSizes.dp64,
    strokeWidth: Dp = CyberPrimitives.BorderWidths.dp4,
    segments: Int = 24,
    warningFraction: Float = CyberConfig.Effects.CountdownWarningFraction,
    driftCyclesPerSecond: Float = CyberConfig.Effects.HoloDriftCyclesPerSecond,
    palette: CyberHoloPalette = cyberHoloPalette(),
    warningColor: Color = CyberTheme.status.warning.color,
    trackColor: Color = CyberTheme.colors.surfaceElevated,
    customA11y: String? = null,
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    val phase by rememberCyberDriftPhase(driftCyclesPerSecond)
    val useShader = LocalCyberHoloUseShader.current
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clearAndSetSemantics { if (customA11y != null) contentDescription = customA11y }
                .drawWithCache {
                    val source = CyberHoloBrushSource(useShader)
                    val stroke = strokeWidth.toPx()
                    val diameter = min(this.size.width, this.size.height) - stroke
                    val topLeft = Offset((this.size.width - diameter) / 2f, (this.size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)
                    val count = segments.coerceAtLeast(1)
                    val segmentLength = (PI * diameter / count).toFloat()
                    // Half of each segment is lit and half is the gap, so ticks read as separate marks.
                    val ticks = PathEffect.dashPathEffect(floatArrayOf(segmentLength / 2f, segmentLength / 2f), 0f)
                    val style = Stroke(width = stroke, cap = StrokeCap.Butt, pathEffect = ticks)
                    onDrawBehind {
                        val fraction = remainingFraction().coerceIn(0f, 1f)
                        drawArc(trackColor, TwelveOClock, FullTurn, false, topLeft, arcSize, style = style)
                        if (fraction > 0f) {
                            val sweep = FullTurn * fraction
                            if (fraction < warningFraction) {
                                drawArc(warningColor, TwelveOClock, sweep, false, topLeft, arcSize, style = style)
                            } else {
                                drawArc(source.brush(this.size, phase, palette), TwelveOClock, sweep, false, topLeft, arcSize, style = style)
                            }
                        }
                    }
                }
        )
        content?.invoke(this)
    }
}
