import os

files_to_check = [
    'cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt',
    'sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt',
    'docs/test_drive_plan.md',
    '.agents/rules/usage_notes.md',
    '.agents/rules/effects.md'
]

for file_path in files_to_check:
    if not os.path.exists(file_path): continue
    with open(file_path, 'r') as f:
        content = f.read()
    
    content = content.replace('cyberNeonBorder', 'cyberGlowBorder')
    content = content.replace('CyberNeonBorder', 'CyberGlowBorder')
    content = content.replace('neonborder', 'glowborder')
    content = content.replace('NeonBorder', 'GlowBorder')
    content = content.replace('Neonborder', 'Glowborder')
    
    with open(file_path, 'w') as f:
        f.write(content)
