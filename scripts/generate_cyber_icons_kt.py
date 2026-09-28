import os
import glob

def to_camel_case(snake_str):
    components = snake_str.split('_')
    return ''.join(x.title() for x in components)

files = glob.glob('cyberpunkandroid/src/main/res/drawable/cyber_ic_*.xml')

out_path = 'cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/icons/CyberIcons.kt'
os.makedirs(os.path.dirname(out_path), exist_ok=True)

with open(out_path, 'w', encoding='utf-8') as f:
    f.write('package com.example.cyberpunkandroid.icons\n\n')
    f.write('import com.example.cyberpunkandroid.R\n\n')
    f.write('/**\n')
    f.write(' * CyberpunkAndroid Icons Registry.\n')
    f.write(' * Generated automatically from cybercore_svgs.\n')
    f.write(' */\n')
    f.write('object CyberIcons {\n')
    for file in sorted(files):
        filename = os.path.basename(file)
        name = filename.replace('cyber_ic_', '').replace('.xml', '')
        camel_name = to_camel_case(name)
        f.write(f'    val {camel_name} = R.drawable.cyber_ic_{name}\n')
    
    f.write('}\n')

print("CyberIcons.kt generated.")
