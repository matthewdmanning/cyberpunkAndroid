# Agent Invariants and UI Rules

This document defines mandatory rules for AI agents implementing user interfaces with the `:cyberpunkandroid` library. Follow these rules without exception.

---

## The 8 Rules

1. **Prefer library APIs over custom canvas or effect code.**
   Never reimplement glows, borders, scanlines, or animations with raw Compose drawing if a `Modifier.cyber...` or component already exists.

2. **Select semantic tokens by meaning, never by apparent color.**
   Select tokens based on their UI role (e.g., `CyberTheme.semantics.colors.danger`), not visual color values (e.g., "magenta").

3. **Never hardcode values represented by tokens.**
   Never write raw `Color(0x...)`, hardcoded `dp` corner cut values, or literal animation durations. Reference `CyberTheme`, `CyberPrimitives`, or `CyberConfig`.

4. **Preserve documented modifier ordering.**
   Compose modifiers are ordered and immutable. Order changes rendering, touch target behavior, and semantic structure. Apply modifiers in this sequence:
   1. Sizing and layout (`fillMaxWidth`, `height`, `size`)
   2. Semantic grouping and container overrides (`semantics(mergeDescendants = true)`)
   3. Outer visual effects and emissive glows (`cyberTextGlow`, `cyberGlowBorder`)
   4. Bounds clipping (`clip`)
   5. Background and surface fills (`background`, `cyberHoloBackground`)
   6. Borders and contour tracers (`cyberBorder`, `cyberGlowBorder`, `cyberLaserOutliner`)
   7. Touch and interaction handlers with native roles (`clickable(role = Role.Button)`, `toggleable`, `selectable`)
   8. Content padding and inner children layout (`padding`)

5. **Use semantic icon aliases and variants intentionally.**
   Select icons by semantic purpose (e.g., `CyberIcons.Warning` for caution, `CyberIcons.Check` for success). Use `CyberIconVariant.Solid` only for active selection and `CyberIconVariant.Overload` for error/hacked accents.

6. **Start from an established pattern.**
   When building a card, button, badge, or HUD panel, use the pattern from `docs/cookbook/patterns/` as the baseline.

7. **Do not combine heavy decorative effects.**
   Do not stack multiple GPU shaders or multi-pass glows on the same component unless the recipe explicitly specifies it. Pair at most one distortion shader (`cyberCrt`, `cyberOverload`, or `cyberScanlines`) with a surface.

8. **Preserve accessibility semantics and zero code-names in the semantic tree.**
   Allow natural visible text to surface as the primary announcement. Never inject synthetic class names or library prefixes into `contentDescription`. Assign native `Role.*` values to interactive controls, use dynamic `stateDescription` (never string concatenation) for state transitions, supply action-oriented `onClickLabel` hints, and suppress inner decorative chevrons/icons via `Modifier.clearAndSetSemantics { }`. Purely decorative standalone icons must pass `contentDescription = null`.
