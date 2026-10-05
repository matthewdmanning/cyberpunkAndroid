package com.example.cyberpunkandroid.utils

import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb

class CyberSweepGradientBrush(
    val colors: List<Color>,
    val centerOffset: Offset = Offset.Unspecified
) : ShaderBrush() {
    var rotation: Float = 0f
    private val matrix = Matrix()

    override fun createShader(size: Size): Shader {
        val cx = if (centerOffset.isUnspecified) size.width / 2f else centerOffset.x
        val cy = if (centerOffset.isUnspecified) size.height / 2f else centerOffset.y

        val sweepShader = SweepGradient(
            cx,
            cy,
            colors.map { it.toArgb() }.toIntArray(),
            null
        )
        matrix.setRotate(rotation, cx, cy)
        sweepShader.setLocalMatrix(matrix)
        return sweepShader
    }
}

/**
 * Creates a sweeping gradient brush that rotates around a given center.
 */
fun cyberSweepGradient(
    center: Offset,
    colors: List<Color>,
    rotation: Float
): Brush {
    return CyberSweepGradientBrush(colors, center).apply {
        this.rotation = rotation
    }
}
