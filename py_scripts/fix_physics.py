import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'r') as f:
    content = f.read()

# Fix Bounce
old_bounce = """    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = keyframes {
            durationMillis = 1000
            1f at 0 using CyberConfig.Easings.AccelEasing
            0f at 500 using CyberConfig.Easings.DecelEasing
            1f at 1000
        },
        repeatMode = RepeatMode.Restart
    ),"""
new_bounce = """    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = tween(500, easing = { t -> 1f - (1f - t) * (1f - t) }), // Parabolic EaseOut
        repeatMode = RepeatMode.Reverse
    ),"""

content = content.replace(old_bounce, new_bounce)

# Fix Float
old_float = """    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis, easing = CyberConfig.Easings.FloatEasing),
        repeatMode = RepeatMode.Reverse
    ),"""
new_float = """    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis / 2, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)), // Smooth EaseInOut sine-like
        repeatMode = RepeatMode.Reverse
    ),"""

content = content.replace(old_float, new_float)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'w') as f:
    f.write(content)
