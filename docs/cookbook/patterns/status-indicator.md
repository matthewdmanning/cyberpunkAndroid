# Pattern: Status Indicator Tile

## Pattern Overview
A compact tile that conveys real-time system status (e.g., firewall health, server latency, power state) with coordinated icons, colors, and pulsing beacons.

## Composition Specification
```text
Required:
- CyberTheme.shapes.cyberCutCornerShapeSmall
- CyberTheme.semantics.colors (warning, error, success, info)
- CyberIcon with CyberIconVariant.Duotone or Overload
- Modifier.cyberBorder

Optional:
- Modifier.cyberPing (for active, live status)
- Modifier.cyberTextGlow on metrics readout
```

## Canonical Assembly
```kotlin
enum class SystemStatusLevel {
    NOMINAL, WARNING, CRITICAL
}

@Composable
fun SystemStatusTile(
    label: String,
    value: String,
    level: SystemStatusLevel,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusIcon, iconVariant) = when (level) {
        SystemStatusLevel.NOMINAL -> Triple(
            CyberTheme.semantics.colors.success,
            CyberIcons.Shield,
            CyberIconVariant.Duotone
        )
        SystemStatusLevel.WARNING -> Triple(
            CyberTheme.semantics.colors.warning,
            CyberIcons.Warning,
            CyberIconVariant.Duotone
        )
        SystemStatusLevel.CRITICAL -> Triple(
            CyberTheme.semantics.colors.error,
            CyberIcons.Error,
            CyberIconVariant.Overload
        )
    }

    Box(
        modifier = modifier
            .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
            .background(CyberTheme.colors.surfacePrimary)
            .cyberBorder(width = 1.dp, color = statusColor.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label.uppercase(),
                    style = CyberTheme.typography.labelSmall,
                    color = CyberTheme.colors.textSecondary
                )
                CyberIcon(
                    iconRes = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    variant = iconVariant,
                    size = CyberPrimitives.IconSizes.dp16
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = CyberTheme.typography.headlineSmall,
                color = statusColor
            )
        }
    }
}
```
