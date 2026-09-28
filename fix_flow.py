import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/utils/CyberDrawUtils.kt', 'r') as f:
    content = f.read()

old_draw = """fun DrawScope.drawGlowBorderFlow(
    outline: Outline,
    sharpBrush: Brush,
    glowBrush: Brush,
    strokeWidthPx: Float,
    glowWidthPx: Float
) {
    drawShapeOutline(outline, glowBrush, Stroke(width = glowWidthPx))
    drawShapeOutline(outline, sharpBrush, Stroke(width = strokeWidthPx))
}"""

new_draw = """fun DrawScope.drawGlowBorderFlow(
    outline: Outline,
    sharpBrush: Brush,
    glowBrush: Brush,
    strokeWidthPx: Float,
    glowWidthPx: Float
) {
    val glowPaint = androidx.compose.ui.graphics.Paint().apply {
        val sweepBrush = glowBrush as? androidx.compose.ui.graphics.ShaderBrush
        if (sweepBrush != null) {
            this.shader = sweepBrush.createShader(size)
        }
        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
        this.strokeWidth = strokeWidthPx
        this.asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(
            glowWidthPx.coerceAtLeast(1f),
            android.graphics.BlurMaskFilter.Blur.NORMAL
        )
    }
    drawIntoCanvas { canvas ->
        canvas.drawOutline(outline, glowPaint)
    }
    
    drawOutline(
        outline = outline,
        brush = sharpBrush,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx)
    )
}"""

content = content.replace(old_draw, new_draw)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/utils/CyberDrawUtils.kt', 'w') as f:
    f.write(content)
