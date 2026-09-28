import re

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberFallbacks.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_fallback = '''    fun ContentDrawScope.drawOverloadFallback(intensity: Float, time: Float) {
        val rPaint = Paint().apply {
            colorFilter = ColorFilter.tint(Color.Red, BlendMode.SrcIn)
            blendMode = BlendMode.Screen
        }
        val bPaint = Paint().apply {
            colorFilter = ColorFilter.tint(Color.Blue, BlendMode.SrcIn)
            blendMode = BlendMode.Screen
        }'''

new_fallback = '''    private val rPaint = Paint().apply {
        colorFilter = ColorFilter.tint(Color.Red, BlendMode.SrcIn)
        blendMode = BlendMode.Screen
    }
    private val bPaint = Paint().apply {
        colorFilter = ColorFilter.tint(Color.Blue, BlendMode.SrcIn)
        blendMode = BlendMode.Screen
    }

    fun ContentDrawScope.drawOverloadFallback(intensity: Float, time: Float) {'''

content = content.replace(old_fallback, new_fallback)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberFallbacks.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated CyberFallbacks.kt")
