# Contributing to CyberpunkAndroid

First off, thank you for considering contributing to CyberpunkAndroid! It's people like you that make open-source development such a great community.

## Development Workflow

1.  **Branching**: We use a feature-branch workflow. Create a new branch for your feature or bug fix (e.g., `feature/awesome-new-component` or `fix/button-glitch`).
2.  **Sample App**: Use the `:sample` module to test your changes. **Do not** introduce breaking changes to public APIs in `:cyberpunkandroid` unless absolutely necessary, and ensure the sample app still compiles.
3.  **Documentation**: When adding new public components or modifiers, please write clear KDoc comments. Describe the visual behavior, parameters, and defaults.
4.  **Pull Requests**: Submit your pull requests against the `main` branch. 

## Architectural Guidelines

*   **Modifiers**: Use the modern `Modifier.Node` API (specifically `ModifierNodeElement`, `DrawModifierNode`, `LayoutModifierNode`) instead of the deprecated `Modifier.composed` for stateful effects.
*   **Performance**: Effects should be as hardware-accelerated as possible. Utilize AGSL `RuntimeShader` on API 33+ and provide lightweight `graphicsLayer` fallbacks for older devices.
*   **Theming**: Do not hardcode colors or dimensions. Pull from `CyberTheme.colors` and `CyberPrimitives`.

## Reporting Bugs

Please use the GitHub Issue tracker to report bugs. Include your Android version, Jetpack Compose version, and a small snippet of code that reproduces the issue.
