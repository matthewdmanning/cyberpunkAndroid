# Easing Survey Results

This document summarizes the findings from a survey of the `cyberpunkAndroid` codebase to identify classes and functions that would benefit from using an Easing function, or utilizing improved/custom easings.

## Overview

Overall, the codebase relies heavily on the default Compose `FastOutSlowInEasing` (implicit in `tween` without an `easing` parameter) and features several hardcoded `CubicBezierEasing` definitions spread across modifiers. Abstracting these into `CyberConfig.Easings` and replacing implicit defaults with custom, thematic easings would enhance the cyberpunk aesthetic and improve maintainability.

## Detailed Findings

### 1. `CyberModifiers.kt`

This file contains the highest concentration of animation logic and presents several opportunities for refactoring:

**Hardcoded `CubicBezierEasing` usages:**
* **`DecelEasing` equivalents:** Lines 699, 707, and 748 manually define `CubicBezierEasing(0f, 0f, 0.2f, 1f)` in `keyframes`. This perfectly matches `CyberConfig.Easings.DecelEasing` and should be updated to use the configured value.
* **Standard Easing equivalents:** Line 627 uses `CubicBezierEasing(0.4f, 0f, 0.2f, 1f)`, which is identical to Compose's standard `FastOutSlowInEasing`.
* **Missing from Config:** Line 747 uses `CubicBezierEasing(0.8f, 0f, 1f, 1f)` and Line 781 uses `CubicBezierEasing(0.4f, 0f, 0.6f, 1f)`. These custom curves should be extracted to `CyberConfig.Easings` (e.g., as `AccelEasing` or `GlowPulseEasing`) for central management.

**Implicit Default Easings:**
* There are widespread uses of `tween(300)` without an explicit easing parameter for state transitions and resets (e.g., lines 91, 92, 170, 292, 293, 401, 472, 540, 590, 669, 712, 784). These all implicitly default to `FastOutSlowInEasing`. Applying a distinct custom easing like `CyberConfig.Easings.CyberEasing` or `OutExpoEasing` would give these micro-interactions a more stylized, digital feel.

### 2. Core Components (`CyberBadge`, `CyberCheckbox`, `CyberNavigationBar`, `CyberTextArea`, `CyberTextField`)

Several fundamental components declare animation specs using `tween(duration)` without specifying an explicit `easing` parameter. Like the modifiers, these fallback to `FastOutSlowInEasing`:

* **`CyberBadge.kt`**: `pulseAnimationSpec` uses `tween(...)` for an `infiniteRepeatable` pulse. A pulse animation often looks sharper with `LinearEasing` or a custom custom `CyberConfig.Easings` curve rather than the default.
* **`CyberCheckbox.kt`**: `colorAnimationSpec` and `checkAnimationSpec` use implicit easings. 
* **`CyberNavigationBar.kt`**: `colorAnimationSpec` and `scaleAnimationSpec` use implicit easings. `beaconAnimationSpec` explicitly uses `FastOutSlowInEasing`, which could be updated to a more thematic curve.
* **`CyberTextArea.kt` & `CyberTextField.kt`**: `colorAnimationSpec` for border and focus transitions uses implicit easings. Replacing these with `CyberConfig.Easings.OutExpoEasing` could provide a more abrupt, "snappy" tech aesthetic for focus states.

### 3. Continuous Animations (`CyberSkeleton`, `CyberSpinner`)

* **`CyberSkeleton.kt` & `CyberSpinner.kt`**: Both animations currently use `LinearEasing`. While typical for continuous rotation and shimmer effects, they could be evaluated to see if a subtle custom non-linear easing creates a more jagged or mechanical feel appropriate for the theme.

## Recommendations

1. **Centralize Hardcoded Easings:** Extract any raw `CubicBezierEasing` usages in `CyberModifiers.kt` to `CyberConfig.Easings`.
2. **Review Implicit Defaults:** Audit usages of `tween()` across `CyberModifiers.kt` and core components. Assign explicit thematic easings where a more digital/cyberpunk micro-interaction is desired.
3. **Reuse Existing Configs:** Replace `CubicBezierEasing(0f, 0f, 0.2f, 1f)` in `keyframes` with the existing `CyberConfig.Easings.DecelEasing`.
