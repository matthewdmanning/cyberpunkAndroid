# Theme Architecture & Design Tokens

Reference for `CyberTheme`, its subsystems, performance optimizations, and integration with Material 3.

---

## 1. Overview & Anatomy

`CyberTheme` follows the official Android Jetpack Compose [Theme Anatomy](https://developer.android.com/develop/ui/compose/designsystems/anatomy) architecture:
1. **Theme System Classes**: `@Immutable` data classes defining tokens for each visual/behavioral subsystem.
2. **Composition Locals**: `staticCompositionLocalOf` instances with safe fallback factories.
3. **Theme Object**: `object CyberTheme` with convenience accessors optimized with `@Composable @ReadOnlyComposable`.
4. **Theme Function**: `@Composable fun CyberTheme(...)` wrapping `CompositionLocalProvider`, establishing content color and typography flow, and bridging into `MaterialTheme`.

```
CyberTheme (Theme Object)
├── colors: CyberColors
├── typography: CyberTypography
├── shapes: CyberShapes
├── elevation: CyberElevation
├── spacing: CyberSpacing
├── semantics: CyberSemanticTokens
└── status: CyberStatusColors
```

---

## 2. Theme Subsystems

### A. Colors (`CyberTheme.colors`)
| Token | Role | Default Value |
|---|---|---|
| `primary` | Primary action and prominent neon accent | `Cyan500` (`#00F0FF`) |
| `secondary` | Supporting action and secondary neon accent | `Magenta500` (`#FF003C`) |
| `background` | Root canvas / deep void background | `Void500` (`#05050A`) |
| `surface` | Card / container background | `Void100` (`#1A1A28`) |
| `surfacePrimary` | Baseline surface tier | `Void500` (`#05050A`) |
| `surfaceSecondary` | Low container tier | `Void400` (`#0A0A14`) |
| `surfaceTertiary` | Standard container tier | `Void300` (`#0F0F1E`) |
| `surfaceElevated` | Elevated / floating surface tier | `Void200` (`#141424`) |
| `textPrimary` | High-contrast body & header text | `Cyan500` (`#00F0FF`) |
| `textSecondary` | Muted supporting text | `Chrome500` (`#8A99AD`) |
| `border` | Standard container stroke | `Cyan700` (`#007A8A`) |

### B. Typography (`CyberTheme.typography`)
| Token | Font Family | Size | Style / Tracking |
|---|---|---|---|
| `display` | `OareSansDisplayFontFamily` | `32.sp` | Black, Italic, tracking `0.1.em` |
| `terminal` | `FontFamily.Monospace` | `14.sp` | Normal, tracking `0.1.em` |
| `body` | `FastupFontFamily` | `16.sp` | Normal, tracking `0.1.em` |

### C. Shapes (`CyberTheme.shapes`)
| Token | Geometry | Use Case |
|---|---|---|
| `cardShape` | CutCorner (20.dp top-end & bottom-start) | Hero cards, large panels |
| `cyberCutCornerShape` | CutCorner (12.dp top-end & bottom-start) | Standard buttons, containers |
| `cyberCutCornerShapeSmall` | CutCorner (8.dp top-end & bottom-start) | Chips, badges, compact controls |
| `roundedSm` | RoundedCorner (2.dp) | Subtle inner accents |
| `roundedMd` | RoundedCorner (4.dp) | Standard rounding |
| `roundedLg` | RoundedCorner (8.dp) | Soft containers |
| `roundedXl` | RoundedCorner (16.dp) | Large dialogs |
| `roundedFull` | RoundedCorner (9999.dp) | Pill shapes, circular buttons |

### D. Elevation (`CyberTheme.elevation`)
Elevation levels for layered HUD surfaces:
| Token | Value | Static Fallback |
|---|---|---|
| `level0` | `0.dp` | `CyberElevation.level0` |
| `level1` | `1.dp` | `CyberElevation.level1` |
| `level2` | `3.dp` | `CyberElevation.level2` |
| `level3` | `6.dp` | `CyberElevation.level3` |
| `level4` | `8.dp` | `CyberElevation.level4` |
| `level5` | `12.dp` | `CyberElevation.level5` |

### E. Spacing (`CyberTheme.spacing`)
Standard spatial increments:
| Token | Value | Token | Value |
|---|---|---|---|
| `dp2` | `2.dp` | `dp24` | `24.dp` |
| `dp4` | `4.dp` | `dp32` | `32.dp` |
| `dp8` | `8.dp` | `dp48` | `48.dp` |
| `dp12` | `12.dp` | `dp64` | `64.dp` |
| `dp16` | `16.dp` | | |

### F. Semantics & Status (`CyberTheme.semantics`, `CyberTheme.status`)
- `CyberTheme.semantics.colors`: `caution`, `warning`, `danger`, `error`, `success`, `info`, `terminal`.
- `CyberTheme.semantics.durations`: `warningPulse` (500 ms).
- `CyberTheme.status`: Full color roles (`color`, `onColor`, `container`, `onContainer`) for `success`, `info`, `warning`, `caution`, `danger`.

---

## 3. Performance: `@ReadOnlyComposable`

All property getters on `CyberTheme` are annotated with `@Composable @ReadOnlyComposable`:
```kotlin
object CyberTheme {
    val colors: CyberColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCyberColors.current
    ...
}
```
`@ReadOnlyComposable` guarantees to the Compose compiler that the getter only reads from `CompositionLocal` and never emits UI nodes or allocates internal state. The compiler eliminates `startRestartGroup` / `endRestartGroup` instructions, avoiding slot-table overhead and recomposition tracking across all token read sites.

---

## 4. Material 3 Interoperability

### Automatic Theming & Content Flow
Wrapping a UI hierarchy in `CyberTheme { content() }` automatically:
1. Configures Material 3's `MaterialTheme` with cohesive `colorScheme`, `typography`, and `shapes`.
2. Provides `LocalContentColor provides colors.textPrimary`.
3. Wraps content in `ProvideTextStyle(value = typography.body)`.

Standard Material 3 components (`Button`, `Card`, `Scaffold`, `Text`, `Icon`) inside `CyberTheme` automatically render in Cyberpunk style with zero manual styling.

### Non-Intrusive Embedding (`wrapMaterialTheme = false`)
To embed a Cyberpunk component in an existing Material 3 app without altering the outer host `MaterialTheme`:
```kotlin
MaterialTheme(colorScheme = enterpriseColorScheme) {
    AppScaffold {
        // Scopes Cyberpunk tokens without overriding host MaterialTheme
        CyberTheme(wrapMaterialTheme = false) {
            CyberCard {
                CyberButton(onClick = { ... }) { Text("CONFIRM") }
            }
        }
    }
}
```

### MaterialTheme Extension Accessors
For developers accustomed to `MaterialTheme.<token>` syntax:
- `MaterialTheme.cyberColors`
- `MaterialTheme.cyberTypography`
- `MaterialTheme.cyberShapes`
- `MaterialTheme.cyberElevation`
- `MaterialTheme.cyberSpacing`
