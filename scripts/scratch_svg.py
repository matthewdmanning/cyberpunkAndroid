import os
import glob
from svgelements import SVG, Shape

svg_dir = 'C:/GitHub/cybercore_svgs'
files = glob.glob(os.path.join(svg_dir, '*.svg'))

for file in files[:2]:
    print(f"Parsing {file}")
    svg = SVG.parse(file)
    for element in svg.elements():
        if isinstance(element, Shape):
            path = abs(element) # Convert to Path
            print("Path:", path.d())
