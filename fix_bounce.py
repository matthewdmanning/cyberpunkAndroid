import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'r') as f:
    content = f.read()

content = content.replace('offsetY: Dp = CyberPrimitives.Spacing.dp16', 'height: Dp = CyberPrimitives.Spacing.dp16')
content = content.replace('translationY = (progress.value - 1f) * offsetY.toPx()', 'translationY = (progress.value - 1f) * height.toPx()')

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'w') as f:
    f.write(content)
