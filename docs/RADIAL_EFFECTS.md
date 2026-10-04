# Radial Effects

Light that moves **around** or **out from** a point: a radar sweep, a radial pulse, icons that glow as the
beam passes them, and a particle shower. Everything here works on any component, but it is built around the
dial (`CyberDialTicks`).

A live catalogue and the cookbook example are in the sample app under the **Radial** tab (`RadialEffectsScreen.kt`).

> **Verification status.** The math (angles, trails, stepped fades, gradient stops) is covered by JVM unit tests.
> The Compose layer has Robolectric tests, but at the time of writing none of the Compose code has been compiled or
> run on a device or emulator, and the default colors, durations and widths have not been tuned by eye.

## The model

Every radial effect has two halves:

| Half | What it is | Where |
| --- | --- | --- |
| **Driver** | Owns the timing and the angles: sector, direction, trail, fade, animation clock. Hoisted so several things can share it. | `rememberCyberRadarSweep`, `rememberCyberRadialPulse` |
| **Painter** | Draws the driver on a component, inside a *region* (origin, radii, clip). | `Modifier.cyberRadarSweep`, `Modifier.cyberRadialPulse` |

Anything can ask a driver how bright a point is (`CyberRadialField.intensityAt`), which is how
`Modifier.cyberRadialIllumination` lights an icon from the same clock as the beam.

All angles are **degrees clockwise from 12 o'clock**.

## Radar sweep

```kotlin
val sweep = rememberCyberRadarSweep(
    sector = CyberRadialSector(300f, 60f),   // a 120-degree fan over 12 o'clock
    mode = CyberSweepMode.BOUNCE,            // WRAP, BOUNCE or HOLD
    clockwise = true,
    tailDegrees = 60f,                       // 0 = line only
    fadeSteps = 5,                           // 0 = smooth, >0 = visibly discrete bands
    edgeFadeDegrees = 15f,                   // fade the beam in and out near the ends of the sector
)
Box(Modifier.size(200.dp).cyberRadarSweep(sweep, region = CyberRadialRegion(clipShape = CircleShape)))
```

- **Appearance:** a bright over-exposed line at the head with a glow, and behind it a wedge fading from the
  `wedgeAlpha` to transparent over `tailDegrees`.
- **WRAP** loops round; **BOUNCE** slows and turns at each end (the trail follows the beam's speed); **HOLD** goes once
  from the start to the end of the sector per loop.
- A full circle is the default sector. `CyberRadialSector(start, end)` wraps, so `(300, 60)` is 120 degrees.

## Radial pulse

```kotlin
val pulse = rememberCyberRadialPulse(
    style = CyberPulseStyle.SONAR,           // RING, DISC or SONAR
    direction = CyberRadialDirection.OUTWARD, // or INWARD
    fadeSteps = 0,
)
Box(Modifier.size(200.dp).cyberRadialPulse(pulse))
```

- **RING:** one thin ring with a soft trail. **DISC:** the same, with the area already crossed filled.
  **SONAR:** several staggered rings in flight at once.
- Use `sector` for a fan, `region.innerRadius` / `region.outerRadius` to limit the reach.
- Functional (tap) feedback must stay under 250 ms: pass a short finite `animationSpec` such as `tween(240)`.

## Icon illumination

```kotlin
CyberIcon(CyberIcons.Wifi, "Wifi", Modifier.cyberRadialIllumination(sweep))
```

The icon's silhouette glows (the `cyberTextGlow` contour glow) in proportion to the driver's brightness at the
icon's center. Nothing is drawn on the background. To light icons with an invisible beam use
`cyberRadarSweep(sweep, showWedge = false, headWidth = 0.dp)` on the container.

## Region: origin, radii and clipping

`CyberRadialRegion` is the same for sweep and pulse:

| Field | Meaning |
| --- | --- |
| `origin` | Center. `CyberRadialOrigin.Center`, `CyberRadialOrigin.fraction(x, y)`, or any lambda (it may read state, such as a touch position). |
| `innerRadius` | Nothing is drawn inside it, to keep a beam off the hub of a dial. |
| `outerRadius` | Where the effect ends. Unspecified reaches the farthest corner. |
| `clipShape` | What the effect is clipped to. Defaults to the component bounds; pass the component's shape, or `null` for none. |

## Particle shower

```kotlin
Modifier.cyberPathBorder(CyberParticleShower(count = 36, speeds = listOf(1, 2, 3)), shape = CircleShape)

Modifier.cyberPathAlong(CyberParticleShower(), path = { size -> myArc(size) })   // any path you build
```

Many small comets with hot heads and fading tails, in whole-number speed classes (faster = brighter), scattered
deterministically from `seed`. It is a normal `CyberPathEffect`, so it works with `cyberPathBorder`,
`cyberPathDivider` and the new `cyberPathAlong`, which takes a path you build yourself.

## Cookbook: build your own component

`RadarDial` in `RadialEffectsScreen.kt` is the template. The recipe:

1. Create **one driver** and pass it to everything that should stay in step.
2. Put the **painter** on the container, with a region that matches its shape.
3. Put **illumination** on each icon, with the same driver.
4. Use **your own `CyberRadialOrigin`** or **`CyberRadialField`** when the built-ins are not enough.

```kotlin
@Composable
fun RadarDial(sweep: CyberRadarSweep, icons: List<@Composable () -> Unit>) {
    Box(
        Modifier.size(220.dp).clip(CircleShape).background(CyberTheme.colors.background)
            .cyberRadarSweep(sweep, region = CyberRadialRegion(clipShape = CircleShape)),
        contentAlignment = Alignment.Center,
    ) {
        CyberDialTicks(size = 220.dp)
        icons.forEach { it() }   // each with Modifier.cyberRadialIllumination(sweep)
    }
}
```

`TouchPing` in the same file shows a pulse whose origin follows the finger.

## Accessibility

The radial modifiers are decorative. They add no accessibility node and keep the content's own description. Pass
`appendedA11y` or `customA11y` to give one. (The path modifiers always add a node; the radial ones do not, because they
are meant to sit on icons and containers whose own descriptions must not be replaced.)

## Not implemented yet (TODO)

- **Feathered sector edges:** the sector edge is a hard cut; a soft fade across it needs tuning on a device.
- **Nearby spill:** lighting neighbours of a lit icon with a soft halo on the background.
- **Steady glow:** holding a lit icon at a base level between passes.
- **Grid, continuous glow and particle physics options for the sweep** were deliberately left out.
