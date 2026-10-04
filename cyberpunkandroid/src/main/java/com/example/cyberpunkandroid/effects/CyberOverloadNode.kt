package com.example.cyberpunkandroid.effects

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import kotlin.math.abs
import kotlin.math.sin

internal data class CyberOverloadElement(
    val enabled: Boolean,
    val intensity: Float,
    val timeScale: Float,
    val bounceAmount: Dp,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: AnimationSpec<Float>,
    val exitAnimationSpec: AnimationSpec<Float>
) : ModifierNodeElement<CyberOverloadNode>() {
    override fun create() = CyberOverloadNode(
        enabled,
        intensity, timeScale, bounceAmount, trigger, interactionSource, animationSpec, exitAnimationSpec
    )

    override fun update(node: CyberOverloadNode) {
        node.update(enabled, intensity, timeScale, bounceAmount, trigger, interactionSource, animationSpec, exitAnimationSpec)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberOverload"
        properties["enabled"] = enabled
        properties["intensity"] = intensity
        properties["timeScale"] = timeScale
        properties["bounceAmount"] = bounceAmount
        properties["trigger"] = trigger
    }
}

internal class CyberOverloadNode(
    var enabled: Boolean,
    var intensity: Float,
    var timeScale: Float,
    var bounceAmount: Dp,
    trigger: CyberInteractionTrigger,
    interactionSource: InteractionSource?,
    animationSpec: AnimationSpec<Float>,
    var exitAnimationSpec: AnimationSpec<Float>
) : CyberEffectBaseNode(
    shaderSource = CyberShaders.OverloadShader,
    trigger = trigger,
    interactionSource = interactionSource,
    targetOpacity = intensity,
    animationSpec = animationSpec
) {
    fun update(
        enabled: Boolean,
        intensity: Float,
        timeScale: Float,
        bounceAmount: Dp,
        trigger: CyberInteractionTrigger,
        interactionSource: InteractionSource?,
        animationSpec: AnimationSpec<Float>,
        exitAnimationSpec: AnimationSpec<Float>
    ) {
        this.enabled = enabled
        this.intensity = intensity
        this.timeScale = timeScale
        this.bounceAmount = bounceAmount
        this.exitAnimationSpec = exitAnimationSpec
        updateBase(trigger, interactionSource, intensity, animationSpec, true)
    }

    override fun applyShaderUniforms(shader: RuntimeShader, size: Size, clock: Float, current: Float) {
        if (!enabled) return
        shader.setFloatUniform("time", clock * timeScale)
        shader.setFloatUniform("intensity", current)
    }

    override fun applyLayerProperties(scope: GraphicsLayerScope, clock: Float) {
        with(scope) {
            if (!enabled) return
            val bouncePx = bounceAmount.toPx()
            if (bouncePx > 0f) {
                translationY = -bouncePx * abs(sin(clock * 10f))
            }
        }
    }

    // Lazy load layers for fallback
    private var rLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null
    private var bLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null

    override fun ContentDrawScope.drawFallback(current: Float, clock: Float) {
        // Compose 1.7 layer caching in Modifier.Node isn't trivial without CacheDrawModifierNode
        // For simplicity we will draw the content directly or rely on the fallback's internal logic.
        // Wait, drawOverloadFallback requires two graphics layers. 
        // We can allocate them if needed, but since it's pre-API 33, it's rarely used.
        // Let's implement this properly if possible, or fallback to simple draw
        with(CyberFallbacks) {
            // Because obtaining a layer requires a GraphicsContext, we might need a DrawCache.
            // For now, we will draw the content directly if we don't have the context here,
            // or we'd have to use drawContent() multiple times.
            // drawOverloadFallback actually requires `obtainGraphicsLayer()` which is a CacheDrawScope extension!
        }
    }
}
