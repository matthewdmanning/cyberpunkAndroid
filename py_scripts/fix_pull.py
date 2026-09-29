import sys

with open('pull_and_merge.py', 'r') as f:
    content = f.read()

content = content.replace('os.remove(asset_file)', '# os.remove(asset_file) # Let the agent manage deletion to avoid race conditions')

with open('pull_and_merge.py', 'w') as f:
    f.write(content)
