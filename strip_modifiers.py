import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'r') as f:
    content = f.read()

# I will just write a function to strip out a block starting with 'fun Modifier.NAME(' up to the next 'fun Modifier.' or end of file.
def remove_modifier(name, text):
    start = text.find(f'fun Modifier.{name}(')
    if start == -1: return text
    
    # Also find preceding comments
    comment_start = text.rfind('/**', 0, start)
    if comment_start != -1 and text.find('*/', comment_start) < start:
        start = comment_start
        
    # Also find preceding // -----
    header_start = text.rfind('// ---', 0, start)
    if header_start != -1 and (start - header_start) < 200:
        start = header_start

    next_fun = text.find('fun Modifier.', start + 20)
    if next_fun == -1:
        return text[:start]
    
    # Keep any headers that belong to the next fun
    next_header = text.rfind('// ---', start, next_fun)
    if next_header != -1 and (next_fun - next_header) < 200:
        next_fun = next_header
    elif text.rfind('/**', start, next_fun) != -1:
        next_comment = text.rfind('/**', start, next_fun)
        if text.find('*/', next_comment) < next_fun:
            next_fun = next_comment
            
    return text[:start] + text[next_fun:]

for mod in ['cyberGlowBorder', 'cyberGlowBorderFlow', 'cyberGlowPulse', 'cyberGlow', 'cyberTextGlow', 'cyberInnerGlow', 'cyberAtmosphericGlow']:
    content = remove_modifier(mod, content)
    # Run twice just in case
    content = remove_modifier(mod, content)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'w') as f:
    f.write(content)
