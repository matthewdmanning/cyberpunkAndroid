import sys
import re

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

pattern = r'\} else if \(testName\.contains\("backdrop_blur"\)\) \{[\s\S]*?Text\("BLUR"[^\)]+\)\s*\}\s*\}'
replacement = '''} else if (testName.contains("backdrop_blur")) {
                    Box(
                        modifier = Modifier
                            .size(iconSize * 2f)
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            "SYSTEM OFFLINE\\nREBOOTING...\\nDATASTREAM\\nWARNING", 
                            color = CyberPrimitives.Colors.Green500, 
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .size(iconSize * 1.5f)
                                .cyberBackdropBlur(
                                    radius = if (testName.contains("radius")) value.dp else 16.dp, 
                                    tint = Color.Black.copy(alpha = if (testName.contains("opacity")) value else 0.5f)
                                )
                                .border(1.dp, CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f))
                        )
                    }'''

new_content = re.sub(pattern, replacement, content)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(new_content)
