import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if '.filter { !it.testName.contains("glow") }' in line:
        continue
    if 'testName.contains("overload_timescale")' in line:
        new_lines.append(line)
        new_lines.append('                    testName.contains("overload_bounceamount") -> com.example.cyberpunkandroid.effects.cyberOverload(bounceAmount = value.dp)\n')
        continue
    if 'testName.contains("noise_opacity")' in line:
        new_lines.append(line)
        new_lines.append('                    testName.contains("noise_speed") -> com.example.cyberpunkandroid.effects.cyberNoise(speed = value)\n')
        continue
    if 'testName.contains("datastream_speed")' in line:
        new_lines.append('                    testName.contains("datastream_speed") -> Modifier.cyberDatastream(color = CyberPrimitives.Colors.Cyan500, speed = value, maxAlpha = 0.5f)\n')
        new_lines.append('                    testName.contains("datastream_maxalpha") -> Modifier.cyberDatastream(color = CyberPrimitives.Colors.Cyan500, maxAlpha = value)\n')
        continue
    if 'testName.contains("neonborder_width")' in line:
        new_lines.append(line)
        new_lines.append('                    testName.contains("neonborder_opacity") -> Modifier.cyberNeonBorder(color = CyberPrimitives.Colors.Cyan500.copy(alpha = value), shape = CutCornerShape(8.dp))\n')
        continue
    if 'testName.contains("backdrop_blur_opacity")' in line:
        continue # Avoid duplicates if rerunning
    if 'testName.contains("stripes_opacity")' in line:
        new_lines.append(line)
        new_lines.append('                    testName.contains("stripes_speed") -> Modifier.cyberStripes(speed = value)\n')
        continue
    if 'testName.contains("innerglow_radius")' in line or 'testName.contains("innerglow_opacity")' in line or 'testName.contains("neonborderflow_width")' in line or 'testName.contains("neonborderflow_glowradius")' in line or 'testName.contains("textglow")' in line:
        continue
    if '} else if (testName.contains("neonborder") || testName.contains("innerglow") || testName.contains("textglow")) {' in line:
        new_lines.append('                } else if (testName.contains("neonborder")) {\n')
        continue
    if 'Text("SECURE", color = CyberPrimitives.Colors.Cyan500, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = ' in line:
        continue
    if 'if (testName.contains("textglow")) {' in line or '} else {' in line:
        # We need to be careful with removing the if/else for textglow
        pass
    
    new_lines.append(line)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.writelines(new_lines)
