package com.example.cyberpunkandroid.sliderpanel.state

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cyberpunkandroid.sliderpanel.model.PanelMode
import com.example.cyberpunkandroid.sliderpanel.model.SliderPanelSpec
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * MVVM owner of a slider panel: keeps its state across configuration changes and, through
 * [SavedStateHandle], across process death. Values, mode and floating position are restored.
 *
 * Parse the JSON in your data layer with `parseSliderPanelSpec`, then create the ViewModel with
 * [factory]. Collect [uiState] in the UI and pass [onEvent] to `CyberSliderPanel`. Read the
 * values for your own logic from `uiState.value.values`, or map the flow.
 */
class SliderPanelViewModel(spec: SliderPanelSpec, private val saved: SavedStateHandle) : ViewModel() {

    private val store = SliderPanelStore(
        initialSliderPanelState(
            spec = spec,
            values = spec.sliders.mapNotNull { s -> saved.get<Double>(valueKey(s.id))?.let { s.id to it } }.toMap(),
            mode = saved.get<String>(ModeKey)?.let { name -> PanelMode.entries.firstOrNull { it.name == name } } ?: spec.mode,
            offsetXDp = saved.get<Float>(XKey) ?: 0f,
            offsetYDp = saved.get<Float>(YKey) ?: 0f
        )
    )

    val uiState: StateFlow<SliderPanelUiState> = store.state

    init {
        viewModelScope.launch {
            store.state.collect { state ->
                state.sliders.forEach { saved[valueKey(it.id)] = it.value }
                saved[ModeKey] = state.mode.name
                saved[XKey] = state.offsetXDp
                saved[YKey] = state.offsetYDp
            }
        }
    }

    fun onEvent(event: SliderPanelEvent) = store.onEvent(event)

    companion object {
        private const val ModeKey = "sliderpanel.mode"
        private const val XKey = "sliderpanel.x"
        private const val YKey = "sliderpanel.y"
        private fun valueKey(id: String) = "sliderpanel.value.$id"

        /** Factory for `viewModel(factory = SliderPanelViewModel.factory(spec))`. */
        fun factory(spec: SliderPanelSpec): ViewModelProvider.Factory = viewModelFactory {
            initializer { SliderPanelViewModel(spec, createSavedStateHandle()) }
        }
    }
}
