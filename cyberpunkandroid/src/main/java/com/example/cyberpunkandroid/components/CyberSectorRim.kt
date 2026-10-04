package com.example.cyberpunkandroid.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberPrimitives

// TODO: document this
@Composable
fun CyberSectorRim(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    thickness: Dp = 2.dp,
    sectorAngles: List<Float> = listOf(90f, 90f, 90f, 90f),
    gapAngle: Float = 10f,
    size: Dp = CyberPrimitives.IconSizes.dp64
) {
    Spacer(
        modifier = modifier
            .size(size)
            .drawBehind {
                val totalGapDegrees = gapAngle * sectorAngles.size
                val rawSectorSum = sectorAngles.sum()
                val targetSectorSum = 360f - totalGapDegrees
                
                // If the sum is 0 (or empty), don't draw
                if (rawSectorSum <= 0f) return@drawBehind
                
                val scaleFactor = if (rawSectorSum != targetSectorSum) targetSectorSum / rawSectorSum else 1f
                
                val strokePx = thickness.toPx()
                // Inset the bounds so the stroke isn't clipped by the layout boundaries
                val drawSize = Size(this.size.width - strokePx, this.size.height - strokePx)
                val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                
                var currentAngle = -90f // Start at the top (12 o'clock)
                
                for (rawAngle in sectorAngles) {
                    val sweepAngle = rawAngle * scaleFactor
                    
                    drawArc(
                        color = color,
                        startAngle = currentAngle + (gapAngle / 2f),
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = drawSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Square)
                    )
                    
                    currentAngle += sweepAngle + gapAngle
                }
            }
    )
}
