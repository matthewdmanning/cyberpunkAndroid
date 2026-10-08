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
import com.example.cyberpunkandroid.effects.CyberShaders
import org.junit.Assert.assertEquals
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

    private companion object {
        /** Time in milliseconds that is longer than any default transition, so the swap has finished. */
        const val LongTransitionMillis = 2_000L
    }
}
