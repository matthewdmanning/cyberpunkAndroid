# Library API conventions

Use these conventions when you create or modify public APIs in the
`cyberpunkandroid` library. This document owns component and modifier signature
ordering, animation configuration, and base-component boundaries.

## Modifier signatures

Order parameters in `Modifier.cyber...` extensions as follows:

1. Required effect-specific parameters without defaults.
2. Visual and configuration parameters with defaults, such as `spacing`,
   `opacity`, `speed`, and `color`.
3. Interaction and state parameters, including `trigger` and
   `interactionSource`.
4. Animation specifications and transitions.
5. Accessibility parameters in this order:
   - `appendedA11y: String? = null`
   - `customA11y: String? = null`

The following signature shows the required ordering:

```kotlin
fun Modifier.cyberOverload(
    severity: Float = 0.5f,
    color: Color = Color.Unspecified,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = tween(300),
    exitAnimationSpec: AnimationSpec<Float> = tween(300),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier
```

## Composable signatures

Order parameters in `@Composable fun Cyber...` components as follows:

1. Required data and state.
2. Optional data and state.
3. `modifier: Modifier = Modifier` as the first optional parameter with a
   default.
4. Component configuration, such as sizes and thresholds.
5. Animation specifications and transitions.
6. Theme and color parameters.
7. Accessibility parameters in this order:
   - `appendedA11y: String? = null`
   - `customA11y: String? = null`
8. Callbacks and events.
9. Content slots, with the trailing content lambda last.

The following signature shows the required ordering:

```kotlin
@Composable
fun CyberProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Float> = spring(),
    appendedA11y: String? = null,
    customA11y: String? = null
)
```

## Component boundaries

- Keep base components functional and minimally styled. Add visual flair with
  `Modifier.cyber...` effects.
- Hoist `tween()`, `spring()`, `infiniteRepeatable()`, and delay values to the
  public function signature. Don't hard-code them inside `LaunchedEffect`,
  `animate*AsState`, or `AnimatedVisibility`.
- Prefer an existing native Android API, then an existing repository API or
  asset, before writing a replacement.
- Add a short line comment that explains each necessary hard-coded constant.
- Give every effect, modifier, shader, and shape a factual visual description.
  Ask the user when the intended appearance is ambiguous.
- Keep blur neutral unless the user explicitly requests a colored blur.

## Animation configuration

- Expose animation behavior through the public function signature. Callers must
  be able to replace durations, easing, springs, transitions, and repeating
  specifications.
- Reuse `CyberPrimitives.Durations` and `CyberConfig.Easings` when an existing
  token expresses the intended behavior.
- Treat the ranges below as design starting points, not values to hard-code
  inside an implementation. Preserve standard touch targets and layout
  distances while changing the motion and rendering style.
- Keep functional feedback, including taps and state changes, under 250 ms.
  Reserve longer glitch, scanline, and transition effects for non-blocking
  transitions or idle states.

### Motion starting points

The following ranges compare restrained motion with a stronger cyberpunk
variant:

| Effect | Standard starting point | Cyberpunk starting point |
| --- | --- | --- |
| Bounce and snap | 200–400 ms; soft spring (`k=200`, `c=15`); 4–12 dp translation; two or three smoothly decaying cycles | 120–250 ms; stiff spring (`k=450`, `c=10`); 8–16 dp translation with overshoot; one or two sharp rebounds |
| Pulse and breathe | 1,200–2,400 ms cycle; smooth sine curve; scale from 1.00 to 1.08; opacity fades toward 0.3–0.0 | 600–1,200 ms cycle; sharp ease-in-out or four-step motion; scale from 1.00 to 1.15 with outline expansion; opacity drops to a persistent wireframe edge |
| Glow and bloom | 1,000–2,000 ms ambient cycle; soft ease-out; 2–12 dp spread; one 4–16 dp Gaussian blur | 400–800 ms state-reactive cycle; rapid attack; 4–20 dp spread; crisp 90% core with a 2 dp blur plus a 30% ambient field with a 20 dp blur |
| Scanline, shimmer, and sweep | 1,200–1,800 ms sweep; soft easing; 15–20 degree gradient; faint highlight across about 35% of the width | 400–800 ms sweep; linear or refresh-pass timing; horizontal or 45-degree slice; bright highlight across about 8% of the width |
| Movement, opacity, and transition | 200–350 ms; standard ease-in or ease-out; 8–24 dp translation; smooth alpha transition | 80–150 ms; three-step or keyframed motion; 2–4 dp color-channel offset; 4–12 dp horizontal jitter; brief keyframed alpha flicker |

The spring symbols `k` and `c` describe relative stiffness and damping. Convert
them to the parameters required by the selected Compose animation API rather
than passing them as literal platform values.

### Motion intensity

- **Low:** Keep standard durations and smooth curves. Add a dual-layer neon
  bloom and a sharp, high-contrast border.
- **Medium:** Increase spring stiffness, reduce durations by about 30%, narrow
  scanline bands, and use stepped transitions for data updates.
- **High:** Add a 40–80 ms color-channel split on press, keyframed exits, and
  wireframe stroke entry animations. Use this level only when it doesn't delay
  functional feedback.
