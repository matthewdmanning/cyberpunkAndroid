import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'r') as f:
    content = f.read()

content = content.replace('import com.example.cyberpunkandroid.utils.drawGlowBorderFlow\n', '')
content = content.replace('import com.example.cyberpunkandroid.utils.drawCyberGlow\n', '')

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'w') as f:
    f.write(content)
