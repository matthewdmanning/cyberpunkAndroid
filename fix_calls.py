import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'com.example.cyberpunkandroid.effects.cyberInnerGlow',
    'Modifier.cyberInnerGlow'
).replace(
    'com.example.cyberpunkandroid.effects.cyberNeonBorderFlow',
    'Modifier.cyberNeonBorderFlow'
).replace(
    'com.example.cyberpunkandroid.effects.cyberTextGlow',
    'Modifier.cyberTextGlow'
)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
