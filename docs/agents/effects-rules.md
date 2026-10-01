# Effect implementation rules

Use these rules when you implement or compose visual effects. See
[Library API conventions](api-conventions.md) for public signature ordering and
base-component boundaries.

## Drawing APIs

- Never use low-level Canvas or native Canvas drawing calls, including
  `drawIntoCanvas`, `nativeCanvas`, and `Canvas`.
- Low-level Canvas bypasses Compose hardware-accelerated effect pipelines,
  ignores `RenderEffect` Android Graphics Shading Language (AGSL) runtime
  shaders, and breaks layout scaling and layering.
- Use Jetpack Compose layout primitives, high-level `DrawScope` methods such as
  `drawWithCache`, `drawWithContent`, and `drawBehind`, `graphicsLayer`, and
  AGSL `RuntimeShader` APIs.
- Avoid `Painter` when another Compose API covers the task.
- Before implementing drawing logic or effects, use the `context7-mcp` skill to
  verify the relevant Jetpack Compose or Android API.

## Modifier ordering

Because Compose modifier ordering is highly effect-dependent and unpredictable for agents who cannot visually verify the screen, you MUST follow these specific ordering and composition rules based on the category of the effect you are applying. 

**1. Macro-Shaders (`cyberScanlines`, `cyberOverload`, `cyberNoise`, `cyberDatastream`)**
- **How they work:** These AGSL shaders sample the pixels drawn *inside* their `graphicsLayer`. They multiply their output by the alpha of the content.
- **The Rule:** The background MUST be drawn *inside* the shader node. You must chain the background *after* the effect modifier. 
- **Correct Order:** `Modifier.then(macroShader).background(Color)` (The shader processes the background).
- **Incorrect Order:** `Modifier.background(Color).then(macroShader)` (The shader sees a transparent void and renders invisible!).
- **Borders:** Component borders should be drawn *outside* (before) the shader so they don't get glitched. 
  `Modifier.border(...).then(macroShader).background(...)`

**2. Glassmorphism & Overlays (`cyberBackdropBlur`)**
- **How they work:** Blurs and tints whatever is drawn *behind* them in the Compose tree.
- **The Rule:** Do NOT apply to the same `Box` as the background. Apply it to a floating foreground `Box` layered over the background content.
- **Correct Order:** 
  ```kotlin
  Box(Modifier.background(DarkColor)) { 
      Text("Content to be blurred")
      Box(Modifier.matchParentSize().cyberBackdropBlur(...)) 
  }
  ```

**3. Shape-based Borders (`cyberNeonBorder`, `cyberNeonBorderFlow`)**
- **How they work:** Uses standard Compose `border()` drawing mechanics on a given `Shape`.
- **The Rule:** Apply to the outer container. Ensure the `Shape` provided matches the component's clip shape.
- **Correct Order:** `Modifier.background(Color, Shape).cyberNeonBorder(Shape)`

**4. Content Transforms (`cyberGlow`, `cyberIconPulse`, `cyberPing`, `cyberFloat`, `cyberBounce`)**
- **How they work:** Animates alpha, scale, or translation of a specific vector or element.
- **The Rule:** Apply directly to the `Icon` or inner content itself, NOT the outer container, otherwise the entire component container will float/pulse/ping.
- **Correct Order:** `Icon(modifier = Modifier.cyberFloat())`

**5. Path borders & dividers (`cyberPathBorder`, `cyberPathDivider`)**
- **How they work:** They draw over the content with `drawWithContent`, along the shape outline or a horizontal line, then add a glow pass.
- **The Rule:** Pass the same `Shape` as the component's background. Apply outside (before) macro-shaders so the shader doesn't distort the border.
- **Correct Order:** `Modifier.background(color, shape).cyberPathBorder(CyberCometTracer(), shape = shape)`
- **With a macro-shader:** `Modifier.cyberPathBorder(effect, shape = shape).cyberOverload().background(color, shape)`
