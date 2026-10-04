package com.example.cyberpunkandroid.effects

import android.os.Build
import com.example.cyberpunkandroid.utils.cyberSweepGradient
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * Defines the placement of a stroke relative to its path boundary.
 */
enum class CyberBorderAlignment {
    /** Stroke is drawn entirely inside the shape bounds. */
    INSET,
    /** Stroke is drawn entirely outside the shape bounds. */
    OUTSET,
    /** Stroke is centered on the shape bounds, drawing half inside and half outside. */
    BOTH
}

/**
 * Text and icon contour glow following the exact object/vector path: a blurred (API 31+), [color]-tinted copy of
 * the content is drawn behind it, giving each glyph or stroke a soft halo.
 *
 * @param color Glow color.
 * @param radius Blur spread; 0.dp disables the glow.
 * @param intensity Glow opacity is 0.85 × intensity (capped at 1); whole numbers above 1 stack extra passes.
 * @param outsideGlowOnly Removes the glow wherever the content itself is drawn, so translucent content doesn't
 *   show the halo through it. Renders this element into an offscreen layer.
 */
fun Modifier.cyberTextGlow(
    color: Color = Color.Cyan,
    radius: Dp = 8.dp,
    intensity: Float = 1f,
    dropoffPower: Float = 3f,
    outsideGlowOnly: Boolean = false,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberTextGlow", appendedA11y, customA11y)
    // Offscreen so the DstOut mask below erases only this element's glow, not what is behind it
    .then(if (outsideGlowOnly) Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen } else Modifier)
    .drawWithCache {
    val graphicsLayer = obtainGraphicsLayer()
    val maskLayer = if (outsideGlowOnly) obtainGraphicsLayer().apply { blendMode = BlendMode.DstOut } else null
    val blurRadius = radius.toPx()

    onDrawWithContent {
        if (radius > 0.dp && intensity > 0f) {
            graphicsLayer.record {
                this@onDrawWithContent.drawContent()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && blurRadius > 0f) {
                val blurEffect = android.graphics.RenderEffect.createBlurEffect(
                    blurRadius,
                    blurRadius,
                    android.graphics.Shader.TileMode.DECAL
                )
                val shader = android.graphics.RuntimeShader(CyberShaders.GlowShader)
                shader.setFloatUniform("resolution", size.width, size.height)
                shader.setFloatUniform("intensity", intensity)
                shader.setFloatUniform("dropoffPower", dropoffPower)
                shader.setColorUniform("glowColor", color.toArgb())

                val shaderEffect = android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "contents")
                graphicsLayer.renderEffect = android.graphics.RenderEffect.createChainEffect(shaderEffect, blurEffect).asComposeRenderEffect()
                drawLayer(graphicsLayer)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f) {
                val matrix = floatArrayOf(
                    0f, 0f, 0f, 0f, color.red * 255f,
                    0f, 0f, 0f, 0f, color.green * 255f,
                    0f, 0f, 0f, 0f, color.blue * 255f,
                    0f, 0f, 0f, intensity * color.alpha, 0f
                )
                val colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
                val colorEffect = android.graphics.RenderEffect.createColorFilterEffect(colorFilter)
                val blurEffect = android.graphics.RenderEffect.createBlurEffect(
                    blurRadius,
                    blurRadius,
                    colorEffect,
                    android.graphics.Shader.TileMode.DECAL
                )
                graphicsLayer.renderEffect = blurEffect.asComposeRenderEffect()
                drawLayer(graphicsLayer)
            } else {
                graphicsLayer.colorFilter = ColorFilter.tint(color, BlendMode.SrcIn)
                graphicsLayer.alpha = (0.85f * intensity).coerceIn(0f, 1f)
                val drawCount = intensity.toInt().coerceAtLeast(1)
                for (i in 0 until drawCount) {
                    drawLayer(graphicsLayer)
                }
            }

            if (maskLayer != null) {
                // Punch the content's silhouette out of the glow before drawing the content itself
                maskLayer.record { this@onDrawWithContent.drawContent() }
                drawLayer(maskLayer)
            }
        }
        drawContent()
    }
}

/**
 * Static neon glow border.
 */
fun Modifier.cyberGlowBorder(
    color: Color = Color.Cyan,
    shape: Shape = CutCornerShape(12.dp),
    glowRadius: Dp = 8.dp,
    width: Dp = 2.dp,
    alignment: CyberBorderAlignment = CyberBorderAlignment.BOTH,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberGlowBorder", appendedA11y, customA11y)
    .cyberGlowStroke(shape, glowRadius, width, alignment) { SolidColor(color) }

/**
 * Static neon glow border with a rounded corner profile shape.
 */
fun Modifier.cyberGlowBorderRounded(
    color: Color = Color.Cyan,
    cornerRadius: Dp = 16.dp,
    glowRadius: Dp = 12.dp,
    width: Dp = 2.dp,
    alignment: CyberBorderAlignment = CyberBorderAlignment.BOTH,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = cyberGlowBorder(
    color = color,
    shape = RoundedCornerShape(cornerRadius),
    glowRadius = glowRadius,
    width = width,
    alignment = alignment,
    appendedA11y = appendedA11y,
    customA11y = customA11y
)

/**
 * Animated flowing neon glow border.
 */
fun Modifier.cyberGlowBorderFlow(
    colors: List<Color> = listOf(Color.Cyan, Color.Magenta),
    shape: Shape = CutCornerShape(12.dp),
    glowRadius: Dp = 8.dp,
    width: Dp = 2.dp,
    speed: Float = 1f,
    alignment: CyberBorderAlignment = CyberBorderAlignment.BOTH,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberGlowBorderFlow", appendedA11y, customA11y).composed {
    val infiniteTransition = rememberInfiniteTransition(label = "glowFlowInfinite")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((2000 / speed.coerceAtLeast(0.1f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowFlow"
    )

    val actualColors = if (colors.isEmpty()) listOf(Color.Cyan, Color.Magenta)
                       else if (colors.size == 1) listOf(colors.first(), colors.first())
                       else colors
    val gradientColors = actualColors + actualColors.first()

    cyberGlowStroke(shape, glowRadius, width, alignment) {
        cyberSweepGradient(
            center = Offset(size.width / 2f, size.height / 2f),
            colors = gradientColors,
            rotation = phase * 360f
        )
    }
}

/**
 * Shared neon border renderer: a soft 35% outer stroke, a blurred double-width stroke (blur on API 31+),
 * and a sharp stroke on top, all following [shape]'s outline and painted with [brush].
 * [brush] is evaluated every draw so animated brushes update without rebuilding the cache.
 */
private fun Modifier.cyberGlowStroke(
    shape: Shape,
    glowRadius: Dp,
    width: Dp,
    alignment: CyberBorderAlignment = CyberBorderAlignment.BOTH,
    brush: DrawScope.() -> Brush
): Modifier = drawWithCache {
    val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
    val glowLayer = obtainGraphicsLayer()
    val blurRadius = glowRadius.toPx()
    val strokeWidthPx = width.toPx()
    val glowWidthPx = strokeWidthPx + blurRadius
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f) {
        glowLayer.renderEffect = android.graphics.RenderEffect.createBlurEffect(
            blurRadius,
            blurRadius,
            android.graphics.Shader.TileMode.DECAL
        ).asComposeRenderEffect()
    }

    onDrawWithContent {
        drawContent()
        val paint = brush()

        if (alignment == CyberBorderAlignment.BOTH) {
            if (glowRadius > 0.dp) {
                drawPath(path = path, brush = paint, style = Stroke(width = glowWidthPx), alpha = 0.35f)
                glowLayer.record {
                    drawPath(path = path, brush = paint, style = Stroke(width = strokeWidthPx * 2f))
                }
                drawLayer(glowLayer)
            }
            drawPath(path = path, brush = paint, style = Stroke(width = strokeWidthPx))
        } else {
            val clipOp = if (alignment == CyberBorderAlignment.INSET) androidx.compose.ui.graphics.ClipOp.Intersect else androidx.compose.ui.graphics.ClipOp.Difference
            clipPath(path, clipOp = clipOp) {
                if (glowRadius > 0.dp) {
                    drawPath(path = path, brush = paint, style = Stroke(width = glowWidthPx * 2f), alpha = 0.35f)
                    glowLayer.record {
                        drawPath(path = path, brush = paint, style = Stroke(width = strokeWidthPx * 4f))
                    }
                    drawLayer(glowLayer)
                }
                drawPath(path = path, brush = paint, style = Stroke(width = strokeWidthPx * 2f))
            }
        }
    }
}

@Deprecated("Use cyberGlowBorder instead", ReplaceWith("cyberGlowBorder(color, shape, glowRadius, width, appendedA11y=appendedA11y, customA11y=customA11y)"))
fun Modifier.cyberNeonBorder(
    color: Color = Color.Cyan,
    shape: Shape = CutCornerShape(12.dp),
    glowRadius: Dp = 8.dp,
    width: Dp = 2.dp,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = cyberGlowBorder(color = color, shape = shape, glowRadius = glowRadius, width = width, appendedA11y = appendedA11y, customA11y = customA11y)

@Deprecated("Use cyberGlowBorderFlow instead", ReplaceWith("cyberGlowBorderFlow(colors, shape, glowRadius, width, speed, appendedA11y=appendedA11y, customA11y=customA11y)"))
fun Modifier.cyberNeonBorderFlow(
    enabled: Boolean = true,
    colors: List<Color> = listOf(Color.Cyan, Color.Magenta),
    shape: Shape = CutCornerShape(12.dp),
    glowRadius: Dp = 8.dp,
    width: Dp = 2.dp,
    speed: Float = 1f,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = if (enabled) cyberGlowBorderFlow(colors = colors, shape = shape, glowRadius = glowRadius, width = width, speed = speed, appendedA11y = appendedA11y, customA11y = customA11y) else this
