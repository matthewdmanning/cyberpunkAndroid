package com.example.cyberpunkandroid.icons

import androidx.compose.runtime.Immutable

/**
 * The centerline of a stroked icon: open or closed contours of lines, Bézier curves and
 * circular arcs, in a [viewportWidth] × [viewportHeight] coordinate box (like a vector
 * drawable's viewport). [CyberStrokeIcon] thickens it into a filled outline.
 *
 * Contours may be concave and may cross themselves or each other, like a cursive "l".
 * Build one with [cyberCenterline].
 */
@Immutable
class CyberCenterline internal constructor(
    val viewportWidth: Float,
    val viewportHeight: Float,
    internal val contours: List<CenterlineContour>
) {
    override fun equals(other: Any?): Boolean =
        other is CyberCenterline && other.viewportWidth == viewportWidth &&
            other.viewportHeight == viewportHeight && other.contours == contours

    override fun hashCode(): Int = (viewportWidth.hashCode() * 31 + viewportHeight.hashCode()) * 31 + contours.hashCode()
}

/**
 * Where the stroke of a closed contour sits relative to its centerline. Open contours are
 * always stroked on both sides, because they have no inside.
 */
enum class CyberStrokeAlignment {
    /** Half the thickness on each side of the centerline. */
    Center,

    /** All of the thickness outside the loop, so the area inside the centerline stays the same. */
    Outside,

    /** All of the thickness inside the loop, so the outer edge stays on the centerline. */
    Inside
}

/** One contour: a start point, the segments that follow it, and whether it closes back. */
internal data class CenterlineContour(
    val startX: Float,
    val startY: Float,
    val segments: List<CenterlineSegment>,
    val closed: Boolean
)

internal sealed interface CenterlineSegment {
    data class Line(val x: Float, val y: Float) : CenterlineSegment
    data class Cubic(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val x: Float, val y: Float) : CenterlineSegment

    /** Circular arc around ([cx], [cy]) from the current point; positive sweeps run clockwise on screen. */
    data class Arc(val cx: Float, val cy: Float, val sweepDegrees: Float) : CenterlineSegment
}

/** Path-style builder for [CyberCenterline]. Each [moveTo] starts a new contour. */
class CyberCenterlineBuilder internal constructor() {
    private val contours = ArrayList<CenterlineContour>()
    private var segments: MutableList<CenterlineSegment>? = null
    private var startX = 0f
    private var startY = 0f
    private var x = 0f
    private var y = 0f

    fun moveTo(x: Float, y: Float) {
        flush(closed = false)
        segments = ArrayList()
        startX = x
        startY = y
        this.x = x
        this.y = y
    }

    fun lineTo(x: Float, y: Float) = add(CenterlineSegment.Line(x, y), x, y)

    fun quadTo(x1: Float, y1: Float, x: Float, y: Float) {
        // A quadratic is the cubic whose control points sit 2/3 of the way to the quad's control point.
        add(
            CenterlineSegment.Cubic(
                this.x + 2f / 3f * (x1 - this.x), this.y + 2f / 3f * (y1 - this.y),
                x + 2f / 3f * (x1 - x), y + 2f / 3f * (y1 - y),
                x, y
            ),
            x, y
        )
    }

    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) =
        add(CenterlineSegment.Cubic(x1, y1, x2, y2, x, y), x, y)

    /**
     * Circular arc around ([centerX], [centerY]) starting at the current point, keeping its
     * distance to the center as the radius. Positive [sweepDegrees] run clockwise on screen.
     */
    fun arcTo(centerX: Float, centerY: Float, sweepDegrees: Float) {
        val angle = Math.toRadians(sweepDegrees.toDouble())
        val dx = (x - centerX).toDouble()
        val dy = (y - centerY).toDouble()
        val endX = centerX + dx * kotlin.math.cos(angle) - dy * kotlin.math.sin(angle)
        val endY = centerY + dx * kotlin.math.sin(angle) + dy * kotlin.math.cos(angle)
        add(CenterlineSegment.Arc(centerX, centerY, sweepDegrees), endX.toFloat(), endY.toFloat())
    }

    /** Closes the current contour back to its start point. */
    fun close() {
        flush(closed = true)
        x = startX
        y = startY
    }

    private fun add(segment: CenterlineSegment, endX: Float, endY: Float) {
        val open = checkNotNull(segments) { "Start each contour with moveTo" }
        open += segment
        x = endX
        y = endY
    }

    private fun flush(closed: Boolean) {
        val open = segments ?: return
        if (open.isNotEmpty()) contours += CenterlineContour(startX, startY, open.toList(), closed)
        segments = null
    }

    internal fun build(): List<CenterlineContour> {
        flush(closed = false)
        return contours.toList()
    }
}

/**
 * Builds a [CyberCenterline] in a [viewportWidth] × [viewportHeight] coordinate box.
 *
 * ```
 * val ring = cyberCenterline(24f, 24f) {
 *     moveTo(12f, 4f)
 *     arcTo(12f, 12f, 360f)
 *     close()
 * }
 * ```
 */
fun cyberCenterline(
    viewportWidth: Float,
    viewportHeight: Float,
    build: CyberCenterlineBuilder.() -> Unit
): CyberCenterline {
    require(viewportWidth.isFinite() && viewportWidth > 0f) { "viewportWidth must be positive and finite" }
    require(viewportHeight.isFinite() && viewportHeight > 0f) { "viewportHeight must be positive and finite" }
    return CyberCenterline(viewportWidth, viewportHeight, CyberCenterlineBuilder().apply(build).build())
}
