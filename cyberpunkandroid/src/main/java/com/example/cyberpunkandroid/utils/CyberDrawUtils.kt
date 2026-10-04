package com.example.cyberpunkandroid.utils

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

// TODO: document this
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

    // TODO: document this
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

