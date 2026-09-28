import sys
import re

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'\} else \{\s*Text\("SECURE"[^\)]+\)\s*\}', 'Text("SECURE", color = CyberPrimitives.Colors.Cyan500, fontSize = 12.sp, fontWeight = FontWeight.Bold)', content)
# Also clean up any lingering `if (testName.contains("textglow")) {` if it's there
content = re.sub(r'if \(testName\.contains\("textglow"\)\) \{\s*\} else \{', '', content)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
