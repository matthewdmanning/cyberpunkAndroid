import sys
import re

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/CyberNavigationBar.kt', 'r') as f:
    content = f.read()

content = content.replace('import com.example.cyberpunkandroid.effects.cyberTextGlow', '')
content = re.sub(r'\.cyberTextGlow\([^)]*\)', '', content, flags=re.DOTALL)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/CyberNavigationBar.kt', 'w') as f:
    f.write(content)
