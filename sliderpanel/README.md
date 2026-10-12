# Slider panel (`:sliderpanel`)

A panel of sliders that floats over content or pins to an edge. JSON defines each slider's label, range, scale and hash marks. The module depends on `:cyberpunkandroid` for its theme and buttons.

## Use it

```kotlin
// Data layer: read and check the JSON.
val spec = when (val result = parseSliderPanelSpec(json)) {
    is SpecParseResult.Success -> result.spec
    is SpecParseResult.Failure -> error(result.errors.joinToString("\n"))
}

// UI layer: the ViewModel owns the state.
val viewModel: SliderPanelViewModel = viewModel(factory = SliderPanelViewModel.factory(spec))
Box(Modifier.fillMaxSize()) {
    AppContent(values = viewModel.uiState.collectAsState().value.values)
    CyberSliderPanel(viewModel) // last child, so it floats over the content
}
```

## Architecture (MVVM)

| Layer | Type | Job |
| --- | --- | --- |
| Model | `SliderPanelSpec`, `SliderSpec`, `ScaleSpec`, `MarksSpec` | Immutable data from JSON. `parseSliderPanelSpec` reports every error with its JSON path. |
| Model logic | `valueAt`, `positionOf`, `snap`, `hashMarks` | Pure functions for the scale mapping, steps and marks. |
| State | `SliderPanelUiState`, `SliderPanelEvent`, `reduce` | Pure reducer: (state, event) → next state. |
| State holder | `SliderPanelStore` | A `StateFlow` and the reducer, in plain Kotlin. |
| ViewModel | `SliderPanelViewModel` | Holds a store. `SavedStateHandle` keeps values, mode and position. |
| View | `CyberSliderPanel`, `CyberSpecSlider` | Stateless composables. They draw state and send events. |

Data flows one way. The view sends events. The ViewModel reduces them. The view draws the new state.

## JSON schema

### Panel

| Key | Type | Default | What it does |
| --- | --- | --- | --- |
| `title` | string | none | Header text. |
| `mode` | `"floating"` or `"pinned"` | `"floating"` | Starting mode. The header button toggles it. |
| `pinEdge` | `"top"`, `"bottom"`, `"start"`, `"end"` | `"bottom"` | Edge for pinned mode. |
| `scrollable` | boolean | `false` | The slider list scrolls past `maxHeightDp`. |
| `maxHeightDp` | number | `320` | Maximum list height when scrollable. |
| `widthDp` | number | `300` | Width when floating, or when pinned to start or end. |
| `sliders` | array | required | One or more sliders. Each `id` must be unique. |

Floating panels move when you drag the header. They stay inside the area.

### Slider

| Key | Type | Default | What it does |
| --- | --- | --- | --- |
| `id` | string | required | Key for the value. |
| `label` | string | required | Text above the track. |
| `start` | number | required | Value at the track start. |
| `stop` | number | required | Value at the track end. It can be less than `start`. |
| `initial` | number | `start` | Starting value. |
| `step` | number | `0` | Snap spacing from `start`. `0` is continuous. |
| `unit` | string | `""` | Text after the value. |
| `decimals` | integer | `2` | Digits after the decimal point. |
| `scale` | object | linear | Position-to-value mapping. |
| `marks` | object | start and stop | Hash marks. |

### Scale (`"type"`)

| Type | Extra keys | Mapping |
| --- | --- | --- |
| `"linear"` | none | Equal distance gives equal change. |
| `"log"` | none | Equal distance gives equal ratio. `start` and `stop` must be above 0. |
| `"power"` | `exponent` | value = start + (stop − start) · position^exponent. |
| `"piecewise"` | `points`: `[{ "position", "value" }]` | Straight lines between points. Positions go from 0 to 1. Values go from `start` to `stop` in one direction. |

### Marks

| Key | Type | What it does |
| --- | --- | --- |
| `values` | `[{ "value", "label"? }]` | Explicit major marks. |
| `every` | number | A major mark every N units from `start`. |
| `minor` | integer, 0–20 | Minor marks between neighboring majors, spaced evenly in value. |
| `labels` | boolean | Show labels on major marks. Default `true`. |

Without `values` or `every`, the majors are `start` and `stop`. On a log scale, each power of ten is also a major. A log scale with `"minor": 8` gives the usual 2 to 9 ticks.

## Verification

- JVM unit tests cover parsing, validation, scales, snapping, marks and the reducer.
- The composables compile against Compose Desktop 1.7.3 with stubs for Android-only APIs.
- Not done: a Gradle Android build, a preview render, an emulator run, a device run.
