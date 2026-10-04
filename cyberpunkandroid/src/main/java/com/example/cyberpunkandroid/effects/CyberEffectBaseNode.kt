package com.example.cyberpunkandroid.effects

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.invalidatePlacement

internal abstract class CyberEffectBaseNode(
    private val shaderSource: String,
    protected var trigger: CyberInteractionTrigger,
    protected var interactionSource: InteractionSource?,
    protected var targetOpacity: Float,
    protected var animationSpec: AnimationSpec<Float>,
    protected var clipWhenIdle: Boolean = true
) : Modifier.Node(), DrawModifierNode, LayoutModifierNode {

    protected var shader: RuntimeShader? = null
    protected var clock = 0f
    protected val levelAnimatable = Animatable(0f)
    private var clockJob: Job? = null
    private var triggerJob: Job? = null

    protected fun updateBase(
        trigger: CyberInteractionTrigger,
        interactionSource: InteractionSource?,
        targetOpacity: Float,
        animationSpec: AnimationSpec<Float>,
        clipWhenIdle: Boolean
    ) {
        this.targetOpacity = targetOpacity
        this.clipWhenIdle = clipWhenIdle
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
            shader = RuntimeShader(shaderSource)
        }

        clockJob = coroutineScope.launch {
            while (true) {
                withFrameMillis { frameTime ->
                    clock = (frameTime % 100_000L) / 1000f
                    if (levelAnimatable.value > 0f) {
                        invalidatePlacement()
                        invalidateDraw()
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
                levelAnimatable.animateTo(targetOpacity, animationSpec)
            } else if (interactionSource != null) {
                // Simplified interaction listening for now
                levelAnimatable.animateTo(targetOpacity, animationSpec)
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
                clip = clipWhenIdle || applies
                applyLayerProperties(this, clock)

                if (applies && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val s = shader
                    if (s != null) {
                        s.setFloatUniform("resolution", size.width, size.height)
                        applyShaderUniforms(s, size, clock, current)
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
            drawFallback(current, clock)
        }
    }

    protected abstract fun applyShaderUniforms(shader: RuntimeShader, size: Size, clock: Float, current: Float)
    protected abstract fun ContentDrawScope.drawFallback(current: Float, clock: Float)
    protected open fun applyLayerProperties(scope: GraphicsLayerScope, clock: Float) {}
}
