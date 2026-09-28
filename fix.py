import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if 'testName.contains("bounce_offset")' in line and not skip:
        skip = True
        new_lines.append(line)
        new_lines.append('                                    offsetY = value.dp\n')
        new_lines.append('                                )\n')
        new_lines.append('                                testName.contains("overload_intensity") -> Modifier.cyberOverload(intensity = value)\n')
        new_lines.append('                                testName.contains("overload_timescale") -> Modifier.cyberOverload(timeScale = value)\n')
        new_lines.append('                                testName.contains("scanlines_opacity") -> Modifier.cyberScanlines(opacity = value)\n')
        new_lines.append('                                testName.contains("scanlines_spacing") -> Modifier.cyberScanlines(spacing = value.dp)\n')
        new_lines.append('                                testName.contains("scanlines_speed") -> Modifier.cyberScanlines(speed = value)\n')
        new_lines.append('                                testName.contains("stripes_width") -> Modifier.cyberStripes(stripeWidth = value.dp)\n')
        new_lines.append('                                testName.contains("stripes_opacity") -> Modifier.cyberStripes(color = Color(0xFFFFFFFF).copy(alpha = value))\n')
        new_lines.append('                                testName.contains("noise_opacity") -> Modifier.cyberNoise(opacity = value)\n')
        new_lines.append('                                testName.contains("datastream_speed") -> Modifier.cyberDatastream(color = CyberPrimitives.Colors.Cyan500, speed = value, maxAlpha = 0.5f)\n')
        new_lines.append('                                testName.contains("neonborder_width") -> Modifier.cyberNeonBorder(color = CyberPrimitives.Colors.Cyan500, shape = CutCornerShape(8.dp), width = value.dp)\n')
        new_lines.append('                                testName.contains("neonborder_glowradius") -> Modifier.cyberNeonBorder(color = CyberPrimitives.Colors.Cyan500, shape = CutCornerShape(8.dp), glowRadius = value.dp)\n')
        new_lines.append('                                else -> Modifier\n')
        new_lines.append('                            }\n')
        new_lines.append('                            \n')
        new_lines.append('                            val isMacroEffect = testName.contains("overload") || testName.contains("scanline") || testName.contains("stripe") || testName.contains("noise") || testName.contains("datastream")\n')
        new_lines.append('                            \n')
        new_lines.append('                            if (isMacroEffect) {\n')
        new_lines.append('                                Box(\n')
        new_lines.append('                                    modifier = Modifier.size(iconSize * 2.5f).padding(4.dp).then(effectModifier).background(Color(0xFF0F172A)).border(1.dp, CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f)),\n')
        new_lines.append('                                    contentAlignment = Alignment.Center\n')
        new_lines.append('                                ) {\n')
        new_lines.append('                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {\n')
        new_lines.append('                                        Text("SYSTEM", color = CyberPrimitives.Colors.Cyan500, fontSize = 12.sp, fontWeight = FontWeight.Bold)\n')
        new_lines.append('                                        Text("ONLINE", color = CyberPrimitives.Colors.Green500, fontSize = 10.sp)\n')
        new_lines.append('                                    }\n')
        new_lines.append('                                }\n')
        new_lines.append('                            } else if (testName.contains("neonborder")) {\n')
        new_lines.append('                                Box(\n')
        new_lines.append('                                    modifier = Modifier.size(iconSize * 2f).background(Color(0xFF0F172A)).then(effectModifier),\n')
        new_lines.append('                                    contentAlignment = Alignment.Center\n')
        new_lines.append('                                ) {\n')
        new_lines.append('                                     Text("SECURE", color = CyberPrimitives.Colors.Cyan500, fontSize = 12.sp, fontWeight = FontWeight.Bold)\n')
        new_lines.append('                                }\n')
        new_lines.append('                            } else {\n')
        new_lines.append('                                Box(modifier = Modifier.size(iconSize * 1.5f).background(CyberPrimitives.Colors.Cyan500.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {\n')
        new_lines.append('                                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = CyberPrimitives.Colors.Cyan500, modifier = Modifier.size(iconSize).then(effectModifier))\n')
        new_lines.append('                                }\n')
        new_lines.append('                            }\n')
        new_lines.append('                        }\n')
        continue
    
    if skip and 'modifier = Modifier.size(iconSize).then(effectModifier)' in line:
        # We need to skip until the end of the else block
        skip = False
        # actually, skip until                         }
        pass
    elif skip:
        if '                        }' in line:
            skip = False
        continue
    else:
        new_lines.append(line)

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.writelines(new_lines)
