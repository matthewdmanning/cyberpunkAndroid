package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.ui.node.invalidatePlacement

data class CyberBounceElement(
    val height: Dp,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: AnimationSpec<Float>,
    val exitAnimationSpec: AnimationSpec<Float>
) : ModifierNodeElement<CyberBounceNode>() {

    override fun create(): CyberBounceNode {
        return CyberBounceNode(height, trigger, interactionSource, animationSpec, exitAnimationSpec)
    }

    override fun update(node: CyberBounceNode) {
        node.update(height, trigger, interactionSource, animationSpec, exitAnimationSpec)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberBounce"
        properties["height"] = height
        properties["trigger"] = trigger
        properties["interactionSource"] = interactionSource
        properties["animationSpec"] = animationSpec
        properties["exitAnimationSpec"] = exitAnimationSpec
    }
}

class CyberBounceNode(
    var height: Dp,
    var trigger: CyberInteractionTrigger,
    var interactionSource: InteractionSource?,
    var animationSpec: AnimationSpec<Float>,
    var exitAnimationSpec: AnimationSpec<Float>
) : Modifier.Node(), LayoutModifierNode {

    private val progress = Animatable(0f)
    private var animationJob: Job? = null
    private var interactionJob: Job? = null

    private var isHovered = false
    private var isPressed = false
    private var isFocused = false

    override fun onAttach() {
        super.onAttach()
        observeInteractions()
        updateAnimation()
    }

    fun update(
        height: Dp,
        trigger: CyberInteractionTrigger,
        interactionSource: InteractionSource?,
        animationSpec: AnimationSpec<Float>,
        exitAnimationSpec: AnimationSpec<Float>
    ) {
        val interactionChanged = this.interactionSource != interactionSource || this.trigger != trigger
        this.height = height
        this.trigger = trigger
        this.interactionSource = interactionSource
        this.animationSpec = animationSpec
        this.exitAnimationSpec = exitAnimationSpec

        if (interactionChanged) {
            observeInteractions()
            updateAnimation()
        }
        invalidatePlacement()
    }

    private fun observeInteractions() {
        interactionJob?.cancel()
        if (interactionSource == null || trigger == CyberInteractionTrigger.ALWAYS || trigger == CyberInteractionTrigger.NONE) {
            return
        }

        interactionJob = coroutineScope.launch {
            interactionSource?.interactions?.collect { interaction ->
                when (interaction) {
                    is HoverInteraction.Enter -> isHovered = true
                    is HoverInteraction.Exit, -> isHovered = false
                    is PressInteraction.Press -> isPressed = true
                    is PressInteraction.Release, is PressInteraction.Cancel -> isPressed = false
                    is FocusInteraction.Focus -> isFocused = true
                    is FocusInteraction.Unfocus -> isFocused = false
                }
                updateAnimation()
                invalidatePlacement()
            }
        }
    }

    private fun isActive(): Boolean {
        if (trigger == CyberInteractionTrigger.ALWAYS) return true
        if (trigger == CyberInteractionTrigger.NONE) return false
        if (interactionSource == null) return false
        return when (trigger) {
            CyberInteractionTrigger.HOVER -> isHovered
            CyberInteractionTrigger.PRESS -> isPressed
            CyberInteractionTrigger.FOCUS -> isFocused
            else -> true
        }
    }

    private fun updateAnimation() {
        val active = isActive()
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            if (active) {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = animationSpec
                ) {
                    invalidatePlacement()
                }
            } else {
                progress.animateTo(
                    targetValue = 0f,
                    animationSpec = exitAnimationSpec
                ) {
                    invalidatePlacement()
                }
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                translationY = -height.toPx() * progress.value
            }
        }
    }
}
