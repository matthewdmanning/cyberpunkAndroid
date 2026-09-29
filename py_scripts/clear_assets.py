import glob
import os

for asset_file in glob.glob('cyberpunkandroid/src/main/assets/test_*.json'):
    os.remove(asset_file)
