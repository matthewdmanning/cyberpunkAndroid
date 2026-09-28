package com.example.cyberpunkandroid.icons

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberPrimitives

@Composable
fun CyberDialTicks(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    tickCount: Int = 60,
    majorTickInterval: Int = 5,
    tickLength: Dp = 4.dp,
    majorTickLength: Dp = 8.dp,
    strokeWidth: Dp = 1.dp,
    size: Dp = CyberPrimitives.IconSizes.dp64
) {
    Spacer(
        modifier = modifier
            .size(size)
            .drawBehind {
                val radius = this.size.width / 2f
                val center = Offset(radius, radius)
                val angleStep = 360f / tickCount
                
                for (i in 0 until tickCount) {
                    val isMajor = i % majorTickInterval == 0
                    val currentTickLength = if (isMajor) majorTickLength.toPx() else tickLength.toPx()
                    
                    rotate(angleStep * i, center) {
                        drawLine(
                            color = color,
                            start = Offset(center.x, center.y - radius),
                            end = Offset(center.x, center.y - radius + currentTickLength),
                            strokeWidth = strokeWidth.toPx(),
                            cap = StrokeCap.Square
                        )
                    }
                }
            }
    )
}
