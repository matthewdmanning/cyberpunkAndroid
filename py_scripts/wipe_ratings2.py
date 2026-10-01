import sys
import json

with open('docs/master_ratings.json', 'r') as f:
    data = json.load(f)

to_remove = ["backdrop_blur_opacity", "datastream_maxalpha", "neonborder_opacity", "neonborder_glowradius", "stripes_speed"]

for key in to_remove:
    if key in data:
        del data[key]

with open('docs/master_ratings.json', 'w') as f:
    json.dump(data, f, indent=4)
