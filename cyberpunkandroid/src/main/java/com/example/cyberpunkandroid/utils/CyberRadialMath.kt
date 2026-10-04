package com.example.cyberpunkandroid.utils

import com.example.cyberpunkandroid.utils.CyberPathGeometry.smoothstep
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// Pure geometry and timing for the radial effects. Nothing here touches Compose drawing or Android,
// so every function can be unit-tested on the JVM. Angles are in degrees, measured clockwise from
// 12 o'clock unless a name says otherwise ("compose" angles start at 3 o'clock).

/** What a radar sweep does when its head reaches the end of its sector. */
enum class CyberSweepMode {
    /**
     * Runs round and starts again. On a full circle the loop is seamless. On a partial sector the beam
     * leaves at the end (its trail runs off the edge) and re-enters at the start.
     */
    WRAP,

    /**
     * Runs to the end and back, easing at both ends like a KITT scanner. The trail shortens as the beam
     * slows at each turn and regrows on the other side, so it never pops from one side to the other.
     */
    BOUNCE,

    /**
     * Runs once from the start to the end and holds there. Use a finite animation spec so it plays once;
     * add an edge fade to make the beam fade out at the end instead of freezing.
     */
    HOLD,
}

/** Which way a radial pulse travels between its inner and outer radius. */
enum class CyberRadialDirection {
    /** From the inner radius (the center by default) out to the outer radius (the circumference). */
    OUTWARD,

    /** From the outer radius in to the inner radius. */
    INWARD,
}

/** The look of a radial pulse. */
enum class CyberPulseStyle {
    /** One bright ring with a fading trail behind it. */
    RING,

    /** A filled disc that grows (or closes like an iris when inward) with a bright rim and soft trail. */
    DISC,

    /** Several staggered rings in flight at once, like repeating sonar pings. */
    SONAR,
}

/**
 * An angular slice of a circle, in degrees clockwise from 12 o'clock.
 *
 * - `(0, 360)` is the full circle (the default).
 * - `(0, 90)` is the top-right quarter.
 * - `(300, 60)` wraps over 12 o'clock and is a 120-degree wedge centered on the top.
 * - An end equal to the start also means the full circle.
 *
 * @param startDegrees Where the sector begins, clockwise from 12 o'clock.
 * @param endDegrees Where the sector ends, clockwise from 12 o'clock.
 */
data class CyberRadialSector(
    val startDegrees: Float = 0f,
    val endDegrees: Float = CyberRadialMath.FULL_TURN,
) {
    /** Size of the sector in degrees, greater than 0 and at most 360. */
    val span: Float get() = CyberRadialMath.sectorSpan(startDegrees, endDegrees)

    /** Whether the sector covers the whole circle. */
    val isFullCircle: Boolean get() = span >= CyberRadialMath.FULL_TURN

    companion object {
        /** The whole circle. */
        val FullCircle = CyberRadialSector(0f, CyberRadialMath.FULL_TURN)
    }
}

/**
 * Position of a sweep head at one moment.
 *
 * @param offset Degrees clockwise from the sector start to the head. It can lie outside `0..span` while a
 *   wrapping beam enters or leaves a partial sector.
 * @param movingClockwise Whether the head is currently moving clockwise; the trail is on the opposite side.
 * @param tailScale Multiplier, 0..1, applied to the trail length. Below 1 only while a bouncing beam is slowing at a turn.
 */
data class SweepPose(val offset: Float, val movingClockwise: Boolean, val tailScale: Float)

/**
 * One gradient stop of an opacity profile.
 *
 * @param position Position along the gradient, 0..1.
 * @param alpha Opacity at that position, 0..1. Two stops at the same position make a hard edge.
 */
data class AlphaStop(val position: Float, val alpha: Float)

/**
 * The opacity stops of a sweep wedge and where its head sits on them.
 *
 * @param headFraction Position (0..1) on the sweep gradient where the head is. The gradient is rotated so this
 *   position lands on the head's real angle.
 * @param stops Stops covering the gradient from position 0 to 1.
 */
data class SweepStops(val headFraction: Float, val stops: List<AlphaStop>)

/** Pure math shared by the sweep, the pulse and icon illumination. */
internal object CyberRadialMath {

    /** Degrees in one full turn. */
    const val FULL_TURN: Float = 360f

    /** Compose measures angles from 3 o'clock; this converts a 12 o'clock angle to Compose's frame. */
    const val TOP_OFFSET: Float = -90f

    /** Degrees in half a turn, used to convert between degrees and radians. */
    private const val HALF_TURN: Double = 180.0

    /** Peak of `6x(1-x)`, the speed of a smoothstep ease, is 1.5; this is its inverse so speed is normalised to 0..1 as `4x(1-x)`. */
    private const val EASE_SPEED_NORMALISER: Float = 4f

    /** Midpoint of a bounce cycle: before it the beam moves out, after it back. */
    private const val BOUNCE_TURN: Float = 0.5f

    /**
     * Converts an angle measured clockwise from 12 o'clock to Compose's angle (clockwise from 3 o'clock).
     * @param degreesFromTop TODO: document this
     */
    fun toComposeDegrees(degreesFromTop: Float): Float = degreesFromTop + TOP_OFFSET

    /**
     * Size of the sector `start..end` in degrees, 0 < span <= 360. An end at or before the start wraps
     * round the circle, so `(300, 60)` is 120 and `(0, 0)` is the full 360. Non-finite input gives the full circle.
      * @param start TODO: document this
      * @param end TODO: document this
     */
    fun sectorSpan(start: Float, end: Float): Float {
        var span = end - start
        if (!span.isFinite()) return FULL_TURN
        while (span <= 0f) span += FULL_TURN
        return min(span, FULL_TURN)
    }

    /**
     * X of the point [radius] away from [cx] at [degreesFromTop] (clockwise from 12 o'clock).
     * @param cx TODO: document this
     * @param radius TODO: document this
     * @param degreesFromTop TODO: document this
     */
    fun polarX(cx: Float, radius: Float, degreesFromTop: Float): Float =
        cx + radius * sin(degreesFromTop * PI / HALF_TURN).toFloat()

    /**
     * Y of the point [radius] away from [cy] at [degreesFromTop] (clockwise from 12 o'clock; screen Y grows downward).
     * @param cy TODO: document this
     * @param radius TODO: document this
     * @param degreesFromTop TODO: document this
     */
    fun polarY(cy: Float, radius: Float, degreesFromTop: Float): Float =
        cy - radius * cos(degreesFromTop * PI / HALF_TURN).toFloat()

    /**
     * Angle in 0..360, clockwise from 12 o'clock, of the vector ([dx], [dy]) in screen coordinates
     * (x grows right, y grows down).
      * @param dx TODO: document this
      * @param dy TODO: document this
     */
    fun angleFromTop(dx: Float, dy: Float): Float =
        (atan2(dx.toDouble(), -dy.toDouble()) * HALF_TURN / PI).toFloat().mod(FULL_TURN)

    /**
     * Distance from ([ox], [oy]) to the farthest corner of a [width] by [height] rectangle: the radius that reaches every pixel.
     * @param ox TODO: document this
     * @param oy TODO: document this
     * @param width TODO: document this
     * @param height TODO: document this
     */
    fun farthestCorner(ox: Float, oy: Float, width: Float, height: Float): Float =
        max(max(hypot(ox, oy), hypot(width - ox, oy)), max(hypot(ox, height - oy), hypot(width - ox, height - oy)))

    /**
     * Where the sweep head is at [progress] (0..1 through one loop).
     *
     * @param progress Position in the loop, clamped to 0..1.
     * @param span Sector size in degrees.
     * @param tail Trail length in degrees; a wrapping beam on a partial sector travels this much further so its trail leaves the sector.
     * @param mode End-of-sector behavior.
     * @param clockwise Whether the beam travels clockwise (true) or counter-clockwise (false).
     * @param fades Whether an edge fade is active; a fading beam is gone before it leaves, so no extra travel is added.
     */
    fun sweepPose(progress: Float, span: Float, tail: Float, mode: CyberSweepMode, clockwise: Boolean, fades: Boolean): SweepPose {
        val p = progress.coerceIn(0f, 1f)
        val full = span >= FULL_TURN
        var local = 0f                   // distance travelled from the start, in the direction of travel
        var forwardLeg = true            // false only on the return leg of a bounce
        var tailScale = 1f
        when (mode) {
            CyberSweepMode.WRAP -> local = p * (if (full || fades) span else span + tail)
            CyberSweepMode.HOLD -> local = p * span
            CyberSweepMode.BOUNCE -> {
                forwardLeg = p < BOUNCE_TURN
                val leg = if (forwardLeg) p / BOUNCE_TURN else (1f - p) / BOUNCE_TURN   // 0..1 along the current leg
                local = smoothstep(0f, 1f, leg) * span                                  // eases to a stop at both ends
                tailScale = EASE_SPEED_NORMALISER * leg * (1f - leg)                    // trail follows the beam's speed
            }
        }
        val offset = if (clockwise) local else span - local
        return SweepPose(offset = offset, movingClockwise = clockwise == forwardLeg, tailScale = tailScale)
    }

    /**
     * How far behind the head a point lies, measured along the trail in degrees. A negative value means the point
     * is ahead of the head (outside the beam).
     *
     * @param pointOffset The point's angle, degrees clockwise from the sector start.
     * @param pose Current head position.
     * @param span Sector size in degrees; on the full circle the distance wraps round.
     */
    fun trailDistance(pointOffset: Float, pose: SweepPose, span: Float): Float {
        val raw = if (pose.movingClockwise) pose.offset - pointOffset else pointOffset - pose.offset
        return if (span >= FULL_TURN) raw.mod(FULL_TURN) else raw
    }

    /**
     * Opacity of a trail at [distance] behind the head.
     *
     * Smooth (`steps == 0`) fades linearly from 1 at the head to [fill] at the trail end. With `steps > 0`
     * the trail is cut into that many bands of constant opacity, the nearest at 1 and the farthest at `1/steps`
     * (scaled toward 1 by [fill]), so the fade is visibly discrete.
     *
     * @param distance Distance behind the head, in the same unit as [trail]. Negative means ahead of the head.
     * @param trail Trail length. Zero or less means only the head itself is lit.
     * @param steps Number of opacity bands, or 0 for a smooth fade.
     * @param fill Opacity left behind the whole trail, for filled shapes such as a disc wipe.
     */
    fun trailAlpha(distance: Float, trail: Float, steps: Int, fill: Float = 0f): Float {
        if (distance < 0f) return 0f
        if (trail <= 0f) return if (distance <= 0f) 1f else fill
        if (distance > trail) return fill
        val t = distance / trail
        val level = if (steps > 0) min(floor(t * steps), steps - 1f) / steps else t
        return fill + (1f - fill) * (1f - level)
    }

    /**
     * Opacity multiplier for the edge fade at the ends of a partial sector; 1 on a full circle or when [fadeDegrees] is 0.
     * @param offset TODO: document this
     * @param span TODO: document this
     * @param fadeDegrees TODO: document this
     */
    fun edgeFactor(offset: Float, span: Float, fadeDegrees: Float): Float {
        if (span >= FULL_TURN || fadeDegrees <= 0f) return 1f
        val fadeIn = smoothstep(0f, fadeDegrees, offset)
        val fadeOut = 1f - smoothstep(span - fadeDegrees, span, offset)
        return fadeIn * fadeOut
    }

    /**
     * Opacity profile of a trailing wedge or ring as gradient stops covering position 0..1.
     *
     * The head is at [head]; the trail lies behind it. Everything ahead of the head is transparent, with a hard
     * edge at the head. Behind the trail the opacity is [fill].
     *
     * @param head Position of the head, 0..1.
     * @param trail Trail length as a fraction of the gradient, 0..1. Zero leaves only the hard head edge.
     * @param steps Number of opacity bands, or 0 for a smooth fade.
     * @param fill Opacity behind the trail, 0..1.
     * @param behindIsLower True when the trail is at lower positions than the head (clockwise sweep, outward pulse).
     */
    fun trailStops(head: Float, trail: Float, steps: Int, fill: Float = 0f, behindIsLower: Boolean = true): List<AlphaStop> =
        if (behindIsLower) lowerTrailStops(head.coerceIn(0f, 1f), trail.coerceIn(0f, 1f), steps, fill.coerceIn(0f, 1f))
        else lowerTrailStops(1f - head.coerceIn(0f, 1f), trail.coerceIn(0f, 1f), steps, fill.coerceIn(0f, 1f))
            .map { AlphaStop(1f - it.position, it.alpha) }.asReversed()

    /**
     * Appends a stop whose position is nudged up to the previous stop's, so float rounding in the band
     * arithmetic can never leave positions out of order (gradient shaders require non-decreasing positions).
     *
     * @param position Wanted position, 0..1.
     * @param alpha Opacity at that position, 0..1.
     */
    private fun MutableList<AlphaStop>.addStop(position: Float, alpha: Float) {
        val previous = lastOrNull()?.position ?: 0f
        add(AlphaStop(position.coerceIn(previous, 1f), alpha))
    }

    /**
     * [trailStops] for the case where the trail lies at lower positions than the head [h].
     *
     * @param h Head position, 0..1.
     * @param trail Trail length as a fraction of the gradient, 0..1.
     * @param steps Number of opacity bands, or 0 for a smooth fade.
     * @param fill Opacity behind the trail, 0..1.
     */
    private fun lowerTrailStops(h: Float, trail: Float, steps: Int, fill: Float): List<AlphaStop> {
        val out = ArrayList<AlphaStop>()
        if (trail <= 0f) {
            out.addStop(0f, fill)
            out.addStop(h, fill)
        } else {
            val start = max(0f, h - trail)
            if (start > 0f) {
                out.addStop(0f, fill)
                out.addStop(start, fill)
            }
            if (steps > 0) {
                val band = trail / steps
                for (k in steps - 1 downTo 0) {                       // farthest band first so positions only increase
                    val hi = h - k * band
                    if (hi <= 0f) continue
                    val alpha = fill + (1f - fill) * (1f - k.toFloat() / steps)
                    out.addStop(max(h - (k + 1) * band, 0f), alpha)
                    out.addStop(hi, alpha)
                }
            } else {
                out.addStop(start, trailAlpha(h - start, trail, 0, fill))
                out.addStop(h, 1f)
            }
        }
        out.addStop(h, 0f)                                             // hard edge: nothing ahead of the head
        out.addStop(1f, 0f)
        return out
    }

    /**
     * Stops for a rotating sweep wedge.
     *
     * The wedge is built with its trail starting at gradient position 0 (clockwise) or ending at it
     * (counter-clockwise), so the gradient never has to wrap its seam; the caller rotates the gradient so the
     * returned [SweepStops.headFraction] lands on the head's real angle.
     *
     * @param trailDegrees Trail length in degrees (clamped to 0..360).
     * @param steps Number of opacity bands, or 0 for a smooth fade.
     * @param movingClockwise Whether the head moves clockwise, which puts the trail counter-clockwise of it.
     */
    fun sweepStops(trailDegrees: Float, steps: Int, movingClockwise: Boolean): SweepStops {
        val trail = (trailDegrees / FULL_TURN).coerceIn(0f, 1f)
        val head = if (movingClockwise) trail else 0f
        return SweepStops(head, trailStops(head, trail, steps, 0f, behindIsLower = movingClockwise))
    }

    /**
     * How far, 0..1, a ring [index] of [count] has travelled through its life when the loop is at [progress]; rings are evenly staggered.
     * @param progress TODO: document this
     * @param index TODO: document this
     * @param count TODO: document this
     */
    fun ringProgress(progress: Float, index: Int, count: Int): Float =
        (progress + index.toFloat() / count).mod(1f)

    /**
     * Position of a ring head along the inner-to-outer travel, 0 (start) to 1 (end), after [lifeProgress] of its life.
     * @param lifeProgress TODO: document this
     */
    fun pulseHead(lifeProgress: Float, direction: CyberRadialDirection): Float =
        if (direction == CyberRadialDirection.OUTWARD) lifeProgress else 1f - lifeProgress

    /**
     * Opacity multiplier of a ring [lifeProgress] of the way through its life: 1 until [fadeStart], then fading to 0 at the end.
     * @param lifeProgress TODO: document this
     * @param fadeStart TODO: document this
     */
    fun pulseFade(lifeProgress: Float, fadeStart: Float): Float =
        if (fadeStart >= 1f) 1f else 1f - smoothstep(fadeStart, 1f, lifeProgress)
}
