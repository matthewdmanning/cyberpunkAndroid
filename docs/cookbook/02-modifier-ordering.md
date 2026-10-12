# Modifier Ordering and Composition

In Jetpack Compose, modifier functions produce an ordered, immutable sequence. The invocation order directly determines:
1. Touch event bounds and hit detection.
2. Drawing order and visual layer stacking.
3. Clipping boundaries for outer glows and shadows.

---

## 1. Canonical Ordering Sequence

Every composite Cyber modifier chain must follow this eight-step order:

```text
1. Sizing & Layout Constraints      (e.g., fillMaxWidth, height, size)
2. Semantic Grouping & Overrides    (e.g., semantics(mergeDescendants = true), cyberComponentSemantics)
3. Outer Emissive Glows             (e.g., cyberTextGlow, cyberGlowBorder)
4. Shape Clipping                   (e.g., clip)
5. Background & Surface Fills       (e.g., background, cyberHoloBackground)
6. Surface FX & Edge Tracing        (e.g., cyberScanlines, cyberBorder, cyberLaserOutliner)
7. Touch Handling & Native Roles    (e.g., clickable(role = Role.Button), toggleable, selectable)
8. Inner Content Padding            (e.g., padding)
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
    .clickable(role = Role.Button, onClick = onSelect)
    .padding(16.dp)

// FAILS: Hit box is smaller than visible surface
Modifier
    .clip(CyberTheme.shapes.cardShape)
    .background(CyberTheme.colors.surfacePrimary)
    .padding(16.dp)
    .clickable(onClick = onSelect) // User cannot click within the 16.dp edge!
```

---

### Constraint D: Semantics Grouping, Roles & Indicator Suppression
```yaml
ordering:
  effect: semantics / clearAndSetSemantics
  guideline:
    - container: semantics(mergeDescendants = true)
    - interactions: provide native Role and onClickLabel
    - indicators: clearAndSetSemantics on decorative inner chevrons/icons
  order_sensitive: true
```
- **Rationale:** Screen readers must perceive compound widgets as unified controls without code names or redundant nested focus targets. Dynamic states must use `stateDescription` instead of string concatenation. Inner decorative indicators handled by the parent container must be stripped with `Modifier.clearAndSetSemantics { }`.

```kotlin
// PREFERRED: Unified focus target, native role, dynamic stateDescription, pruned inner chevron
Row(
    modifier = Modifier
        .semantics(mergeDescendants = true) {
            stateDescription = if (expanded) "Expanded" else "Collapsed"
            onClick(label = if (expanded) "Collapse section" else "Expand section") {
                onToggle()
                true
            }
        }
        .toggleable(value = expanded, role = Role.Button, onValueChange = { onToggle() })
        .padding(12.dp)
) {
    Text("DIAGNOSTICS", modifier = Modifier.weight(1f))
    Text("⌄", modifier = Modifier.clearAndSetSemantics { }) // Pruned
}
```

---

## 3. Order Reference Matrix

| Modifier | Must Precede | Must Follow |
| :--- | :--- | :--- |
| `semantics(mergeDescendants = true)` | Emissive glows, background, clip | Layout constraints |
| `cyberTextGlow` | `clip`, `background` | `size`, `fillMaxWidth` |
| `cyberGlowBorder` | `clip`, `background` | Sizing constraints |
| `clip` | `background`, `clickable` | Emissive glows |
| `cyberHoloBackground` | `cyberBorder`, `padding` | `clip` |
| `cyberBorder` | `clickable`, `padding` | `background` |
| `cyberLaserOutliner` | `clickable`, `padding` | `background` |
| `clickable` / `toggleable` / `selectable` | Content `padding` | `clip`, `background` |
| `clearAndSetSemantics` | Parent row content arrangement | Decorative inner elements only |
