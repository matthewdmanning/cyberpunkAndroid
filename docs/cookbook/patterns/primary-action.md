# Pattern: Primary Action Button

## Pattern Overview
A prominent call-to-action button featuring diagonal cut corners, emissive border glow, and feedback triggers.

## Composition Specification
```text
Required:
- CyberTheme.shapes.cyberCutCornerShape
- CyberTheme.colors.primary
- CyberTheme.colors.surfaceElevated
- Modifier.cyberBorder or cyberGlowBorder
- CyberButton component (or custom surface assembly)

Optional:
- Modifier.cyberGlowBorder
- Trailing CyberIcon with IconSizes.dp24
```

## Canonical Assembly
```kotlin
@Composable
fun PrimaryCyberAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    enabled: Boolean = true
) {
    CyberButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .then(
                if (enabled) {
                    Modifier.cyberGlowBorder(
                        color = CyberTheme.colors.primary.copy(alpha = 0.35f),
                        glowRadius = 8.dp
                    )
                } else Modifier
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text.uppercase(),
                style = CyberTheme.typography.labelLarge,
                color = if (enabled) CyberTheme.colors.primary else CyberTheme.colors.textSecondary
            )

            if (iconRes != null) {
                Spacer(modifier = Modifier.width(8.dp))
                CyberIcon(
                    iconRes = iconRes,
                    contentDescription = null,
                    tint = if (enabled) CyberTheme.colors.primary else CyberTheme.colors.textSecondary,
                    size = CyberPrimitives.IconSizes.dp24
                )
            }
        }
    }
}
```
