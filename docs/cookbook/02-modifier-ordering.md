# Modifier Ordering and Composition

In Jetpack Compose, modifier functions produce an ordered, immutable sequence. The invocation order directly determines:
1. Touch event bounds and hit detection.
2. Drawing order and visual layer stacking.
3. Clipping boundaries for outer glows and shadows.

---

## 1. Canonical Ordering Sequence

Every composite Cyber modifier chain must follow this seven-step order:

```text
1. Sizing & Layout Constraints      (e.g., fillMaxWidth, height, size)
2. Outer Emissive Glows             (e.g., cyberTextGlow, cyberGlowBorder)
3. Shape Clipping                   (e.g., clip)
4. Background & Surface Fills       (e.g., background, cyberHoloBackground)
5. Surface FX & Edge Tracing        (e.g., cyberScanlines, cyberBorder, cyberLaserOutliner)
6. Touch Handling & Gestures        (e.g., clickable, cyberPress)
7. Inner Content Padding            (e.g., padding)
```

---

## 2. Order Constraints & Explanations

### Constraint A: Emissive Glows vs Clipping
```yaml
ordering:
  effect: cyberGlowBorder
  before:
    - clip
  order_sensitive: true
```
- **Rationale:** If `clip(shape)` precedes `cyberGlowBorder()`, the outer emissive blur is clipped by the node boundary, rendering the glow invisible or producing hard-cropped rectangular artifacts.

```kotlin
// PREFERRED: Outer bloom radiates freely
Modifier
    .cyberGlowBorder(color = CyberTheme.colors.primary, glowRadius = 8.dp, shape = CyberTheme.shapes.cardShape)
    .clip(CyberTheme.shapes.cardShape)
    .background(CyberTheme.colors.surfacePrimary)

// FAILS: Glow is cut off at the edge
Modifier
    .clip(CyberTheme.shapes.cardShape)
    .cyberGlowBorder(color = CyberTheme.colors.primary, glowRadius = 8.dp, shape = CyberTheme.shapes.cardShape) // CLIPPED!
    .background(CyberTheme.colors.surfacePrimary)
```

---

### Constraint B: Background vs Border
```yaml
ordering:
  effect: cyberBorder / cyberGlowBorder
  after:
    - background
  order_sensitive: true
```
- **Rationale:** `cyberBorder` draws an outline stroke along the exact geometry of the shape. If `background` is applied after `cyberBorder`, the background fill will draw over interior-aligned borders.

```kotlin
// PREFERRED: Border renders cleanly over background fill
Modifier
    .clip(CyberTheme.shapes.cyberCutCornerShape)
    .background(CyberTheme.colors.surfacePrimary)
    .cyberBorder(width = 2.dp, color = CyberTheme.colors.border)

// FAILS: Background covers inner half of border stroke
Modifier
    .clip(CyberTheme.shapes.cyberCutCornerShape)
    .cyberBorder(width = 2.dp, color = CyberTheme.colors.border)
    .background(CyberTheme.colors.surfacePrimary)
```

---

### Constraint C: Interaction Touch Boundaries
```yaml
ordering:
  effect: clickable
  before:
    - padding (content padding)
  after:
    - clip
  order_sensitive: true
```
- **Rationale:** Calling `padding()` before `clickable()` shrinks the touch hit area. Calling `clip()` before `clickable()` ensures ripple animations and touch interactions stay within the cut-corner shape.

```kotlin
// PREFERRED: Full surface is tappable, ripple respects polygon shape
Modifier
    .clip(CyberTheme.shapes.cardShape)
    .background(CyberTheme.colors.surfacePrimary)
    .clickable(onClick = onSelect)
    .padding(16.dp)

// FAILS: Hit box is smaller than visible surface
Modifier
    .clip(CyberTheme.shapes.cardShape)
    .background(CyberTheme.colors.surfacePrimary)
    .padding(16.dp)
    .clickable(onClick = onSelect) // User cannot click within the 16.dp edge!
```

---

## 3. Order Reference Matrix

| Modifier | Must Precede | Must Follow |
| :--- | :--- | :--- |
| `cyberTextGlow` | `clip`, `background` | `size`, `fillMaxWidth` |
| `cyberGlowBorder` | `clip`, `background` | Sizing constraints |
| `clip` | `background`, `clickable` | Emissive glows |
| `cyberHoloBackground` | `cyberBorder`, `padding` | `clip` |
| `cyberBorder` | `clickable`, `padding` | `background` |
| `cyberLaserOutliner` | `clickable`, `padding` | `background` |
| `clickable` | Content `padding` | `clip`, `background` |
