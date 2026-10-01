import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'com.example.cyberpunkandroid.effects.cyberOverload',
    'Modifier.cyberOverload'
).replace(
    'com.example.cyberpunkandroid.effects.cyberNoise',
    'Modifier.cyberNoise'
)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
