package com.example.cyberpunkandroid.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min

/**
 * Three nested, right-facing chevrons with individually adjustable stroke thickness,
 * spacing, vertical shear, and proportions. Each chevron is drawn as the filled outline
 * of its stroke, with sharp corners that keep the centerline angle and square-cut ends.
 *
 * [width] and [aspectRatio] size the centerlines: at zero thickness the three chevrons
 * fill a [width] × [width] / [aspectRatio] box. The centerlines never change with
 * [thickness]; the stroke grows around them and the component grows to hold it.
 * [spacing] is the visible gap between neighboring chevrons at their closest points, so
 * the chevrons move apart as the stroke thickens and the gap holds.
 * [shear] ranges from -1 to 1. Positive values shift the top end of each
 * chevron right and the bottom end left; the tip stays at the vertical center.
 *
 * The thickness has a natural limit: the space the parent allows, or the inner corner
 * trimming past a whole arm, whichever comes first. A thicker request is drawn at that
 * limit, so a growing value never breaks the shape.
 *
 * The same outline is available as [CyberTripleChevronShape] for path borders and tracers.
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

    val shape = CyberTripleChevronShape(width, aspectRatio, spacing, thickness, shear)
    val accessibility = if (contentDescription == null) Modifier
        else Modifier.semantics { this.contentDescription = contentDescription }

    Spacer(
        modifier = modifier
            .layout { measurable, constraints ->
                // Grow to hold the outline, up to what the parent allows.
                val size = shape.geometry(this, constraints.maxWidth.bound(), constraints.maxHeight.bound())?.size
                val w = ceil(size?.width ?: 0f).toInt().coerceIn(constraints.minWidth, constraints.maxWidth)
                val h = ceil(size?.height ?: 0f).toInt().coerceIn(constraints.minHeight, constraints.maxHeight)
                val placeable = measurable.measure(Constraints.fixed(w, h))
                layout(w, h) { placeable.place(0, 0) }
            }
            .then(accessibility)
            .background(color, shape)
    )
}

private fun Int.bound() = if (this == Constraints.Infinity) Float.POSITIVE_INFINITY else toFloat()

/**
 * Outline of [CyberTripleChevron] for the same parameters, centered in the given size,
 * which is also the space the stroke may grow into. With [chevron] null, the outline holds
 * all three chevrons; with 0, 1 or 2 it holds only that chevron, left to right. Path
 * borders and tracers measure one contour, so trace each chevron with its own index. Each
 * contour is closed and winds clockwise.
 */
internal data class CyberTripleChevronShape(
    val width: Dp,
    val aspectRatio: Float,
    val spacing: Dp,
    val thickness: Dp,
    val shear: Float,
    val chevron: Int? = null
) : Shape {
    fun geometry(density: Density, maxWidth: Float, maxHeight: Float): TripleChevronGeometry? = with(density) {
        tripleChevronGeometry(
            width.toPx(), (width / aspectRatio).toPx(), thickness.toPx(), spacing.toPx(), shear, maxWidth, maxHeight
        )
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val geometry = geometry(density, size.width, size.height) ?: return Outline.Generic(path)
        val dx = (size.width - geometry.size.width) / 2f
        val dy = (size.height - geometry.size.height) / 2f
        geometry.outlines.forEachIndexed { index, contour ->
            if (chevron == null || chevron == index) {
                path.moveTo(contour[0].x + dx, contour[0].y + dy)
                for (i in 1 until contour.size) path.lineTo(contour[i].x + dx, contour[i].y + dy)
                path.close()
            }
        }
        return Outline.Generic(path)
    }
}

/** How close a stroke is to the thickest the geometry can hold. */
internal enum class StrokeLoad {
    Ok, Notice, Warning, Limit;

    companion object {
        // Starting points for the graded warning; tune by eye against the thickness slider.
        private const val NoticeAt = 0.6f
        private const val WarningAt = 0.85f

        fun of(load: Float): StrokeLoad = when {
            load > 1f -> Limit
            load >= WarningAt -> Warning
            load >= NoticeAt -> Notice
            else -> Ok
        }
    }
}

/**
 * Triple-chevron outlines in pixels, starting at (0, 0) and filling [size]. [strokeWidth]
 * is the drawn thickness: the requested one, or [maxStrokeWidth] when the request is
 * past the natural limit. [load] is requested / max; above 1 the drawing is held.
 */
internal class TripleChevronGeometry(
    val outlines: List<List<Offset>>,
    val size: Size,
    val strokeWidth: Float,
    val maxStrokeWidth: Float,
    val load: Float
)

// Bisection steps; each halves the bracket, so these resolve to well under a pixel.
private const val SearchSteps = 24

/**
 * Outlines of three nested chevrons around fixed centerlines that, at zero thickness, fill
 * a [designWidth] × [designHeight] box with [gap] between them. The stroke grows around
 * the centerlines, neighbors move apart to keep [gap] at their closest points, and the
 * result may grow up to [maxWidth] × [maxHeight]. Returns null when no stroke fits.
 */
internal fun tripleChevronGeometry(
    designWidth: Float,
    designHeight: Float,
    strokeWidth: Float,
    gap: Float,
    shear: Float,
    maxWidth: Float = Float.POSITIVE_INFINITY,
    maxHeight: Float = Float.POSITIVE_INFINITY
): TripleChevronGeometry? {
    if (!designWidth.isFinite() || !designHeight.isFinite() ||
        !strokeWidth.isFinite() || !gap.isFinite() || !shear.isFinite() ||
        designWidth <= 0f || designHeight <= 0f || strokeWidth <= 0f ||
        gap < 0f || abs(shear) > 1f || maxWidth.isNaN() || maxHeight.isNaN()
    ) return null

    val centerline = designCenterline(designWidth, designHeight, gap, shear) ?: return null
    fun fit(stroke: Float): Placed? =
        place(centerline, stroke, gap)?.takeIf { it.size.width <= maxWidth && it.size.height <= maxHeight }

    val requested = fit(strokeWidth)
    // Past the limit the inner corner is gone or the space is used up; a stroke as thick
    // as the whole design is always past it. Thinner strokes always fit, so bisect.
    var lo = 0f
    var loFit: Placed? = null
    var hi = designWidth + designHeight
    if (requested != null) {
        lo = strokeWidth
        loFit = requested
    }
    repeat(SearchSteps) {
        val mid = (lo + hi) / 2f
        val placed = fit(mid)
        if (placed != null) {
            lo = mid
            loFit = placed
        } else {
            hi = mid
        }
    }
    val drawn = requested ?: loFit ?: return null
    return TripleChevronGeometry(
        outlines = drawn.outlines,
        size = drawn.size,
        strokeWidth = if (requested != null) strokeWidth else lo,
        maxStrokeWidth = lo,
        load = strokeWidth / lo
    )
}

private class Placed(val outlines: List<List<Offset>>, val size: Size)

/**
 * Strokes the fixed [centerline] and places three copies [gap] apart at their closest
 * points, starting at (0, 0). Null once the inner corner trims past a whole arm.
 */
private fun place(centerline: List<Offset>, strokeWidth: Float, gap: Float): Placed? {
    val outline = StrokeOutline.of(centerline, strokeWidth / 2f) ?: return null
    if (!outline.valid) return null
    val contour = outline.contours[0]
    val left = contour.minOf { it.x }
    val top = contour.minOf { it.y }
    val pitch = pitch(contour, closed = true, gap = gap)
    val size = Size(2f * pitch + contour.maxOf { it.x } - left, contour.maxOf { it.y } - top)
    return Placed(List(3) { index -> contour.map { Offset(it.x - left + index * pitch, it.y - top) } }, size)
}

/**
 * The zero-thickness chevron whose nested group of three, [gap] apart, fills the box.
 * Its angles are fixed for every thickness.
 */
private fun designCenterline(width: Float, height: Float, gap: Float, shear: Float): List<Offset>? {
    fun centerline(run: Float): List<Offset> {
        val shift = run * shear / 2f
        return listOf(
            Offset(abs(shift) + shift, 0f),
            Offset(abs(shift) + run, height / 2f),
            Offset(abs(shift) - shift, height)
        )
    }
    // The group widens with the arm run, so find the longest run that fits.
    var lo = 0f
    var hi = width
    var best: List<Offset>? = null
    repeat(SearchSteps) {
        val mid = (lo + hi) / 2f
        val line = centerline(mid)
        val pitch = pitch(line, closed = false, gap = gap)
        if (2f * pitch + line.maxOf { it.x } - line.minOf { it.x } <= width) {
            lo = mid
            best = line
        } else {
            hi = mid
        }
    }
    return best
}

/** Smallest horizontal shift that leaves [gap] between [shape] and its shifted copy. */
private fun pitch(shape: List<Offset>, closed: Boolean, gap: Float): Float {
    val shapeWidth = shape.maxOf { it.x } - shape.minOf { it.x }
    // Shifting by the full width plus the gap always clears it; the clearance grows with the shift.
    var lo = 0f
    var hi = shapeWidth + gap
    repeat(SearchSteps) {
        val mid = (lo + hi) / 2f
        if (clearance(shape, closed, mid) >= gap) hi = mid else lo = mid
    }
    return hi
}

/** Closest distance between [shape] and its copy shifted right by [shift]; 0 when they cross. */
private fun clearance(shape: List<Offset>, closed: Boolean, shift: Float): Float {
    val edges = if (closed) shape.size else shape.size - 1
    var best = Float.POSITIVE_INFINITY
    for (i in 0 until edges) {
        val a = shape[i]
        val b = shape[(i + 1) % shape.size]
        for (j in 0 until edges) {
            val c = shape[j] + Offset(shift, 0f)
            val d = shape[(j + 1) % shape.size] + Offset(shift, 0f)
            if (crosses(a, b, c, d)) return 0f
            best = min(best, min(min(toSegment(a, c, d), toSegment(b, c, d)), min(toSegment(c, a, b), toSegment(d, a, b))))
        }
    }
    return best
}

private fun cross(o: Offset, a: Offset, b: Offset) = (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)

private fun crosses(a: Offset, b: Offset, c: Offset, d: Offset): Boolean {
    val d1 = cross(c, d, a)
    val d2 = cross(c, d, b)
    val d3 = cross(a, b, c)
    val d4 = cross(a, b, d)
    return ((d1 > 0f && d2 < 0f) || (d1 < 0f && d2 > 0f)) && ((d3 > 0f && d4 < 0f) || (d3 < 0f && d4 > 0f))
}

private fun toSegment(p: Offset, a: Offset, b: Offset): Float {
    val ab = b - a
    val lengthSquared = ab.x * ab.x + ab.y * ab.y
    val t = if (lengthSquared == 0f) 0f else (((p.x - a.x) * ab.x + (p.y - a.y) * ab.y) / lengthSquared).coerceIn(0f, 1f)
    return (p - (a + ab * t)).getDistance()
}
