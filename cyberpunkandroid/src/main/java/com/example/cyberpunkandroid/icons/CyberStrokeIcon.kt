package com.example.cyberpunkandroid.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberPerimeterShape
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.ceil

/**
 * An icon drawn as the filled outline of a thickened [centerline]: open or closed, concave, or
 * crossing itself, like a cursive "l". Corners keep the centerline's angle at every thickness,
 * curves keep their shape (radius r ± offset around the same center), and where the stroke
 * overlaps itself the overlaps merge, so it never breaks as it thickens.
 *
 * [width] sets the centerline's width; its height follows the centerline's viewport. The
 * centerline never moves or shrinks: the stroke grows around it and the component grows to
 * hold it, up to the space the parent allows. A thicker request is drawn at the thickest that
 * fits, so a growing value never breaks the shape.
 *
 * Closed contours put the stroke [CyberStrokeAlignment.Outside] the loop by default, so what
 * the loop encloses keeps its size. Open contours are stroked on both sides.
 *
 * To run path effects along the icon's outside edge, build a [CyberStrokeShape] and pass it to
 * both this composable's other overload and `Modifier.cyberPerimeter`.
 *
 * Use a null [contentDescription] for decorative artwork.
 */
@Composable
fun CyberStrokeIcon(
    centerline: CyberCenterline,
    modifier: Modifier = Modifier,
    width: Dp = CyberPrimitives.IconSizes.dp64,
    thickness: Dp = CyberPrimitives.BorderWidths.dp4,
    alignment: CyberStrokeAlignment = CyberStrokeAlignment.Outside,
    color: Color = CyberTheme.colors.primary,
    contentDescription: String? = null
) {
    val shape = remember(centerline, width, thickness, alignment) {
        CyberStrokeShape(centerline, width, thickness, alignment)
    }
    CyberStrokeIcon(shape, modifier, color, contentDescription)
}

/** [CyberStrokeIcon] for a prepared [shape], so path effects can share it. */
@Composable
fun CyberStrokeIcon(
    shape: CyberStrokeShape,
    modifier: Modifier = Modifier,
    color: Color = CyberTheme.colors.primary,
    contentDescription: String? = null
) {
    val accessibility = if (contentDescription == null) Modifier
        else Modifier.semantics { this.contentDescription = contentDescription }

    Spacer(
        modifier = modifier
            .layout { measurable, constraints ->
                // Grow to hold the outline, up to what the parent allows.
                val fit = shape.fit(this, constraints.maxWidth.bound(), constraints.maxHeight.bound())
                val w = ceil(fit?.bounds?.width ?: 0.0).toInt().coerceIn(constraints.minWidth, constraints.maxWidth)
                val h = ceil(fit?.bounds?.height ?: 0.0).toInt().coerceIn(constraints.minHeight, constraints.maxHeight)
                val placeable = measurable.measure(Constraints.fixed(w, h))
                layout(w, h) { placeable.place(0, 0) }
            }
            .then(accessibility)
            .background(color, shape)
    )
}

private fun Int.bound() = if (this == Constraints.Infinity) Double.POSITIVE_INFINITY else toDouble()

/**
 * Outline of [CyberStrokeIcon] for the same parameters, centered in the given size, which is
 * also the space the stroke may grow into. Holes wind against the outer edges, so the outline
 * fills correctly with the default non-zero rule.
 *
 * As a [CyberPerimeterShape], it lists the outside edge of each filled part for
 * `Modifier.cyberPerimeter`. The geometry is cached per size and thickness, so reuse one
 * instance for the icon and its effects.
 */
@Immutable
class CyberStrokeShape(
    val centerline: CyberCenterline,
    val width: Dp,
    val thickness: Dp,
    val alignment: CyberStrokeAlignment = CyberStrokeAlignment.Outside
) : CyberPerimeterShape {

    init {
        require(width.value.isFinite() && width >= 0.dp) { "width must be nonnegative and finite" }
        require(thickness.value.isFinite() && thickness > 0.dp) { "thickness must be positive and finite" }
    }

    // Single-entry caches: layout, drawing and effects ask for the same size in turn.
    private var stroker: Pair<Float, CenterlineStroker>? = null
    private var lastFit: Pair<List<Double>, StrokeFit?>? = null
    private var lastRegion: Pair<List<Double>, StrokeRegion?>? = null

    private fun stroker(density: Density): CenterlineStroker {
        val px = with(density) { width.toPx() }
        stroker?.let { if (it.first == px) return it.second }
        val scale = px.toDouble() / centerline.viewportWidth
        return CenterlineStroker(centerline.contours, scale, alignment).also { stroker = px to it }
    }

    /** The drawn stroke for [maxWidth] × [maxHeight] px of space; null when nothing fits. */
    internal fun fit(density: Density, maxWidth: Double, maxHeight: Double): StrokeFit? {
        val requested = with(density) { thickness.toPx() }.toDouble()
        val key = listOf(density.density.toDouble(), maxWidth, maxHeight)
        lastFit?.let { if (it.first == key) return it.second }
        return stroker(density).fit(requested, maxWidth, maxHeight).also { lastFit = key to it }
    }

    private fun region(density: Density, size: Size): StrokeRegion? {
        val fit = fit(density, size.width.toDouble(), size.height.toDouble()) ?: return null
        val key = listOf(density.density.toDouble(), fit.strokeWidth)
        lastRegion?.let { if (it.first == key) return it.second }
        return stroker(density).region(fit.strokeWidth).also { lastRegion = key to it }
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val region = region(density, size) ?: return Outline.Generic(path)
        for (loop in region.solids + region.holes) path.addLoop(loop, region.bounds, size)
        return Outline.Generic(path)
    }

    override fun perimeters(size: Size, layoutDirection: LayoutDirection, density: Density, includeHoles: Boolean): List<Path> {
        val region = region(density, size) ?: return emptyList()
        val loops = if (includeHoles) region.solids + region.holes else region.solids
        return loops.map { Path().apply { addLoop(it, region.bounds, size) } }
    }

    override fun equals(other: Any?): Boolean =
        other is CyberStrokeShape && other.centerline == centerline && other.width == width &&
            other.thickness == thickness && other.alignment == alignment

    override fun hashCode(): Int =
        ((centerline.hashCode() * 31 + width.hashCode()) * 31 + thickness.hashCode()) * 31 + alignment.hashCode()
}

/** Adds [loop] as a closed contour, centering [bounds] in [size]. */
private fun Path.addLoop(loop: List<Pt>, bounds: Bounds, size: Size) {
    val dx = (size.width - bounds.width) / 2.0 - bounds.left
    val dy = (size.height - bounds.height) / 2.0 - bounds.top
    moveTo((loop[0].x + dx).toFloat(), (loop[0].y + dy).toFloat())
    for (i in 1 until loop.size) lineTo((loop[i].x + dx).toFloat(), (loop[i].y + dy).toFloat())
    close()
}
