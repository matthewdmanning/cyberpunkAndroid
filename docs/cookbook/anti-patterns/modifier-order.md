# Anti-Pattern: Flawed Modifier Ordering

## The Mistake
Placing outer emissive glows after clipping operations, or placing clickable targets after content padding.

### Incorrect
```kotlin
// DON'T: Flawed modifier ordering
Box(
    modifier = Modifier
        .clip(CyberTheme.shapes.cardShape)
        .cyberGlowBorder(color = CyberTheme.colors.primary, glowRadius = 12.dp) // Glow is cut off!
        .padding(16.dp)
        .clickable(onClick = onClick) // Hit target is shrunken!
        .background(CyberTheme.colors.surfacePrimary) // Background covers border/glow!
)
```

### Problems Identified
1. **Glow Clipping:** `cyberGlowBorder` is placed after `clip(shape)`. Outer blur pixels outside the shape boundary are clipped and disappear.
2. **Reduced Touch Target:** `padding(16.dp)` before `clickable` reduces the interactive hit box by 16.dp on all sides.
3. **Layer Occlusion:** Applying `background` after `cyberGlowBorder` renders the background fill directly on top of earlier draw operations.

---

## The Correct Implementation
```kotlin
// DO: Strict canonical sequence
Box(
    modifier = Modifier
        // 1. Outer Glow (before clip)
        .cyberGlowBorder(color = CyberTheme.colors.primary, glowRadius = 12.dp)
        // 2. Shape clip
        .clip(CyberTheme.shapes.cardShape)
        // 3. Background surface
        .background(CyberTheme.colors.surfacePrimary)
        // 4. Border stroke
        .cyberBorder(width = 1.dp, color = CyberTheme.colors.border)
        // 5. Clickable hit target (full surface area)
        .clickable(onClick = onClick)
        // 6. Content padding (inner layout only)
        .padding(16.dp)
)
```
