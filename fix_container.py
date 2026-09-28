import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/CyberContainer.kt', 'r') as f:
    content = f.read()

content = content.replace("CyberPrimitives.Colors.Slate900", "Color(0xFF0F172A)")

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/CyberContainer.kt', 'w') as f:
    f.write(content)
