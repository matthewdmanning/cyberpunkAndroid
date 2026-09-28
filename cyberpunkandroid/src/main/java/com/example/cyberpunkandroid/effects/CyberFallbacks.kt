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
object CyberFallbacks {

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
        val drawColor = if (isClear) Color.Black.copy(alpha = opacity) else color.copy(alpha = opacity)
        val blendMode = if (isClear) BlendMode.DstOut else BlendMode.SrcOver
        
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

    /**
     * Rasterizes popcorn spark particles with parabolic downward arc trajectories,
     * initial upward burst launch, plasma colorscale interpolation, and 50% radius decay.
     *
     * @param primaryColor Semantic primary tint color (e.g. Info/Primary).
     * @param secondaryColor Semantic secondary tint color (e.g. Danger/Secondary).
     * @param warningColor Semantic warning tint color (e.g. Caution/Warning).
     * @param sparkCount Number of spark particles rendered.
     * @param intensity Brightness and radius multiplier.
     * @param time Frame animation timestamp.
     */
    fun ContentDrawScope.drawSparksFallback(
        primaryColor: Color,
        secondaryColor: Color = Color(0xFFFF2A6D),
        warningColor: Color = Color(0xFFFCEE0A),
        sparkCount: Int = 32,
        intensity: Float = 1.0f,
        time: Float
    ) {
        drawContent()
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxExtent = minOf(size.width, size.height) * 0.45f

        for (i in 0 until sparkCount) {
            val seed = (i * 17 + (time * 1.2f).toInt()).toLong()
            val random = Random(seed)

            val burstCycle = ((time * (0.8f + random.nextFloat() * 1.2f) + random.nextFloat()) % 1.0f)
            val t = burstCycle

            // LinearInSlowOut easing curve
            val easedT = t * t * (3f - 2f * t)

            // Always start off going UP: angle between -135 deg and -45 deg (-2.356 rad to -0.785 rad)
            val angle = -2.356f + random.nextFloat() * 1.5708f
            val speed = (0.4f + random.nextFloat() * 0.6f) * maxExtent

            val vx = kotlin.math.cos(angle) * speed
            val vy = kotlin.math.sin(angle) * speed // vy < 0 (upward)
            val gravity = 1.2f * maxExtent

            val sparkPos = Offset(
                center.x + vx * easedT,
                center.y + vy * easedT + 0.5f * gravity * easedT * easedT
            )

            // Radius decreases by half from beginning to end: r(t) = r_base * (1.0 - 0.5 * t)
            val baseRadius = (3f + random.nextFloat() * 3f) * intensity
            val currentRadius = baseRadius * (1.0f - 0.5f * t)

            // Plasma colorscale interpolation across semantic keywords
            val plasmaColor = when {
                t < 0.25f -> {
                    val localT = t / 0.25f
                    Color(
                        red = Color.White.red + (warningColor.red - Color.White.red) * localT,
                        green = Color.White.green + (warningColor.green - Color.White.green) * localT,
                        blue = Color.White.blue + (warningColor.blue - Color.White.blue) * localT,
                        alpha = 1.0f
                    )
                }
                t < 0.60f -> {
                    val localT = (t - 0.25f) / 0.35f
                    Color(
                        red = primaryColor.red + (secondaryColor.red - primaryColor.red) * localT,
                        green = primaryColor.green + (secondaryColor.green - primaryColor.green) * localT,
                        blue = primaryColor.blue + (secondaryColor.blue - primaryColor.blue) * localT,
                        alpha = 1.0f - (t * 0.3f)
                    )
                }
                else -> {
                    val localT = (t - 0.60f) / 0.40f
                    Color(
                        red = secondaryColor.red + (warningColor.red - secondaryColor.red) * localT,
                        green = secondaryColor.green + (warningColor.green - secondaryColor.green) * localT,
                        blue = secondaryColor.blue + (warningColor.blue - secondaryColor.blue) * localT,
                        alpha = (1.0f - t).coerceAtLeast(0f)
                    )
                }
            }

            drawCircle(
                color = plasmaColor,
                radius = currentRadius,
                center = sparkPos
            )
        }
    }
}
