import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/utils/CyberDrawUtils.kt', 'r') as f:
    content = f.read()

old_draw = """    drawOutline(
        outline = outline,
        brush = sharpBrush,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx)
    )"""

new_draw = """    val sharpPath = androidx.compose.ui.graphics.Path().apply { when(outline) { is androidx.compose.ui.graphics.Outline.Rectangle -> addRect(outline.rect); is androidx.compose.ui.graphics.Outline.Rounded -> addRoundRect(outline.roundRect); is androidx.compose.ui.graphics.Outline.Generic -> addPath(outline.path) } }
    drawPath(
        path = sharpPath,
        brush = sharpBrush,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx)
    )"""

content = content.replace(old_draw, new_draw)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/utils/CyberDrawUtils.kt', 'w') as f:
    f.write(content)
