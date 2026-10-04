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
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Text and icon contour glow following the exact object/vector path.
 */
fun Modifier.cyberTextGlow(
    color: Color = Color.Cyan,
    radius: Dp = 8.dp,
    intensity: Float = 1f,
    outsideGlowOnly: Boolean = false,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberTextGlow", appendedA11y, customA11y).drawWithCache {
    val graphicsLayer = obtainGraphicsLayer()
    val blurRadius = radius.toPx()

    onDrawWithContent {
        if (radius > 0.dp && intensity > 0f) {
            drawContourGlow(graphicsLayer, color, blurRadius, intensity)
        }
        drawContent()
    }
}

/**
 * Draws a blurred, recolored copy of the content: the contour glow behind text or an icon. The content itself is
 * not drawn here; call `drawContent()` afterwards. Shared by [cyberTextGlow] and `cyberRadialIllumination`.
 *
 * On Android 12+ (API 31+) the copy is blurred with a `RenderEffect`; below that it is only tinted, because no blur is available.
 *
 * @param graphicsLayer Layer that records the content; obtain it once with `obtainGraphicsLayer()` inside `drawWithCache`.
 * @param color Glow color.
 * @param blurRadiusPx Blur radius in pixels. Zero or less skips the blur (tint only).
 * @param intensity Glow strength: 1 is the standard strength, values above 1 draw the glow that many extra times.
 */
internal fun ContentDrawScope.drawContourGlow(
    graphicsLayer: GraphicsLayer,
    color: Color,
    blurRadiusPx: Float,
    intensity: Float,
) {
    graphicsLayer.record {
        this@drawContourGlow.drawContent()
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadiusPx > 0f) {
        val colorFilter = android.graphics.PorterDuffColorFilter(
            color.toArgb(),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        val colorEffect = android.graphics.RenderEffect.createColorFilterEffect(colorFilter)
        val blurEffect = android.graphics.RenderEffect.createBlurEffect(
            blurRadiusPx,
            blurRadiusPx,
            colorEffect,
            android.graphics.Shader.TileMode.DECAL
        )
        graphicsLayer.renderEffect = blurEffect.asComposeRenderEffect()
    } else {
        graphicsLayer.colorFilter = ColorFilter.tint(color, BlendMode.SrcIn)
    }
    graphicsLayer.alpha = (0.85f * intensity).coerceIn(0f, 1f) // 0.85: the glow stays just under the crisp content

    val drawCount = intensity.toInt().coerceAtLeast(1)
    for (i in 0 until drawCount) {
        drawLayer(graphicsLayer)
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
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberGlowBorder", appendedA11y, customA11y).drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = Path().apply {
        when (outline) {
            is Outline.Rectangle -> addRect(outline.rect)
            is Outline.Rounded -> addRoundRect(outline.roundRect)
            is Outline.Generic -> addPath(outline.path)
        }
    }
    
    val graphicsLayer = obtainGraphicsLayer()
    val blurRadius = glowRadius.toPx()
    val strokeWidthPx = width.toPx()
    val glowWidthPx = strokeWidthPx + blurRadius

    onDrawWithContent {
        drawContent()
        
        if (glowRadius > 0.dp) {
            // Draw outer atmospheric glow pass
            drawPath(
                path = path,
                color = color.copy(alpha = 0.35f),
                style = Stroke(width = glowWidthPx)
            )
            graphicsLayer.record {
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = strokeWidthPx * 2f)
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f) {
                val blurEffect = android.graphics.RenderEffect.createBlurEffect(
                    blurRadius,
                    blurRadius,
                    android.graphics.Shader.TileMode.DECAL
                )
                graphicsLayer.renderEffect = blurEffect.asComposeRenderEffect()
            }
            drawLayer(graphicsLayer)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidthPx)
        )
    }
}

/**
 * Static neon glow border with a rounded corner profile shape.
 */
fun Modifier.cyberGlowBorderRounded(
    color: Color = Color.Cyan,
    cornerRadius: Dp = 16.dp,
    glowRadius: Dp = 12.dp,
    width: Dp = 2.dp,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = cyberGlowBorder(
    color = color,
    shape = RoundedCornerShape(cornerRadius),
    glowRadius = glowRadius,
    width = width,
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

    drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = Path().apply {
            when (outline) {
                is Outline.Rectangle -> addRect(outline.rect)
                is Outline.Rounded -> addRoundRect(outline.roundRect)
                is Outline.Generic -> addPath(outline.path)
            }
        }
        
        val graphicsLayer = obtainGraphicsLayer()
        val blurRadius = glowRadius.toPx()
        val strokeWidthPx = width.toPx()
        val glowWidthPx = strokeWidthPx + blurRadius
        val center = Offset(size.width / 2f, size.height / 2f)
        val gradientColors = actualColors + actualColors.first()

        onDrawWithContent {
            drawContent()
            
            val sweepBrush = cyberSweepGradient(
                center = center,
                colors = gradientColors,
                rotation = phase * 360f
            )

            if (glowRadius > 0.dp) {
                drawPath(
                    path = path,
                    brush = sweepBrush,
                    style = Stroke(width = glowWidthPx),
                    alpha = 0.35f
                )
                graphicsLayer.record {
                    drawPath(
                        path = path,
                        brush = sweepBrush,
                        style = Stroke(width = strokeWidthPx * 2f)
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f) {
                    val blurEffect = android.graphics.RenderEffect.createBlurEffect(
                        blurRadius,
                        blurRadius,
                        android.graphics.Shader.TileMode.DECAL
                    )
                    graphicsLayer.renderEffect = blurEffect.asComposeRenderEffect()
                }
                drawLayer(graphicsLayer)
            }
            
            drawPath(
                path = path,
                brush = sweepBrush,
                style = Stroke(width = strokeWidthPx)
            )
        }
    }
}

@Deprecated("Use cyberGlowBorder instead", ReplaceWith("cyberGlowBorder(color, shape, glowRadius, width, appendedA11y, customA11y)"))
fun Modifier.cyberNeonBorder(
    color: Color = Color.Cyan,
    shape: Shape = CutCornerShape(12.dp),
    glowRadius: Dp = 8.dp,
    width: Dp = 2.dp,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = cyberGlowBorder(color, shape, glowRadius, width, appendedA11y, customA11y)

@Deprecated("Use cyberGlowBorderFlow instead", ReplaceWith("cyberGlowBorderFlow(colors, shape, glowRadius, width, speed, appendedA11y, customA11y)"))
fun Modifier.cyberNeonBorderFlow(
    enabled: Boolean = true,
    colors: List<Color> = listOf(Color.Cyan, Color.Magenta),
    shape: Shape = CutCornerShape(12.dp),
    glowRadius: Dp = 8.dp,
    width: Dp = 2.dp,
    speed: Float = 1f,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = if (enabled) cyberGlowBorderFlow(colors, shape, glowRadius, width, speed, appendedA11y, customA11y) else this
