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
   Compose modifiers are ordered and immutable. Order changes rendering and touch target behavior. Apply modifiers in this sequence:
   1. Sizing and layout (`fillMaxWidth`, `height`, `padding`)
   2. Outer visual effects and emissive glows (`cyberTextGlow`, `cyberGlowBorder`)
   3. Bounds clipping (`clip`)
   4. Background and surface fills (`background`, `cyberHoloBackground`)
   5. Borders and contour tracers (`cyberBorder`, `cyberGlowBorder`, `cyberLaserOutliner`)
   6. Touch and interaction handlers (`clickable`, `combinedClickable`)
   7. Content padding and inner children layout (`padding`)

5. **Use semantic icon aliases and variants intentionally.**
   Select icons by semantic purpose (e.g., `CyberIcons.Warning` for caution, `CyberIcons.Check` for success). Use `CyberIconVariant.Solid` only for active selection and `CyberIconVariant.Overload` for error/hacked accents.

6. **Start from an established pattern.**
   When building a card, button, badge, or HUD panel, use the pattern from `docs/cookbook/patterns/` as the baseline.

7. **Do not combine heavy decorative effects.**
   Do not stack multiple GPU shaders or multi-pass glows on the same component unless the recipe explicitly specifies it. Pair at most one distortion shader (`cyberCrt`, `cyberOverload`, or `cyberScanlines`) with a surface.

8. **Preserve accessibility semantics for interactive controls.**
   Every interactive surface and standalone icon must provide a descriptive `contentDescription` or explicit `Modifier.cyberSemantics`. Purely decorative icons must pass `contentDescription = null`.
