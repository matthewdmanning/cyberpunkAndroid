package com.example.cyberpunkandroid.effects

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import kotlin.math.cos
import kotlin.math.sin

// =================================================================================================
// Palette and drift clock
// =================================================================================================

/**
 * Three colors that a holographic brush blends between, like the bands of a hologram foil.
 *
 * @param color0 First band color.
 * @param color1 Second band color.
 * @param color2 Third band color.
 */
@Immutable
data class CyberHoloPalette(
    val color0: Color,
    val color1: Color,
    val color2: Color,
)

/**
 * `true` (default) lets holographic components use the iridescence runtime shader on API 33+. Provide `false`
 * for a subtree that is drawn into a software canvas, or to force the gradient look everywhere.
 */
val LocalCyberHoloUseShader = staticCompositionLocalOf { true }

/**
 * Returns the theme's holographic palette: primary (cyan), secondary (magenta), and the success role (green).
 * These are the three accents of the theme, so holographic surfaces stay inside its color system.
 */
@Composable
@ReadOnlyComposable
fun cyberHoloPalette(): CyberHoloPalette = CyberHoloPalette(
    color0 = CyberTheme.colors.primary,
    color1 = CyberTheme.colors.secondary,
    color2 = CyberTheme.status.success.color,
)

/**
 * Returns a slow, endless phase in `0f until 1f` that holographic effects use to drift their colors.
 *
 * The phase advances by [cyclesPerSecond] full cycles each second, frame-synced. A value of `0f` stops the
 * drift and holds the phase at `0f`, for static screens or reduced motion. Read the state only in a draw lambda,
 * so the drift redraws without recomposition.
 *
 * Share one phase between related effects on a screen, so their colors drift together.
 *
 * @param cyclesPerSecond Drift speed in palette cycles per second.
 * @return State holding the current phase.
 */
@Composable
fun rememberCyberDriftPhase(
    cyclesPerSecond: Float = CyberConfig.Effects.HoloDriftCyclesPerSecond,
): State<Float> {
    val phase = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(cyclesPerSecond) {
        if (cyclesPerSecond == 0f) {
            phase.floatValue = 0f
            return@LaunchedEffect
        }
        val start = withFrameNanos { it }
        val startPhase = phase.floatValue
        while (true) {
            withInfiniteAnimationFrameNanos { now ->
                // Nanoseconds to seconds, then cycles; keep only the fraction so the value never grows.
                val cycles = (now - start) / 1_000_000_000.0 * cyclesPerSecond
                phase.floatValue = ((startPhase + cycles) % 1.0).toFloat()
            }
        }
    }
    return phase
}

// =================================================================================================
// Holographic brush
// =================================================================================================

/**
 * Builds holographic brushes. On Android 13+ (API 33) it uses the iridescence runtime shader
 * ([CyberShaders.HoloIridescenceShader]); below that it uses a repeating linear gradient through the same
 * palette, which looks close but has harder band transitions.
 *
 * Create one per drawing element (for example inside `drawWithCache`) and call [brush] on each draw.
 *
 * Android cannot draw a runtime shader into a software canvas (for example `View.draw` into a `Bitmap`). Pass
 * `useShader = false` for content that is drawn that way; the gradient fallback works everywhere.
 *
 * @param useShader `true` to use the runtime shader where the API level supports it.
 */
class CyberHoloBrushSource(useShader: Boolean = true) {
    private val shader: RuntimeShader? =
        if (useShader && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) RuntimeShader(CyberShaders.HoloIridescenceShader) else null
    private val shaderBrush: Brush? = shader?.let { ShaderBrush(it) }

    /**
     * Returns a brush for an area of [size] at the given [phase].
     *
     * @param size Size of the area that the brush covers, in pixels.
     * @param phase Drift phase, `0f` to `1f` (see [rememberCyberDriftPhase]).
     * @param palette Band colors.
     * @param angleDegrees Band direction, clockwise from the +x axis.
     * @param bands Full palette cycles across the area.
     * @param alpha Opacity multiplier for every color, `0f` to `1f`.
     */
    fun brush(
        size: Size,
        phase: Float,
        palette: CyberHoloPalette,
        angleDegrees: Float = CyberConfig.Effects.HoloAngleDegrees,
        bands: Float = CyberConfig.Effects.HoloBands,
        alpha: Float = 1f,
    ): Brush {
        val radians = Math.toRadians(angleDegrees.toDouble())
        val dx = cos(radians).toFloat()
        val dy = sin(radians).toFloat()
        val c0 = palette.color0.copy(alpha = palette.color0.alpha * alpha)
        val c1 = palette.color1.copy(alpha = palette.color1.alpha * alpha)
        val c2 = palette.color2.copy(alpha = palette.color2.alpha * alpha)
        val s = shader
        if (s != null && shaderBrush != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            s.setFloatUniform("resolution", size.width.coerceAtLeast(1f), size.height.coerceAtLeast(1f))
            s.setFloatUniform("direction", dx, dy)
            s.setFloatUniform("bands", bands)
            s.setFloatUniform("phase", phase)
            s.setPremultipliedColor("color0", c0)
            s.setPremultipliedColor("color1", c1)
            s.setPremultipliedColor("color2", c2)
            return shaderBrush
        }
        // Fallback: one palette cycle spans the projection of the area onto the band direction, divided by bands.
        val span = (kotlin.math.abs(size.width * dx) + kotlin.math.abs(size.height * dy)).coerceAtLeast(1f) / bands
        val shift = -phase * span
        return Brush.linearGradient(
            colors = listOf(c0, c1, c2, c0),
            start = Offset(shift * dx, shift * dy),
            end = Offset((shift + span) * dx, (shift + span) * dy),
            tileMode = TileMode.Repeated
        )
    }
}

/**
 * Sets a `half4` uniform to [color] as premultiplied sRGB floats, which is what a shader returns.
 *
 * @param name Uniform name.
 * @param color Color to set.
 */
@androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun RuntimeShader.setPremultipliedColor(name: String, color: Color) {
    val c = color.convert(androidx.compose.ui.graphics.colorspace.ColorSpaces.Srgb)
    setFloatUniform(name, c.red * c.alpha, c.green * c.alpha, c.blue * c.alpha, c.alpha)
}

/**
 * Draws an outline of [shape] with a holographic brush whose colors drift with [phase].
 *
 * Visual: a thin stroke whose color flows from cyan to magenta to green (by default) along a shallow diagonal,
 * and slowly slides along the outline. It draws over the content, inside the bounds.
 *
 * @param shape Outline shape. Match the element's background shape.
 * @param phase Supplies the drift phase, read only in the draw phase.
 * @param palette Band colors. Use [cyberHoloPalette] for the theme palette.
 * @param width Stroke width.
 * @param angleDegrees Band direction, clockwise from the +x axis.
 * @param bands Full palette cycles across the element.
 * @param alpha Stroke opacity, `0f` to `1f`.
 * @param useShader `true` to use the runtime shader where supported; see [CyberHoloBrushSource].
 * @param appendedA11y Optional accessibility label. The border is decorative, so no label is applied unless one
 *   of the accessibility parameters is supplied.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
fun Modifier.cyberHoloBorder(
    shape: Shape,
    phase: () -> Float,
    palette: CyberHoloPalette,
    width: Dp = CyberPrimitives.BorderWidths.dp1,
    angleDegrees: Float = CyberConfig.Effects.HoloAngleDegrees,
    bands: Float = CyberConfig.Effects.HoloBands,
    alpha: Float = 1f,
    useShader: Boolean = true,
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .cyberComponentSemantics("CyberHoloBorder", appendedA11y, customA11y)
    .drawWithCache {
        val strokePx = width.toPx()
        // Inset by half the stroke so the whole stroke stays inside the bounds.
        val inset = strokePx / 2f
        val inner = Size((size.width - strokePx).coerceAtLeast(0f), (size.height - strokePx).coerceAtLeast(0f))
        val path = Path().apply { addOutline(shape.createOutline(inner, layoutDirection, this@drawWithCache)) }
        path.translate(Offset(inset, inset))
        val source = CyberHoloBrushSource(useShader)
        val stroke = Stroke(width = strokePx)
        onDrawWithContent {
            drawContent()
            drawPath(path, source.brush(size, phase(), palette, angleDegrees, bands, alpha), style = stroke)
        }
    }

// =================================================================================================
// Beam, ticks, materialize
// =================================================================================================

/**
 * Draws a projector beam behind the content: [color] at the top edge fading to transparent at the bottom.
 *
 * Visual: a soft vertical gradient, like light cast down from an emitter line. Pair it with a bright line on the
 * top edge to read as a hologram projected from that edge. Static.
 *
 * @param color Beam color.
 * @param alpha Opacity at the top edge, `0f` to `1f`.
 * @param appendedA11y Optional accessibility label. The beam is decorative.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
fun Modifier.cyberBeam(
    color: Color,
    alpha: Float = CyberConfig.Effects.BeamAlpha,
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .cyberComponentSemantics("CyberBeam", appendedA11y, customA11y)
    .drawBehind {
        drawRect(Brush.verticalGradient(listOf(color.copy(alpha = color.alpha * alpha), Color.Transparent)))
    }

/** Edge of an element that [cyberTickEdge] draws on. */
enum class CyberTickSide {
    /** The top edge, ticks pointing down. */
    Top,

    /** The bottom edge, ticks pointing up. */
    Bottom,
}

/**
 * Draws a ruler of short ticks along one edge, with a longer tick every [majorEvery] ticks.
 *
 * Visual: evenly spaced hairline ticks rising from (or hanging off) the edge, like the scale of an instrument.
 * It reads as "measured data" under a value. Static.
 *
 * @param color Tick color.
 * @param side Edge that carries the ticks.
 * @param spacing Distance between two ticks.
 * @param minorLength Length of a minor tick.
 * @param majorLength Length of a major tick.
 * @param majorEvery Ticks per major tick. Values below 1 are treated as 1.
 * @param strokeWidth Tick stroke width.
 * @param appendedA11y Optional accessibility label. The ruler is decorative.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
fun Modifier.cyberTickEdge(
    color: Color,
    side: CyberTickSide = CyberTickSide.Bottom,
    spacing: Dp = CyberPrimitives.Spacing.dp8,
    minorLength: Dp = CyberPrimitives.Spacing.dp4,
    majorLength: Dp = CyberPrimitives.Spacing.dp8,
    majorEvery: Int = 5,
    strokeWidth: Dp = CyberPrimitives.BorderWidths.dp1,
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .cyberComponentSemantics("CyberTickEdge", appendedA11y, customA11y)
    .drawBehind {
        val step = spacing.toPx().coerceAtLeast(1f)
        val stroke = strokeWidth.toPx()
        val every = majorEvery.coerceAtLeast(1)
        val edgeY = if (side == CyberTickSide.Bottom) size.height - stroke / 2f else stroke / 2f
        val sign = if (side == CyberTickSide.Bottom) -1f else 1f
        var x = stroke / 2f
        var i = 0
        while (x <= size.width) {
            val length = (if (i % every == 0) majorLength else minorLength).toPx()
            drawLine(color, Offset(x, edgeY), Offset(x, edgeY + sign * length), stroke)
            x += step
            i++
        }
    }

/**
 * Reveals the content from the start edge to the end edge, with a bright scan line at the reveal front, like a
 * hologram being projected line by line.
 *
 * Visual: content is clipped to the revealed width. A [width]-wide [color] line marks the front, with a short
 * gradient trail behind it. At progress `1f` the content draws normally and nothing else is drawn.
 *
 * Drive [progress] from `0f` to `1f` once (for example with an `Animatable` when the element appears).
 *
 * @param progress Supplies the reveal progress, `0f` (hidden) to `1f` (fully shown). Read in the draw phase.
 * @param color Scan line color.
 * @param width Scan line width.
 * @param trail Length of the gradient trail behind the scan line.
 * @param trailAlpha Trail opacity next to the scan line, `0f` to `1f`. The trail fades to transparent behind it.
 * @param appendedA11y Optional accessibility label. The reveal is decorative.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
fun Modifier.cyberMaterialize(
    progress: () -> Float,
    color: Color,
    width: Dp = CyberPrimitives.BorderWidths.dp2,
    trail: Dp = CyberPrimitives.Spacing.dp24,
    trailAlpha: Float = 0.5f, // Half the line's opacity, so the front stays the brightest part.
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .cyberComponentSemantics("CyberMaterialize", appendedA11y, customA11y)
    .drawWithContent {
        val p = progress().coerceIn(0f, 1f)
        if (p >= 1f) {
            drawContent()
            return@drawWithContent
        }
        val front = size.width * p
        clipRect(right = front) { this@drawWithContent.drawContent() }
        val trailPx = trail.toPx()
        drawRect(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, color.copy(alpha = color.alpha * trailAlpha)), front - trailPx, front),
            topLeft = Offset(front - trailPx, 0f),
            size = Size(trailPx, size.height)
        )
        drawLine(color, Offset(front, 0f), Offset(front, size.height), width.toPx())
    }

// =================================================================================================
// Echoes
// =================================================================================================

/**
 * Afterimage of a moving indicator: where it was, and how visible that afterimage still is.
 *
 * @see rememberCyberEcho
 */
@Stable
class CyberEchoState internal constructor(initial: Float) {
    /** Position that the indicator left, in the caller's units (for example an item index). */
    var position: Float by mutableFloatStateOf(initial)
        internal set

    internal val level = Animatable(0f)

    /** Afterimage opacity, from `1f` just after the move to `0f` when it has faded. */
    val alpha: Float get() = level.value
}

/**
 * Tracks an afterimage for an indicator at [target]. When [target] changes, the echo stays at the old position
 * and fades with [fadeSpec], so the eye sees where the selection came from.
 *
 * Read [CyberEchoState.alpha] and [CyberEchoState.position] only in draw or layer lambdas.
 *
 * @param target Current indicator position (for example the selected index).
 * @param fadeSpec Fade of the afterimage from `1f` to `0f`.
 * @return The echo state.
 */
@Composable
fun rememberCyberEcho(
    target: Float,
    fadeSpec: AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms500, easing = CyberConfig.Easings.OutExpoEasing),
): CyberEchoState {
    val echo = remember { CyberEchoState(target) }
    val last = remember { mutableFloatStateOf(target) }
    val spec by rememberUpdatedState(fadeSpec)
    LaunchedEffect(target) {
        val previous = last.floatValue
        last.floatValue = target
        if (previous == target) return@LaunchedEffect
        echo.position = previous
        echo.level.snapTo(1f)
        echo.level.animateTo(0f, spec)
    }
    return echo
}

/**
 * Text with two chromatic echoes (by default cyan and magenta) that split away from it when [pulse] is high and
 * converge back into it as [pulse] decays, like a display re-locking its signal after an update.
 *
 * Visual: at rest only the main text draws. When [pulse] rises, a copy in the first echo color shifts toward
 * the start edge and a copy in the second shifts toward the end edge by up to [offset], at up to [echoAlpha].
 * Echo copies are hidden from screen readers. The shift runs in the layer phase only.
 *
 * Drive [pulse] with [rememberCyberChangePulse] keyed on the text.
 *
 * @param text Text to show.
 * @param pulse Supplies the echo level, `0f` (merged) to `1f` (fully split).
 * @param modifier Modifier applied to the text stack.
 * @param style Text style.
 * @param color Main text color.
 * @param echoColors First and second echo colors.
 * @param offset Largest horizontal shift of each echo.
 * @param echoAlpha Echo opacity at full split.
 */
@Composable
fun CyberChromaEchoText(
    text: String,
    pulse: () -> Float,
    modifier: Modifier = Modifier,
    style: TextStyle = CyberTheme.typography.display,
    color: Color = CyberTheme.colors.textPrimary,
    echoColors: Pair<Color, Color> = CyberTheme.colors.primary to CyberTheme.colors.secondary,
    offset: Dp = CyberPrimitives.Spacing.dp4,
    echoAlpha: Float = CyberConfig.Effects.ChromaEchoAlpha,
) {
    Box(modifier = modifier) {
        listOf(echoColors.first to -1f, echoColors.second to 1f).forEach { (echoColor, direction) ->
            Text(
                text = text,
                style = style,
                color = echoColor,
                maxLines = 1,
                modifier = Modifier
                    .clearAndSetSemantics {}
                    .graphicsLayer {
                        val level = pulse().coerceIn(0f, 1f)
                        alpha = level * echoAlpha
                        translationX = direction * offset.toPx() * level
                    }
            )
        }
        Text(text = text, style = style, color = color, maxLines = 1)
    }
}
