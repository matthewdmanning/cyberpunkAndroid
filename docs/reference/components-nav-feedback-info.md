# Navigation, feedback & information components

Reference for `CyberBreadcrumbs`, `CyberNavRail`, `CyberStepper`, `CyberBanner`, `CyberTimeoutBar`, `CyberReadout`, `CyberDataList`, and `CyberCountBadge`. Source: `cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/`. KDoc owns exact parameters.

> Visual descriptions are read from the source code. They are not yet checked on a device.

## Design rules for this set

1. **Effects mark the function.** Each component has at most one effect. The effect shows the component's job (a selection moves, a value updates, time runs out). There is no idle animation.
2. **Effects are separate.** Each effect is a public modifier or hook in `effects/` (`rememberCyberChangePulse`, `cyberChangeFlash`, `cyberChangeBump`, `rememberCyberCountdown`, `cyberLeaderLine`, `cyberCornerBrackets`). Components only compose them. Each component exposes an on/off flag and its `AnimationSpec`.
3. **Shapes follow the job.** Edge-to-edge bars are rectangles. Step nodes are true circles (`CircleShape`). Counts are pills (`roundedFull`). Readouts are rectangles with corner brackets. No parallelogram default. No hexagon in place of a circle.
4. **Tokens follow the property.**

| Property | Token source |
| --- | --- |
| Padding, gaps, row pitch | `CyberTheme.spacing` |
| Stroke, outline, indicator thickness | `CyberPrimitives.BorderWidths` |
| Icon, node, badge minimum size | `CyberPrimitives.IconSizes` |
| Status meaning (fill, accent, text on fill) | `CyberTheme.status.<role>.color / container / onContainer / onColor` |
| Selection, links, muted text, frames | `CyberTheme.colors.primary / textSecondary / border` |
| Surface tiers (rail, selected slot, readout) | `CyberTheme.colors.surfaceSecondary / surfaceElevated` |
| Durations, easing | `CyberPrimitives.Durations`, `CyberConfig.Easings` |
| Effect strengths | `CyberConfig.Effects` |
| Touch target | Material 3 `minimumInteractiveComponentSize()` / `IconButton` |

## Navigation

### `CyberBreadcrumbs`

**Looks like:** `ROOT › SECTOR 7 › NODE 42` in terminal type. Ancestors in `textSecondary`; current location in `primary`; chevron separators in `border`.

**Behavior:** Ancestors are buttons (`onSegmentClick(index)`). The current location is not clickable; state = "Current location". Past `maxVisibleSegments` (default 4) the middle collapses to `…`; a tap expands the full trail. No effect.

### `CyberNavRail`

**Looks like:** A vertical column of icon-over-label slots on `surfaceSecondary`. The selected slot has a `surfaceElevated` fill and a straight `primary` bar on the start edge. Optional `CyberCountBadge` on each icon (`CyberNavRailItem.badgeCount`).

**Effect:** The start-edge bar slides to the new slot (`indicatorAnimationSpec`, default `spring()`), through `graphicsLayer`. Colors cross-fade (`colorAnimationSpec`, 150 ms).

**Semantics:** Selectable group; each slot is `Role.Tab`.

### `CyberStepper`

**Looks like:** Circular nodes on a straight connector, labels under the nodes. Completed: filled `primary` with a check. Current: `primary` outline with the number. Upcoming: `border` outline, `textSecondary` number.

**Effect:** The active part of the connector extends to the new current step (`connectorAnimationSpec`, 300 ms `CyberEasing`). Read in the draw phase only.

**Behavior:** With `onStepClick`, completed steps are buttons. Spoken states come from `stateDescriptions`.

## Feedback

### `CyberBanner`

**Looks like:** A full-width rectangle in `status.<role>.container`, a `status.<role>.color` stripe on the start edge, the variant icon, optional title, message in `onContainer`, optional `actions`, optional dismiss `X`.

**Effect:** On a message change while visible, the bar flashes once in the status color (`highlightMessageChange`, `flashAnimationSpec`). Show/hide expands and collapses vertically (`enterTransition`, `exitTransition`).

**Semantics:** Polite live region; assertive for `CyberAlertVariant.Error`.

**Use instead of `CyberSnackbar` when:** the condition persists, needs actions, or must not time out.

### `CyberTimeoutBar`

**Looks like:** A `BorderWidths.dp2` line. A `primary` fill drains toward the start edge over a `surfaceElevated` track. Below `warningFraction` (0.25) the fill is `status.warning.color`.

**Behavior:** Display only. Drive it with `rememberCyberCountdown`. Hidden from screen readers unless `customA11y` is set; give the timed element a text alternative.

```kotlin
var visible by remember { mutableStateOf(true) }
val remaining = rememberCyberCountdown(durationMillis = 6_000, running = visible, onTimeout = { visible = false })
CyberSnackbar(title = "Saved", message = "Profile synced", visible = visible)
CyberTimeoutBar(remainingFraction = { remaining.value })
```

## Information

### `CyberReadout`

**Looks like:** A `surfaceSecondary` rectangle with `border` corner brackets. Uppercase label, display-type value, terminal-type unit on the value baseline, optional delta row (arrow + text).

**Delta color:** `deltaSentiment`, not `trend`: Positive → `status.success`, Negative → `status.danger`, Neutral → `textSecondary`. "Up" can be bad (latency).

**Effect:** On a value change, the value flashes once in `primary` (`highlightChanges`). The first value does not flash.

**Semantics:** One merged node. `announceChanges = true` adds a polite live region.

### `CyberDataList`

**Looks like:** Key (`textSecondary`, uppercase) … dotted leader (`border`) … value (`textPrimary`, end-aligned) per row. No container, no motion. The leader sits on the first-line baseline.

**Per-row color:** `CyberDataEntry.valueColor` (for example a status color).

### `CyberCountBadge`

**Looks like:** A `status.danger.color` pill with an `onColor` number in `labelSmall`. One digit gives a circle (`roundedFull` + square minimum `IconSizes.dp16`). `> maxCount` shows `99+`. `count <= 0` draws nothing.

**Effect:** On an increase only, one bump (`cyberChangeBump`, 300 ms). Decreases do not animate.

---

## Holographic set

Same UX jobs as the components above, in a digital, cybernetic and holographic style. Each one pairs with a base component.

| Holographic | Base | Shared job |
| --- | --- | --- |
| `CyberTraceBreadcrumbs` | `CyberBreadcrumbs` | Show location in a hierarchy |
| `CyberBeamNavBar` | `CyberNavRail` | Switch top-level destinations (phone bottom bar) |
| `CyberTraceStepper` | `CyberStepper` | Show progress through steps |
| `CyberHoloBanner` | `CyberBanner` | Persistent notice |
| `CyberRingTimer` | `CyberTimeoutBar` | Time left before expiry |
| `CyberSpectrumReadout` | `CyberReadout` | One key metric |

### Visual language

- **Light, not boxes.** Edges are lines that fade (horizon lines), emitters that cast a beam, traces with nodes, and rulers. No component draws a closed frame.
- **One holographic brush.** Iridescent color comes from `CyberHoloBrushSource`: an AGSL shader on API 33+, a repeating linear gradient below. Its palette is the theme's three accents (`cyberHoloPalette()`: primary, secondary, success).
- **Subtle movement.** The brush drifts at 0.1 cycles/s (`CyberConfig.Effects.HoloDriftCyclesPerSecond`). Each component takes `driftCyclesPerSecond`; `0f` stops it. Other motion is one-shot and tied to a state change.
- **Geometry.** Nodes and rings are true circles. Bars and lines are straight. No parallelograms, no hexagons.

### `CyberTraceBreadcrumbs`

**Looks like:** Labels with no separators over one continuous holographic trace. Ancestors have small `border` dots on the trace. The current location has a holographic ring, where the trace ends.

**Motion:** Brush drift only.

### `CyberBeamNavBar`

**Looks like:** Equal-width icon-over-label slots on `surfaceSecondary`, with a `border` hairline on top. Over the selected slot: a short holographic emitter line on the top edge and a `primary` beam that fades down behind the icon (`cyberBeam`).

**Motion:** On selection change, the beam slides to the new slot (`indicatorAnimationSpec`). An afterimage stays at the old slot and fades (`rememberCyberEcho`, `echoFadeSpec`, 500 ms).

### `CyberTraceStepper`

**Looks like:** A header with the current label (start) and `2/4` (end). Under it, one trace with evenly spaced circular nodes: completed = small filled dot; current = ring inside a thinner reticle ring; upcoming = hollow `border` ring. The completed part of the trace is holographic.

**Motion:** The holographic part extends to the new node (`traceAnimationSpec`, 300 ms).

**Semantics:** One progress-bar node, state "Step 2 of 4, Verify".

### `CyberHoloBanner`

**Looks like:** No box. Content between top and bottom status-color lines that fade at both ends, over a half-transparent status container with a faint beam from the top.

**Motion:** On appearance, a scan line projects the banner from the start edge to the end edge (`cyberMaterialize`, `materializeSpec`, 500 ms). No idle motion.

### `CyberRingTimer`

**Looks like:** A circle of `segments` (24) ticks. Remaining time lights ticks clockwise from 12 o'clock with the holographic brush; below `warningFraction` they are `status.warning.color`. A center slot holds content (for example the seconds left).

**Motion:** Drain plus brush drift. Drive it with `rememberCyberCountdown`.

### `CyberSpectrumReadout`

**Looks like:** No panel. Label, value (display type), unit on the baseline. Under them, a holographic baseline with a tick ruler hanging from it (`cyberTickEdge`). Delta row as in `CyberReadout`.

**Motion:** On a value change, cyan and magenta echoes of the digits split and converge (`CyberChromaEchoText`, `echoAnimationSpec`, 300 ms).
