# Android Directional Inlay / Shadow Reference

## Goal
Create recessed, carved, or inset-looking containers whose apparent lighting has a controllable direction.

## Primary Android Pattern
Jetpack Compose supports `Modifier.innerShadow()`. Directionality comes from the shadow offset. Multiple inner shadows can be layered to create a more convincing inset rim or opposing light/dark edges.

## Authoritative Example
Android's shadow API exposes an offset on the shadow definition:

```kotlin
.innerShadow(
    shape = RoundedCornerShape(20.dp),
    shadow = Shadow(
        radius = 10.dp,
        spread = 2.dp,
        color = Color(0x40000000),
        offset = DpOffset(x = 6.dp, y = 7.dp)
    )
)
```

The non-zero X/Y offset establishes the apparent light/shadow direction.

## References
- Android Developers — Add shadows in Compose: https://developer.android.com/develop/ui/compose/graphics/draw/shadows
- Android Developers — Compose styling fundamentals: https://developer.android.com/develop/ui/compose/styles/fundamentals

## Design Direction
Represent light direction, shadow radius, spread, opacity, and shape as configurable styling parameters. Avoid hard-coding a particular lighting direction into components.
