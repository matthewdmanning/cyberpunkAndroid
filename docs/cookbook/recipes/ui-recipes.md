# UI Implementation Recipes

This guide provides copyable examples for constructing sci-fi UIs from the ground up, organized by how you build a screen.

---

## 1. Background & Screen Styling

Recipes for setting up the foundational layers of a screen or main container.

---

## Example 1: The Holographic Terminal

**Intent:** Set up a sci-fi screen foundation with a holographic base and subtle scanlines.
**API:** `Modifier.cyberHoloBackground`, `Modifier.cyberScanlines`
**Tokens:** `CyberTheme.colors.background`

**Canonical example:**
```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(CyberTheme.colors.background)
        .cyberHoloBackground()
        .cyberScanlines(opacity = 0.3f)
) {
    // Screen content goes here
}
```

**Operations Note:**
- `background` must be applied before holographic or scanline overlays so the effects draw on top of the base color.

---

## Example 2: The Retro CRT Display

**Intent:** Create a vintage, curved-screen look with chromatic aberration for a specific view or full app.
**API:** `Modifier.cyberCrt`
**Tokens:** `CyberTheme.colors.background`

**Canonical example:**
```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .cyberCrt(trigger = CyberInteractionTrigger.ALWAYS)
        .background(CyberTheme.colors.background)
) {
    // Terminal content goes here
}
```

**Operations Note:** 
- `cyberCrt` applies a heavy shader. Limit this to exactly one full-screen or prominent container per screen.


## 2. Object & Icon Styling

Recipes for styling the static parts of the UI (cards, containers, and icons) sitting on top of the background.

---

## Example 3: The Neon Task Card

**Intent:** Frame a piece of content, like a task or metric, with signature cut corners and glowing edges.
**API:** `Modifier.clip`, `Modifier.cyberGlowBorder`, `Modifier.cyberBorder`
**Tokens:** `CyberTheme.shapes.cardShape`, `CyberTheme.colors.surfacePrimary`, `CyberTheme.colors.border`, `CyberTheme.colors.primary`

**Canonical example:**
```kotlin
Box(
    modifier = Modifier
        // 1. Outer emissive glow (must precede clip)
        .cyberGlowBorder(
            color = CyberTheme.colors.primary,
            glowRadius = 8.dp,
            shape = CyberTheme.shapes.cardShape
        )
        // 2. Shape boundary
        .clip(CyberTheme.shapes.cardShape)
        // 3. Surface fill
        .background(CyberTheme.colors.surfacePrimary)
        // 4. Inner border tracer
        .cyberBorder(
            width = 1.dp,
            color = CyberTheme.colors.border
        )
        .padding(16.dp)
) {
    // Task details here
}
```

**Operations Note:** 
- **Ordering is critical**: Outer glows (`cyberGlowBorder`) *must* be called before `clip`, otherwise the blur is clipped. Backgrounds *must* be called before inner borders (`cyberBorder`), otherwise the background covers the border.

---

## Example 4: The Emissive Hero Icon

**Intent:** Display a prominent, glowing icon for a status or hero header instead of a standard flat icon.
**API:** `CyberGlowIconPath`
**Tokens:** `CyberIcons.*`, `CyberPrimitives.IconSizes.dp48`, `CyberTheme.colors.primary`

**Canonical example:**
```kotlin
CyberGlowIconPath(
    iconRes = CyberIcons.Shield,
    contentDescription = "System Security Status",
    color = CyberTheme.colors.primary,
    glowRadius = 12.dp,
    modifier = Modifier.size(CyberPrimitives.IconSizes.dp48)
)
```

**Operations Note:**
- Use `CyberGlowIconPath` for standalone hero graphics. For standard inline buttons or lists, use the standard `CyberIcon`.


## 3. Reactive & State-Driven Styling

Recipes for making effects respond to app state variables or physical user touch. These cover the core operations of `CyberInteractionTrigger` and `MutableInteractionSource`.

---

## Example 5: The Tactile Action Surface

**Intent:** Provide visual glitch feedback specifically when a user physically presses a button or card.
**API:** `Modifier.cyberOverload`, `Modifier.clickable`, `CyberInteractionTrigger.PRESS`
**Tokens:** `CyberTheme.shapes.cyberCutCornerShape`

**Canonical example:**
```kotlin
val interactionSource = remember { MutableInteractionSource() }

Box(
    modifier = Modifier
        .clip(CyberTheme.shapes.cyberCutCornerShape)
        .background(CyberTheme.colors.surfacePrimary)
        // Link the effect to the PRESS state of the interaction source
        .cyberOverload(
            trigger = CyberInteractionTrigger.PRESS,
            interactionSource = interactionSource,
            intensity = 0.5f
        )
        // Link the clickable modifier to the same interaction source
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Disable default Android ripple
            onClick = onTaskClicked
        )
        .padding(16.dp)
) {
    Text("EXECUTE", color = CyberTheme.colors.textPrimary)
}
```

**Operations Note:**
- For touch-based effects, you *must* pass the same `MutableInteractionSource` to both the effect modifier and `clickable`.

---

## Example 6: The Active Process Beacon

**Intent:** Visually indicate that a background task is actively running by pulsing an element.
**API:** `Modifier.cyberPing`, `CyberInteractionTrigger`
**Tokens:** `CyberTheme.semantics.colors.info`

**Canonical example:**
```kotlin
// Dynamically set the trigger based on app state
val runTrigger = if (isLoading) CyberInteractionTrigger.ALWAYS else CyberInteractionTrigger.NONE

Box(
    modifier = Modifier
        .size(48.dp)
        .cyberPing(
            color = CyberTheme.semantics.colors.info,
            borderWidth = 2.dp,
            durationMillis = 1500,
            trigger = runTrigger
        ),
    contentAlignment = Alignment.Center
) {
    CyberIcon(
        iconRes = CyberIcons.Sync,
        contentDescription = if (isLoading) "Syncing..." else "Synced",
        tint = CyberTheme.semantics.colors.info
    )
}
```

---

## Example 7: The Critical Error Glitch

**Intent:** Intensely grab the user's attention when a form or task enters a failed state.
**API:** `Modifier.cyberOverload`
**Tokens:** `CyberTheme.semantics.colors.error`

**Canonical example:**
```kotlin
val isError = taskState == TaskState.ERROR
val borderColor = if (isError) CyberTheme.semantics.colors.error else CyberTheme.colors.border
val triggerState = if (isError) CyberInteractionTrigger.ALWAYS else CyberInteractionTrigger.NONE

Box(
    modifier = Modifier
        .clip(CyberTheme.shapes.cardShape)
        .background(CyberTheme.colors.surfacePrimary)
        .cyberBorder(width = 2.dp, color = borderColor)
        .cyberOverload(
            trigger = triggerState,
            intensity = 0.8f
        )
        .padding(16.dp)
) {
    // Error details or task content
}
```

**Operations Note:**
- You can dynamically change tokens (like `borderColor`) alongside `CyberInteractionTrigger` states to compound the reactive effect.


