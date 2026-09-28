import sys
import re

# Clean DoEMatrices.kt
with open('sample/src/main/java/com/example/sample/screens/DoEMatrices.kt', 'r') as f:
    content = f.read()

content = re.sub(r'// 5\. GlowBorder.*?// 6\. Glow.*?(?=// 7\. Datastream)', '', content, flags=re.DOTALL)

with open('sample/src/main/java/com/example/sample/screens/DoEMatrices.kt', 'w') as f:
    f.write(content)

# Clean FeedbackFormScreen.kt
with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

# Remove glow mappings
content = re.sub(r'\s*testName\.contains\("doe_glowborder"\).*?Modifier\.cyberGlowBorderFlow.*?\}', '', content, flags=re.DOTALL)
content = re.sub(r'\s*testName\.contains\("doe_glow"\).*?Modifier\.cyberGlow.*?\}', '', content, flags=re.DOTALL)
content = re.sub(r'\s*testName\.contains\("glow_.*?\}', '', content, flags=re.DOTALL)
content = re.sub(r'\s*testName\.contains\("glowborder_.*?\}', '', content, flags=re.DOTALL)

# Remove the empty container block for glowborder
glowborder_container = """                } else if (testName.contains("glowborder")) {
                    Box(
                        modifier = Modifier
                            .size(actualIconSize * 1.5f)
                            .background(CyberPrimitives.Colors.Cyan500.copy(alpha = 0.1f), shape = CutCornerShape(8.dp))
                            .then(effectModifier)
                    )"""
content = content.replace(glowborder_container, "")

# Remove imports
content = re.sub(r'import com\.example\.cyberpunkandroid\.effects\..*?Glow.*?\n', '', content)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
