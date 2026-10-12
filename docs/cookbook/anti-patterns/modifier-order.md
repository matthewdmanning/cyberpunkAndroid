# Anti-Pattern: Flawed Modifier Ordering & Semantics Collision

## The Mistakes
1. **Layout & Visual Order**: Placing outer emissive glows after clipping operations, applying background fills over borders, or placing clickable targets after content padding.
2. **Semantics & Accessibility Order**: Omitting native accessibility roles, baking dynamic states into string descriptions, failing to group compound rows with `mergeDescendants`, and allowing inner decorative indicators to pollute the semantic tree.

---

## 1. Visual & Touch Target Ordering

### Incorrect
```kotlin
// DON'T: Flawed visual and hit-target ordering
Box(
    modifier = Modifier
        .clip(CyberTheme.shapes.cardShape)
        .cyberGlowBorder(color = CyberTheme.colors.primary, glowRadius = 12.dp) // Glow is cut off!
        .padding(16.dp)
        .clickable(onClick = onClick) // Hit target is shrunken by padding!
        .background(CyberTheme.colors.surfacePrimary) // Background covers border/glow!
)
```

### Problems Identified
1. **Glow Clipping:** `cyberGlowBorder` is placed after `clip(shape)`. Outer blur pixels outside the shape boundary are clipped and disappear.
2. **Reduced Touch Target:** `padding(16.dp)` before `clickable` reduces the interactive hit box by 16.dp on all sides.
3. **Layer Occlusion:** Applying `background` after `cyberGlowBorder` renders the background fill directly on top of earlier draw operations.

### Correct
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
        // 5. Touch Handling & Accessibility Role
        .clickable(role = Role.Button, onClick = onClick)
        // 6. Content padding (inner layout only)
        .padding(16.dp)
)
```

---

## 2. Accessibility Semantics & Indicator Collision

### Incorrect
```kotlin
// DON'T: Synthetic string concatenation, missing roles, unmerged children, unpruned indicators
Row(
    modifier = Modifier
        .clickable { onExpandedChange(!expanded) }
        .semantics { 
            // ❌ Anti-pattern: String concatenation of dynamic state and component names
            contentDescription = "Diagnostics - ${if (expanded) "Expanded" else "Collapsed"}" 
        }
) {
    Text("DIAGNOSTICS")
    // ❌ Anti-pattern: Inner chevron left exposed, creating redundant second TalkBack focus target
    Text(
        text = if (expanded) "⌃" else "⌄",
        modifier = Modifier.cyberTextGlow(color = CyberTheme.colors.primary)
    )
}
```

### Problems Identified
1. **Redundant Focus Targets:** The row handles the click event, but the inner chevron is exposed as an isolated focus rectangle. TalkBack users must swipe through both the row and the chevron.
2. **String Concatenation Anti-Pattern:** Concatenating state into `contentDescription` prevents accessibility services from localizing, tracking state transitions, or vocalizing standard state announcements.
3. **Missing Native Roles & Actions:** Calling bare `.clickable` without `role = Role.Button` and `onClickLabel` forces TalkBack to guess the control type.
4. **Semantics Collision:** If an earlier modifier in the chain sets `contentDescription` (e.g. an unmanaged glow modifier), it clobbers downstream semantic labels.

### Correct
```kotlin
// DO: Group compound items, use native roles, dynamic stateDescription, and prune inner indicators
val actionLabel = if (expanded) "Collapse section" else "Expand section"

Row(
    modifier = Modifier
        .semantics(mergeDescendants = true) {
            this.stateDescription = if (expanded) "Expanded" else "Collapsed"
            this.onClick(label = actionLabel) {
                onExpandedChange(!expanded)
                true
            }
        }
        .toggleable(
            value = expanded,
            role = Role.Button,
            onValueChange = onExpandedChange
        )
        .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    // Natural visible text surfaces automatically
    Text(text = "DIAGNOSTICS", modifier = Modifier.weight(1f))

    // ✅ Correct: Inner decorative chevron stripped of semantics to avoid noise
    Text(
        text = if (expanded) "⌃" else "⌄",
        modifier = Modifier
            .clearAndSetSemantics { } // Pruned from accessibility tree
            .cyberTextGlow(color = CyberTheme.colors.primary)
    )
}
```
