import os
import glob
import re

components_dir = 'cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/components/*.kt'

for file_path in glob.glob(components_dir):
    with open(file_path, 'r') as f:
        content = f.read()
    
    if 'cyberGlowBorder' in content or 'cyberGlowBorderFlow' in content:
        # We need to remove the modifier call and the import.
        content = re.sub(r'\.cyberGlowBorder.*?\(.*?\)', '', content, flags=re.DOTALL)
        content = re.sub(r'\.cyberGlowBorderFlow.*?\(.*?\)', '', content, flags=re.DOTALL)
        content = re.sub(r'import com\.example\.cyberpunkandroid\.effects\.cyberGlowBorder.*?\n', '', content)
        
        # Clean up dangling dots if we removed something like `.cyberGlowBorder(...)`
        # e.g., `Modifier.cyberGlowBorder().padding()` -> `Modifier.padding()`
        # Actually a simpler string replacement for just the function call is safer.
        pass

