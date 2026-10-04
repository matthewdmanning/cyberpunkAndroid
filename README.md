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

## Using the Library

CyberpunkAndroid is designed around two main concepts:
1. **Components**: Themed, structural building blocks (e.g., `CyberButton`, `CyberContainer`, `CyberTabs`).
2. **Modifiers**: High-performance, shader-backed visual effects you can attach to *any* Compose layout (e.g., `Modifier.cyberOverload()`, `Modifier.cyberScanlines()`).

To get started, simply wrap your application content in `CyberTheme` to initialize the design tokens, colors, and typography. From there, you can drop in structural components and seamlessly chain Cyberpunk modifiers onto them to create highly interactive, futuristic UIs.

## Performance Tips & Best Practices

- **Hardware Acceleration:** Under the hood, stateful effects (like CRT curves, overloads, and datastreams) leverage AGSL `RuntimeShader` on API 33+ devices. They execute entirely on the GPU and skip Compose recomposition phases. You can freely stack these modifiers on top-level screens without lagging the UI thread.
- **Pre-API 33 Fallbacks:** For older devices, the library automatically falls back to procedural Canvas drawing. While optimized, applying heavy visual modifiers to dozens of items in a deeply nested `LazyColumn` on an older device may impact frame rates. Apply them strategically to focal points.
- **Triggering Effects:** Use `CyberInteractionTrigger` effectively. Instead of running continuous animated effects (`ALWAYS`) on every single item, bind heavy effects to `PRESSED` or `FOCUSED` states using an `InteractionSource`. This ensures they only consume rendering resources when the user is actively engaging with the component.
- **Keep it Lean:** The library's effects are built using the modern `Modifier.Node` API to guarantee zero allocations during recomposition. If you wrap these effects in your own custom modifiers, try to use `Modifier.Node` yourself or standard composable functions, avoiding the deprecated `Modifier.composed` anti-pattern to preserve these performance gains.

## Documentation

For a comprehensive breakdown of every available component, shader effect, and thematic token, see the [Full Reference Documentation](docs/reference/README.md).
