package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.ui.unit.dp

class CyberSparkElement(
    val color: Color,
    val secondaryColor: Color,
    val warningColor: Color,
    val sparkCount: Int,
    val intensity: Float,
    val speed: Float,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: AnimationSpec<Float>
) : ModifierNodeElement<CyberSparkNode>() {
    override fun create() = CyberSparkNode(
        color, secondaryColor, warningColor, sparkCount, intensity, speed, trigger, interactionSource, animationSpec
    )

    override fun update(node: CyberSparkNode) {
        val oldIntensity = node.intensity
        node.color = color
        node.secondaryColor = secondaryColor
        node.warningColor = warningColor
        node.sparkCount = sparkCount
        node.intensity = intensity
        node.speed = speed
        node.trigger = trigger
        node.interactionSource = interactionSource
        node.animationSpec = animationSpec
        if (oldIntensity != intensity) {
            node.updateIntensity(intensity, animationSpec)
        }
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberSpark"
        properties["color"] = color
        properties["secondaryColor"] = secondaryColor
        properties["warningColor"] = warningColor
        properties["sparkCount"] = sparkCount
        properties["intensity"] = intensity
        properties["speed"] = speed
        properties["trigger"] = trigger
        properties["interactionSource"] = interactionSource
        properties["animationSpec"] = animationSpec
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CyberSparkElement) return false
        return color == other.color &&
               secondaryColor == other.secondaryColor &&
               warningColor == other.warningColor &&
               sparkCount == other.sparkCount &&
               intensity == other.intensity &&
               speed == other.speed &&
               trigger == other.trigger &&
               interactionSource == other.interactionSource &&
               animationSpec == other.animationSpec
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + secondaryColor.hashCode()
        result = 31 * result + warningColor.hashCode()
        result = 31 * result + sparkCount
        result = 31 * result + intensity.hashCode()
        result = 31 * result + speed.hashCode()
        result = 31 * result + trigger.hashCode()
        result = 31 * result + (interactionSource?.hashCode() ?: 0)
        result = 31 * result + animationSpec.hashCode()
        return result
    }
}

class CyberSparkNode(
    var color: Color,
    var secondaryColor: Color,
    var warningColor: Color,
    var sparkCount: Int,
    var intensity: Float,
    var speed: Float,
    var trigger: CyberInteractionTrigger,
    var interactionSource: InteractionSource?,
    var animationSpec: AnimationSpec<Float>
) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {

    private val level = Animatable(0f)
    private var time = 0f

    fun updateIntensity(newIntensity: Float, spec: AnimationSpec<Float>) {
        coroutineScope.launch {
            level.animateTo(newIntensity, spec)
        }
    }

    override fun onAttach() {
        super.onAttach()
        coroutineScope.launch {
            level.animateTo(intensity, animationSpec)
        }
        coroutineScope.launch {
            var lastTime = 0L
            while (isActive) {
                withInfiniteAnimationFrameNanos { frameTime ->
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
        
        val primary = if (color == Color.Unspecified) currentValueOf(com.example.cyberpunkandroid.theme.LocalCyberColors).primary else color
        val secondary = if (secondaryColor == Color.Unspecified) currentValueOf(com.example.cyberpunkandroid.theme.LocalCyberColors).secondary else secondaryColor
        val warning = if (warningColor == Color.Unspecified) Color(0xFFFFB800) else warningColor

        val currentIntensity = level.value
        if (currentIntensity <= 0f || sparkCount <= 0) return

        val center = Offset(size.width / 2f, size.height / 2f)
        val maxExtent = maxOf(size.width, size.height) * 0.75f

        fun hash(n: Float): Float = (kotlin.math.sin(n * 127.1f) * 43758.545f).let { it - kotlin.math.floor(it) }

        for (i in 0 until sparkCount) {
            val sparkSpeedMultiplier = 0.8f + hash(i * 3.1f) * 1.2f
            val sparkTimeOffset = hash(i * 7.7f)

            val cycleFloat = time * speed * sparkSpeedMultiplier + sparkTimeOffset
            val epoch = kotlin.math.floor(cycleFloat)
            val t = cycleFloat - epoch

            val seed = (i * 10000 + epoch.toInt()).toLong()
            val random = kotlin.random.Random(seed)

            val easedT = t * t * (3f - 2f * t)

            val angle = -2.356f + random.nextFloat() * 1.5708f
            val spd = (0.4f + random.nextFloat() * 0.6f) * maxExtent

            val vx = kotlin.math.cos(angle) * spd
            val vy = kotlin.math.sin(angle) * spd
            val gravity = 1.0f * maxExtent

            val sparkPos = Offset(
                center.x + vx * easedT,
                center.y + vy * easedT + 0.5f * gravity * easedT * easedT
            )

            // Scaled ~10x from original 3f..6f to 25f..55f for bold high-impact particle rendering
            val baseRadius = (25f + random.nextFloat() * 30f) * currentIntensity
            val currentRadius = baseRadius * (1.0f - 0.4f * t)

            val plasmaColor = when {
                t < 0.25f -> {
                    val localT = t / 0.25f
                    androidx.compose.ui.graphics.lerp(Color.White, warning, localT)
                }
                t < 0.60f -> {
                    val localT = (t - 0.25f) / 0.35f
                    androidx.compose.ui.graphics.lerp(primary, secondary, localT)
                }
                else -> {
                    val localT = (t - 0.60f) / 0.40f
                    androidx.compose.ui.graphics.lerp(secondary, warning.copy(alpha = 0.5f), localT)
                }
            }

            val alphaDecay = (1.0f - t)
            val finalColor = plasmaColor.copy(alpha = plasmaColor.alpha * alphaDecay)

            // Outer plasma halo
            drawCircle(
                color = finalColor.copy(alpha = finalColor.alpha * 0.35f),
                radius = currentRadius * 2.2f,
                center = sparkPos
            )
            // Main incandescent particle body
            drawCircle(
                color = finalColor,
                radius = currentRadius,
                center = sparkPos
            )
            // Hot white core kernel
            drawCircle(
                color = Color.White.copy(alpha = finalColor.alpha * 0.85f),
                radius = currentRadius * 0.45f,
                center = sparkPos
            )
        }
    }
}
