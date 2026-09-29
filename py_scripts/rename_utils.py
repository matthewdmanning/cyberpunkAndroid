import os

files_to_check = [
    'cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/utils/CyberDrawUtils.kt',
]

for file_path in files_to_check:
    if not os.path.exists(file_path): continue
    with open(file_path, 'r') as f:
        content = f.read()
    
    content = content.replace('neonBorder', 'glowBorder')
    content = content.replace('NeonBorder', 'GlowBorder')
    content = content.replace('Neonborder', 'Glowborder')
    
    with open(file_path, 'w') as f:
        f.write(content)
