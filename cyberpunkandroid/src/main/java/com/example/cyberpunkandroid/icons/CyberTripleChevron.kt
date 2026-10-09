package com.example.cyberpunkandroid.icons

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.abs
import kotlin.math.min

/**
 * Three open, right-facing chevrons with individually adjustable stroke thickness,
 * horizontal clear spacing, vertical shear, and proportions. Draws sharp, unfilled
 * paths; callers may add cyberpunk effects with existing modifiers.
 *
 * [shear] ranges from -1 to 1. Positive values shift the top end of each
 * chevron right and the bottom end left; the tip stays at the vertical center.
 * [spacing] is the horizontal clearance between the chevrons' bounding boxes.
 * [width] supplies a default width, and [aspectRatio] controls the preferred
 * layout proportions. Explicit size constraints in [modifier] take precedence.
 *
 * Use a null [contentDescription] for decorative artwork.
 */
@Composable
fun CyberTripleChevron(
    modifier: Modifier = Modifier,
    width: Dp = CyberPrimitives.IconSizes.dp64,
    aspectRatio: Float = 2.5f,
    spacing: Dp = CyberPrimitives.Spacing.dp4,
    thickness: Dp = CyberPrimitives.BorderWidths.dp4,
    shear: Float = 0.2f,
    color: Color = CyberTheme.colors.primary,
    contentDescription: String? = null
) {
    require(aspectRatio.isFinite() && aspectRatio > 0f) { "aspectRatio must be positive and finite" }
    require(width.value.isFinite() && width >= 0.dp) { "width must be nonnegative and finite" }
    require(spacing.value.isFinite() && spacing >= 0.dp) { "spacing must be nonnegative and finite" }
    require(thickness.value.isFinite() && thickness > 0.dp) { "thickness must be positive and finite" }
    require(shear.isFinite() && shear in -1f..1f) { "shear must be between -1 and 1" }

    val accessibility = if (contentDescription == null) Modifier
        else Modifier.semantics { this.contentDescription = contentDescription }

    Spacer(
        modifier = modifier
            .width(width)
            .aspectRatio(aspectRatio)
            .then(accessibility)
            .drawWithCache {
                val strokeWidth = thickness.toPx()
                val paths = tripleChevronPoints(
                    width = size.width,
                    height = size.height,
                    strokeWidth = strokeWidth,
                    gap = spacing.toPx(),
                    shear = shear
                ).map { points ->
                    Path().apply {
                        moveTo(points[0].x, points[0].y)
                        lineTo(points[1].x, points[1].y)
                        lineTo(points[2].x, points[2].y)
                    }
                }
                onDrawBehind {
                    val stroke = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Butt,
                        join = StrokeJoin.Miter
                    )
                    paths.forEach { drawPath(it, color = color, style = stroke) }
                }
            }
    )
}

/**
 * Calculates the three chevron centerlines in pixels. Empty when there is not
 * enough room to preserve the requested thickness and clear spacing.
 */
internal fun tripleChevronPoints(
    width: Float,
    height: Float,
    strokeWidth: Float,
    gap: Float,
    shear: Float
): List<List<Offset>> {
    if (!width.isFinite() || !height.isFinite() ||
        !strokeWidth.isFinite() || !gap.isFinite() || !shear.isFinite() ||
        width <= 0f || height <= 0f || strokeWidth <= 0f ||
        gap < 0f || abs(shear) > 1f
    ) return emptyList()

    val inset = strokeWidth / 2f
    val usableWidth = width - strokeWidth - 2f * gap
    val usableHeight = height - strokeWidth
    if (usableWidth <= 0f || usableHeight <= 0f) return emptyList()

    // The shear displacement contributes to each chevron's horizontal bounds.
    val run = usableWidth / (3f * (1f + abs(shear) / 2f))
    val shift = run * shear / 2f
    val footprint = run + abs(shift)
    // A stroke wider than the geometric feature cannot preserve distinct tips.
    if (run <= strokeWidth) return emptyList()

    val top = inset
    val mid = height / 2f
    val bottom = height - inset
    return List(3) { index ->
        val left = inset + index * (footprint + gap)
        val x = left + abs(shift) / 2f
        val a = Offset(x + shift, top)
        val b = Offset(x + run, mid)
        val c = Offset(x - shift, bottom)
        listOf(a, b, c)
    }
}
