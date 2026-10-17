# High-Impact Visual Effects Layout & Composition

## Core Principle

Maximizing the visual impact of special effects (AGSL shaders, particle simulations, blurs, and glow passes) requires **focal hierarchy** paired with a **strict GPU rendering budget**. When every element on a screen glows, pulses, and glitches simultaneously, visual contrast collapses into noise and GPU fill-rate limits trigger thermal throttling. 

Maximum aesthetic impact is achieved through **contrast against restraint**: anchoring heavy visual effects to a single hero focal point, stabilizing the perimeter with geometric frames, and driving secondary effects through user interaction.

---

## 1. Visual Hierarchy: The "One-Hero" Architecture

```
┌──────────────────────────────────────────────────────────┐
│  PERIMETER: Ambient framing (static scanlines/dividers)  │
│                                                          │
│     ┌──────────────────────────────────────────────┐     │
│     │        HERO COMPONENT (Focal Center)         │     │
│     │  Heavy Shader (CRT / Overload / Sparks)      │     │
│     │  Highest Luminosity & Continuous Motion      │     │
│     └──────────────────────────────────────────────┘     │
│                                                          │
│     ┌────────────────────────┐  ┌────────────────────────┐│
│     │ Secondary Metric Card  │  │ Secondary Metric Card  ││
│     │ Crisp Static Border    │  │ Crisp Static Border    ││
│     │ Reactive-Only State    │  │ Reactive-Only State    ││
│     └────────────────────────┘  └────────────────────────┘│
│                                                          │
│  INTERACTIVE ANCHOR: High-contrast CTA with localized glow│
└──────────────────────────────────────────────────────────┘
```

### Tier 1 — The Hero Surface (Max 1 per screen)
- **Role:** Primary eye magnet and narrative focal point (e.g., active telemetry monitor, radar sweep, 3D hologram, or overloaded core).
- **Effect Type:** Continuous AGSL runtime shaders (`cyberOverload`, `cyberCrt`, `cyberSpark`), complex particle physics, or multi-pass blurs.
- **Budget Rule:** Confine to one distinct container. Never tile or duplicate continuous macro-shaders across multiple sibling cards.

### Tier 2 — Supporting Panels & Data Metrics
- **Role:** Legibility and structural balance.
- **Effect Type:** High-contrast geometric outlines (`border`, `cyberCutCornerShape`), static noise grain (`cyberNoise` with low opacity), or subtle path accents (`cyberPathDivider`).
- **Budget Rule:** Zero continuous per-pixel GPU recalculation. Use vector drawing paths and static shaders.

### Tier 3 — Reactive & Event-Driven Effects (Zero Idle Cost)
- **Role:** Tactile satisfaction and dynamic feedback.
- **Effect Type:** Momentary chromatic glitches, expanding radar pings (`cyberPing`), icon pulses, or border bloom.
- **Budget Rule:** Trigger strictly on interaction (`PRESS`, `HOVER`, `FOCUS`) or discrete state changes (data alert, completion). When idle, cost drops to 0 FPS / 0 GPU overhead.

---

## 2. Spatial Composition & Z-Axis Layering

### A. Perimeter Anchoring (HUD / Cockpit Technique)
- **Anchor visual geometry to the edges:** Human vision processes ambient orientation along the display periphery. Frame the viewport using corner brackets, edge borders, and path tracers.
- **Protect content core:** Keep data feeds, lists, and dense telemetry free of heavy chromatic aberration or barrel distortion. A high-contrast, stable core framed by stylized perimeters delivers immediate cyberpunk/sci-fi impact without causing visual fatigue.

### B. Z-Axis Layer Depth
Arrange UI layers in a deliberate stack:
1. **Backdrop Layer:** Faint procedural background grid or subtle scanline canvas (alpha 0.10–0.20).
2. **Structural Midground:** Solid surface containers with sharp cutouts and tinted borders.
3. **Internal Shader Layer:** Clipped strictly within the hero card boundary.
4. **Foreground Embellishments:** Accent badges, pulsing status icons, and glowing edge tracers.
5. **Transient Overlays:** Floating backdrop blur or scanline sweeps triggered during modal/alert states.

---

## 3. GPU Performance Architecture & Compositor Budget

### A. The "Offscreen Buffer Tax" & Surface Area Sizing
Any composable applying a `RenderEffect`, dynamic AGSL shader, or fractional alpha requires the GPU compositor to allocate an isolated offscreen texture ($Width \times Height$):
- **Sizing Considerations:** Applying a shader modifier (`cyberOverload`, `cyberCrt`) over larger containers scales GPU fragment processing with resolution. Modern tested hardware can readily power large hero panels and full-card distortion passes at 60–120 FPS; profile frame times when scaling across wider form factors.

### B. Never Stack Shaders Inside Lazy Lists
- **Anti-Pattern:** Placing multi-pass blurs, CRT shaders, or overload shaders inside repeating items in a `LazyColumn` or `LazyVerticalGrid`.
- **Best Practice:** Keep list items lightweight using vector borders and solid fills. Keep the animated shader on a pinned header or floating hero card above the list.

### C. Consolidate `graphicsLayer` Calls
Avoid chaining separate modifiers that each create distinct graphics layers (`.alpha()`, `.clip()`, and custom shaders). Combine transformations into a single `graphicsLayer { ... }` block so the GPU executes them in a single offscreen pass.

---

## 4. Modifier Chaining Rules for Compose Effects

Compose modifier order determines how hardware layers process content:

1. **Macro-Shaders (`cyberScanlines`, `cyberOverload`, `cyberNoise`):**
   - Sample pixels drawn *inside* their node and multiply by alpha.
   - **Correct:** `Modifier.border(...).cyberOverload().background(color, shape)`
   - *Note: Borders drawn before the shader remain crisp and undistorted.*
2. **Glassmorphism / Blurs (`cyberBackdropBlur`):**
   - Must be placed on a floating foreground container above the content being blurred, not on the background itself.
3. **Content Transforms (`cyberIconPulse`, `cyberPing`, `cyberFloat`):**
   - Apply directly to the icon or vector element, never to the parent container (which would cause the entire card to shift).

---

## 5. Canonical Layout Recipe

```kotlin
@Composable
fun HighImpactTelemetryScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberTheme.colors.background)
    ) {
        // 1. BACKDROP: Minimal-overhead ambient grid
        AmbientGridBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2. HERO FOCAL POINT: Confined heavy AGSL shader
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.5.dp, CyberTheme.colors.primary, CyberTheme.shapes.cutCorner)
                    .cyberOverload(intensity = 0.85f)
                    .background(CyberTheme.colors.surfaceSecondary, CyberTheme.shapes.cutCorner)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                HeroTelemetryDisplay()
            }

            // 3. SUPPORTING PANELS: Crisp geometry, zero continuous GPU drain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusCard(
                    title = "REACTOR CORE",
                    value = "NOMINAL",
                    modifier = Modifier.weight(1f)
                )
                StatusCard(
                    title = "FLUX DENSITY",
                    value = "94.2%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. ACTION ANCHOR: High-contrast glow with event-driven interaction
            CyberButton(
                text = "INITIATE PURGE",
                onClick = { /* trigger reactive effect */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .cyberGlowBorder(CyberTheme.shapes.buttonShape)
            )
        }
    }
}
```

---

---

## 6. Physical Hardware Sizing & Visibility Thresholds

Designing high-impact cybernetic UI requires calibrating component and stroke geometry to physical high-DPI smartphone screens (such as 400+ dpi OLED panels). 

> [!CAUTION]
> **Anti-Pattern: Choosing the Smallest Non-Prohibited Size as a Design Default**
> A common failure mode in generative and systematic UI composition is selecting the lowest boundary accepted by the compiler (e.g. `1.dp` border width, `16.dp` or `24.dp` icon size, or low alpha on broken strokes) simply because it is permitted. This results in faint, invisible hairline borders and timid interactive targets that whisper rather than command attention. Always size for visual prominence and physical touch ergonomics.

### A. Interactive Hero & Highlight Sizing ("Scream 'HOLD ME!'")
- **Primary Interactive Targets:** Central user interaction points (hold-to-charge actuators, limiter bypass buttons, emergency sever clamps) must never use secondary status icon sizes (`dp16`/`dp24` or even `dp32`).
- **Hero Icon Token:** Use `CyberPrimitives.IconSizes.dp64` (64.dp) centered inside a dedicated tactile button (88dp–96dp diameter).
- **Actuator Frame:** Frame the central hero icon with `CyberRim` (116dp–124dp) and `CyberDialTicks` (136dp–144dp) to establish an unmistakable, massive tactile focal point that screams its affordance to the user.

### B. High-DPI Stroke Legibility for Dotted & Dashed Borders
- **The Hairline Scattering Problem:** On high-density screens, `1.dp` strokes (~2.6 to 3.5 physical device pixels) rendered with dashed path effects (`10px` on / `10px` off) or radial ticks at reduced opacity (< 0.6f) become practically invisible hairline dust.
- **Minimum Stroke for Broken Paths:** Dotted, dashed, or ticked strokes (`Modifier.cyberBorder`, `CyberDialTicks`) must be at least `CyberPrimitives.BorderWidths.dp2` (2.dp) or `dp4` (4.dp).
- **Dash and Tick Calibration:** Specify major tick lengths of at least `10.dp–12.dp` and minor ticks of `6.dp`, with contrast opacity ≥ `0.75f` (or emissive glow) to guarantee optical legibility in all ambient lighting environments.
- **Solid Dividers:** Reserve `1.dp` strokes strictly for solid, unbroken divider lines rendered in high contrast against dark surfaces.

---

## Layout Checklist

- [ ] **One Hero Surface:** Exactly one prominent component uses continuous AGSL shaders or particle physics.
- [ ] **Layer Scope:** Hero shader container bounds match tested device performance targets at steady frame rates.
- [ ] **Hero Interactive Affordance:** Primary interactive touch actuators use `dp64` hero icons inside prominent (120dp–144dp) actuator clusters rather than small status glyphs.
- [ ] **Stroke Legibility:** Dotted, dashed, and ticked strokes specify at least `2.dp` width with high contrast (alpha ≥ 0.75f); no invisible 1.dp hairline dots.
- [ ] **List Isolation:** No repeating GPU shaders or multi-pass blurs inside `LazyColumn`/`LazyGrid` items.
- [ ] **Edge Anchoring:** Peripheral geometry (brackets, path tracers) frames the screen without covering central data.
- [ ] **Event-Driven Effects:** Heavy dynamic bursts (glitches, radar pulses) are bound to user touch or state updates rather than infinite loops.
- [ ] **Correct Chaining:** Outer borders placed before macro-shaders; backgrounds placed after shaders.

---

## Visual Arranging Rules Summary

| Screen Zone | Effect Type | Movement / Animation | Performance Cost |
| :--- | :--- | :--- | :--- |
| **Viewport Edges / Frame** | Bracket borders, path tracers, subtle vignettes | Static or slow loop | **Low** (Vector drawing) |
| **Hero Card (Top 1/3 to Center)** | AGSL macro-shaders (CRT, Overload, Sparks) | Dynamic & continuous | **Moderate–High** |
| **Tactile Interactive Actuators** | Hero icons (`dp64`), radial dial ticks (`dp2`), progress rims | Event-driven on touch / long press | **Low** (Vector / Canvas) |
| **Data Cards & Grids** | Cut-corner shapes, clean outlines, static noise | Static | **Low** |
| **Primary Buttons / Triggers** | Edge glows, momentary glitch burst on click | Event-driven (0 FPS when idle) | **Zero idle cost** |


