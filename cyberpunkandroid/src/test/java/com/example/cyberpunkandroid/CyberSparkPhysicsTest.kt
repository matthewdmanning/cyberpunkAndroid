package com.example.cyberpunkandroid

import com.example.cyberpunkandroid.config.CyberSparkDefaults
import com.example.cyberpunkandroid.utils.CyberSparkCooling
import com.example.cyberpunkandroid.utils.CyberSparkDrag
import com.example.cyberpunkandroid.utils.CyberSparkLaunch
import com.example.cyberpunkandroid.utils.CyberSparkSpray
import com.example.cyberpunkandroid.utils.CyberSparkMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow

/**
 * JVM tests for the pure spark physics: two-regime cooling, drag and the spray.
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

    /** Invalid inputs are rejected. */
    @Test(expected = IllegalArgumentException::class)
    fun rejectsRatioBelowOne() {
        CyberSparkCooling(fastToSlowRateRatio = 0.5f)
    }
    /** Drag model under test. */
    private val drag = CyberSparkDrag()

    /** Drag model without gravity, for straight-line checks. */
    private val dragNoGravity = CyberSparkDrag(gravity = 0f)

    /** Mean launch radius from the source, in meters. */
    private val radius = CyberSparkDefaults.Launch.RadiusMeanMeters

    /** Mean launch speed from the source, in m/s. */
    private val speed = CyberSparkDefaults.Launch.SpeedMean

    /** Allen's law gives its prefactor at Re = 1. */
    @Test
    fun allenLawAtReynoldsOne() {
        assertEquals(CyberSparkDefaults.Drag.AllenCoefficient, drag.dragCoefficient(1f), eps)
    }

    /** The closed-form deceleration matches the force equation from the source. */
    @Test
    fun decelerationMatchesForceEquation() {
        val area = (PI * radius * radius).toFloat()
        val mass = (4.0 / 3.0 * PI * radius.toDouble().pow(3) * drag.particleDensity).toFloat()
        val c = drag.dragCoefficient(drag.reynolds(radius, speed))
        val direct = 0.5f * drag.airDensity * area * c * speed * speed / mass
        assertEquals(direct, drag.deceleration(radius, speed), direct * eps)
    }

    /** The stepped flight matches the exact straight-line time. */
    @Test
    fun steppedFlightMatchesClosedForm() {
        val distance = CyberSparkDefaults.Launch.ReferenceDistanceMeters
        val exact = dragNoGravity.timeToTravel(radius, speed, distance)
        val state = floatArrayOf(0f, 0f, speed, 0f)
        dragNoGravity.advance(state, radius, exact)
        assertEquals(distance, state[CyberSparkDrag.X], distance * 0.01f)
    }

    /** Speed falls steadily during a flight. */
    @Test
    fun speedFallsDuringFlight() {
        val state = floatArrayOf(0f, 0f, speed, 0f)
        var last = speed
        repeat(40) {
            dragNoGravity.advance(state, radius, 1e-3f)
            val now = state[CyberSparkDrag.VX]
            assertTrue(now < last)
            last = now
        }
    }

    /** A spark that falls long enough reaches terminal speed: drag equals gravity. */
    @Test
    fun reachesTerminalSpeed() {
        val state = floatArrayOf(0f, 0f, 0f, 0f)
        drag.advance(state, radius, 2f)
        val terminal = (drag.gravity / drag.dragConstant(radius)).pow(1f / (2f - drag.allenReynoldsExponent))
        assertEquals(terminal, state[CyberSparkDrag.VY], terminal * 0.01f)
    }

    /** Launch samples stay inside the cut range. */
    @Test
    fun launchSamplesStayInRange() {
        val launch = CyberSparkLaunch()
        val state = FloatArray(CyberSparkDrag.STATE_SIZE)
        val random = java.util.Random(1L)
        repeat(500) {
            val r = launch.sample(random, state)
            assertTrue(r in launch.radiusMinMeters..launch.radiusMaxMeters)
            val v = kotlin.math.hypot(state[CyberSparkDrag.VX], state[CyberSparkDrag.VY])
            val vMax = launch.speedMean + launch.sigmaLimit * launch.speedStd
            assertTrue(v <= vMax * (1f + eps))
            assertTrue("spark must launch upward", state[CyberSparkDrag.VY] < 0f)
        }
    }

    /** Equal seeds give equal sprays. */
    @Test
    fun sprayIsDeterministic() {
        val a = CyberSparkSpray(8, seed = 42L)
        val b = CyberSparkSpray(8, seed = 42L)
        repeat(100) { a.advance(1f / 60f); b.advance(1f / 60f) }
        for (i in 0 until 8) {
            assertEquals(a.xMeters(i), b.xMeters(i), 0f)
            assertEquals(a.yMeters(i), b.yMeters(i), 0f)
        }
    }

    /** Ages stay inside one glow life, also after a long frame. */
    @Test
    fun sprayAgesStayInGlowLife() {
        val spray = CyberSparkSpray(16)
        repeat(20) { spray.advance(0.5f) }
        for (i in 0 until spray.count) {
            assertTrue(spray.ageSeconds(i) >= 0f && spray.ageSeconds(i) < spray.cooling.glowSeconds)
        }
    }

    /** A fresh spark is above the launch point and moves fast. */
    @Test
    fun freshSparkRisesFast() {
        val spray = CyberSparkSpray(32)
        for (i in 0 until spray.count) {
            if (spray.ageSeconds(i) < spray.cooling.glowSeconds * 0.1f) {
                assertTrue(spray.yMeters(i) <= 0f)
                assertTrue(spray.speed(i) > CyberSparkDefaults.Launch.SpeedMean * 0.3f)
            }
        }
    }
}
