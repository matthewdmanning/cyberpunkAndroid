package com.example.cyberpunkandroid.icons

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Stroke geometry for centerlines of any shape: closed, concave, or crossing itself.
 *
 * The centerline is split into its segments. Each segment is thickened on its own into a
 * quad, and each corner gets a wedge that keeps the centerline's angle (the point where the
 * two outer offset lines meet, with no miter limit). The union of those convex pieces is the
 * stroke. Overlaps from inner corners, tight curves, thick strokes and self-crossings merge in
 * the union, so the outline is valid at every thickness. Curves are flattened finely enough
 * that their offsets keep the curve's shape (radius r ± offset around the same center).
 *
 * Pure Kotlin in doubles: no Compose types, so it runs in plain JVM tests.
 */

/** A point in pixels. */
internal data class Pt(val x: Double, val y: Double) {
    operator fun plus(o: Pt) = Pt(x + o.x, y + o.y)
    operator fun minus(o: Pt) = Pt(x - o.x, y - o.y)
    operator fun times(s: Double) = Pt(x * s, y * s)
    fun cross(o: Pt) = x * o.y - y * o.x
    fun dot(o: Pt) = x * o.x + y * o.y
    fun length() = hypot(x, y)
}

/** Axis-aligned bounds in pixels. */
internal class Bounds(val left: Double, val top: Double, val right: Double, val bottom: Double) {
    val width get() = right - left
    val height get() = bottom - top

    companion object {
        fun of(polygons: List<List<Pt>>): Bounds? {
            var l = Double.POSITIVE_INFINITY
            var t = Double.POSITIVE_INFINITY
            var r = Double.NEGATIVE_INFINITY
            var b = Double.NEGATIVE_INFINITY
            for (poly in polygons) for (p in poly) {
                l = min(l, p.x); t = min(t, p.y); r = max(r, p.x); b = max(b, p.y)
            }
            return if (l <= r && t <= b) Bounds(l, t, r, b) else null
        }
    }
}

/**
 * A flattened centerline contour in pixels. [curveStart] and [curveEnd] hold each segment's
 * signed curvature at its two ends, from the source curve (0 on lines, ±1/r on arcs). For a
 * closed contour the last segment runs from the last point back to the first.
 */
internal class Polyline(
    val points: List<Pt>,
    val closed: Boolean,
    val curveStart: DoubleArray = DoubleArray(points.size),
    val curveEnd: DoubleArray = DoubleArray(points.size),
    /** Per segment: true where it came from a straight line in the centerline. */
    val straight: BooleanArray = BooleanArray(points.size)
) {
    /** Per segment of a closed contour: +1 where its +normal side is outside the loop, else -1. */
    val outward: IntArray by lazy { outwardSides(points) }
}

/**
 * Filled stroke region. [solids] are outer edges, clockwise on screen (positive shoelace
 * area with y down); [holes] are inner edges, counter-clockwise. Filled with the non-zero
 * rule, the holes cut out. Every loop is simple: it never crosses itself or another loop.
 */
internal class StrokeRegion(val solids: List<List<Pt>>, val holes: List<List<Pt>>, val bounds: Bounds)

/** How thick the drawn stroke is: the request, or the thickest that fits the space ([maxStrokeWidth]). */
internal class StrokeFit(val strokeWidth: Double, val maxStrokeWidth: Double, val load: Double, val bounds: Bounds)

// Largest distance, in px, between a flattened curve (or its offset) and the true curve.
private const val FlattenTolerance = 0.2

// Turn limits per flattened step: 15° keeps thin offsets smooth; 1° stops runaway subdivision.
private const val MaxStepTurn = 15.0 * PI / 180.0
private const val MinStepTurn = 1.0 * PI / 180.0

// Turn limits are rounded down to steps of this size, so nearby thicknesses share a flattening.
private const val TurnBucket = 0.5 * PI / 180.0

// Cubic subdivision depth; 2^16 pieces is far past any visible error.
private const val MaxCubicDepth = 16

/**
 * Thickens [contours] (viewport units, scaled by [scale] to px) with [alignment] for closed
 * contours. The centerline never moves; the stroke grows around it.
 */
internal class CenterlineStroker(
    private val contours: List<CenterlineContour>,
    private val scale: Double,
    private val alignment: CyberStrokeAlignment,
    private val minGap: Double = DefaultMinGap
) {
    // Flattenings by turn limit; a thickness search reuses them instead of re-flattening.
    private val flattened = HashMap<Int, List<Polyline>>()

    fun lines(strokeWidth: Double): List<Polyline> {
        // A step that turns by θ puts the offset's corner reach/cos(θ/2) out instead of reach.
        val reach = strokeWidth
        val turn = (2.0 * acos(1.0 / (1.0 + FlattenTolerance / reach))).coerceIn(MinStepTurn, MaxStepTurn)
        val bucket = floor(turn / TurnBucket).toInt()
        return flattened.getOrPut(bucket) {
            flatten(contours, scale, FlattenTolerance, max(bucket * TurnBucket, MinStepTurn))
        }
    }

    fun pieces(strokeWidth: Double): List<List<Pt>> =
        strokePieces(crowdedLines(lines(strokeWidth), strokeWidth, minGap), strokeWidth, alignment)

    fun bounds(strokeWidth: Double): Bounds? = Bounds.of(pieces(strokeWidth))

    fun region(strokeWidth: Double): StrokeRegion? {
        val pieces = pieces(strokeWidth)
        val bounds = Bounds.of(pieces) ?: return null
        val loops = unionOutline(pieces)
        if (loops.isEmpty()) return null
        return StrokeRegion(
            solids = loops.filter { signedArea(it) > 0.0 },
            holes = loops.filter { signedArea(it) < 0.0 },
            bounds = bounds
        )
    }

    /**
     * The drawn stroke width for a space of [maxWidth] × [maxHeight] px. The space is the only
     * limit: past it the stroke is held at the thickest that fits and [StrokeFit.load] goes
     * above 1. Null when even a hairline doesn't fit, or there is nothing to stroke.
     */
    fun fit(requested: Double, maxWidth: Double, maxHeight: Double): StrokeFit? {
        if (!(requested > 0.0) || requested.isInfinite()) return null
        // One flattening for the whole search: it moves the bounds by far less than a pixel.
        val lines = crowdedLines(lines(requested), requested, minGap)
        fun fits(w: Double): Bounds? =
            Bounds.of(strokePieces(lines, w, alignment))?.takeIf { it.width <= maxWidth && it.height <= maxHeight }
        if (maxWidth.isInfinite() && maxHeight.isInfinite()) {
            val bounds = bounds(requested) ?: return null
            return StrokeFit(requested, Double.POSITIVE_INFINITY, 0.0, bounds)
        }
        val requestedBounds = fits(requested)
        var lo = 0.0
        var loBounds: Bounds? = null
        var hi = requested
        if (requestedBounds != null) {
            lo = requested
            loBounds = requestedBounds
            // Grow until the space runs out; past the bound below, report no limit.
            hi = requested * 2.0
            while (true) {
                if (hi >= UnboundedStroke) return StrokeFit(requested, Double.POSITIVE_INFINITY, 0.0, requestedBounds)
                loBounds = fits(hi) ?: break
                lo = hi
                hi *= 2.0
            }
        }
        repeat(FitSteps) {
            val mid = (lo + hi) / 2.0
            val b = fits(mid)
            if (b != null) {
                lo = mid
                loBounds = b
            } else {
                hi = mid
            }
        }
        if (requestedBounds != null) return StrokeFit(requested, lo, requested / lo, requestedBounds)
        val held = loBounds ?: return null
        return StrokeFit(lo, lo, requested / lo, held)
    }

    private companion object {
        // Bisection steps; each halves the bracket, so these resolve far below a pixel.
        const val FitSteps = 30

        // A stroke this wide (px) is past any screen, so the space doesn't limit it.
        const val UnboundedStroke = 1e6
    }
}

// ------------------------------------------------------------------ flattening

internal fun flatten(contours: List<CenterlineContour>, scale: Double, tolerance: Double, maxTurn: Double): List<Polyline> {
    val out = ArrayList<Polyline>(contours.size)
    for (c in contours) {
        val path = FlatPath(Pt(c.startX * scale, c.startY * scale))
        for (s in c.segments) {
            when (s) {
                is CenterlineSegment.Line -> path.add(Pt(s.x * scale, s.y * scale), 0.0, 0.0, straight = true)
                is CenterlineSegment.Cubic -> {
                    val curve = CubicCurve(arrayOf(path.last, Pt(s.x1 * scale, s.y1 * scale), Pt(s.x2 * scale, s.y2 * scale), Pt(s.x * scale, s.y * scale)))
                    // Pieces that bend one way and are monotonic in x and y, split at the curve's
                    // critical points and inflections.
                    val cuts = listOf(0.0) + curve.splitParameters() + 1.0
                    for (k in 0 until cuts.size - 1) {
                        val piece = curve.piece(cuts[k], cuts[k + 1])
                        flattenCubic(curve, cuts[k], cuts[k + 1], piece[0], piece[1], piece[2], piece[3], tolerance, maxTurn, 0, path)
                    }
                }
                is CenterlineSegment.Arc -> {
                    val center = Pt(s.cx * scale, s.cy * scale)
                    val cur = path.last
                    val r = (cur - center).length()
                    if (r > 0.0) {
                        val a0 = atan2(cur.y - center.y, cur.x - center.x)
                        val sweep = s.sweepDegrees * PI / 180.0
                        // A positive sweep turns toward +normal, the center.
                        val k = if (sweep > 0) 1.0 / r else -1.0 / r
                        val chordTurn = if (tolerance >= r) PI / 2.0 else 2.0 * acos(1.0 - tolerance / r)
                        val n = max(1, ceil(abs(sweep) / min(maxTurn, chordTurn)).toInt())
                        for (step in 1..n) {
                            val a = a0 + sweep * step / n
                            path.add(Pt(center.x + r * cos(a), center.y + r * sin(a)), k, k)
                        }
                    }
                }
            }
        }
        path.toPolyline(c.closed)?.let { out += it }
    }
    return out
}

/** Points with the curvature of the segment that ends at each one. */
private class FlatPath(start: Pt) {
    val points = arrayListOf(start)
    val curveStart = arrayListOf(0.0) // entry k is the segment ending at point k; entry 0 is unused
    val curveEnd = arrayListOf(0.0)
    val straight = arrayListOf(true) // the implicit closing segment is a straight line
    val last: Pt get() = points.last()

    fun add(p: Pt, kStart: Double, kEnd: Double, straight: Boolean = false) {
        if ((p - points.last()).length() <= SamePoint) return
        points += p
        curveStart += kStart
        curveEnd += kEnd
        this.straight += straight
    }

    fun toPolyline(closedRequested: Boolean): Polyline? {
        val pts = ArrayList(points)
        val ks = ArrayList(curveStart)
        val ke = ArrayList(curveEnd)
        val st = ArrayList(straight)
        var closed = closedRequested
        // A segment ending back at the start is the closing segment: keep its curvature in slot 0.
        if (closed && pts.size > 1 && (pts.first() - pts.last()).length() <= SamePoint) {
            ks[0] = ks.removeAt(ks.lastIndex)
            ke[0] = ke.removeAt(ke.lastIndex)
            st[0] = st.removeAt(st.lastIndex)
            pts.removeAt(pts.lastIndex)
        }
        if (closed && pts.size < 3) closed = false
        if (pts.size < 2) return null
        val count = if (closed) pts.size else pts.size - 1
        // Segment i runs from point i to point i + 1, which is entry i + 1 (or 0 for the closing one).
        return Polyline(
            pts, closed,
            DoubleArray(count) { ks[(it + 1) % pts.size] },
            DoubleArray(count) { ke[(it + 1) % pts.size] },
            BooleanArray(count) { st[(it + 1) % pts.size] }
        )
    }
}

// Points closer than this (px) are the same point.
private const val SamePoint = 1e-9

/** Flattens the piece of [curve] between [t0] and [t1], whose control points are p0–p3. */
private fun flattenCubic(
    curve: CubicCurve, t0: Double, t1: Double,
    p0: Pt, p1: Pt, p2: Pt, p3: Pt,
    tolerance: Double, maxTurn: Double, depth: Int, out: FlatPath
) {
    val flat = max(distanceToLine(p1, p0, p3), distanceToLine(p2, p0, p3)) <= tolerance
    val chord = p3 - p0
    val start = firstNonZero(p1 - p0, p2 - p0, chord)
    val end = firstNonZero(p3 - p2, p3 - p1, chord)
    val turn = if (start == null || end == null) 0.0
    else if (chord.length() > SamePoint) angleBetween(start, chord) + angleBetween(chord, end) else angleBetween(start, end)
    if (depth >= MaxCubicDepth || (flat && turn <= maxTurn)) {
        out.add(p3, curve.curvature(t0), curve.curvature(t1))
        return
    }
    // de Casteljau split at the middle.
    val a = (p0 + p1) * 0.5
    val b = (p1 + p2) * 0.5
    val c = (p2 + p3) * 0.5
    val ab = (a + b) * 0.5
    val bc = (b + c) * 0.5
    val mid = (ab + bc) * 0.5
    val tm = (t0 + t1) / 2.0
    flattenCubic(curve, t0, tm, p0, a, ab, mid, tolerance, maxTurn, depth + 1, out)
    flattenCubic(curve, tm, t1, mid, bc, c, p3, tolerance, maxTurn, depth + 1, out)
}

private fun firstNonZero(vararg v: Pt): Pt? = v.firstOrNull { it.length() > SamePoint }

private fun angleBetween(a: Pt, b: Pt): Double = abs(atan2(a.cross(b), a.dot(b)))

private fun distanceToLine(p: Pt, a: Pt, b: Pt): Double {
    val ab = b - a
    val len = ab.length()
    return if (len <= SamePoint) (p - a).length() else abs(ab.cross(p - a)) / len
}

// ------------------------------------------------------------------ pieces

/**
 * Convex pieces whose union is the stroke: one quad per segment and one wedge per corner,
 * on the outer side of the turn. Closed contours with [CyberStrokeAlignment.Outside] or
 * [CyberStrokeAlignment.Inside] put the whole width on one side of each segment. Each
 * segment end's reach on each side is capped by its [StrokeLine] room, so the quads become
 * trapezoids that taper where other lines come close.
 */
internal fun strokePieces(lines: List<StrokeLine>, strokeWidth: Double, alignment: CyberStrokeAlignment): List<List<Pt>> {
    val pieces = ArrayList<List<Pt>>()
    for (line in lines) {
        val pts = line.points
        val n = pts.size
        val closed = line.closed
        val segCount = if (closed) n else n - 1
        val dirs = line.directions
        val normals = Array(segCount) { Pt(-dirs[it].y, dirs[it].x) }
        // Offsets along the normal at each segment's start (A) and end (B).
        val loA = DoubleArray(segCount)
        val hiA = DoubleArray(segCount)
        val loB = DoubleArray(segCount)
        val hiB = DoubleArray(segCount)
        val oneSided = closed && alignment != CyberStrokeAlignment.Center
        // Open contours lean outside; closed ones follow their alignment exactly.
        val lean = if (closed) DoubleArray(n) else convexLean(line, strokeWidth)
        for (i in 0 until segCount) {
            if (oneSided) {
                val toward = if (alignment == CyberStrokeAlignment.Outside) line.outward[i] else -line.outward[i]
                val lo = if (toward > 0) 0.0 else -strokeWidth
                val hi = if (toward > 0) strokeWidth else 0.0
                loA[i] = lo; hiA[i] = hi; loB[i] = lo; hiB[i] = hi
            } else {
                val leanA = lean[i]
                val leanB = lean[(i + 1) % lean.size]
                loA[i] = -strokeWidth * (1 - leanA) / 2; hiA[i] = strokeWidth * (1 + leanA) / 2
                loB[i] = -strokeWidth * (1 - leanB) / 2; hiB[i] = strokeWidth * (1 + leanB) / 2
            }
            loA[i] = max(loA[i], -line.minusA[i]); hiA[i] = min(hiA[i], line.plusA[i])
            loB[i] = max(loB[i], -line.minusB[i]); hiB[i] = min(hiB[i], line.plusB[i])
            val a = pts[i]
            val b = pts[(i + 1) % n]
            pieces += listOf(a + normals[i] * loA[i], b + normals[i] * loB[i], b + normals[i] * hiB[i], a + normals[i] * hiA[i])
        }
        val corners = if (closed) 0 until n else 1 until n - 1
        for (k in corners) {
            val j = if (k == 0) segCount - 1 else k - 1
            val i = k
            val turn = dirs[j].cross(dirs[i])
            if (abs(turn) < ParallelTurn) continue // straight on, or a full reversal with no finite corner
            // turn > 0 bends toward +normal, so the outer side of the corner is -normal.
            val oj = if (turn > 0) loB[j] else hiB[j]
            val oi = if (turn > 0) loA[i] else hiA[i]
            if (oj == 0.0 || oi == 0.0 || (oj > 0) != (oi > 0)) continue
            val p = pts[k]
            val a = p + normals[j] * oj
            val b = p + normals[i] * oi
            val t = (b - a).cross(dirs[i]) / dirs[j].cross(dirs[i])
            val corner = a + dirs[j] * t
            // The offset lines meet ahead of segment j's end and behind segment i's start: the
            // corner keeps the centerline's angle. When the two sides' offsets differ (the
            // stroke is tapering here) they can meet far away or behind; then bevel.
            pieces += if (t >= 0 && (corner - b).dot(dirs[i]) <= 0) listOf(p, a, corner, b) else listOf(p, a, b)
        }
    }
    return pieces
}

/**
 * How far each vertex's stroke leans to one side, −1 (all on −normal) to 1 (all on +normal).
 *
 * Curves lean toward their convex side, away from the center of the bend, by a share that
 * grows with curvature × width: a bend tighter than the width puts the whole stroke outside.
 * Then, unsmoothed, the inner reach w(1 − w/r)/2 stays below the radius r, so the stroke never
 * fills the inside of a curl.
 *
 * Straight lines lean fully to their outside, from [StrokeLine.sideLean]: the side opposite
 * the nearest place where the line's perpendicular meets another part of the shape.
 *
 * The lean is averaged over one stroke width of arc around each vertex, so it ramps through
 * curve joins and corners instead of stepping.
 */
private fun convexLean(line: StrokeLine, strokeWidth: Double): DoubleArray {
    val pts = line.points
    val n = pts.size
    val segCount = if (line.closed) n else n - 1
    // Curvature > 0 bends toward +normal, so its convex side is −normal.
    fun curveLean(k: Double) = -kotlin.math.sign(k) * min(1.0, abs(k) * strokeWidth)
    fun leanA(i: Int) = line.sideLean[i].takeUnless { it.isNaN() } ?: curveLean(line.curveA[i])
    fun leanB(i: Int) = line.sideLean[i].takeUnless { it.isNaN() } ?: curveLean(line.curveB[i])
    val raw = DoubleArray(n) { v ->
        val ends = ArrayList<Double>(2)
        if (line.closed || v < segCount) ends += leanA(v % segCount)
        if (line.closed || v > 0) ends += leanB((v - 1 + segCount) % segCount)
        ends.average()
    }
    // Vertex arc positions; closed lines repeat once on each side so windows can wrap.
    val arc = DoubleArray(n)
    for (v in 1 until n) arc[v] = arc[v - 1] + (pts[v] - pts[v - 1]).length()
    val total = if (line.closed) arc[n - 1] + (pts[0] - pts[n - 1]).length() else arc[n - 1]
    val copies = if (line.closed) 3 else 1
    val at = DoubleArray(n * copies) { arc[it % n] + (if (line.closed) (it / n - 1) * total else 0.0) }
    val prefix = DoubleArray(n * copies + 1)
    for (u in at.indices) prefix[u + 1] = prefix[u] + raw[u % n]
    val window = strokeWidth / 2
    fun firstAtLeast(x: Double): Int {
        var lo = 0
        var hi = at.size
        while (lo < hi) { val mid = (lo + hi) / 2; if (at[mid] < x) lo = mid + 1 else hi = mid }
        return lo
    }
    val tiny = 1e-9 * (total + 1.0)
    return DoubleArray(n) { v ->
        val center = arc[v]
        // On a closed loop the window covers at most the loop once (half each way).
        val reach = if (line.closed) min(window, total / 2 - tiny) else window
        val from = firstAtLeast(center - reach)
        val to = firstAtLeast(center + reach + tiny)
        (prefix[to] - prefix[from]) / (to - from)
    }
}

// Sine of the smallest turn that still gets a corner wedge.
private const val ParallelTurn = 1e-9

/** +1 where a segment's +normal side is outside its own closed contour, -1 where it is inside. */
private fun outwardSides(pts: List<Pt>): IntArray {
    val bounds = Bounds.of(listOf(pts))!!
    val probe = hypot(bounds.width, bounds.height) * 1e-6
    // Positive area (clockwise on screen) has its inside on the +normal side.
    val fallback = if (signedArea(pts) > 0.0) -1 else 1
    return IntArray(pts.size) { i ->
        val a = pts[i]
        val b = pts[(i + 1) % pts.size]
        val d = b - a
        val normal = Pt(-d.y, d.x) * (1.0 / d.length())
        val mid = (a + b) * 0.5
        val plusInside = winding(mid + normal * probe, pts) != 0
        val minusInside = winding(mid - normal * probe, pts) != 0
        when {
            !plusInside && minusInside -> 1
            plusInside && !minusInside -> -1
            else -> fallback // a segment between two overlapping lobes: follow the contour's direction
        }
    }
}

private fun winding(p: Pt, poly: List<Pt>): Int {
    var w = 0
    for (i in poly.indices) {
        val a = poly[i]
        val b = poly[(i + 1) % poly.size]
        val side = (b - a).cross(p - a)
        if (a.y <= p.y) {
            if (b.y > p.y && side > 0) w++
        } else if (b.y <= p.y && side < 0) {
            w--
        }
    }
    return w
}

internal fun signedArea(pts: List<Pt>): Double {
    var a = 0.0
    for (i in pts.indices) {
        val p = pts[i]
        val q = pts[(i + 1) % pts.size]
        a += p.x * q.y - q.x * p.y
    }
    return a / 2.0
}

// ------------------------------------------------------------------ union

/**
 * Outline of the union of convex [pieces]: the edges with filled space on exactly one side,
 * linked into loops. Solids come out clockwise on screen, holes counter-clockwise.
 *
 * Runs on primitive arrays: it reruns whenever a thickness slider moves.
 */
internal fun unionOutline(pieces: List<List<Pt>>): List<List<Pt>> {
    val all = Bounds.of(pieces) ?: return emptyList()
    val size = max(hypot(all.width, all.height), 1e-6)
    val set = PieceSet(pieces, size)
    if (set.count == 0) return emptyList()
    val onLine = size * 1e-9

    // An edge with both ends strictly inside another (convex) piece lies wholly inside it, so it
    // can't be on the outline. Dropping these first keeps thick strokes fast.
    val edges = EdgeList()
    for (k in 0 until set.count) {
        for (i in set.start[k] until set.start[k + 1]) {
            val j = if (i + 1 == set.start[k + 1]) set.start[k] else i + 1
            val ax = set.x[i]; val ay = set.y[i]; val bx = set.x[j]; val by = set.y[j]
            if (hypot(bx - ax, by - ay) <= size * 1e-12) continue
            var buried = false
            set.forCandidates(ax, ay) { q ->
                if (q != k && set.strictlyInside(q, ax, ay, onLine) && set.strictlyInside(q, bx, by, onLine)) {
                    buried = true
                    true
                } else false
            }
            if (!buried) edges.add(ax, ay, bx, by)
        }
    }
    val n = edges.size
    if (n == 0) return emptyList()

    // 1. Split every edge where another edge crosses or touches it. A sweep along x over edges
    // sorted by their left end; float sort keys are only nearly exact, so the cutoff has slack.
    val order = LongArray(n) { i -> (sortableFloat(edges.minX(i).toFloat()).toLong() shl 32) or i.toLong() }
    order.sort()
    val splits = SplitList()
    val touch = size * 1e-9
    val slack = touch + size * 1e-6
    for (oi in 0 until n) {
        val i = order[oi].toInt()
        val maxX = edges.maxX(i)
        for (oj in oi + 1 until n) {
            val j = order[oj].toInt()
            if (edges.minX(j) > maxX + slack) break
            if (edges.minY(j) > edges.maxY(i) + touch || edges.maxY(j) < edges.minY(i) - touch) continue
            intersect(i, j, edges, splits, touch)
        }
    }

    // 2. Sub-edges between merged vertices, each once regardless of direction.
    val vertices = VertexIndex(size * 1e-7)
    val bySplit = splits.groupedByEdge(n)
    val keys = LongArrayList()
    for (i in 0 until n) {
        var prev = vertices.id(edges.ax[i], edges.ay[i])
        for (s in bySplit.start[i] until bySplit.start[i + 1] + 1) {
            val next = if (s == bySplit.start[i + 1]) vertices.id(edges.bx[i], edges.by[i])
            else bySplit.order[s].let { vertices.id(splits.x[it], splits.y[it]) }
            if (next != prev) {
                keys.add(min(prev, next).toLong() shl 32 or max(prev, next).toLong())
                prev = next
            }
        }
    }
    val unique = keys.sortedUnique()

    // 3. Keep the sub-edges with filled space on exactly one side; filled goes on the left of
    // the direction (cross > 0), which is the right-hand side on screen.
    val keptFrom = IntArrayList()
    val keptTo = IntArrayList()
    for (key in unique) {
        val a = (key ushr 32).toInt()
        val b = key.toInt()
        when (set.filledSides(vertices.x[a], vertices.y[a], vertices.x[b], vertices.y[b], onLine)) {
            FilledLeft -> { keptFrom.add(a); keptTo.add(b) }
            FilledRight -> { keptFrom.add(b); keptTo.add(a) }
        }
    }

    // 4. Link them into loops, then split any loop that passes through a vertex twice (loops
    // that touch at a point), so every loop is simple.
    val m = keptFrom.size
    val outStart = IntArray(vertices.size + 1)
    for (e in 0 until m) outStart[keptFrom[e] + 1]++
    for (v in 0 until vertices.size) outStart[v + 1] += outStart[v]
    val outEdges = IntArray(m)
    val fill = outStart.copyOf()
    for (e in 0 until m) outEdges[fill[keptFrom[e]]++] = e
    val used = BooleanArray(m)
    val loops = ArrayList<List<Pt>>()
    for (startEdge in 0 until m) {
        if (used[startEdge]) continue
        val start = keptFrom[startEdge]
        val ids = ArrayList<Int>()
        var edge = startEdge
        var closed = false
        while (true) {
            used[edge] = true
            val from = keptFrom[edge]
            val to = keptTo[edge]
            ids += from
            if (to == start) {
                closed = true
                break
            }
            val dinX = vertices.x[to] - vertices.x[from]
            val dinY = vertices.y[to] - vertices.y[from]
            var next = -1
            var bestTurn = Double.NEGATIVE_INFINITY
            for (o in outStart[to] until outStart[to + 1]) {
                val candidate = outEdges[o]
                if (used[candidate]) continue
                val doutX = vertices.x[keptTo[candidate]] - vertices.x[to]
                val doutY = vertices.y[keptTo[candidate]] - vertices.y[to]
                val turn = atan2(dinX * doutY - dinY * doutX, dinX * doutX + dinY * doutY)
                if (turn > bestTurn) {
                    bestTurn = turn
                    next = candidate
                }
            }
            if (next < 0) break
            edge = next
        }
        if (!closed) continue // an unmatched edge from rounding; it bounds no area
        for (part in splitAtRepeats(ids)) {
            val simple = dropCollinear(part.map { Pt(vertices.x[it], vertices.y[it]) }, size)
            if (simple.size >= 3 && abs(signedArea(simple)) > size * size * 1e-12) loops += simple
        }
    }
    return loops
}

private const val FilledLeft = 1
private const val FilledRight = 2

/** Maps a float to an int whose signed order matches the float's order. */
private fun sortableFloat(f: Float): Int {
    val bits = java.lang.Float.floatToRawIntBits(f)
    return bits xor ((bits shr 31) and 0x7fffffff)
}

/** Convex pieces, oriented with positive area, plus a uniform grid for point queries. */
private class PieceSet(pieces: List<List<Pt>>, size: Double) {
    val start: IntArray
    val x: DoubleArray
    val y: DoubleArray
    val count: Int
    private val left: DoubleArray
    private val top: DoubleArray
    private val right: DoubleArray
    private val bottom: DoubleArray
    private val gridLeft: Double
    private val gridTop: Double
    private val cellWidth: Double
    private val cellHeight: Double
    private val cellStart = IntArray(Cells * Cells + 1)
    private val cellItems: IntArray

    init {
        val kept = pieces.mapNotNull { piece ->
            val area = signedArea(piece)
            when {
                abs(area) <= size * size * 1e-14 -> null
                area < 0.0 -> piece.asReversed()
                else -> piece
            }
        }
        count = kept.size
        start = IntArray(count + 1)
        for (k in kept.indices) start[k + 1] = start[k] + kept[k].size
        x = DoubleArray(start[count])
        y = DoubleArray(start[count])
        left = DoubleArray(count); top = DoubleArray(count); right = DoubleArray(count); bottom = DoubleArray(count)
        for ((k, piece) in kept.withIndex()) {
            left[k] = Double.POSITIVE_INFINITY; top[k] = Double.POSITIVE_INFINITY
            right[k] = Double.NEGATIVE_INFINITY; bottom[k] = Double.NEGATIVE_INFINITY
            for ((i, p) in piece.withIndex()) {
                x[start[k] + i] = p.x
                y[start[k] + i] = p.y
                left[k] = min(left[k], p.x); top[k] = min(top[k], p.y)
                right[k] = max(right[k], p.x); bottom[k] = max(bottom[k], p.y)
            }
        }
        gridLeft = left.minOrNull() ?: 0.0
        gridTop = top.minOrNull() ?: 0.0
        cellWidth = max(((right.maxOrNull() ?: 0.0) - gridLeft) / Cells, 1e-9)
        cellHeight = max(((bottom.maxOrNull() ?: 0.0) - gridTop) / Cells, 1e-9)
        for (k in 0 until count) forCells(k) { cell -> cellStart[cell + 1]++ }
        for (c in 0 until Cells * Cells) cellStart[c + 1] += cellStart[c]
        cellItems = IntArray(cellStart[Cells * Cells])
        val fill = cellStart.copyOf()
        for (k in 0 until count) forCells(k) { cell -> cellItems[fill[cell]++] = k }
    }

    private inline fun forCells(k: Int, visit: (Int) -> Unit) {
        for (cy in row(top[k])..row(bottom[k])) for (cx in column(left[k])..column(right[k])) visit(cy * Cells + cx)
    }

    inline fun forCandidates(px: Double, py: Double, visit: (Int) -> Boolean) {
        val cell = row(py) * Cells + column(px)
        for (i in cellStart[cell] until cellStart[cell + 1]) if (visit(cellItems[i])) return
    }

    fun strictlyInside(k: Int, px: Double, py: Double, onLine: Double): Boolean {
        if (px < left[k] || px > right[k] || py < top[k] || py > bottom[k]) return false
        for (i in start[k] until start[k + 1]) {
            val j = if (i + 1 == start[k + 1]) start[k] else i + 1
            val ex = x[j] - x[i]
            val ey = y[j] - y[i]
            if ((ex * (py - y[i]) - ey * (px - x[i])) / hypot(ex, ey) <= onLine) return false
        }
        return true
    }

    /**
     * Which side of the sub-edge a→b is inside some piece. Exact for the slivers where two
     * offset lines meet at a small angle: a piece whose edge carries the sub-edge fills only its
     * own side; a piece that holds the midpoint strictly inside fills both sides.
     */
    fun filledSides(ax: Double, ay: Double, bx: Double, by: Double, onLine: Double): Int {
        val mx = (ax + bx) * 0.5
        val my = (ay + by) * 0.5
        val leftX = -(by - ay)
        val leftY = bx - ax
        var sides = 0
        forCandidates(mx, my) { k ->
            if (mx < left[k] - onLine || mx > right[k] + onLine || my < top[k] - onLine || my > bottom[k] + onLine) return@forCandidates false
            var outside = false
            var nx = 0.0
            var ny = 0.0
            var onEdge = false
            for (i in start[k] until start[k + 1]) {
                val j = if (i + 1 == start[k + 1]) start[k] else i + 1
                val ex = x[j] - x[i]
                val ey = y[j] - y[i]
                val distance = (ex * (my - y[i]) - ey * (mx - x[i])) / hypot(ex, ey) // > 0 inside
                if (distance < -onLine) {
                    outside = true
                    break
                }
                if (distance <= onLine) {
                    onEdge = true
                    nx = -ey
                    ny = ex
                }
            }
            if (outside) return@forCandidates false
            sides = if (!onEdge) FilledLeft or FilledRight
            else sides or if (nx * leftX + ny * leftY > 0.0) FilledLeft else FilledRight
            sides == FilledLeft or FilledRight
        }
        return sides
    }

    private fun column(px: Double) = ((px - gridLeft) / cellWidth).toInt().coerceIn(0, Cells - 1)
    private fun row(py: Double) = ((py - gridTop) / cellHeight).toInt().coerceIn(0, Cells - 1)

}

// Grid cells per side; icons have hundreds of pieces, so this keeps each cell's list short.
private const val Cells = 32

private class EdgeList {
    var ax = DoubleArray(64); var ay = DoubleArray(64); var bx = DoubleArray(64); var by = DoubleArray(64)
    var size = 0

    fun add(x0: Double, y0: Double, x1: Double, y1: Double) {
        if (size == ax.size) {
            ax = ax.copyOf(size * 2); ay = ay.copyOf(size * 2); bx = bx.copyOf(size * 2); by = by.copyOf(size * 2)
        }
        ax[size] = x0; ay[size] = y0; bx[size] = x1; by[size] = y1
        size++
    }

    fun minX(i: Int) = min(ax[i], bx[i])
    fun maxX(i: Int) = max(ax[i], bx[i])
    fun minY(i: Int) = min(ay[i], by[i])
    fun maxY(i: Int) = max(ay[i], by[i])
}

/** Split points: which edge, where along it (t), and the point. */
private class SplitList {
    var edge = IntArray(64); var t = DoubleArray(64); var x = DoubleArray(64); var y = DoubleArray(64)
    var size = 0

    fun add(e: Int, at: Double, px: Double, py: Double) {
        if (size == edge.size) {
            edge = edge.copyOf(size * 2); t = t.copyOf(size * 2); x = x.copyOf(size * 2); y = y.copyOf(size * 2)
        }
        edge[size] = e; t[size] = at; x[size] = px; y[size] = py
        size++
    }

    class Grouped(val start: IntArray, val order: IntArray)

    /** Split indices grouped by edge, in order along each edge. */
    fun groupedByEdge(edges: Int): Grouped {
        val start = IntArray(edges + 1)
        for (s in 0 until size) start[edge[s] + 1]++
        for (e in 0 until edges) start[e + 1] += start[e]
        val order = IntArray(size)
        val fill = start.copyOf()
        for (s in 0 until size) order[fill[edge[s]]++] = s
        for (e in 0 until edges) { // few splits per edge: insertion sort by t
            for (a in start[e] + 1 until start[e + 1]) {
                val v = order[a]
                var b = a - 1
                while (b >= start[e] && t[order[b]] > t[v]) {
                    order[b + 1] = order[b]
                    b--
                }
                order[b + 1] = v
            }
        }
        return Grouped(start, order)
    }
}

private class IntArrayList {
    private var data = IntArray(64)
    var size = 0
        private set

    fun add(v: Int) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = v
    }

    operator fun get(i: Int) = data[i]
}

private class LongArrayList {
    private var data = LongArray(64)
    private var size = 0

    fun add(v: Long) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = v
    }

    fun sortedUnique(): LongArray {
        val sorted = data.copyOf(size)
        sorted.sort()
        var n = 0
        for (i in sorted.indices) if (i == 0 || sorted[i] != sorted[i - 1]) sorted[n++] = sorted[i]
        return sorted.copyOf(n)
    }
}

private fun intersect(i: Int, j: Int, edges: EdgeList, splits: SplitList, touch: Double) {
    val px = edges.ax[i]; val py = edges.ay[i]
    val rx = edges.bx[i] - px; val ry = edges.by[i] - py
    val qx = edges.ax[j]; val qy = edges.ay[j]
    val sx = edges.bx[j] - qx; val sy = edges.by[j] - qy
    val rl = hypot(rx, ry)
    val sl = hypot(sx, sy)
    val den = rx * sy - ry * sx
    val ti = touch / rl
    val tj = touch / sl
    val qpx = qx - px
    val qpy = qy - py
    if (abs(den) > 1e-12 * rl * sl) {
        val t = (qpx * sy - qpy * sx) / den
        val u = (qpx * ry - qpy * rx) / den
        if (t < -ti || t > 1 + ti || u < -tj || u > 1 + tj) return
        val tInside = t > ti && t < 1 - ti
        val uInside = u > tj && u < 1 - tj
        if (!tInside && !uInside) return
        // Reuse an existing endpoint when the crossing is at one, so shared vertices stay exact.
        val cx: Double
        val cy: Double
        when {
            !tInside -> if (t <= ti) { cx = px; cy = py } else { cx = edges.bx[i]; cy = edges.by[i] }
            !uInside -> if (u <= tj) { cx = qx; cy = qy } else { cx = edges.bx[j]; cy = edges.by[j] }
            else -> { cx = px + rx * t; cy = py + ry * t }
        }
        if (tInside) splits.add(i, t, cx, cy)
        if (uInside) splits.add(j, u, cx, cy)
    } else {
        if (abs(qpx * ry - qpy * rx) / rl > touch) return // parallel, not on the same line
        for (end in 0..1) {
            val ex = if (end == 0) qx else edges.bx[j]
            val ey = if (end == 0) qy else edges.by[j]
            val t = ((ex - px) * rx + (ey - py) * ry) / (rl * rl)
            if (t > ti && t < 1 - ti) splits.add(i, t, ex, ey)
        }
        for (end in 0..1) {
            val ex = if (end == 0) px else edges.bx[i]
            val ey = if (end == 0) py else edges.by[i]
            val u = ((ex - qx) * sx + (ey - qy) * sy) / (sl * sl)
            if (u > tj && u < 1 - tj) splits.add(j, u, ex, ey)
        }
    }
}

/** Splits a closed vertex loop at every vertex it visits twice. */
private fun splitAtRepeats(ids: List<Int>): List<List<Int>> {
    val parts = ArrayList<List<Int>>()
    val stack = ArrayList<Int>()
    val position = HashMap<Int, Int>()
    for (id in ids) {
        val at = position[id]
        if (at != null) {
            val part = stack.subList(at, stack.size)
            parts += part.toList()
            for (v in part) position.remove(v)
            part.clear()
        }
        position[id] = stack.size
        stack += id
    }
    parts += stack.toList()
    return parts
}

private fun dropCollinear(loop: List<Pt>, size: Double): List<Pt> {
    var pts = loop
    var changed = true
    while (changed && pts.size > 3) {
        changed = false
        val out = ArrayList<Pt>(pts.size)
        for (i in pts.indices) {
            val a = out.lastOrNull() ?: pts[(i - 1 + pts.size) % pts.size]
            val b = pts[i]
            val c = pts[(i + 1) % pts.size]
            val ab = b - a
            val bc = c - b
            val straight = abs(ab.cross(bc)) <= 1e-9 * ab.length() * bc.length() + size * size * 1e-15 && ab.dot(bc) > 0.0
            if (straight) changed = true else out += b
        }
        pts = out
    }
    return pts
}

/** Merges points closer than [tolerance] into one vertex id. */
private class VertexIndex(private val tolerance: Double) {
    // Cells are wider than the tolerance, so most lookups stay inside one cell.
    private val cell = tolerance * 4.0

    var x = DoubleArray(256)
    var y = DoubleArray(256)
    var size = 0
        private set
    private var next = IntArray(256)
    private val heads = HashMap<Long, Int>()

    fun id(px: Double, py: Double): Int {
        val fx = px / cell
        val fy = py / cell
        val cx = floor(fx).toLong()
        val cy = floor(fy).toLong()
        // Only look in a neighbor cell when the point is within the tolerance of that side.
        val x0 = if (fx - cx < 0.25) -1L else 0L
        val x1 = if (fx - cx > 0.75) 1L else 0L
        val y0 = if (fy - cy < 0.25) -1L else 0L
        val y1 = if (fy - cy > 0.75) 1L else 0L
        for (dx in x0..x1) for (dy in y0..y1) {
            var v = heads[key(cx + dx, cy + dy)] ?: -1
            while (v >= 0) {
                if (hypot(x[v] - px, y[v] - py) <= tolerance) return v
                v = next[v]
            }
        }
        if (size == x.size) {
            x = x.copyOf(size * 2); y = y.copyOf(size * 2); next = next.copyOf(size * 2)
        }
        val k = key(cx, cy)
        x[size] = px
        y[size] = py
        next[size] = heads[k] ?: -1
        heads[k] = size
        return size++
    }

    private fun key(cx: Long, cy: Long) = (cx shl 32) xor (cy and 0xffffffffL)
}

/** Length of a closed loop. */
internal fun perimeter(loop: List<Pt>): Double {
    var total = 0.0
    for (i in loop.indices) total += (loop[(i + 1) % loop.size] - loop[i]).length()
    return total
}
