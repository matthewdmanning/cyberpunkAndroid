package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameMillis

internal data class CyberPingElement(
    val color: Color,
    val durationMillis: Int,
    val startScale: Float,
    val maxDiffuseScale: Float,
    val alphaDecayExponent: Float,
    val shape: Shape,
    val borderWidth: Dp,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: InfiniteRepeatableSpec<Float>
) : ModifierNodeElement<CyberPingNode>() {
    override fun create(): CyberPingNode {
        return CyberPingNode(
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
    }

    override fun update(node: CyberPingNode) {
        node.update(
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
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberPingNode"
        properties["color"] = color
        properties["durationMillis"] = durationMillis
        properties["startScale"] = startScale
        properties["maxDiffuseScale"] = maxDiffuseScale
        properties["alphaDecayExponent"] = alphaDecayExponent
        properties["shape"] = shape
        properties["borderWidth"] = borderWidth
        properties["trigger"] = trigger
        properties["interactionSource"] = interactionSource
        properties["animationSpec"] = animationSpec
    }
}

internal class CyberPingNode(
    private var color: Color,
    private var durationMillis: Int,
    private var startScale: Float,
    private var maxDiffuseScale: Float,
    private var alphaDecayExponent: Float,
    private var shape: Shape,
    private var borderWidth: Dp,
    private var trigger: CyberInteractionTrigger,
    private var interactionSource: InteractionSource?,
    private var animationSpec: InfiniteRepeatableSpec<Float>
) : Modifier.Node(), DrawModifierNode {

    private val progress = Animatable(0f)
    private var animationJob: Job? = null
    private var invalidationJob: Job? = null

    override fun onAttach() {
        super.onAttach()
        launchAnimation()
        invalidationJob = coroutineScope.launch {
            while (true) {
                withFrameMillis {
                    if (isActive()) {
                        invalidateDraw()
                    }
                }
            }
        }
    }

    fun update(
        color: Color,
        durationMillis: Int,
        startScale: Float,
        maxDiffuseScale: Float,
        alphaDecayExponent: Float,
        shape: Shape,
        borderWidth: Dp,
        trigger: CyberInteractionTrigger,
        interactionSource: InteractionSource?,
        animationSpec: InfiniteRepeatableSpec<Float>
    ) {
        this.color = color
        this.durationMillis = durationMillis
        this.startScale = startScale
        this.maxDiffuseScale = maxDiffuseScale
        this.alphaDecayExponent = alphaDecayExponent
        this.shape = shape
        this.borderWidth = borderWidth

        val triggerChanged = this.trigger != trigger || this.interactionSource != interactionSource || this.animationSpec != animationSpec
        this.trigger = trigger
        this.interactionSource = interactionSource
        this.animationSpec = animationSpec

        if (triggerChanged && isAttached) {
            launchAnimation()
        }
    }

    private fun isActive(): Boolean {
        // Simplified trigger logic
        return trigger == CyberInteractionTrigger.ALWAYS
    }

    private fun launchAnimation() {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            if (isActive()) {
                progress.snapTo(0f)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = animationSpec
                )
            } else {
                progress.snapTo(0f)
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()

        if (!isActive()) return

        val currentProgress = progress.value
        val waveColor = if (color == Color.Unspecified) CyberPrimitives.Colors.Cyan500 else color

        val maxDelta = maxDiffuseScale - startScale
        val diffuseScale = startScale + currentProgress * maxDelta
        val diffuseAlpha = (1f - currentProgress).toDouble().coerceAtLeast(0.0).let { Math.pow(it, alphaDecayExponent.toDouble()) }.toFloat()

        val strokeWidthPx = borderWidth.toPx()
        scale(diffuseScale, diffuseScale) {
            val outline = shape.createOutline(size, layoutDirection, this)
            val path = Path().apply { addOutline(outline) }
            drawPath(
                path = path,
                color = waveColor.copy(alpha = diffuseAlpha),
                style = Stroke(width = strokeWidthPx / diffuseScale)
            )
        }
    }
}
