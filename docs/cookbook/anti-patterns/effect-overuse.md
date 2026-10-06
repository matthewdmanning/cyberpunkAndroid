# Anti-Pattern: Decorative Effect Overuse

## The Mistake
Stacking multiple GPU shaders, infinite animations, and multi-pass blurs on a single component or repeatedly in a list.

### Incorrect
```kotlin
// DON'T: Overloading effects on a single item in a list
LazyColumn {
    items(itemsList) { item ->
        Box(
            modifier = Modifier
                .cyberCrt() // Heavy AGSL shader pass
                .cyberOverload(strength = 0.5f) // Second AGSL shader pass
                .cyberScanlines() // Third procedural pass
                .cyberTextGlow(glowRadius = 24.dp) // 3-layer BlurMaskFilter pass
                .cyberFloat() // Per-frame vertical translation
        ) {
            ItemContent(item)
        }
    }
}
```

### Problems Identified
1. **Severe Frame Drops:** Stacking 2+ AGSL runtime shaders with offscreen render layers per item in a `LazyColumn` will trigger continuous GPU rasterization and thermal throttling.
2. **Visual Clutter:** High visual noise compromises legibility and user focus.
3. **Redundant Math:** CRT barrel distortion and overload chromatic aberration fight each other visually.

---

## The Correct Implementation
```kotlin
// DO: Restrained, single-effect hierarchy
LazyColumn {
    items(itemsList) { item ->
        Box(
            modifier = Modifier
                .clip(CyberTheme.shapes.cardShape)
                .background(CyberTheme.colors.surfacePrimary)
                .cyberBorder(width = 1.dp, color = CyberTheme.colors.border)
                .padding(16.dp)
        ) {
            ItemContent(item)
        }
    }
}
```
*Note: Reserve heavy effects like `cyberCrt` or `cyberTextGlow` for one prominent screen-level or hero-level surface.*
