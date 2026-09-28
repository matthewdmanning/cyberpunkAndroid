import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'r') as f:
    content = f.read()

content = content.replace('translationY = -offsetY.toPx() * progress.value', 'translationY = -height.toPx() * progress.value')

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'w') as f:
    f.write(content)
