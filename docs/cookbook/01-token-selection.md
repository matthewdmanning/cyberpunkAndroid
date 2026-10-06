# Token Selection and Ontology

This guide maps semantic UI intent to the correct tokens in `:cyberpunkandroid`.

> [!IMPORTANT]
> Select tokens according to **semantic role**, never according to apparent color.
> Do not infer appearance from names when making design choices.

---

## 1. Decision Table: Intent to Token

| UI Intent | Target Token | Fallback Substitution | Prohibited Fallback |
| :--- | :--- | :--- | :--- |
| **Primary surface background** | `CyberTheme.colors.background` | `CyberTheme.colors.surfacePrimary` | `Color.Black`, `Void500` |
| **Standard component card surface** | `CyberTheme.colors.surfacePrimary` | `CyberTheme.colors.surface` | `Color.DarkGray` |
| **Secondary surface / container** | `CyberTheme.colors.surfaceSecondary` | `CyberTheme.colors.surfacePrimary` | `Color(0xFF...)` |
| **Tertiary surface / nested container**| `CyberTheme.colors.surfaceTertiary` | `CyberTheme.colors.surfaceSecondary` | `Color(0xFF...)` |
| **Floating / elevated surface** | `CyberTheme.colors.surfaceElevated` | `CyberTheme.colors.surfacePrimary` | `MaterialTheme.colorScheme.surface` |
| **Primary foreground / brand accent** | `CyberTheme.colors.primary` | *None* | `Color.Cyan`, `Cyan500` |
| **Secondary foreground / accent** | `CyberTheme.colors.secondary` | `CyberTheme.colors.primary` | `Color.Magenta`, `Magenta500` |
| **Default neutral border** | `CyberTheme.colors.border` | `CyberTheme.colors.primary.copy(0.4f)` | `Color.Gray` |
| **Primary content text** | `CyberTheme.colors.textPrimary` | `CyberTheme.colors.primary` | `Color.White` |
| **Secondary / supporting text** | `CyberTheme.colors.textSecondary` | `CyberTheme.colors.textPrimary.copy(0.7f)` | `Color.Gray`, `Chrome500` |
| **Caution / warning status** | `CyberTheme.semantics.colors.warning` | `CyberTheme.semantics.colors.caution` | `Color.Yellow` |
| **Danger / critical error status** | `CyberTheme.semantics.colors.error` | `CyberTheme.semantics.colors.danger` | `Color.Red` |
| **Success / operational status** | `CyberTheme.semantics.colors.success` | `CyberTheme.colors.primary` | `Color.Green` |
| **Info / system telemetry status** | `CyberTheme.semantics.colors.info` | `CyberTheme.colors.primary` | `Color.Cyan` |
| **Terminal prompt / code accent** | `CyberTheme.semantics.colors.terminal`| `CyberTheme.colors.primary` | `Color.Green` |

---

## 2. Token Ontologies

### Shapes
| Intent | Shape Token | Raw Geometry |
| :--- | :--- | :--- |
| **Interactive card or hero panel** | `CyberTheme.shapes.cardShape` | Cut corners (20.dp top-end & bottom-start) |
| **Default component container** | `CyberTheme.shapes.cyberCutCornerShape` | Cut corners (12.dp top-end & bottom-start) |
| **Small badge or chip container** | `CyberTheme.shapes.cyberCutCornerShapeSmall`| Cut corners (8.dp top-end & bottom-start) |
| **Subtle rounded edge** | `CyberTheme.shapes.roundedSm` | Rounded 2.dp |
| **Medium rounded edge** | `CyberTheme.shapes.roundedMd` | Rounded 4.dp |
| **Pill button / circular avatar** | `CyberTheme.shapes.roundedFull` | Rounded 9999.dp |

### Icon Sizes
| Intent | Size Token | Metric |
| :--- | :--- | :--- |
| **Inline text ornament or status dot** | `CyberPrimitives.IconSizes.dp16` | 16.dp |
| **Standard action button / list icon** | `CyberPrimitives.IconSizes.dp24` | 24.dp (default) |
| **Prominent card / navigation icon** | `CyberPrimitives.IconSizes.dp32` | 32.dp |
| **Feature tile / empty state hero** | `CyberPrimitives.IconSizes.dp48` | 48.dp |
| **Hero graphic or dialog header** | `CyberPrimitives.IconSizes.dp64` | 64.dp |

### Animation Durations
| Intent | Duration Token | Value |
| :--- | :--- | :--- |
| **Instant micro-interaction / click** | `CyberPrimitives.Durations.ms150` | 150 ms |
| **Standard transition / state expand** | `CyberPrimitives.Durations.ms300` | 300 ms |
| **Status warning pulse** | `CyberTheme.semantics.durations.warningPulse` | 500 ms |
| **Ambient slow scan / sweep** | `CyberPrimitives.Durations.ms2000` | 2000 ms |

---

## 3. Allowed vs Prohibited Practices

```kotlin
// BAD: Raw color literal
val badColor = Color(0xFF00E5FF)

// BAD: Palette primitive referenced directly in component code
val badToken = CyberPrimitives.Colors.Cyan500

// GOOD: Semantic token from CyberTheme
val goodColor = CyberTheme.colors.primary
val statusColor = CyberTheme.semantics.colors.error
```

```kotlin
// BAD: Hardcoded corner cut values
val badShape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp)

// GOOD: Design system shape token
val goodShape = CyberTheme.shapes.cardShape
```

### Substitution Rules
1. If a semantic status color is unavailable, use `CyberTheme.colors.primary`.
2. If an elevated surface color is unavailable, use `CyberTheme.colors.surfacePrimary`.
3. **Never** fall back to:
   - `CyberPrimitives.Colors.*` directly in UI composables
   - Android standard `Color.*`
   - Arbitrary hex codes `Color(0x...)`
