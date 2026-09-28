import json
import os
import glob

# Remove from master ratings
if os.path.exists('docs/master_ratings.json'):
    with open('docs/master_ratings.json', 'r') as f:
        master = json.load(f)
    
    if 'neonborder_glowradius' in master:
        del master['neonborder_glowradius']
        
    with open('docs/master_ratings.json', 'w') as f:
        json.dump(master, f, indent=4)

# Remove from assets
for asset_file in glob.glob('cyberpunkandroid/src/main/assets/test_neonborder_*.json'):
    os.remove(asset_file)
