import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

old_block = """                } else if (testName.contains("backdrop_blur")) {
                    Box(
                        modifier = Modifier
                            .size(iconSize * 1.5f)
                            .background(CyberPrimitives.Colors.Yellow500),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(iconSize)
                                .cyberBackdropBlur(radius = value.dp, tint = Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Text("BLUR", color = Color.White, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }"""

new_block = """                } else if (testName.contains("backdrop_blur")) {
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
                    }
                }"""

content = content.replace(old_block, new_block)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
