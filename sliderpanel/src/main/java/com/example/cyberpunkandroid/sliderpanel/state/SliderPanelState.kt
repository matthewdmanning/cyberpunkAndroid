package com.example.cyberpunkandroid.sliderpanel.state

import androidx.compose.runtime.Immutable
import com.example.cyberpunkandroid.sliderpanel.model.HashMark
import com.example.cyberpunkandroid.sliderpanel.model.PanelMode
import com.example.cyberpunkandroid.sliderpanel.model.PinEdge
import com.example.cyberpunkandroid.sliderpanel.model.SliderPanelSpec
import com.example.cyberpunkandroid.sliderpanel.model.SliderSpec
import com.example.cyberpunkandroid.sliderpanel.model.formatWithUnit
import com.example.cyberpunkandroid.sliderpanel.model.hashMarks
import com.example.cyberpunkandroid.sliderpanel.model.positionOf
import com.example.cyberpunkandroid.sliderpanel.model.snap
import com.example.cyberpunkandroid.sliderpanel.model.valueAt

/**
 * Everything the panel draws, derived from a [SliderPanelSpec] and the current values. The UI
 * reads it and sends [SliderPanelEvent]s back; [reduce] gives the next state.
 *
 * @param offsetXDp Floating position of the panel's top-left corner from the area's top-left.
 * @param offsetYDp See [offsetXDp].
 */
@Immutable
data class SliderPanelUiState(
    val spec: SliderPanelSpec,
    val mode: PanelMode,
    val offsetXDp: Float,
    val offsetYDp: Float,
    val sliders: List<SliderUiState>
) {
    val title: String? get() = spec.title
    val pinEdge: PinEdge get() = spec.pinEdge
    val scrollable: Boolean get() = spec.scrollable
    val maxHeightDp: Float get() = spec.maxHeightDp
    val widthDp: Float get() = spec.widthDp

    /** Current values by slider id. */
    val values: Map<String, Double> get() = sliders.associate { it.id to it.value }
}

/**
 * One slider as drawn.
 *
 * @param value Current value, snapped to the step.
 * @param position Where the thumb sits along the track, 0 to 1.
 * @param valueText The value formatted with its unit, also read by accessibility services.
 * @param marks Hash marks along the track.
 */
@Immutable
data class SliderUiState(
    val spec: SliderSpec,
    val value: Double,
    val position: Float,
    val valueText: String,
    val marks: List<HashMark>
) {
    val id: String get() = spec.id
    val label: String get() = spec.label
}

/** What the user did; the store or ViewModel turns these into the next [SliderPanelUiState]. */
sealed interface SliderPanelEvent {
    /** The thumb moved to [position] along the track (0 to 1), by drag, tap or accessibility action. */
    data class PositionChanged(val id: String, val position: Float) : SliderPanelEvent

    /** A value set directly, such as from app logic. */
    data class ValueChanged(val id: String, val value: Double) : SliderPanelEvent

    /** Switch between floating and pinned. */
    data object TogglePinned : SliderPanelEvent

    /** The floating panel was dragged to this offset. */
    data class Moved(val xDp: Float, val yDp: Float) : SliderPanelEvent

    /** Every slider back to its initial value. */
    data object Reset : SliderPanelEvent
}

/** The starting state: each slider at [values] when given, else at its spec's initial value. */
fun initialSliderPanelState(
    spec: SliderPanelSpec,
    values: Map<String, Double> = emptyMap(),
    mode: PanelMode = spec.mode,
    offsetXDp: Float = 0f,
    offsetYDp: Float = 0f
): SliderPanelUiState = SliderPanelUiState(
    spec = spec,
    mode = mode,
    offsetXDp = offsetXDp,
    offsetYDp = offsetYDp,
    sliders = spec.sliders.map { slider(it, values[it.id] ?: it.initial ?: it.start) }
)

/** The state after [event]. Pure: no side effects, so it is easy to test and replay. */
fun SliderPanelUiState.reduce(event: SliderPanelEvent): SliderPanelUiState = when (event) {
    is SliderPanelEvent.PositionChanged -> update(event.id) { slider(it.spec, it.spec.valueAt(event.position.toDouble()), it.marks) }
    is SliderPanelEvent.ValueChanged -> update(event.id) { slider(it.spec, event.value, it.marks) }
    SliderPanelEvent.TogglePinned -> copy(mode = if (mode == PanelMode.Pinned) PanelMode.Floating else PanelMode.Pinned)
    is SliderPanelEvent.Moved -> copy(offsetXDp = event.xDp, offsetYDp = event.yDp)
    SliderPanelEvent.Reset -> copy(sliders = sliders.map { slider(it.spec, it.spec.initial ?: it.spec.start, it.marks) })
}

private inline fun SliderPanelUiState.update(id: String, change: (SliderUiState) -> SliderUiState): SliderPanelUiState {
    val index = sliders.indexOfFirst { it.id == id }
    if (index < 0) return this // an unknown id changes nothing
    val next = change(sliders[index])
    if (next == sliders[index]) return this
    return copy(sliders = sliders.toMutableList().also { it[index] = next })
}

// Marks depend only on the spec, so value changes reuse them.
private fun slider(spec: SliderSpec, raw: Double, marks: List<HashMark> = spec.hashMarks()): SliderUiState {
    val value = spec.snap(if (raw.isNaN()) spec.initial ?: spec.start else raw)
    return SliderUiState(
        spec = spec,
        value = value,
        position = spec.positionOf(value).toFloat(),
        valueText = spec.formatWithUnit(value),
        marks = marks
    )
}
