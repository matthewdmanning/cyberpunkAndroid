package com.example.cyberpunkandroid.sliderpanel.state

import com.example.cyberpunkandroid.sliderpanel.model.SliderPanelSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds a slider panel's state as a [StateFlow] and applies events with [reduce]. Plain Kotlin
 * with no Android types, so a ViewModel, a presenter or a test can own it.
 */
class SliderPanelStore(initial: SliderPanelUiState) {
    constructor(spec: SliderPanelSpec, values: Map<String, Double> = emptyMap()) : this(initialSliderPanelState(spec, values))

    private val mutableState = MutableStateFlow(initial)
    val state: StateFlow<SliderPanelUiState> = mutableState.asStateFlow()

    fun onEvent(event: SliderPanelEvent) = mutableState.update { it.reduce(event) }
}
