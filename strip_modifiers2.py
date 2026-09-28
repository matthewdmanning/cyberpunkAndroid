import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'r') as f:
    lines = f.readlines()

modifiers_to_remove = [
    'fun Modifier.cyberGlowBorder(',
    'fun Modifier.cyberGlowBorderFlow(',
    'fun Modifier.cyberGlow(',
    'fun Modifier.cyberTextGlow(',
    'fun Modifier.cyberInnerGlow(',
    'fun Modifier.cyberAtmosphericGlow(',
    'fun Modifier.cyberGlowPulse('
]

new_lines = []
skip = False
for line in lines:
    if any(line.startswith(m) for m in modifiers_to_remove):
        skip = True
    elif skip and line.startswith('fun Modifier.'):
        skip = False
        
    if not skip:
        new_lines.append(line)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'w') as f:
    f.writelines(new_lines)
