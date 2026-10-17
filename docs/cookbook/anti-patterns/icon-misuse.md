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

---

## Anti-Pattern 3: Timid Sizing on Interactive Highlight Icons

### The Mistake
Using the smallest allowable icon size (`dp16`, `dp24`, or `dp32`) inside a primary interactive focal point (e.g. hold-to-charge actuators, critical toggle switches, or central hero buttons) simply because the compiler allows it.

### Incorrect
```kotlin
// DON'T: Tiny 24dp or 32dp icon buried inside a hero hold actuator
Box(modifier = Modifier.size(108.dp), contentAlignment = Alignment.Center) {
    CyberRim(progress = progress, modifier = Modifier.size(90.dp))
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .cyberLongPressFill(durationMillis = 1200L, onComplete = { ... })
    ) {
        CyberIcon(
            iconRes = CyberIcons.Lock,
            contentDescription = null,
            size = CyberPrimitives.IconSizes.dp24 // Anti-pattern: Whispers instead of screams "HOLD ME!"
        )
    }
}
```

### Problems Identified
1. **Weak Affordance:** The icon fails to register as the primary user interaction target. It reads as an incidental decorative label rather than an active actuator.
2. **Poor Ergonomic Proportion:** Placing a 24dp or 32dp glyph inside a 64dp+ button leaves excessive empty padding, reducing the optical clarity and prominence of the active state change.

### The Correct Implementation
```kotlin
// DO: Use dp64 for hero highlight actuators to scream "HOLD ME!"
Box(modifier = Modifier.size(144.dp), contentAlignment = Alignment.Center) {
    CyberDialTicks(
        size = 144.dp,
        strokeWidth = CyberPrimitives.BorderWidths.dp2,
        color = CyberTheme.colors.primary.copy(alpha = 0.75f)
    )
    CyberRim(
        progress = progress,
        modifier = Modifier.size(122.dp),
        strokeWidth = 6f
    )
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .cyberLongPressFill(durationMillis = 1200L, onComplete = { ... }),
        contentAlignment = Alignment.Center
    ) {
        CyberIcon(
            iconRes = CyberIcons.Lock,
            contentDescription = "Hold to bypass synaptic limiter",
            size = CyberPrimitives.IconSizes.dp64, // Hero Highlight: unmistakable interactive focal point
            tint = CyberTheme.colors.primary
        )
    }
}
```

