package com.example.cyberpunkandroid.effects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Container-type effect wrapper. Applies the text glow effect to the container itself,
 * avoiding modifier stacking issues on the final leaf components.
 */
@Composable
fun CyberGlowContainer(
    modifier: Modifier = Modifier,
    color: Color = Color.Cyan,
    radius: Dp = 8.dp,
    intensity: Float = 1f,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.cyberTextGlow(color, radius, intensity),
        contentAlignment = contentAlignment,
        content = content
    )
}
