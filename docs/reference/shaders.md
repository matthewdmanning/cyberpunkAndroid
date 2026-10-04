# Shaders

The distortion effects are built on AGSL (Android Graphics Shading Language) runtime shaders: small programs that run on the GPU once per pixel. This page is for **contributors**. It covers what each shader computes, the uniforms (inputs) it takes, how its fallback differs, and how to add a new one.

To *use* these effects, go to [Modifiers & effects](modifiers-and-effects.md#distortion-shaders).

---

## How shader effects are wired

```
Modifier.cyberOverload(…)            public API, CyberModifiers.kt
  └─ animateTriggeredLevel(…)         trigger → animated strength (0 = off)
  └─ rememberEffectClock()            seconds since start, wraps every 100 s
  └─ CyberEffectBaseNode (abstract base class)
        shaderSource = CyberShaders.OverloadShader,
        level, uniforms = { … }, fallback = { … })
          ├─ API 33+: RuntimeShader → RenderEffect on a graphicsLayer
          └─ API < 33: drawWithCache → CyberFallbacks.draw…Fallback
```

`CyberEffectBaseNode` owns everything the shader effects have in common:

- **Compiling:** compiles the shader once per call site.
- **Standard uniforms:** sets the `resolution` uniform and attaches the content as the `contents` input.
- **Skipping:** skips all work while `level` is 0 or the element has no size.
- **Clipping:** clips to bounds (optionally only while active, as `cyberCrt` does).
- **Fallback:** runs the fallback drawing on API < 33.

**Contract:** every shader declares `uniform float2 resolution;` and `uniform shader contents;` and returns the final color for the pixel at `fragCoord`. Setting a uniform the shader doesn't declare throws at runtime, so uniform names in Kotlin must match the AGSL exactly.

`CyberShaders` (sources) and `CyberFallbacks` (API < 33 drawing) are `internal`.

---

## `CrtShader`

**Effect:** CRT screen look, specified as barrel distortion and vignette. Used by `Modifier.cyberCrt`. Steps 2–3 below go beyond that spec and are pending a keep/drop decision.

**What it computes**
1. **Barrel distortion:** maps each pixel to −1…1 space and pushes it outward by `1 + r² × 0.20`, so the center bulges.
2. **Off-screen corners:** pixels that map outside the image return opaque black.
3. **Edge color fringing:** red samples shift left and blue right by `r² × 0.015`, so fringing grows toward the edges.
4. **Vignette:** darkens from the center outward with `smoothstep(1.6, 0.3, length)`.

| Uniform | Type | Set from |
| --- | --- | --- |
| `resolution` | `float2` | runtime |
| `time` | `float` | effect clock (declared, **not used** by the math) |

**Fallback:** radial vignette only (transparent → 55% black). No curvature or fringing.

**Tuning constants:** curvature `0.20`, fringing `0.015`, vignette `1.6 / 0.3`. `CyberConfig.Shaders.CrtCurvature` (0.3) exists but isn't wired in; see [issue #1](https://github.com/matthewdmanning/cyberpunkAndroid/issues/1).

## `OverloadShader`

**Effect:** RGB channel split with per-row jitter. Used by `Modifier.cyberOverload`.

**What it computes**
1. **Per-row jitter:** a random value per row and time, `random(uv.y, time)`, in −1…1, times `0.08 × intensity`.
2. **Channel sampling:** red is sampled at `+0.04 × intensity + jitter`, blue at `−0.04 × intensity + jitter`, and green at `jitter` (fractions of the width).
3. **Output:** returns `(r, g, b)` from the three samples. Alpha is the maximum of all samples, so ghosts show over transparent areas.

| Uniform | Type | Set from |
| --- | --- | --- |
| `resolution` | `float2` | runtime |
| `time` | `float` | clock × `timeScale` |
| `intensity` | `float` | animated level |

**Fallback:**
- **Channel copies:** red- and blue-tinted copies (Screen blend), offset ±2% × `intensity` plus jitter.
- **Slice glitch:** on about 20% of jitter steps, a random horizontal slice is displaced.
- **Speed:** jitter time runs at `timeScale ÷ 0.15`, faster than the shader.

## `ScanlinesShader`

**Effect:** Moving horizontal scanlines. Used by `Modifier.cyberScanlines`.

**What it computes**
1. **Bands:** a sine wave down the screen, `sin((y − time × 30) × 2π / spacing)`, remapped to 0…1, so there's one band per `spacing` pixels moving down at 30 px/s.
2. **Band strength:** the wave times `scanlineOpacity`.
3. **Color:** if `scanlineColor` is fully transparent, the content is darkened by up to 70% × strength; otherwise the content is mixed toward `scanlineColor`.

| Uniform | Type | Set from |
| --- | --- | --- |
| `resolution` | `float2` | runtime |
| `time` | `float` | clock × `speed` |
| `scanlineOpacity` | `float` | animated level |
| `spacing` | `float` | `spacing` in px (min 1) |
| `scanlineColor` | `layout(color) half4` | `color` |

**Fallback:** solid lines half a `spacing` thick, moving at 20 px/s × `speed`. A transparent color darkens with black at 70% × opacity, matching the shader's darkening.

## `NoiseShader`

**Effect:** Per-pixel grain. Used by `Modifier.cyberNoise`.

**What it computes:** `color.rgb += (random(uv + time) × 2 − 1) × intensity`. Each pixel is brightened or darkened by up to `intensity`, and the pattern re-rolls whenever `time` changes.

| Uniform | Type | Set from |
| --- | --- | --- |
| `resolution` | `float2` | runtime |
| `time` | `float` | clock × `speed` (0 when `animated = false`) |
| `intensity` | `float` | animated level |

**Fallback:** 200 random 2 px points per frame, half white and half black, at `opacity × 2.5` alpha.

## `SparkShader`

**Effect:** Particle sparks drawn analytically: no particle objects, and each pixel sums the light from every spark. Used by `Modifier.cyberSpark`.

**What it computes, for each of `sparkCount` sparks (`i`, up to `MaxSparks` = 64)**
1. **Life cycle:** each spark has its own staggered 0…1 life, `t = fract(time × speed × (0.8…2.0) + offset)`, with smoothstep easing for position.
2. **Trajectory:** launched from the center upward (`vy ≈ −1.2 ± 10%`) with a small random sideways velocity (`vx` in ±0.5), then pulled down by gravity 1.8. The horizontal reach is about ±12% of the short side, so the burst is a narrow fountain.
3. **Brightness:** a pinpoint glow, `radius × flicker ÷ (d² + ε)`. The radius is `0.0006 × intensity` and halves over the spark's life. Flicker re-rolls 30 times per second in 0.85–1.15.
4. **Color:** white → `warningColor` (life 0–25%), `primaryColor` → `secondaryColor` (25–60%), `secondaryColor` → half-brightness `warningColor` (60–100%). The color jumps at 25%.
5. **Fade:** multiplied by `1 − t`, then added on top of the content.

| Uniform | Type | Set from |
| --- | --- | --- |
| `resolution` | `float2` | runtime |
| `time` | `float` | raw clock (seconds) |
| `intensity` | `float` | animated level |
| `speed` | `float` | `speed` |
| `sparkCount` | `float` | `sparkCount`, clamped 0–64 |
| `primaryColor`, `secondaryColor`, `warningColor` | `layout(color) half4` | parameters / theme |

**Implementation notes:**
- **Constant loop bound:** AGSL loops need a constant bound, so the loop always runs 64 times and skips sparks past `sparkCount`.
- **Speed applied in the shader:** Kotlin passes the raw clock as `time`; the shader applies `speed`.

**Fallback:**
- **Dots:** `sparkCount` dots of 3–6 px × `intensity`.
- **Launch angle:** ±45° from vertical, with launch speed scaled to 45% of the short side.
- **Life:** same color stages, fading out and halving in radius over each spark's life.

---

## Adding a shader effect

1. **Write the AGSL** as a `const val` in `CyberShaders`, annotated `@Language("AGSL")`. Declare `uniform float2 resolution;` and `uniform shader contents;`, and sample the content with `contents.eval(coord)`.
2. **Write a fallback** in `CyberFallbacks` as a `ContentDrawScope` extension that calls `drawContent()` itself. It can be a simpler approximation.
3. **Add the modifier node element** in `CyberModifiers`, following the AGENTS.md parameter order:

```kotlin
internal data class CyberExampleElement(
    val strength: Float,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: AnimationSpec<Float>
) : ModifierNodeElement<CyberExampleNode>() {
    override fun create() = CyberExampleNode(strength, trigger, interactionSource, animationSpec)
    override fun update(node: CyberExampleNode) = node.update(strength, trigger, interactionSource, animationSpec)
    override fun InspectorInfo.inspectableProperties() { name = "cyberExample" }
}

internal class CyberExampleNode(
    var strength: Float,
    trigger: CyberInteractionTrigger,
    interactionSource: InteractionSource?,
    animationSpec: AnimationSpec<Float>
) : CyberEffectBaseNode(CyberShaders.ExampleShader, trigger, interactionSource, strength, animationSpec) {
    fun update(strength: Float, trigger: CyberInteractionTrigger, interactionSource: InteractionSource?, animationSpec: AnimationSpec<Float>) {
        this.strength = strength
        updateBase(trigger, interactionSource, strength, animationSpec, true)
    }
    override fun applyShaderUniforms(shader: RuntimeShader, size: Size, clock: Float, current: Float) {
        shader.setFloatUniform("time", clock)
        shader.setFloatUniform("strength", current)
    }
    override fun ContentDrawScope.drawFallback(current: Float, clock: Float) {
        with(CyberFallbacks) { drawExampleFallback(current, clock) }
    }
}

fun Modifier.cyberExample(
    strength: Float = 0.5f,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = tween(300),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberExample", appendedA11y, customA11y).then(
    CyberExampleElement(strength, trigger, interactionSource, animationSpec)
)
```

4. **Document it:**
   - add an entry to [modifiers-and-effects.md](modifiers-and-effects.md) with its look, when to use it, and parameters;
   - add a section to this page;
   - add a KDoc visual description (required by AGENTS.md).

**Tips**
- Read `clock.value` and the `level` state inside `uniforms`/`fallback` lambdas, never in the composable body. That keeps per-frame updates in the draw phase, with no recomposition.
- Use `colorUniform` (not `floatUniform`) for `layout(color)` uniforms so colors are converted to the right color space.
- Keep loops bounded by constants; AGSL requires it.
