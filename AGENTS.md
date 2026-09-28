# Agent Instructions & Project Conventions

This document outlines project-wide conventions that all agents and subagents MUST follow when modifying or creating code in the `cyberpunkandroid` library.

## Strict Prohibition on Canvas & Painter

- **NEVER use low-level Canvas or native Canvas drawing calls (`drawIntoCanvas`, `nativeCanvas`, `Canvas`, `@Painter` / `Painter`)**.
- Low-level Canvas and `Painter` bypass Compose hardware acceleration pipelines, ignore `RenderEffect` AGSL runtime shaders, and break layout scaling/layering.
- Always use pure Jetpack Compose layout primitives, `DrawScope` high-level drawing methods (`drawWithCache`, `drawWithContent`, `drawBehind`), `graphicsLayer`, and AGSL `RuntimeShader`s.
- Always consult the `context7-mcp` skill for official Jetpack Compose and library API documentation before implementing drawing logic or effects.

## Signature Ordering Convention: Composables, Effects, and Modifiers

To maintain strict consistency across the library (especially for our minimal base classes and composable effects), parameter signatures MUST adhere to the following order. This ensures predictable APIs and that all animation specs and accessibility configurations are properly exposed.

### 1. Modifier Extensions (`Modifier.cyber...`)

1. **Required effect-specific parameters** (no default values).
2. **Visual & Configuration parameters** with default values (e.g., `spacing: Dp`, `opacity: Float`, `speed: Float`, `color: Color`).
3. **Interaction & State parameters** (e.g., `trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS`, `interactionSource: InteractionSource? = null`).
4. **Animation Specs & Transitions** (e.g., `animationSpec: AnimationSpec<Float> = tween(300)`, `exitAnimationSpec: AnimationSpec<Float> = tween(300)`).
5. **Accessibility parameters** (Must ALWAYS be the last parameters before the block):
   - `appendedA11y: String? = null`
   - `customA11y: String? = null`

**Example:**
```kotlin
fun Modifier.cyberOverload(
    // 1 & 2. Visual & Config
    severity: Float = 0.5f,
    color: Color = Color.Unspecified,
    // 3. Interaction
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    // 4. Animation Specs
    animationSpec: AnimationSpec<Float> = tween(300),
    exitAnimationSpec: AnimationSpec<Float> = tween(300),
    // 5. Accessibility
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier
```

### 2. Composable Components (`@Composable fun Cyber...`)

1. **Required Data/State** (e.g., `text: String`, `progress: Float`).
2. **Optional Data/State** (e.g., `label: String? = null`).
3. **`modifier: Modifier = Modifier`** (Always the first optional parameter with a default).
4. **Component Configuration** (e.g., sizes, thresholds).
5. **Animation Specs & Transitions** (e.g., `animationSpec: AnimationSpec<Float> = spring()`, `enter: EnterTransition = fadeIn()`).
6. **Theming & Colors** (e.g., `color: Color = ...`).
7. **Accessibility parameters**:
   - `appendedA11y: String? = null`
   - `customA11y: String? = null`
8. **Callbacks & Events** (e.g., `onClick: () -> Unit`).
9. **Content Slots** (e.g., `content: @Composable () -> Unit` as the final trailing lambda).

**Example:**
```kotlin
@Composable
fun CyberProgress(
    // 1. Required State
    progress: Float,
    // 3. Modifier
    modifier: Modifier = Modifier,
    // 5. Animation Specs
    animationSpec: AnimationSpec<Float> = spring(),
    // 7. Accessibility
    appendedA11y: String? = null,
    customA11y: String? = null
)
```

## Architecture Requirements

- **Minimal Base Classes:** Components should have minimal stylistic bloat and act as functional base classes. Visual flair is added via `Modifier.cyber...` effects.
- **Zero Hardcoded Animations:** No `tween()`, `spring()`, `infiniteRepeatable()`, or `delay()` should be hardcoded inside a `LaunchedEffect`, `animate*AsState`, or `AnimatedVisibility` block. They MUST be hoisted to the function signature following the ordering rules above.

## Code Quality

- **NEVER Reinvent the Wheel** If there's an existing native Android, already imported, or repo asset -- in that order -- that can be used, do no write new code. 
- **Documentation** Justification for hard-coding any constant must be briefly documented in a line comment.
- **Visual Descriptions**: All effects, modifiers, shaders, and shapes must include a description of the visual appearance. Do not invent descriptions: ask a human if there is any ambiguity.
- **Blur Usage**: Blurs should always be clear unless the user explicitly specifies that they want the blur itself to be colored.

## Agent skills

### Issue tracker

Issues and specs for this repo live as markdown files in .scratch/. See docs/agents/issue-tracker.md.

### Domain docs

Single-context layout (one CONTEXT.md at root). See docs/agents/domain.md.
