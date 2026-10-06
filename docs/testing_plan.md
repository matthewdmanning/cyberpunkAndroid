# Codebase Deepening Testing Plan

This document details the multi-tiered verification protocol for validating component rendering, AGSL shaders, fallback paths, performance frame budgets, and accessibility compliance on the `codebase-deepening` branch.

---

## 🎯 Testing Objectives

> **Project Scope Note**: Low-resource/budget devices and legacy compatibility fallbacks are explicitly **out of scope**. Performance is defined as **95% visual appearance fidelity**—combining Compose best practices, clean draw-phase isolation, and high-fidelity graphics that maintain high FPS with zero frame drops/jank.

1. **Automated Regression Prevention**: Ensure zero compilation errors and passing unit tests across JVM, Robolectric, and Compose UI test suites.
2. **Implementation Efficiency Benchmarking**: Validate that shader uniform bindings and effect clocks remain isolated in the draw phase ($\le 16.6\text{ ms}$ frame budget) without triggering unnecessary composition passes or memory leaks.
3. **Visual & Accessibility Sweep**: Audit components under light/dark themes, varying screen densities, 100%–200% font scaling, and TalkBack accessibility navigation.

---

## 🧪 Testing Tiers & Execution

### Tier 1: Automated Unit & Robolectric Suite
*Execution Command:*
```cmd
./gradlew test
```

- **Scope**:
  - `CyberComponentsTest`: Validates component instantiation, parameter bounds, and slot layout properties.
  - `CyberEffectsFallbackTest`: Renders all effect fallback passes under Robolectric (API 30 & API 33) to ensure exception-free drawing.
  - `CyberTokensTest`: Validates theme tokens, primitive colors, shapes, and status role maps.
  - `CyberPathEffectsTest`: Tests path border geometry fitting, curvature corner detection, and seam relocation.

### Tier 2: Static Analysis & Format Verification
*Execution Commands:*
```cmd
./gradlew lint
git diff --check
```

- **Scope**:
  - **Lint & Syntax**: Confirm zero Android lint errors or deprecation warnings.
  - **Whitespace & Line Endings**: Ensure clean Unix LF line endings without trailing whitespace across all `.kt` and `.md` files.

### Tier 3: GPU & Resource Optimization Benchmarking
*Tools:* Android Studio System Trace / Perfetto & Layout Inspector.

- **Layer Compositing Audit**:
  - Open Layout Inspector on API 30 and API 33 test runs.
  - Verify that resting components do NOT force `CompositingStrategy.Offscreen`.
  - Confirm `cyberBackdropBlur` does not capture background subtrees into offscreen intermediate layers.
- **Frame Budget & GPU Profiling**:
  - Profile `Modifier.cyberSpark`, `CyberWeld`, `cyberOverload`, `cyberScanlines`, and `cyberNoise` on target hardware (e.g., Pixel 7 / Vulkan pipeline).
  - Target: $\le 16.6\text{ ms}$ per frame during active animation loops.

### Tier 4: Visual & Preview Regression Sweep
*Scope:* Sample app screens (`:sample`) & Compose `@Preview` variants.

- **Theme & Scalability Matrix**:
  - Check Light Mode and Dark Mode rendering.
  - Verify 100% and 200% font scaling (system accessibility settings).
- **Physical Device Test Drive**:
  - Sweep through `SandboxScreen` and `FeedbackFormScreen` on physical test devices.
  - Record device ratings in `ratings.json` fixture to verify physical feel.
- **Screen Reader (TalkBack) Inspection**:
  - Enable TalkBack on test device/emulator.
  - Confirm interactive components (`CyberButton`, `CyberCard`, `cyberDraggable`) announce labels correctly when `appendedA11y` or `customA11y` parameters are provided.

---

## 📊 Summary Checklist

| Tier | Test Type | Target Environment | Automated / Manual | Pass Criteria |
|---|---|---|---|---|
| **Tier 1** | Unit & Robolectric | JVM / Robolectric (API 30/33) | Automated (`./gradlew test`) | 100% tests pass, 0 failures |
| **Tier 2** | Static Analysis | CLI | Automated (`git diff --check`) | Clean exit code 0 |
| **Tier 3** | GPU Profiling | Physical Device / Emulator | Manual (Perfetto / System Trace) | $\le 16.6\text{ ms}$ frame time |
| **Tier 4** | Visual & A11y | Physical Device (`:sample`) | Manual Inspection | Zero layout clipping, TalkBack announces properly |
