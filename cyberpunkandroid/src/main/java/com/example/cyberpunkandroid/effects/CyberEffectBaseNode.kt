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

/**
 * Base [Modifier.Node] architecture for stateful, shader-backed cyberpunk effects.
 *
 * This class abstracts the boilerplate of managing Compose interaction boundaries, 
 * hardware-accelerated AGSL `RuntimeShader` execution (API 33+), and pre-API 33 canvas fallbacks.
 * By inheriting from this node, complex shader modifiers avoid the deprecated `Modifier.composed` 
 * anti-pattern, saving heavily on composition-phase allocations.
 *
 * ### Responsibilities
 * - **Animation Clock:** Automatically spawns a frame-synchronized `coroutineScope` to drive the shader `time` uniform.
 * - **State Management:** Manages an internal `Animatable` bound to the [trigger] state and [animationSpec].
 * - **Hardware Rendering:** Binds the compiled AGSL string to the [LayoutModifierNode]'s layer configuration natively via `RenderEffect`.
 *
 * @param shaderSource The raw AGSL shader string (e.g., from [CyberShaders]).
 * @param trigger Defines when the effect is active (e.g., [CyberInteractionTrigger.PRESSED]).
 * @param interactionSource The observable event stream for the user interaction.
 * @param targetOpacity The peak intensity/opacity the effect reaches when triggered.
 * @param animationSpec The easing curve to use when transitioning the intensity.
 * @param clipWhenIdle Whether the graphics layer should remain clipped to bounds when the effect is at 0 intensity.
 */
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

    /**
     * Updates the base node properties. Subclasses must call this inside their `update()` implementation
     * to ensure the underlying coroutine bindings react to recomposition of the modifier parameters.
     */
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

    /**
     * Injects uniform variables into the hardware AGSL shader instance (API 33+).
     * @param shader The compiled runtime shader.
     * @param size The physical dimensions of the layout node.
     * @param clock The continuously incrementing animation clock time.
     * @param current The interpolated intensity value of the effect.
     */
    protected abstract fun applyShaderUniforms(shader: RuntimeShader, size: Size, clock: Float, current: Float)
    
    /**
     * Defines the Canvas-drawing fallback for devices below API 33 that cannot execute AGSL code.
     * @param current The interpolated intensity value of the effect.
     * @param clock The continuously incrementing animation clock time.
     */
    protected abstract fun ContentDrawScope.drawFallback(current: Float, clock: Float)
    
    /**
     * Allows subclasses to directly manipulate standard `graphicsLayer` properties 
     * (e.g., [GraphicsLayerScope.rotationZ], [GraphicsLayerScope.translationX]) alongside the shader execution.
     * @param scope The layer scope context.
     * @param clock The animation clock time.
     */
    protected open fun applyLayerProperties(scope: GraphicsLayerScope, clock: Float) {}
}
