# Android Material Surface Appearance Reference

## Purpose
References for altering Compose component surfaces so they can suggest glass, metal, or plastic without introducing a custom rendering system.

## Official Android References
- Shadows: https://developer.android.com/develop/ui/compose/graphics/draw/shadows
- Blur: https://developer.android.com/reference/kotlin/androidx/compose/ui/draw/blur
- Styling fundamentals: https://developer.android.com/develop/ui/compose/styles/fundamentals
- GraphicsLayer: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/layer/GraphicsLayer

## Material Recipes
**Glass:** transparency + gradient/highlight + blur + subtle border/shadow.

**Metal:** directional gradients + sharp highlights + layered inner/drop shadows.

**Plastic:** smooth gradients + specular-style highlight + softer shadows + optional translucency.

## Authoritative Examples
Android's `innerShadow` supports directional offsets, providing a standard primitive for recessed surfaces. Android also demonstrates gradient-backed shadows using a `Brush`, and its realistic shadow example layers `dropShadow()` and `innerShadow()` to create what the documentation describes as a metallic rim effect.

Example pattern:

```kotlin
.dropShadow(
    shape = RoundedCornerShape(70.dp),
    shadow = Shadow(
        radius = 10.dp,
        spread = animatedSpread.dp,
        brush = Brush.sweepGradient(colors),
        offset = DpOffset.Zero,
        alpha = animatedAlpha
    )
)
```

Source: https://developer.android.com/develop/ui/compose/graphics/draw/shadows

## Resource-Intensity Rule
Prefer `Brush`, gradients, borders, shadows, alpha, and ordinary drawing before lower-level effects. Use `graphicsLayer` for layer semantics/transforms and `RenderEffect` only when the desired effect genuinely requires it.

Keep material appearances as configurable styling recipes rather than embedding glass/metal/plastic behavior into individual components.
