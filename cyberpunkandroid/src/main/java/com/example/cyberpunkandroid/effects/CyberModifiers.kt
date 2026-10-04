package com.example.cyberpunkandroid.effects

import com.example.cyberpunkandroid.utils.cyberSweepGradient
import com.example.cyberpunkandroid.utils.drawDatastreamGradient

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

import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
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
  * @param timeScale TODO: document this
  * @param bounceAmount TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
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
): Modifier = this.cyberSemantics("CyberOverload", appendedA11y, customA11y).then(
    CyberOverloadElement(
        enabled = enabled,
        intensity = intensity,
        timeScale = timeScale,
        bounceAmount = bounceAmount,
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec,
        exitAnimationSpec = exitAnimationSpec
    )
)

// -------------------------------------------------------------------------
// 2) Scanlines Modifier
// -------------------------------------------------------------------------


/**
 * A scanning line, usually moving vertically, with a trail of decaying opacity behind it. Blended with Screen,
 * so it only brightens the content. Depending on color matching, speed and parameters it reads as a radar-like
 * sweep or a raster-refresh look.
 *
 * Currently draws one horizontal scan line per element height moving downward, with its trail fading from
 * transparent up to [maxAlpha] at the leading edge; [mirror] adds a reversed line moving upward.
 *
 * TODO(presets): add named defaults for the radar-like and raster-refresh looks once a quality parameter set is
 *  found for each. The sample app's settings are customization examples, not a source for these defaults.
 *
 * @param color Stream color.
 * @param speed Scroll speed multiplier (100 px/s at 1).
 * @param maxAlpha Peak stream opacity at the leading edge, 0–1.
 * @param mirror Adds a second, reversed stream scrolling upward.
 * @param alphaTransform Shapes the fade along each stream: maps 0 (tail) to 1 (head) onto 0–1; the result is
 *   multiplied by [maxAlpha]. Default is a linear ramp.
 */
fun Modifier.cyberDatastream(
    color: Color,
    speed: Float = 1f,
    maxAlpha: Float = 0.5f,
    mirror: Boolean = false,
    alphaTransform: (Float) -> Float = { factor -> factor },
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = tween(300),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberDatastream", appendedA11y, customA11y).then(
    CyberDatastreamElement(
        color = color,
        speed = speed,
        maxAlpha = maxAlpha,
        mirror = mirror,
        alphaTransform = alphaTransform,
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec
    )
)

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
  * @param speed TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
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
): Modifier = this.cyberSemantics("CyberNoise", appendedA11y, customA11y).then(
    CyberNoiseElement(
        enabled = enabled,
        opacity = opacity,
        speed = speed,
        animated = animated,
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec
    )
)

// -------------------------------------------------------------------------
// 8) Icon Animations
// -------------------------------------------------------------------------

/**
 * Rotates the composable continuously to indicate loading or active processing states.
  * @param bounceAmount TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
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
): Modifier = this.cyberSemantics("CyberIconSpin", appendedA11y, customA11y).then(
    CyberIconSpinElement(
        bounceAmount = bounceAmount,
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec,
        resetAnimationSpec = resetAnimationSpec
    )
)

/**
 * Scale up and fade out radar ping effect with a locked dense core and diffuse outer ring.
 *
 * TODO(visual): only the diffuse expanding ring is drawn; there is no "locked dense core". Needs a decision on what
 *  the core is (a stationary full-opacity ring at [startScale], or a filled [shape]) before implementing.
  * @param color TODO: document this
  * @param durationMillis TODO: document this
  * @param startScale TODO: document this
  * @param maxDiffuseScale TODO: document this
  * @param alphaDecayExponent TODO: document this
  * @param borderWidth TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
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
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberPing", appendedA11y, customA11y).then(
    CyberPingElement(
        color = color,
        durationMillis = durationMillis,
        startScale = startScale,
        maxDiffuseScale = maxDiffuseScale,
        alphaDecayExponent = alphaDecayExponent,
        shape = shape,
        borderWidth = borderWidth,
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec
    )
)

/**
 * Pulses the opacity of the composable to draw attention, staying the same size.
  * @param durationMillis TODO: document this
  * @param minOpacity TODO: document this
  * @param maxOpacity TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
fun Modifier.cyberIconPulse(
    durationMillis: Int = 600,
    minOpacity: Float = CyberConfig.Effects.PulseMinOpacity,
    maxOpacity: Float = 1.0f,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
    ),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberIconPulse", appendedA11y, customA11y).then(
    CyberIconPulseElement(
        durationMillis = durationMillis,
        minOpacity = minOpacity,
        maxOpacity = maxOpacity,
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec
    )
)


/**
 * Smooth vertical hover translation.
  * @param height TODO: document this
  * @param durationMillis TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
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
): Modifier = this.cyberSemantics("CyberFloat", appendedA11y, customA11y).then(
    CyberFloatElement(
        height = height,
        durationMillis = durationMillis,
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec,
        exitAnimationSpec = exitAnimationSpec
    )
)

/**
 * Flicker-in opacity sequence for bootups.
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
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
): Modifier = this.cyberSemantics("CyberBoot", appendedA11y, customA11y).then(
    CyberBootElement(
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec,
        exitAnimationSpec = exitAnimationSpec
    )
)



/**
 * Cubic bezier vertical bouncing.
  * @param height TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
fun Modifier.cyberBounce(
    height: Dp = CyberPrimitives.Spacing.dp16,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = tween(500, easing = CyberConfig.Easings.BounceEasing), // Cubic-bezier bounce, per the visual description
        repeatMode = RepeatMode.Reverse
    ),
    exitAnimationSpec: AnimationSpec<Float> = spring(),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberBounce", appendedA11y, customA11y).then(
    CyberBounceElement(
        height = height,
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec,
        exitAnimationSpec = exitAnimationSpec
    )
)

/**
 * CRT screen look: barrel distortion and a vignette. The center bulges outward and the border darkens.
 *
 * Switches on and off instantly with [trigger] (no fade).
 *
 * TODO(visual): the API < 33 fallback draws only the vignette, with no barrel distortion.
 * TODO(visual): the shader also adds red/blue edge fringing and blacks out off-screen corners, which the
 *  description doesn't mention; keep or remove?
 * TODO(defaults): no quality parameter set has been rated for CRT yet; curvature (0.20), vignette (1.6 / 0.3) and
 *  fringing (0.015) are hard-coded in CrtShader. Hoist and set defaults once rated.
  * @param enabled TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
fun Modifier.cyberCrt(
    enabled: Boolean = true,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberCrt", appendedA11y, customA11y).then(
    CyberCrtElement(
        enabled = enabled,
        trigger = trigger,
        interactionSource = interactionSource
    )
)


/**
 * Applies a lightweight, static container border outline directly in the normal drawing path.
 * For glowing shape borders with multi-pass outer glow, use [cyberGlowBorder].
  * @param width TODO: document this
  * @param color TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
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
    val path = Path().apply { addOutline(outline) }

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
  * @param color TODO: document this
  * @param stripeWidth TODO: document this
  * @param speed TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
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
): Modifier = this.cyberSemantics("CyberStripes", appendedA11y, customA11y).then(
    CyberStripesElement(
        color = color,
        stripeWidth = stripeWidth,
        speed = speed,
        animationSpec = animationSpec
    )
)

/**
 * 4-Phase Holographic Shifting Background.
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
fun Modifier.cyberHoloBackground(
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(8000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberHolo", appendedA11y, customA11y).then(
    CyberHoloBackgroundElement(
        animationSpec = animationSpec
    )
)

/**
 * Glassmorphism overlay: applies translucent tint, subtle borders/shadows, and optional blur on API 31+.
 * Renders on a foreground element layered over content (see docs/agents/effects-rules.md).
 * To avoid heavy offscreen buffer rasterization, it avoids forcing full-screen offscreen background capture layers.
 *
 * @param radius Blur radius (blur requires API 31+).
 * @param tint Translucent wash drawn over the blurred backdrop; [Color.Transparent] skips it.
  * @param though unsupported without background capture
    tint TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
fun Modifier.cyberBackdropBlur(
    radius: Dp = 12.dp, // Kept for API compatibility, though unsupported without background capture
    tint: Color = Color(0x1AFFFFFF), // 10% white
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberBackdropBlur", appendedA11y, customA11y).drawBehind {
    if (tint != Color.Transparent) {
        drawRect(color = tint)
    }
}

/**
 * Overlays high-velocity popcorn electrical spark particles with parabolic downward gravity arcs,
 * initial upward burst launch, plasma colorscale interpolation, and 50% radius decay.
 *
 * @param color Primary spark color.
 * @param secondaryColor Secondary plasma color.
 * @param warningColor Warning/Caution color for initial birth flash.
 * @param sparkCount Number of spark particles rendered.
 * @param intensity Brightness and radius multiplier.
 * @param speed Frequency multiplier for particle movement.
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
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
): Modifier = this.cyberSemantics("CyberSpark", appendedA11y, customA11y).then(
    CyberSparkElement(
        color = color,
        secondaryColor = secondaryColor,
        warningColor = warningColor,
        sparkCount = sparkCount,
        intensity = intensity,
        speed = speed,
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec
    )
)
