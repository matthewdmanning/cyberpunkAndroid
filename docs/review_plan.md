# Codebase Deepening Review Plan

This document outlines the systematic code review protocol for the `codebase-deepening` branch prior to merging into `main`. The review validates architectural boundaries, Compose resource optimizations, material surface recipes, directional lighting, API signature conventions, and accessibility compliance.

---

## 🎯 Review Objectives

> **Project Scope Note**: Low-resource/budget devices and legacy API compatibility fallbacks are explicitly **out of scope**. Performance is defined as **95% visual appearance fidelity**—combining Compose best practices, draw-phase state isolation, and high-fidelity graphics that maintain high FPS with zero frame drops/jank.

1. **Verify Architectural Integrity**: Ensure components act as functional, unbloated base classes with visual flair applied via `Modifier.cyber...` extensions.
2. **Enforce Implementation Efficiency**: Audit `graphicsLayer` usage, allocation caching (`remember`), and AGSL shaders to ensure animation ticks remain isolated in the draw phase without triggering recompositions or unnecessary offscreen compositing passes.
3. **Validate Surface & Shadow Recipes**: Confirm glass, metal, and plastic surface styling utilize standard Compose primitives (`Brush`, `dropShadow`, `innerShadow`) and expose configurable directional lighting parameters.
4. **Maintain API Signature Consistency**: Enforce standard parameter ordering rules across all public composables and modifier extensions.
5. **Ensure Full Accessibility**: Verify `appendedA11y` and `customA11y` semantics handling across all UI primitives.

---

## 📋 Review Checklist & Gates

### Gate 1: Architecture & API Conventions
- [ ] **Minimal Base Classes**: Components are free from hardcoded stylistic bloat. Visual flair is applied exclusively via modifier extensions.
- [ ] **Signature Ordering Convention**:
  - **Modifiers**: `[1-2. Visual & Config]` $\rightarrow$ `[3. Trigger & InteractionSource]` $\rightarrow$ `[4. Animation Specs]` $\rightarrow$ `[5. Accessibility (appendedA11y, customA11y)]`.
  - **Composables**: `[1. Required Data]` $\rightarrow$ `[2. Optional Data]` $\rightarrow$ `[3. modifier: Modifier]` $\rightarrow$ `[4. Config]` $\rightarrow$ `[5. Specs]` $\rightarrow$ `[6. Theming]` $\rightarrow$ `[7. Accessibility]` $\rightarrow$ `[8. Callbacks]` $\rightarrow$ `[9. Content Slot]`.
- [ ] **Zero Hardcoded Animation Specs**: No `tween()`, `spring()`, `infiniteRepeatable()`, or `delay()` hardcoded inside `LaunchedEffect`, `animate*AsState`, or `AnimatedVisibility`. All specs must be hoisted to function parameters.
- [ ] **Zero Magic Numbers**: All spatial dimensions, durations, and colors reference `CyberPrimitives`, `CyberConfig`, or `CyberTheme`.

### Gate 2: Resource Optimization & Graphics Layers
*Reference: [ANDROID-COMPOSE-GRAPHICS-LAYER-RESOURCE-OPTIMIZATION.md](reference/ANDROID-COMPOSE-GRAPHICS-LAYER-RESOURCE-OPTIMIZATION.md)*

- [ ] **No Unnecessary Offscreen Rasterization**:
  - `graphicsLayer` uses `CompositingStrategy.Auto` unless offscreen rendering is strictly required for correctness.
  - `CompositingStrategy.Offscreen` is NOT forced for static borders or basic overlays.
- [ ] **Draw-Phase State Deferral**:
  - Shader uniforms and effect clocks (e.g., `clock.value`, `level`) are read inside `uniforms` or `drawWithCache` / `onDrawWithContent` lambdas, NEVER in the composable body.
- [ ] **No Offscreen Background Captures**:
  - `cyberBackdropBlur` uses foreground translucent washes, gradient `Brush`es, and `RenderEffect` (API 31+) on foreground nodes without forcing heavy full-screen offscreen background capture layers.
- [ ] **Resource Documentation**:
  - Resource-intensive techniques document: (1) Jetpack Compose example, (2) condition causing the expensive path, (3) cheaper standard alternative, and (4) correctness constraints.

### Gate 3: Material Surfaces & Directional Inlay Shadows
*References: [ANDROID-MATERIAL-SURFACE-APPEARANCE-REFERENCE.md](reference/ANDROID-MATERIAL-SURFACE-APPEARANCE-REFERENCE.md) & [ANDROID-DIRECTIONAL-INLAY-SHADOW-REFERENCE.md](reference/ANDROID-DIRECTIONAL-INLAY-SHADOW-REFERENCE.md)*

- [ ] **Standard Primitives First**:
  - Glass, metal, and plastic surface recipes prioritize standard Compose primitives (`Brush`, `drawOutline`, `dropShadow`, `innerShadow`) over custom AGSL/RenderEffect passes.
- [ ] **Configurable Directional Lighting**:
  - Inset and carved container borders expose directional offsets (`DpOffset`), spread, radius, and color as configurable styling parameters rather than hardcoding light directions.
- [ ] **Lightweight Container Borders**:
  - `cyberBorder` uses standard `Stroke` and `drawOutline` in the normal drawing path. Active multi-pass glows are reserved for `cyberGlowBorder`.

### Gate 4: Animated Text Transitions & Shared Bounds
*Reference: [ANDROID-ANIMATED-TEXT-TRANSITION-REFERENCE.md](reference/ANDROID-ANIMATED-TEXT-TRANSITION-REFERENCE.md)*

- [ ] **Transform/Bounds Scaling**:
  - Prominent display text moving between positions utilizes bounds-based scaling (`ScaleToBounds`) rather than continuously recalculating font sizes or reflowing text during transition.

---

## 🛠️ Review Execution Workflow

1. **Static Analysis & Diff Audit**:
   - Run `git diff main...codebase-deepening` to inspect all code changes against the gate criteria.
   - Run `git diff --check` to verify zero trailing whitespace or line ending issues.
2. **Automated Verification**:
   - Run `./gradlew test` to execute the full unit test suite.
3. **Physical & Emulator Inspection**:
   - Run the sample app (`:sample`) on API 30 (fallback path) and API 33 (AGSL shader path).
4. **Sign-off**:
   - Document review findings, log profiling metrics, and approve for merge.
