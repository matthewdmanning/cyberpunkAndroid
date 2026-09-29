### Modifier Ordering Rules

Because Compose modifier ordering is highly effect-dependent and unpredictable for agents who cannot visually verify the screen, you MUST follow these specific ordering and composition rules based on the category of the effect you are applying. 

**1. Macro-Shaders (`cyberScanlines`, `cyberOverload`, `cyberNoise`, `cyberDatastream`)**
- **How they work:** These AGSL shaders sample the pixels drawn *inside* their `graphicsLayer`. They multiply their output by the alpha of the content.
- **The Rule:** The background MUST be drawn *inside* the shader node. You must chain the background *after* the effect modifier. 
- **Correct Order:** `Modifier.then(macroShader).background(Color)` (The shader processes the background).
- **Incorrect Order:** `Modifier.background(Color).then(macroShader)` (The shader sees a transparent void and renders invisible!).
- **Borders:** Component borders should be drawn *outside* (before) the shader so they don't get glitched. 
  `Modifier.border(...).then(macroShader).background(...)`

**2. Glassmorphism & Overlays (`cyberBackdropBlur`)**
- **How they work:** Blurs the content drawn *inside* the modified element (its children) and draws a tint behind them. A layer's render effect cannot sample siblings or the parent, so it never blurs what is *behind* it.
- **The Rule:** Put the content that should look frosted *inside* the blurred element. An empty overlay `Box` on top of other content blurs nothing and only shows the tint.
- **Correct Order:** 
  ```kotlin
  Box(Modifier.background(DarkColor).cyberBackdropBlur(...)) { 
      Text("Content to be blurred")
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
