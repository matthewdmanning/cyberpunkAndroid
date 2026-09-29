import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if 'testName.contains("neonborder_glowradius")' in line:
        new_lines.append(line)
        new_lines.append('                    testName.contains("innerglow_radius") -> com.example.cyberpunkandroid.effects.cyberInnerGlow(color = CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f), radius = value.dp, shape = CutCornerShape(8.dp))\n')
        new_lines.append('                    testName.contains("innerglow_opacity") -> com.example.cyberpunkandroid.effects.cyberInnerGlow(color = CyberPrimitives.Colors.Cyan500.copy(alpha = value), radius = 12.dp, shape = CutCornerShape(8.dp))\n')
        new_lines.append('                    testName.contains("neonborderflow_width") -> com.example.cyberpunkandroid.effects.cyberNeonBorderFlow(shape = CutCornerShape(8.dp), width = value.dp, colors = listOf(CyberPrimitives.Colors.Cyan500, CyberPrimitives.Colors.Magenta500))\n')
        new_lines.append('                    testName.contains("neonborderflow_glowradius") -> com.example.cyberpunkandroid.effects.cyberNeonBorderFlow(shape = CutCornerShape(8.dp), glowRadius = value.dp, colors = listOf(CyberPrimitives.Colors.Cyan500, CyberPrimitives.Colors.Magenta500))\n')
        new_lines.append('                    testName.contains("textglow") -> Modifier // textglow handled separately in the text component itself\n')
    elif '} else if (testName.contains("neonborder")) {' in line:
        new_lines.append(line.replace('neonborder', 'neonborder" || testName.contains("innerglow"'))
    elif 'Text("SECURE"' in line:
        new_lines.append('                         if (testName.contains("textglow")) {\n')
        new_lines.append('                             Text("SECURE", color = CyberPrimitives.Colors.Cyan500, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = com.example.cyberpunkandroid.effects.cyberTextGlow(color = CyberPrimitives.Colors.Cyan500, radius = if (testName.contains("radius")) value.dp else 12.dp, intensity = if (testName.contains("intensity")) value.toInt() else 3))\n')
        new_lines.append('                         } else {\n')
        new_lines.append(line)
        new_lines.append('                         }\n')
    elif 'val isMacroEffect =' in line:
        new_lines.append('                val isMacroEffect = testName.contains("overload") || testName.contains("scanline") || testName.contains("stripe") || testName.contains("noise") || testName.contains("datastream")\n')
    else:
        # Avoid duplicate isMacroEffect
        if 'val isMacroEffect = ' not in line:
            new_lines.append(line)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.writelines(new_lines)
