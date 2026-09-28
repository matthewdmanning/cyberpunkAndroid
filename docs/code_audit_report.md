# CyberpunkAndroid Code Audit Report

## 1. Compliance with `AGENTS.md`

### 1.1 Signature Ordering Convention Violations
Several core components and modifiers violate the strict parameter signature ordering defined in `AGENTS.md`.
*   **`CyberButton`**: The `onClick: () -> Unit` callback is placed as the very first parameter. According to the guidelines, Callbacks & Events must be at position 8, immediately preceding the trailing `content` lambda.
*   **`CyberCard`**: The `header` and `footer` slot parameters are placed before configuration variables (`interactive`, `holo`, `interactionSource`) and animation specs. Content slots must be the final parameters.
*   **`CyberRim`**: The `strokeWidth` configuration parameter is placed after `color` and `backgroundColor`. Component Configuration (position 4) must precede Theming & Colors (position 6).

### 1.2 Missing Accessibility Parameters
The guidelines explicitly require `appendedA11y: String? = null` and `customA11y: String? = null` to be the final parameters before the block on all components and modifiers.
*   **Composables**: 15 out of 24 components completely lack these parameters. Violators include: `CyberButton`, `CyberCard`, `CyberDecrypter`, `CyberDragDrop`, `CyberField`, `CyberHexGrid`, `CyberModal`, `CyberNavigationBar`, `CyberSkeleton`, `CyberSpinnerOverlay`, `CyberTable`, `CyberTabs`, `CyberTerminal`, `CyberTime`, and `CyberBiometrics`.
*   **Modifiers**: The `cyberLongPressFill` modifier in `CyberRim.kt` omits the accessibility parameters entirely.

### 1.3 Documentation for Hardcoded Constants
*   **`cyberDatastream` (in `CyberModifiers.kt`)**: Uses the hardcoded modulo constant `100000L` without any accompanying line comment or justification as required.

### 1.4 Hardcoded Animations
*   Animations are largely well-hoisted to component signatures. However, the use of inline parameter initialization like `animationSpec = if (isActive) animationSpec else exitAnimationSpec` correctly follows the rule of not hardcoding specs directly into the functional block.

## 2. Errors & Bugs

### 2.1 `CyberDecrypter` Logic Flaw (Coroutine Starvation)
In `CyberDecrypter.kt`, there is a critical logic bug related to the `LaunchedEffect` that cycles unresolved characters:
```kotlin
val resolvedCount = (targetText.length * clampedProgress).toInt()
var tick by remember { mutableIntStateOf(0) }

LaunchedEffect(resolvedCount, tickDelayMs) {
    if (resolvedCount < targetText.length) {
        while (true) {
            delay(tickDelayMs)
            tick++
        }
    }
}
```
**The Bug:** The `LaunchedEffect` is keyed to `resolvedCount`, which is derived from the continuously animating `progress` float. As `progress` updates (e.g., at 60fps / 16ms), `resolvedCount` changes rapidly. Every time `resolvedCount` changes, the `LaunchedEffect` is cancelled and restarted. If `progress` updates faster than `tickDelayMs` (default 50ms), the coroutine is constantly preempted before `delay` finishes, meaning `tick` never increments and characters fail to cycle.
**Fix:** Remove `resolvedCount` as a key. The coroutine should be keyed to `targetText` and `tickDelayMs` only, looping as long as `clampedProgress < 1f`.

## 3. Performance Traps

The codebase suffers from numerous severe object-allocation issues inside Compose drawing phases and animation loops.

### 3.1 Misuse of `drawWithCache`
Several modifiers use `drawWithCache` but incorrectly place their heavy object allocations *inside* the `onDrawWithContent` block. `drawWithCache` is designed to allocate objects before returning `onDrawWithContent` so they can be reused across frames.
*   **`cyberAtmosphericGlow` (in `CyberModifiers.kt`)**: Allocates three distinct `Paint` objects and `BlurMaskFilter`s on every draw frame.
*   **`cyberInnerGlow` (in `CyberModifiers.kt`)**: Allocates a new `Paint` and `BlurMaskFilter` inside `onDrawWithContent` on every frame.
*   **`cyberBorder` (in `CyberModifiers.kt`)**: Allocates a new `Path` object inside `onDrawWithContent` on every frame.

### 3.2 Object Allocations inside Animation Loops
*   **`cyberSweepGradient` (in `CyberBrushes.kt`)**: Allocates a new `Matrix`, `SweepGradient`, and `ShaderBrush` on every invocation. Because this is called directly inside the `drawWithContent` loop of the rotating `cyberNeonBorderFlow` modifier, it triggers significant garbage collection churn every frame of the animation.
*   **`cyberNeonBorderFlow` (in `CyberModifiers.kt`)**: Evaluates `colors.map { ... }` inside the `drawWithContent` block, allocating a new `ArrayList` on every animated frame.
*   **`CyberFallbacks.drawOverloadFallback`**: Called by `cyberOverload` on older APIs. It allocates new `Paint` and `ColorFilter` instances on every frame during the active overload animation.
*   **`Canvas` Drawing (in `components`)**: Components like `CyberCheckbox`, `CyberTextArea`, and `CyberTextField` routinely allocate new `Path` objects directly inside their `Canvas` or `drawBehind` blocks.

### 3.3 Re-creation of `RenderEffect`
*   In `CyberShaders.kt`, the wrapper functions `scanlinesEffect` and `overloadEffect` execute `RenderEffect.createRuntimeShaderEffect()`, which creates a new effect wrapper instance. Because these functions are called continuously inside the `graphicsLayer { ... }` block of their respective modifiers (`cyberScanlines`, `cyberOverload`), a new wrapper is allocated on every frame. This should be optimized by either caching the `RenderEffect` or utilizing raw shader uniforms if possible.
