import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('testName.contains("bounce_offset") -> Modifier.cyberBounce(offsetY = value.dp)', 'testName.contains("bounce_offset") -> Modifier.cyberBounce(height = value.dp)')

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
