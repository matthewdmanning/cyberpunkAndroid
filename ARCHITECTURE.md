# Cyberpunk Android architecture

This document is the architectural map for the `:cyberpunkandroid` library. Use it to find the owning source area; use the source code for exact APIs.

## Module map

| Area | Source | Responsibility |
|---|---|---|
| Components | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/` | Minimal, functional Compose components and feedback fixtures. Visual effects are added with modifiers. |
| Effects | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/` | Public `Modifier.cyber...` effects, shader-backed rendering, Compose fallbacks, interaction triggers, and telemetry-driven state. |
| Icons | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/icons/` | Cyber icon vectors and the Outline, Solid, Duotone, and Overload variants. |
| Configuration | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/config/` | Primitive and semantic design tokens shared across themes and components. |
| Theme | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/theme/` | Colors, typography, shapes, and `CyberTheme` composition locals. |
| Drawing utilities | `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/utils/` | Reusable high-level Compose drawing helpers such as datastream gradients. |
| Android resources | `cyberpunkandroid/src/main/res/` | Drawables and bundled fonts. Neusharp is the display face, Fastup is the body face, and Monospace is used for terminal text. |
| Feedback assets | `cyberpunkandroid/src/main/assets/` | Design-of-experiments JSON consumed by the feedback fixtures. |
| Consumer rules | `cyberpunkandroid/consumer-rules.pro` and `cyberpunkandroid/src/main/keepRules/rules.keep` | Shrinker configuration shipped with or applied to the library. Keep these files aligned with resources that actually require preservation. |

## Dependency direction

The token path is:

`CyberPrimitives` → semantic tokens, colors, typography, and shapes → `CyberTheme` → components and effects

Components remain functional base elements. Effects and visual flair belong in reusable `Modifier.cyber...` extensions rather than component-specific copies. Public API signature rules live in [API conventions](docs/agents/api-conventions.md); rendering constraints live in [effects rules](docs/agents/effects-rules.md).

## Effects pipeline

1. `CyberBrushes` defines reusable gradients, including sweep gradients.
2. Shader-backed effects use AGSL where supported.
3. `CyberFallbacks` provides procedural high-level Compose drawing for older Android versions and tooling previews.
4. `CyberInteraction` controls when effects respond to interaction state.
5. `CyberTelemetry` converts external values into observable Compose state.
6. Public modifiers compose those pieces into reusable effects.

Path deformation effects are documented in [path effects](docs/PATH_EFFECTS.md).

## Feedback data ownership

- `docs/recorded_ratings.json` is ephemeral output from a connected device. It may be replaced after its data has been copied into the aggregate.
- `docs/master_ratings.json` is the durable, cumulative source of truth for completed ratings.
- The design-of-experiments inputs belong in `cyberpunkandroid/src/main/assets/`; ratings do not.

See the [feedback flow](docs/agents/feedback_flow.md) for the operational steps.
