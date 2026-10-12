package com.example.cyberpunkandroid.sliderpanel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberButtonSize
import com.example.cyberpunkandroid.components.CyberButtonStyle
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.sliderpanel.model.PanelMode
import com.example.cyberpunkandroid.sliderpanel.model.PinEdge
import com.example.cyberpunkandroid.sliderpanel.state.SliderPanelEvent
import com.example.cyberpunkandroid.sliderpanel.state.SliderPanelUiState
import com.example.cyberpunkandroid.sliderpanel.state.SliderPanelViewModel
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.roundToInt

/**
 * A panel of JSON-defined sliders that floats over content or is pinned to an edge.
 *
 * It fills the space its parent gives it (narrow that with [modifier]); only the panel itself
 * takes touches, so put it over your content, for example as the last child of a `Box`. Floating, the panel sits at the state's offset and moves by
 * dragging its header; it stays inside the area. Pinned, it docks to the spec's edge: full
 * width at the top or bottom, full height at the start or end. The header's buttons reset the
 * sliders and switch between floating and pinned. With a scrollable spec, the slider list
 * scrolls once it is taller than the spec's maximum height.
 *
 * Stateless, for MVVM: it draws [state] and reports everything through [onEvent]. Own the
 * state in `SliderPanelViewModel` (or a `SliderPanelStore`) and pass its `onEvent`.
 *
 * @param state What to draw.
 * @param modifier Modifier for the area the panel floats or pins in.
 * @param color Accent for tracks, thumbs and the frame.
 * @param containerColor Panel background.
 * @param textColor Title and slider text.
 * @param appendedA11y Optional text appended to the panel's accessibility name.
 * @param customA11y Optional custom accessibility name for the panel.
 * @param onEvent Receives slider moves, panel drags, pin toggles and resets.
 */
@Composable
fun CyberSliderPanel(
    state: SliderPanelUiState,
    modifier: Modifier = Modifier,
    color: Color = CyberTheme.colors.primary,
    containerColor: Color = CyberTheme.colors.surface,
    textColor: Color = CyberTheme.colors.textSecondary,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onEvent: (SliderPanelEvent) -> Unit
) {
    val send by rememberUpdatedState(onEvent)
    BoxWithConstraints(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        var panelSize by remember { mutableStateOf(IntSize.Zero) }
        val areaWidth = if (constraints.maxWidth == Constraints.Infinity) Int.MAX_VALUE else constraints.maxWidth
        val areaHeight = if (constraints.maxHeight == Constraints.Infinity) Int.MAX_VALUE else constraints.maxHeight
        val maxX = (areaWidth - panelSize.width).coerceAtLeast(0).toFloat()
        val maxY = (areaHeight - panelSize.height).coerceAtLeast(0).toFloat()

        val frame = Modifier
            .cyberComponentSemantics("CyberSliderPanel", appendedA11y, customA11y)
            .semantics { state.title?.let { paneTitle = it } }
            .background(containerColor)
            .border(1.dp, color.copy(alpha = 0.6f))
            .padding(12.dp)

        when (state.mode) {
            PanelMode.Floating -> {
                val x = with(density) { state.offsetXDp.dp.toPx() }.coerceIn(0f, maxX)
                val y = with(density) { state.offsetYDp.dp.toPx() }.coerceIn(0f, maxY)
                val latest by rememberUpdatedState(x to y)
                val bounds by rememberUpdatedState(maxX to maxY)
                val drag = Modifier.pointerInput(Unit) {
                    var px = 0f
                    var py = 0f
                    detectDragGestures(onDragStart = { px = latest.first; py = latest.second }) { change, amount ->
                        change.consume()
                        // Accumulate here: state round-trips through the owner and can lag a frame.
                        px = (px + amount.x).coerceIn(0f, bounds.first)
                        py = (py + amount.y).coerceIn(0f, bounds.second)
                        send(with(density) { SliderPanelEvent.Moved(px.toDp().value, py.toDp().value) })
                    }
                }
                Panel(
                    state = state,
                    modifier = Modifier
                        .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                        .width(state.widthDp.dp)
                        .onSizeChanged { panelSize = it }
                        .then(frame),
                    handle = drag,
                    color = color,
                    textColor = textColor,
                    send = send
                )
            }
            PanelMode.Pinned -> {
                val placement = when (state.pinEdge) {
                    PinEdge.Top -> Modifier.align(Alignment.TopCenter).fillMaxWidth()
                    PinEdge.Bottom -> Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    PinEdge.Start -> Modifier.align(Alignment.CenterStart).fillMaxHeight().width(state.widthDp.dp)
                    PinEdge.End -> Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(state.widthDp.dp)
                }
                Panel(state, placement.onSizeChanged { panelSize = it }.then(frame), Modifier, color, textColor, send)
            }
        }
    }
}

@Composable
private fun Panel(
    state: SliderPanelUiState,
    modifier: Modifier,
    handle: Modifier,
    color: Color,
    textColor: Color,
    send: (SliderPanelEvent) -> Unit
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Header(state, handle, color, textColor, send)
        val list = if (state.scrollable) {
            Modifier.weight(1f, fill = false).heightIn(max = state.maxHeightDp.dp).verticalScroll(rememberScrollState())
        } else Modifier
        Column(list, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (slider in state.sliders) {
                CyberSpecSlider(
                    slider = slider,
                    modifier = Modifier.fillMaxWidth(),
                    color = color,
                    textColor = textColor,
                    onPositionChange = { send(SliderPanelEvent.PositionChanged(slider.id, it)) }
                )
            }
        }
    }
}

@Composable
private fun Header(
    state: SliderPanelUiState,
    handle: Modifier,
    color: Color,
    textColor: Color,
    send: (SliderPanelEvent) -> Unit
) {
    val style = CyberTheme.typography.terminal.copy(color = textColor)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // The title area is the drag handle while floating.
        Row(handle.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (state.mode == PanelMode.Floating) BasicText("⠿ ", style = style.copy(color = color))
            BasicText(state.title.orEmpty(), style = style)
            Spacer(Modifier.weight(1f))
        }
        CyberButton(
            style = CyberButtonStyle.Ghost,
            size = CyberButtonSize.Small,
            customA11y = "Reset sliders",
            onClick = { send(SliderPanelEvent.Reset) }
        ) { BasicText("RESET", style = style) }
        CyberButton(
            style = CyberButtonStyle.Ghost,
            size = CyberButtonSize.Small,
            customA11y = if (state.mode == PanelMode.Pinned) "Float panel" else "Pin panel",
            onClick = { send(SliderPanelEvent.TogglePinned) }
        ) { BasicText(if (state.mode == PanelMode.Pinned) "FLOAT" else "PIN", style = style) }
    }
}

/** [CyberSliderPanel] bound to a [SliderPanelViewModel]: collects its state and sends it events. */
@Composable
fun CyberSliderPanel(
    viewModel: SliderPanelViewModel,
    modifier: Modifier = Modifier,
    color: Color = CyberTheme.colors.primary,
    containerColor: Color = CyberTheme.colors.surface,
    textColor: Color = CyberTheme.colors.textSecondary,
    appendedA11y: String? = null,
    customA11y: String? = null
) {
    val state by viewModel.uiState.collectAsState()
    CyberSliderPanel(state, modifier, color, containerColor, textColor, appendedA11y, customA11y, onEvent = viewModel::onEvent)
}
