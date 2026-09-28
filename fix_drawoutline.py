import sys

files = [
    'cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt',
    'cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/utils/CyberDrawUtils.kt'
]

for file_path in files:
    with open(file_path, 'r') as f:
        content = f.read()

    # Convert outline to path
    conversion = """val path = androidx.compose.ui.graphics.Path().apply {
                        when (outline) {
                            is androidx.compose.ui.graphics.Outline.Rectangle -> addRect(outline.rect)
                            is androidx.compose.ui.graphics.Outline.Rounded -> addRoundRect(outline.roundRect)
                            is androidx.compose.ui.graphics.Outline.Generic -> addPath(outline.path)
                        }
                    }"""

    # In CyberModifiers.kt cyberGlowBorder:
    if 'canvas.drawOutline(outline, glowPaint)' in content:
        content = content.replace('canvas.drawOutline(outline, glowPaint)', 'val p = androidx.compose.ui.graphics.Path().apply { when(outline) { is androidx.compose.ui.graphics.Outline.Rectangle -> addRect(outline.rect); is androidx.compose.ui.graphics.Outline.Rounded -> addRoundRect(outline.roundRect); is androidx.compose.ui.graphics.Outline.Generic -> addPath(outline.path) } }; canvas.drawPath(p, glowPaint)')
        
    if 'canvas.drawOutline(outline, solidPaint)' in content:
        content = content.replace('canvas.drawOutline(outline, solidPaint)', 'val p = androidx.compose.ui.graphics.Path().apply { when(outline) { is androidx.compose.ui.graphics.Outline.Rectangle -> addRect(outline.rect); is androidx.compose.ui.graphics.Outline.Rounded -> addRoundRect(outline.roundRect); is androidx.compose.ui.graphics.Outline.Generic -> addPath(outline.path) } }; canvas.drawPath(p, solidPaint)')
        
    if 'canvas.drawOutline(outline, paint)' in content:
        content = content.replace('canvas.drawOutline(outline, paint)', 'val p = androidx.compose.ui.graphics.Path().apply { when(outline) { is androidx.compose.ui.graphics.Outline.Rectangle -> addRect(outline.rect); is androidx.compose.ui.graphics.Outline.Rounded -> addRoundRect(outline.roundRect); is androidx.compose.ui.graphics.Outline.Generic -> addPath(outline.path) } }; canvas.drawPath(p, paint)')

    with open(file_path, 'w') as f:
        f.write(content)

