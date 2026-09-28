import json
import os
import glob

master_path = 'docs/master_ratings.json'
if os.path.exists(master_path):
    with open(master_path, 'r') as f:
        master = json.load(f)
else:
    master = {}

pulled_files = ['docs/ratings.json'] + glob.glob('docs/ratings_archive_*.json')

changed = False
for path in pulled_files:
    if not os.path.exists(path): continue
    try:
        if os.path.getsize(path) > 0:
            with open(path, 'r') as f:
                data = json.load(f)
            for k, v in data.items():
                master[k] = v
                changed = True
        os.remove(path)
    except Exception as e:
        pass

if changed:
    with open(master_path, 'w') as f:
        json.dump(master, f, indent=4)
        
    for test_name in master.keys():
        asset_file = f'cyberpunkandroid/src/main/assets/test_{test_name}.json'
        if os.path.exists(asset_file):
            os.remove(asset_file)
            print(f"Removed {asset_file} because it is completed.")
