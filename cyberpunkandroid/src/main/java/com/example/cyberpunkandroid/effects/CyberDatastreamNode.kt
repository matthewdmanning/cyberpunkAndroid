package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import com.example.cyberpunkandroid.utils.drawDatastreamGradient
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import androidx.compose.runtime.withFrameNanos

class CyberDatastreamElement(
    val color: Color,
    val speed: Float,
    val maxAlpha: Float,
    val mirror: Boolean,
    val alphaTransform: (Float) -> Float,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: AnimationSpec<Float>
) : ModifierNodeElement<CyberDatastreamNode>() {
    override fun create() = CyberDatastreamNode(
        color, speed, maxAlpha, mirror, alphaTransform, trigger, interactionSource, animationSpec
    )

    override fun update(node: CyberDatastreamNode) {
        node.color = color
        node.speed = speed
        node.maxAlpha = maxAlpha
        node.mirror = mirror
        node.alphaTransform = alphaTransform
        node.trigger = trigger
        node.interactionSource = interactionSource
        node.animationSpec = animationSpec
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberDatastream"
        properties["color"] = color
        properties["speed"] = speed
        properties["maxAlpha"] = maxAlpha
        properties["mirror"] = mirror
        properties["alphaTransform"] = alphaTransform
        properties["trigger"] = trigger
        properties["interactionSource"] = interactionSource
        properties["animationSpec"] = animationSpec
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CyberDatastreamElement) return false
        return color == other.color &&
               speed == other.speed &&
               maxAlpha == other.maxAlpha &&
               mirror == other.mirror &&
               alphaTransform === other.alphaTransform &&
               trigger == other.trigger &&
               interactionSource == other.interactionSource &&
               animationSpec == other.animationSpec
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + speed.hashCode()
        result = 31 * result + maxAlpha.hashCode()
        result = 31 * result + mirror.hashCode()
        result = 31 * result + alphaTransform.hashCode()
        result = 31 * result + trigger.hashCode()
        result = 31 * result + (interactionSource?.hashCode() ?: 0)
        result = 31 * result + animationSpec.hashCode()
        return result
    }
}

class CyberDatastreamNode(
    var color: Color,
    var speed: Float,
    var maxAlpha: Float,
    var mirror: Boolean,
    var alphaTransform: (Float) -> Float,
    var trigger: CyberInteractionTrigger,
    var interactionSource: InteractionSource?,
    var animationSpec: AnimationSpec<Float>
) : Modifier.Node(), DrawModifierNode {

    private val level = Animatable(0f)
    private var time = 0f

    override fun onAttach() {
        super.onAttach()
        coroutineScope.launch {
            level.animateTo(maxAlpha, animationSpec)
        }
        coroutineScope.launch {
            var lastTime = 0L
            while (isActive) {
                withFrameNanos { frameTime ->
                    if (lastTime == 0L) lastTime = frameTime
                    val delta = (frameTime - lastTime) / 1_000_000_000f
                    time += delta
                    lastTime = frameTime
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (level.value > 0f && size.height > 0f) {
            val baseExtent = size.height * 0.5f
            val extent = if (mirror) baseExtent * 0.5f else baseExtent
            val resolution = 8

            val colors = List(resolution) { index ->
                val factor = index.toFloat() / (resolution - 1)
                color.copy(alpha = alphaTransform(factor) * level.value)
            }

            drawDatastreamGradient(
                extent = extent,
                forwardCenter = (time * speed * 100f) % size.height,
                mirror = mirror,
                colors = colors
            )
        }
    }
}
