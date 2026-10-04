# CyberpunkAndroid

A Cyberpunk-themed UI component and effects library for Jetpack Compose. 
It provides hardware-accelerated, futuristic visual effects and highly customizable UI elements out of the box.

## Installation

Add the JitPack repository to your `settings.gradle.kts` file:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.matthewdmanning:cyberpunkAndroid:1.0.5")
}
```

## Quick Start

CyberpunkAndroid is designed around two main concepts:
1. **Components**: Themed, structural building blocks (e.g., `CyberButton`, `CyberContainer`, `CyberTabs`).
2. **Modifiers**: High-performance, shader-backed visual effects you can attach to *any* Compose layout (e.g., `Modifier.cyberOverload()`, `Modifier.cyberScanlines()`).

### 1. Set up the Theme
Wrap your application in the `CyberTheme` to provide the necessary design tokens, colors, and typography to the library's components:

```kotlin
setContent {
    CyberTheme {
        // Your app content here
    }
}
```

### 2. Use Components & Modifiers
You can seamlessly combine Cyberpunk components with Cyberpunk modifiers to create highly interactive, futuristic UI elements:

```kotlin
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberContainer
import com.example.cyberpunkandroid.effects.cyberScanlines
import com.example.cyberpunkandroid.effects.cyberOverload
import com.example.cyberpunkandroid.theme.CyberTheme

@Composable
fun SystemBootScreen() {
    // A stylized container with a background scanline effect
    CyberContainer(
        modifier = Modifier.cyberScanlines(speed = 0.5f)
    ) {
        CyberButton(
            onClick = { /* Launch Sequence */ },
            text = "INITIALIZE",
            // Applies a heavy GPU glitch effect when interacted with
            modifier = Modifier.cyberOverload(intensity = 0.8f)
        )
    }
}
```

## Documentation

For a comprehensive breakdown of every available component, shader effect, and thematic token, see the [Full Reference Documentation](docs/reference/README.md).
