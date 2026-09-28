import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'r') as f:
    content = f.read()

import re

# Find the start and end of cyberGlow
start_idx = content.find('fun Modifier.cyberGlow(')
end_idx = content.find('fun Modifier.cyberBorder(', start_idx)

if start_idx != -1 and end_idx != -1:
    old_glow = content[start_idx:end_idx]
    
    new_glow = """fun Modifier.cyberGlow(
    color: Color = Color.Unspecified,
    radius: Dp = CyberPrimitives.Spacing.dp24,
    shape: Shape? = null,
    intensity: Float = CyberConfig.Effects.GlowIntensity,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberGlow", appendedA11y, customA11y).drawWithCache {
    val fallbackColor = if (color == Color.Unspecified) CyberPrimitives.Colors.Cyan500 else color
    val radiusPx = radius.toPx()

    val paint = androidx.compose.ui.graphics.Paint().apply {
        this.color = fallbackColor
        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(
            radiusPx.coerceAtLeast(1f),
            android.graphics.BlurMaskFilter.Blur.NORMAL
        )
    }

    onDrawWithContent {
        if (radiusPx > 0f) {
            val passes = intensity.toInt().coerceAtLeast(1)
            drawIntoCanvas { canvas ->
                if (shape != null) {
                    val outline = shape.createOutline(size, layoutDirection, this)
                    repeat(passes) {
                        canvas.drawOutline(outline, paint)
                    }
                } else {
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                    val baseRadius = (size.width.coerceAtLeast(size.height) / 2f) * 0.8f
                    repeat(passes) {
                        canvas.drawCircle(center, baseRadius, paint)
                    }
                }
            }
        }
        drawContent()
    }
}

/**
 * Draws a sharp high-tech border.
 */
"""
    
    content = content.replace(old_glow, new_glow)
    
    with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'w') as f:
        f.write(content)
else:
    print("Could not find cyberGlow block")
