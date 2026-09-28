package com.example.cyberpunkandroid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberPrimitives

@Composable
fun CyberContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF0F172A),
    borderColor: Color = CyberPrimitives.Colors.Cyan500,
    borderWidth: Dp = 1.dp,
    shape: Shape = RectangleShape,
    effectModifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .border(borderWidth, borderColor, shape)
            .then(effectModifier)
            .background(backgroundColor, shape),
        content = content
    )
}
