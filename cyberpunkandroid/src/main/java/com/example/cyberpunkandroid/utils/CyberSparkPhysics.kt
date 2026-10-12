package com.example.cyberpunkandroid.utils

import android.hardware.SensorManager
import com.example.cyberpunkandroid.config.CyberSparkDefaults
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin

/**
 * Two-regime cooling of a hot spark in air.
 *
 * The model follows the electric spark kernel research in [CyberSparkDefaults].
 * Cooling has two stages:
 * 1. A fast stage removes [fastRegimeFraction] of the excess heat.
 * 2. A slow stage removes the remaining heat.
 *
 * Each stage is Newton cooling (exponential decay toward [ambientKelvin]).
 * The fast rate is [fastToSlowRateRatio] times the slow rate.
 * The temperature is continuous where the stages meet.
 *
 * This class has no Compose types. JVM tests can use it directly.
 *
 * @property peakKelvin Temperature at age zero, in kelvin. Must be above [draperKelvin].
 * @property ambientKelvin Temperature the spark cools toward, in kelvin. Must be below [draperKelvin].
 * @property draperKelvin Lowest temperature with visible glow, in kelvin.
 *   Below this value the spark is dark. [glowSeconds] ends here.
 * @property slowTimeConstantSeconds Time constant of the slow stage, in seconds.
 *   A larger value gives a longer glow. Must be above zero.
 * @property fastToSlowRateRatio Fast cooling rate divided by slow rate.
 *   The sources give 2 to 10. Must be 1 or more.
 * @property fastRegimeFraction Fraction of excess heat lost in the fast stage.
 *   Zero gives one-stage Newton cooling. Must be in 0 until 1.
 */
data class CyberSparkCooling(
    val peakKelvin: Float = CyberSparkDefaults.Cooling.PeakKelvin,
    val ambientKelvin: Float = CyberSparkDefaults.Cooling.AmbientKelvin,
    val draperKelvin: Float = CyberSparkDefaults.Cooling.DraperKelvin,
    val slowTimeConstantSeconds: Float = CyberSparkDefaults.Cooling.SlowTimeConstantSeconds,
    val fastToSlowRateRatio: Float = CyberSparkDefaults.Cooling.FastToSlowRateRatio,
    val fastRegimeFraction: Float = CyberSparkDefaults.Cooling.FastRegimeFraction,
) {
    init {
        require(peakKelvin > draperKelvin) { "peakKelvin must be above draperKelvin" }
        require(ambientKelvin < draperKelvin) { "ambientKelvin must be below draperKelvin" }
        require(slowTimeConstantSeconds > 0f) { "slowTimeConstantSeconds must be above zero" }
        require(fastToSlowRateRatio >= 1f) { "fastToSlowRateRatio must be 1 or more" }
        require(fastRegimeFraction >= 0f && fastRegimeFraction < 1f) {
            "fastRegimeFraction must be in 0 until 1"
        }
    }

    /** Slow-stage cooling rate, in 1/s. */
    private val slowRate: Float get() = 1f / slowTimeConstantSeconds

    /** Fast-stage cooling rate, in 1/s. */
    private val fastRate: Float get() = fastToSlowRateRatio * slowRate

    /** Excess temperature at age zero, in kelvin. */
    private val excessAtPeak: Float get() = peakKelvin - ambientKelvin

    /** Excess temperature where the fast stage ends, in kelvin. */
    private val excessAtSwitch: Float get() = excessAtPeak * (1f - fastRegimeFraction)

    /** Age where the fast stage ends and the slow stage starts, in seconds. */
    val switchSeconds: Float get() = ln(excessAtPeak / excessAtSwitch) / fastRate

    /** Age where the spark reaches [draperKelvin] and stops glowing, in seconds. */
    val glowSeconds: Float get() = ageAt(draperKelvin)

    /**
     * Gives the spark temperature at an age.
     *
     * @param ageSeconds Time since launch, in seconds. Negative values clamp to zero.
     * @return Temperature in kelvin, from [peakKelvin] down toward [ambientKelvin].
     */
    fun kelvinAt(ageSeconds: Float): Float {
        val age = ageSeconds.coerceAtLeast(0f)
        val excess = if (age < switchSeconds) {
            excessAtPeak * exp(-fastRate * age)
        } else {
            excessAtSwitch * exp(-slowRate * (age - switchSeconds))
        }
        return ambientKelvin + excess
    }

    /**
     * Gives the age where the spark has a temperature. This is the inverse of [kelvinAt].
     *
     * @param kelvin Target temperature, in kelvin.
     *   Values above [peakKelvin] give zero.
     *   Values at or below [ambientKelvin] give positive infinity.
     * @return Age in seconds.
     */
    fun ageAt(kelvin: Float): Float {
        val excess = kelvin - ambientKelvin
        if (excess <= 0f) return Float.POSITIVE_INFINITY
        if (excess >= excessAtPeak) return 0f
        return if (excess >= excessAtSwitch) {
            ln(excessAtPeak / excess) / fastRate
        } else {
            switchSeconds + ln(excessAtSwitch / excess) / slowRate
        }
    }

    /**
     * Gives how far the spark has cooled through its visible range.
     *
     * @param kelvin Spark temperature, in kelvin.
     * @return 0 at [peakKelvin], 1 at or below [draperKelvin].
     */
    fun coolingProgress(kelvin: Float): Float =
        (1f - (kelvin - draperKelvin) / (peakKelvin - draperKelvin)).coerceIn(0f, 1f)

    /**
     * Gives the perceived glow strength at a temperature.
     *
     * The glow fades to zero at [draperKelvin].
     * The curve matches the `CyberWeld` bead fade.
     *
     * @param kelvin Spark temperature, in kelvin.
     * @param exponent Fade curve shape. Below 1 keeps the glow bright for longer.
     * @return Glow strength from 0 to 1.
     */
    fun emission(
        kelvin: Float,
        exponent: Float = CyberSparkDefaults.Cooling.EmissionExponent,
    ): Float = (1f - coolingProgress(kelvin)).pow(exponent)
}

/** Pure launch-direction helper for sparks. No Compose types. */
object CyberSparkMotion {

    /**
     * Gives a launch angle in a symmetric cone.
     *
     * The research shows a symmetric trajectory distribution.
     * A uniform [unitRandom] gives equal spread on each side of [centerRadians].
     *
     * @param unitRandom Random value in 0..1. 0.5 gives [centerRadians].
     * @param centerRadians Direction of the cone center, in radians.
     * @param halfSpreadRadians Half-width of the cone, in radians.
     * @return Launch angle, in radians.
     */
    fun launchAngle(unitRandom: Float, centerRadians: Float, halfSpreadRadians: Float): Float =
        centerRadians + (2f * unitRandom - 1f) * halfSpreadRadians
}

/**
 * Air drag on a small hot sphere, from the steel-spark study.
 *
 * The equation of motion is `m dV/dt = m g - 1/2 rho S C V^2`.
 * The drag coefficient follows Allen's law, `C = a / Re^p`.
 * The Reynolds number is `Re = rho R V / eta`, with R the radius.
 * The study defines Re with the radius, so this class does too.
 *
 * Substituted, the drag deceleration is `k V^(2 - p)`, with
 * `k = 3 a rho^(1 - p) eta^p / (8 rhoP R^(1 + p))`.
 *
 * @property airDensity Air density, in kg/m^3.
 * @property airViscosity Dynamic viscosity of air, in Pa*s.
 * @property particleDensity Spark density, in kg/m^3.
 * @property allenCoefficient Prefactor `a` of Allen's law.
 * @property allenReynoldsExponent Exponent `p` of Allen's law. Must be in 0 until 1.
 * @property gravity Downward acceleration, in m/s^2. Defaults to standard Earth gravity.
 */
data class CyberSparkDrag(
    val airDensity: Float = CyberSparkDefaults.Drag.AirDensity,
    val airViscosity: Float = CyberSparkDefaults.Drag.AirViscosity,
    val particleDensity: Float = CyberSparkDefaults.Drag.ParticleDensity,
    val allenCoefficient: Float = CyberSparkDefaults.Drag.AllenCoefficient,
    val allenReynoldsExponent: Float = CyberSparkDefaults.Drag.AllenReynoldsExponent,
    val gravity: Float = SensorManager.GRAVITY_EARTH,
) {
    init {
        require(airDensity > 0f && airViscosity > 0f && particleDensity > 0f) { "densities and viscosity must be above zero" }
        require(allenReynoldsExponent > 0f && allenReynoldsExponent < 1f) { "allenReynoldsExponent must be in 0 until 1" }
    }

    /**
     * Gives the Reynolds number of a moving sphere.
     *
     * @param radiusMeters Sphere radius, in meters.
     * @param speed Speed relative to the air, in m/s.
     */
    fun reynolds(radiusMeters: Float, speed: Float): Float = airDensity * radiusMeters * speed / airViscosity

    /**
     * Gives the drag coefficient from Allen's law.
     *
     * @param reynolds Reynolds number. Must be above zero.
     */
    fun dragCoefficient(reynolds: Float): Float = allenCoefficient / reynolds.pow(allenReynoldsExponent)

    /**
     * Gives the drag constant `k` in `deceleration = k V^(2 - p)`.
     *
     * @param radiusMeters Sphere radius, in meters.
     */
    fun dragConstant(radiusMeters: Float): Float =
        SPHERE_AREA_TO_MASS * allenCoefficient * airDensity.pow(1f - allenReynoldsExponent) *
            airViscosity.pow(allenReynoldsExponent) /
            (particleDensity * radiusMeters.pow(1f + allenReynoldsExponent))

    /**
     * Gives the drag deceleration magnitude.
     *
     * @param radiusMeters Sphere radius, in meters.
     * @param speed Speed relative to the air, in m/s. Zero gives zero.
     * @return Deceleration, in m/s^2, opposite to the velocity.
     */
    fun deceleration(radiusMeters: Float, speed: Float): Float =
        if (speed <= 0f) 0f else dragConstant(radiusMeters) * speed.pow(2f - allenReynoldsExponent)

    /**
     * Gives the time to travel a distance in a straight line, without gravity.
     *
     * This is the exact solution of `dV/dt = -k V^m`, with `m = 2 - p`.
     * Gravity is left out. During this flight drag is much larger than gravity.
     *
     * @param radiusMeters Sphere radius, in meters.
     * @param launchSpeed Speed at launch, in m/s. Must be above zero.
     * @param distanceMeters Distance to travel, in meters.
     * @return Time in seconds. Positive infinity if drag stops the sphere first.
     */
    fun timeToTravel(radiusMeters: Float, launchSpeed: Float, distanceMeters: Float): Float {
        val k = dragConstant(radiusMeters)
        val p = allenReynoldsExponent
        val m = 2f - p
        val speedPowP = launchSpeed.pow(p) - k * p * distanceMeters
        if (speedPowP <= 0f) return Float.POSITIVE_INFINITY
        val speed = speedPowP.pow(1f / p)
        return (speed.pow(1f - m) - launchSpeed.pow(1f - m)) / ((m - 1f) * k)
    }

    /**
     * Moves one spark forward in time with gravity and drag.
     *
     * The step is semi-implicit Euler: velocity first, then position.
     * Drag cannot reverse the velocity in one step.
     *
     * @param state Spark state `[x, y, vx, vy]` in meters and m/s. Y points down. Changed in place.
     * @param radiusMeters Spark radius, in meters.
     * @param seconds Step length, in seconds. Use [CyberSparkDefaults.Drag.MaxStepSeconds] or less.
     */
    fun step(state: FloatArray, radiusMeters: Float, seconds: Float) {
        val vx = state[VX]
        val vy = state[VY]
        val speed = hypot(vx, vy)
        // Fraction of speed that is left after drag. Clamp at zero so drag never reverses motion.
        val keep = if (speed > 0f) (1f - deceleration(radiusMeters, speed) * seconds / speed).coerceAtLeast(0f) else 1f
        state[VX] = vx * keep
        state[VY] = vy * keep + gravity * seconds
        state[X] += state[VX] * seconds
        state[Y] += state[VY] * seconds
    }

    /**
     * Moves one spark forward over a longer time, in safe sub-steps.
     *
     * @param state Spark state `[x, y, vx, vy]`. Changed in place.
     * @param radiusMeters Spark radius, in meters.
     * @param seconds Total time, in seconds. Zero or less does nothing.
     * @param maxStepSeconds Longest sub-step, in seconds.
     */
    fun advance(
        state: FloatArray,
        radiusMeters: Float,
        seconds: Float,
        maxStepSeconds: Float = CyberSparkDefaults.Drag.MaxStepSeconds,
    ) {
        if (seconds <= 0f) return
        val steps = ceil(seconds / maxStepSeconds).toInt()
        val dt = seconds / steps
        repeat(steps) { step(state, radiusMeters, dt) }
    }

    companion object {
        /** Index of x position in a state array. */
        const val X = 0

        /** Index of y position in a state array. Y points down. */
        const val Y = 1

        /** Index of x velocity in a state array. */
        const val VX = 2

        /** Index of y velocity in a state array. */
        const val VY = 3

        /** Length of a state array. */
        const val STATE_SIZE = 4

        /** Drag area over mass for a sphere: (pi R^2 / 2) / (4/3 pi R^3) = 3 / (8 R). */
        private const val SPHERE_AREA_TO_MASS = 3f / 8f
    }
}

/**
 * Random launch values for sparks, from the steel-spark study.
 *
 * Radius and speed are normal samples, cut at [sigmaLimit] deviations.
 *
 * @property radiusMeanMeters Mean spark radius, in meters.
 * @property radiusStdMeters Standard deviation of radius, in meters.
 * @property speedMean Mean launch speed, in m/s.
 * @property speedStd Standard deviation of launch speed, in m/s.
 * @property sigmaLimit Samples are cut at this many deviations. Must give positive values.
 * @property centerRadians Launch cone center, in radians. Screen Y points down.
 * @property halfSpreadRadians Launch cone half-width, in radians.
 * @property referenceDistanceMeters Distance shown in one glow life, in meters.
 *   It also maps to one screen extent.
 */
data class CyberSparkLaunch(
    val radiusMeanMeters: Float = CyberSparkDefaults.Launch.RadiusMeanMeters,
    val radiusStdMeters: Float = CyberSparkDefaults.Launch.RadiusStdMeters,
    val speedMean: Float = CyberSparkDefaults.Launch.SpeedMean,
    val speedStd: Float = CyberSparkDefaults.Launch.SpeedStd,
    val sigmaLimit: Float = CyberSparkDefaults.Launch.SigmaLimit,
    val centerRadians: Float = CyberSparkDefaults.Motion.LaunchCenterRadians,
    val halfSpreadRadians: Float = CyberSparkDefaults.Motion.LaunchHalfSpreadRadians,
    val referenceDistanceMeters: Float = CyberSparkDefaults.Launch.ReferenceDistanceMeters,
) {
    init {
        require(radiusMeanMeters - sigmaLimit * radiusStdMeters > 0f) { "radius samples must stay above zero" }
        require(speedMean - sigmaLimit * speedStd > 0f) { "speed samples must stay above zero" }
        require(referenceDistanceMeters > 0f) { "referenceDistanceMeters must be above zero" }
    }

    /** Smallest radius a sample can have, in meters. */
    val radiusMinMeters: Float get() = radiusMeanMeters - sigmaLimit * radiusStdMeters

    /** Largest radius a sample can have, in meters. */
    val radiusMaxMeters: Float get() = radiusMeanMeters + sigmaLimit * radiusStdMeters

    /**
     * Gives a normal sample, cut at [sigmaLimit] deviations.
     *
     * @param random Source of Gaussian values.
     * @param mean Mean of the distribution.
     * @param std Standard deviation of the distribution.
     */
    private fun cutNormal(random: java.util.Random, mean: Float, std: Float): Float =
        mean + std * random.nextGaussian().toFloat().coerceIn(-sigmaLimit, sigmaLimit)

    /**
     * Fills a new launch state.
     *
     * @param random Source of random values. Calls are in a fixed order.
     * @param state Array to fill as `[0, 0, vx, vy]`. Changed in place.
     * @return The sampled radius, in meters.
     */
    fun sample(random: java.util.Random, state: FloatArray): Float {
        val angle = CyberSparkMotion.launchAngle(random.nextFloat(), centerRadians, halfSpreadRadians)
        val speed = cutNormal(random, speedMean, speedStd)
        val radius = cutNormal(random, radiusMeanMeters, radiusStdMeters)
        state[CyberSparkDrag.X] = 0f
        state[CyberSparkDrag.Y] = 0f
        state[CyberSparkDrag.VX] = cos(angle) * speed
        state[CyberSparkDrag.VY] = sin(angle) * speed
        return radius
    }
}

/**
 * A spray of sparks that fly with drag and cool on a schedule.
 *
 * Each spark has a screen age, in seconds.
 * At [CyberSparkCooling.glowSeconds] it launches again on a new path.
 * Screen time maps to physical time by [physicalSecondsPerScreenSecond].
 * That map shows one mean flight to the reference distance per glow life.
 *
 * The spray is deterministic: equal seeds give equal paths.
 *
 * @param count Number of sparks. Zero or more.
 * @param cooling Cooling model. It sets the glow life.
 * @param drag Drag model for the flight.
 * @param launch Launch values for each new flight.
 * @param seed Base seed for all random values.
 */
class CyberSparkSpray(
    count: Int,
    val cooling: CyberSparkCooling = CyberSparkCooling(),
    val drag: CyberSparkDrag = CyberSparkDrag(),
    val launch: CyberSparkLaunch = CyberSparkLaunch(),
    private val seed: Long = 0L,
) {
    /** Number of sparks. */
    val count: Int = count.coerceAtLeast(0)

    /** Physical seconds shown per screen second at playback rate 1. */
    val physicalSecondsPerScreenSecond: Float =
        drag.timeToTravel(launch.radiusMeanMeters, launch.speedMean, launch.referenceDistanceMeters) /
            cooling.glowSeconds

    /** Spark states, `STATE_SIZE` values per spark. */
    private val states = FloatArray(this.count * CyberSparkDrag.STATE_SIZE)

    /** Spark radii, in meters. */
    private val radii = FloatArray(this.count)

    /** Screen age of each spark, in seconds. */
    private val ages = FloatArray(this.count)

    /** Launch number of each spark. It seeds the path. */
    private val launches = IntArray(this.count)

    /** Fixed playback rate of each spark. It staggers the launches. */
    private val rates = FloatArray(this.count)

    /** One spark state, reused for each step to avoid allocation. */
    private val scratch = FloatArray(CyberSparkDrag.STATE_SIZE)

    init {
        for (i in 0 until this.count) {
            val schedule = java.util.Random(seed + CyberSparkDefaults.Salt.Schedule + i)
            rates[i] = CyberSparkDefaults.Motion.RateMultiplierMin +
                schedule.nextFloat() * CyberSparkDefaults.Motion.RateMultiplierRange
            relaunch(i)
            ages[i] = schedule.nextFloat() * cooling.glowSeconds
            fly(i, ages[i] * physicalSecondsPerScreenSecond)
        }
    }

    /**
     * Starts a new flight for one spark.
     *
     * @param i Spark index.
     */
    private fun relaunch(i: Int) {
        val random = java.util.Random(
            seed + CyberSparkDefaults.Salt.Launch + i * CyberSparkDefaults.Salt.SeedStride + launches[i],
        )
        radii[i] = launch.sample(random, scratch)
        scratch.copyInto(states, i * CyberSparkDrag.STATE_SIZE)
    }

    /**
     * Moves one spark forward in physical time.
     *
     * @param i Spark index.
     * @param physicalSeconds Physical time to move, in seconds.
     */
    private fun fly(i: Int, physicalSeconds: Float) {
        val offset = i * CyberSparkDrag.STATE_SIZE
        states.copyInto(scratch, 0, offset, offset + CyberSparkDrag.STATE_SIZE)
        drag.advance(scratch, radii[i], physicalSeconds)
        scratch.copyInto(states, offset)
    }

    /**
     * Moves all sparks forward in screen time.
     *
     * A spark that passes its glow life launches again.
     * It then flies for the time left over.
     *
     * @param screenSeconds Screen time since the last call, in seconds.
     * @param playbackRate Speed multiplier for all sparks. 1 is the model time scale.
     */
    fun advance(screenSeconds: Float, playbackRate: Float = 1f) {
        if (screenSeconds <= 0f || playbackRate <= 0f) return
        val glow = cooling.glowSeconds
        for (i in 0 until count) {
            val dt = screenSeconds * playbackRate * rates[i]
            var age = ages[i] + dt
            if (age >= glow) {
                launches[i] += (age / glow).toInt()
                age %= glow
                relaunch(i)
                fly(i, age * physicalSecondsPerScreenSecond)
            } else {
                fly(i, dt * physicalSecondsPerScreenSecond)
            }
            ages[i] = age
        }
    }

    /**
     * Gives the screen age of a spark.
     *
     * @param i Spark index.
     * @return Age in seconds, from zero up to the glow life.
     */
    fun ageSeconds(i: Int): Float = ages[i]

    /**
     * Gives the x position of a spark, in meters from the launch point.
     *
     * @param i Spark index.
     */
    fun xMeters(i: Int): Float = states[i * CyberSparkDrag.STATE_SIZE + CyberSparkDrag.X]

    /**
     * Gives the y position of a spark, in meters from the launch point. Y points down.
     *
     * @param i Spark index.
     */
    fun yMeters(i: Int): Float = states[i * CyberSparkDrag.STATE_SIZE + CyberSparkDrag.Y]

    /**
     * Gives the speed of a spark, in m/s.
     *
     * @param i Spark index.
     */
    fun speed(i: Int): Float {
        val offset = i * CyberSparkDrag.STATE_SIZE
        return hypot(states[offset + CyberSparkDrag.VX], states[offset + CyberSparkDrag.VY])
    }

    /**
     * Gives where a spark radius sits in the sample range.
     *
     * @param i Spark index.
     * @return 0 for the smallest radius, 1 for the largest.
     */
    fun radiusFraction(i: Int): Float =
        ((radii[i] - launch.radiusMinMeters) / (launch.radiusMaxMeters - launch.radiusMinMeters)).coerceIn(0f, 1f)
}
