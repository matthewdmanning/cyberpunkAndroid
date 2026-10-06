# Icon Selection and Usage

The `:cyberpunkandroid` library includes 154 stroke-based icons in `CyberIcons`. Do not select icons solely by visual appearance. Select them by semantic UI intent.

---

## 1. Semantic Lookup Table

| UI Intent | Recommended Icon | Recommended Variant | Sizing Token |
| :--- | :--- | :--- | :--- |
| **Confirm / Approve action** | `CyberIcons.Check` | `Outline` | `IconSizes.dp24` |
| **Reject / Dismiss / Close** | `CyberIcons.X` | `Outline` | `IconSizes.dp24` |
| **Back navigation** | `CyberIcons.ArrowLeft` or `ChevronLeft` | `Outline` | `IconSizes.dp24` |
| **System warning / Caution** | `CyberIcons.Warning` | `Duotone` | `IconSizes.dp24` |
| **Critical failure / Error** | `CyberIcons.Error` | `Overload` | `IconSizes.dp24` |
| **Security locked / Encrypted** | `CyberIcons.Lock` | `Outline` | `IconSizes.dp24` |
| **Security unlocked / Breached**| `CyberIcons.Unlock` | `Overload` | `IconSizes.dp24` |
| **Firewall / Defensive state** | `CyberIcons.Shield` | `Duotone` | `IconSizes.dp32` |
| **Command console / CLI** | `CyberIcons.Terminal` | `Outline` | `IconSizes.dp24` |
| **Hardware / Core compute** | `CyberIcons.Cpu` or `Chip` | `Duotone` | `IconSizes.dp32` |
| **Live network connection** | `CyberIcons.Online` | `Outline` | `IconSizes.dp16` |
| **Data transmission / Sync** | `CyberIcons.Sync` | `Outline` | `IconSizes.dp24` |

---

## 2. Choosing the Render Variant

`CyberIcon` supports four variants via `CyberIconVariant`:

| Variant | UI Purpose | Avoid When |
| :--- | :--- | :--- |
| **`Outline`** *(Default)* | Standard UI actions, neutral buttons, navigation bars. | Showing active selection state. |
| **`Solid`** | Active tab selection, toggled bookmarks, filled indicators. | Using icons composed only of single strokes (e.g., `Check`, `X`, arrows). |
| **`Duotone`** | Hero tiles, status cards, prominent dashboard metrics. | Compact 16.dp inline glyphs. |
| **`Overload`** | Critical alerts, hacked state indicators, system errors. | Standard navigation or calm states. |

> [!WARNING]
> Do not use `CyberIconVariant.Solid` on line-only icons like `Minus`, `X`, `Check`, or arrows. These glyphs lack closed loops and will render empty or broken. For filled stars, bookmarks, and hearts, use the dedicated icons: `CyberIcons.BookmarkFilled` or `CyberIcons.StarFilled`.

---

## 3. Directional and RTL Rules

- Navigation icons (`ArrowLeft`, `ArrowRight`, `ChevronLeft`, `ChevronRight`, `SkipBack`, `SkipForward`) communicate reading direction.
- In bidirectional layouts, navigation arrows must mirror in RTL configurations.
- Action icons (`Download`, `Upload`, `Search`, `Settings`, `Terminal`) are non-directional and must not be mirrored.

---

## 4. Accessibility and Semantics

```kotlin
// REQUIRED: Actionable icon button must have a meaningful description
CyberIcon(
    iconRes = CyberIcons.Terminal,
    contentDescription = "Open Command Shell",
    size = CyberPrimitives.IconSizes.dp24,
    tint = CyberTheme.colors.primary
)

// REQUIRED: Decorative icon accompanying a text label must pass null
Row(verticalAlignment = Alignment.CenterVertically) {
    CyberIcon(
        iconRes = CyberIcons.Shield,
        contentDescription = null, // Decorative: text below labels the control
        size = CyberPrimitives.IconSizes.dp16,
        tint = CyberTheme.colors.textSecondary
    )
    Text(
        text = "DEFENSIVE PROTOCOL",
        color = CyberTheme.colors.textSecondary
    )
}
```

---

## 5. Glowing Icons: CyberIcon vs CyberGlowIconPath

- Use `CyberIcon` for standard controls, buttons, toolbars, and high-frequency list items.
- Use `CyberGlowIconPath` only for prominent telemetry readouts, status badges, or hero headers.

```kotlin
// Emissive HUD status readout
CyberGlowIconPath(
    iconRes = CyberIcons.Shield,
    contentDescription = "Shield Integrity Nominal",
    color = CyberTheme.colors.primary,
    glowColor = CyberTheme.colors.primary,
    glowRadius = 12.dp
)
```
