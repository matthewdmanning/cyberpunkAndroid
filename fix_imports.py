import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.material.icons.filled.CellTower\n', '')
content = content.replace('import androidx.compose.material.icons.filled.Bolt\n', '')

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
