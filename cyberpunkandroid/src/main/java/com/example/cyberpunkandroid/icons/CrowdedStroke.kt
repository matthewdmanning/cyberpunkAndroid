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

private class Proximity(val lines: List<Polyline>, val width: Double, val minGap: Double) {
    private val segCount = IntArray(lines.size) { if (lines[it].closed) lines[it].points.size else lines[it].points.size - 1 }
    private val start = Array(lines.size) { li -> DoubleArray(segCount[li]) }
    private val length = Array(lines.size) { li -> DoubleArray(segCount[li]) }
    private val total = DoubleArray(lines.size)
    // Absolute turning summed over vertices 1..k (vertex k starts segment k), and in all.
    private val turning = Array(lines.size) { li -> DoubleArray(segCount[li] + 1) }
    private val totalTurning = DoubleArray(lines.size)
    private val crossings = ArrayList<Crossing>()

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

    private fun a(li: Int, i: Int) = lines[li].points[i]
    private fun b(li: Int, i: Int) = lines[li].points[(i + 1) % lines[li].points.size]

    private fun findCrossings() {
        for (li in lines.indices) for (i in 0 until segCount[li]) for (lj in li until lines.size) {
            for (j in (if (lj == li) i + 2 else 0) until segCount[lj]) {
                if (lj == li && lines[li].closed && i == 0 && j == segCount[li] - 1) continue // neighbors across the seam
                val p = a(li, i); val r = b(li, i) - p
                val q = a(lj, j); val s = b(lj, j) - q
                val den = r.cross(s)
                if (abs(den) <= 1e-12 * length[li][i] * length[lj][j]) continue
                val t = (q - p).cross(s) / den
                val u = (q - p).cross(r) / den
                if (t < 0 || t > 1 || u < 0 || u > 1) continue
                val sine = abs(den) / (length[li][i] * length[lj][j])
                // Near the crossing the strokes overlap anyway. The window ends where the lines
                // are far enough apart that the room is the full width, so the stroke doesn't step.
                val window = width / ((1.0 - minGap) / 2.0 * max(sine, MinCrossingSine))
                crossings += Crossing(li, start[li][i] + t * length[li][i], lj, start[lj][j] + u * length[lj][j], window)
            }
        }
    }

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

    private fun nearCrossing(li: Int, sp: Double, lj: Int, sq: Double): Boolean = crossings.any { c ->
        (c.lineA == li && c.lineB == lj && arcSeparation(li, sp, c.arcA) <= c.window && arcSeparation(lj, sq, c.arcB) <= c.window) ||
            (c.lineB == li && c.lineA == lj && arcSeparation(li, sp, c.arcB) <= c.window && arcSeparation(lj, sq, c.arcA) <= c.window)
    }

    /** The largest offset each side of point [p] (normal [n], arc position [sp] on line [li]) may reach. */
    fun room(li: Int, seg: Int, p: Pt, n: Pt, sp: Double): DoubleArray {
        var plus = Double.POSITIVE_INFINITY
        var minus = Double.POSITIVE_INFINITY
        for (lj in lines.indices) for (j in 0 until segCount[lj]) {
            val len = length[lj][j]
            if (len <= 0.0) continue
            val a = a(lj, j)
            val ab = b(lj, j) - a
            val u = ((p - a).dot(ab) / (len * len)).coerceIn(0.0, 1.0)
            val q = a + ab * u
            val v = q - p
            val d = v.length()
            if (d <= 1e-9) continue
            val sq = start[lj][j] + u * len
            if (lj == li && turnBetween(li, seg, sp, j, sq) <= SameRunTurn) continue
            val facing = v.dot(n) / d
            if (abs(facing) < SideCone) continue // ahead or behind, not beside
            if (nearCrossing(li, sp, lj, sq)) continue
            if (facing > 0) plus = min(plus, d) else minus = min(minus, d)
        }
        val share = (1.0 - minGap) / 2.0
        return doubleArrayOf(min(width, plus * share), min(width, minus * share))
    }

    /**
     * The outside side of each straight segment (NaN for curves). A straight segment whose
     * perpendicular meets nothing (the bottom of a U) takes the polarity of the nearest decided
     * segment along its line: sides are measured against the direction of travel, so the
     * outside keeps its sign along the line. A curve's polarity is its convex side. A line with
     * nothing decided stays centered.
     */
    private val polarity: Array<DoubleArray> = Array(lines.size) { li ->
        val line = lines[li]
        val own = DoubleArray(segCount[li]) { i ->
            if (line.straight[i]) outsideOf(li, i)
            else -kotlin.math.sign(line.curveStart[i] + line.curveEnd[i]) // curvature > 0: convex side is −normal
        }
        DoubleArray(segCount[li]) { i ->
            when {
                !line.straight[i] -> Double.NaN
                own[i] != 0.0 -> own[i]
                else -> nearestDecided(li, i, own)
            }
        }
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
        val outward = ArrayList<Int>()
        val curveA = ArrayList<Double>(); val curveB = ArrayList<Double>()
        val sideLean = ArrayList<Double>()
        val plusA = ArrayList<Double>(); val minusA = ArrayList<Double>()
        val plusB = ArrayList<Double>(); val minusB = ArrayList<Double>()
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
                return room(li, i, a + (b - a) * tt, n, start[li][i] + tt * len)
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
            for (k in 0 until ts.size - 1) {
                pts += if (k == 0) a else a + (b - a) * ts[k]
                directions += dir
                outward += if (line.closed) line.outward[i] else 0
                curveA += line.curveStart[i] + (line.curveEnd[i] - line.curveStart[i]) * ts[k]
                curveB += line.curveStart[i] + (line.curveEnd[i] - line.curveStart[i]) * ts[k + 1]
                sideLean += side
                plusA += rooms[k][0]; minusA += rooms[k][1]
                plusB += rooms[k + 1][0]; minusB += rooms[k + 1][1]
            }
        }
        if (!line.closed) pts += line.points.last()
        return StrokeLine(
            pts, line.closed, directions, outward.toIntArray(),
            curveA.toDoubleArray(), curveB.toDoubleArray(), sideLean.toDoubleArray(),
            plusA.toDoubleArray(), minusA.toDoubleArray(), plusB.toDoubleArray(), minusB.toDoubleArray()
        )
    }
}

// A point on the same line counts as "other" once the line turns more than this to reach it:
// right-angle neighbors don't crowd each other; hairpins, loops and notches do.
private const val SameRunTurn = kotlin.math.PI / 2 + 1e-6

// Only lines within 60° of a side's normal crowd that side.
private const val SideCone = 0.5

// Crossings flatter than this sine use its window, so near-parallel crossings stay bounded.
private const val MinCrossingSine = 0.2

// Room samples sit this far (px) inside a segment's ends.
private const val EndInset = 1e-3

// Split a segment while the room bends by more than this (px) between samples...
private const val RoomTolerance = 0.25

// ...down to pieces this short (px), at most this many halvings deep.
private const val MinRoomPiece = 1.0
private const val MaxRoomDepth = 8
