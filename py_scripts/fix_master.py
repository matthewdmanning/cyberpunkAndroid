import sys

with open('docs/master_ratings.json', 'r') as f:
    content = f.read()

content = content.replace('neonborder', 'glowborder')

with open('docs/master_ratings.json', 'w') as f:
    f.write(content)
