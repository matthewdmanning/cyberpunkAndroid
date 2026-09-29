# Modifiers & Effects

Every public visual effect in `cyberpunkandroid`: what it is, what it looks like, when to use it, and what each parameter does.

Shared parameters (`trigger`, `interactionSource`, `appendedA11y`, `customA11y`) behave the same everywhere and are explained once in the [reference index](README.md#concepts-shared-by-every-effect). They're left out of the tables below unless an effect treats them differently.

**Jump to:** [Distortion shaders](#distortion-shaders) · [Glow](#glow) · [Motion](#motion) · [Surfaces & patterns](#surfaces--patterns) · [Interaction](#interaction) · [Utilities](#utilities) · [Choosing an effect](#choosing-an-effect)

---

## Distortion shaders

These run an AGSL shader over the content on Android 13+ and a Compose-drawn fallback below that ([details](shaders.md)). They distort what's drawn **after** them in the chain, so put `.background(…)` after the effect. Each clips to its bounds.

### `Modifier.cyberOverload`

**What:** A glitch/"system overload" effect that splits the red, green and blue channels apart and tears the content sideways.

**Looks like:** Red and blue ghost copies offset left and right of the content (±4% of the width × `intensity`). Every horizontal row also jitters by a random amount that changes each frame, which reads as horizontal tearing. At the default intensity (0.1) this is a slight fringe; at 1.0 it's a violent glitch. The fallback draws red- and blue-tinted copies offset ±2% × `intensity`, and on some frames displaces a random horizontal slice.

**Use for:** Error or alarm states, a press reaction on a critical button (`trigger = PRESS`), or a brief "reactor failing" moment. **Avoid** leaving it on at high intensity on text people need to read.

```kotlin
Modifier
    .cyberOverload(intensity = 0.6f, trigger = CyberInteractionTrigger.PRESS, interactionSource = source)
    .background(CyberTheme.colors.surfaceSecondary)
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `enabled` | `Boolean` | `true` | `false` removes the effect entirely (no layer, no clip). |
| `intensity` | `Float` | `0.1` (`CyberConfig.Shaders.OverloadCoefficient`) | Size of the channel split and jitter. ~0.1 subtle, ~0.5 strong, 1.0 extreme. |
| `timeScale` | `Float` | `1` | Speed of the jitter animation. |
| `bounceAmount` | `Dp` | `0.dp` | If > 0, the content also hops upward by up to this much, rapidly (API 33+ only). |
| `animationSpec` | `AnimationSpec<Float>` | `tween(300)` | How intensity ramps up when the trigger activates. |
| `exitAnimationSpec` | `AnimationSpec<Float>` | `tween(300)` | How intensity ramps down when the trigger deactivates. |

### `Modifier.cyberScanlines`

**What:** Moving horizontal scanlines, like an old CRT monitor or a security feed.

**Looks like:** Soft horizontal bands, one per `spacing`, drifting slowly downward. With the default transparent `color` the bands darken the content (up to 70% × `opacity`); with a color they tint it toward that color. The default opacity (2%) is very subtle; 0.2–0.5 is clearly visible.

**Use for:** Terminal panels, video/feed placeholders, "monitor" cards. Frame the panel with a border drawn *before* the effect so the frame stays crisp.

```kotlin
Modifier
    .border(2.dp, CyberTheme.colors.primary, CutCornerShape(12.dp))
    .cyberScanlines(spacing = 6.dp, opacity = 0.4f)
    .background(CyberTheme.colors.surfaceSecondary, CutCornerShape(12.dp))
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `spacing` | `Dp` | `4.dp` | Distance between line centers. Larger = fewer, thicker-looking lines. |
| `opacity` | `Float` | `0.02` (`CyberConfig.Shaders.ScanlineOpacity`) | Line strength, 0–1. |
| `speed` | `Float` | `1` | Scroll speed multiplier (shader ≈ 30 px/s at 1; fallback ≈ 20 px/s). |
| `color` | `Color` | `Transparent` | `Transparent` darkens bands; any other color tints bands toward it. **Fallback:** transparent cuts see-through bands instead of darkening. |
| `animationSpec` | `AnimationSpec<Float>` | `tween(300)` | Fade in *and* out when the trigger changes. |

### `Modifier.cyberNoise`

**What:** Film-grain / static noise over the content.

**Looks like:** Per-pixel random brightening and darkening (± `opacity`) that re-rolls every frame, so it shimmers like TV static. With `animated = false` the grain is frozen. The fallback scatters 200 small black and white dots per frame instead of per-pixel grain.

**Use for:** Adding texture to large flat surfaces (backgrounds, hero cards) so they feel less digital-clean. Keep opacity low (0.02–0.08).

```kotlin
Modifier.cyberNoise(opacity = 0.05f).background(CyberTheme.colors.background)
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `enabled` | `Boolean` | `true` | `false` removes the effect entirely. |
| `opacity` | `Float` | `0.03` (`CyberConfig.Shaders.NoiseOpacity`) | Grain strength. |
| `speed` | `Float` | `1` | How fast the grain pattern changes. |
| `animated` | `Boolean` | `true` | `false` freezes the grain (and stops per-frame updates). |
| `animationSpec` | `AnimationSpec<Float>` | `tween(300)` | Fade in/out when the trigger changes. |

### `Modifier.cyberCrt`

**What:** Makes the content look like it's on a curved CRT (cathode-ray tube) screen.

**Looks like:** The content bulges outward (barrel distortion), with red/blue color fringing that grows toward the edges and a dark vignette around the border. Corners pushed outside the screen area turn black. The fallback draws only the vignette (transparent center fading to 55% black at the edges).

**Use for:** A retro monitor frame around a whole panel or screen. **Avoid** on small elements; the distortion needs room.

```kotlin
Modifier.cyberCrt().background(CyberTheme.colors.surfacePrimary)
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `enabled` | `Boolean` | `true` | `false` removes the effect entirely. |
| `trigger` / `interactionSource` | | `ALWAYS` / `null` | On/off only: switches instantly, no fade. |

Unlike the other shader effects, it clips to its bounds only while active.

### `Modifier.cyberSpark` and `CyberSpark`

**What:** An electrical-spark burst: glowing embers shoot up from the center, arc over and fall under gravity.

**Looks like:** Up to 32 pinpoint embers launching upward from the center, curving back down in parabolic arcs, and fading out. Each ember changes color over its life: white-hot → warning color (yellow) → primary (cyan) → secondary (magenta) → dim warning, and it shrinks to half size. Bursts restart at staggered times, so it pops continuously like popcorn. The fallback draws small (3–6 px) colored dots on similar arcs.

**Use for:** Short-circuit / damage moments, a "powering up" accent behind an icon, or celebratory energy. The container clips sparks at its edges, so give it room.

```kotlin
CyberSpark(Modifier.size(160.dp), intensity = 1.2f) {
    CyberIcon(CyberIcons.Zap, contentDescription = "Power surge", size = 48.dp)
}
```

`CyberSpark` is a `Box` with `Modifier.cyberSpark` applied and its content centered. It has the same parameters (always on, no trigger).

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `modifier` | `Modifier` | `Modifier` | (`CyberSpark` only) Size/layout of the spark container. |
| `color` | `Color` | theme `primary` | Mid-life ember color. |
| `secondaryColor` | `Color` | theme `secondary` | Late-life ember color. |
| `warningColor` | `Color` | semantic `warning` | Birth flash color and final dim color. |
| `sparkCount` | `Int` | `32` | Number of embers. **Fallback only**; the shader always draws 32. |
| `intensity` | `Float` | `1` | Brightness and ember size. |
| `speed` | `Float` | `1` | How fast bursts cycle. |
| `animationSpec` | `AnimationSpec<Float>` | `tween(300)` | Fade in/out when the trigger changes (modifier only). |
| `content` | `@Composable () -> Unit` | `null` | (`CyberSpark` only) Drawn centered, under the sparks. |

---

## Glow

Neon glow around text, icons and borders. The blur needs Android 12+ (API 31); below that, glows have no soft spread (see each entry).

### `Modifier.cyberTextGlow`

**What:** A glow that follows the exact outline of whatever it's applied to: the letter shapes of text, the strokes of an icon.

**Looks like:** A blurred, color-tinted copy of the content drawn behind it, so each glyph or stroke gets a soft halo in `color`. Below API 31 there's no blur, so the tinted copy sits directly under the content and the glow is effectively invisible.

**Use for:** Headings, key numbers and icons that should read as emissive neon. For big display text, [`GlowingText`](#glowingtext) gives a richer multi-layer bloom.

```kotlin
Text("ONLINE", style = CyberTheme.typography.terminal, modifier = Modifier.cyberTextGlow(color = CyberTheme.colors.primary))
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `color` | `Color` | `Color.Cyan` | Glow color. |
| `radius` | `Dp` | `8.dp` | Blur spread. `0.dp` disables the glow. |
| `intensity` | `Float` | `1` | Glow opacity is `0.85 × intensity` (capped at 1). Whole numbers above 1 also stack extra glow passes (2.0 = two passes). `0` disables. |
| `outsideGlowOnly` | `Boolean` | `false` | **Not implemented**: currently has no effect. |

### `Modifier.cyberGlowBorder`

**What:** A neon tube border around a shape.

**Looks like:** Three layers following the shape's outline: a wide, 35%-opacity soft stroke; a blurred double-width stroke for the halo; and a sharp stroke of width `width` on top. Below API 31 the halo layer isn't blurred, so the border looks thicker and harder rather than glowing.

**Use for:** Framing cards, buttons and focused elements. Match `shape` to the element's background/clip shape.

```kotlin
Modifier
    .background(CyberTheme.colors.surfaceSecondary, CutCornerShape(12.dp))
    .cyberGlowBorder(color = CyberTheme.colors.primary, shape = CutCornerShape(12.dp))
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `color` | `Color` | `Color.Cyan` | Border and glow color. |
| `shape` | `Shape` | `CutCornerShape(12.dp)` | Outline to trace. |
| `glowRadius` | `Dp` | `8.dp` | Halo spread. `0.dp` draws only the sharp stroke. |
| `width` | `Dp` | `2.dp` | Sharp stroke width. |

### `Modifier.cyberGlowBorderRounded`

Shortcut for `cyberGlowBorder(shape = RoundedCornerShape(cornerRadius))` with a larger default glow.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `color` | `Color` | `Color.Cyan` | Border and glow color. |
| `cornerRadius` | `Dp` | `16.dp` | Corner rounding. |
| `glowRadius` | `Dp` | `12.dp` | Halo spread. |
| `width` | `Dp` | `2.dp` | Sharp stroke width. |

### `Modifier.cyberGlowBorderFlow`

**What:** An animated version of `cyberGlowBorder` where the colors flow around the outline.

**Looks like:** The same three-layer neon border, painted with a sweep gradient of `colors` that rotates around the shape's center once every 2 s ÷ `speed`.

**Use for:** Drawing attention to one element: the selected card, an active process, a call to action. One per screen is usually enough.

```kotlin
Modifier.cyberGlowBorderFlow(colors = listOf(CyberTheme.colors.primary, CyberTheme.colors.secondary))
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `colors` | `List<Color>` | `[Cyan, Magenta]` | Gradient colors. The first is repeated at the end for a seamless loop. Empty falls back to the default; one color draws a solid border. |
| `shape` | `Shape` | `CutCornerShape(12.dp)` | Outline to trace. |
| `glowRadius` | `Dp` | `8.dp` | Halo spread. |
| `width` | `Dp` | `2.dp` | Sharp stroke width. |
| `speed` | `Float` | `1` | Rotation speed; one turn per 2000 ms ÷ `speed` (minimum speed 0.1). |

`cyberNeonBorder` and `cyberNeonBorderFlow` are **deprecated** aliases of `cyberGlowBorder` and `cyberGlowBorderFlow`.

### `GlowingText`

**What:** A text composable with a strong, layered neon bloom, for big display text.

**Looks like:** Bold display-style text in `textColor` with three concentric glows in `glowColor`: a far, faint bloom (2× `glowRadius`), a mid glow (1×) and a tight inner glow (0.4×). It glows on every API level because it uses text shadows, not blur.

**Use for:** Titles, splash screens, big status words ("ACCESS GRANTED"). The composable adds 1.5 × `glowRadius` padding so the bloom isn't cut off; account for that in layouts.

```kotlin
GlowingText("NEON CITY", glowColor = CyberTheme.colors.secondary, fontSize = 36.sp)
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `text` | `String` | required | Text to display. |
| `modifier` | `Modifier` | `Modifier` | Applied to the outer padded box. |
| `glowRadius` | `Dp` | `16.dp` | Bloom size; also sets the padding (1.5×). |
| `glowColor` | `Color` | `Color.Cyan` | Bloom color. |
| `textColor` | `Color` | `Color.White` | Crisp foreground text color. |
| `fontSize` | `TextUnit` | `44.sp` | Text size (display font, bold). |

### `CyberGlowIcon`

**What:** An icon with a neon glow that follows its strokes.

**Looks like:** The icon tinted `color` with a soft `glowColor` halo along its shape (via `cyberTextGlow`). It's padded by `radius` so the halo fits.

**Use for:** Status icons and nav icons that should look lit. For outline icons where the glow should stay *outside* closed shapes, use `CyberGlowIconPath`.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `painter` | `Painter` | required | Icon image, e.g. `painterResource(CyberIcons.Shield)`. |
| `contentDescription` | `String?` | required | Screen reader label; `null` if decorative. |
| `modifier` | `Modifier` | `Modifier` | Size/layout. |
| `color` | `Color` | `Color.Cyan` | Icon tint. |
| `glowColor` | `Color` | `color` | Halo color. |
| `radius` | `Dp` | `12.dp` | Halo spread and padding. |
| `intensity` | `Float` | `1.5` | Halo opacity/stacking; see `cyberTextGlow`. |

### `CyberGlowIconPath`

**What:** An outline icon with a bloom that stays along its strokes and leaves the inside of closed shapes dark.

**Looks like:** Three stacked copies of the icon: a faint (25%) outer bloom inset by `outerPadding`, a 55% mid glow inset by `innerPadding`, and the crisp icon on top.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `painter`, `contentDescription`, `modifier`, `color`, `glowColor` | | | As `CyberGlowIcon`. |
| `outerPadding` | `Dp` | `6.dp` | Inset of the far bloom layer. |
| `innerPadding` | `Dp` | `3.dp` | Inset of the mid glow layer. |
| `radius` | `Dp` | `16.dp` | Far bloom spread (mid layer uses half). |
| `intensity` | `Float` | `2` | Bloom strength. |

### `CyberGlowContainer`

**What:** A `Box` that applies `cyberTextGlow` to everything inside it.

**Use for:** Glowing a group (an icon plus label) with one glow pass, instead of stacking a glow modifier on each child.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `modifier` | `Modifier` | `Modifier` | Applied to the box. |
| `color` | `Color` | `Color.Cyan` | Glow color. |
| `radius` | `Dp` | `8.dp` | Glow spread. |
| `intensity` | `Float` | `1` | Glow strength. |
| `contentAlignment` | `Alignment` | `Center` | Child alignment. |
| `content` | `BoxScope.() -> Unit` | required | Children. |

---

## Motion

Small looping animations. Apply them to the **icon or inner content**, not the whole card, or the whole card will move.

### `Modifier.cyberIconSpin`

**What:** Continuous rotation, for loading or "processing".

**Looks like:** Spins clockwise one full turn every 1.2 s. When it deactivates, it rotates back to 0° over 300 ms. With `bounceAmount`, it also lifts up and down twice per turn.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `bounceAmount` | `Dp` | `0.dp` | Vertical hop height synced to the rotation. |
| `animationSpec` | `AnimationSpec<Float>` | infinite 1200 ms linear | One turn's timing. |
| `resetAnimationSpec` | `AnimationSpec<Float>` | `tween(300)` | Return to 0° when deactivated. |

### `Modifier.cyberPing`

**What:** A radar/sonar ping: rings expand outward from the element and fade.

**Looks like:** A ring in `shape`'s outline starts at the element's size, grows to `maxDiffuseScale` × and fades to transparent, repeating every `durationMillis`. The ring keeps the same stroke width while it grows. Nothing is drawn while inactive.

**Use for:** Live indicators (recording, online, incoming signal), map markers, "look here" hints.

```kotlin
CyberIcon(CyberIcons.Signal, contentDescription = "Live", modifier = Modifier.cyberPing(color = CyberTheme.semantics.colors.success))
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `color` | `Color` | cyan (`Cyan500`) | Ring color. |
| `durationMillis` | `Int` | `1200` | Time for one ping. |
| `startScale` | `Float` | `1` | Ring size at the start, relative to the element. |
| `maxDiffuseScale` | `Float` | `2.5` (`CyberConfig.Effects.PingScale`) | Ring size at the end. |
| `alphaDecayExponent` | `Float` | `1` | Fade curve. 1 = linear; > 1 fades faster early; < 1 lingers. |
| `shape` | `Shape` | `CircleShape` | Ring outline. |
| `borderWidth` | `Dp` | `2.dp` | Ring stroke width. |
| `animationSpec` | `AnimationSpec<Float>` | infinite linear restart | **Must be `infiniteRepeatable(…)`**, or it crashes. |

### `Modifier.cyberIconPulse`

**What:** Opacity pulse without changing size.

**Looks like:** Fades between `maxOpacity` and `minOpacity` and back, 600 ms each way.

**Use for:** Attention states: unread, warning, waiting for input.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `durationMillis` | `Int` | `600` | One fade direction. |
| `minOpacity` | `Float` | `0.2` (`CyberConfig.Effects.PulseMinOpacity`) | Dimmest point. |
| `maxOpacity` | `Float` | `1` | Brightest point. |
| `animationSpec` | `AnimationSpec<Float>` | infinite linear reverse | Pulse timing. |

### `Modifier.cyberFloat`

**What:** Gentle hovering, like a hologram or a drone.

**Looks like:** Drifts up by `height` and back down with smooth easing, one full cycle per `durationMillis`. When deactivated, it springs back to rest.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `height` | `Dp` | `12.dp` | How high it floats. |
| `durationMillis` | `Int` | `3000` | Full up-and-down cycle. |
| `animationSpec` | `AnimationSpec<Float>` | infinite ease-in-out reverse | Float timing. |
| `exitAnimationSpec` | `AnimationSpec<Float>` | `spring()` | Settle back when deactivated. |

### `Modifier.cyberBounce`

**What:** A springy vertical bounce.

**Looks like:** Rises by `height` quickly, easing out at the top, and drops back, 500 ms each way on repeat.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `height` | `Dp` | `16.dp` | Bounce height. |
| `animationSpec` | `AnimationSpec<Float>` | infinite 500 ms ease-out reverse | Bounce timing. |
| `exitAnimationSpec` | `AnimationSpec<Float>` | `spring()` | Settle back when deactivated. |

### `Modifier.cyberBoot`

**What:** A power-on flicker, like a failing fluorescent tube or a display booting.

**Looks like:** Over 800 ms the element flickers 0 → 60% → 20% → 80% → 40% → 100% → 70% → 100% → 90% → 100% opacity, then stays fully visible. It plays each time the trigger activates.

**Use for:** Screen or panel entrances, revealing a result.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `animationSpec` | `AnimationSpec<Float>` | 800 ms flicker keyframes | The flicker pattern. |
| `exitAnimationSpec` | `AnimationSpec<Float>` | `tween(300)` | Currently no visible effect (alpha is 1 while inactive). |

---

## Surfaces & patterns

### `Modifier.cyberDatastream`

**What:** Data streaming down a surface, like falling code.

**Looks like:** A repeating vertical gradient in `color` that fades from transparent to bright and scrolls downward (100 px/s × `speed`), blended with Screen so it only brightens. With `mirror = true`, a second reversed stream scrolls upward through it.

**Use for:** Backgrounds of data panels, loading states, network activity.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `color` | `Color` | required | Stream color. |
| `speed` | `Float` | `1` | Scroll speed. |
| `maxAlpha` | `Float` | `0.5` | Target strength. With the default `alphaTransform` the peak opacity is `maxAlpha²` (0.25). |
| `mirror` | `Boolean` | `false` | Adds an upward stream. |
| `alphaTransform` | `(Float) -> Float` | `f × maxAlpha` | Shapes the fade along each stream; input runs 0 (tail) to 1 (head). |
| `animationSpec` | `AnimationSpec<Float>` | `tween(300)` | Fade in/out when the trigger changes. |

### `Modifier.cyberStripes`

**What:** Moving diagonal hazard stripes.

**Looks like:** 45° stripes `stripeWidth` wide with equal gaps, sliding sideways continuously, drawn over the content and clipped to its bounds. Default is 15% white.

**Use for:** Caution zones, disabled or locked areas, "under construction" and progress fills.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `color` | `Color` | 15% white | Stripe color. |
| `stripeWidth` | `Dp` | `5.dp` | Stripe (and gap) width. |
| `speed` | `Float` | `1` | Only used by the default `animationSpec`: one stripe-period every 500 ms ÷ `speed`. |
| `animationSpec` | `InfiniteRepeatableSpec<Float>` | infinite linear | Movement timing. |

No trigger: always on.

### `Modifier.cyberHoloBackground`

**What:** A slowly rotating holographic color sweep behind the content.

**Looks like:** A conic (sweep) gradient from the theme secondary color through translucent cyan, magenta and green, rotating once every 8 s behind the content. The gradient is drawn larger than the element; clip the element (e.g. `.clip(shape)`) or it can spill outside.

**Use for:** Premium/special cards, badges, loading splash backgrounds.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `animationSpec` | `InfiniteRepeatableSpec<Float>` | 8000 ms linear | One rotation. |

### `Modifier.cyberBorder`

**What:** A plain dashed technical border (no glow).

**Looks like:** A thin cyan outline of `shape`, dashed 10 px on / 10 px off by default.

**Use for:** Drop zones, placeholders, secondary frames. Pass `pathEffect = null` for a solid line.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `width` | `Dp` | `1.dp` | Stroke width. |
| `color` | `Color` | `Color.Cyan` | Stroke color. |
| `shape` | `Shape` | `CutCornerShape(12.dp)` | Outline to trace. |
| `pathEffect` | `PathEffect?` | 10 px dash, 10 px gap | Dash pattern; `null` = solid. |

### `Modifier.cyberBackdropBlur`

**What:** Blurs the element's own content and lays a light tint behind it, for a frosted-glass look.

**Looks like:** Children drawn inside the element are Gaussian-blurred by `radius`, over a 10% white tint. It does **not** blur content behind the element (siblings or the parent's background). Below Android 12 there's no blur, only the tint.

**Use for:** A frosted panel whose own contents should be soft (a blurred preview image, an obscured secret). For true "glass over the app" blur, render the background inside the blurred element.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `radius` | `Dp` | `12.dp` | Blur radius (API 31+). |
| `tint` | `Color` | 10% white | Drawn behind the content; `Transparent` to skip. |

---

## Interaction

### `Modifier.cyberDraggable`

**What:** Makes an element draggable and reports it to the surrounding drag-and-drop scope. Adds no visuals.

**How:** Wrap the area in `CyberDragDropProvider { … }`, apply `cyberDraggable(data)` to each item, and read `LocalCyberDragDropState.current` (`isDragging`, `dragPosition`, `draggedData`) wherever you render drop targets.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `data` | `T` | required | Payload exposed as `draggedData` while dragging. |
| `snapBackOnRelease` | `Boolean` | `false` | Return to the original position on release. |
| `onDragStart` / `onDragEnd` | `() -> Unit` | no-op | Callbacks. `onDragEnd` also runs on cancel. |

Throws if used outside `CyberDragDropProvider`.

### `Modifier.cyberLongPressFill`

**What:** Hold-to-confirm. Reports fill progress while the user holds, and fires once it's full.

**How:** Drive a visual (e.g. `CyberRim(progress)` or a fill bar) from `onProgressUpdate`. Releasing early resets progress to 0.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `durationMillis` | `Long` | `1500` | Hold time to complete. |
| `pollingDelayMillis` | `Long` | `16` | Progress update interval (~60 Hz). |
| `onProgressUpdate` | `(Float) -> Unit` | required | Called with 0–1 while held, and 0 on early release. |
| `onComplete` | `() -> Unit` | required | Called once when progress reaches 1. |

---

## Utilities

### `Modifier.cyberSemantics`

Applies the library's accessibility label pattern to your own components. See [Accessibility parameters](README.md#2-accessibility-parameters).

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `name` | `String` | required | Default label. |
| `appendedA11y` | `String?` | `null` | Appended as `"name - appended"`. |
| `customA11y` | `String?` | `null` | Replaces the label. |

### `rememberDrivenState` / `rememberDrivenFloatState`

**What:** Drive an effect parameter from live data instead of a fixed animation. On a fixed tick, the current state is combined with the latest data through your `operation`.

**Use for:** Rotation speed from CPU load, glow intensity from signal strength, a smoothed gauge.

```kotlin
// Needle eases toward the latest reading (temporal smoothing)
val needle by rememberDrivenFloatState(telemetry = reading) { current, target -> current + (target - current) * 0.1f }
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `telemetry` | `T` / `Float` | required | Latest data value; always read fresh. |
| `initialState` | `R` / `Float` | required / `0f` | Starting state. |
| `updateFrequencyMs` | `Long` | `16` | Tick interval (~60 Hz). |
| `operation` | `(current, telemetry) -> R` | required | Returns the next state. Patterns: map (`t * 2`), smooth (`c + (t - c) * 0.1f`), accumulate (`c + t`), decay (`(c + t) * 0.95f`). |

`rememberDrivenFloatState` avoids boxing; prefer it for Float animation values.

---

## Choosing an effect

| You want… | Use |
| --- | --- |
| Error / glitch / alarm | `cyberOverload` (brief, or on `PRESS`) |
| Retro monitor or feed | `cyberScanlines`, add `cyberCrt` for a full screen |
| Texture on flat backgrounds | `cyberNoise` at low opacity |
| Neon text | `GlowingText` (big), `cyberTextGlow` (inline) |
| Neon icon | `CyberGlowIcon`, or `CyberGlowIconPath` for outline icons |
| Neon frame | `cyberGlowBorder`; `cyberGlowBorderFlow` for the one highlighted item |
| Live / online indicator | `cyberPing` or `cyberIconPulse` |
| Loading | `cyberIconSpin`, `cyberDatastream` |
| Entrance | `cyberBoot` |
| Caution / locked | `cyberStripes` |
| Energy burst | `CyberSpark` |
