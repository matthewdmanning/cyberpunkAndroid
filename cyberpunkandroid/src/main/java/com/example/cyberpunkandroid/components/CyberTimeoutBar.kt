package com.example.cyberpunkandroid.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Thin bar that shows how much time is left before something expires (a notice auto-dismisses, a code becomes
 * invalid, an offer closes).
 *
 * ### Visual behavior
 * - A full-width [trackColor] line with a [color] fill whose length is the remaining fraction. The fill drains
 *   toward the start edge (left in left-to-right layouts), so the remaining time reads like a fuel gauge.
 * - When the remaining fraction drops below [warningFraction], the fill switches to [warningColor]. This is the
 *   only state change: it shows urgency without motion beyond the drain itself.
 * - [remainingFraction] is read only in the draw phase, so a running countdown redraws the bar without
 *   recomposition.
 *
 * Drive it with [com.example.cyberpunkandroid.effects.rememberCyberCountdown]:
 * ```
 * val remaining = rememberCyberCountdown(durationMillis = 6_000, onTimeout = { visible = false })
 * CyberTimeoutBar(remainingFraction = { remaining.value })
 * ```
 *
 * ### Accessibility
 * The bar is a visual aid and is hidden from screen readers unless [customA11y] is supplied. Do not rely on it
 * as the only way to communicate a deadline: give the expiring element a text alternative, and pause the
 * countdown while the user interacts with it.
 *
 * @param remainingFraction Supplies the remaining fraction, from `1f` (full) to `0f` (expired).
 * @param modifier Modifier applied to the bar.
 * @param thickness Bar height.
 * @param warningFraction Remaining fraction below which the fill uses [warningColor].
 * @param color Fill color while time is not short.
 * @param warningColor Fill color when time is short.
 * @param trackColor Color of the drained part.
 * @param customA11y Optional label that makes the bar visible to screen readers.
 */
@Composable
fun CyberTimeoutBar(
    remainingFraction: () -> Float,
    modifier: Modifier = Modifier,
    thickness: Dp = CyberPrimitives.BorderWidths.dp2,
    warningFraction: Float = CyberConfig.Effects.CountdownWarningFraction,
    color: Color = CyberTheme.colors.primary,
    warningColor: Color = CyberTheme.status.warning.color,
    trackColor: Color = CyberTheme.colors.surfaceElevated,
    customA11y: String? = null,
) {
    Spacer(
        modifier = modifier
            .clearAndSetSemantics { if (customA11y != null) contentDescription = customA11y }
            .fillMaxWidth()
            .height(thickness)
            .drawBehind {
                val fraction = remainingFraction().coerceIn(0f, 1f)
                val rtl = layoutDirection == LayoutDirection.Rtl
                drawRect(trackColor)
                if (fraction > 0f) {
                    val width = size.width * fraction
                    drawRect(
                        color = if (fraction < warningFraction) warningColor else color,
                        topLeft = Offset(if (rtl) size.width - width else 0f, 0f),
                        size = Size(width, size.height)
                    )
                }
            }
    )
}
