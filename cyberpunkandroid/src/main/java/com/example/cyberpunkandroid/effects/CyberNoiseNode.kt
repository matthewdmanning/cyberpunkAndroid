package com.example.cyberpunkandroid.effects

import android.graphics.RuntimeShader
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo

internal data class CyberNoiseElement(
    val enabled: Boolean,
    val opacity: Float,
    val speed: Float,
    val animated: Boolean,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: AnimationSpec<Float>
) : ModifierNodeElement<CyberNoiseNode>() {
    override fun create(): CyberNoiseNode {
        return CyberNoiseNode(
            enabled = enabled,
            opacity = opacity,
            speed = speed,
            animated = animated,
            trigger = trigger,
            interactionSource = interactionSource,
            animationSpec = animationSpec
        )
    }

    override fun update(node: CyberNoiseNode) {
        node.update(
            enabled = enabled,
            opacity = opacity,
            speed = speed,
            animated = animated,
            trigger = trigger,
            interactionSource = interactionSource,
            animationSpec = animationSpec
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberNoiseNode"
        properties["enabled"] = enabled
        properties["opacity"] = opacity
        properties["speed"] = speed
        properties["animated"] = animated
        properties["trigger"] = trigger
        properties["interactionSource"] = interactionSource
        properties["animationSpec"] = animationSpec
    }
}

internal class CyberNoiseNode(
    private var enabled: Boolean,
    opacity: Float,
    private var speed: Float,
    private var animated: Boolean,
    trigger: CyberInteractionTrigger,
    interactionSource: InteractionSource?,
    animationSpec: AnimationSpec<Float>
) : CyberEffectBaseNode(
    shaderSource = CyberShaders.NoiseShader,
    trigger = trigger,
    interactionSource = interactionSource,
    targetOpacity = if (enabled) opacity else 0f,
    animationSpec = animationSpec,
    clipWhenIdle = true
) {
    private var pausedClock = 0f

    fun update(
        enabled: Boolean,
        opacity: Float,
        speed: Float,
        animated: Boolean,
        trigger: CyberInteractionTrigger,
        interactionSource: InteractionSource?,
        animationSpec: AnimationSpec<Float>
    ) {
        this.enabled = enabled
        this.speed = speed
        
        if (this.animated && !animated) {
            pausedClock = clock
        } else if (!this.animated && animated) {
            // Simple resume logic
        }
        this.animated = animated

        updateBase(
            trigger = trigger,
            interactionSource = interactionSource,
            targetOpacity = if (enabled) opacity else 0f,
            animationSpec = animationSpec,
            clipWhenIdle = true
        )
    }

    private fun getEffectiveClock(currentClock: Float): Float {
        return if (animated) currentClock else pausedClock
    }

    override fun applyShaderUniforms(shader: RuntimeShader, size: Size, clock: Float, current: Float) {
        shader.setFloatUniform("time", getEffectiveClock(clock) * speed)
        shader.setFloatUniform("intensity", current)
    }

    override fun ContentDrawScope.drawFallback(current: Float, clock: Float) {
        with(CyberFallbacks) {
            drawNoiseFallback(current, getEffectiveClock(clock) * speed)
        }
    }
}
