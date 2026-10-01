# Android Compose Graphics-Layer Resource Optimization

## Core Principle
A `graphicsLayer` does not inherently mean Compose must rasterize content into a separate offscreen buffer. It can isolate recorded drawing instructions and allow efficient transform updates. Optimize the expensive rendering work, not layer count alone.

## Best Practices
- Prefer `CompositingStrategy.Auto`.
- Do not force `Offscreen` without a rendering reason.
- Treat `RenderEffect`, especially blur, as a higher-cost capability.
- Use graphics layers for translation, scale, rotation, alpha, and other transforms where layer isolation is useful.
- Group elements that always transform together; retain independent layers where independent animation benefits from them.
- Consider `ModulateAlpha` for suitable non-overlapping content.
- Keep gradients, fills, borders, highlights, and ordinary shadows in the normal drawing path when possible.
- Profile pixel-heavy effects on representative lower-performance devices.

## Authoritative Android Examples

### Plain graphics layer
Android documents that a plain layer can provide isolation without automatically forcing offscreen rasterization:

```kotlin
Canvas(
    modifier = Modifier
        .graphicsLayer()
        .size(100.dp)
) {
    drawRect(
        color = Color.Magenta,
        size = Size(200.dp.toPx(), 200.dp.toPx())
    )
}
```

### Forced offscreen compositing

```kotlin
Canvas(
    modifier = Modifier
        .graphicsLayer(
            compositingStrategy = CompositingStrategy.Offscreen
        )
        .size(100.dp)
) {
    drawRect(
        color = Color.Red,
        size = Size(200.dp.toPx(), 200.dp.toPx())
    )
}
```

`Offscreen` rasterizes to an intermediate buffer. Require a concrete rendering reason before forcing it.

### ModulateAlpha

```kotlin
Modifier.graphicsLayer {
    compositingStrategy = CompositingStrategy.ModulateAlpha
    alpha = 0.75f
}
```

This can avoid an offscreen alpha buffer, but overlapping draw operations can produce different visual results.

### Transform animation

```kotlin
Modifier.graphicsLayer {
    alpha = animatedAlpha
    scaleX = animatedScale
    scaleY = animatedScale
    translationX = animatedTranslationX
}
```

Graphics-layer transform changes can remain in the drawing phase, avoiding unnecessary composition/layout work.

## High-Cost Boundary: RenderEffect
Under normal compositing, effects such as `RenderEffect` can require offscreen rendering. Multiple large blur/effect layers therefore deserve more scrutiny than many simple transform layers.

## Review Checklist
Before introducing a lower-level graphics effect:
1. Can standard drawing, `Brush`, gradient, border, or shadow produce it?
2. Does it require layer isolation?
3. Does it trigger offscreen rendering?
4. Is `RenderEffect` involved?
5. How large is the affected pixel area?
6. How many instances can be visible simultaneously?
7. Does it animate?
8. Can related transforms share a parent layer?
9. Would `ModulateAlpha` preserve correctness?
10. Has the real UI been profiled?

## Official Sources
- Android Developers — Graphics modifiers: https://developer.android.com/develop/ui/compose/graphics/draw/modifiers
- Android Developers — Modifier phases and performance: https://developer.android.com/develop/ui/compose/performance/modifier-phases

## Documentation Rule
For lower-level or resource-intensive graphics techniques, include an authoritative Android/Jetpack example, the condition that causes the expensive path, a cheaper standard alternative when one exists, and any correctness constraint on the optimization.
