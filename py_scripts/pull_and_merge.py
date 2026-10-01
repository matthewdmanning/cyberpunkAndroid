import subprocess
import json
import os

master_path = 'docs/master_ratings.json'
if os.path.exists(master_path):
    with open(master_path, 'r') as f:
        master = json.load(f)
else:
    master = {}

# List files
try:
    output = subprocess.check_output(['adb', 'shell', 'run-as', 'com.example.sample', 'ls', 'files/']).decode('utf-8')
except Exception as e:
    output = ""

changed = False
for filename in output.split():
    filename = filename.strip()
    if filename.startswith('ratings') and filename.endswith('.json'):
        print(f"Pulling {filename}...")
        try:
            content = subprocess.check_output(['adb', 'shell', 'run-as', 'com.example.sample', 'cat', f'files/{filename}']).decode('utf-8')
            if content.strip():
                data = json.loads(content)
                for k, v in data.items():
                    master[k] = v
                    changed = True
            # Delete from device
            subprocess.check_call(['adb', 'shell', 'run-as', 'com.example.sample', 'rm', f'files/{filename}'])
        except Exception as e:
            print(f"Error processing {filename}: {e}")

if changed:
    with open(master_path, 'w') as f:
        json.dump(master, f, indent=4)
        
    for test_name in master.keys():
        asset_file = f'cyberpunkandroid/src/main/assets/test_{test_name}.json'
        if os.path.exists(asset_file):
            # os.remove(asset_file) # Let the agent manage deletion to avoid race conditions
            print(f"Removed {asset_file} because it is completed.")
