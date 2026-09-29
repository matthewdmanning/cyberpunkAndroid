# CyberpunkAndroid Module Reference Guide

This reference guide details the file locations, architectural roles, and non-obvious implementation details for essential and specialized sections of the `:cyberpunkandroid` library module.

---

## Essential Sections

### 1. Components (`src/main/java/.../components/`)
Contains functional UI base classes including buttons, cards, modals, terminal windows, decrypters, and biometric monitors.
- Base components act as unadorned structural containers with minimal built-in stylistic bloat, designed to be decorated via `Modifier.cyber...` effects.
- Includes specialized testing fixtures like `CyberFeedbackFixture`, which renders adaptive parameter grading grids that record physical device ratings directly to local device storage JSON files.

### 2. Effects & Modifiers (`src/main/java/.../effects/`)
Contains hardware AGSL shaders (`CyberShaders.kt`), Canvas fallback rasterizers (`CyberFallbacks.kt`), and composable modifiers (`CyberModifiers.kt`, `GlowModifiers.kt`, `CyberSpark.kt`, `GlowingText.kt`).
- Macro-shaders (`cyberScanlines`, `cyberOverload`, `cyberNoise`, `cyberCrt`) require the background fill to be chained *after* the shader modifier (`Modifier.cyberNoise().background(...)`) so the shader samples rendered pixels instead of transparent voids.
- Automatically enforces `graphicsLayer { clip = true }` to prevent AGSL canvas output from leaking outside composable card boundaries, falling back to graphics-layer canvas rasterizers on API < 33.

### 3. Icons & HUD Graphics (`src/main/java/.../icons/`)
Houses vector iconography (`CyberIcon.kt`), icon registry constants (`CyberIcons.kt`), and specialized HUD graphics (`CyberSectorRim.kt`, `CyberDialTicks.kt`).
- `CyberIcon` supports four visual rendering variants (`Outline`, `Solid`, `Duotone`, `Overload`). `Overload` renders offset cyan and magenta ghost layers using `graphicsLayer` translations, while `Solid` dynamically strips vector strokes into solid white fills.
- HUD components (`CyberSectorRim`, `CyberDialTicks`) draw procedural angular arc gaps and radial tick geometry directly in `DrawScope` without vector resource overhead.

### 4. Themes & Design Tokens (`src/main/java/.../theme/`, `src/main/java/.../config/`)
Defines the theme provider (`CyberTheme.kt`), shape tokens (`CyberShapes.kt`), primitives (`CyberPrimitives.kt`), and semantic tokens (`CyberSemanticTokens.kt`).
- Implements a strict 4-tier token hierarchy (`CyberPrimitives` $\to$ `CyberSemanticTokens` -> `CyberTheme` $\to$ Composable) that enforces zero magic numbers throughout the library.
- `CyberShapes` defaults to asymmetric chamfered cut-corner shapes (`CutCornerShape`) rather than standard Material rounded corners.

### 5. Fonts & Typography (`src/main/res/font/`, `src/main/java/.../theme/CyberTypography.kt`)
Stores custom font assets (`neusharp_bold.otf`, `fastup_bold.ttf`, `fastup_regular.ttf`) and configures type tokens in `CyberTypography.kt`.
- `NeusharpFontFamily` maps the custom angular `neusharp_bold.otf` font asset to `CyberTypography.display` for display headers and title banners.
- `FastupFontFamily` maps `fastup_regular.ttf` and `fastup_bold.ttf` to `CyberTypography.body` for body and regular copy.
- Mandates strict lowercase underscore resource naming (`neusharp_bold.otf`, `fastup_bold.ttf`, `fastup_regular.ttf`) in `src/main/res/font/` for Android R-class compilation compliance, while terminal text relies on system `FontFamily.Monospace`.

### 6. Drawing Utilities (`src/main/java/.../utils/`)
Houses custom brush generators (`CyberBrushes.kt`), drawing extensions (`CyberDrawUtils.kt`), and data mappers (`CyberDataMapper.kt`).
- Provides `drawDatastreamGradient` for multi-pass animated flow sweeps and `cyberSweepGradient` for phase-shifted rotational brushes without allocating new Shader objects on every frame.
- Contains mathematical mapping functions that translate continuous numeric telemetry into color stops and geometric stroke paths.

---

## Specialized Sections

### 7. Assets & Design of Experiments (`src/main/assets/`)
Stores JSON matrix configuration files (`test_doe_blur.json`, `test_doe_bounce.json`, `test_doe_datastream.json`, etc.) in `src/main/assets/`.
- Stores Design of Experiments (DoE) parameter matrices used by `CyberFeedbackFixture` and automated test harnesses.
- Enables physical device testing and parameter evaluation across different hardware screen density profiles without rebuilding the APK.

### 8. Resource Protection & Keep Rules (`src/main/keepRules/rules.keep`, `consumer-rules.pro`)
Contains ProGuard and R8 resource shrinking keep rules (`rules.keep`, `consumer-rules.pro`).
- Explicitly preserves dynamically reflective vector drawable assets (`R.drawable.cyber_ic_*`) and font resources from being stripped during R8 resource shrinking.
- Protects icon resource IDs accessed programmatically via `CyberIcons` integer fields or asset JSON test fixtures.

### 9. Interaction & Telemetry Engines (`src/main/java/.../effects/CyberInteraction.kt`, `effects/CyberTelemetry.kt`)
Contains state trigger resolvers (`CyberInteractionTrigger`) and state-driven telemetry engines (`rememberDrivenState`, `rememberDrivenFloatState`).
- `CyberInteractionTrigger.isActive` resolves hover, press, and focus states from a composable's `InteractionSource` to toggle visual effects dynamically.
- `rememberDrivenFloatState` processes continuous 60Hz telemetry updates using unboxed float state (`mutableFloatStateOf`) to eliminate Java autoboxing overhead during animation loops.
