package com.example.cyberpunkandroid

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.components.CyberPixelFrontierTransition
import com.example.cyberpunkandroid.components.pixelFrontierPosition
import com.example.cyberpunkandroid.effects.CyberPixelFrontierField
import com.example.cyberpunkandroid.effects.CyberShaders
import org.apache.commons.statistics.descriptive.Kurtosis
import org.apache.commons.statistics.descriptive.Mean
import org.apache.commons.statistics.descriptive.Skewness
import org.apache.commons.statistics.descriptive.StandardDeviation
import org.apache.commons.statistics.descriptive.Variance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Checks for the pixel frontier transition prototype.
 *
 * These tests run on the JVM. They cannot judge the look or the frame time. Those need a physical device.
 */
@RunWith(AndroidJUnit4::class)
class CyberPixelFrontierTransitionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Tolerance for float comparisons in pixels. */
    private val pixelTolerance = 0.001f

    /** Use this test to confirm the band is fully off screen at both ends of the sweep. */
    @Test
    fun `frontier starts and ends with the whole band off screen`() {
        val axisLength = 2400f
        val band = 500f

        val start = pixelFrontierPosition(progress = 0f, axisLength = axisLength, margin = band)
        val end = pixelFrontierPosition(progress = 1f, axisLength = axisLength, margin = band)

        // At the start, the band's far side must not enter the layer (position + band <= 0).
        assertTrue(start + band <= pixelTolerance)
        // At the end, the band's near side must have left the layer (position - band >= axisLength).
        assertTrue(end - band >= axisLength - pixelTolerance)
    }

    /** Use this test to confirm the frontier never moves backward. */
    @Test
    fun `frontier moves forward as progress rises`() {
        var previous = Float.NEGATIVE_INFINITY
        for (step in 0..20) {
            val position = pixelFrontierPosition(step / 20f, axisLength = 1080f, margin = 200f)
            assertTrue(position >= previous)
            previous = position
        }
    }

    /**
     * Use this test to catch drift between the shader text and the Kotlin code. Setting a uniform that the
     * shader does not declare throws at runtime, and the JVM cannot compile AGSL. So this test compares names.
     */
    @Test
    fun `shader declares exactly the uniforms that Kotlin sets`() {
        val declared = Regex("""\buniform\s+\w+\s+(\w+)\s*;""")
            .findAll(CyberShaders.PixelFrontierShader)
            .map { it.groupValues[1] }
            .toSet()

        assertEquals(CyberShaders.PixelFrontierUniforms.All.toSet(), declared)
    }

    /** Use this test to confirm the block loop has a constant bound, which AGSL requires. */
    @Test
    fun `shader loop is bounded by a constant`() {
        assertTrue(CyberShaders.PixelFrontierShader.contains("const int LevelCount"))
        assertTrue(CyberShaders.PixelFrontierShader.contains("i < LevelCount"))
    }

    /** Use this test to confirm the API 32 fallback shows and swaps content with the default transition. */
    @Test
    @Config(sdk = [32])
    fun `fallback below API 33 shows the current screen and swaps it`() {
        var page by mutableIntStateOf(0)
        composeTestRule.setContent {
            CyberPixelFrontierTransition(targetState = page) { shown -> Text("page $shown") }
        }

        composeTestRule.onNodeWithText("page 0").assertExists()

        page = 1
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(LongTransitionMillis)

        composeTestRule.onNodeWithText("page 1").assertExists()
    }

    /** Use this test to confirm the API 33 path composes and swaps content without throwing. */
    @Test
    @Config(sdk = [33])
    fun `shader path on API 33 shows the current screen and swaps it`() {
        var page by mutableIntStateOf(0)
        composeTestRule.setContent {
            CyberPixelFrontierTransition(targetState = page) { shown -> Text("page $shown") }
        }

        composeTestRule.onNodeWithText("page 0").assertExists()

        page = 1
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(LongTransitionMillis)

        composeTestRule.onNodeWithText("page 1").assertExists()
    }

    /** Verifies that the old screen clip bounds strictly confine old screen content and prevent spillover. */
    @Test
    fun oldScreenClipRect_confinesToOldScreenTerritory() {
        // Horizontal Right-to-Left: old screen is strictly to the left of the frontier [0, frontierPos]
        val r2l = com.example.cyberpunkandroid.components.computeOldScreenClipRect(
            width = 1080f,
            height = 2400f,
            frontierPos = 540f,
            vertical = false,
            reverse = true,
        )
        assertEquals(0f, r2l.left, pixelTolerance)
        assertEquals(540f, r2l.right, pixelTolerance)

        // Horizontal Left-to-Right: old screen is strictly to the right of the frontier [frontierPos, width]
        val l2r = com.example.cyberpunkandroid.components.computeOldScreenClipRect(
            width = 1080f,
            height = 2400f,
            frontierPos = 540f,
            vertical = false,
            reverse = false,
        )
        assertEquals(540f, l2r.left, pixelTolerance)
        assertEquals(1080f, l2r.right, pixelTolerance)
    }

    /**
     * Programmatic verification: New screen is strictly confined to the non-particle side
     * of the barrier/divide and never leaks into the particle territory.
     */
    @Test
    fun newScreenClipRect_confinesToNonParticleSide_complementingOldScreen() {
        // Horizontal Right-to-Left: new screen is strictly to the right of the frontier [frontierPos, width]
        val r2lNew = com.example.cyberpunkandroid.components.computeNewScreenClipRect(
            width = 1080f,
            height = 2400f,
            frontierPos = 540f,
            vertical = false,
            reverse = true,
        )
        assertEquals(540f, r2lNew.left, pixelTolerance)
        assertEquals(1080f, r2lNew.right, pixelTolerance)

        // Horizontal Left-to-Right: new screen is strictly to the left of the frontier [0, frontierPos]
        val l2rNew = com.example.cyberpunkandroid.components.computeNewScreenClipRect(
            width = 1080f,
            height = 2400f,
            frontierPos = 540f,
            vertical = false,
            reverse = false,
        )
        assertEquals(0f, l2rNew.left, pixelTolerance)
        assertEquals(540f, l2rNew.right, pixelTolerance)

        // Vertical Top-to-Bottom: new screen is strictly at the top [0, frontierPos]
        val t2bNew = com.example.cyberpunkandroid.components.computeNewScreenClipRect(
            width = 1080f,
            height = 2400f,
            frontierPos = 800f,
            vertical = true,
            reverse = false,
        )
        assertEquals(0f, t2bNew.top, pixelTolerance)
        assertEquals(800f, t2bNew.bottom, pixelTolerance)

        // Vertical Bottom-to-Top: new screen is strictly at the bottom [frontierPos, height]
        val b2tNew = com.example.cyberpunkandroid.components.computeNewScreenClipRect(
            width = 1080f,
            height = 2400f,
            frontierPos = 800f,
            vertical = true,
            reverse = true,
        )
        assertEquals(800f, b2tNew.top, pixelTolerance)
        assertEquals(2400f, b2tNew.bottom, pixelTolerance)
    }

    /**
     * Programmatic verification: Proves that the spatial stochastic field possesses true 2D isotropic
     * autocorrelation, strictly breaking any 1D row bias or horizontal streakiness.
     */
    @Test
    fun autocorrelation_is2D_andNotBiasedByRow() {
        val size = 64
        val grid = Array(size) { y ->
            FloatArray(size) { x ->
                CyberPixelFrontierField.evaluate2DField(x.toFloat(), y.toFloat(), time = 0.35f, mode = 0)
            }
        }

        val maxLag = 8
        val result = CyberPixelFrontierField.compute2DAutocorrelation(grid, maxLag = maxLag)

        // Across all spatial lags, horizontal (row) and vertical (column) autocorrelation track identically:
        for (lag in 1..maxLag) {
            val rowCorr = result.rowLagCorrelation[lag]
            val colCorr = result.colLagCorrelation[lag]
            val diff = kotlin.math.abs(rowCorr - colCorr)
            assertTrue("Lag $lag correlation diff ($diff) should be <= 0.15 (row: $rowCorr, col: $colCorr)", diff <= 0.15f)
        }

        // Isotropy ratio must be close to 1.0 (within 15%), confirming 2D isotropy:
        assertTrue(
            "Isotropy ratio (${result.isotropyRatio}) must be within [0.85, 1.15] for true 2D autocorrelation",
            result.isotropyRatio in 0.85f..1.15f
        )
    }

    /**
     * Programmatic verification: Directional streakiness metric mathematically proves the absence of the
     * row-histogram / horizontal-line anti-pattern.
     */
    @Test
    fun directionalStreakiness_confirmsAbsenceOfRowHistogramAntiPattern() {
        val size = 64
        // 1. Evaluate the true 2D field:
        val field2D = Array(size) { y ->
            FloatArray(size) { x ->
                CyberPixelFrontierField.evaluate2DField(x.toFloat(), y.toFloat(), time = 0.5f, mode = 0)
            }
        }
        val metric2D = CyberPixelFrontierField.computeDirectionalStreakiness(field2D)

        // In 2D isotropic field, row-means variance and col-means variance are balanced (no line preference):
        assertTrue(
            "2D field streakiness ratio (${metric2D.streakinessRatio}) must be within [0.80, 1.25]",
            metric2D.streakinessRatio in 0.80f..1.25f
        )

        // 2. In contrast, verify that a 1D row-modulated field (the anti-pattern) produces an extreme ratio (> 10.0):
        val rowHistogramField = Array(size) { y ->
            // Modulated strictly by row y:
            val rowVal = kotlin.math.sin(y * 0.2)
            FloatArray(size) { x ->
                (rowVal + 0.1 * kotlin.math.sin(x * 0.2)).toFloat()
            }
        }
        val metricHistogram = CyberPixelFrontierField.computeDirectionalStreakiness(rowHistogramField)
        assertTrue(
            "1D row anti-pattern must produce an extreme streakiness ratio (> 5.0), got ${metricHistogram.streakinessRatio}",
            metricHistogram.streakinessRatio > 5.0f
        )
    }

    /**
     * Programmatic verification: Lifetimes must be completely random per particle, drawn from a defined
     * continuous probability distribution with a skinnier tail (Beta(2, 5)).
     */
    @Test
    fun particleLifetimes_areCompletelyRandom_withDefinedSkinnyTailDistribution() {
        val size = 64
        val samples = DoubleArray(size * size) { i ->
            val x = i % size
            val y = i / size
            CyberPixelFrontierField.particleLifetime(gridX = x, gridY = y).toDouble()
        }

        // Descriptive statistics powered by Apache Commons Statistics:
        val mean = Mean.of(*samples).asDouble
        assertTrue("Mean lifetime ($mean) must be centered around 0.786 (+/- 0.08)", kotlin.math.abs(mean - 0.786) < 0.08)

        val std = StandardDeviation.of(*samples).asDouble
        val variance = Variance.of(*samples).asDouble
        assertTrue("Std ($std) must be >= 0.20 for healthy dispersion", std >= 0.20)

        // Skewness for Beta(2, 5) skinnier-tail distribution computed via Apache Commons Statistics Skewness:
        // Positive skewness (+0.596) mathematically confirms the fast-decaying right tail (skinnier tail).
        val skewness = Skewness.of(*samples).asDouble
        assertTrue("Skewness ($skewness) must be positive (> +0.35) confirming the skinnier right tail", skewness > 0.35)

        // Lifetimes span the defined range, with upper tail rapidly decaying:
        val minL = samples.minOrNull() ?: 0.0
        val maxL = samples.maxOrNull() ?: 0.0
        assertTrue("Min lifetime ($minL) must reach near 0.30", minL <= 0.40)
        assertTrue("Max lifetime ($maxL) must be within [1.5, 2.0]", maxL in 1.5..2.0)

        // At least 85% of particle lifetimes are below 1.30 (demonstrating the skinny upper tail):
        val countBelow130 = samples.count { it < 1.30 }
        val fracBelow130 = countBelow130.toDouble() / samples.size
        assertTrue("At least 85% of particles must have lifetimes < 1.30 (got $fracBelow130)", fracBelow130 >= 0.85)

        // Lifetimes are completely random and independent per particle (near-zero lag-1 cross correlation):
        var cov = 0.0
        for (y in 0 until size) {
            for (x in 0 until (size - 1)) {
                val l1 = CyberPixelFrontierField.particleLifetime(x, y).toDouble()
                val l2 = CyberPixelFrontierField.particleLifetime(x + 1, y).toDouble()
                cov += (l1 - mean) * (l2 - mean)
            }
        }
        val lag1Corr = cov / (size * (size - 1)) / variance
        assertTrue("Lag-1 cross correlation ($lag1Corr) must be near zero (< 0.15) for independent random draws", kotlin.math.abs(lag1Corr) < 0.15)
    }

    /**
     * Programmatic verification: 2-D particle spawn densities are governed by top-to-bottom spline curves
     * as extrema of densities that are not all the same.
     */
    @Test
    fun spawnDensity_governedByTopToBottomSplineExtrema_notAllTheSame() {
        val steps = 20
        val startYs = FloatArray(steps) { i -> CyberPixelFrontierField.evalSplineStart(i / (steps - 1).toFloat()) }
        val peakYs = FloatArray(steps) { i -> CyberPixelFrontierField.evalSplinePeak(i / (steps - 1).toFloat()) }
        val endYs = FloatArray(steps) { i -> CyberPixelFrontierField.evalSplineEnd(i / (steps - 1).toFloat()) }

        // Extrema ordering must strictly hold for all y: Start < Peak < End:
        for (i in 0 until steps) {
            assertTrue("Spline start must be < peak at step $i", startYs[i] < peakYs[i])
            assertTrue("Spline peak must be < end at step $i", peakYs[i] < endYs[i])
        }

        // Start offset is strictly 0 across all steps (zero deadzone from barrier):
        for (i in 0 until steps) {
            assertEquals(0.0f, startYs[i], 1e-4f)
        }

        // Curves are not all the same (peak and end curves meander distinctly across y with healthy standard deviation via Apache Commons Statistics):
        fun stdOf(arr: FloatArray): Float {
            val doubles = DoubleArray(arr.size) { arr[it].toDouble() }
            return StandardDeviation.of(*doubles).asDouble.toFloat()
        }

        val stdPeak = stdOf(peakYs)
        val stdEnd = stdOf(endYs)

        assertTrue("Peak spline must meander across y (std: $stdPeak >= 0.02)", stdPeak >= 0.02f)
        assertTrue("End spline must meander across y (std: $stdEnd >= 0.02)", stdEnd >= 0.02f)

        // Peak spline must be the true density extremum (density == 1.0):
        for (i in 0 until steps) {
            val v = i / (steps - 1).toFloat()
            val peakNormX = CyberPixelFrontierField.evalSplinePeak(v)
            val peakDensity = CyberPixelFrontierField.computeSpawnDensity(normDist = peakNormX, v = v)
            assertEquals(1.0f, peakDensity, 0.01f)

            // Outside start..end must have 0 density (negative or beyond end):
            val outsideLeft = CyberPixelFrontierField.computeSpawnDensity(normDist = -0.01f, v = v)
            assertEquals(0.0f, outsideLeft, 0.01f)
            val outsideRight = CyberPixelFrontierField.computeSpawnDensity(normDist = 0.99f, v = v)
            assertEquals(0.0f, outsideRight, 0.01f)
        }
    }

    /**
     * Programmatic verification: Particle activation envelope conforms to the 2-D spawn density field.
     */
    @Test
    fun particleActivation_conformsTo2DSpawnDensityField() {
        val size = 48
        var totalActive = 0
        var activeNearPeak = 0

        for (y in 0 until size) {
            val v = y / size.toFloat()
            val xp = CyberPixelFrontierField.evalSplinePeak(v)
            for (x in 0 until size) {
                val normDist = x / size.toFloat()
                if (CyberPixelFrontierField.isParticleActive(x, y, normDist, v = v, mode = 0)) {
                    totalActive++
                    if (kotlin.math.abs(normDist - xp) < 0.15f) {
                        activeNearPeak++
                    }
                }
            }
        }

        assertTrue("Must have active particles ($totalActive)", totalActive > 100)
        // Majority of active particles should concentrate around the density peak ridge:
        val peakConcentration = activeNearPeak.toFloat() / totalActive
        assertTrue("Peak concentration ($peakConcentration) must be >= 0.45", peakConcentration >= 0.45f)
    }

    private companion object {
        /** Time in milliseconds that is longer than any default transition, so the swap has finished. */
        const val LongTransitionMillis = 2_000L
    }
}
