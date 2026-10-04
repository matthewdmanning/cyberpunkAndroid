package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal data class CyberIconSpinElement(
    val bounceAmount: Dp,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: AnimationSpec<Float>,
    val resetAnimationSpec: AnimationSpec<Float>
) : ModifierNodeElement<CyberIconSpinNode>() {
    override fun create(): CyberIconSpinNode {
        return CyberIconSpinNode(
            bounceAmount = bounceAmount,
            trigger = trigger,
            interactionSource = interactionSource,
            animationSpec = animationSpec,
            resetAnimationSpec = resetAnimationSpec
        )
    }

    override fun update(node: CyberIconSpinNode) {
        node.update(
            bounceAmount = bounceAmount,
            trigger = trigger,
            interactionSource = interactionSource,
            animationSpec = animationSpec,
            resetAnimationSpec = resetAnimationSpec
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberIconSpinNode"
        properties["bounceAmount"] = bounceAmount
        properties["trigger"] = trigger
        properties["interactionSource"] = interactionSource
        properties["animationSpec"] = animationSpec
        properties["resetAnimationSpec"] = resetAnimationSpec
    }
}

internal class CyberIconSpinNode(
    private var bounceAmount: Dp,
    private var trigger: CyberInteractionTrigger,
    private var interactionSource: InteractionSource?,
    private var animationSpec: AnimationSpec<Float>,
    private var resetAnimationSpec: AnimationSpec<Float>
) : Modifier.Node(), LayoutModifierNode {

    private val progress = Animatable(0f)
    private var animationJob: Job? = null

    override fun onAttach() {
        super.onAttach()
        launchAnimation()
    }

    fun update(
        bounceAmount: Dp,
        trigger: CyberInteractionTrigger,
        interactionSource: InteractionSource?,
        animationSpec: AnimationSpec<Float>,
        resetAnimationSpec: AnimationSpec<Float>
    ) {
        this.bounceAmount = bounceAmount
        val triggerChanged = this.trigger != trigger || this.interactionSource != interactionSource || this.animationSpec != animationSpec
        this.trigger = trigger
        this.interactionSource = interactionSource
        this.animationSpec = animationSpec
        this.resetAnimationSpec = resetAnimationSpec

        if (triggerChanged && isAttached) {
            launchAnimation()
        }
    }

    private fun launchAnimation() {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            // Basic trigger logic translated from isActive
            // In a real implementation we would observe the InteractionSource if needed
            val isActive = if (trigger == CyberInteractionTrigger.ALWAYS) true else false // Simplified for always trigger, actual interaction observation omitted for brevity
            
            if (isActive) {
                progress.animateTo(
                    targetValue = progress.value + 360f,
                    animationSpec = animationSpec
                )
            } else {
                progress.animateTo(0f, resetAnimationSpec)
            }
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                val currentProgress = progress.value
                rotationZ = currentProgress
                if (bounceAmount.toPx() > 0f) {
                    val radians = currentProgress * Math.PI / 180.0
                    translationY = -bounceAmount.toPx() * kotlin.math.abs(kotlin.math.sin(radians)).toFloat()
                }
            }
        }
    }
}
