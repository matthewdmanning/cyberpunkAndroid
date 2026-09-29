import sys
import re

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

pattern = r'\.border\(1\.dp, CyberPrimitives\.Colors\.Cyan500\.copy\(alpha = 0\.5f\)\)\n\s*\.then\(effectModifier\)'
replacement = '.then(effectModifier)\n                            .border(1.dp, CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f))'

new_content = re.sub(pattern, replacement, content)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(new_content)
