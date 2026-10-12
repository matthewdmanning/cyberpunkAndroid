package com.example.cyberpunkandroid.sliderpanel.model

import androidx.compose.runtime.Immutable
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

/** The value at track [position] (0 at start, 1 at stop), clamped to the track. */
fun SliderSpec.valueAt(position: Double): Double {
    val t = position.coerceIn(0.0, 1.0)
    return when (val s = scale) {
        ScaleSpec.Linear -> start + (stop - start) * t
        ScaleSpec.Log -> start * (stop / start).pow(t)
        is ScaleSpec.Power -> start + (stop - start) * t.pow(s.exponent)
        is ScaleSpec.Piecewise -> {
            val i = s.points.indexOfLast { it.position <= t }.coerceIn(0, s.points.size - 2)
            val a = s.points[i]
            val b = s.points[i + 1]
            a.value + (b.value - a.value) * (t - a.position) / (b.position - a.position)
        }
    }
}

/** The track position (0 at start, 1 at stop) of [value], clamped to the track. */
fun SliderSpec.positionOf(value: Double): Double {
    val lo = min(start, stop)
    val hi = max(start, stop)
    val v = value.coerceIn(lo, hi)
    val t = when (val s = scale) {
        ScaleSpec.Linear -> (v - start) / (stop - start)
        ScaleSpec.Log -> ln(v / start) / ln(stop / start)
        is ScaleSpec.Power -> ((v - start) / (stop - start)).coerceAtLeast(0.0).pow(1.0 / s.exponent)
        is ScaleSpec.Piecewise -> {
            val rising = stop > start
            val i = s.points.indexOfLast { if (rising) it.value <= v else it.value >= v }.coerceIn(0, s.points.size - 2)
            val a = s.points[i]
            val b = s.points[i + 1]
            a.position + (b.position - a.position) * (v - a.value) / (b.value - a.value)
        }
    }
    return t.coerceIn(0.0, 1.0)
}

/** [value] snapped to [SliderSpec.step] counted from the start, and clamped to the range. */
fun SliderSpec.snap(value: Double): Double {
    val lo = min(start, stop)
    val hi = max(start, stop)
    if (step <= 0.0) return value.coerceIn(lo, hi)
    val snapped = start + round((value - start) / step) * step
    // A step that doesn't divide the range can round past the far end; step back inside.
    return when {
        snapped > hi -> snapped - step
        snapped < lo -> snapped + step
        else -> snapped
    }.coerceIn(lo, hi)
}

/** [value] with [SliderSpec.decimals] digits, independent of the device locale. */
fun SliderSpec.format(value: Double): String = String.format(Locale.ROOT, "%.${decimals}f", value)

/** [format] plus the unit, such as "4.0 dp". */
fun SliderSpec.formatWithUnit(value: Double): String = if (unit.isEmpty()) format(value) else "${format(value)} $unit"

/** One hash mark on a track: where it sits (0 to 1), whether it is major, and its label. */
@Immutable
data class HashMark(val position: Float, val major: Boolean, val label: String?)

/** The slider's hash marks, ordered along the track. */
fun SliderSpec.hashMarks(): List<HashMark> {
    val lo = min(start, stop)
    val hi = max(start, stop)
    // Float noise from repeated additions must not drop the mark at the far end.
    val slack = (hi - lo) * 1e-9
    val majors: List<MarkSpec> = marks.values
        ?: marks.every?.let { every ->
            val count = floor((hi - lo) / every + 1e-9).toInt().coerceAtMost(MaxMarks)
            val direction = if (stop > start) 1.0 else -1.0
            List(count + 1) { MarkSpec(start + direction * every * it) }
        }
        ?: defaultMajors(lo, hi).map { MarkSpec(it) }
    val sorted = majors.filter { it.value >= lo - slack && it.value <= hi + slack }.distinctBy { it.value }
        .sortedBy { positionOf(it.value) }
    val out = ArrayList<HashMark>()
    for ((i, m) in sorted.withIndex()) {
        out += HashMark(positionOf(m.value).toFloat(), major = true, label = if (marks.labels) m.label ?: format(m.value) else null)
        val next = sorted.getOrNull(i + 1) ?: continue
        for (k in 1..marks.minor) {
            val v = m.value + (next.value - m.value) * k / (marks.minor + 1)
            out += HashMark(positionOf(v).toFloat(), major = false, label = null)
        }
    }
    return out
}

private fun SliderSpec.defaultMajors(lo: Double, hi: Double): List<Double> {
    if (scale != ScaleSpec.Log) return listOf(start, stop)
    val decades = (ceil(log10(lo) - 1e-9).toInt()..floor(log10(hi) + 1e-9).toInt()).map { 10.0.pow(it) }
    return (listOf(start, stop) + decades).distinct()
}
