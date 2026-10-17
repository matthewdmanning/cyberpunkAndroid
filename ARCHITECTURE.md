# Cyberpunk Android architecture

This document is the architectural map for the `:cyberpunkandroid` library. Use it to find the owning source area; use the source code for exact APIs.

## Module map

| Area | Source | Responsibility |
|---|---|---|
| Components | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/` | Minimal, functional Compose components and feedback fixtures. Visual effects are added with modifiers. |
| Effects | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/` | Public `Modifier.cyber...` effects, shader-backed rendering, Compose fallbacks, interaction triggers, and telemetry-driven state. |
| Icons | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/icons/` | Cyber icon vectors and the Outline, Solid, Duotone, and Overload variants. |
| Configuration | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/config/` | Primitive and semantic design tokens shared across themes and components. |
| Theme | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/theme/` | Subsystems (colors, typography, shapes, elevation, spacing, semantics, status), `@ReadOnlyComposable` `CyberTheme` accessors, and Material 3 extensions. |
| Drawing utilities | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/utils/` | Reusable high-level Compose drawing helpers such as datastream gradients. |
| Android resources | `cyberpunkandroid/src/main/res/` | Drawables and bundled fonts. Oare Sans is the display face, Fastup is the body face, and Monospace is used for terminal text. |
| Feedback assets | `cyberpunkandroid/src/main/assets/` | Design-of-experiments JSON consumed by the feedback fixtures. |
| Consumer rules | `cyberpunkandroid/consumer-rules.pro` and `cyberpunkandroid/src/main/keepRules/rules.keep` | Shrinker configuration shipped with or applied to the library. Keep these files aligned with resources that actually require preservation. |

## Dependency direction

### 1. `effects/CyberShaders.kt` (The GPU Math)
This file holds raw AGSL (Android Graphics Shading Language) code as string constants (like `CrtShader`). Compiling them and applying them as a `RenderEffect` (with a Compose fallback below API 33) is handled by the individual `Modifier.Node` implementations (e.g., `CyberOverloadNode`); see [docs/reference/shaders.md](docs/reference/shaders.md).
- **Purpose:** It handles complex pixel-by-pixel manipulations on the GPU (like barrel distortion, chromatic aberration, or overload glitches) that are too expensive or impossible to do with standard Canvas drawing.
- **Relationship:** Like `CyberBrushes`, it is a low-level graphics tool, but for distortion rather than painting.

The token path is:

`CyberPrimitives` → semantic tokens, colors, typography, shapes, elevation, and spacing → `CyberTheme` → components, effects, and Material 3 extensions

Components remain functional base elements. Effects and visual flair belong in reusable `Modifier.cyber...` extensions rather than component-specific copies. Public API signature rules live in [API conventions](docs/agents/api-conventions.md); rendering constraints live in [effects rules](docs/agents/effects-rules.md); design token reference lives in [theme reference](docs/reference/theme.md).

## Theme and design system architecture

`CyberTheme` follows the official Android Jetpack Compose [Theme Anatomy](https://developer.android.com/develop/ui/compose/designsystems/anatomy) best practices:

1. **Subsystems (`@Immutable` data classes):** Seven distinct visual and behavioral subsystems:
   - `colors`: `CyberColors` (primary, secondary, void surfaces, text, border).
   - `typography`: `CyberTypography` (display, terminal, body text styles).
   - `shapes`: `CyberShapes` (chamfered cut-corner polygons and rounded scales).
   - `elevation`: `CyberElevation` (levels `level0` to `level5`).
   - `spacing`: `CyberSpacing` (standard spatial increments `dp2` to `dp64`).
   - `semantics`: `CyberSemanticTokens` (status colors and pulse durations).
   - `status`: `CyberStatusColors` (roles with `color`, `onColor`, `container`, `onContainer`).
2. **Theme object & performance:** Every getter on `object CyberTheme` is annotated with `@Composable @ReadOnlyComposable` to eliminate slot-table restart groups and avoid recomposition tracking overhead at token read sites.
3. **Material 3 interoperability:**
   - `CyberTheme(...)` automatically maps tokens to Material 3's `ColorScheme`, `Typography`, and `Shapes` for seamless styling of stock Material components.
   - Ambient propagation: provides `LocalContentColor provides colors.textPrimary` and `ProvideTextStyle(typography.body)`.
   - Non-intrusive embedding: `wrapMaterialTheme = false` allows scoping Cyber tokens to subtrees without overriding host `MaterialTheme`.
   - Discovery extensions: `MaterialTheme.cyberColors`, `MaterialTheme.cyberShapes`, `MaterialTheme.cyberElevation`, `MaterialTheme.cyberSpacing`.


## Effects pipeline

1. `CyberBrushes` defines reusable gradients, including sweep gradients.
2. Shader-backed effects use AGSL where supported.
3. `CyberFallbacks` provides procedural high-level Compose drawing for older Android versions and tooling previews.
4. `CyberInteraction` controls when effects respond to interaction state.
5. `CyberTelemetry` converts external values into observable Compose state.
6. Public modifiers compose those pieces into reusable effects.

Path deformation effects are documented in [path effects](docs/PATH_EFFECTS.md). Radial effects (sweep, pulse, icon illumination, particle shower) are documented in [radial effects](docs/RADIAL_EFFECTS.md).

## Feedback data ownership

- `docs/recorded_ratings.json` is ephemeral output from a connected device. It may be replaced after its data has been copied into the aggregate.
- `docs/master_ratings.json` is the durable, cumulative source of truth for completed ratings.
- The design-of-experiments inputs belong in `cyberpunkandroid/src/main/assets/`; ratings do not.

See the [feedback flow](docs/agents/feedback_flow.md) for the operational steps.
