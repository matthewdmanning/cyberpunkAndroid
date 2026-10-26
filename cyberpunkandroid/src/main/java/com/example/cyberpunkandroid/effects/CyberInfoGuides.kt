package com.example.cyberpunkandroid.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives

/**
 * Draws a dotted leader line across the element's width, like the dots between an entry and its page number.
 *
 * Visual: a row of round dots in [color], [strokeWidth] in diameter and [gap] apart, at [verticalFraction] of the
 * element's height. It draws behind the content. Use it on the flexible spacer between a label and its value so
 * the eye can follow a row across a wide gap.
 *
 * @param color Dot color. Use a low-emphasis role (the border or secondary text color) so the leader guides
 *   without competing with the values.
 * @param strokeWidth Dot diameter.
 * @param gap Distance between the edges of two dots.
 * @param verticalFraction Vertical position of the line, as a fraction of the element's height (`0.5f` is the
 *   center).
 * @param appendedA11y Optional accessibility label. The leader is decorative, so no label is applied unless one
 *   of the accessibility parameters is supplied.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
fun Modifier.cyberLeaderLine(
    color: Color,
    strokeWidth: Dp = CyberPrimitives.BorderWidths.dp2,
    gap: Dp = CyberPrimitives.Spacing.dp4,
    verticalFraction: Float = CyberConfig.Effects.LeaderLineCenterFraction,
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .cyberComponentSemantics("CyberLeaderLine", appendedA11y, customA11y)
    .drawWithCache {
        val strokePx = strokeWidth.toPx()
        // A zero-length dash with a round cap draws one dot; the interval is dot diameter plus gap.
        val dots = PathEffect.dashPathEffect(floatArrayOf(0f, strokePx + gap.toPx()), 0f)
        val y = size.height * verticalFraction.coerceIn(0f, 1f)
        val halfStroke = strokePx / 2f
        onDrawBehind {
            if (size.width > strokePx) {
                drawLine(
                    color = color,
                    start = Offset(halfStroke, y),
                    end = Offset(size.width - halfStroke, y),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round,
                    pathEffect = dots
                )
            }
        }
    }

/**
 * Draws four L-shaped corner marks over the element, like the brackets of a viewfinder or targeting frame.
 *
 * Visual: at each corner, two straight strokes of length [armLength] meet at a right angle. Nothing is drawn
 * along the middle of the edges, so the frame marks the area of a readout without boxing it in. The marks draw
 * over the content, [inset] from the edges.
 *
 * Use it to frame a single key value (a readout or a focused target). It is static, so it adds no motion.
 *
 * @param color Stroke color of the marks.
 * @param strokeWidth Stroke width of the marks.
 * @param armLength Length of each arm of an L, measured from the corner.
 * @param inset Distance between the element's edges and the outer side of the marks.
 * @param appendedA11y Optional accessibility label. The frame is decorative, so no label is applied unless one
 *   of the accessibility parameters is supplied.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
fun Modifier.cyberCornerBrackets(
    color: Color,
    strokeWidth: Dp = CyberPrimitives.BorderWidths.dp2,
    armLength: Dp = CyberPrimitives.Spacing.dp12,
    inset: Dp = 0.dp, // Flush with the edges by default.
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .cyberComponentSemantics("CyberCornerBrackets", appendedA11y, customA11y)
    .drawWithCache {
        val strokePx = strokeWidth.toPx()
        // Center the stroke on a line half a stroke inside the inset, so the outer side sits exactly at the inset.
        val edge = inset.toPx() + strokePx / 2f
        val arm = armLength.toPx().coerceAtMost(minOf(size.width, size.height) / 2f)
        val left = edge
        val top = edge
        val right = size.width - edge
        val bottom = size.height - edge
        val path = Path().apply {
            moveTo(left, top + arm); lineTo(left, top); lineTo(left + arm, top)
            moveTo(right - arm, top); lineTo(right, top); lineTo(right, top + arm)
            moveTo(right, bottom - arm); lineTo(right, bottom); lineTo(right - arm, bottom)
            moveTo(left + arm, bottom); lineTo(left, bottom); lineTo(left, bottom - arm)
        }
        val stroke = Stroke(width = strokePx, cap = StrokeCap.Butt, join = StrokeJoin.Miter)
        onDrawWithContent {
            drawContent()
            drawPath(path = path, color = color, style = stroke)
        }
    }
