# Anti-Pattern: Hardcoded Raw Values

## The Mistake
Using literal numbers, raw hex colors, or custom CornerRadius shapes instead of design tokens.

### Incorrect
```kotlin
// DON'T: Hardcoded raw values and primitives
Box(
    modifier = Modifier
        .size(200.dp, 100.dp)
        .clip(CutCornerShape(20.dp))
        .background(Color(0xFF0D1117))
        .border(1.dp, Color(0xFF00E5FF))
) {
    Text(
        text = "TERMINAL",
        color = Color(0xFF00E5FF),
        fontSize = 14.sp
    )
}
```

### Problems Identified
1. **Bypasses Theme Dynamic Styling:** Hardcoded hex values will ignore runtime theme changes and high-contrast modes.
2. **Breaks Shape Harmony:** `CutCornerShape(20.dp)` cuts all four corners, whereas the design system signature cuts only `topEnd` and `bottomStart`.
3. **Typography Inconsistency:** Ignores the monospace / Neusharp font hierarchy defined in `CyberTheme.typography`.

---

## The Correct Implementation
```kotlin
// DO: Use semantic tokens from CyberTheme
Box(
    modifier = Modifier
        .size(200.dp, 100.dp)
        .clip(CyberTheme.shapes.cardShape)
        .background(CyberTheme.colors.surfacePrimary)
        .cyberBorder(width = 1.dp, color = CyberTheme.colors.border)
) {
    Text(
        text = "TERMINAL",
        style = CyberTheme.typography.labelMedium,
        color = CyberTheme.colors.textPrimary
    )
}
```
