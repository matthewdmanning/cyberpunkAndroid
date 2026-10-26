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
