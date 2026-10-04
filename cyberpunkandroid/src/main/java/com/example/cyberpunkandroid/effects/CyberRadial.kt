package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.example.cyberpunkandroid.config.CyberRadialDefaults
import kotlin.math.hypot
import kotlin.math.max

// Radial effects share one model: a REGION (where the effect lives: origin, radii, clip) and a driver
// (the hoisted state that owns timing: a CyberRadarSweep or a CyberRadialPulse). Modifiers draw from a
// driver; CyberRadialField lets other components, such as icons, ask the same driver how bright a point is.

/**
 * Resolves the center of a radial effect for a component of a given size.
 *
 * Built-ins are [Center] and [fraction]. For anything else, supply a lambda. A lambda may read Compose
 * `State` (for example a touch position): the effect redraws when it changes.
 *
 * ```
 * var touch by remember { mutableStateOf(Offset.Unspecified) }
 * val origin = CyberRadialOrigin { size -> if (touch.isSpecified) touch else Offset(size.width / 2, size.height / 2) }
 * ```
 */
@Stable
fun interface CyberRadialOrigin {
    /**
     * The origin, in pixels from the component's top-left corner.
     *
     * @param size Size of the component the effect is drawn on, in pixels.
     */
    fun resolve(size: Size): Offset

    companion object {
        /** The center of the component. */
        val Center = CyberRadialOrigin { size -> Offset(size.width / 2f, size.height / 2f) }

        /**
         * A point given as a fraction of the component's width and height: `fraction(0f, 0f)` is the top-left
         * corner, `fraction(1f, 0.5f)` the middle of the right edge.
         *
         * @param x Horizontal position, 0 (left) to 1 (right).
         * @param y Vertical position, 0 (top) to 1 (bottom).
         */
        fun fraction(x: Float, y: Float) = CyberRadialOrigin { size -> Offset(size.width * x, size.height * y) }
    }
}

/**
 * Where a radial effect lives on a component: its origin, the ring of radii it covers and what clips it.
 * Angular limits belong to the driver ([rememberCyberRadarSweep] and [rememberCyberRadialPulse]) because they
 * also shape the motion.
 *
 * @param origin Center of the effect. Defaults to the component center.
 * @param innerRadius Radius inside which the effect is not drawn. Use it to keep a sweep off the hub of a dial.
 * @param outerRadius Radius at which the effect ends. `Dp.Unspecified` reaches the farthest corner, so a card has no dark gaps.
 * @param clipShape What the effect is clipped to. The default clips to the component bounds so a radius that
 *   reaches the corners never draws outside it; pass the component's own shape to follow its outline, or `null` for no clip.
 */
@Immutable
data class CyberRadialRegion(
    val origin: CyberRadialOrigin = CyberRadialOrigin.Center,
    val innerRadius: Dp = 0.dp,
    val outerRadius: Dp = Dp.Unspecified,
    val clipShape: Shape? = RectangleShape,
)

/**
 * A [CyberRadialRegion] resolved against one component size, in pixels.
 *
 * @param origin Center of the effect, from the component's top-left corner.
 * @param innerPx Radius inside which nothing is drawn.
 * @param outerPx Radius at which the effect ends.
 */
internal data class CyberResolvedRegion(val origin: Offset, val innerPx: Float, val outerPx: Float)

/**
 * Resolves this region for a component of [size].
 *
 * @param size Component size in pixels.
 * @param density Converts the region's `Dp` radii to pixels.
 */
internal fun CyberRadialRegion.resolve(size: Size, density: Density): CyberResolvedRegion {
    val center = origin.resolve(size)
    val outer = if (outerRadius.isSpecified) with(density) { outerRadius.toPx() }
    else CyberRadialMath.farthestCorner(center.x, center.y, size.width, size.height)
    val inner = with(density) { innerRadius.toPx() }.coerceIn(0f, outer)
    return CyberResolvedRegion(center, inner, outer)
}

/**
 * Where a drawn radial effect sits in the window, published by its modifier so other components can place
 * themselves relative to it.
 *
 * @param rootOffset Top-left corner of the component the effect is drawn on, in root (window) coordinates, in pixels.
 * @param size Size of that component, in pixels.
 * @param region The region the effect was drawn with.
 * @param density Converts `Dp` settings to pixels.
 */
internal data class CyberRadialPlacement(
    val rootOffset: Offset,
    val size: Size,
    val region: CyberRadialRegion,
    val density: Density,
)

/**
 * Where a drawn radial effect sits in the window, with every value resolved to pixels.
 *
 * @param originInRoot Effect center in root (window) coordinates.
 * @param innerPx Radius inside which the effect is not drawn.
 * @param outerPx Radius at which the effect ends.
 * @param pxPerDp Pixels per dp, so a driver can convert its `Dp` settings.
 */
internal data class CyberRadialGeometry(val originInRoot: Offset, val innerPx: Float, val outerPx: Float, val pxPerDp: Float)

/** Resolves this placement to pixels. Reads the region's origin, so a state-driven origin is observed by the caller. */
internal fun CyberRadialPlacement.geometry(): CyberRadialGeometry {
    val resolved = region.resolve(size, density)
    return CyberRadialGeometry(rootOffset + resolved.origin, resolved.innerPx, resolved.outerPx, density.density)
}

/**
 * A window point seen from the effect's origin.
 *
 * @param radius Distance from the origin, in pixels.
 * @param offset Angle in degrees clockwise from the sector start, 0 until 360.
 */
internal class CyberRadialSample(val radius: Float, val offset: Float)

/**
 * Where [point] lies relative to this effect, or null when it is outside the ring of radii or the sector.
 *
 * @param point A point in root (window) coordinates, in pixels.
 * @param sector Angular slice the effect covers.
 */
internal fun CyberRadialGeometry.sample(point: Offset, sector: CyberRadialSector): CyberRadialSample? {
    val dx = point.x - originInRoot.x
    val dy = point.y - originInRoot.y
    val radius = hypot(dx, dy)
    if (radius < innerPx || radius > outerPx) return null
    val offset = (CyberRadialMath.angleFromTop(dx, dy) - sector.startDegrees).mod(CyberRadialMath.FULL_TURN)
    if (!sector.isFullCircle && offset > sector.span) return null
    return CyberRadialSample(radius, offset)
}

/**
 * A brightness map over the window. Radial drivers implement it so any component can be lit by the same
 * effect that is drawn behind it; implement it yourself to light things from your own effect.
 */
@Stable
fun interface CyberRadialField {
    /**
     * How strongly the effect lights the point right now.
     *
     * @param pointInRoot A point in root (window) coordinates, in pixels.
     * @return Brightness from 0 (unlit) to 1 (at the head of the beam or ring).
     */
    fun intensityAt(pointInRoot: Offset): Float
}

/** Default sweep loop: constant speed, one revolution (or one bounce) per `CyberRadialDefaults.Sweep.LoopMillis`. */
private val DefaultSweepLoop: AnimationSpec<Float> =
    infiniteRepeatable(tween(CyberRadialDefaults.Sweep.LoopMillis, easing = LinearEasing), RepeatMode.Restart)

/** Default pulse loop: constant speed, one pulse per `CyberRadialDefaults.Pulse.LoopMillis`. */
private val DefaultPulseLoop: AnimationSpec<Float> =
    infiniteRepeatable(tween(CyberRadialDefaults.Pulse.LoopMillis, easing = LinearEasing), RepeatMode.Restart)

/**
 * The state of a radar sweep: its sector, motion and fade, plus the animation clock. Create it with
 * [rememberCyberRadarSweep], draw it with [cyberRadarSweep] and light icons from it with
 * [cyberRadialIllumination] (it is a [CyberRadialField]).
 *
 * @param sector Angular slice the beam travels in.
 * @param mode What the beam does at the end of the sector.
 * @param clockwise Whether the beam travels clockwise.
 * @param tailDegrees Length of the trail behind the head, in degrees. Zero leaves just the head line.
 * @param fadeSteps Opacity bands in the trail; 0 is a smooth fade, more is a visibly discrete fade.
 * @param edgeFadeDegrees Degrees at each end of a partial sector over which the beam fades in and out; 0 is hard edges.
 * @param hideWhenIdle Draw and light nothing while the trigger is inactive.
 * @param progressState Position in the loop, 0..1.
 * @param activeState Whether the trigger is currently active.
 */
@Stable
class CyberRadarSweep internal constructor(
    val sector: CyberRadialSector,
    val mode: CyberSweepMode,
    val clockwise: Boolean,
    val tailDegrees: Float,
    val fadeSteps: Int,
    val edgeFadeDegrees: Float,
    val hideWhenIdle: Boolean,
    private val progressState: State<Float>,
    private val activeState: State<Boolean>,
) : CyberRadialField {

    /** Where the sweep was last drawn, published by its modifier. Null until the first layout. Observable state, so lit icons redraw when it appears. */
    internal var placement: CyberRadialPlacement? by mutableStateOf(null)

    /** Whether the trigger is active right now. */
    val isActive: Boolean get() = activeState.value

    /** Position in the loop, 0..1. Reading it in a draw block redraws every frame. */
    val progress: Float get() = progressState.value

    /** Current head position (see [SweepPose]). Reads the animation clock. */
    internal fun pose(): SweepPose =
        CyberRadialMath.sweepPose(progress, sector.span, tailDegrees, mode, clockwise, edgeFadeDegrees > 0f)

    /** Where the head is now, in degrees clockwise from 12 o'clock (0..360). Use it to drive your own indicators. */
    val headDegrees: Float get() = (sector.startDegrees + pose().offset).mod(CyberRadialMath.FULL_TURN)

    override fun intensityAt(pointInRoot: Offset): Float {
        val g = placement?.geometry() ?: return 0f
        if (hideWhenIdle && !isActive) return 0f
        val hit = g.sample(pointInRoot, sector) ?: return 0f
        val pose = pose()
        val distance = CyberRadialMath.trailDistance(hit.offset, pose, sector.span)
        val trail = max(tailDegrees * pose.tailScale, CyberRadialDefaults.Sweep.MinLitDegrees)
        return CyberRadialMath.trailAlpha(distance, trail, fadeSteps) *
            CyberRadialMath.edgeFactor(pose.offset, sector.span, edgeFadeDegrees)
    }
}

/**
 * Creates and remembers the state of a radar sweep.
 *
 * Timing lives here (not on the modifier) so the beam and anything lit by it (icons) share one clock.
 *
 * ```
 * val sweep = rememberCyberRadarSweep(sector = CyberRadialSector(300f, 60f), mode = CyberSweepMode.BOUNCE)
 * Box(Modifier.size(200.dp).cyberRadarSweep(sweep))
 * ```
 *
 * @param sector Angular slice the beam travels in, in degrees clockwise from 12 o'clock. Defaults to the full circle.
 * @param mode What the beam does at the end of the sector.
 * @param clockwise Whether the beam travels clockwise.
 * @param tailDegrees Length of the trail behind the head, in degrees. Zero draws just the head line.
 * @param fadeSteps Opacity bands in the trail; 0 is a smooth fade, 3 to 8 gives a visibly discrete fade.
 * @param edgeFadeDegrees Degrees at each end of a partial sector over which the beam fades in and out; 0 is hard edges.
 *   This fades the whole beam in time as its head nears an end; the sector's own edge stays a hard cut.
 * @param trigger When the sweep runs; while inactive the beam rests at the start (or is hidden, see [hideWhenIdle]).
 * @param interactionSource Source for HOVER / PRESS / FOCUS triggers.
 * @param hideWhenIdle Draw and light nothing while the trigger is inactive.
 * @param animationSpec Drives progress 0 to 1. Infinite specs loop; a finite spec plays once and holds.
 */
@Composable
fun rememberCyberRadarSweep(
    sector: CyberRadialSector = CyberRadialSector.FullCircle,
    mode: CyberSweepMode = CyberSweepMode.WRAP,
    clockwise: Boolean = true,
    tailDegrees: Float = CyberRadialDefaults.Sweep.TrailDegrees,
    fadeSteps: Int = 0,
    edgeFadeDegrees: Float = 0f,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    hideWhenIdle: Boolean = false,
    animationSpec: AnimationSpec<Float> = DefaultSweepLoop,
): CyberRadarSweep {
    val active = rememberUpdatedState(trigger.isActive(interactionSource))
    val progress = rememberPathProgress(active.value, animationSpec)
    return remember(sector, mode, clockwise, tailDegrees, fadeSteps, edgeFadeDegrees, hideWhenIdle, progress, active) {
        CyberRadarSweep(sector, mode, clockwise, tailDegrees, fadeSteps, edgeFadeDegrees, hideWhenIdle, progress, active)
    }
}

/**
 * The state of a radial pulse: rings or a disc travelling between an inner and an outer radius. Create it with
 * [rememberCyberRadialPulse], draw it with [cyberRadialPulse] and light icons from it with
 * [cyberRadialIllumination] (it is a [CyberRadialField]).
 *
 * @param style Ring, disc wipe or sonar.
 * @param direction Outward from the inner radius, or inward from the outer radius.
 * @param sector Angular slice the pulse covers; a partial sector makes it a fan.
 * @param trail Length of the fading trail behind the ring head (the soft rim of a disc).
 * @param fadeSteps Opacity bands in the trail; 0 is a smooth fade, more is a visibly discrete fade.
 * @param ringCount Rings in flight at once for [CyberPulseStyle.SONAR]; other styles use one.
 * @param fadeStart Fraction of a ring's life after which it fades out.
 * @param discFill Opacity of the filled area behind the rim of a [CyberPulseStyle.DISC].
 * @param hideWhenIdle Draw and light nothing while the trigger is inactive.
 * @param progressState Position in the loop, 0..1.
 * @param activeState Whether the trigger is currently active.
 */
@Stable
class CyberRadialPulse internal constructor(
    val style: CyberPulseStyle,
    val direction: CyberRadialDirection,
    val sector: CyberRadialSector,
    val trail: Dp,
    val fadeSteps: Int,
    val ringCount: Int,
    val fadeStart: Float,
    val discFill: Float,
    val hideWhenIdle: Boolean,
    private val progressState: State<Float>,
    private val activeState: State<Boolean>,
) : CyberRadialField {

    /** Where the pulse was last drawn, published by its modifier. Null until the first layout. Observable state, so lit icons redraw when it appears. */
    internal var placement: CyberRadialPlacement? by mutableStateOf(null)

    /** Whether the trigger is active right now. */
    val isActive: Boolean get() = activeState.value

    /** Position in the loop, 0..1. Reading it in a draw block redraws every frame. */
    val progress: Float get() = progressState.value

    /** Rings in flight: [ringCount] for sonar, otherwise one. */
    internal val rings: Int get() = if (style == CyberPulseStyle.SONAR) ringCount.coerceAtLeast(1) else 1

    /** Opacity left behind the trail: the disc fill for a disc wipe, none for rings. */
    internal val fill: Float get() = if (style == CyberPulseStyle.DISC) discFill else 0f

    /**
     * How far, 0..1, ring [index] has travelled through its life at the current loop position.
     *
     * @param index Ring number, 0 until [rings].
     */
    internal fun lifeProgress(index: Int): Float =
        if (rings > 1) CyberRadialMath.ringProgress(progress, index, rings) else progress.coerceIn(0f, 1f)

    override fun intensityAt(pointInRoot: Offset): Float {
        val g = placement?.geometry() ?: return 0f
        if (hideWhenIdle && !isActive) return 0f
        val hit = g.sample(pointInRoot, sector) ?: return 0f
        val trailPx = trail.value * g.pxPerDp
        var strongest = 0f
        for (ring in 0 until rings) {
            val life = lifeProgress(ring)
            val headRadius = g.innerPx + CyberRadialMath.pulseHead(life, direction) * (g.outerPx - g.innerPx)
            val distance = if (direction == CyberRadialDirection.OUTWARD) headRadius - hit.radius else hit.radius - headRadius
            val level = CyberRadialMath.trailAlpha(distance, trailPx, fadeSteps, fill) * CyberRadialMath.pulseFade(life, fadeStart)
            if (level > strongest) strongest = level
        }
        return strongest
    }
}

/**
 * Creates and remembers the state of a radial pulse.
 *
 * ```
 * val pulse = rememberCyberRadialPulse(style = CyberPulseStyle.SONAR)
 * Box(Modifier.size(200.dp).cyberRadialPulse(pulse))
 * ```
 *
 * For tap feedback pass a short finite `animationSpec`, for example `tween(240)`, and a `PRESS` trigger: functional
 * feedback should finish within 250 ms (see `docs/agents/api-conventions.md`).
 *
 * @param style Ring, disc wipe or sonar.
 * @param direction Outward from the inner radius, or inward from the outer radius.
 * @param sector Angular slice the pulse covers, in degrees clockwise from 12 o'clock; a partial sector makes it a fan.
 * @param trail Length of the fading trail behind the ring head (the soft rim of a disc).
 * @param fadeSteps Opacity bands in the trail; 0 is a smooth fade, 3 to 8 gives a visibly discrete fade.
 * @param ringCount Rings in flight at once for [CyberPulseStyle.SONAR].
 * @param fadeStart Fraction of a ring's life, 0..1, after which it fades out; 1 disables the fade.
 * @param discFill Opacity of the filled area behind the rim of a [CyberPulseStyle.DISC].
 * @param trigger When the pulse runs; while inactive it rests at progress 0 (or is hidden, see [hideWhenIdle]).
 * @param interactionSource Source for HOVER / PRESS / FOCUS triggers.
 * @param hideWhenIdle Draw and light nothing while the trigger is inactive.
 * @param animationSpec Drives progress 0 to 1. Infinite specs loop; a finite spec plays once and holds.
 */
@Composable
fun rememberCyberRadialPulse(
    style: CyberPulseStyle = CyberPulseStyle.RING,
    direction: CyberRadialDirection = CyberRadialDirection.OUTWARD,
    sector: CyberRadialSector = CyberRadialSector.FullCircle,
    trail: Dp = CyberRadialDefaults.Pulse.Trail,
    fadeSteps: Int = 0,
    ringCount: Int = CyberRadialDefaults.Pulse.SonarRings,
    fadeStart: Float = CyberRadialDefaults.Pulse.FadeStart,
    discFill: Float = CyberRadialDefaults.Pulse.DiscFill,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    hideWhenIdle: Boolean = false,
    animationSpec: AnimationSpec<Float> = DefaultPulseLoop,
): CyberRadialPulse {
    val active = rememberUpdatedState(trigger.isActive(interactionSource))
    val progress = rememberPathProgress(active.value, animationSpec)
    return remember(style, direction, sector, trail, fadeSteps, ringCount, fadeStart, discFill, hideWhenIdle, progress, active) {
        CyberRadialPulse(style, direction, sector, trail, fadeSteps, ringCount, fadeStart, discFill, hideWhenIdle, progress, active)
    }
}
