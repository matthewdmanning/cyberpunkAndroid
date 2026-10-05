# Brainstorm: Text Edge Detection & Partial Icon/Tick Illumination

This document compiles technical explorations and architectural strategies for two related challenges in the `cyberpunkAndroid` library:
1. **Partial / Per-Tick Illumination**: Selectively lighting sub-elements of a single icon or gauge (e.g., individual dial ticks).
2. **Text Edge Detection**: Detecting, tracing, and highlighting the geometric or raster edges of text for cyberpunk styling (neon wireframes, tracing particles, contour halos).

---

## 1. Partial & Per-Tick Illumination

### The Core Problem: Modifier Boundaries
Modifiers like `Modifier.cyberTextGlow` and `Modifier.cyberRadialIllumination` operate at the **`LayoutModifierNode` / `GraphicsLayer` boundary**. They capture the composable's entire drawing pass into an offscreen layer, apply a blur filter, and render the resulting halo. 

When applied to a monolithic component like `CyberDialTicks`, the modifier treats all 60 ticks as a single atomic silhouette: the entire dial glows or dims uniformly according to the scalar intensity sampled at the bounding box.

### Architectural Approaches

#### Approach A: Decomposed Composables (Coarse Elements Only)
Each element is rendered as an independent `Box` or `CyberIcon` with its own `cyberRadialIllumination` modifier.
* **When to use**: Coarse groupings (4–12 items, e.g., the 6 `DialIcons` in `RadialEffectsScreen.kt`).
* **Why it fails for 60+ ticks**:
  - **Layout Tree Bloat**: Instantiates 60+ separate `LayoutNode` objects, triggering 60+ measurement and placement passes.
  - **GPU Pressure**: If each tick uses a glow modifier, it allocates an offscreen `GraphicsLayer` running a Gaussian blur `RenderEffect`. Running 60 separate blur passes every frame can severely degrade frame rates on mobile hardware.

#### Approach B: Draw-Phase Grouping (Recommended for Ticks)
The ticks remain mathematical models or loop indices within a single composable (`Canvas` or `drawBehind`). The illumination driver is sampled per tick during the draw pass.

```kotlin
@Composable
fun CyberIlluminatedDial(
    field: CyberRadialField,
    modifier: Modifier = Modifier,
    tickCount: Int = 60,
    color: Color = CyberTheme.colors.primary,
) {
    Spacer(
        modifier = modifier
            .size(220.dp)
            .drawBehind {
                val radius = size.width / 2f
                val center = Offset(radius, radius)
                val angleStep = 360f / tickCount

                for (i in 0 until tickCount) {
                    val angle = angleStep * i
                    
                    // Sample driver at this specific tick's angle/position
                    val intensity = field.intensityAtAngle(angle) // 0.0f .. 1.0f

                    rotate(angle, center) {
                        val tickStart = Offset(center.x, center.y - radius)
                        val tickEnd = Offset(center.x, center.y - radius + 8.dp.toPx())

                        // 1. Soft proxy glow for active ticks
                        if (intensity > 0.1f) {
                            drawLine(
                                color = color.copy(alpha = intensity * 0.5f),
                                strokeWidth = 4.dp.toPx(),
                                start = tickStart,
                                end = tickEnd,
                                cap = StrokeCap.Round
                            )
                        }

                        // 2. Crisp core tick
                        drawLine(
                            color = if (intensity > 0.1f) color else color.copy(alpha = 0.25f),
                            strokeWidth = 1.dp.toPx(),
                            start = tickStart,
                            end = tickEnd,
                            cap = StrokeCap.Square
                        )
                    }
                }
            }
    )
}
```
* **Pros**: 0 extra layout nodes, single draw pass, sub-millisecond execution, 120 FPS performance.

#### Approach C: Spatial Shader Masking / Blend Modes
Instead of post-processing the dial's silhouette, overlay a radial driver like `Modifier.cyberRadarSweep` or an AGSL mask over the component with additive blending (`BlendMode.Screen` or `BlendMode.Plus`). Only the ticks physically covered by the beam head and fading trail are illuminated.

---

## 2. Text Edge & Contour Detection

Android and Jetpack Compose provide multiple distinct mechanisms for detecting and isolating the edges of text, categorized by the level of abstraction:

### 1. Vector Glyph Geometry (`Paint.getTextPath`)
Extracts the exact mathematical vector path (`android.graphics.Path`) from font glyphs.

```kotlin
val paint = android.graphics.Paint().apply {
    textSize = 64f
    typeface = android.graphics.Typeface.MONOSPACE
}
val textPath = android.graphics.Path()
paint.getTextPath("CYBER", 0, 5, 0f, 0f, textPath)

// Convert to Compose Path
val composePath = textPath.asComposePath()
```
* **Capabilities**:
  - Provides precise contour lines, curves, and vertices.
  - Can be measured with `PathMeasure` to determine total perimeter length.
* **Cyberpunk Use Cases**:
  - Tracing laser particles around letter perimeters using `cyberPathAlong`.
  - Animating neon wireframe reveals or segment marching.

---

### 2. Hollow / Stroked Text (`drawStyle = Stroke`)
Compose natively supports stroking text contours without filling the glyph interior.

```kotlin
Text(
    text = "OVERRIDE",
    style = TextStyle(
        fontSize = 32.sp,
        color = CyberTheme.colors.primary,
        drawStyle = Stroke(
            width = 2f,
            join = StrokeJoin.Miter
        )
    )
)
```
* **Capabilities**: Instant, zero-overhead edge rendering supported on all Android API levels.
* **Cyberpunk Use Cases**: Technical wireframe typography, HUD labels, blueprint mode.

---

### 3. GPU Edge Detection Shader (AGSL on API 33+)
Computes pixel-level edge gradients directly on the GPU using an AGSL `RuntimeShader` with a Sobel or Laplacian kernel over the text's alpha channel.

```glsl
// AGSL fragment shader (Sobel edge filter)
uniform shader contents;

half4 main(float2 coord) {
    float d = 1.0; // 1-pixel kernel offset
    
    // Sample neighboring alpha values
    float aTop    = contents.eval(coord + float2(0.0, -d)).a;
    float aBottom = contents.eval(coord + float2(0.0,  d)).a;
    float aLeft   = contents.eval(coord + float2(-d,  0.0)).a;
    float aRight  = contents.eval(coord + float2( d,  0.0)).a;
    
    // Calculate gradient magnitude
    float edge = length(float2(aRight - aLeft, aBottom - aTop));
    
    // Output glowing neon rim
    return half4(half3(0.0, 1.0, 1.0) * edge, edge);
}
```
* **Capabilities**: Operates on arbitrary rendered content in real-time, regardless of font complexity.
* **Cyberpunk Use Cases**: Chromatic aberration on text borders, CRT phosphorescence rims, animated energy pulses rippling along glyph boundaries.

---

### 4. Silhouette Punching (`BlendMode.DstOut`)
Combines blur effects with silhouette subtraction to preserve only the halo around the outer perimeter of the text.

```kotlin
// In drawWithContent:
// 1. Record blurred glow of text into glowLayer
// 2. Punch out the solid text interior using DstOut
maskLayer.record { this@onDrawWithContent.drawContent() }
drawLayer(maskLayer) // with blendMode = BlendMode.DstOut
```
* **Capabilities**: Erases the internal fill, ensuring translucent content doesn't get muddied by its own halo. Used in `Modifier.cyberTextGlow(outsideGlowOnly = true)`.
* **Cyberpunk Use Cases**: Pure exterior neon halos for semi-transparent HUD readouts.

---

### 5. Layout Bounds & Character Rectangles (`TextLayoutResult`)
Extracts the structural bounding rectangles for characters, words, and lines.

```kotlin
Text(
    text = "TARGET ACQUIRED",
    onTextLayout = { layoutResult ->
        // Bounding box of character at index 0:
        val charRect: Rect = layoutResult.getBoundingBox(0)
        
        // Exact Path enclosing a word or range of characters:
        val wordPath: Path = layoutResult.getPathForRange(0, 6)
    }
)
```
* **Capabilities**: Provides pixel-accurate bounding geometry aligned with the layout coordinate space.
* **Cyberpunk Use Cases**:
  - Drawing bracketed targeting reticles around active words.
  - Terminal selection boxes, cursor highlights, and scanning highlight sweeps across lines.

---

## 3. Technology Selection Matrix

| Objective | Recommended Method | API Level | Performance |
| :--- | :--- | :--- | :--- |
| **Selectively light dial ticks** | Draw-phase sampling (`drawBehind` + `CyberRadialField`) | API 24+ | Optimal (1 draw call) |
| **Light 6–12 discrete icons** | `Modifier.cyberRadialIllumination(field)` | API 24+ | Good (6–12 layers) |
| **Trace particles along letters** | Vector outline via `Paint.getTextPath()` | API 24+ | High (vector math) |
| **Hollow / wireframe font** | `TextStyle(drawStyle = Stroke(...))` | API 24+ | Native (zero overhead) |
| **Dynamic GPU neon rim glow** | AGSL `RuntimeShader` (Sobel kernel) | API 33+ | Fast (GPU fragment shader) |
| **Exterior-only halo** | `cyberTextGlow(outsideGlowOnly = true)` | API 31+ | Medium (offscreen layer) |
| **HUD word/character brackets** | `TextLayoutResult.getPathForRange()` | API 24+ | Fast (layout metadata) |
