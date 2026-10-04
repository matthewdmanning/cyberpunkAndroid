package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.tween
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

data class CyberIconPulseElement(
    val durationMillis: Int,
    val minOpacity: Float,
    val maxOpacity: Float,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: InfiniteRepeatableSpec<Float>
) : ModifierNodeElement<CyberIconPulseNode>() {

    override fun create(): CyberIconPulseNode {
        return CyberIconPulseNode(
            durationMillis, minOpacity, maxOpacity, trigger, interactionSource, animationSpec
        )
    }

    override fun update(node: CyberIconPulseNode) {
        node.update(durationMillis, minOpacity, maxOpacity, trigger, interactionSource, animationSpec)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberIconPulse"
        properties["durationMillis"] = durationMillis
        properties["minOpacity"] = minOpacity
        properties["maxOpacity"] = maxOpacity
        properties["trigger"] = trigger
        properties["interactionSource"] = interactionSource
        properties["animationSpec"] = animationSpec
    }
}

class CyberIconPulseNode(
    var durationMillis: Int,
    var minOpacity: Float,
    var maxOpacity: Float,
    var trigger: CyberInteractionTrigger,
    var interactionSource: InteractionSource?,
    var animationSpec: InfiniteRepeatableSpec<Float>
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
        durationMillis: Int,
        minOpacity: Float,
        maxOpacity: Float,
        trigger: CyberInteractionTrigger,
        interactionSource: InteractionSource?,
        animationSpec: InfiniteRepeatableSpec<Float>
    ) {
        val interactionChanged = this.interactionSource != interactionSource || this.trigger != trigger
        this.durationMillis = durationMillis
        this.minOpacity = minOpacity
        this.maxOpacity = maxOpacity
        this.trigger = trigger
        this.interactionSource = interactionSource
        this.animationSpec = animationSpec

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
        if (active) {
            if (animationJob?.isActive != true) {
                animationJob = coroutineScope.launch {
                    progress.snapTo(0f)
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = animationSpec
                    ) {
                        invalidatePlacement()
                    }
                }
            }
        } else {
            animationJob?.cancel()
            animationJob = coroutineScope.launch {
                progress.animateTo(0f, tween(100)) {
                    invalidatePlacement()
                }
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0) {
                alpha = if (isActive()) {
                    minOpacity + progress.value * (maxOpacity - minOpacity)
                } else {
                    1f
                }
            }
        }
    }
}
