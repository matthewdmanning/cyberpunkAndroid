# Path Effects: Tracers & Box Patterns

Light and geometry that run **along** a component's outline or a divider line. They're built from
Compose `PathEffect`s (dash, stamped, chained) plus Android's `DiscretePathEffect`, and drawn with
a neon glow.

- **Tracers** move light along the path: comets, charges, arcs, packets.
- **Box patterns** are geometric border styles: brackets, ticks, barcode, hazard band, and so on.
  They march along the outline while animating.

```kotlin
val card = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp)

Box(
    Modifier
        .background(CyberTheme.colors.surfaceSecondary, card)
        .cyberPathBorder(CyberCometTracer(), shape = card)            // glowing comet round the card
)

Box(Modifier.fillMaxWidth().height(24.dp).cyberPathDivider(CyberScanner()))   // KITT divider

// Only while pressed, with an eased lap (surge, then settle):
Modifier.cyberPathBorder(
    CyberCometTracer(count = 2, tail = 0.2f),
    shape = card,
    trigger = CyberInteractionTrigger.PRESS,
    interactionSource = interaction,
    hideWhenIdle = true,
    animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing)),
)

// Static pattern (no motion):
Modifier.cyberPathBorder(CyberGraduatedTicks(), shape = CircleShape, trigger = CyberInteractionTrigger.NONE)
```

A live catalogue is in the sample app under the **Paths** tab (`PathEffectsScreen.kt`).

## Modifiers

| Modifier | Draws along | Notes |
|---|---|---|
| `Modifier.cyberPathBorder(effect, color, shape, glowRadius, inset, steps, trigger, interactionSource, hideWhenIdle, animationSpec, …)` | the `shape` outline, over the content | Auto-insets by the effect's extent. Moves the outline's start to the middle of its longest side, so seams never sit on a corner. |
| `Modifier.cyberPathDivider(effect, color, alignment, horizontalInset, glowRadius, steps, …)` | a horizontal line across the component | An open path, so tracers enter at the start and exit at the end. |

- `animationSpec` drives progress 0 → 1. An infinite spec loops. A finite spec (for example `tween(900)`) plays once and holds; use `fadeOut = false` on `CyberDrawOn`, `CyberCornerCharge` and `CyberChargeMeter` for one-shots.
- `steps > 0` quantizes progress into that many steps per cycle, for stepped HUD motion.
- `trigger` controls **motion**. While inactive, the effect is drawn at progress 0, or hidden with `hideWhenIdle = true`.
- **Ordering:** apply `cyberPathBorder` *outside* (before) macro-shaders such as `cyberOverload`, so the shader doesn't distort the border.

## Tracers

| Effect | What you see | Key parameters |
|---|---|---|
| `CyberCometTracer` | A white-hot head with a colored tail that fades behind it, running the loop. Optional stutter: stepped jumps with fading afterimages. | `count`, `tail` (fraction of length), `stutter`, `ghosts`, `trackAlpha` |
| `CyberCornerCharge` | Light grows out of every corner in both directions until the outline closes, flashes white-hot, then fades. Circles charge from four points. | `head`, `fadeOut` |
| `CyberDrawOn` | The outline traces itself. Two hot heads leave the middle of the longest side and meet opposite; dividers draw left to right. | `head`, `fadeOut` |
| `CyberScanner` | A KITT-style light that sweeps to one end and back, slowing at the ends, with a trail that flips sides. Best on dividers. | `length`, `steps` |
| `CyberLiveWire` | A jagged electric arc crawling the edge. A wide pass plus a thin white-hot core; the shape re-rolls many times per loop and flickers. | `length`, `jitter`, `segment`, `frames`, `count` |
| `CyberPacketStream` | Lanes of data packets at different sizes and 1×/2×/3× speeds, so small fast packets overtake big slow ones. | `lanes` (`CyberPacketLane(count, length, speed, alpha)`) |
| `CyberChargeMeter` | The outline is a ring of segments that power on in order. The leading segment flickers, then the full ring holds and fades. | `segment`, `gap`, `height`, `fadeOut` |
| `CyberSequencedLights` | Dim chevrons pointing along the path. One lit chevron per group steps forward with a decaying afterglow, like runway approach strobes. | `groups`, `trail`, `size`, `spacing` |
| `CyberBracketLock` | Corner brackets snap in from nothing with a slight overshoot, flash as they lock, then breathe. | `arm`, `notches` |
| `CyberWeld` | A welding arc (white core, blue halo) that flickers every frame and flares now and then. Tiny sparks fizz off it; occasional pops throw bigger, faster sparks that burst as they die. Behind it, a jittered bead cools from yellow-white through orange and red to gunmetal. See [Weld](#weld). | `cycleMillis`, `coolingSeconds`, `peakKelvin`, `fizzRate`, `popChance`, `colorScale`, `coolColor` |

### Weld

```kotlin
Modifier.cyberWeld(shape = card)                                   // shortcut: stronger glow, loop = weld.cycleMillis
Modifier.cyberPathBorder(CyberWeld(), shape = card,                // same effect through the generic modifier:
    animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)))   // loop must equal cycleMillis
Modifier.cyberPathDivider(CyberWeld(cycleMillis = 3000),
    animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)))
```

- **Cooling is physical.** The bead follows Newton's law of cooling from `peakKelvin` (3000 K) toward 300 K, with time constant `coolingSeconds`. Its glow fades out at the Draper point (798 K), below which solids stop visibly glowing, leaving `coolColor`.
- **Glow color comes from `colorScale`**, a list of (kelvin, color) stops. The default is the blackbody scale from published table values: 1000 K `#FF3800`, 1200 K `#FF5300`, 1800 K `#FF7E00`, 2000 K `#FF8912`, 3000 K `#FFB46B`. Above 2400 K the bead also mixes toward white, because it reads overexposed.
- **The final color is `CyberPrimitives.Colors.Void100` (#2A2D3A).** It's the palette token closest to gunmetal (#2A3439), at CIEDE2000 ΔE 7.6.
- **Sparks are ballistic.** Each spark has a launch velocity out of the shape plus screen-down gravity, and cools along the same color scale. Fizz sparks come at `fizzRate` per second. Pops are decided per `popSlotSeconds` slot with probability `popChance`; each pop throws 5–9 big sparks and flashes the arc.
- **The loop is seamless.** All randomness is a hash of indices that repeat every cycle.
- **Particle time needs the loop duration.** Spark physics is in seconds, so `cycleMillis` must match the loop duration of the `animationSpec`. `Modifier.cyberWeld` sets this up for you.

Sources: blackbody sRGB values from the [vendian.org blackbody table](http://www.vendian.org/mncharity/dir3/blackbody/), as listed on [temperature.m15y.com](https://temperature.m15y.com/); [Draper point](https://en.wikipedia.org/wiki/Draper_point); gunmetal [#2A3439](https://encycolorpedia.com/2a3439).

## Box patterns

| Effect | What you see | Key parameters |
|---|---|---|
| `CyberCornerBrackets` | Only the corners are stroked: L-brackets on sharp corners, one bracket wrapping each chamfer, arcs on rounded corners, four quadrant arcs on circles. Optional mid-side notches. Static. | `arm`, `notches` |
| `CyberGraduatedTicks` | An instrument scale: minor ticks into the shape, a long tick every Nth, an optional spine. Reads as a dial bezel on circles. | `spacing`, `minor`, `major`, `every`, `spine` |
| `CyberBarcode` | A thick band of bars and spaces of seeded widths. | `module`, `height`, `seed`, `bars` |
| `CyberHazardBand` | A band of slanted stripes that bends round corners. | `band`, `stripe`, `slant` |
| `CyberBraid` | Hairline sine strands woven together, optionally between two rails (a banknote-style guilloché band). | `wavelength`, `amplitude`, `strands`, `rails` |
| `CyberBarbedWire` | Two twisted strands with a crossed barb every few twists. | `twist`, `twistsPerBarb`, `barb` |
| `CyberCircuitTrace` | A PCB trace with hollow via rings and 45° branch stubs ending in pads. | `spacing`, `via`, `stub` |
| `CyberChain` | Face-on and edge-on links alternating, bent round corners. | `link`, `thickness`, `wire` |

## Writing a new effect

Implement `CyberPathEffect` as a `data class`:

- `extent(density)` returns how far the geometry reaches from the outline, in px. Borders inset by this amount.
- `prepare(outline, closed, density)` does all measuring and stamp building. It runs once per size, inside `drawWithCache`.
- The returned `CyberPathRenderer.layers(progress)` returns `CyberPathLayer`s. Each layer is one `drawPath` pass with its own path effect, width, cap, alpha, `hot` (mix toward white), `glow` flag, optional replacement `path` (free-standing strokes such as sparks), and optional `color` (defaults to the modifier's color).

Use several layers instead of `SumPathEffect`, which is not in Compose's common API. Helpers live in `utils/CyberPathGeometry.kt`.

## Lessons from prototyping against Skia

Android's `PathEffect`s are Skia path effects. Every effect here was prototyped and tuned in Skia first. These rules came out of that:

1. **Stamps are always filled.** `stampedPathEffect` output is a fill, so every stamp must be a closed area: a ribbon around a polyline, a polygon, or a disc. A bare polyline stamp draws nothing.
2. **Make holes with opposite winding.** Skia rebuilds the output path and drops the stamp's fill type, so even-odd holes vanish. `CyberPathGeometry` makes every solid clockwise and every hole counter-clockwise (e.g. via rings and chain links).
3. **Subdivide Morph stamps.** `StampedPathEffectStyle.Morph` maps each stamp edge through a single quad. Long edges cut corners, so edges are split to about 1 dp.
4. **Fit spacing to the outline length.** A pattern period that doesn't divide the perimeter leaves a visible seam. Every advance, dash period and segment is refitted with `fit(length, desired)`.
5. **Never animate stamps with phase on closed outlines.** Skia doesn't place a partial stamp before the contour start, so a phase shift leaves a gap there. Closed outlines are animated by rotating the contour start with `PathMeasure.getSegment` (`CyberMarching`). Dashes wrap correctly, so they use phase.
6. **Stretch stamp advances by 1e-5.** When the fitted advance divides the length exactly, float error adds an extra stamp on top of the first. Rotate-style stamps then double-draw.
7. **Move the seam off corners.** Compose shapes start their contour at a corner. Morph patterns (rails, weaves) break where the last and first stamps meet at a corner, so the start moves to the middle of the longest side. Equal sides break ties deterministically.
8. **Detect corners by curvature, not total turning.** A corner is where the outline turns faster than 1/24 dp for at least 30°. Gaps up to 2 dp are merged. With this rule, 12 dp rounded corners count and a 58 dp circle doesn't.
9. **Avoid composing a dash inside a stamp.** `chainPathEffect(stamped, dash)` couldn't be verified in the Skia build used for prototyping, so `CyberSequencedLights` positions lit stamps with a long advance and a phase instead.

## How this was verified

- Every effect was rendered in Skia (skia-python, the same engine as Android's path effects) on a chamfered card, a circle, a rounded rect, an octagon and a divider. About 30 candidates went through several rounds of visual review; these 17 survived.
- The Kotlin was compiled with Kotlin 2.x (language 2.1) against stubs of the Compose APIs used. Stub signatures were checked against the Compose docs.
- The Kotlin geometry code was run on the JVM and its output rendered in Skia. It was diffed pixel by pixel against the reference prototypes: all 20 variants (including `CyberWeld`, colors and sparks) match to within sub-pixel differences.
- **Not yet verified:** a real Gradle/Android build, the on-device look of the RenderEffect glow (previews approximate its blur), and performance on device. `CyberPathEffectsTest` renders every effect on API 33 and API 30 under Robolectric.
