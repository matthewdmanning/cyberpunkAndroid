package com.example.cyberpunkandroid.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate

fun DrawScope.drawShapeOutline(
    outline: Outline,
    brush: Brush,
    style: DrawStyle
) {
    when (outline) {
        is Outline.Rectangle -> drawRect(brush = brush, topLeft = outline.rect.topLeft, size = outline.rect.size, style = style)
        is Outline.Rounded -> {
            val path = Path().apply { addRoundRect(outline.roundRect) }
            drawPath(path = path, brush = brush, style = style)
        }
        is Outline.Generic -> drawPath(path = outline.path, brush = brush, style = style)
    }
}

fun DrawScope.drawDatastreamGradient(
    extent: Float,
    forwardCenter: Float,
    mirror: Boolean,
    colors: List<Color>
) {
    // Generate a full wave [Transparent, Colors..., Transparent]
    val waveColors = buildList {
        add(Color.Transparent)
        addAll(colors)
        add(Color.Transparent)
    }
    
    fun drawStream(centerOffset: Float, streamColors: List<Color>) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = streamColors,
                startY = centerOffset - size.height,
                endY = centerOffset,
                tileMode = androidx.compose.ui.graphics.TileMode.Repeated
            ),
            blendMode = BlendMode.Screen
        )
    }

    drawStream(forwardCenter, waveColors)

    if (mirror) {
        val backwardCenter = size.height - (forwardCenter % size.height)
        drawStream(backwardCenter, waveColors.reversed())
    }
}

