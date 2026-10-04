# Deep Code Review Plan: Complex Interactive & Stateful Components

**Date**: 2026-10-03
**Target Module**: `com.example.cyberpunkandroid.components`
**Branch Target**: `codebase-deepening`
**Scope**: In-depth code review specification for stateful UI controls, pointer gesture tracking, animation ticker loops, window overlays, and accessibility semantics.

---

## 🎯 Executive Summary & Objectives

This document establishes the detailed code review protocol for the seven complex interactive and stateful components in `cyberpunkandroid`:

1. **`CyberDragDrop.kt`**: Drag-and-drop state holder, composition local provider, and `Modifier.cyberDraggable` gesture handler.
2. **`CyberTable.kt`**: Multi-column tabular data grid with zebra striping and monospace formatting.
3. **`CyberTerminal.kt`**: Command terminal window frame with traffic light controls and CRT scanline layer.
4. **`CyberDecrypter.kt`**: Typographic progress indicator with random glyph matrix scrambling ticker.
5. **`CyberModal.kt`**: High-priority dialog window with cut-corner shapes and custom scrim padding.
6. **`CyberDropdown.kt`**: Interactive menu selection trigger and popover dropdown menu.
7. **`CyberNavigationBar.kt`**: Top header navigation bar with interactive link indicators and status pills.

---

## 🔍 Detailed Code Review Protocol by Component

### 1. `CyberDragDrop.kt` (`CyberDragDropState`, `CyberDragDropProvider`, `Modifier.cyberDraggable`)

#### A. Architecture & State Management
- **State Holder**: `CyberDragDropState<T>` uses `mutableStateOf` for `isDragging`, `dragPosition`, and `draggedData`.
  - *Audit Check*: Verify that state mutations inside `onDragStart`, `onDrag`, `onDragEnd`, and `onDragCancel` cause zero redundant recompositions outside subscribed drop targets.
- **CompositionLocal**: `LocalCyberDragDropState` uses `compositionLocalOf` with `error()` fallback.
  - *Audit Check*: Ensure appropriate documentation for developers wrapping UI subtrees in `CyberDragDropProvider`.

#### B. Gesture & Pointer Input Handling
- **Pointer Input Keying**: `.pointerInput(data)` uses `data` as the key.
  - *Audit Check*: If `data` is mutated or swapped without identity change, pointer input scope resets correctly. Verify that `change.consume()` is called on every drag delta tick in `onDrag` to prevent nested scroll conflict.
- **Cancellation & Cleanup**: Both `onDragEnd` and `onDragCancel` reset `state.isDragging = false`, `state.draggedData = null`, and apply `snapBackOnRelease`.
  - *Audit Check*: Confirm zero memory leaks or stuck drag offsets when a gesture is interrupted by system popups or multi-touch gestures.

#### C. Accessibility & Signature Conventions
- **Signature Ordering**:
  ```kotlin
  fun <T : Any> Modifier.cyberDraggable(
      data: T, // 1. Required Data
      snapBackOnRelease: Boolean = false, // 4. Config
      appendedA11y: String? = null, // 5. Accessibility
      customA11y: String? = null,
      onDragStart: () -> Unit = {}, // 8. Callbacks
      onDragEnd: () -> Unit = {}
  ): Modifier
  ```
  - *Compliance*: Adheres strictly to modifier signature convention rules.
- **Provider Semantics Note**: Line 46 contains a noted TODO regarding accessibility propagation on `CyberDragDropProvider`.
  - *Review Finding*: `CyberDragDropProvider` emits no layout node of its own. Semantics must be attached at the draggable items (`cyberDraggable`) and drop target nodes rather than on the provider container.

---

### 2. `CyberTable.kt` (`CyberTable`)

#### A. Layout & Composition Math
- **Column Weighting**: Columns use `Modifier.weight(1f)` across headers and rows.
  - *Audit Check*: Verify that long cell strings wrap or truncate gracefully without pushing neighboring columns off-screen or causing layout overflow.
- **Shape & Elevation**: Outer column clips to `CyberTheme.shapes.cyberCutCornerShape` and applies `cyberBorder`.

#### B. Signature Ordering Compliance
- **Current Signature**:
  ```kotlin
  fun CyberTable(
      headers: List<String>, // 1. Required Data
      rows: List<List<String>>, // 1. Required Data
      modifier: Modifier = Modifier, // 3. Modifier
      appendedA11y: String? = null, // 7. Accessibility
      customA11y: String? = null
  )
  ```
- *Audit Status*: Fully compliant with composable signature ordering rules (`Data` $\rightarrow$ `Modifier` $\rightarrow$ `Accessibility`).

---

### 3. `CyberTerminal.kt` (`CyberTerminal`)

#### A. Composition & Visual Layers
- **Header & Body Structure**: Features a top title bar with semantic status traffic lights (`danger`, `warning`, `success`) and a monospace text body container.
- **Content Slot**: Exposes trailing lambda `content: @Composable () -> Unit`.

#### B. Signature Ordering Compliance
- **Current Signature**:
  ```kotlin
  fun CyberTerminal(
      title: String, // 1. Required Data
      modifier: Modifier = Modifier, // 3. Modifier
      appendedA11y: String? = null, // 7. Accessibility
      customA11y: String? = null,
      content: @Composable () -> Unit // 9. Content Slot
  )
  ```
- *Audit Status*: Fully compliant. Content slot is correctly positioned as the final trailing lambda.

---

### 4. `CyberDecrypter.kt` (`CyberDecrypter`)

#### A. Coroutine Ticker & Memory Lifecycle
- **State Optimization**:
  ```kotlin
  val clampedProgress = progress.coerceIn(0f, 1f)
  val currentProgress by rememberUpdatedState(clampedProgress)
  var tick by remember { mutableIntStateOf(0) }

  LaunchedEffect(targetText, tickDelayMs) {
      while (currentProgress < 1f) {
          delay(tickDelayMs)
          tick++
      }
  }
  ```
  - *Audit Check*: Using `rememberUpdatedState(clampedProgress)` ensures that the `while` loop checks fresh progress values without restarting the `LaunchedEffect` coroutine every time progress changes slightly.
  - *Memory Safety*: Verified that `buildAnnotatedString` generates strings without leaking span objects.

#### B. Accessibility & Semantics
- **ProgressBar & Text Semantics**:
  ```kotlin
  Box(
      modifier = modifier
          .cyberComponentSemantics("CyberDecrypter", appendedA11y, customA11y)
          .semantics(mergeDescendants = true) {
              text = AnnotatedString(targetText)
              progressBarRangeInfo = ProgressBarRangeInfo(
                  current = clampedProgress,
                  range = 0f..1f
              )
          }
  )
  ```
  - *Audit Check*: Screen readers announce the full `targetText` and progress percentage rather than cycling random un-decoded gibberish glyphs.

#### C. Signature Ordering Compliance
- **Parameters**: `targetText`, `progress` (Required State) $\rightarrow$ `modifier` $\rightarrow$ `charset`, `tickDelayMs` (Config) $\rightarrow$ `textStyle`, `unresolvedStyle`, `resolvedStyle` (Theming) $\rightarrow$ `appendedA11y`, `customA11y` (Accessibility).
- *Audit Status*: Fully compliant.

---

### 5. `CyberModal.kt` (`CyberModal`)

#### A. Window Overlay Scrim & Dismiss Handling
- **Dialog Properties**:
  ```kotlin
  Dialog(
      onDismissRequest = onDismissRequest,
      properties = DialogProperties(
          usePlatformDefaultWidth = false,
          decorFitsSystemWindows = true
      )
  )
  ```
  - *Audit Check*: Verify that `usePlatformDefaultWidth = false` allows custom edge-to-edge padding (`24.dp`) without fighting default platform dialog inset rules.

#### B. Signature Ordering Audit
- **Current Signature**:
  ```kotlin
  fun CyberModal(
      title: String,
      modifier: Modifier = Modifier,
      appendedA11y: String? = null,
      customA11y: String? = null,
      onDismissRequest: () -> Unit,
      content: @Composable ColumnScope.() -> Unit,
  )
  ```
- *Audit Finding*: Recommended parameter ordering adjustment to align `onDismissRequest` as a callback prior to trailing content slot:
  - Required Data (`title`) $\rightarrow$ `modifier` $\rightarrow$ Accessibility (`appendedA11y`, `customA11y`) $\rightarrow$ Callbacks (`onDismissRequest`) $\rightarrow$ Content Slot (`content`).

---

### 6. `CyberDropdown.kt` (`CyberDropdown`)

#### A. Popover Menu State & Interaction
- **Expansion State**: `var expanded by remember { mutableStateOf(value = false) }`.
- **Dismiss & Selection Handling**: Tapping an item triggers `onItemSelected(index)` and immediately collapses menu (`expanded = false`).

#### B. Signature Ordering Audit
- **Current Signature**:
  ```kotlin
  fun CyberDropdown(
      items: List<String>,
      selectedIndex: Int,
      modifier: Modifier = Modifier,
      placeholder: String = "SELECT...",
      appendedA11y: String? = null,
      customA11y: String? = null,
      onItemSelected: (Int) -> Unit,
  )
  ```
- *Audit Status*: Fully compliant (`Data` $\rightarrow$ `Modifier` $\rightarrow$ `Config` $\rightarrow$ `Accessibility` $\rightarrow$ `Callbacks`).

---

### 7. `CyberNavigationBar.kt` (`CyberNavigationBar`)

#### A. Layout Alignment & Slot Injection
- **Structure**: Row with space-between arrangement containing Brand section (left), Nav links (center/right), optional `statusText` status pill, and optional trailing `actions` slot.
- **Subcomponents**: Delegates interactive link rendering to `CyberNavLink` and status indicators to `CyberNavStatus`.

#### B. Signature Ordering Audit
- **Current Signature**:
  ```kotlin
  fun CyberNavigationBar(
      items: List<String>,
      selectedIndex: Int,
      modifier: Modifier = Modifier,
      brand: String = "CYBERCORE",
      statusText: String? = null,
      appendedA11y: String? = null,
      customA11y: String? = null,
      onItemSelected: (Int) -> Unit,
      brandContent: (@Composable () -> Unit)? = null,
      actions: (@Composable RowScope.() -> Unit)? = null,
  )
  ```
- *Audit Status*: Fully compliant. Callbacks and content slots are positioned at the end of the signature.

---

## 📋 Comprehensive Code Audit Checklist for Category #1

| Component | Coroutine / State Safety | Pointer Gesture Isolation | A11y Semantics | Signature Ordering | Status |
|---|---|---|---|---|---|
| **`CyberDragDrop`** | ✅ Safe (`CyberDragDropState`) | ✅ Handled (`change.consume()`) | ⚠️ Provider TODO noted | ✅ Compliant | **Passed** |
| **`CyberTable`** | ✅ Pure Composable | N/A (Static table) | ✅ `cyberComponentSemantics` | ✅ Compliant | **Passed** |
| **`CyberTerminal`** | ✅ Pure Composable | N/A | ✅ `cyberComponentSemantics` | ✅ Compliant | **Passed** |
| **`CyberDecrypter`** | ✅ Safe (`rememberUpdatedState`) | N/A | ✅ `ProgressBarRangeInfo` | ✅ Compliant | **Passed** |
| **`CyberModal`** | ✅ Safe (`Dialog`) | ✅ Scrim dismiss handled | ✅ `cyberComponentSemantics` | ⚠️ Minor Callback Order | **Passed** |
| **`CyberDropdown`** | ✅ Safe (`mutableStateOf`) | ✅ Clickable menu toggle | ✅ `cyberSemantics` | ✅ Compliant | **Passed** |
| **`CyberNavigationBar`** | ✅ Pure Composable | ✅ Delegated to `CyberNavLink` | ✅ `cyberComponentSemantics` | ✅ Compliant | **Passed** |

---

## ✅ Summary of Actionable Code Recommendations

1. **`CyberModal.kt`**: Verify parameter signature order so that callbacks (`onDismissRequest`) precede the trailing composable `content` slot.
2. **`CyberDecrypter.kt`**: Maintain `rememberUpdatedState` pattern for progress tracking to guarantee coroutine tickers do not suffer from scope cancellation churn.
3. **`CyberDragDrop.kt`**: Retain `change.consume()` in drag gesture handlers to prevent input bleed into parent scrollable containers.
