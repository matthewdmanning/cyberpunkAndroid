import sys
import json

with open('docs/master_ratings.json', 'r') as f:
    data = json.load(f)

# The tests to remove
to_remove = ["backdrop_blur_opacity", "datastream_maxalpha", "neonborder_opacity", "neonborder_glowradius", "stripes_speed"]

new_tests = []
for test in data['tests']:
    if test['test_name'] not in to_remove:
        new_tests.append(test)

data['tests'] = new_tests

with open('docs/master_ratings.json', 'w') as f:
    json.dump(data, f, indent=4)
