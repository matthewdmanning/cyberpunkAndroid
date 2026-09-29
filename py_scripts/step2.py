import json
import os
import glob

master = json.load(open('docs/master_ratings.json'))
completed = [k for k, v in master.items() if all(rating != -1 for rating in v.values())]

asset_files = glob.glob('cyberpunkandroid/src/main/assets/test_*.json')

for asset_file in asset_files:
    test_name = os.path.basename(asset_file).replace('test_', '').replace('.json', '')
    if test_name in completed:
        os.remove(asset_file)
        print(f"Agent deleted completed test: {test_name}")
