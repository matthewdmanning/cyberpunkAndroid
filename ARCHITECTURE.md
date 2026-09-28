# Cyberpunk Android Architecture

In this architecture, the graphics and effects pipeline moves from low-level graphics primitives up to the high-level public API that developers actually use. 

Here is the breakdown of the responsibilities of each core effects file:

### 1. `utils/CyberBrushes.kt` (The Paint)
This is a low-level drawing utility class. It provides custom `Brush` implementations (like `cyberSweepGradient`) that interface directly with the Android Canvas or Compose graphics. 
- **Purpose:** It provides the exact "paint" used to draw shapes, gradients, and borders.
- **Why it's in `utils/`:** It doesn't know about UI components or state; it just provides raw coloring instructions.

### 2. `effects/CyberShaders.kt` (The GPU Math)
This file holds raw AGSL (Android Graphics Shading Language) code as string constants (like `CrtShader`) and wraps them into Android `RenderEffect` objects.
- **Purpose:** It handles complex pixel-by-pixel manipulations on the GPU (like barrel distortion, chromatic aberration, or overload glitches) that are too expensive or impossible to do with standard Canvas drawing. 
- **Relationship:** Like `CyberBrushes`, it is a low-level graphics tool, but for distortion rather than painting.

### 3. `effects/CyberInteraction.kt` (The Triggers)
This file contains the `CyberInteractionTrigger` enum and the logic to listen to an `InteractionSource` (Compose's system for tracking gestures).
- **Purpose:** It answers the question: *"When should this effect happen?"* It determines if an effect should activate based on the user hovering, pressing, or focusing on a component. 

### 4. `effects/CyberTelemetry.kt` (The Data Engine)
This is a specialized state-management engine for data-driven effects. 
- **Purpose:** Instead of standard time-based animations (like `tween` or `spring`), `CyberTelemetry` processes raw data streams (telemetry) over time. It allows UI effects to react continuously to incoming data with mathematical operations like temporal smoothing, decay, or capacitance.

### 5. `effects/CyberModifiers.kt` (The Public API)
This is the "conductor" that ties everything else together. It contains the actual `Modifier.cyber...` extension functions that developers apply to Compose UI components.
- **Purpose:** It is the high-level, declarative API. 
- **Relationship:** A modifier in this file will typically check **`CyberInteraction`** to see if it should run, request a brush from **`CyberBrushes`** or a distortion from **`CyberShaders`**, and potentially drive its animation state using **`CyberTelemetry`**. 

## Summary
`Brushes` and `Shaders` are the raw graphics. `Interaction` and `Telemetry` decide when and how those graphics animate. `Modifiers` packages them all into a single line of code that you can apply to a UI element.
