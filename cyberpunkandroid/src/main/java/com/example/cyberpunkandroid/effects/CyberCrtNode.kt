package com.example.cyberpunkandroid.effects

import android.graphics.RuntimeShader
import androidx.compose.animation.core.snap
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo

internal data class CyberCrtElement(
    val enabled: Boolean,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?
) : ModifierNodeElement<CyberCrtNode>() {
    override fun create(): CyberCrtNode {
        return CyberCrtNode(
            enabled = enabled,
            trigger = trigger,
            interactionSource = interactionSource
        )
    }

    override fun update(node: CyberCrtNode) {
        node.update(
            enabled = enabled,
            trigger = trigger,
            interactionSource = interactionSource
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberCrtNode"
        properties["enabled"] = enabled
        properties["trigger"] = trigger
        properties["interactionSource"] = interactionSource
    }
}

internal class CyberCrtNode(
    private var enabled: Boolean,
    trigger: CyberInteractionTrigger,
    interactionSource: InteractionSource?
) : CyberEffectBaseNode(
    shaderSource = CyberShaders.CrtShader,
    trigger = trigger,
    interactionSource = interactionSource,
    targetOpacity = if (enabled) 1f else 0f, // Instant toggle with snap spec
    animationSpec = snap(),
    clipWhenIdle = false
) {
    fun update(
        enabled: Boolean,
        trigger: CyberInteractionTrigger,
        interactionSource: InteractionSource?
    ) {
        this.enabled = enabled
        updateBase(
            trigger = trigger,
            interactionSource = interactionSource,
            targetOpacity = if (enabled) 1f else 0f,
            animationSpec = snap(),
            clipWhenIdle = false
        )
    }

    override fun applyShaderUniforms(shader: RuntimeShader, size: Size, clock: Float, current: Float) {
        shader.setFloatUniform("time", clock)
    }

    override fun ContentDrawScope.drawFallback(current: Float, clock: Float) {
        val radialGradient = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
            radius = size.width.coerceAtLeast(size.height) * 0.75f,
            center = Offset(size.width / 2f, size.height / 2f)
        )
        drawContent()
        drawRect(brush = radialGradient)
    }
}
