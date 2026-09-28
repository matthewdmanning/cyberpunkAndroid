import sys

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'r') as f:
    content = f.read()

doe_mappings = """                    // DoE Mappings
                    testName.contains("doe_bounce") -> {
                        val run = DoEMatrices.bounceRuns.getOrNull(value.toInt()) ?: DoEMatrices.bounceRuns[0]
                        Modifier.cyberBounce(height = run.height, animationSpec = infiniteRepeatable(tween(run.duration, easing = run.easing), RepeatMode.Reverse))
                    }
                    testName.contains("doe_float") -> {
                        val run = DoEMatrices.floatRuns.getOrNull(value.toInt()) ?: DoEMatrices.floatRuns[0]
                        Modifier.cyberFloat(height = run.height, durationMillis = run.duration) // Float doesn't explicitly expose easing in signature, using duration
                    }
                    testName.contains("doe_blur") -> {
                        val run = DoEMatrices.blurRuns.getOrNull(value.toInt()) ?: DoEMatrices.blurRuns[0]
                        Modifier.cyberBackdropBlur(radius = run.radius, tint = Color(0x1AFFFFFF).copy(alpha = run.opacity))
                    }
                    testName.contains("doe_stripes") -> {
                        val run = DoEMatrices.stripesRuns.getOrNull(value.toInt()) ?: DoEMatrices.stripesRuns[0]
                        Modifier.cyberStripes(speed = run.speed, stripeWidth = run.size)
                    }
                    testName.contains("doe_glowborder") -> {
                        val run = DoEMatrices.glowBorderRuns.getOrNull(value.toInt()) ?: DoEMatrices.glowBorderRuns[0]
                        val colors = if (run.multiColor) {
                            listOf(CyberPrimitives.Colors.Cyan500.copy(alpha = run.opacity), CyberPrimitives.Colors.Magenta500.copy(alpha = run.opacity), CyberPrimitives.Colors.Yellow500.copy(alpha = run.opacity), CyberPrimitives.Colors.Cyan500.copy(alpha = run.opacity))
                        } else {
                            listOf(CyberPrimitives.Colors.Cyan500.copy(alpha = run.opacity), CyberPrimitives.Colors.Cyan500.copy(alpha = run.opacity))
                        }
                        Modifier.cyberGlowBorderFlow(colors = colors, durationMillis = run.duration)
                    }
                    testName.contains("doe_glow") -> {
                        val run = DoEMatrices.glowRuns.getOrNull(value.toInt()) ?: DoEMatrices.glowRuns[0]
                        Modifier.cyberGlow(color = CyberPrimitives.Colors.Cyan500.copy(alpha = run.opacity), radius = run.radius, intensity = run.decay.toInt())
                    }
                    testName.contains("doe_datastream") -> {
                        val run = DoEMatrices.datastreamRuns.getOrNull(value.toInt()) ?: DoEMatrices.datastreamRuns[0]
                        Modifier.cyberDatastream(color = CyberPrimitives.Colors.Cyan500, maxAlpha = run.maxAlpha) // Assuming animationSpec mapping if we add it
                    }
"""

content = content.replace('else -> Modifier', doe_mappings + '                    else -> Modifier')

with open('sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt', 'w') as f:
    f.write(content)
