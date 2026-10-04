# CyberpunkAndroid Reference

Reference docs for the visual layer of the `cyberpunkandroid` library: what each piece is, what it looks like, when to reach for it, and what every parameter does.

| Doc | Covers |
| --- | --- |
| [Modifiers & effects](modifiers-and-effects.md) | Every public `Modifier.cyber…` function and the effect composables (`GlowingText`, `CyberGlowIcon`, …) |
| [Shaders](shaders.md) | The AGSL (Android Graphics Shading Language) shaders behind the distortion effects, their fallbacks, and how to add one |
| [Icons](icons.md) | `CyberIcon`, its four render variants, the `CyberIcons` catalog, and the dial/rim decorations |

For copy-paste layouts, see [EFFECTS_EXAMPLES.md](../EFFECTS_EXAMPLES.md). For modifier **ordering** rules (which matter a lot for shader effects), see [effects-rules.md](../agents/effects-rules.md).

> **Visual descriptions are read from the source code**, not yet checked on a device. Items worth confirming by eye are listed under [To confirm on device](#to-confirm-on-device).

---

## Concepts shared by every effect

### 1. Triggers: when an effect is on

Most effect modifiers take the same pair of parameters:

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `trigger` | `CyberInteractionTrigger` | `ALWAYS` | When the effect is active. |
| `interactionSource` | `InteractionSource?` | `null` | The interaction stream to watch for `HOVER`, `PRESS` and `FOCUS`. |

`CyberInteractionTrigger` values:

| Value | Active when |
| --- | --- |
| `ALWAYS` | Always. No `interactionSource` needed. |
| `HOVER` | A pointer hovers the element (mouse, stylus, ChromeOS). |
| `PRESS` | The element is pressed. |
| `FOCUS` | The element has focus (keyboard / D-pad). |
| `NONE` | Never. Useful to switch an effect off without removing the modifier. |

`HOVER`, `PRESS` and `FOCUS` **require** an `interactionSource`; without one the effect stays off. Share the same source with the clickable:

```kotlin
val source = remember { MutableInteractionSource() }

Box(
    Modifier
        .clickable(interactionSource = source, indication = null) { /* … */ }
        .cyberOverload(trigger = CyberInteractionTrigger.PRESS, interactionSource = source)
        .background(CyberTheme.colors.surfaceSecondary)
)
```

When a trigger turns an effect on or off, its strength animates with `animationSpec` (and `exitAnimationSpec` where offered) instead of snapping.

### 2. Accessibility parameters

Every modifier and composable ends with the same two parameters:

| Parameter | Default | What it does |
| --- | --- | --- |
| `appendedA11y` | `null` | Extra context appended to the default label: `"CyberOverload - Reactor offline"`. |
| `customA11y` | `null` | Replaces the label entirely. |

Two behaviors, depending on what you're labeling:

- **Effects** (`cyberOverload`, `cyberScanlines`, …) always apply a label. Its default is the effect's name (e.g. `"CyberScanlines"`), with `mergeDescendants = true`, so screen readers announce that name: **set `customA11y` on anything a user can reach with TalkBack**.
- **Components and interaction modifiers** (`CyberButton`, `CyberCard`, `cyberDraggable`, `cyberLongPressFill`, …) apply a label **only when you pass one**. Without it, screen readers keep reading the component's own content (a button's text), not its type name.

Helpers for your own components: `Modifier.cyberSemantics(name, appendedA11y, customA11y)` always labels; `Modifier.cyberComponentSemantics(…)` labels only when a value is passed.

`CyberDragDropProvider` accepts both parameters but can't apply them yet: it adds no layout node to attach a label to (marked `TODO(a11y)` in code).

### 3. Rendering: shader vs. fallback

The distortion effects run an AGSL runtime shader on **Android 13+ (API 33)**. Older versions and some previews draw a simpler fallback with ordinary Compose drawing. Expect the fallback to look close but not identical.

| Effect | API 33+ | API 31–32 | API 24–30 |
| --- | --- | --- | --- |
| `cyberOverload`, `cyberScanlines`, `cyberNoise`, `cyberCrt`, `cyberSpark` | AGSL shader | Fallback | Fallback |
| `cyberTextGlow`, `cyberGlowBorder*`, `cyberBackdropBlur` | Blur | Blur | No blur (see each entry) |
| Everything else | Same everywhere | | |

Details per shader: [shaders.md](shaders.md).

### 4. Ordering

Shader effects process what is drawn **after** them in the modifier chain, so put the background after the effect: `Modifier.cyberScanlines().background(color)`. Put borders before the effect so they are not distorted. Motion effects (`cyberFloat`, `cyberPing`, …) belong on the icon or inner content, not the whole card. Full rules: [effects-rules.md](../agents/effects-rules.md).

---

## Doesn't match its description yet

The visual description is the spec. These effects don't draw what it describes yet; each carries a `TODO(visual)` in its KDoc with the open decision.

| Effect | Described as | Currently draws | Recommended direction |
| --- | --- | --- | --- |
| `cyberBackdropBlur` | Blurs and tints whatever is **behind** it (frosted glass over content) | Blurs foreground content over a tint | Style using translucent tint, gradient `Brush`, subtle border/shadow; avoid offscreen background capture layers |
| `cyberScanlines` | Scanlines **and subtle barrel curvature** | Scanlines only | Curvature strength; whether the fallback approximates it |
| `cyberPing` | Expanding ring **with a locked dense core** | Expanding ring only | Is the core a stationary ring or a filled shape? |
| `cyberBorder` | Static container border outline | Thin dashed or solid stroke in normal drawing path | Keep in normal drawing path (`Stroke`/`Brush`); use `cyberGlowBorder` or `dropShadow()` for active outer glow |
| `cyberCrt` | **Barrel distortion and vignette** | Shader: both, plus red/blue edge fringing and black off-screen corners. Fallback: vignette only | Keep or drop the fringing; whether the fallback approximates the distortion |

## Limitations

- **Motion effects' inactive spec is fixed.** `cyberPing` and `cyberIconPulse` switch to a built-in 100 ms loop while inactive; only the active spec is configurable.

## To confirm on device

These descriptions follow from the code, but the final look depends on content and screen density:

- `cyberHoloBackground` uses the opaque theme **secondary** (magenta) as its base, so it likely reads as mostly magenta with translucent cyan/magenta/green bands.
- `cyberNoise` at the default 3% opacity is very subtle on the shader path; the fallback's 200 dots at ~7.5% alpha may look sparser.
- `cyberScanlines` at the default 2% opacity may be barely visible without raising `opacity`.
- `cyberTextGlow` below API 31 draws a tinted copy with no blur, so the glow is effectively invisible there.
