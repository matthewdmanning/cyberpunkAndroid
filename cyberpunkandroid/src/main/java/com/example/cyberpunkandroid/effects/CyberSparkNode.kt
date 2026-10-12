package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import com.example.cyberpunkandroid.config.CyberSparkDefaults
import com.example.cyberpunkandroid.theme.LocalCyberColors
import com.example.cyberpunkandroid.utils.CyberSparkCooling
import com.example.cyberpunkandroid.utils.CyberSparkDrag
import com.example.cyberpunkandroid.utils.CyberSparkLaunch
import com.example.cyberpunkandroid.utils.CyberSparkSpray
import kotlin.math.min
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Element for [Modifier.cyberSpark]. It holds the inputs and makes a [CyberSparkNode].
 *
 * @property color Mid-life ember color for the theme scale. Unspecified gives theme `primary`.
 * @property secondaryColor Late-life ember color for the theme scale. Unspecified gives theme `secondary`.
 * @property warningColor Birth flash and end color for the theme scale. Unspecified gives `#FFB800`.
 * @property sparkCount Number of embers. Zero or less draws nothing.
 * @property intensity Brightness and size multiplier. Zero or less draws nothing.
 * @property speed Playback rate of the spark physics. 1 is the model time scale.
 * @property cooling Two-regime cooling model. It sets ember life, color and fade.
 * @property drag Air drag and gravity on each ember in flight.
 * @property launch Random launch radius, speed and direction of each ember.
 * @property colorScale (kelvin, color) stops for physical color.
 *   Null uses the theme scale. Any order is accepted.
 * @property trigger When the effect runs.
 * @property interactionSource Source for interaction triggers.
 * @property animationSpec Fade-in of [intensity] when the node attaches.
 */
data class CyberSparkElement(
    val color: Color,
    val secondaryColor: Color,
    val warningColor: Color,
    val sparkCount: Int,
    val intensity: Float,
    val speed: Float,
    val cooling: CyberSparkCooling,
    val drag: CyberSparkDrag,
    val launch: CyberSparkLaunch,
    val colorScale: List<Pair<Float, Color>>?,
    val trigger: CyberInteractionTrigger,
    val interactionSource: InteractionSource?,
    val animationSpec: AnimationSpec<Float>,
) : ModifierNodeElement<CyberSparkNode>() {

    /** Makes a new node with the current inputs. */
    override fun create() = CyberSparkNode(
        color, secondaryColor, warningColor, sparkCount, intensity, speed,
        cooling, drag, launch, colorScale, trigger, interactionSource, animationSpec,
    )

    /**
     * Copies the current inputs into a node that already exists.
     *
     * @param node The node to change. It redraws after the change.
     */
    override fun update(node: CyberSparkNode) {
        node.color = color
        node.secondaryColor = secondaryColor
        node.warningColor = warningColor
        node.sparkCount = sparkCount
        node.intensity = intensity
        node.speed = speed
        node.cooling = cooling
        node.drag = drag
        node.launch = launch
        node.colorScale = colorScale
        node.trigger = trigger
        node.interactionSource = interactionSource
        node.animationSpec = animationSpec
        node.invalidateDraw()
    }

    /** Gives the inputs to the layout inspector. */
    override fun InspectorInfo.inspectableProperties() {
        name = "cyberSpark"
        properties["color"] = color
        properties["secondaryColor"] = secondaryColor
        properties["warningColor"] = warningColor
        properties["sparkCount"] = sparkCount
        properties["intensity"] = intensity
        properties["speed"] = speed
        properties["cooling"] = cooling
        properties["drag"] = drag
        properties["launch"] = launch
        properties["colorScale"] = colorScale
        properties["trigger"] = trigger
        properties["interactionSource"] = interactionSource
        properties["animationSpec"] = animationSpec
    }
}

/**
 * Draws embers that fly with air drag, cool and fade.
 *
 * Each ember launches from the center in a symmetric upward cone.
 * Air drag slows it, and gravity pulls it down ([drag]).
 * Its temperature follows [cooling]. Temperature sets color, size and opacity.
 * The ember disappears at the Draper point, then launches again.
 * Each new launch has a new random path.
 *
 * See [CyberSparkElement] for the meaning of each property.
 */
class CyberSparkNode(
    var color: Color,
    var secondaryColor: Color,
    var warningColor: Color,
    var sparkCount: Int,
    var intensity: Float,
    var speed: Float,
    var cooling: CyberSparkCooling,
    var drag: CyberSparkDrag,
    var launch: CyberSparkLaunch,
    colorScale: List<Pair<Float, Color>>?,
    var trigger: CyberInteractionTrigger,
    var interactionSource: InteractionSource?,
    var animationSpec: AnimationSpec<Float>,
) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {

    /** Physical color stops, sorted by kelvin. Null uses the theme scale. */
    var colorScale: List<Pair<Float, Color>>? = colorScale?.sortedBy { it.first }
        set(value) {
            field = value?.sortedBy { it.first }
        }

    /** Animated intensity. It rises from zero to [intensity] on attach. */
    private val level = Animatable(0f)

    /** The simulated embers. Rebuilt when [sparkCount], [cooling], [drag] or [launch] change. */
    private var spray: CyberSparkSpray? = null

    /**
     * Gives the current spray. It makes a new one if an input changed.
     */
    private fun currentSpray(): CyberSparkSpray {
        val old = spray
        if (old != null && old.count == sparkCount.coerceAtLeast(0) &&
            old.cooling == cooling && old.drag == drag && old.launch == launch
        ) return old
        return CyberSparkSpray(sparkCount, cooling, drag, launch).also { spray = it }
    }

    /** Starts the fade-in and the frame clock that moves the embers. */
    override fun onAttach() {
        super.onAttach()
        coroutineScope.launch {
            level.animateTo(intensity, animationSpec)
        }
        coroutineScope.launch {
            var lastFrameNanos = 0L
            while (isActive) {
                withInfiniteAnimationFrameNanos { frameNanos ->
                    if (lastFrameNanos == 0L) lastFrameNanos = frameNanos
                    val frameSeconds = (frameNanos - lastFrameNanos).nanoseconds.toDouble(DurationUnit.SECONDS).toFloat()
                    currentSpray().advance(frameSeconds, speed)
                    lastFrameNanos = frameNanos
                }
                // The spray is not snapshot state, so ask for a redraw each frame.
                invalidateDraw()
            }
        }
    }

    /**
     * Gives the theme color for an ember.
     *
     * The stages are white to warning, primary to secondary, then secondary to dim warning.
     *
     * @param progress Cooling progress, 0 (hottest) to 1 (Draper point).
     * @param primary Resolved mid-life color.
     * @param secondary Resolved late-life color.
     * @param warning Resolved birth flash and end color.
     */
    private fun themeColor(progress: Float, primary: Color, secondary: Color, warning: Color): Color {
        val whiteEnd = CyberSparkDefaults.Look.ThemeStageWhiteEnd
        val primaryEnd = CyberSparkDefaults.Look.ThemeStagePrimaryEnd
        return when {
            progress < whiteEnd -> lerp(Color.White, warning, progress / whiteEnd)
            progress < primaryEnd -> lerp(primary, secondary, (progress - whiteEnd) / (primaryEnd - whiteEnd))
            else -> lerp(
                secondary,
                warning.copy(alpha = CyberSparkDefaults.Look.ThemeEndWarningAlpha),
                (progress - primaryEnd) / (1f - primaryEnd),
            )
        }
    }

    /** Draws the content, then each ember on top. */
    override fun ContentDrawScope.draw() {
        drawContent()

        val currentIntensity = level.value
        if (currentIntensity <= 0f || sparkCount <= 0) return

        val themeColors = currentValueOf(LocalCyberColors)
        val primary = color.takeOrElse { themeColors.primary }
        val secondary = secondaryColor.takeOrElse { themeColors.secondary }
        val warning = warningColor.takeOrElse { Color(CyberSparkDefaults.Look.FallbackWarningArgb) }
        val stops = colorScale

        val look = CyberSparkDefaults.Look
        val sparks = currentSpray()

        // The reference distance maps to one extent, a fixed fraction of the smaller side.
        val origin = Offset(size.width / 2f, size.height / 2f)
        val extent = min(size.width, size.height) * CyberSparkDefaults.Motion.ExtentFraction
        val pixelsPerMeter = extent / sparks.launch.referenceDistanceMeters

        for (i in 0 until sparks.count) {
            val ageSeconds = sparks.ageSeconds(i)
            val position = origin + Offset(sparks.xMeters(i), sparks.yMeters(i)) * pixelsPerMeter

            val kelvin = cooling.kelvinAt(ageSeconds)
            val progress = cooling.coolingProgress(kelvin)
            // Larger physical sparks draw larger.
            val baseRadius = (look.RadiusMinPx + sparks.radiusFraction(i) * look.RadiusRangePx) * currentIntensity
            val radius = baseRadius * (1f - look.RadiusShrink * progress)

            val hue = if (stops != null) colorAtKelvin(stops, kelvin) else themeColor(progress, primary, secondary, warning)
            val core = hue.copy(alpha = hue.alpha * cooling.emission(kelvin))

            drawCircle(core.copy(alpha = core.alpha * look.HaloAlpha), radius * look.HaloRadiusScale, position)
            drawCircle(core, radius, position)
        }
    }
}
