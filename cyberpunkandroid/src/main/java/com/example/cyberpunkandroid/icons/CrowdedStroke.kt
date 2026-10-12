package com.example.cyberpunkandroid.icons

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/*
 * Where two parts of a centerline run close together (the two arms of a tight corner, the
 * sides of a loop, a narrow notch), their strokes would meet and close the space between
 * them. Here each side of the stroke gets "room": the distance, toward that side, to the
 * nearest other part of the centerline. A side may fill at most (1 − minGap) / 2 of that
 * distance, so at least minGap of every gap stays open, and the stroke tapers smoothly as
 * lines approach each other.
 *
 * "Other part" means another contour, or a part of the same contour that the line turns
 * more than 90° to reach (a hairpin, a loop, the far side of a notch or ring). It excludes
 * the neighborhood of a crossing, so lines that cross stay solid where they cross.
 */

/** Default share of the distance between two nearby centerline parts that stays open. */
internal const val DefaultMinGap = 0.25

/**
 * A centerline contour ready to stroke: segments split where the room changes, and for each
 * segment end its curvature and the room on each side (+normal and −normal), in px.
 */
internal class StrokeLine(
    val points: List<Pt>,
    val closed: Boolean,
    /** Unit direction of each segment: its source segment's, so split pieces stay exactly collinear. */
    val directions: List<Pt>,
    val outward: IntArray,
    val curveA: DoubleArray,
    val curveB: DoubleArray,
    /** For straight segments, the side the whole stroke goes to (±1, 0 when undecided); NaN on curves. */
    val sideLean: DoubleArray,
    val plusA: DoubleArray,
    val minusA: DoubleArray,
    val plusB: DoubleArray,
    val minusB: DoubleArray
)

/** [lines] with room for a stroke of [strokeWidth], keeping [minGap] of each gap open. */
internal fun crowdedLines(lines: List<Polyline>, strokeWidth: Double, minGap: Double): List<StrokeLine> {
    val field = Proximity(lines, strokeWidth, minGap.coerceIn(0.0, 1.0))
    return lines.indices.map { field.refine(it) }
}

private class Crossing(val lineA: Int, val arcA: Double, val lineB: Int, val arcB: Double, val window: Double)

/**
 * Uniform grid over every segment of every line, for radius queries. Room is capped at the
 * stroke width, so a segment farther than width / share can't change it: [room] only asks
 * for segments within that radius instead of scanning them all.
 */
private class SegmentGrid(lines: List<Polyline>, segCount: IntArray, cell: Double) {
    private val cellSize = max(cell, 1e-6)
    private val cells = HashMap<Long, IntArrayList>()
    /** Flat segment index → line, and line → first flat index. */
    val lineOf: IntArray
    val firstOf: IntArray
    private var stamp = IntArray(0)
    private var visit = 0

    init {
        firstOf = IntArray(lines.size + 1)
        for (li in lines.indices) firstOf[li + 1] = firstOf[li] + segCount[li]
        lineOf = IntArray(firstOf[lines.size])
        stamp = IntArray(lineOf.size)
        for (li in lines.indices) {
            val pts = lines[li].points
            for (i in 0 until segCount[li]) {
                val flat = firstOf[li] + i
                lineOf[flat] = li
                val a = pts[i]
                val b = pts[(i + 1) % pts.size]
                for (cx in cellOf(min(a.x, b.x))..cellOf(max(a.x, b.x))) for (cy in cellOf(min(a.y, b.y))..cellOf(max(a.y, b.y))) {
                    cells.getOrPut(key(cx, cy)) { IntArrayList() }.add(flat)
                }
            }
        }
    }

    /** Calls [visitSegment] once for each segment whose cells meet the box of [radius] around ([x], [y]). */
    inline fun near(x: Double, y: Double, radius: Double, visitSegment: (Int) -> Unit) {
        visit++
        val x0 = cellOf(x - radius); val x1 = cellOf(x + radius)
        val y0 = cellOf(y - radius); val y1 = cellOf(y + radius)
        if ((x1 - x0 + 1L) * (y1 - y0 + 1L) > lineOf.size) { // the box covers more cells than there are segments
            for (flat in lineOf.indices) visitSegment(flat)
            return
        }
        for (cx in x0..x1) for (cy in y0..y1) {
            val list = cells[key(cx, cy)] ?: continue
            for (k in 0 until list.size) {
                val flat = list[k]
                if (stamp[flat] == visit) continue
                stamp[flat] = visit
                visitSegment(flat)
            }
        }
    }

    fun cellOf(v: Double): Long = kotlin.math.floor(v / cellSize).toLong()
    private fun key(cx: Long, cy: Long) = (cx shl 32) xor (cy and 0xffffffffL)
}

private class Proximity(val lines: List<Polyline>, val width: Double, val minGap: Double) {
    private val segCount = IntArray(lines.size) { if (lines[it].closed) lines[it].points.size else lines[it].points.size - 1 }
    private val start = Array(lines.size) { li -> DoubleArray(segCount[li]) }
    private val length = Array(lines.size) { li -> DoubleArray(segCount[li]) }
    private val total = DoubleArray(lines.size)
    // Absolute turning summed over vertices 1..k (vertex k starts segment k), and in all.
    private val turning = Array(lines.size) { li -> DoubleArray(segCount[li] + 1) }
    private val totalTurning = DoubleArray(lines.size)
    // Crossings by line pair (smaller line index first), so a room query checks only its pair.
    private val crossings = HashMap<Long, ArrayList<Crossing>>()
    private val share = (1.0 - minGap) / 2.0
    // Beyond this distance another segment can't lower the room below the width cap.
    private val reach = if (share > 0.0) width / share else Double.POSITIVE_INFINITY

    init {
        for (li in lines.indices) {
            var s = 0.0
            for (i in 0 until segCount[li]) {
                start[li][i] = s
                length[li][i] = (b(li, i) - a(li, i)).length()
                s += length[li][i]
            }
            total[li] = s
            fun turnAt(k: Int): Double { // between segment k - 1 and segment k
                val d1 = b(li, (k - 1 + segCount[li]) % segCount[li]) - a(li, (k - 1 + segCount[li]) % segCount[li])
                val d2 = b(li, k) - a(li, k)
                return abs(kotlin.math.atan2(d1.cross(d2), d1.dot(d2)))
            }
            for (k in 1 until segCount[li]) turning[li][k] = turning[li][k - 1] + turnAt(k)
            turning[li][segCount[li]] = turning[li][segCount[li] - 1]
            totalTurning[li] = turning[li][segCount[li]] + if (lines[li].closed) turnAt(0) else 0.0
        }
        findCrossings()
    }

    private val grid = SegmentGrid(lines, segCount, if (reach.isFinite()) reach else 1e9)

    private fun a(li: Int, i: Int) = lines[li].points[i]
    private fun b(li: Int, i: Int) = lines[li].points[(i + 1) % lines[li].points.size]

    private fun findCrossings() {
        // Sweep along x: only segments whose x ranges overlap can cross.
        val flat = ArrayList<IntArray>()
        for (li in lines.indices) for (i in 0 until segCount[li]) flat += intArrayOf(li, i)
        val minX = DoubleArray(flat.size) { min(a(flat[it][0], flat[it][1]).x, b(flat[it][0], flat[it][1]).x) }
        val maxX = DoubleArray(flat.size) { max(a(flat[it][0], flat[it][1]).x, b(flat[it][0], flat[it][1]).x) }
        val order = flat.indices.sortedBy { minX[it] }
        for (oi in order.indices) {
            val x = order[oi]
            for (oj in oi + 1 until order.size) {
                val y = order[oj]
                if (minX[y] > maxX[x]) break
                var (li, i) = flat[x].let { it[0] to it[1] }
                var (lj, j) = flat[y].let { it[0] to it[1] }
                if (li > lj || (li == lj && i > j)) { val tl = li; li = lj; lj = tl; val ti = i; i = j; j = ti }
                if (li == lj && j - i < 2) continue // the same segment, or neighbors
                if (li == lj && lines[li].closed && i == 0 && j == segCount[li] - 1) continue // neighbors across the seam
                val px = a(li, i).x; val py = a(li, i).y
                val rx = b(li, i).x - px; val ry = b(li, i).y - py
                val qx = a(lj, j).x; val qy = a(lj, j).y
                val sx = b(lj, j).x - qx; val sy = b(lj, j).y - qy
                val den = rx * sy - ry * sx
                if (abs(den) <= 1e-12 * length[li][i] * length[lj][j]) continue
                val t = ((qx - px) * sy - (qy - py) * sx) / den
                val u = ((qx - px) * ry - (qy - py) * rx) / den
                if (t < 0 || t > 1 || u < 0 || u > 1) continue
                val sine = abs(den) / (length[li][i] * length[lj][j])
                // Near the crossing the strokes overlap anyway. The window ends where the lines
                // are far enough apart that the room is the full width, so the stroke doesn't step.
                val window = width / (share * max(sine, MinCrossingSine))
                crossings.getOrPut(pairKey(li, lj)) { ArrayList() } +=
                    Crossing(li, start[li][i] + t * length[li][i], lj, start[lj][j] + u * length[lj][j], window)
            }
        }
    }

    private fun pairKey(l1: Int, l2: Int) = min(l1, l2).toLong() shl 32 or max(l1, l2).toLong()

    private fun arcSeparation(li: Int, s1: Double, s2: Double): Double {
        val d = abs(s1 - s2)
        return if (lines[li].closed) min(d, total[li] - d) else d
    }

    /** How far the line turns between segments [i] and [j], along the shorter way round. */
    private fun turnBetween(li: Int, i: Int, sp: Double, j: Int, sq: Double): Double {
        val forward = abs(turning[li][max(i, j)] - turning[li][min(i, j)])
        if (!lines[li].closed) return forward
        // Take the way round that the arc separation measured.
        val shortWay = abs(sp - sq) <= total[li] - abs(sp - sq)
        return if (shortWay) forward else totalTurning[li] - forward
    }

    private fun nearCrossing(li: Int, sp: Double, lj: Int, sq: Double): Boolean {
        val list = crossings[pairKey(li, lj)] ?: return false
        return list.any { c ->
            (c.lineA == li && c.lineB == lj && arcSeparation(li, sp, c.arcA) <= c.window && arcSeparation(lj, sq, c.arcB) <= c.window) ||
                (c.lineB == li && c.lineA == lj && arcSeparation(li, sp, c.arcB) <= c.window && arcSeparation(lj, sq, c.arcA) <= c.window)
        }
    }

    /**
     * The largest offset each side of point ([px], [py]) (unit normal ([nx], [ny]), arc
     * position [sp] on segment [seg] of line [li]) may reach, written to [out] as (+normal, −normal).
     */
    fun room(li: Int, seg: Int, px: Double, py: Double, nx: Double, ny: Double, sp: Double, out: DoubleArray) {
        var plus = Double.POSITIVE_INFINITY
        var minus = Double.POSITIVE_INFINITY
        grid.near(px, py, reach) { flat ->
            val lj = grid.lineOf[flat]
            val j = flat - grid.firstOf[lj]
            val len = length[lj][j]
            if (len <= 0.0) return@near
            val pts = lines[lj].points
            val ax = pts[j].x; val ay = pts[j].y
            val b = pts[(j + 1) % pts.size]
            val abx = b.x - ax; val aby = b.y - ay
            val u = (((px - ax) * abx + (py - ay) * aby) / (len * len)).coerceIn(0.0, 1.0)
            val vx = ax + abx * u - px
            val vy = ay + aby * u - py
            val d = kotlin.math.sqrt(vx * vx + vy * vy)
            if (d <= 1e-9 || d >= reach) return@near
            val sq = start[lj][j] + u * len
            if (lj == li && turnBetween(li, seg, sp, j, sq) <= SameRunTurn) return@near
            val facing = (vx * nx + vy * ny) / d
            if (abs(facing) < SideCone) return@near // ahead or behind, not beside
            if (nearCrossing(li, sp, lj, sq)) return@near
            if (facing > 0) plus = min(plus, d) else minus = min(minus, d)
        }
        out[0] = min(width, plus * share)
        out[1] = min(width, minus * share)
    }

    /**
     * The outside side of each straight segment (NaN for curves). A straight segment whose
     * perpendicular meets nothing (the bottom of a U) takes the polarity of the nearest decided
     * segment along its line: sides are measured against the direction of travel, so the
     * outside keeps its sign along the line. A curve's polarity is its convex side. A line with
     * nothing decided stays centered.
     */
    private val polarity: Array<DoubleArray> by lazy { Array(lines.size) { li -> polarityOf(li) } }

    private fun polarityOf(li: Int): DoubleArray {
        val line = lines[li]
        val own = DoubleArray(segCount[li]) { i ->
            if (line.straight[i]) outsideOf(li, i)
            else -kotlin.math.sign(line.curveStart[i] + line.curveEnd[i]) // curvature > 0: convex side is −normal
        }
        return DoubleArray(segCount[li]) { i ->
            when {
                !line.straight[i] -> Double.NaN
                own[i] != 0.0 -> own[i]
                else -> outsideOfCurveCorner(li, i) ?: nearestDecided(li, i, own)
            }
        }
    }

    /**
     * Edge-case patch: an undecided straight segment that meets a curve at a corner would copy
     * the curve's polarity and put its stroke on the inside of that corner. It takes the
     * corner's outer side instead. Null when no curve meets it at a corner.
     */
    private fun outsideOfCurveCorner(li: Int, i: Int): Double? {
        val line = lines[li]
        val n = segCount[li]
        fun direction(k: Int) = (b(li, k) - a(li, k)) * (1.0 / length[li][k])
        for (other in listOf(i - 1, i + 1)) {
            if (!line.closed && (other < 0 || other >= n)) continue
            val k = (other + n) % n
            if (line.straight[k]) continue
            val (first, second) = if (other < i) k to i else i to k
            val turn = direction(first).cross(direction(second))
            if (abs(kotlin.math.asin(turn.coerceIn(-1.0, 1.0))) <= CurveCornerTurn) continue // a smooth join, not a corner
            // turn > 0 bends toward +normal, so the corner's outer side is −normal.
            return if (turn > 0) -1.0 else 1.0
        }
        return null
    }

    private fun nearestDecided(li: Int, i: Int, own: DoubleArray): Double {
        val center = start[li][i] + length[li][i] / 2
        var best = Double.POSITIVE_INFINITY
        var sum = 0.0
        for (j in own.indices) {
            if (own[j] == 0.0) continue
            val d = arcSeparation(li, center, start[li][j] + length[li][j] / 2)
            when {
                d < best - 1e-9 -> { best = d; sum = own[j] }
                d <= best + 1e-9 -> sum += own[j] // equally near on both sides: they vote
            }
        }
        return kotlin.math.sign(sum)
    }

    /**
     * The outside of straight segment [i] on line [li]: cast its perpendicular (the reciprocal
     * slope) both ways from its midpoint. The side whose ray meets another part of the shape
     * first is inside; returns +1 when the outside is +normal, −1 for −normal, and 0 when
     * neither ray meets anything (a lone line has no outside, so it stays centered).
     */
    fun outsideOf(li: Int, i: Int): Double {
        val a = a(li, i)
        val d = b(li, i) - a
        val mid = a + d * 0.5
        val n = Pt(-d.y, d.x) * (1.0 / d.length())
        var plus = Double.POSITIVE_INFINITY
        var minus = Double.POSITIVE_INFINITY
        for (lj in lines.indices) for (j in 0 until segCount[lj]) {
            if (lj == li && j == i) continue
            val q = a(lj, j)
            val s = b(lj, j) - q
            val den = n.cross(s)
            if (abs(den) <= 1e-12 * s.length()) continue // parallel to the ray
            val t = (q - mid).cross(s) / den // distance along n
            val u = (q - mid).cross(n) / den // where on the other segment
            if (u < 0 || u > 1 || abs(t) <= 1e-9) continue
            if (t > 0) plus = min(plus, t) else minus = min(minus, -t)
        }
        return when {
            plus < minus -> -1.0
            minus < plus -> 1.0
            else -> 0.0
        }
    }

    /** Line [li] with segments split where the room changes, each end carrying its room. */
    fun refine(li: Int): StrokeLine {
        val line = lines[li]
        val pts = ArrayList<Pt>()
        val directions = ArrayList<Pt>()
        val outward = IntArrayList()
        val curveA = DoubleArrayList(); val curveB = DoubleArrayList()
        val sideLean = DoubleArrayList()
        val plusA = DoubleArrayList(); val minusA = DoubleArrayList()
        val plusB = DoubleArrayList(); val minusB = DoubleArrayList()
        for (i in 0 until segCount[li]) {
            val a = a(li, i)
            val b = b(li, i)
            val len = length[li][i]
            val dir = (b - a) * (1.0 / len)
            val n = Pt(-dir.y, dir.x)
            // Sample just inside the ends: at a vertex the neighboring segment is not "other".
            val inset = min(0.5, EndInset / len)
            fun sample(t: Double): DoubleArray {
                val tt = t.coerceIn(inset, 1.0 - inset)
                val out = DoubleArray(2)
                room(li, i, a.x + (b.x - a.x) * tt, a.y + (b.y - a.y) * tt, n.x, n.y, start[li][i] + tt * len, out)
                return out
            }
            val side = if (line.straight[i]) polarity[li][i] else Double.NaN
            val ts = arrayListOf(0.0)
            val rooms = arrayListOf(sample(0.0))
            fun split(t0: Double, r0: DoubleArray, t1: Double, r1: DoubleArray, depth: Int) {
                val tm = (t0 + t1) / 2.0
                val rm = sample(tm)
                val bent = abs(rm[0] - (r0[0] + r1[0]) / 2) > RoomTolerance || abs(rm[1] - (r0[1] + r1[1]) / 2) > RoomTolerance
                if (bent && depth < MaxRoomDepth && (t1 - t0) * len > MinRoomPiece) {
                    split(t0, r0, tm, rm, depth + 1)
                    ts += tm; rooms += rm
                    split(tm, rm, t1, r1, depth + 1)
                }
            }
            // Straight lines also split one stroke width in from each end, so the lean ramps
            // only near corners and holds the line's own side in between.
            val marks = if (line.straight[i] && len > 2 * width) listOf(width / len, 1.0 - width / len) else emptyList()
            var t0 = 0.0
            for (t1 in marks + 1.0) {
                val r1 = sample(t1)
                split(t0, rooms.last(), t1, r1, 0)
                ts += t1; rooms += r1
                t0 = t1
            }
            if (line.straight[i]) {
                // A straight line keeps its slope: its edges stay parallel to it, so each side's
                // room is the tightest along the whole line, not a taper.
                val plus = rooms.minOf { it[0] }
                val minus = rooms.minOf { it[1] }
                for (r in rooms) { r[0] = plus; r[1] = minus }
            }
            for (k in 0 until ts.size - 1) {
                pts += if (k == 0) a else a + (b - a) * ts[k]
                directions += dir
                outward.add(if (line.closed) line.outward[i] else 0)
                curveA.add(line.curveStart[i] + (line.curveEnd[i] - line.curveStart[i]) * ts[k])
                curveB.add(line.curveStart[i] + (line.curveEnd[i] - line.curveStart[i]) * ts[k + 1])
                sideLean.add(side)
                plusA.add(rooms[k][0]); minusA.add(rooms[k][1])
                plusB.add(rooms[k + 1][0]); minusB.add(rooms[k + 1][1])
            }
        }
        if (!line.closed) pts += line.points.last()
        return StrokeLine(
            pts, line.closed, directions, outward.toArray(),
            curveA.toArray(), curveB.toArray(), sideLean.toArray(),
            plusA.toArray(), minusA.toArray(), plusB.toArray(), minusB.toArray()
        )
    }
}

// A point on the same line counts as "other" once the line turns more than this to reach it:
// right-angle neighbors don't crowd each other; hairpins, loops and notches do.
private const val SameRunTurn = kotlin.math.PI / 2 + 1e-6

// A line and a curve meeting at more than this turn form a corner (for the edge-case patch).
private const val CurveCornerTurn = 20.0 * kotlin.math.PI / 180.0

// Only lines within 60° of a side's normal crowd that side.
private const val SideCone = 0.5

// Crossings flatter than this sine use its window, so near-parallel crossings stay bounded.
private const val MinCrossingSine = 0.2

// Room samples sit this far (px) inside a segment's ends.
private const val EndInset = 1e-3

// Split a segment while the room bends by more than this (px) between samples...
private const val RoomTolerance = 0.05

// ...down to pieces this short (px), at most this many halvings deep.
private const val MinRoomPiece = 0.25
private const val MaxRoomDepth = 12
