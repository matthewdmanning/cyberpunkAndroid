import os
import glob

dirs_to_check = [
    'sample/src/main/java/com/example/sample/screens/*.kt',
    'sample/src/main/java/com/example/sample/components/*.kt'
]

for d in dirs_to_check:
    for file_path in glob.glob(d):
        with open(file_path, 'r') as f:
            content = f.read()
        
        if 'cyberNeonBorder' in content or 'CyberNeonBorder' in content:
            content = content.replace('cyberNeonBorder', 'cyberGlowBorder')
            content = content.replace('CyberNeonBorder', 'CyberGlowBorder')
            
            with open(file_path, 'w') as f:
                f.write(content)
