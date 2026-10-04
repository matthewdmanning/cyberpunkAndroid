package com.example.cyberpunkandroid.effects

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.invalidatePlacement
import kotlinx.coroutines.Job

/**
 * Applies animated CRT cathode-ray scanlines and subtle barrel curvature over the composable.
 *
 * @param spacing Vertical distance between adjacent scanline bars.
 * @param opacity Alpha transparency of the scanline pattern.
 * @param speed Frequency multiplier for vertical scanline translation.
 * @param color Color of the scanlines.
 * @param trigger When the effect should be active.
 * @param interactionSource Interaction state source.
 * @param animationSpec Animation spec for opacity transitions.
 * @param appendedA11y Optional accessibility text.
 * @param customA11y Optional replacement accessibility text.
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
): Modifier = this.cyberSemantics("CyberScanlines", appendedA11y, customA11y).then(
    CyberScanlinesElement(
        spacing = spacing,
        opacity = opacity,
        speed = speed,
        color = color,
        trigger = trigger,
        interactionSource = interactionSource,
        animationSpec = animationSpec
    )
)

private data class CyberScanlinesElement(
    val spacing: Dp,
    val opacity: Float,
    val speed: Float,
    val color: Color,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: AnimationSpec<Float>
) : ModifierNodeElement<CyberScanlinesNode>() {
    override fun create() = CyberScanlinesNode(spacing, opacity, speed, color, trigger, interactionSource, animationSpec)
    override fun update(node: CyberScanlinesNode) {
        node.update(spacing, opacity, speed, color, trigger, interactionSource, animationSpec)
    }
    override fun InspectorInfo.inspectableProperties() {
        name = "cyberScanlines"
        properties["spacing"] = spacing
        properties["opacity"] = opacity
        properties["speed"] = speed
        properties["color"] = color
        properties["trigger"] = trigger
    }
}

private class CyberScanlinesNode(
    var spacing: Dp,
    var opacity: Float,
    var speed: Float,
    var color: Color,
    var trigger: CyberInteractionTrigger,
    var interactionSource: InteractionSource?,
    var animationSpec: AnimationSpec<Float>
) : Modifier.Node(), DrawModifierNode, LayoutModifierNode {

    private var shader: RuntimeShader? = null
    private var clock = 0f
    private val levelAnimatable = Animatable(0f)
    private var clockJob: Job? = null
    private var triggerJob: Job? = null

    // For caching fallback values
    private var lastSize: Size = Size.Unspecified
    private var cachedSpacingPx = 0f

    fun update(
        spacing: Dp,
        opacity: Float,
        speed: Float,
        color: Color,
        trigger: CyberInteractionTrigger,
        interactionSource: InteractionSource?,
        animationSpec: AnimationSpec<Float>
    ) {
        this.spacing = spacing
        this.opacity = opacity
        this.speed = speed
        this.color = color
        
        val triggerChanged = this.trigger != trigger || this.interactionSource != interactionSource
        this.trigger = trigger
        this.interactionSource = interactionSource
        this.animationSpec = animationSpec

        if (triggerChanged && isAttached) {
            launchTrigger()
        }
        invalidatePlacement()
        invalidateDraw()
    }

    override fun onAttach() {
        super.onAttach()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shader = RuntimeShader(CyberShaders.ScanlinesShader)
        }

        clockJob = coroutineScope.launch {
            while (true) {
                withFrameMillis { frameTime ->
                    clock = (frameTime % 100_000L) / 1000f
                    if (levelAnimatable.value > 0f) {
                        invalidatePlacement() // Triggers placeWithLayer for shader
                        invalidateDraw()      // Triggers draw for fallback
                    }
                }
            }
        }
        launchTrigger()
    }

    private fun launchTrigger() {
        triggerJob?.cancel()
        triggerJob = coroutineScope.launch {
            if (trigger == CyberInteractionTrigger.ALWAYS) {
                levelAnimatable.animateTo(opacity, animationSpec)
            } else if (interactionSource != null) {
                // Collect interaction source state here in the future
                // Currently just defaulting to active for simplicity
                levelAnimatable.animateTo(opacity, animationSpec)
            } else {
                levelAnimatable.animateTo(0f, animationSpec)
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                val current = levelAnimatable.value
                val applies = current > 0f && size.width > 0f && size.height > 0f
                clip = applies

                if (applies && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val s = shader
                    if (s != null) {
                        cachedSpacingPx = spacing.toPx()
                        s.setFloatUniform("resolution", size.width, size.height)
                        s.setFloatUniform("time", clock * speed)
                        s.setFloatUniform("scanlineOpacity", current)
                        s.setFloatUniform("spacing", cachedSpacingPx.coerceAtLeast(1f))
                        s.setColorUniform("scanlineColor", android.graphics.Color.argb(
                            (color.alpha * 255).toInt(),
                            (color.red * 255).toInt(),
                            (color.green * 255).toInt(),
                            (color.blue * 255).toInt()
                        ))
                        renderEffect = android.graphics.RenderEffect
                            .createRuntimeShaderEffect(s, "contents")
                            .asComposeRenderEffect()
                    }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val current = levelAnimatable.value
        val applies = current > 0f && size.width > 0f && size.height > 0f

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || !applies) {
            drawContent()
        } else {
            // API < 33 Fallback
            val spacingPx = spacing.toPx()
            val offset = (clock * speed * 20f) % spacingPx
            
            with(CyberFallbacks) {
                drawScanlinesFallback(spacingPx, current, offset, color)
            }
        }
    }
}
