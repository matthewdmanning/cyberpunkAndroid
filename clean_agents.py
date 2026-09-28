import sys

with open('AGENTS.md', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "### Effects Rules" in line or "### Feedback Flow" in line:
        skip = True
    elif skip and line.startswith("### "):
        skip = False
        
    if not skip:
        new_lines.append(line)

with open('AGENTS.md', 'w') as f:
    f.writelines(new_lines)
