import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/utils/CyberDrawUtils.kt', 'r') as f:
    content = f.read()

def remove_fun(name, text):
    start = text.find(f'fun DrawScope.{name}(')
    if start == -1: return text
    
    next_fun = text.find('fun ', start + 20)
    if next_fun == -1:
        return text[:start]
        
    return text[:start] + text[next_fun:]

content = remove_fun('drawCyberGlow', content)
content = remove_fun('drawGlowBorderFlow', content)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/utils/CyberDrawUtils.kt', 'w') as f:
    f.write(content)
