# CyberpunkAndroid

A Cyberpunk-themed UI component and effects library for Jetpack Compose.

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

## Documentation

See the [docs/reference/README.md](docs/reference/README.md) for full component and modifier references.
