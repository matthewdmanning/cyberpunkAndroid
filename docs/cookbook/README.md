# Cyberpunk Android UI Cookbook for AI Agents

Welcome to the AI-agent UI cookbook for `:cyberpunkandroid`. 

This cookbook provides **normative guidance, design grammar, and copyable implementation patterns**. It teaches **what to use, when to use it, how to compose it correctly, and what to avoid**.

For exact API signatures and parameter definitions, consult the [API Reference](../reference/README.md).

---

## 1. Quick Decision Matrix

Find your design intent in the table below to jump directly to the correct recipe:

| Design Intent | Recommended Modifier / Component | Recipe Document |
| :--- | :--- | :--- |
| **Make a tactile interactive card** | `Modifier.cyberOverload` (on PRESS) + `clip` + `cyberBorder` | [patterns/interactive-card.md](patterns/interactive-card.md) |
| **Primary call-to-action button** | `CyberButton` + `cyberGlowBorder` | [patterns/primary-action.md](patterns/primary-action.md) |
| **Render a cut-corner container** | `Modifier.clip(cardShape)` + `cyberBorder` | [recipes/ui-recipes.md](recipes/ui-recipes.md) |
| **Indicate active process or state** | `Modifier.cyberPing` beacon / `cyberOverload` | [recipes/ui-recipes.md](recipes/ui-recipes.md) |
| **Holographic rotating surface** | `Modifier.cyberHoloBackground` | [recipes/ui-recipes.md](recipes/ui-recipes.md) |
| **CRT monitor screen simulation** | `Modifier.cyberCrt` | [recipes/ui-recipes.md](recipes/ui-recipes.md) |

---

## 2. Cookbook Structure

### Core Invariants
- **[00-agent-rules.md](00-agent-rules.md)**: 8 mandatory agent rules for UI construction.
- **[01-token-selection.md](01-token-selection.md)**: Intent-to-token ontology and substitution rules.
- **[02-modifier-ordering.md](02-modifier-ordering.md)**: Seven-step modifier chain order and order constraints.
- **[03-icons.md](03-icons.md)**: Semantic icon selection, variants, RTL rules, and accessibility.

### Implementation Recipes
- **[recipes/ui-recipes.md](recipes/ui-recipes.md)**: All core recipes for setting up backgrounds, framing objects, and adding reactive state feedback.

### Canonical Patterns
- **[patterns/interactive-card.md](patterns/interactive-card.md)**: Grouped controls, item slots, and metrics cards.
- **[patterns/primary-action.md](patterns/primary-action.md)**: Primary cut-corner CTA button with glow.
- **[patterns/status-indicator.md](patterns/status-indicator.md)**: Coordinated status tiles with iconography.
- **[patterns/hud-panel.md](patterns/hud-panel.md)**: Scanned diagnostic telemetry window.

### Anti-Patterns
- **[anti-patterns/raw-values.md](anti-patterns/raw-values.md)**: Hardcoding colors and dimensions.
- **[anti-patterns/modifier-order.md](anti-patterns/modifier-order.md)**: Glow clipping and shrunk touch targets.
- **[anti-patterns/effect-overuse.md](anti-patterns/effect-overuse.md)**: Stacked shaders and frame drop causes.
- **[anti-patterns/icon-misuse.md](anti-patterns/icon-misuse.md)**: Broken `Solid` variants and visual guessing.
