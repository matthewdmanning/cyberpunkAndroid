package com.example.cyberpunkandroid

import com.example.cyberpunkandroid.config.CyberSparkDefaults
import com.example.cyberpunkandroid.utils.CyberSparkCooling
import com.example.cyberpunkandroid.utils.CyberSparkMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.exp

/**
 * JVM tests for the pure spark physics: two-regime cooling and ballistic motion.
 * No Android or Compose runtime is necessary.
 */
class CyberSparkPhysicsTest {

    /** Relative tolerance for temperatures and times. */
    private val eps = 1e-3f

    /** Default cooling model under test. */
    private val cooling = CyberSparkCooling()

    /** Age zero gives the peak temperature. */
    @Test
    fun startsAtPeak() {
        assertEquals(cooling.peakKelvin, cooling.kelvinAt(0f), eps)
    }

    /** The fast stage ends after it removes the fast fraction of excess heat. */
    @Test
    fun fastStageRemovesItsFraction() {
        val excess0 = cooling.peakKelvin - cooling.ambientKelvin
        val expected = cooling.ambientKelvin + excess0 * (1f - cooling.fastRegimeFraction)
        assertEquals(expected, cooling.kelvinAt(cooling.switchSeconds), expected * eps)
    }

    /** The fast stage cools faster than the slow stage by the rate ratio. */
    @Test
    fun fastStageRateIsRatioTimesSlowRate() {
        val dt = cooling.switchSeconds / 4f
        val excess = { t: Float -> cooling.kelvinAt(t) - cooling.ambientKelvin }
        val fastLogSlope = kotlin.math.ln(excess(0f) / excess(dt)) / dt
        val t1 = cooling.switchSeconds + dt
        val slowLogSlope = kotlin.math.ln(excess(t1) / excess(t1 + dt)) / dt
        assertEquals(cooling.fastToSlowRateRatio, fastLogSlope / slowLogSlope, eps * 10f)
    }

    /** The default rate ratio is in the 2 to 10 source range. */
    @Test
    fun defaultRatioIsInSourceRange() {
        val ratio = CyberSparkDefaults.Cooling.FastToSlowRateRatio
        assertTrue(ratio >= CyberSparkDefaults.Cooling.FastToSlowRateRatioMin)
        assertTrue(ratio <= CyberSparkDefaults.Cooling.FastToSlowRateRatioMax)
    }

    /** Temperature decreases with age at all sample points. */
    @Test
    fun coolsMonotonically() {
        var last = Float.POSITIVE_INFINITY
        val steps = 200
        for (k in 0..steps) {
            val t = cooling.glowSeconds * 2f * k / steps
            val kelvin = cooling.kelvinAt(t)
            assertTrue("not decreasing at t=$t", kelvin < last)
            last = kelvin
        }
    }

    /** ageAt is the inverse of kelvinAt in both stages. */
    @Test
    fun ageAtInvertsKelvinAt() {
        for (t in listOf(0.01f, cooling.switchSeconds / 2f, cooling.switchSeconds * 1.5f, cooling.glowSeconds)) {
            assertEquals(t, cooling.ageAt(cooling.kelvinAt(t)), t * eps)
        }
    }

    /** The glow ends at the Draper point with zero emission. */
    @Test
    fun glowEndsAtDraperPoint() {
        assertEquals(cooling.draperKelvin, cooling.kelvinAt(cooling.glowSeconds), cooling.draperKelvin * eps)
        assertEquals(0f, cooling.emission(cooling.draperKelvin), eps)
        assertEquals(1f, cooling.emission(cooling.peakKelvin), eps)
        assertEquals(1f, cooling.coolingProgress(cooling.draperKelvin), eps)
    }

    /** Zero fast fraction gives one-stage Newton cooling. */
    @Test
    fun zeroFastFractionIsNewtonCooling() {
        val newton = CyberSparkCooling(fastRegimeFraction = 0f)
        val t = newton.slowTimeConstantSeconds
        val expected = newton.ambientKelvin + (newton.peakKelvin - newton.ambientKelvin) * exp(-1f)
        assertEquals(expected, newton.kelvinAt(t), expected * eps)
    }

    /** The default glow life is near the old 0.5 s to 1.25 s ember life. */
    @Test
    fun defaultGlowLifeIsInOldRange() {
        assertTrue(cooling.glowSeconds in 0.5f..1.25f)
    }

    /** The launch cone is symmetric about its center. */
    @Test
    fun launchConeIsSymmetric() {
        val center = CyberSparkDefaults.Motion.LaunchCenterRadians
        val half = CyberSparkDefaults.Motion.LaunchHalfSpreadRadians
        assertEquals(center, CyberSparkMotion.launchAngle(0.5f, center, half), eps)
        val low = CyberSparkMotion.launchAngle(0f, center, half)
        val high = CyberSparkMotion.launchAngle(1f, center, half)
        assertEquals(center - low, high - center, eps)
    }

    /** Ballistic displacement is v*t + a*t^2/2. */
    @Test
    fun displacementIsConstantAcceleration() {
        assertEquals(2f * 3f + 0.5f * 4f * 9f, CyberSparkMotion.displacement(2f, 4f, 3f), eps)
    }

    /** Invalid inputs are rejected. */
    @Test(expected = IllegalArgumentException::class)
    fun rejectsRatioBelowOne() {
        CyberSparkCooling(fastToSlowRateRatio = 0.5f)
    }
}
