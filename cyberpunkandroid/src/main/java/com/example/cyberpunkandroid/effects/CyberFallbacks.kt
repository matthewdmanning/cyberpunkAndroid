package com.example.cyberpunkandroid.effects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import kotlin.random.Random

/**
 * Procedural Compose graphics fallback implementations for visual shaders on Android API < 33 (pre-Tiramisu)
 * and tooling environments where AGSL RuntimeShader is unsupported.
 */
internal object CyberFallbacks {

    /**
     * Rasterizes a multi-pass chromatic aberration overload effect by shifting R and B color channels
     * horizontally with pseudo-random jitter clipping using Compose GraphicsLayers.
     *
     * @param intensity Overload displacement multiplier.
     * @param time Frame animation timestamp driving seed generation and jitter offsets.
     * @param rLayer Pre-obtained GraphicsLayer for the Red channel.
     * @param bLayer Pre-obtained GraphicsLayer for the Blue channel.
     */
    fun ContentDrawScope.drawOverloadFallback(
        intensity: Float,
        time: Float,
        rLayer: GraphicsLayer,
        bLayer: GraphicsLayer
    ) {
        val seed = (time * 10f).toLong()
        val random = Random(seed)
        val jitter = random.nextFloat() * 2f - 1f
        val jitterOffset = jitter * 0.05f * intensity * size.width

        val rOffset = size.width * 0.02f * intensity + jitterOffset
        val bOffset = -size.width * 0.02f * intensity + jitterOffset

        // Base content
        drawContent()

        rLayer.record {
            this@drawOverloadFallback.drawContent()
        }
        rLayer.colorFilter = ColorFilter.tint(Color.Red, BlendMode.SrcIn)
        rLayer.blendMode = BlendMode.Screen

        bLayer.record {
            this@drawOverloadFallback.drawContent()
        }
        bLayer.colorFilter = ColorFilter.tint(Color.Blue, BlendMode.SrcIn)
        bLayer.blendMode = BlendMode.Screen

        // Draw R channel shifted
        translate(left = rOffset) {
            drawLayer(rLayer)
        }

        // Draw B channel shifted
        translate(left = bOffset) {
            drawLayer(bLayer)
        }

        // Add horizontal slice clipping for jitter
        if (random.nextFloat() > 0.8f) {
            val sliceY = random.nextFloat() * size.height
            val sliceHeight = random.nextFloat() * size.height * 0.1f
            clipRect(top = sliceY, bottom = sliceY + sliceHeight) {
                translate(left = jitterOffset * 2f) {
                    this@drawOverloadFallback.drawContent()
                }
            }
        }
    }

    /**
     * Rasterizes horizontal CRT scanlines with animated vertical displacement over the composable content.
     *
     * @param spacingPx Pixel distance between adjacent scanline bars.
     * @param opacity Alpha transparency of the scanline strokes.
     * @param offset Pixel vertical phase translation.
     */
    fun ContentDrawScope.drawScanlinesFallback(
        spacingPx: Float,
        opacity: Float,
        offset: Float,
        color: Color = Color.Black
    ) {
        drawContent()
        val isClear = color == Color.Transparent
        // Transparent means "darken", matching the shader's up-to-70% darkening; otherwise tint toward color
        val drawColor = if (isClear) Color.Black.copy(alpha = opacity * 0.7f) else color.copy(alpha = opacity)
        val blendMode = BlendMode.SrcOver

        var y = offset
        while (y < size.height) {
            drawLine(
                color = drawColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = spacingPx / 2f,
                blendMode = blendMode
            )
            y += spacingPx
        }
    }

    /**
     * Rasterizes an animated procedural static noise grain texture over the drawing area for API < 33.
     *
     * @param opacity Alpha intensity of the noise grain points.
     * @param time Timestamp driving the pseudo-random grain distribution seed.
     * @param sampleCount Number of pseudo-random noise particle points generated across the surface.
     */
    fun ContentDrawScope.drawNoiseFallback(opacity: Float, time: Float, sampleCount: Int = 200) {
        drawContent()
        val seed = (time * 1000f).toLong()
        val random = Random(seed)
        val alpha = (opacity * 2.5f).coerceIn(0f, 1f)
        val pointsWhite = mutableListOf<Offset>()
        val pointsBlack = mutableListOf<Offset>()
        for (i in 0 until sampleCount) {
            val pt = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
            if (random.nextBoolean()) {
                pointsWhite.add(pt)
            } else {
                pointsBlack.add(pt)
            }
        }
        drawPoints(
            points = pointsWhite,
            pointMode = PointMode.Points,
            color = Color.White.copy(alpha = alpha),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        drawPoints(
            points = pointsBlack,
            pointMode = PointMode.Points,
            color = Color.Black.copy(alpha = alpha),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }

}
