package com.example.cyberpunkandroid.icons

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.hypot

/**
 * Filled outline of a stroked polyline: the centerline offset by [halfWidth] on each side.
 *
 * Corners keep the centerline's angle. Each side's corner is where its two offset lines
 * meet, so the outer corner is sharp at every thickness (no miter limit) and the inner
 * corner trims both segments. Open polylines get butt caps and give one closed contour.
 * Closed polylines give the outer contour and a hole. Solids wind clockwise and holes
 * counter-clockwise (screen coordinates), matching `CyberPathGeometry`.
 *
 * [valid] is false when an inner corner trims past a whole segment: the stroke is too
 * thick for this centerline. The contours are still returned for measurement.
 */
internal class StrokeOutline(val contours: List<List<Offset>>, val valid: Boolean) {

    companion object {
        // Below this cross product two segments count as collinear (unit directions).
        private const val ParallelEpsilon = 1e-6f

        /** Returns null for fewer than two distinct points, a nonpositive width, or a 180° turn. */
        fun of(points: List<Offset>, halfWidth: Float, closed: Boolean = false): StrokeOutline? {
            if (!(halfWidth > 0f) || !halfWidth.isFinite()) return null
            val pts = distinct(points, closed)
            if (pts.size < 2 || (closed && pts.size < 3)) return null
            val segments = if (closed) pts.size else pts.size - 1
            val dirs = List(segments) { i ->
                val a = pts[i]
                val b = pts[(i + 1) % pts.size]
                val len = hypot(b.x - a.x, b.y - a.y)
                Offset((b.x - a.x) / len, (b.y - a.y) / len)
            }
            val left = side(pts, dirs, halfWidth, closed) ?: return null
            val right = side(pts, dirs, -halfWidth, closed) ?: return null
            val valid = keepsDirection(left, dirs, closed) && keepsDirection(right, dirs, closed)
            val contours = if (closed) {
                val outerIsLeft = abs(signedArea(left)) >= abs(signedArea(right))
                listOf(
                    wind(if (outerIsLeft) left else right, hole = false),
                    wind(if (outerIsLeft) right else left, hole = true)
                )
            } else {
                listOf(wind(left + right.asReversed(), hole = false))
            }
            return StrokeOutline(contours, valid)
        }

        private fun distinct(points: List<Offset>, closed: Boolean): List<Offset> {
            val out = ArrayList<Offset>(points.size)
            for (p in points) if (out.isEmpty() || out.last() != p) out += p
            if (closed && out.size > 1 && out.first() == out.last()) out.removeAt(out.lastIndex)
            return out
        }

        private fun normal(d: Offset) = Offset(-d.y, d.x)

        /** One side's offset polyline: segment ends at the caps, offset-line intersections at corners. */
        private fun side(pts: List<Offset>, dirs: List<Offset>, offset: Float, closed: Boolean): List<Offset>? {
            val out = ArrayList<Offset>(pts.size)
            for (k in pts.indices) {
                val prev = if (k > 0) k - 1 else if (closed) dirs.lastIndex else -1
                val next = if (k < dirs.size) k else -1
                val p = pts[k]
                out += when {
                    prev < 0 -> p + normal(dirs[next]) * offset
                    next < 0 -> p + normal(dirs[prev]) * offset
                    else -> corner(p, dirs[prev], dirs[next], offset) ?: return null
                }
            }
            return out
        }

        /** Where the offset lines of the incoming and outgoing segments at [p] meet. */
        private fun corner(p: Offset, d1: Offset, d2: Offset, offset: Float): Offset? {
            val a = p + normal(d1) * offset
            val b = p + normal(d2) * offset
            val cross = d1.x * d2.y - d1.y * d2.x
            if (abs(cross) < ParallelEpsilon) {
                return if (d1.x * d2.x + d1.y * d2.y > 0f) a else null // straight through, or a 180° turn
            }
            val t = ((b.x - a.x) * d2.y - (b.y - a.y) * d2.x) / cross
            return a + d1 * t
        }

        /** False when an offset segment runs backwards: the corner trims overlapped. */
        private fun keepsDirection(side: List<Offset>, dirs: List<Offset>, closed: Boolean): Boolean {
            for (i in dirs.indices) {
                if (!closed && i + 1 >= side.size) break
                val a = side[i]
                val b = side[(i + 1) % side.size]
                if ((b.x - a.x) * dirs[i].x + (b.y - a.y) * dirs[i].y <= 0f) return false
            }
            return true
        }

        private fun signedArea(pts: List<Offset>): Float {
            var a = 0f
            for (i in pts.indices) {
                val p = pts[i]
                val q = pts[(i + 1) % pts.size]
                a += p.x * q.y - q.x * p.y
            }
            return a / 2f
        }

        private fun wind(pts: List<Offset>, hole: Boolean): List<Offset> =
            if ((signedArea(pts) < 0f) != hole) pts.asReversed() else pts
    }
}
