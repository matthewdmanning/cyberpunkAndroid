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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.ui.node.invalidatePlacement

data class CyberBootElement(
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: AnimationSpec<Float>,
    val exitAnimationSpec: AnimationSpec<Float>
) : ModifierNodeElement<CyberBootNode>() {

    override fun create(): CyberBootNode {
        return CyberBootNode(trigger, interactionSource, animationSpec, exitAnimationSpec)
    }

    override fun update(node: CyberBootNode) {
        node.update(trigger, interactionSource, animationSpec, exitAnimationSpec)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberBoot"
        properties["trigger"] = trigger
        properties["interactionSource"] = interactionSource
        properties["animationSpec"] = animationSpec
        properties["exitAnimationSpec"] = exitAnimationSpec
    }
}

class CyberBootNode(
    var trigger: CyberInteractionTrigger,
    var interactionSource: InteractionSource?,
    var animationSpec: AnimationSpec<Float>,
    var exitAnimationSpec: AnimationSpec<Float>
) : Modifier.Node(), LayoutModifierNode {

    private var bootAlpha: Animatable<Float, *>? = null
    private var animationJob: Job? = null
    private var interactionJob: Job? = null

    private var isHovered = false
    private var isPressed = false
    private var isFocused = false

    override fun onAttach() {
        super.onAttach()
        bootAlpha = Animatable(if (isActive()) 0f else 1f)
        observeInteractions()
        updateAnimation(isInitial = true)
    }

    fun update(
        trigger: CyberInteractionTrigger,
        interactionSource: InteractionSource?,
        animationSpec: AnimationSpec<Float>,
        exitAnimationSpec: AnimationSpec<Float>
    ) {
        val interactionChanged = this.interactionSource != interactionSource || this.trigger != trigger
        this.trigger = trigger
        this.interactionSource = interactionSource
        this.animationSpec = animationSpec
        this.exitAnimationSpec = exitAnimationSpec

        if (interactionChanged) {
            observeInteractions()
            updateAnimation(isInitial = false)
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
                updateAnimation(isInitial = false)
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

    private fun updateAnimation(isInitial: Boolean) {
        val active = isActive()
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            val animatable = bootAlpha ?: return@launch
            if (active) {
                if (!isInitial) {
                    animatable.snapTo(0f)
                }
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = animationSpec
                ) {
                    invalidatePlacement()
                }
            } else {
                animatable.animateTo(
                    targetValue = 1f, // The exit animation for boot settles back to full opacity
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
                alpha = bootAlpha?.value ?: 1f
            }
        }
    }
}
