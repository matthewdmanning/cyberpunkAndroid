package com.example.cyberpunkandroid.sliderpanel.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A slider panel as defined in JSON: how it sits on screen and the sliders it holds.
 *
 * ```json
 * {
 *   "title": "Stroke lab",
 *   "mode": "floating",
 *   "pinEdge": "bottom",
 *   "scrollable": true,
 *   "maxHeightDp": 320,
 *   "widthDp": 300,
 *   "sliders": [
 *     {
 *       "id": "thickness", "label": "Thickness", "unit": "dp",
 *       "start": 0.5, "stop": 64, "initial": 4, "step": 0.5, "decimals": 1,
 *       "scale": { "type": "log" },
 *       "marks": { "values": [{ "value": 1 }, { "value": 8, "label": "8" }, { "value": 64 }], "minor": 4 }
 *     }
 *   ]
 * }
 * ```
 *
 * Parse it with [parseSliderPanelSpec], which checks every rule below and reports all errors.
 *
 * @param title Text in the panel's header, or null for none.
 * @param mode Whether the panel starts floating or pinned.
 * @param pinEdge The edge the panel docks to when pinned.
 * @param scrollable Whether the slider list scrolls once it is taller than [maxHeightDp].
 * @param maxHeightDp Largest height of the slider list when [scrollable].
 * @param widthDp Panel width when floating, or when pinned to the start or end edge.
 * @param sliders The sliders, top to bottom. At least one; ids must be unique.
 */
@Immutable
@Serializable
data class SliderPanelSpec(
    val title: String? = null,
    val mode: PanelMode = PanelMode.Floating,
    val pinEdge: PinEdge = PinEdge.Bottom,
    val scrollable: Boolean = false,
    val maxHeightDp: Float = 320f,
    val widthDp: Float = 300f,
    val sliders: List<SliderSpec>
)

@Serializable
enum class PanelMode {
    /** Over the content at an offset the user can drag. */
    @SerialName("floating") Floating,

    /** Docked to [SliderPanelSpec.pinEdge]. */
    @SerialName("pinned") Pinned
}

@Serializable
enum class PinEdge {
    @SerialName("top") Top,
    @SerialName("bottom") Bottom,
    @SerialName("start") Start,
    @SerialName("end") End
}

/**
 * One slider.
 *
 * @param id Key for the slider's value in state and events.
 * @param label Text shown above the track.
 * @param start Value at the track's start. May be larger than [stop] for a reversed slider.
 * @param stop Value at the track's end.
 * @param initial Starting value; [start] when null.
 * @param step Value spacing the slider snaps to, counted from [start]; 0 for continuous.
 * @param unit Text after the value, such as "dp" or "dB".
 * @param decimals Digits after the decimal point in the shown value and mark labels.
 * @param scale How the track position maps to a value.
 * @param marks Hash marks along the track.
 */
@Immutable
@Serializable
data class SliderSpec(
    val id: String,
    val label: String,
    val start: Double,
    val stop: Double,
    val initial: Double? = null,
    val step: Double = 0.0,
    val unit: String = "",
    val decimals: Int = 2,
    val scale: ScaleSpec = ScaleSpec.Linear,
    val marks: MarksSpec = MarksSpec()
)

/** How a track position from 0 (start) to 1 (stop) maps to a value. Selected by "type". */
@Immutable
@Serializable
sealed interface ScaleSpec {
    /** Equal track distances give equal value changes. */
    @Serializable
    @SerialName("linear")
    data object Linear : ScaleSpec

    /** Equal track distances give equal value ratios. Needs [SliderSpec.start] and stop above 0. */
    @Serializable
    @SerialName("log")
    data object Log : ScaleSpec

    /** value = start + (stop − start) · position^[exponent]; above 1 gives fine control near start. */
    @Serializable
    @SerialName("power")
    data class Power(val exponent: Double) : ScaleSpec

    /**
     * Straight lines between [points]. Positions run from 0 to 1 in increasing order; values
     * run from the slider's start to its stop, always rising or always falling.
     */
    @Serializable
    @SerialName("piecewise")
    data class Piecewise(val points: List<ScalePoint>) : ScaleSpec
}

@Immutable
@Serializable
data class ScalePoint(val position: Double, val value: Double)

/**
 * Hash marks along a track. Major marks come from [values], else one every [every] value
 * units from the start, else the start and stop (plus each power of ten between them on a
 * log scale). [minor] marks are spaced evenly in value between neighboring majors, so a log
 * scale with majors at powers of ten and `minor = 8` gives the classic 2…9 ticks.
 *
 * @param values Explicit major marks.
 * @param every Spacing of major marks in value units.
 * @param minor Minor marks between each pair of neighboring majors.
 * @param labels Whether major marks show their label.
 */
@Immutable
@Serializable
data class MarksSpec(
    val values: List<MarkSpec>? = null,
    val every: Double? = null,
    val minor: Int = 0,
    val labels: Boolean = true
)

/** A major mark at [value], labeled with [label], or with the formatted value when null. */
@Immutable
@Serializable
data class MarkSpec(val value: Double, val label: String? = null)

/** The result of [parseSliderPanelSpec]. */
sealed interface SpecParseResult {
    data class Success(val spec: SliderPanelSpec) : SpecParseResult

    /** Every problem found, each with the JSON path it concerns. */
    data class Failure(val errors: List<String>) : SpecParseResult
}

private val SpecJson = Json {
    ignoreUnknownKeys = false // a misspelled key is an error, not a silent default
    classDiscriminator = "type"
}

/** Parses and checks a slider panel definition. Never throws. */
fun parseSliderPanelSpec(json: String): SpecParseResult {
    val spec = try {
        SpecJson.decodeFromString(SliderPanelSpec.serializer(), json)
    } catch (e: SerializationException) {
        return SpecParseResult.Failure(listOf(e.message ?: "Invalid JSON"))
    } catch (e: IllegalArgumentException) {
        return SpecParseResult.Failure(listOf(e.message ?: "Invalid JSON"))
    }
    val errors = spec.validate()
    return if (errors.isEmpty()) SpecParseResult.Success(spec) else SpecParseResult.Failure(errors)
}

/** Writes a spec back to JSON, for storing or editing. */
fun SliderPanelSpec.toJson(): String = SpecJson.encodeToString(SliderPanelSpec.serializer(), this)

// Most hash marks a slider may draw; more would merge into a solid band.
internal const val MaxMarks = 400

/** Every rule broken by this spec, or empty when it is valid. */
fun SliderPanelSpec.validate(): List<String> {
    val errors = ArrayList<String>()
    if (sliders.isEmpty()) errors += "sliders: needs at least one slider"
    if (!(maxHeightDp > 0f) || maxHeightDp.isInfinite()) errors += "maxHeightDp: must be positive"
    if (!(widthDp > 0f) || widthDp.isInfinite()) errors += "widthDp: must be positive"
    sliders.groupBy { it.id }.filter { it.value.size > 1 }.keys.forEach { errors += "sliders: id \"$it\" is used more than once" }
    sliders.forEachIndexed { i, s -> errors += s.validate().map { "sliders[$i] (${s.id}).$it" } }
    return errors
}

private fun SliderSpec.validate(): List<String> {
    val errors = ArrayList<String>()
    if (id.isBlank()) errors += "id: must not be blank"
    if (!start.isFinite() || !stop.isFinite()) errors += "start/stop: must be finite"
    if (start == stop) errors += "start/stop: must differ"
    val lo = min(start, stop)
    val hi = max(start, stop)
    initial?.let { if (!it.isFinite() || it < lo || it > hi) errors += "initial: must be between start and stop" }
    if (!step.isFinite() || step < 0.0) errors += "step: must be 0 or positive"
    else if (step > hi - lo) errors += "step: must not exceed the range"
    if (decimals !in 0..10) errors += "decimals: must be between 0 and 10"
    when (val s = scale) {
        ScaleSpec.Linear -> Unit
        ScaleSpec.Log -> if (!(lo > 0.0)) errors += "scale: log needs start and stop above 0"
        is ScaleSpec.Power -> if (!(s.exponent > 0.0) || s.exponent.isInfinite()) errors += "scale: power exponent must be positive"
        is ScaleSpec.Piecewise -> errors += s.validate(start, stop)
    }
    marks.every?.let { if (!(it > 0.0) || it.isInfinite()) errors += "marks.every: must be positive" }
    if (marks.minor !in 0..20) errors += "marks.minor: must be between 0 and 20"
    marks.values?.forEachIndexed { i, m ->
        if (!m.value.isFinite() || m.value < lo || m.value > hi) errors += "marks.values[$i]: must be between start and stop"
    }
    if (errors.isEmpty() && hashMarks().size > MaxMarks) errors += "marks: more than $MaxMarks marks"
    return errors
}

private fun ScaleSpec.Piecewise.validate(start: Double, stop: Double): List<String> {
    val errors = ArrayList<String>()
    if (points.size < 2) return listOf("scale: piecewise needs at least 2 points")
    // Ends must match within float noise of the range.
    val tolerance = abs(stop - start) * 1e-9
    if (points.first().position != 0.0 || points.last().position != 1.0) errors += "scale: piecewise positions must run from 0 to 1"
    if (points.zipWithNext().any { (a, b) -> !(b.position > a.position) }) errors += "scale: piecewise positions must increase"
    if (abs(points.first().value - start) > tolerance || abs(points.last().value - stop) > tolerance) {
        errors += "scale: piecewise values must start at start and end at stop"
    }
    val rising = stop > start
    if (points.zipWithNext().any { (a, b) -> if (rising) !(b.value > a.value) else !(b.value < a.value) }) {
        errors += "scale: piecewise values must move steadily from start to stop"
    }
    return errors
}
