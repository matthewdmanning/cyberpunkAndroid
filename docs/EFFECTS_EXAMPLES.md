# CyberpunkAndroid Effects & Modifiers Usage Examples

This document serves as the reference guide for correctly applying visual effects and modifiers from the `cyberpunkandroid` library.

---

## Macro-Shaders & Screen-Space Distortion

> **Crucial Rule:** Macro-shaders sample the pixels rendered *inside* their `graphicsLayer`. Clipping (`clip = true`) is automatically enforced on shader layers to constrain rendering strictly within composable card boundaries.

### 1. Cyber Overload (`Modifier.cyberOverload`)
Applies hardware AGSL chromatic aberration distortion and horizontal slice displacement.

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .cyberOverload(intensity = 1.0f)
        .background(CyberTheme.colors.surfaceSecondary)
        .padding(16.dp),
    contentAlignment = Alignment.Center
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CyberIcon(
            iconRes = CyberIcons.Cpu,
            contentDescription = "Overload",
            size = CyberPrimitives.IconSizes.dp64,
            tint = CyberTheme.colors.primary
        )
        Text("SYSTEM OVERLOAD", style = CyberTheme.typography.terminal)
    }
}
```

### 2. Cyber Scanlines (`Modifier.cyberScanlines`)
Simulates cathode-ray TV scanlines over content. Always pair with a distinct border frame.

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .border(2.dp, CyberTheme.colors.primary, CutCornerShape(12.dp))
        .cyberScanlines(spacing = 6.dp, opacity = 0.5f)
        .background(CyberTheme.colors.surfaceSecondary, CutCornerShape(12.dp))
        .padding(16.dp),
    contentAlignment = Alignment.Center
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CyberIcon(
            iconRes = CyberIcons.Terminal,
            contentDescription = "Scanlines",
            size = CyberPrimitives.IconSizes.dp64,
            tint = CyberTheme.colors.primary
        )
        Text("SCANLINE DISPLAY", style = CyberTheme.typography.terminal)
    }
}
```

### 3. Cyber Noise (`Modifier.cyberNoise`)
Overlays procedural static noise grain texture over a solid fill.

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .cyberNoise(opacity = 0.40f)
        .background(CyberTheme.colors.surfaceSecondary)
        .padding(16.dp),
    contentAlignment = Alignment.Center
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CyberIcon(
            iconRes = CyberIcons.Bug,
            contentDescription = "Noise",
            size = CyberPrimitives.IconSizes.dp64,
            tint = CyberTheme.colors.primary
        )
        Text("STATIC NOISE GRAIN", style = CyberTheme.typography.terminal)
    }
}
```

### 4. Cyber CRT (`Modifier.cyberCrt`)
Applies fisheye barrel distortion and corner vignette.

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .cyberCrt()
        .background(CyberTheme.colors.primary.copy(alpha = 0.20f))
        .padding(16.dp),
    contentAlignment = Alignment.Center
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CyberIcon(
            iconRes = CyberIcons.Server,
            contentDescription = "CRT",
            size = CyberPrimitives.IconSizes.dp64,
            tint = CyberTheme.colors.primary
        )
        Text("CRT MONITOR", style = CyberTheme.typography.terminal)
    }
}
```

### 5. Cyber Spark (`CyberSpark` / `Modifier.cyberSpark`)
AGSL popcorn spark particles with parabolic downward gravity trajectories and initial high-luminosity flashes.

```kotlin
CyberSpark(
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .background(CyberTheme.colors.surfaceSecondary)
        .padding(16.dp),
    sparkCount = 32,
    intensity = 1.0f,
    speed = 1.2f,
    color = CyberTheme.colors.primary
) {
    CyberIcon(
        iconRes = CyberIcons.Zap,
        contentDescription = "Sparks",
        size = CyberPrimitives.IconSizes.dp64,
        tint = CyberTheme.colors.primary
    )
}
```

---

## Overlays & Glassmorphism

### 6. Cyber Backdrop Blur (`Modifier.cyberBackdropBlur`)
Blurs content drawn *behind* it in the Compose tree. Must be applied over background content.

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .background(CyberTheme.colors.surfaceSecondary)
        .cyberBackdropBlur(radius = 24.dp, tint = Color.Black.copy(alpha = 0.35f)),
    contentAlignment = Alignment.Center
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        CyberIcon(iconRes = CyberIcons.Eye, contentDescription = "Backdrop", size = CyberPrimitives.IconSizes.dp64, tint = CyberTheme.colors.secondary)
        Text("BACKGROUND CONTENT", style = CyberTheme.typography.terminal, color = CyberTheme.colors.secondary)
    }
}
```

---

## Glow Composables & Text Effects

### 7. GlowingText
Emissive text glyph bloom rendered via native `BlurMaskFilter` on Canvas (`GlowingText.kt`), producing a clean contour glow with zero rectangular box.

```kotlin
GlowingText(
    text = "test",
    fontSize = 56.sp,
    glowRadius = 24.dp,
    glowColor = CyberTheme.colors.primary,
    textColor = Color.White
)
```

### 8. CyberGlowIcon & CyberGlowIconPath
Outer-contour vector path glow icons in [CyberGlowIcon.kt](file:///C:/Users/mattm/AndroidStudioProjects/cyberpunkAndroid/cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberGlowIcon.kt) that project bloom along vector path outlines.

```kotlin
CyberGlowIconPath(
    painter = painterResource(id = CyberIcons.Zap),
    contentDescription = "Glow Icon Path",
    color = CyberTheme.colors.primary,
    glowColor = CyberTheme.colors.secondary,
    radius = 24.dp,
    intensity = 3f,
    modifier = Modifier.size(100.dp)
)
```

---

## Borders & Decorative Surfaces

### 9. Cyber Glow Border & Rounded Profile (`Modifier.cyberGlowBorder`, `Modifier.cyberGlowBorderRounded`)
Multi-pass soft atmospheric neon glow perimeters. Wrap with padding buffer to prevent glow truncation.

```kotlin
// Cut Corner Glow Border
Box(modifier = Modifier.padding(24.dp)) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .cyberGlowBorder(color = CyberTheme.colors.primary, shape = CutCornerShape(20.dp), glowRadius = 24.dp, width = 2.dp)
            .background(CyberTheme.colors.surfaceSecondary, shape = CutCornerShape(20.dp))
    )
}

// Rounded Profile Glow Border
Box(modifier = Modifier.padding(24.dp)) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .cyberGlowBorderRounded(color = CyberTheme.colors.secondary, cornerRadius = 24.dp, glowRadius = 24.dp, width = 2.dp)
            .background(CyberTheme.colors.surfaceSecondary, shape = RoundedCornerShape(24.dp))
    )
}
```

---

## Interactive Tactical Modifiers

### 10. Cyber Draggable (`Modifier.cyberDraggable`)
Handles drag gesture tracking and translates element position in real time inside `CyberDragDropProvider`.

```kotlin
CyberDragDropProvider {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Column(
            modifier = Modifier.cyberDraggable(data = "Module")
        ) {
            CyberIcon(iconRes = CyberIcons.Drag, contentDescription = "Drag", size = CyberPrimitives.IconSizes.dp64)
            Text("DRAG ME")
        }
    }
}
```

### 11. Cyber Long Press Fill (`Modifier.cyberLongPressFill`)
Charges continuously on touch/hold without resetting on finger micro-shifts.

```kotlin
var fillProgress by remember { mutableFloatStateOf(0f) }

CyberRim(
    progress = fillProgress,
    modifier = Modifier
        .size(140.dp)
        .cyberLongPressFill(
            durationMillis = 1500L,
            onProgressUpdate = { fillProgress = it },
            onComplete = { /* Charge completed */ }
        )
)
```
