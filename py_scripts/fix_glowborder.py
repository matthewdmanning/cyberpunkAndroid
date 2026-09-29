import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'r') as f:
    content = f.read()

old_glow_border = """fun Modifier.cyberGlowBorder(
    color: Color,
    shape: Shape,
    width: Dp = CyberPrimitives.BorderWidths.dp2,
    glowRadius: Dp = CyberPrimitives.Spacing.dp8,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this
    .cyberSemantics("CyberGlowBorder", appendedA11y, customA11y)
    .border(width, color, shape)
    .border(width + glowRadius, color.copy(alpha = 0.25f), shape)"""

new_glow_border = """fun Modifier.cyberGlowBorder(
    color: Color,
    shape: Shape,
    width: Dp = CyberPrimitives.BorderWidths.dp2,
    glowRadius: Dp = CyberPrimitives.Spacing.dp8,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberGlowBorder", appendedA11y, customA11y).drawWithCache {
    val widthPx = width.toPx()
    val glowRadiusPx = glowRadius.toPx()
    
    val outline = shape.createOutline(size, layoutDirection, this)
    
    val glowPaint = androidx.compose.ui.graphics.Paint().apply {
        this.color = color
        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
        this.strokeWidth = widthPx
        this.asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(
            glowRadiusPx.coerceAtLeast(1f),
            android.graphics.BlurMaskFilter.Blur.NORMAL
        )
    }
    
    val solidPaint = androidx.compose.ui.graphics.Paint().apply {
        this.color = color
        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
        this.strokeWidth = widthPx
    }

    onDrawWithContent {
        if (glowRadiusPx > 0f) {
            drawIntoCanvas { canvas ->
                canvas.drawOutline(outline, glowPaint)
            }
        }
        drawContent()
        drawIntoCanvas { canvas ->
            canvas.drawOutline(outline, solidPaint)
        }
    }
}"""

content = content.replace(old_glow_border, new_glow_border)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'w') as f:
    f.write(content)
