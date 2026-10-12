package com.example.cyberpunkandroid.utils

import com.example.cyberpunkandroid.config.CyberSparkDefaults
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

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

/** Pure motion helpers for ballistic sparks. No Compose types. */
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

    /**
     * Gives the displacement on one axis under constant acceleration.
     *
     * This is `v * t + a * t^2 / 2`. There is no drag term.
     * The research gives no drag data, and buoyancy is negligible.
     *
     * @param velocity Launch velocity on this axis, in units per second.
     * @param acceleration Constant acceleration on this axis, in units per second squared.
     * @param timeSeconds Time since launch, in seconds.
     * @return Displacement from the launch point, in the same units.
     */
    fun displacement(velocity: Float, acceleration: Float, timeSeconds: Float): Float =
        velocity * timeSeconds + HALF * acceleration * timeSeconds * timeSeconds

    /** The one-half factor in the constant-acceleration equation. */
    private const val HALF = 0.5f
}
