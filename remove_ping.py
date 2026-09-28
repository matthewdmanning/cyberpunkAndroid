import json
import os
import glob

# Remove from master ratings
if os.path.exists('docs/master_ratings.json'):
    with open('docs/master_ratings.json', 'r') as f:
        master = json.load(f)
    
    if 'ping_scale' in master:
        del master['ping_scale']
    if 'ping_decay' in master:
        del master['ping_decay']
        
    with open('docs/master_ratings.json', 'w') as f:
        json.dump(master, f, indent=4)

# Remove from assets
for asset_file in glob.glob('cyberpunkandroid/src/main/assets/test_ping_*.json'):
    os.remove(asset_file)
