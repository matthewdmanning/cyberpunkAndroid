# CyberpunkAndroid

A Cyberpunk-themed UI component and effects library for Jetpack Compose. 
It provides hardware-accelerated, futuristic visual effects and highly customizable UI elements out of the box.

## Installation

Add the JitPack repository to your `settings.gradle.kts` file:

```kotlin
maven { url = uri("https://jitpack.io") }
```

Add the dependency to your app-level `build.gradle.kts`:

```kotlin
implementation("com.github.matthewdmanning:cyberpunkAndroid:1.0.5")
```

## How do I use this to create usable components for the app?

The library is split into three core types of functionality. Before using them, always wrap your app content in `CyberTheme` to initialize the necessary design tokens and colors:

```kotlin
setContent {
    CyberTheme { /* Your app content */ }
}
```

### 1. Ready-to-use Components
If you need standard structural UI elements with a cyberpunk aesthetic, use the pre-built components. They are fully themed and handle their own interaction states out of the box.

```kotlin
// Example: A fully styled, interactive button
CyberButton(
    onClick = { launchSequence() }
) {
    Text("INITIALIZE SYSTEM", style = CyberTheme.typography.terminal)
}
```

### 2. Visual Effects (Modifiers)
If you want to apply futuristic styles to an *existing* Compose layout (like a standard `Box`, `Image`, or custom layout), attach our shader-backed modifiers.

```kotlin
// Example: Adding continuous scanlines and a press-triggered overload to a Box
Box(
    modifier = Modifier
        .size(200.dp)
        .cyberScanlines(speed = 0.5f)
        .cyberOverload(
            trigger = CyberInteractionTrigger.PRESS,
            intensity = 0.8f
        )
)
```

### 3. Themed Icons
The library includes custom cyberpunk vector icons that support built-in neon glows and varying stylistic weights.

```kotlin
// Example: A warning icon that projects a bloom effect
CyberGlowIcon(
    iconRes = CyberIcons.Warning,
    contentDescription = "System Alert"
)
```

## Performance Tips & Best Practices

- **Hardware Acceleration:** Under the hood, stateful effects (like CRT curves, overloads, and datastreams) leverage AGSL `RuntimeShader` on API 33+ devices. They execute entirely on the GPU and skip Compose recomposition phases. You can safely stack these modifiers on top-level screens.
- **Pre-API 33 Fallbacks:** For older devices, the library falls back to procedural Canvas drawing. While optimized, applying heavy visual modifiers to dozens of items in a deeply nested `LazyColumn` on an older device may impact frame rates. Apply them strategically to focal points.
- **Triggering Effects:** Use `CyberInteractionTrigger` effectively. Instead of running continuous animated effects (`ALWAYS`) on every item, bind heavy effects to `PRESS` or `FOCUS` states. This ensures they only consume rendering resources when the user is actively engaging with the component.
- **Keep it Lean:** The library's effects are built using the modern `Modifier.Node` API to guarantee zero allocations during recomposition. If you wrap these effects in your own custom modifiers, try to use `Modifier.Node` yourself or standard composable functions to preserve these performance gains.

## Documentation

For UI construction patterns, layout examples, and recipes targeted at AI agents (or humans building screens conceptually), see the [UI Agent Cookbook](docs/cookbook/README.md).

For a comprehensive breakdown of every available component, shader effect, and thematic token, see the [Full Reference Documentation](docs/reference/README.md).
