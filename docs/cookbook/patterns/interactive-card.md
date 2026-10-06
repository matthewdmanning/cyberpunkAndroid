# Pattern: Primary Interactive Card

## Pattern Overview
The primary interactive card is the foundational building block for list items, inventory slots, and dashboard widgets.

## Composition Specification
```text
Required:
- CyberTheme.shapes.cardShape
- CyberTheme.colors.surfacePrimary
- CyberTheme.colors.border
- Modifier.clickable (or CyberButton)
- Modifier.cyberBorder

Optional:
- Modifier.cyberGlowBorder / cyberGlowBorder (when focused or active)
- Modifier.cyberOverload (trigger = PRESS for tactile glitch)

Do not combine:
- Modifier.cyberCrt inside an individual card
- Ambient non-interactive glows with heavy hover shaders
```

## Canonical Assembly
```kotlin
@Composable
fun InteractiveCyberCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = if (isSelected) CyberTheme.colors.primary else CyberTheme.colors.border
    val surfaceColor = if (isSelected) CyberTheme.colors.surfaceElevated else CyberTheme.colors.surfacePrimary

    Box(
        modifier = modifier
            .then(
                if (isSelected) {
                    Modifier.cyberGlowBorder(color = CyberTheme.colors.primary.copy(alpha = 0.4f), glowRadius = 8.dp)
                } else Modifier
            )
            .clip(CyberTheme.shapes.cardShape)
            .background(surfaceColor)
            .cyberBorder(width = 1.dp, color = borderColor)
            .cyberOverload(
                trigger = CyberInteractionTrigger.PRESS,
                interactionSource = interactionSource,
                strength = 0.3f
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CyberIcon(
                iconRes = iconRes,
                contentDescription = null, // Decorative icon next to title
                tint = if (isSelected) CyberTheme.colors.primary else CyberTheme.colors.textSecondary,
                variant = if (isSelected) CyberIconVariant.Solid else CyberIconVariant.Outline,
                size = CyberPrimitives.IconSizes.dp32
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = CyberTheme.typography.titleMedium,
                    color = CyberTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = CyberTheme.typography.bodySmall,
                    color = CyberTheme.colors.textSecondary
                )
            }
        }
    }
}
```
