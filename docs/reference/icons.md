# Icons

`cyberpunkandroid` ships 154 line icons plus two drawn decorations (a dial and a segmented rim). This page covers how to draw them, the four render variants, the full catalog, and when to use each.

---

## `CyberIcon`

**What:** The library's icon composable. It draws one of the bundled vector icons at a given size and color, optionally in a stylized variant.

**Looks like:** Thin geometric outline glyphs on a 24 × 24 grid, drawn with 1.5-unit strokes and rounded ends and corners. Icons are drawn white and tinted at runtime, so they take any color.

```kotlin
CyberIcon(
    iconRes = CyberIcons.Shield,
    contentDescription = "Firewall active",
    size = CyberPrimitives.IconSizes.dp32,
    tint = CyberTheme.colors.primary,
    variant = CyberIconVariant.Duotone
)
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `iconRes` | `@DrawableRes Int` | required | Which icon, from [`CyberIcons`](#catalog) (or any vector drawable). |
| `contentDescription` | `String?` | required | Screen reader label. Use `null` for decorative icons next to a text label. |
| `modifier` | `Modifier` | `Modifier` | Layout and effects (e.g. `cyberPing`, `cyberIconSpin`). |
| `size` | `Dp` | `24.dp` (`CyberPrimitives.IconSizes.dp24`) | Width and height. Standard sizes: 16 inline, 24 default, 32 large, 48 XL, 64 hero. |
| `tint` | `Color` | `LocalContentColor` | Stroke/fill color. Inherits from the surrounding text color by default. |
| `variant` | `CyberIconVariant` | `Outline` | Render style; see below. |

### Variants

| Variant | Looks like | Use for |
| --- | --- | --- |
| `Outline` | The plain line icon. | Default everywhere. |
| `Solid` | Every path is filled with `tint` and its stroke removed. | Selected/active states (a filled tab icon). |
| `Duotone` | A 40% `tint` fill under the full-strength outline. | Emphasis without losing the line style: feature tiles, empty states. |
| `Overload` | The outline in `tint`, with a primary-color (cyan) ghost offset down-left and a secondary-color (magenta) ghost offset up-right, both at 80% opacity. | Glitch/error accents and "hacked" states. A static, cheaper cousin of `cyberOverload`. |

**Variant gotchas**
- **Solid on line-only icons:** `Solid` and `Duotone` fill paths but don't stroke them. Icons drawn only with open lines (`Minus`, `X`, `Check`, arrows, chevrons) have no area to fill, so `Solid` can come out empty or partial. For hearts, stars and bookmarks, prefer the dedicated `HeartFilled`, `StarFilled` and `BookmarkFilled` icons.
- **Overload offset in pixels:** the `Overload` ghost offset is 2 **pixels** (`CyberConfig.Icon.OverloadOffset`), not dp, so it looks smaller on high-density screens.
- **Overload colors from the theme:** the ghost colors come from `CyberTheme.colors.primary` and `.secondary`, not from `tint`.

### With effects

`CyberIcon` takes a `modifier`, so motion and glow effects compose onto it:

```kotlin
CyberIcon(CyberIcons.Loading, contentDescription = "Loading", modifier = Modifier.cyberIconSpin())
CyberIcon(CyberIcons.Online, contentDescription = "Live", modifier = Modifier.cyberPing())
```

For glowing icons, use [`CyberGlowIcon` / `CyberGlowIconPath`](modifiers-and-effects.md#cyberglowicon), which take the same `iconRes`: `CyberGlowIcon(iconRes = CyberIcons.Shield, contentDescription = "Firewall")`.

---

## `SemanticIcons`

Icons picked for a meaning rather than a picture, so status UIs stay consistent:

| Name | Icon | Use for |
| --- | --- | --- |
| `SemanticIcons.Caution` | `Warning` | Warnings, risky actions |
| `SemanticIcons.Danger` | `Error` | Errors, destructive results |
| `SemanticIcons.Success` | `Success` | Completion, healthy state |
| `SemanticIcons.Info` | `Terminal` | Informational / system messages (note: the terminal glyph, not `Info`) |

Pair them with the matching semantic color, e.g. `CyberTheme.semantics.colors.warning` for `Caution`.

---

## Catalog

All icons are `CyberIcons.<Name>` (resource `R.drawable.cyber_ic_<snake_name>`).

| Group | Icons |
| --- | --- |
| Navigation & arrows | `ArrowDown`, `ArrowLeft`, `ArrowRight`, `ArrowUp`, `ChevronDown`, `ChevronLeft`, `ChevronRight`, `ChevronUp`, `Home`, `Menu`, `MenuDots`, `ExternalLink`, `Maximize`, `Minimize`, `Drag` |
| Actions & editing | `Plus`, `Minus`, `X`, `Check`, `DoubleCheck`, `Edit`, `Delete`, `Copy`, `Cut`, `Paste`, `Undo`, `Redo`, `Save`, `Search`, `Filter`, `Sort`, `Refresh`, `Sync`, `Share`, `Send`, `Download`, `Upload`, `Pin`, `Link`, `Unlink`, `Attachment`, `Clipboard` |
| Status & feedback | `Success`, `Error`, `Warning`, `Info`, `Help`, `Loading`, `Progress`, `Bell`, `BellOff`, `Flag`, `Online`, `Offline` |
| Files & storage | `File`, `FileArchive`, `FileAudio`, `FileCode`, `FileImage`, `FileMinus`, `FilePlus`, `FileText`, `FileVideo`, `Folder`, `FolderOpen`, `FolderPlus`, `Archive`, `Inbox`, `Database`, `Cloud`, `CloudDownload`, `CloudUpload` |
| Tech & hardware | `Cpu`, `Chip`, `Circuit`, `Memory`, `Server`, `Terminal`, `Code`, `Api`, `Bug`, `Zap`, `Hash`, `BatteryCharging`, `BatteryFull`, `BatteryLow` |
| Connectivity | `Wifi`, `WifiOff`, `Bluetooth`, `Signal`, `Globe` |
| Security & identity | `Lock`, `Unlock`, `Key`, `Shield`, `ShieldCheck`, `ShieldX`, `Fingerprint`, `Eye`, `EyeOff`, `LogIn`, `LogOut`, `QrCode`, `User`, `UserMinus`, `UserPlus`, `Users` |
| Communication | `Mail`, `Message`, `Phone`, `PhoneOff`, `AtSign`, `VideoCall` |
| Media | `Play`, `Pause`, `Stop`, `Rewind`, `FastForward`, `SkipBack`, `SkipForward`, `Music`, `Video`, `Image`, `Camera`, `CameraOff`, `Mic`, `MicOff`, `VolumeHigh`, `VolumeLow`, `VolumeOff` |
| Data & charts | `ChartBar`, `ChartLine`, `ChartPie`, `Table`, `Trending`, `Percent`, `Sliders`, `Settings` |
| Time | `Clock`, `Timer`, `Calendar` |
| Social & rating | `Heart`, `HeartFilled`, `Star`, `StarFilled`, `Bookmark`, `BookmarkFilled`, `ThumbsUp`, `ThumbsDown`, `Award` |
| Developer / git | `GitBranch`, `GitCommit`, `GitMerge`, `GitPull` |

To add an icon: add a 24 × 24 vector drawable named `cyber_ic_<name>.xml` to `cyberpunkandroid/src/main/res/drawable` in the same style (white `strokeColor`, `strokeWidth` 1.5, round cap and join, transparent fill), then register it in `CyberIcons`.

---

## Decorations

Drawn shapes for building HUD (heads-up display) dials, gauges and clocks. They're decorative, with no semantics, so pair them with text or a labeled component for accessibility.

### `CyberDialTicks`

**What:** A ring of tick marks, like a watch face or a gauge bezel.

**Looks like:** `tickCount` short lines around the edge, pointing inward, starting at 12 o'clock. Every `majorTickInterval`-th tick is longer.

**Use for:** Clocks, speedometer-style gauges, a frame around `CyberRim` or a spinner.

```kotlin
Box(contentAlignment = Alignment.Center) {
    CyberDialTicks(color = CyberTheme.colors.border, size = 96.dp)
    CyberRim(progress = load, modifier = Modifier.size(72.dp))
}
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `modifier` | `Modifier` | `Modifier` | Layout/effects. |
| `color` | `Color` | `Color.White` | Tick color. |
| `tickCount` | `Int` | `60` | Ticks around the circle (60 = minutes). |
| `majorTickInterval` | `Int` | `5` | Every Nth tick is major (5 = hour marks on a 60-tick dial). |
| `tickLength` | `Dp` | `4.dp` | Minor tick length. |
| `majorTickLength` | `Dp` | `8.dp` | Major tick length. |
| `strokeWidth` | `Dp` | `1.dp` | Tick thickness (square ends). |
| `size` | `Dp` | `64.dp` | Diameter. The dial is always square. |

### `CyberSectorRim`

**What:** A ring split into arc segments with gaps between them, like a sci-fi reticle or a segmented shield meter.

**Looks like:** Arcs drawn clockwise from 12 o'clock with `gapAngle`° gaps. `sectorAngles` sets the relative length of each arc and is scaled so arcs plus gaps fill the full circle. Arcs have square ends and sit inside the bounds.

**Use for:** Targeting reticles, segmented status rings (one arc per subsystem), clock faces (used by `CyberTime`).

```kotlin
// Three unequal segments: 50%, 30%, 20% of the ring
CyberSectorRim(sectorAngles = listOf(5f, 3f, 2f), gapAngle = 8f, color = CyberTheme.colors.primary)
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `modifier` | `Modifier` | `Modifier` | Layout/effects. |
| `color` | `Color` | `Color.White` | Arc color. |
| `thickness` | `Dp` | `2.dp` | Stroke width. |
| `sectorAngles` | `List<Float>` | four equal (`90f` each) | Relative arc lengths; only the proportions matter. Empty or all zeros draws nothing. |
| `gapAngle` | `Float` | `10f` | Degrees of gap after each arc. |
| `size` | `Dp` | `64.dp` | Diameter. |

---

## Stroked shapes

Icons made from a centerline that is thickened into a filled outline. The outline is the single source of truth. Drawing, layout bounds and path effects all use it.

### `CyberStrokeIcon`

**What:** An icon drawn from a `CyberCenterline` that you define in code.

**Looks like:** A solid shape with the stroke's thickness. Corners stay sharp and keep the centerline's angle. Curves keep their shape. Overlaps merge into one shape.

**Use for:** Custom HUD glyphs, closed or concave shapes, and shapes that cross themselves.

```kotlin
val cursiveL = cyberCenterline(24f, 24f) {
    moveTo(4f, 21f)
    cubicTo(9f, 19f, 17f, 10f, 17f, 5f)
    cubicTo(17f, 2f, 13f, 2f, 12f, 5f)
    cubicTo(10f, 11f, 9f, 17f, 10f, 20f)
    cubicTo(11f, 22f, 14f, 21f, 16f, 19f)
}
CyberStrokeIcon(cursiveL, width = 96.dp, thickness = 8.dp)
```

| Parameter | Type | Default | What it does |
| --- | --- | --- | --- |
| `centerline` | `CyberCenterline` | required | The path to thicken. |
| `modifier` | `Modifier` | `Modifier` | Layout/effects. |
| `width` | `Dp` | `64.dp` | Width of the centerline. The height follows the viewport. |
| `thickness` | `Dp` | `4.dp` | Stroke thickness. |
| `alignment` | `CyberStrokeAlignment` | `Outside` | Stroke position on closed contours. |
| `color` | `Color` | `CyberTheme.colors.primary` | Fill color. |
| `contentDescription` | `String?` | `null` | Accessibility label. Use `null` for decoration. |

A second overload takes a `CyberStrokeShape`. Use it to share one shape with `Modifier.cyberPerimeter`.

#### Geometry rules

- **The centerline does not move.** The stroke grows around it.
- **The component grows to hold the stroke.** The parent's space is the only limit.
- **At the limit, the thickness stops.** The shape does not shrink.
- **Corners keep their angle.** There is no miter limit and no bevel.
- **Curves keep their center.** Arc radii become r + offset and r − offset.
- **Closed loops thicken outside by default.** The enclosed area keeps its size.
- **Open contours thicken on both sides.** They have no inside.
- **Overlaps merge.** Inner corners, tight curves and self-crossings stay valid.

The geometry splits the centerline into segments. Each segment becomes a quad. Each corner gets a wedge that keeps the angle. The union of these pieces is the outline.

#### `CyberCenterline`

Build it with `cyberCenterline(viewportWidth, viewportHeight) { … }`. The builder has these calls:

| Call | What it does |
| --- | --- |
| `moveTo(x, y)` | Starts a new contour. |
| `lineTo(x, y)` | Adds a straight line. |
| `quadTo(x1, y1, x, y)` | Adds a quadratic Bézier curve. |
| `cubicTo(x1, y1, x2, y2, x, y)` | Adds a cubic Bézier curve. |
| `arcTo(centerX, centerY, sweepDegrees)` | Adds a circular arc around a center. Positive sweeps go clockwise. |
| `close()` | Closes the contour back to its start. |

#### `CyberStrokeAlignment`

| Value | Stroke position on a closed loop |
| --- | --- |
| `Center` | Half inside and half outside the centerline. |
| `Outside` | Fully outside. The enclosed area stays the same. |
| `Inside` | Fully inside. The outer edge stays on the centerline. |

#### `CyberStrokeShape`

The outline of a `CyberStrokeIcon` as a `Shape`. It caches its geometry for each size and thickness. Use one instance for the icon and its effects. It is a `CyberPerimeterShape`, so `Modifier.cyberPerimeter` can trace its edges.

### `CyberTripleChevron`

**What:** Three nested chevrons that point right.

**Looks like:** Filled stroke outlines with sharp tips and square ends.

The centerlines are fixed by `width` and `aspectRatio`. `spacing` is the visible gap between neighbors. The thickness limit is the parent's space or the inner corner. `CyberTripleChevronShape` is internal and is a `CyberPerimeterShape`.

### Playgrounds

Debug builds have interactive previews with sliders:

- `CyberStrokeIconPlayground`: five sample shapes, width, thickness, alignment and a tracer.
- `CyberTripleChevronPlayground`: every chevron parameter and a tracer.

They are in the `debug` source set and are not in release builds.
