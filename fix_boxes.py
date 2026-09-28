import sys
import re

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

old_block = """                if (isMacroEffect) {
                    Box(
                        modifier = Modifier
                            .size(iconSize * 2.5f)
                            .padding(4.dp)
                            .background(Color(0xFF0F172A))
                            .then(effectModifier)
                            .border(1.dp, CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SYSTEM", color = CyberPrimitives.Colors.Cyan500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("ONLINE", color = CyberPrimitives.Colors.Green500, fontSize = 10.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(iconSize * 2f)
                            .background(Color(0xFF0F172A))
                            .then(effectModifier),
                        contentAlignment = Alignment.Center
                    ) {
                         Text("SECURE", color = CyberPrimitives.Colors.Cyan500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }"""

new_block = """                if (isMacroEffect) {
                    Box(
                        modifier = Modifier
                            .size(iconSize * 2.5f)
                            .padding(4.dp)
                            .border(1.dp, CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f)) // Outer border
                            .then(effectModifier) // Shader
                            .background(Color(0xFF0F172A)), // Inner background
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SYSTEM", color = CyberPrimitives.Colors.Cyan500, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("ONLINE", color = CyberPrimitives.Colors.Green500, fontSize = 10.sp)
                        }
                    }
                }"""

content = content.replace(old_block, new_block)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
