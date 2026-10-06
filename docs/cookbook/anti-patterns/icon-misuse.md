# Anti-Pattern: Icon Misuse and Variant Violations

## The Mistake
Applying `CyberIconVariant.Solid` to stroke-only vector glyphs, or selecting icons by literal visual resemblance rather than semantic intent.

### Incorrect
```kotlin
// DON'T 1: Solid variant applied to open stroke lines
CyberIcon(
    iconRes = CyberIcons.Check, // Single open checkmark path!
    contentDescription = "Done",
    variant = CyberIconVariant.Solid // Renders blank or broken artifact!
)

// DON'T 2: Visual guessing instead of semantic icon
CyberIcon(
    iconRes = CyberIcons.Bug, // Used for a system alert warning!
    contentDescription = "Warning"
)
```

### Problems Identified
1. **Broken Silhouette:** `CyberIconVariant.Solid` removes vector path stroke and applies a fill. Open lines (e.g., `Check`, `X`, `Minus`, `ArrowLeft`) have zero fill area and render as invisible or fragmented artifacts.
2. **Semantic Confusion:** Using `CyberIcons.Bug` for a general system warning breaks user expectations and automated semantic tests. Use `CyberIcons.Warning` instead.

---

## The Correct Implementation
```kotlin
// DO 1: Use Outline for line-based glyphs, or dedicated filled icons
CyberIcon(
    iconRes = CyberIcons.Check,
    contentDescription = "Task Complete",
    variant = CyberIconVariant.Outline
)

// For bookmarks/stars with solid fill, use dedicated icons:
CyberIcon(
    iconRes = CyberIcons.BookmarkFilled,
    contentDescription = "Saved Bookmark"
)

// DO 2: Use matching semantic icon
CyberIcon(
    iconRes = CyberIcons.Warning,
    contentDescription = "System Warning",
    variant = CyberIconVariant.Duotone,
    tint = CyberTheme.semantics.colors.warning
)
```
