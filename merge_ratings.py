import json
import os
import glob

master_path = 'docs/master_ratings.json'
if os.path.exists(master_path):
    with open(master_path, 'r') as f:
        master = json.load(f)
else:
    master = {}

# We merge device_ratings.json and any ratings_archive*.json if we pulled them
pulled_files = ['docs/device_ratings.json'] + glob.glob('docs/ratings_archive_*.json')

for path in pulled_files:
    if not os.path.exists(path): continue
    try:
        with open(path, 'r') as f:
            data = json.load(f)
        for k, v in data.items():
            master[k] = v
        os.remove(path)
    except Exception as e:
        print(f"Error reading {path}: {e}")

with open(master_path, 'w') as f:
    json.dump(master, f, indent=4)

# Delete corresponding asset files
for test_name in master.keys():
    asset_file = f'sample/src/main/assets/test_{test_name}.json'
    if os.path.exists(asset_file):
        os.remove(asset_file)
        print(f"Removed {asset_file} because it is completed.")

