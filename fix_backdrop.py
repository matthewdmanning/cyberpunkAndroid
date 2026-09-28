import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if skip:
        if '}' in line and 'else {' not in line and 'Box(' not in line and 'modifier =' not in line and '.size(' not in line and '.background(' not in line and 'contentAlignment =' not in line and '.cyberBackdropBlur(' not in line and '.border(' not in line and 'Text(' not in line:
            # We skip until we hit the end of the backdrop_blur block
            # Actually, this is fragile. Let's use a safer replacement.
            pass
    
    if '} else if (testName.contains("backdrop_blur")) {' in line:
        new_lines.append(line)
        new_lines.append("""                      Box(
                          modifier = Modifier
                              .size(iconSize * 2f)
                              .background(Color(0xFF0F172A)),
                          contentAlignment = Alignment.Center
                      ) {
                          // Text BEHIND the blur
                          Text(
                              "SYSTEM OFFLINE\\nREBOOTING...\\nDATASTREAM ACTIVE\\nWARNING", 
                              color = CyberPrimitives.Colors.Green500, 
                              fontSize = 12.sp,
                              fontWeight = FontWeight.Bold,
                              lineHeight = 16.sp,
                              textAlign = TextAlign.Center
                          )
                          // The blur box floating OVER the text
                          Box(
                              modifier = Modifier
                                  .size(iconSize * 1.5f)
                                  .cyberBackdropBlur(
                                      radius = if (testName.contains("radius")) value.dp else 16.dp, 
                                      tint = Color.Black.copy(alpha = if (testName.contains("opacity")) value else 0.5f)
                                  )
                                  .border(1.dp, CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f))
                          )
                      }\n""")
        skip = True
    else:
        if not skip:
            new_lines.append(line)
            
    if skip and 'Text("BLUR"' in line:
        # The next line is `}`, then `}`, then `}`
        # Let's just wait until we see the else block or similar
        pass
