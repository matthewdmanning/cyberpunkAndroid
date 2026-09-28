import os
import glob
import re

components_dir = 'cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/*.kt'

for file_path in glob.glob(components_dir):
    with open(file_path, 'r') as f:
        content = f.read()
    
    # Remove cyberGlowBorder imports, and add cyberBorder if missing
    if 'import com.example.cyberpunkandroid.effects.cyberGlowBorder' in content:
        content = content.replace('import com.example.cyberpunkandroid.effects.cyberGlowBorder', '')
        if 'import com.example.cyberpunkandroid.effects.cyberBorder' not in content:
            content = content.replace('import com.example.cyberpunkandroid.effects.', 'import com.example.cyberpunkandroid.effects.cyberBorder\nimport com.example.cyberpunkandroid.effects.', 1)

    # Replace cyberGlowBorder(color = X, shape = Y) with cyberBorder(color = X, shape = Y)
    # The arguments are mostly the same, just remove glowRadius if present.
    content = re.sub(r'\.cyberGlowBorder\(\s*color\s*=\s*(.*?),\s*shape\s*=\s*(.*?)\s*(?:,\s*glowRadius\s*=\s*.*?)?\)', r'.cyberBorder(color = \1, shape = \2)', content, flags=re.DOTALL)
    content = re.sub(r'\.cyberGlowBorder\(\s*color\s*=\s*(.*?),\s*shape\s*=\s*(.*?),\s*width\s*=\s*(.*?)\s*(?:,\s*glowRadius\s*=\s*.*?)?\)', r'.cyberBorder(color = \1, shape = \2, width = \3)', content, flags=re.DOTALL)
    
    with open(file_path, 'w') as f:
        f.write(content)

