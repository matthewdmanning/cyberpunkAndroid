import os
import glob

components_dir = 'cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/*.kt'

for file_path in glob.glob(components_dir):
    with open(file_path, 'r') as f:
        content = f.read()
    
    if 'cyberBorder' in content and 'import com.example.cyberpunkandroid.effects.cyberBorder' not in content:
        content = content.replace('import androidx.compose.ui.Modifier\n', 'import androidx.compose.ui.Modifier\nimport com.example.cyberpunkandroid.effects.cyberBorder\n')
        with open(file_path, 'w') as f:
            f.write(content)
