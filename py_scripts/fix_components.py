import os
import glob

components_dir = 'cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/*.kt'

for file_path in glob.glob(components_dir):
    with open(file_path, 'r') as f:
        content = f.read()
    
    if 'cyberNeonBorder' in content or 'CyberNeonBorder' in content:
        content = content.replace('cyberNeonBorder', 'cyberGlowBorder')
        content = content.replace('CyberNeonBorder', 'CyberGlowBorder')
        
        with open(file_path, 'w') as f:
            f.write(content)
