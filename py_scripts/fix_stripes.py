import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'fun Modifier.cyberStripes(\n    color: Color = Color(0x26FFFFFF), // 15% white\n    stripeWidth: Dp = 5.dp,\n    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(\n        animation = tween(500, easing = LinearEasing),\n        repeatMode = RepeatMode.Restart\n    ),',
    'fun Modifier.cyberStripes(\n    color: Color = Color(0x26FFFFFF), // 15% white\n    stripeWidth: Dp = 5.dp,\n    speed: Float = 1.0f,\n    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(\n        animation = tween((500 / speed.coerceAtLeast(0.1f)).toInt(), easing = LinearEasing),\n        repeatMode = RepeatMode.Restart\n    ),'
)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'w') as f:
    f.write(content)
