package com.example.cyberpunkandroid.effects

import com.example.cyberpunkandroid.utils.cyberSweepGradient
import com.example.cyberpunkandroid.utils.drawDatastreamGradient

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.foundation.shape.CutCornerShape
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.theme.LocalCyberColors
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.interaction.InteractionSource
import kotlin.math.roundToInt
import com.example.cyberpunkandroid.utils.CyberDataMapper
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.material3.LocalContentColor
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.cyberpunkandroid.theme.CyberRadius

// -------------------------------------------------------------------------
// 1) Overload Modifier
// -------------------------------------------------------------------------

/**
 * Applies a real-time chromatic aberration overload distortion effect.
 *
 * ### Rendering Architecture:
 * - **Android 13+ (API 33+)**: Uses hardware-accelerated AGSL [com.example.cyberpunkandroid.effects.CyberShaders.OverloadShader]
 *   via [android.graphics.RenderEffect] to split RGB color channels and inject horizontal slice displacements.
 * - **API < 33 & Previews**: Gracefully falls back to [com.example.cyberpunkandroid.effects.CyberFallbacks.drawOverloadFallback],
 *   rasterizing shifted red/blue channel layers and jitter bands on Compose graphics.
 *
 * @param enabled Controls whether the overload shader is active. When false, acts as a no-op identity modifier.
 * @param intensity Overload displacement multiplier. Defaults to [com.example.cyberpunkandroid.config.CyberConfig.Shaders.OverloadCoefficient].
 */
fun Modifier.cyberOverload(
    enabled: Boolean = true,
    intensity: Float = CyberConfig.Shaders.OverloadCoefficient,
    timeScale: Float = 1f,
    bounceAmount: Dp = 0.dp,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = tween(300),
    exitAnimationSpec: AnimationSpec<Float> = tween(300),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberOverload", appendedA11y, customA11y).composed {
    if (!enabled) return@composed this

    val isActive = trigger.isActive(interactionSource)
    val activeIntensity by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActive) intensity else 0f,
        animationSpec = if (isActive) animationSpec else exitAnimationSpec,
        label = "overloadIntensity"
    )

    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis { frameTime ->
                // 100000L prevents Float precision loss over long uptimes while keeping loop smooth
                value = (frameTime % 100000L) / 1000f
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createOverloadShader() }
        graphicsLayer {
            clip = true
            if (activeIntensity == 0f && bounceAmount.toPx() == 0f) return@graphicsLayer
            if (activeIntensity > 0f && size.width > 0f && size.height > 0f) {
                renderEffect = CyberShaders.overloadEffect(
                    shader = shader,
                    width = size.width,
                    height = size.height,
                    time = time * timeScale,
                    intensity = activeIntensity
                )
            }
            if (bounceAmount.toPx() > 0f) {
                translationY = -bounceAmount.toPx() * kotlin.math.abs(kotlin.math.sin(time * 10f)).toFloat()
            }
        }
    } else {
        // Fallback for API < 33
        drawWithCache {
            val rLayer = obtainGraphicsLayer()
            val bLayer = obtainGraphicsLayer()
            onDrawWithContent {
                if (activeIntensity > 0f) {
                    with(CyberFallbacks) {
                        drawOverloadFallback(activeIntensity, time * (timeScale / 0.15f), rLayer, bLayer)
                    }
                } else {
                    drawContent()
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 2) Scanlines Modifier
// -------------------------------------------------------------------------

/**
 * Applies animated CRT cathode-ray scanlines and subtle barrel curvature over the composable.
 *
 * ### Rendering Architecture:
 * - **Android 13+ (API 33+)**: Uses hardware AGSL [com.example.cyberpunkandroid.effects.CyberShaders.CrtShader]
 *   simulating physical CRT phosphor scanlines and geometric curvature distortion.
 * - **API < 33 & Previews**: Falls back to [com.example.cyberpunkandroid.effects.CyberFallbacks.drawScanlinesFallback],
 *   rendering animated semi-transparent horizontal stroke lines on Compose graphics.
 *
 * @param spacing Vertical distance between adjacent scanline bars. Defaults to [com.example.cyberpunkandroid.config.CyberPrimitives.Spacing.dp4].
 * @param opacity Alpha transparency of the scanline pattern. Defaults to [com.example.cyberpunkandroid.config.CyberConfig.Shaders.ScanlineOpacity].
 * @param speed Frequency multiplier for vertical scanline translation.
 */
fun Modifier.cyberScanlines(
    spacing: Dp = CyberPrimitives.Spacing.dp4,
    opacity: Float = CyberConfig.Shaders.ScanlineOpacity,
    speed: Float = 1.0f,
    color: Color = Color.Transparent,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = tween(300),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberScanlines", appendedA11y, customA11y).composed {
    val scanlineColor = color

    val isActive = trigger.isActive(interactionSource)
    val activeOpacity by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActive) opacity else 0f,
        animationSpec = animationSpec,
        label = "scanlinesOpacity"
    )

    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis { frameTime ->
                // 100000L prevents Float precision loss over long uptimes while keeping loop smooth
                value = (frameTime % 100000L) / 1000f
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createScanlinesShader() }
        graphicsLayer {
            clip = true
            if (activeOpacity == 0f) return@graphicsLayer
            if (size.width > 0f && size.height > 0f) {
                renderEffect = CyberShaders.scanlinesEffect(
                    shader = shader,
                    width = size.width,
                    height = size.height,
                    time = time * speed,
                    opacity = activeOpacity,
                    spacing = spacing.toPx().coerceAtLeast(1f),
                    colorArgb = scanlineColor.toArgb()
                )
            }
        }
    } else {
        // Fallback for API < 33
        drawWithContent {
            if (activeOpacity > 0f) {
                val spacingPx = spacing.toPx()
                val offset = (time * speed * 20f) % spacingPx
                with(CyberFallbacks) {
                    drawScanlinesFallback(spacingPx, activeOpacity, offset, scanlineColor)
                }
            } else {
                drawContent()
            }
        }
    }
}

/**
 * Draws a static high-intensity neon glow border around the specified [shape].
 *
 * Composes a dual-layer stroke consisting of a sharp inner perimeter border accompanied by an expanded,
 * semi-transparent atmospheric glow perimeter.
 *
 * @param color Solid emissive neon tint color.
 * @param width Stroke thickness of the sharp inner perimeter border. Defaults to [com.example.cyberpunkandroid.config.CyberPrimitives.BorderWidths.dp2].
 * @param shape Geometric shape outline of the bordered surface.
 * @param glowRadius Radial spread and thickness of the diffused outer glow. Defaults to [com.example.cyberpunkandroid.config.CyberPrimitives.Spacing.dp8].
 */
fun Modifier.cyberDatastream(
    color: Color,
    speed: Float = 1f,
    maxAlpha: Float = 0.5f,
    mirror: Boolean = false,
    alphaTransform: (Float) -> Float = { factor -> factor * maxAlpha },
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = tween(300),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberDatastream", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    val activeAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActive) maxAlpha else 0f,
        animationSpec = animationSpec,
        label = "datastreamAlpha"
    )

    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis { frameTime ->
                // 100000L prevents Float precision loss over long uptimes while keeping loop smooth
                value = (frameTime % 100000L) / 1000f
            }
        }
    }

    drawWithContent {
        drawContent()
        if (activeAlpha > 0f && size.height > 0f) {
            val baseExtent = size.height * 0.5f
            val extent = if (mirror) baseExtent * 0.5f else baseExtent
            val resolution = 8

            val alphaFactors = List(resolution) { index ->
                index.toFloat() / (resolution - 1)
            }
            val colors = alphaFactors.map { factor ->
                color.copy(alpha = alphaTransform(factor) * activeAlpha)
            }

            val forwardCenter = (time * speed * 100f) % size.height
            
            drawDatastreamGradient(
                extent = extent,
                forwardCenter = forwardCenter,
                mirror = mirror,
                colors = colors
            )
        }
    }
}

// -------------------------------------------------------------------------
// 7) Noise Texture Overlay Modifier
// -------------------------------------------------------------------------

/**
 * Overlays an animated procedural static noise texture grain across the composable.
 *
 * Imitates the Cybercore CSS `.cyber-noise` effect.
 *
 * ### Rendering Architecture:
 * - **Android 13+ (API 33+)**: Executes AGSL [com.example.cyberpunkandroid.effects.CyberShaders.NoiseShader] via [android.graphics.RenderEffect],
 *   generating hardware-accelerated procedural pseudo-random noise grain modulated over time.
 * - **API < 33 & Previews**: Gracefully falls back to [com.example.cyberpunkandroid.effects.CyberFallbacks.drawNoiseFallback],
 *   rasterizing animated procedural point grain across the surface bounds via Compose graphics.
 *
 * @param enabled Controls whether the noise overlay is active. When `false`, acts as a no-op identity modifier.
 * @param opacity Alpha intensity of the noise grain. Defaults to [CyberConfig.Shaders.NoiseOpacity].
 * @param animated When `true`, animates noise grain dynamically on each frame. When `false`, renders static grain.
 */
fun Modifier.cyberNoise(
    enabled: Boolean = true,
    opacity: Float = CyberConfig.Shaders.NoiseOpacity,
    speed: Float = 1f,
    animated: Boolean = true,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = tween(300),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberNoise", appendedA11y, customA11y).composed {
    if (!enabled) return@composed this

    val isActive = trigger.isActive(interactionSource)
    val activeOpacity by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActive) opacity else 0f,
        animationSpec = animationSpec,
        label = "noiseOpacity"
    )

    val time by produceState(0f, animated, speed) {
        if (animated) {
            while (true) {
                withInfiniteAnimationFrameMillis { frameTime ->
                    value = ((frameTime % 100000L) / 1000f) * speed
                }
            }
        } else {
            value = 0f
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createNoiseShader() }
        graphicsLayer {
            clip = true
            if (activeOpacity == 0f) return@graphicsLayer
            if (size.width > 0f && size.height > 0f) {
                renderEffect = CyberShaders.noiseEffect(
                    shader = shader,
                    width = size.width,
                    height = size.height,
                    time = time,
                    intensity = activeOpacity
                )
            }
        }
    } else {
        drawWithCache {
            onDrawWithContent {
                if (activeOpacity > 0f) {
                    with(CyberFallbacks) {
                        drawNoiseFallback(activeOpacity, time)
                    }
                } else {
                    drawContent()
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 8) Icon Animations
// -------------------------------------------------------------------------

/**
 * Rotates the composable continuously to indicate loading or active processing states.
 */
fun Modifier.cyberIconSpin(
    bounceAmount: Dp = 0.dp,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = tween(1200, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    resetAnimationSpec: AnimationSpec<Float> = tween(300),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberIconSpin", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(isActive, animationSpec) {
        if (isActive) {
            progress.animateTo(
                targetValue = progress.value + 360f,
                animationSpec = animationSpec
            )
        } else {
            progress.animateTo(0f, resetAnimationSpec)
        }
    }
    
    graphicsLayer {
        rotationZ = progress.value
        if (bounceAmount.toPx() > 0f) {
            val radians = progress.value * Math.PI / 180.0
            translationY = -bounceAmount.toPx() * kotlin.math.abs(kotlin.math.sin(radians)).toFloat()
        }
    }
}

/**
 * Scale up and fade out radar ping effect with a locked dense core and diffuse outer ring.
 */
fun Modifier.cyberPing(
    color: Color = Color.Unspecified,
    durationMillis: Int = 1200,
    startScale: Float = 1.0f,
    maxDiffuseScale: Float = CyberConfig.Effects.PingScale,
    alphaDecayExponent: Float = 1.0f,
    shape: Shape = androidx.compose.foundation.shape.CircleShape,
    borderWidth: Dp = 2.dp,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberPing", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val specToUse = if (isActive) animationSpec else infiniteRepeatable(tween(100), RepeatMode.Restart)
    
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = specToUse as InfiniteRepeatableSpec<Float>
    )

    if (!isActive) return@composed this

    val waveColor = if (color == Color.Unspecified) CyberPrimitives.Colors.Cyan500 else color

    drawWithContent {
        val maxDelta = maxDiffuseScale - startScale
        val diffuseScale = startScale + progress * maxDelta
        val diffuseAlpha = (1f - progress).toDouble().coerceAtLeast(0.0).let { Math.pow(it, alphaDecayExponent.toDouble()) }.toFloat()

        drawContent()

        val strokeWidthPx = borderWidth.toPx()
        scale(diffuseScale, diffuseScale) {
            val outline = shape.createOutline(size, layoutDirection, this)
            val path = androidx.compose.ui.graphics.Path().apply {
                when (outline) {
                    is Outline.Rectangle -> addRect(outline.rect)
                    is Outline.Rounded -> addRoundRect(outline.roundRect)
                    is Outline.Generic -> addPath(outline.path)
                }
            }
            drawPath(
                path = path,
                color = waveColor.copy(alpha = diffuseAlpha),
                style = Stroke(width = strokeWidthPx / diffuseScale) // Counteract the scale so stroke width stays constant
            )
        }
    }
}

/**
 * Pulses the opacity of the composable to draw attention, staying the same size.
 */
fun Modifier.cyberIconPulse(
    durationMillis: Int = 600,
    minOpacity: Float = CyberConfig.Effects.PulseMinOpacity,
    maxOpacity: Float = 1.0f,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    ),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberIconPulse", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val specToUse = if (isActive) animationSpec else infiniteRepeatable(tween(100), RepeatMode.Reverse)
    
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = specToUse as InfiniteRepeatableSpec<Float>
    )

    graphicsLayer {
        this.alpha = if (isActive) minOpacity + progress * (maxOpacity - minOpacity) else 1f
    }
}


/**
 * Smooth vertical hover translation.
 */
fun Modifier.cyberFloat(
    height: Dp = CyberPrimitives.Spacing.dp12,
    durationMillis: Int = 3000,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis / 2, easing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)), // Smooth EaseInOut sine-like
        repeatMode = RepeatMode.Reverse
    ),
    exitAnimationSpec: AnimationSpec<Float> = spring(),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberFloat", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            progress.animateTo(1f, animationSpec)
        } else {
            progress.animateTo(0f, exitAnimationSpec)
        }
    }
    
    graphicsLayer {
        translationY = -height.toPx() * progress.value
    }
}

/**
 * Flicker-in opacity sequence for bootups.
 */
fun Modifier.cyberBoot(
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = keyframes {
        durationMillis = 800
        0f at 0
        0.6f at 80
        0.2f at 160
        0.8f at 240
        0.4f at 320
        1.0f at 400
        0.7f at 480
        1.0f at 560
        0.9f at 640
        1.0f at 800
    },
    exitAnimationSpec: AnimationSpec<Float> = tween(300),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberBoot", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            progress.animateTo(1f, animationSpec)
        } else {
            progress.animateTo(0f, exitAnimationSpec)
        }
    }
    
    graphicsLayer {
        this.alpha = if (isActive) progress.value else 1f
    }
}



/**
 * Cubic bezier vertical bouncing.
 */
fun Modifier.cyberBounce(
    height: Dp = CyberPrimitives.Spacing.dp16,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = tween(500, easing = { t -> 1f - (1f - t) * (1f - t) }), // Parabolic EaseOut
        repeatMode = RepeatMode.Reverse
    ),
    exitAnimationSpec: AnimationSpec<Float> = spring(),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberBounce", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            progress.animateTo(1f, animationSpec)
        } else {
            progress.animateTo(0f, exitAnimationSpec)
        }
    }
    
    graphicsLayer {
        translationY = -height.toPx() * progress.value
    }
}

/**
 * Pulsing brightness and drop shadow.
 */
fun Modifier.cyberCrt(
    enabled: Boolean = true,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberCrt", appendedA11y, customA11y).composed {
    if (!enabled) return@composed this
    val isActive = trigger.isActive(interactionSource)
    
    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis { millis ->
                value = millis / 1000f
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createCrtShader() }
        graphicsLayer {
            if (!isActive) return@graphicsLayer
            if (size.width > 0f && size.height > 0f) {
                renderEffect = CyberShaders.crtEffect(
                    shader = shader,
                    width = size.width,
                    height = size.height,
                    time = time
                )
                clip = true
            }
        }
    } else {
        // Fallback for older APIs: Draw a vignette overlay
        drawWithCache {
            val radialGradient = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                radius = size.width.coerceAtLeast(size.height) * 0.75f,
                center = Offset(size.width / 2f, size.height / 2f)
            )
            onDrawWithContent {
                drawContent()
                if (isActive) {
                    drawRect(brush = radialGradient)
                }
            }
        }
    }
}


/**
 * Applies a static rectangular/shape-based outer neon glow to a container.
 * For contour-following glows on text or icons, use [cyberTextGlow].
 */
fun Modifier.cyberBorder(
    width: Dp = 1.dp,
    color: Color = Color.Cyan,
    shape: Shape = CutCornerShape(12.dp),
    pathEffect: PathEffect? = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberBorder", appendedA11y, customA11y).drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = androidx.compose.ui.graphics.Path()
    when (outline) {
        is androidx.compose.ui.graphics.Outline.Rectangle -> path.addRect(outline.rect)
        is androidx.compose.ui.graphics.Outline.Rounded -> path.addRoundRect(outline.roundRect)
        is androidx.compose.ui.graphics.Outline.Generic -> path.addPath(outline.path)
    }

    onDrawWithContent {
        drawContent()
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = width.toPx(), pathEffect = pathEffect)
        )
    }
}

/**
 * Animated diagonal hazard stripes background.
 */
fun Modifier.cyberStripes(
    color: Color = Color(0x26FFFFFF), // 15% white
    stripeWidth: Dp = 5.dp,
    speed: Float = 1.0f,
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween((500 / speed.coerceAtLeast(0.1f)).toInt(), easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberStripes", appendedA11y, customA11y).composed {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "stripePhase"
    )

    drawWithCache {
        val widthPx = stripeWidth.toPx()
        // We draw overlapping lines diagonally
        onDrawWithContent {
            drawContent()
            clipRect {
                val diagonalLength = size.width + size.height
                val numStripes = (diagonalLength / (widthPx * 2)).toInt() + 2
                val shift = phase * widthPx * 2
                
                for (i in -1..numStripes) {
                    val offset = i * widthPx * 2 + shift
                    val start = Offset(offset - size.height, size.height)
                    val end = Offset(offset, 0f)
                    drawLine(
                        color = color,
                        start = start,
                        end = end,
                        strokeWidth = widthPx
                    )
                }
            }
        }
    }
}

/**
 * 4-Phase Holographic Shifting Background.
 */
fun Modifier.cyberHoloBackground(
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(8000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberHolo", appendedA11y, customA11y).composed {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "holoPhase"
    )
    
    val bg = LocalCyberColors.current.secondary
    val c1 = CyberPrimitives.Colors.Cyan500.copy(alpha = 0.3f)
    val c2 = CyberPrimitives.Colors.Magenta500.copy(alpha = 0.3f)
    val c3 = CyberPrimitives.Colors.Green500.copy(alpha = 0.3f)
    
    drawWithCache {
        val sweep = Brush.sweepGradient(
            0.0f to bg,
            0.25f to c1,
            0.5f to c2,
            0.75f to c3,
            1.0f to bg,
            center = Offset(size.width / 2, size.height / 2)
        )
        onDrawWithContent {
            // We use graphicsLayer rotation for phase shift instead of recalculating brush natively
            // Rotate the draw context
            rotate(phase * 360f) {
                drawRect(
                    brush = sweep,
                    size = size.copy(width = size.width * 2, height = size.height * 2),
                    topLeft = Offset(-size.width/2, -size.height/2)
                )
            }
            drawContent()
        }
    }
}

/**
 * Multi-layer atmospheric neon glow (omnidirectional bloom).
 */
fun Modifier.cyberBackdropBlur(
    radius: Dp = 12.dp,
    tint: Color = Color(0x1AFFFFFF), // 10% white
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberBackdropBlur", appendedA11y, customA11y).composed {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val blurPx = remember(radius, density) { with(density) { radius.toPx() } }

    graphicsLayer {
        clip = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurPx > 0f) {
            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                blurPx,
                blurPx,
                android.graphics.Shader.TileMode.CLAMP
            ).asComposeRenderEffect()
        }
    }.drawBehind {
        if (tint != Color.Transparent) {
            drawRect(color = tint)
        }
    }
}

/**
 * Overlays high-velocity popcorn electrical spark particles with parabolic downward gravity arcs,
 * wide upward burst launch, plasma colorscale interpolation, and a small ember core that shrinks to nothing over its lifetime.
 *
 * @param color Primary spark color.
 * @param secondaryColor Secondary plasma color.
 * @param warningColor Warning/Caution color for initial birth flash.
 * @param sparkCount Number of spark particles rendered.
 * @param intensity Brightness and radius multiplier.
 * @param speed Frequency multiplier for particle movement.
 */
fun Modifier.cyberSpark(
    color: Color = Color.Unspecified,
    secondaryColor: Color = Color.Unspecified,
    warningColor: Color = Color.Unspecified,
    sparkCount: Int = 32,
    intensity: Float = 1.0f,
    speed: Float = 1.0f,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = tween(300),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberSpark", appendedA11y, customA11y).composed {
    val primary = if (color == Color.Unspecified) CyberTheme.colors.primary else color
    val secondary = if (secondaryColor == Color.Unspecified) CyberTheme.colors.secondary else secondaryColor
    val warning = if (warningColor == Color.Unspecified) CyberTheme.semantics.colors.warning else warningColor

    val isActive = trigger.isActive(interactionSource)
    val activeIntensity by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActive) intensity else 0f,
        animationSpec = animationSpec,
        label = "sparkIntensity"
    )

    val time by produceState(0f) {
        while (true) {
            withInfiniteAnimationFrameMillis { frameTime ->
                value = ((frameTime % 100000L) / 1000f) * speed
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createSparkShader() }
        graphicsLayer {
            clip = true
            if (activeIntensity > 0f && size.width > 0f && size.height > 0f) {
                renderEffect = CyberShaders.sparkEffect(
                    shader = shader,
                    width = size.width,
                    height = size.height,
                    time = time,
                    intensity = activeIntensity,
                    speed = speed,
                    primaryColorArgb = primary.toArgb(),
                    secondaryColorArgb = secondary.toArgb(),
                    warningColorArgb = warning.toArgb()
                )
            }
        }
    } else {
        drawWithCache {
            onDrawWithContent {
                if (activeIntensity > 0f) {
                    with(CyberFallbacks) {
                        drawSparksFallback(
                            primaryColor = primary,
                            secondaryColor = secondary,
                            warningColor = warning,
                            sparkCount = sparkCount,
                            intensity = activeIntensity,
                            time = time
                        )
                    }
                } else {
                    drawContent()
                }
            }
        }
    }
}

















