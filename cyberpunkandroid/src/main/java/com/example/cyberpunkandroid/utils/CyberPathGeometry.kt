package com.example.cyberpunkandroid.utils

import androidx.compose.ui.geometry.Offset
import com.example.cyberpunkandroid.config.CyberPathDefaults
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StampedPathEffectStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Low-level geometry for the path-effect system (the "paint" layer, like [CyberSweepGradientBrush]).
 *
 * Rules learned while prototyping against Skia, the engine behind Android's PathEffect:
 * - Stamps from [PathEffect.stampedPathEffect] are always FILLED, so every stamp is a closed area
 *   (ribbons, discs), never a bare polyline.
 * - The stamp's fill type is dropped when Skia rebuilds the output path, so holes are made with the
 *   opposite winding, not even-odd.
 * - [StampedPathEffectStyle.Morph] maps each stamp edge through one quad, so edges are pre-subdivided.
 * - Skia never places a partial stamp before a contour's start, so a phase shift leaves a gap there;
 *   closed outlines are animated by rotating the contour start instead ([CyberMarching]).
 */
object CyberPathGeometry {

    /** Stretch applied to stamp advances so float error never adds an (n+1)th stamp on top of the first. */
    const val STAMP_EPS = 1e-5f

    /**
     * Largest spacing near [desired] that divides [length] exactly, so closed outlines have no seam.
     * @param length TODO: document this
     * @param desired TODO: document this
     */
    fun fit(length: Float, desired: Float): Float {
        val n = max(1, (length / desired).roundToInt())
        return length / n
    }

    /**
     * Shape outline as a single path, shrunk by [inset] on every side so stroke geometry is not clipped.
     * @param inset TODO: document this
     */
    fun outlinePath(shape: Shape, size: Size, layoutDirection: LayoutDirection, density: Density, inset: Float): Path {
        val inner = Size((size.width - 2 * inset).coerceAtLeast(1f), (size.height - 2 * inset).coerceAtLeast(1f))
        val src = Path().apply {
            when (val outline = shape.createOutline(inner, layoutDirection, density)) {
                is Outline.Rectangle -> addRect(outline.rect)
                is Outline.Rounded -> addRoundRect(outline.roundRect)
                is Outline.Generic -> addPath(outline.path)
            }
        }
        return Path().apply { addPath(src, Offset(inset, inset)) }
    }

    // ------------------------------------------------------------------ polygons

    private fun signedArea(pts: List<Offset>): Float {
        var a = 0f
        for (i in pts.indices) {
            val p = pts[i]
            val q = pts[(i + 1) % pts.size]
            a += p.x * q.y - q.x * p.y
        }
        return a / 2f
    }

    /** Solids get positive shoelace area, holes negative, so overlapping solids never cancel (non-zero rule). */
    private fun oriented(pts: List<Offset>, hole: Boolean): List<Offset> =
        if ((signedArea(pts) < 0f) != hole) pts.asReversed() else pts

    private fun closedPath(pts: List<Offset>): Path = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
        close()
    }

    /**
     * Splits each segment of an open polyline into pieces no longer than [step].
     * @param step TODO: document this
     */
    fun subdivide(pts: List<Offset>, step: Float): List<Offset> {
        val out = ArrayList<Offset>()
        for (i in 0 until pts.size - 1) {
            val a = pts[i]
            val b = pts[i + 1]
            val n = max(1, ceil(hypot(b.x - a.x, b.y - a.y) / step).toInt())
            for (k in 0 until n) out.add(Offset(a.x + (b.x - a.x) * k / n, a.y + (b.y - a.y) * k / n))
        }
        out.add(pts.last())
        return out
    }

    /**
     * Closed polygon; [step] > 0 subdivides the edges (needed for Morph stamps).
     * @param step TODO: document this
     * @param hole TODO: document this
     */
    fun polygon(pts: List<Offset>, step: Float = 0f, hole: Boolean = false): Path {
        val src = if (step > 0f) subdivide(pts + pts[0], step).dropLast(1) else pts
        return closedPath(oriented(src, hole))
    }

    /**
     * Filled band of [thickness] around an open polyline.
     * @param thickness TODO: document this
     */
    fun ribbon(pts: List<Offset>, thickness: Float): Path {
        val left = ArrayList<Offset>(pts.size)
        val right = ArrayList<Offset>(pts.size)
        val h = thickness / 2f
        for (i in pts.indices) {
            val a = pts[max(i - 1, 0)]
            val b = pts[min(i + 1, pts.size - 1)]
            val dx = b.x - a.x
            val dy = b.y - a.y
            val len = hypot(dx, dy).takeIf { it > 0f } ?: 1f
            val nx = -dy / len
            val ny = dx / len
            left.add(Offset(pts[i].x + nx * h, pts[i].y + ny * h))
            right.add(Offset(pts[i].x - nx * h, pts[i].y - ny * h))
        }
        return closedPath(oriented(left + right.asReversed(), hole = false))
    }

    /**
     * Circle as an n-gon (a hole when [hole]).
     * @param cx TODO: document this
     * @param cy TODO: document this
     * @param r TODO: document this
     * @param hole TODO: document this
     * @param segments TODO: document this
     */
    fun circle(cx: Float, cy: Float, r: Float, hole: Boolean = false, segments: Int = 24): Path =
        polygon(List(segments) { k ->
            val a = 2.0 * PI * k / segments
            Offset(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
        }, hole = hole)

    /**
     * Annulus of stroke [thickness] centred on radius [r].
     * @param cx TODO: document this
     * @param cy TODO: document this
     * @param r TODO: document this
     * @param thickness TODO: document this
     * @param segments TODO: document this
     */
    fun ring(cx: Float, cy: Float, r: Float, thickness: Float, segments: Int = 24): Path =
        merge(circle(cx, cy, r + thickness / 2f, false, segments), circle(cx, cy, r - thickness / 2f, true, segments))

    /**
     * Capsule spanning [x0]..[x1] (long axis along x) as a polygon; [edgeStep] subdivides the straight edges.
     * @param x0 TODO: document this
     * @param y0 TODO: document this
     * @param x1 TODO: document this
     * @param y1 TODO: document this
     * @param edgeStep TODO: document this
     * @param hole TODO: document this
     * @param capSegments TODO: document this
     */
    fun stadium(x0: Float, y0: Float, x1: Float, y1: Float, edgeStep: Float, hole: Boolean = false, capSegments: Int = 10): Path {
        val r = (y1 - y0) / 2f
        val cy = (y0 + y1) / 2f
        val pts = ArrayList<Offset>()
        for (k in 0..capSegments) {
            val a = -PI / 2 + PI * k / capSegments
            pts.add(Offset(x1 - r + r * cos(a).toFloat(), cy + r * sin(a).toFloat()))
        }
        for (k in 0..capSegments) {
            val a = PI / 2 + PI * k / capSegments
            pts.add(Offset(x0 + r + r * cos(a).toFloat(), cy + r * sin(a).toFloat()))
        }
        return polygon(pts, step = edgeStep, hole = hole)
    }

    // TODO: document this
    fun merge(vararg paths: Path): Path = Path().apply { paths.forEach { addPath(it) } }

    // ------------------------------------------------------------------ measuring

    /** Length of the (single-contour) outline. */
    fun length(path: Path): Float = PathMeasure().apply { setPath(path, false) }.length

    /**
     * Distances along a closed outline where it turns faster than 1/[maxRadius] for at least
     * [minTurnDeg] in total: sharp corners, chamfers and tight rounded corners count, a large
     * circle does not. Turning samples separated by up to [gapTolerance] are merged so a
     * polygon-approximated arc still reads as one corner. Centres are turn-weighted.
      * @param length TODO: document this
      * @param sampleStep TODO: document this
      * @param maxRadius TODO: document this
      * @param gapTolerance TODO: document this
      * @param minTurnDeg TODO: document this
     */
    fun cornerCenters(path: Path, length: Float, sampleStep: Float, maxRadius: Float, gapTolerance: Float, minTurnDeg: Float = 30f): FloatArray {
        val measure = PathMeasure().apply { setPath(path, true) }
        val n = max(8, (length / sampleStep).toInt())
        val step = length / n
        val ang = FloatArray(n) { i ->
            val t = measure.getTangent(i * step)
            atan2(t.y, t.x)
        }
        val turn = FloatArray(n) { i ->
            var a = ang[(i + 1) % n] - ang[i]
            a = ((a + PI.toFloat()).mod(2f * PI.toFloat())) - PI.toFloat()
            a
        }
        val threshold = step / maxRadius
        val hot = BooleanArray(n) { abs(turn[it]) > threshold }
        if (hot.none { it }) return FloatArray(0)
        val gapN = (gapTolerance / step).toInt()
        if (gapN > 0) {
            val idx = hot.indices.filter { hot[it] }
            val filled = hot.copyOf()
            for (j in idx.indices) {
                val a = idx[j]
                val b = if (j + 1 < idx.size) idx[j + 1] else idx[0] + n
                if (b - a in 2..(gapN + 1)) for (k in a + 1 until b) filled[k % n] = true
            }
            filled.copyInto(hot)
        }
        if (hot.all { it }) return FloatArray(0)
        val start = hot.indexOfFirst { !it }
        val result = ArrayList<Float>()
        val group = ArrayList<Int>()
        // TODO: document this
        fun flush() {
            if (group.isEmpty()) return
            var total = 0f
            var weight = 0f
            var moment = 0f
            val base = group[0]
            for (g in group) {
                total += turn[g]
                val w = abs(turn[g])
                weight += w
                moment += ((g - base).mod(n)) * w
            }
            if (abs(Math.toDegrees(total.toDouble())) >= minTurnDeg) {
                val c = base + moment / max(weight, 1e-9f)
                result.add(((c + 0.5f) * step).mod(length))
            }
            group.clear()
        }
        for (k in 0 until n) {
            val i = (start + k) % n
            if (hot[i]) group.add(i) else flush()
        }
        flush()
        result.sort()
        return result.toFloatArray()
    }

    /**
     * Closed contour re-started [distance] along itself (same geometry).
     * @param length TODO: document this
     * @param distance TODO: document this
     */
    fun rotateStart(path: Path, measure: PathMeasure, length: Float, distance: Float): Path {
        val d = distance.mod(length)
        if (d < 1e-3f) return path
        return Path().apply {
            measure.getSegment(d, length, this, true)
            measure.getSegment(0f, d, this, false)
            close()
        }
    }

    /**
     * Moves a closed outline's start to the middle of its longest corner-free run, so pattern
     * seams never land on a corner (Compose shapes start their contours at a corner).
     */
    fun relocateSeam(path: Path, density: Density): Path {
        val len = length(path)
        val corners = with(density) {
            cornerCenters(path, len, CyberPathDefaults.Geometry.CornerSample.toPx(), CyberPathDefaults.Geometry.CornerMaxRadius.toPx(), CyberPathDefaults.Geometry.CornerGapTolerance.toPx())
        }
        if (corners.isEmpty()) return path
        val runs = FloatArray(corners.size)
        val mids = FloatArray(corners.size)
        for (i in corners.indices) {
            val c = corners[i]
            val next = if (i + 1 < corners.size) corners[i + 1] else corners[0] + len
            runs[i] = next - c
            mids[i] = (c + (next - c) / 2f).mod(len)
        }
        val longest = runs.max()
        // equal sides (within 1px) resolve to the one whose middle comes first: deterministic
        var start = Float.MAX_VALUE
        for (i in runs.indices) if (runs[i] >= longest - 1f && mids[i] < start) start = mids[i]
        return rotateStart(path, PathMeasure().apply { setPath(path, true) }, len, start)
    }

    // ------------------------------------------------------------------ dashes

    /**
     * Dash that is ON for [start, start + length). Closed contours wrap every [period];
     * open contours are clipped to [0, total] and never wrap round to the far end.
     * Returns null when nothing is visible.
      * @param total TODO: document this
      * @param start TODO: document this
      * @param length TODO: document this
      * @param period TODO: document this
      * @param closed TODO: document this
     */
    fun window(total: Float, start: Float, length: Float, period: Float = total, closed: Boolean = true): PathEffect? {
        if (!closed) {
            val a = max(0f, start)
            val b = min(total, start + length)
            if (b - a <= 0.01f) return null
            val gap = total * 4f + 1f // one 'on' interval, gap longer than the whole path
            return PathEffect.dashPathEffect(floatArrayOf(b - a, gap), (-a).mod(b - a + gap))
        }
        val on = max(0.01f, min(length, period - 0.01f)) // keep both dash intervals positive
        return PathEffect.dashPathEffect(floatArrayOf(on, period - on), (-start).mod(period))
    }

    /** Result of turning explicit spans into a dash: [full] means the spans cover the whole contour. */
    class Spans(val pathEffect: PathEffect?, val full: Boolean)

    /**
     * Dash for [a, b] spans (flattened pairs) on a closed contour; merged and wrap-aware.
     * @param total TODO: document this
     */
    fun spansDash(total: Float, spans: FloatArray): Spans {
        val list = ArrayList<FloatArray>()
        var i = 0
        while (i < spans.size) {
            val a = spans[i]
            val b = spans[i + 1]
            if (b - a > 0.01f) {
                val s = a.mod(total)
                list.add(floatArrayOf(s, s + (b - a)))
            }
            i += 2
        }
        if (list.isEmpty()) return Spans(null, false)
        list.sortBy { it[0] }
        val merged = ArrayList<FloatArray>()
        for (s in list) {
            val last = merged.lastOrNull()
            if (last != null && s[0] <= last[1]) last[1] = max(last[1], s[1]) else merged.add(s.copyOf())
        }
        if (merged.size > 1 && merged.last()[1] - total >= merged[0][0]) {
            merged[0][0] = merged.last()[0] - total
            merged.removeAt(merged.size - 1)
        }
        if (merged[0][1] - merged[0][0] >= total - 0.01f) return Spans(null, true)
        val intervals = FloatArray(merged.size * 2)
        for (k in merged.indices) {
            val next = if (k + 1 < merged.size) merged[k + 1][0] else merged[0][0] + total
            intervals[2 * k] = merged[k][1] - merged[k][0]
            intervals[2 * k + 1] = max(next - merged[k][1], 0.01f)
        }
        return Spans(PathEffect.dashPathEffect(intervals, (-merged[0][0]).mod(total)), false)
    }

    /**
     * Dash for [a, b] spans (flattened pairs) on an OPEN path: clipped to [0, total], never wraps.
     * @param total TODO: document this
     */
    fun openSpansDash(total: Float, spans: FloatArray): Spans {
        val list = ArrayList<FloatArray>()
        var i = 0
        while (i < spans.size) {
            val a = max(0f, spans[i])
            val b = min(total, spans[i + 1])
            if (b - a > 0.01f) list.add(floatArrayOf(a, b))
            i += 2
        }
        if (list.isEmpty()) return Spans(null, false)
        list.sortBy { it[0] }
        val merged = ArrayList<FloatArray>()
        for (s in list) {
            val last = merged.lastOrNull()
            if (last != null && s[0] <= last[1]) last[1] = max(last[1], s[1]) else merged.add(s.copyOf())
        }
        if (merged[0][0] <= 0.01f && merged[0][1] >= total - 0.01f) return Spans(null, true)
        val intervals = FloatArray(merged.size * 2)
        for (k in merged.indices) {
            intervals[2 * k] = merged[k][1] - merged[k][0]
            intervals[2 * k + 1] = if (k + 1 < merged.size) merged[k + 1][0] - merged[k][1] else 2f * total + 1f // last gap never wraps back in
        }
        val period = intervals.sum()
        return Spans(PathEffect.dashPathEffect(intervals, (-merged[0][0]).mod(period)), false)
    }

    /**
     * Stamped effect with the seam-safe advance stretch applied.
     * @param advance TODO: document this
     * @param phase TODO: document this
     */
    fun stamp(shape: Path, advance: Float, phase: Float, style: StampedPathEffectStyle): PathEffect =
        PathEffect.stampedPathEffect(shape, advance * (1f + STAMP_EPS), phase, style)

    // ------------------------------------------------------------------ timing helpers

    // TODO: document this
    fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    /** Deterministic pseudo-random value in [0, 1) for frame index [n] (classic GLSL-style sine hash). */
    fun hash01(n: Double): Float = (sin(n * 12.9898) * 43758.5453).mod(1.0).toFloat()

    // TODO: document this
    fun floorF(x: Float): Float = floor(x)
}

/**
 * Advances a stamped pattern by a fraction of one advance. Closed outlines rotate their
 * start (gap-free); open outlines use the stamp phase, so the pattern streams in from the start.
 */
class CyberMarching(private val outline: Path, private val closed: Boolean, private val advance: Float) {
    private val measure = PathMeasure().apply { setPath(outline, true) }
    private val length = measure.length

    /**
     * Stamped effect plus the path to draw it on (null = draw on the original outline).
     * @param progress TODO: document this
     * @param stampAdvance TODO: document this
     */
    fun at(shape: Path, style: StampedPathEffectStyle, progress: Float, stampAdvance: Float = advance): Pair<PathEffect, Path?> =
        if (closed) {
            CyberPathGeometry.stamp(shape, stampAdvance, 0f, style) to
                CyberPathGeometry.rotateStart(outline, measure, length, progress * advance)
        } else {
            CyberPathGeometry.stamp(shape, stampAdvance, (-(progress * advance)).mod(stampAdvance), style) to null
        }
}
