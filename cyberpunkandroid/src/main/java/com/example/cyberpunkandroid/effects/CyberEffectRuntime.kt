package com.example.cyberpunkandroid.effects

import android.annotation.SuppressLint
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.draw.CacheDrawScope

/*
 * Shared runtime behind every animated Cyber effect modifier: a frame clock, trigger-driven strength,
 * and one shader-or-fallback rendering layer. Internal: the public interface stays the Modifier.cyber* functions.
 */

/**
 * Seconds elapsed since the effect started, updated every frame.
 *
 * Wraps every 100 s so Float precision doesn't degrade over long uptimes (the wrap is a single-frame jump).
 * When [running] is false the clock holds at 0 and no frame callbacks are scheduled.
 */
@Composable
internal fun rememberEffectClock(running: Boolean = true): State<Float> = produceState(0f, running) {
    if (!running) {
        value = 0f
        return@produceState
    }
    while (true) {
        withInfiniteAnimationFrameMillis { frameTime ->
            value = (frameTime % 100_000L) / 1000f
        }
    }
}

/**
 * Resolves [trigger] against [interactionSource] and animates toward [level] while active, 0 otherwise.
 *
 * Starts at its first target without animating (so an ALWAYS effect appears immediately), then animates
 * with [enterSpec] when activating and [exitSpec] when deactivating. Read the returned state inside draw or
 * graphicsLayer lambdas so frame updates skip recomposition.
 */
@Composable
internal fun animateTriggeredLevel(
    trigger: CyberInteractionTrigger,
    interactionSource: InteractionSource?,
    level: Float,
    enterSpec: AnimationSpec<Float>,
    exitSpec: AnimationSpec<Float> = enterSpec,
    label: String
): State<Float> {
    val active = trigger.isActive(interactionSource)
    return animateFloatAsState(
        targetValue = if (active) level else 0f,
        animationSpec = if (active) enterSpec else exitSpec,
        label = label
    )
}

/**
 * Progress from 0 to 1 that runs [enterSpec] each time [active] becomes true and [exitSpec] back to 0 when it
 * becomes false. Always starts at 0, so entrances (boot flicker, float, bounce) play on first composition.
 * [enterSpec] may be infinite (e.g. a repeating float); it is cancelled on deactivation.
 */
@Composable
internal fun animateTriggeredProgress(
    active: Boolean,
    enterSpec: AnimationSpec<Float>,
    exitSpec: AnimationSpec<Float>
): State<Float> {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(active) {
        progress.animateTo(if (active) 1f else 0f, if (active) enterSpec else exitSpec)
    }
    return progress.asState()
}

/**
 * Typed uniform setter handed to [cyberShaderEffect]. Only ever constructed on API 33+.
 */
internal class ShaderUniforms @RequiresApi(Build.VERSION_CODES.TIRAMISU) constructor(
    private val shader: RuntimeShader
) {
    @SuppressLint("NewApi") // Constructor requires API 33, so every instance lives on API 33+
    fun floatUniform(name: String, vararg values: Float) = shader.setFloatUniform(name, values)

    @SuppressLint("NewApi") // Constructor requires API 33, so every instance lives on API 33+
    fun colorUniform(name: String, color: Color) = shader.setColorUniform(name, color.toArgb())
}

/** Fallback draw for API < 33: must call drawContent() itself. Receives the current effect level (> 0). */
internal typealias CyberFallbackDraw = ContentDrawScope.(level: Float) -> Unit

/**
 * Renders the content through an AGSL runtime shader on API 33+, or through [fallback] on older APIs.
 *
 * Both paths are skipped (content drawn untouched) while [level] is 0, and the shader path also waits for a
 * non-empty size. The shader must declare `uniform float2 resolution` and `uniform shader contents`;
 * resolution is set here, everything else in [uniforms].
 *
 * @param shaderSource AGSL source, compiled once per call site.
 * @param level Effect strength; 0 disables the effect.
 * @param uniforms Sets the shader's own uniforms for the current size and level.
 * @param fallback Builds the API < 33 draw once per size (use the CacheDrawScope for layers or brushes).
 * @param clipWhenIdle Whether to clip to bounds while the effect is off (it always clips while on).
 * @param shaderLayer Extra graphicsLayer properties for the shader path (e.g. a bounce translation).
 */
internal fun Modifier.cyberShaderEffect(
    shaderSource: String,
    level: State<Float>,
    uniforms: ShaderUniforms.(size: Size, level: Float) -> Unit,
    fallback: CacheDrawScope.() -> CyberFallbackDraw,
    clipWhenIdle: Boolean = true,
    shaderLayer: GraphicsLayerScope.() -> Unit = {}
): Modifier = composed {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember(shaderSource) { RuntimeShader(shaderSource) }
        val binder = remember(shader) { ShaderUniforms(shader) }
        graphicsLayer {
            val current = level.value
            val applies = current > 0f && size.width > 0f && size.height > 0f
            clip = clipWhenIdle || applies
            shaderLayer()
            if (applies) {
                binder.float("resolution", size.width, size.height)
                binder.uniforms(size, current)
                renderEffect = android.graphics.RenderEffect
                    .createRuntimeShaderEffect(shader, "contents")
                    .asComposeRenderEffect()
            }
        }
    } else {
        drawWithCache {
            val draw = fallback()
            onDrawWithContent {
                val current = level.value
                if (current > 0f) this.draw(current) else drawContent()
            }
        }
    }
}
