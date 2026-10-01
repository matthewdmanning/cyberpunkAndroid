import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

# Add necessary imports
if 'import androidx.compose.material.icons.filled.Warning' not in content:
    content = content.replace('import androidx.compose.material.icons.filled.Settings', 'import androidx.compose.material.icons.filled.Settings\nimport androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.CellTower\nimport androidx.compose.material.icons.filled.Bolt')

if 'import androidx.compose.material3.Text' not in content:
    content = content.replace('import androidx.compose.material3.Icon', 'import androidx.compose.material3.Icon\nimport androidx.compose.material3.Text\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.sp')

old_else_block = """                } else {
                    Box(
                        modifier = Modifier
                            .size(actualIconSize * 1.5f)
                            .background(CyberPrimitives.Colors.Cyan500.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = CyberPrimitives.Colors.Cyan500,
                            modifier = Modifier.size(actualIconSize).then(effectModifier)
                        )
                    }
                }"""

new_else_block = """                } else {
                    Box(
                        modifier = Modifier
                            .size(actualIconSize * 1.5f)
                            .background(CyberPrimitives.Colors.Cyan500.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (testName.contains("blur") || testName.contains("scanlines") || testName.contains("datastream") || testName.contains("stripes") || testName.contains("noise") || testName.contains("overload")) {
                            Text(
                                text = "SYSTEM\\nWARNING",
                                color = CyberPrimitives.Colors.Cyan500,
                                fontWeight = FontWeight.Black,
                                fontSize = (actualIconSize.value / 3).sp,
                                lineHeight = (actualIconSize.value / 2.5).sp,
                                modifier = effectModifier
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CyberPrimitives.Colors.Cyan500,
                                modifier = Modifier.size(actualIconSize).then(effectModifier)
                            )
                        }
                    }
                }"""

content = content.replace(old_else_block, new_else_block)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
