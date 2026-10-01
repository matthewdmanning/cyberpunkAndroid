import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

replacement = """                  val baseIconSize = iconSize
                  val actualIconSize = when {
                      testName.contains("doe_bounce") -> DoEMatrices.bounceRuns.getOrNull(value.toInt())?.size ?: baseIconSize
                      testName.contains("doe_float") -> DoEMatrices.floatRuns.getOrNull(value.toInt())?.size ?: baseIconSize
                      else -> baseIconSize
                  }"""

content = content.replace('val isMacroEffect = testName.contains("overload")', replacement + '\n                  val isMacroEffect = testName.contains("overload")')
content = content.replace('Modifier.size(iconSize).then(effectModifier)', 'Modifier.size(actualIconSize).then(effectModifier)')
content = content.replace('Modifier.size(iconSize * 1.5f)', 'Modifier.size(actualIconSize * 1.5f)')
content = content.replace('Modifier.size(iconSize * 2f)', 'Modifier.size(actualIconSize * 2f)')

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
