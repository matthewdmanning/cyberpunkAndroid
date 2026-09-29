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
