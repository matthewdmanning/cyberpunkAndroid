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

**Looks like:** Soft horizontal bands, one per `spacing`, drifting slowly downward. The description also calls for subtle barrel curvature, which isn't implemented yet ([see the table](README.md#doesnt-match-its-description-yet)). With the default transparent `color` the bands darken the content (up to 70% × `opacity`); with a color they tint it toward that color. The default opacity (2%) is very subtle; 0.2–0.5 is clearly visible.

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
| `color` | `Color` | `Transparent` | `Transparent` darkens bands; any other color tints bands toward it. |
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

**What:** A CRT (cathode-ray tube) screen look.

**Looks like (spec):** Barrel distortion and a vignette: the content bulges outward and the border darkens.

Today the shader also adds red/blue fringing toward the edges and turns corners pushed off-screen black, which the spec doesn't mention. The fallback below Android 13 draws only the vignette (transparent center fading to 55% black), with no distortion ([see the table](README.md#doesnt-match-its-description-yet)). No quality parameter set has been rated yet, so strength values are still hard-coded in the shader.

**Use for:** A retro monitor frame around a whole panel or screen. **Avoid** on small elements; the distortion needs room.

```kotlin
Modifier.cyberCrt().background(CyberTheme.colors.surfacePrimary)
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `enabled` | `Boolean` | `true` | `false` removes the effect entirely. |
| `trigger` / `interactionSource` | | `ALWAYS` / `null` | On/off only: switches instantly, no fade. |

Unlike the other shader effects, it clips to its bounds only while active.

### `Modifier.cyberSpark`

**What:** An electrical-spark burst: glowing embers shoot up from the center, arc over and fall under gravity.

**Looks like:** `sparkCount` (default 32) pinpoint embers launching upward from the center, curving back down in parabolic arcs, and fading out. Each ember changes color over its life: white-hot → warning color (yellow) → primary (cyan) → secondary (magenta) → dim warning, and it shrinks to half size. Bursts restart at staggered times, so it pops continuously like popcorn. The fallback draws small (3–6 px) colored dots on similar arcs.

**Use for:** Short-circuit / damage moments, a "powering up" accent behind an icon, or celebratory energy. The container clips sparks at its edges, so give it room.

```kotlin
Box(modifier = Modifier.size(160.dp).cyberSpark(intensity = 1.2f)) {
    CyberIcon(CyberIcons.Zap, contentDescription = "Power surge", size = 48.dp)
}
```



| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `color` | `Color` | `Color.Unspecified` | Mid-life ember color (resolves to theme `primary`). |
| `secondaryColor` | `Color` | `Color.Unspecified` | Late-life ember color (resolves to theme `secondary`). |
| `warningColor` | `Color` | `Color.Unspecified` | Birth flash color and final dim color (resolves to `#FFB800`). |
| `sparkCount` | `Int` | `32` | Number of embers rendered. |
| `intensity` | `Float` | `1.0f` | Brightness and ember size multiplier. |
| `speed` | `Float` | `1.0f` | Frequency multiplier for particle movement. |
| `animationSpec` | `AnimationSpec<Float>` | `tween(300)` | Fade in/out when the trigger changes. |

---

## Glow

Neon glow around text, icons and borders. The blur needs Android 12+ (API 31); below that, glows have no soft spread (see each entry).

### `Modifier.cyberTextGlow`

**What:** A glow that follows the exact outline of whatever it's applied to: the letter shapes of text, the strokes of an icon.

**Looks like:** A blurred, color-tinted copy of the content drawn behind it, so each glyph or stroke gets a soft halo in `color`. On Android 13+ (API 33), this is powered by a high-fidelity AGSL `RuntimeShader` that creates a realistic "white-hot" neon core where the density is highest, tapering smoothly into the color. On API 31-32, it falls back to a fast `ColorMatrix` bloom. Below API 31 there's no blur, so the tinted copy sits directly under the content and the glow is effectively invisible.

**Use for:** Headings, key numbers and icons that should read as emissive neon. For big display text, [`GlowingText`](#glowingtext) gives a richer multi-layer bloom. Use `dropoffPower` to control how sharp or fuzzy the neon tube feels.

```kotlin
// Default cyber glow
Text("ONLINE", style = CyberTheme.typography.terminal, modifier = Modifier.cyberTextGlow(color = CyberTheme.colors.primary))

// Sharp, dense neon core with faint outer aura
Text(
    text = "WARNING",
    modifier = Modifier.cyberTextGlow(
        color = CyberTheme.semantics.colors.warning,
        intensity = 2f,
        dropoffPower = 4.5f
    )
)
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `color` | `Color` | `Color.Cyan` | Glow color. |
| `radius` | `Dp` | `8.dp` | Blur spread. `0.dp` disables the glow. |
| `intensity` | `Float` | `1` | Amplifies the glow opacity. On API 31+, this continuously multiplies the alpha for a brighter neon core. Below API 31, whole numbers stack extra drawing passes. `0` disables. |
| `dropoffPower` | `Float` | `3f` | Controls the shader's falloff curve (API 33+ only). `1f` is a smooth linear fade; higher values (e.g. `4f-6f`) create a sharp white-hot core with a wispy outer aura. |
| `outsideGlowOnly` | `Boolean` | `false` | Erases the glow wherever the content is drawn, so translucent text or icons don't show the halo through them. Renders the element offscreen (slightly more GPU memory). |

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
| `alignment` | `CyberBorderAlignment` | `CyberBorderAlignment.BOTH` | Stroke alignment relative to the boundary (`INSIDE`, `OUTSIDE`, or `BOTH`). |

### `Modifier.cyberGlowBorderRounded`

Shortcut for `cyberGlowBorder(shape = RoundedCornerShape(cornerRadius))` with a larger default glow.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `color` | `Color` | `Color.Cyan` | Border and glow color. |
| `cornerRadius` | `Dp` | `16.dp` | Corner rounding. |
| `glowRadius` | `Dp` | `12.dp` | Halo spread. |
| `width` | `Dp` | `2.dp` | Sharp stroke width. |
| `alignment` | `CyberBorderAlignment` | `CyberBorderAlignment.BOTH` | Stroke alignment relative to the boundary (`INSIDE`, `OUTSIDE`, or `BOTH`). |

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
| `alignment` | `CyberBorderAlignment` | `CyberBorderAlignment.BOTH` | Stroke alignment relative to the boundary (`INSIDE`, `OUTSIDE`, or `BOTH`). |

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
| `iconRes` | `@DrawableRes Int` | required | Vector drawable, e.g. `CyberIcons.Shield`. |
| `contentDescription` | `String?` | required | Screen reader label; `null` if decorative. |
| `modifier` | `Modifier` | `Modifier` | Size/layout. |
| `color` | `Color` | `Color.Cyan` | Icon tint. |
| `glowColor` | `Color` | `color` | Halo color. |
| `radius` | `Dp` | `12.dp` | Halo spread and padding. |
| `intensity` | `Float` | `1.5` | Halo opacity/stacking; see `cyberTextGlow`. |
| `dropoffPower` | `Float` | `3f` | Non-linear glow dropoff curve exponent. |

### `CyberGlowIconPath`

**What:** An outline icon with a bloom that stays along its strokes and leaves the inside of closed shapes dark.

**Looks like:** Three stacked copies of the icon: a faint (25%) outer bloom inset by `outerPadding`, a 55% mid glow inset by `innerPadding`, and the crisp icon on top.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `iconRes`, `contentDescription`, `modifier`, `color`, `glowColor` | | | As `CyberGlowIcon`. |
| `outerPadding` | `Dp` | `6.dp` | Inset of the far bloom layer. |
| `innerPadding` | `Dp` | `3.dp` | Inset of the mid glow layer. |
| `radius` | `Dp` | `16.dp` | Far bloom spread (mid layer uses half). |
| `intensity` | `Float` | `2` | Bloom strength. |
| `dropoffPower` | `Float` | `3f` | Non-linear glow dropoff curve exponent. |

### `Modifier.cyberLaserOutliner` & `CyberLaserText`

**What:** Sequential letter-by-letter laser beam tracing with molten contact arc, falling sparks, and cooling weld bead.

**Looks like:** A piercing vertical laser beam tracks along letter borders sequentially from left to right. The contact point emits a molten arc flare and gravity-bound spark bursts, trailing a hot molten bead that settles into the final cooled outline. Defaults to a finite one-shot reveal.

**Use for:** Hero headers, futuristic title entrance reveals, and cybernetic text borders.

```kotlin
CyberLaserText(
    text = "CYBER",
    fontSize = 44.sp,
    laserColor = CyberTheme.colors.primary,
    weldColor = CyberTheme.colors.secondary,
    durationMillis = 3000
)
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `text` | `String` | required | Text whose glyph contours will be laser-welded (`CyberLaserText` only). |
| `fontSize` | `TextUnit` | `36.sp` | Font size of the text glyphs (`CyberLaserText` only). |
| `strokeWidth` | `Dp` | `1.5.dp` | Thickness of the laser ray and welded outline stroke. |
| `glowRadius` | `Dp` | `8.dp` | Optical bloom halo around the laser and molten seam. |
| `sparkCount` | `Int` | `10` | Number of fizzing weld spark particles emitted from the contact point. |
| `durationMillis` | `Int` | `2400` | Duration for the complete left-to-right welding pass. |
| `laserColor` | `Color` | `Cyan` | Color of the vertical laser beam and contact flare. |
| `weldColor` | `Color` | `#FFB800` | Hot molten color of the freshly deposited weld pool. |
| `coolColor` | `Color` | `primary` | Settled color of the cooled weld outline once tracing completes. |
| `guideAlpha` | `Float` | `0.08f` | Opacity of the unwelded blueprint guide outline. |

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

**Looks like (spec):** A locked dense core with a diffuse outer ring that scales up and fades out. Today only the ring is drawn ([see the table](README.md#doesnt-match-its-description-yet)): a ring in `shape`'s outline starts at the element's size, grows to `maxDiffuseScale` × and fades to transparent, repeating every `durationMillis`. The ring keeps the same stroke width while it grows. Nothing is drawn while inactive.

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
| `animationSpec` | `InfiniteRepeatableSpec<Float>` | infinite linear restart | Ping timing; must be `infiniteRepeatable(…)` (enforced by the type). |

### `Modifier.cyberIconPulse`

**What:** Opacity pulse without changing size.

**Looks like:** Fades between `maxOpacity` and `minOpacity` and back, 600 ms each way.

**Use for:** Attention states: unread, warning, waiting for input.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `durationMillis` | `Int` | `600` | One fade direction. |
| `minOpacity` | `Float` | `0.2` (`CyberConfig.Effects.PulseMinOpacity`) | Dimmest point. |
| `maxOpacity` | `Float` | `1` | Brightest point. |
| `animationSpec` | `InfiniteRepeatableSpec<Float>` | infinite linear reverse | Pulse timing; must be `infiniteRepeatable(…)`. |

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

**Looks like:** Rises by `height` and drops back, 500 ms each way on repeat, on a cubic-bezier bounce curve (`CyberConfig.Easings.BounceEasing`) that dips slightly before rising and overshoots at the top.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `height` | `Dp` | `16.dp` | Bounce height. |
| `animationSpec` | `AnimationSpec<Float>` | infinite 500 ms `BounceEasing` reverse | Bounce timing. |
| `exitAnimationSpec` | `AnimationSpec<Float>` | `spring()` | Settle back when deactivated. |

### `Modifier.cyberBoot`

**What:** A power-on flicker, like a failing fluorescent tube or a display booting.

**Looks like:** Over 800 ms the element flickers 0 → 60% → 20% → 80% → 40% → 100% → 70% → 100% → 90% → 100% opacity, then stays fully visible. It plays each time the trigger activates.

**Use for:** Screen or panel entrances, revealing a result.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `animationSpec` | `AnimationSpec<Float>` | 800 ms flicker keyframes | The flicker pattern, replayed each time the trigger activates. |
| `exitAnimationSpec` | `AnimationSpec<Float>` | `tween(300)` | If the trigger deactivates mid-flicker, how the element settles to full opacity. |

---

## Surfaces & patterns

### `Modifier.cyberDatastream`

**What:** A scanning line with a decaying trail.

**Looks like (spec):** A scanning line, usually moving vertically, with opacity that decays behind it. Blended with Screen, so it only brightens. Depending on color matching, speed and parameters it reads as a **radar-like sweep** or a **raster-refresh** look.

Today it draws one horizontal line per element height moving downward (100 px/s at the default speed), with the trail fading from transparent up to `maxAlpha` at the leading edge. With `mirror = true`, a reversed line moves upward through it.

**Use for:** Radar and scanner panels, "refreshing" displays, loading states.

Named presets for the radar and raster-refresh looks will be added once a quality parameter set is found for each (`TODO(presets)` in code).

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `color` | `Color` | required | Line color. |
| `speed` | `Float` | `1` | Scroll speed (100 px/s × `speed`). |
| `maxAlpha` | `Float` | `0.5` | Peak opacity at the leading edge, 0–1. |
| `mirror` | `Boolean` | `false` | Adds an upward stream. |
| `alphaTransform` | `(Float) -> Float` | linear (`f`) | Shapes the fade along each stream: maps 0 (tail)…1 (head) to 0…1; the result is scaled by `maxAlpha`. E.g. `{ it * it }` for a sharper head. |
| `animationSpec` | `AnimationSpec<Float>` | `tween(300)` | Fade in/out when the trigger changes. |

### `Modifier.cyberStripes`

**What:** Moving diagonal hazard stripes.

**Looks like:** 45° stripes `stripeWidth` wide with equal gaps, sliding sideways continuously, drawn **behind** the content (it's a background) and clipped to its bounds. Default is 15% white.

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

**What:** A lightweight, static container border outline.

**Looks like:** A thin cyan outline of `shape`, dashed 10 px on / 10 px off (or solid line when `pathEffect = null`), rendered directly in the normal Compose drawing path (`Stroke`/`drawOutline`). For an active glowing border with outer glow passes, use `cyberGlowBorder` or `dropShadow()`.

**Use for:** Drop zones, placeholders, secondary frames. Pass `pathEffect = null` for a solid line.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `width` | `Dp` | `1.dp` | Stroke width. |
| `color` | `Color` | `Color.Cyan` | Stroke color. |
| `shape` | `Shape` | `CutCornerShape(12.dp)` | Outline to trace. |
| `pathEffect` | `PathEffect?` | 10 px dash, 10 px gap | Dash pattern; `null` = solid. |

### `Modifier.cyberBackdropBlur`

**What:** A glassmorphism overlay using translucent tint, gradient `Brush`, and optional blur on API 31+.

**Looks like:** Renders a translucent wash with subtle borders/shadows over foreground content (or API 31+ `RenderEffect.createBlurEffect` on the foreground node). To avoid heavy offscreen rendering passes, it does not force offscreen background capture layers.

**Use for:** Floating panels, modals and nav bars over busy backgrounds.

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `radius` | `Dp` | `12.dp` | Blur radius (API 31+). |
| `tint` | `Color` | 10% white | Wash over the blurred backdrop; `Transparent` to skip. |

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

Applies the library's accessibility label pattern to your own components. See [Accessibility parameters](README.md#2-accessibility-parameters). `Modifier.cyberComponentSemantics` takes the same parameters but applies the label only when `appendedA11y` or `customA11y` is set; use it for components whose own content should be read by default.

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
| Energy burst | `Modifier.cyberSpark` |
