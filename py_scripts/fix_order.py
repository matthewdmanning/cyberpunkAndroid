import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

old_block = """                        modifier = Modifier
                            .size(iconSize * 2.5f)
                            .padding(4.dp)
                            .then(effectModifier)
                            .background(Color(0xFF0F172A))
                            .border(1.dp, CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f)),"""

new_block = """                        modifier = Modifier
                            .size(iconSize * 2.5f)
                            .padding(4.dp)
                            .background(Color(0xFF0F172A))
                            .then(effectModifier)
                            .border(1.dp, CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f)),"""

content = content.replace(old_block, new_block)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
