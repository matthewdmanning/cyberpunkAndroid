import os
import glob
import xml.etree.ElementTree as ET
from svgelements import SVG, Shape

svg_dir = 'C:/GitHub/cybercore_svgs'
out_dir = 'cyberpunkandroid/src/main/res/drawable'

os.makedirs(out_dir, exist_ok=True)
files = glob.glob(os.path.join(svg_dir, '*.svg'))

print(f"Found {len(files)} SVGs.")

for file in files:
    filename = os.path.basename(file)
    name, ext = os.path.splitext(filename)
    out_name = f"cyber_ic_{name.replace('-', '_')}.xml"
    out_path = os.path.join(out_dir, out_name)
    
    svg = SVG.parse(file)
    paths = []
    
    for element in svg.elements():
        if isinstance(element, Shape):
            path_str = abs(element).d()
            if path_str:
                paths.append(path_str)
                
    with open(out_path, 'w', encoding='utf-8') as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n')
        f.write('<vector xmlns:android="http://schemas.android.com/apk/res/android"\n')
        f.write('    android:width="24dp"\n')
        f.write('    android:height="24dp"\n')
        f.write('    android:viewportWidth="24"\n')
        f.write('    android:viewportHeight="24">\n')
        
        for p in paths:
            f.write('    <path\n')
            f.write('        android:fillColor="#00000000"\n')
            f.write('        android:strokeColor="#FFFFFFFF"\n')
            f.write('        android:strokeWidth="1.5"\n')
            f.write('        android:strokeLineCap="round"\n')
            f.write('        android:strokeLineJoin="round"\n')
            f.write(f'        android:pathData="{p}" />\n')
            
        f.write('</vector>\n')

print("Conversion complete.")
