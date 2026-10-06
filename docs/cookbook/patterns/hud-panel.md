# Pattern: Scanned HUD Panel

## Pattern Overview
A sci-fi diagnostic telemetry frame equipped with animated scanlines, cut corners, and a header accent bar.

## Composition Specification
```text
Required:
- CyberTheme.shapes.cardShape
- CyberTheme.colors.surfacePrimary
- Modifier.cyberScanlines (subtle opacity <= 0.2f)
- Modifier.cyberBorder

Optional:
- Modifier.cyberLaserOutliner (on initial presentation)
- CyberDialTicks or CyberSectorRim in corners
```

## Canonical Assembly
```kotlin
@Composable
fun ScannedHudPanel(
    headerTitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(CyberTheme.shapes.cardShape)
            .background(CyberTheme.colors.surfacePrimary)
            .cyberScanlines(
                color = CyberTheme.colors.primary.copy(alpha = 0.12f),
                spacing = 4.dp,
                speed = 0.8f
            )
            .cyberBorder(width = 1.dp, color = CyberTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 14.dp)
                        .background(CyberTheme.colors.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = headerTitle.uppercase(),
                    style = CyberTheme.typography.titleSmall,
                    color = CyberTheme.colors.textPrimary
                )
            }

            content()
        }
    }
}
```
