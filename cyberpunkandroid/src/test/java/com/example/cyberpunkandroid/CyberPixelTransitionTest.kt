package com.example.cyberpunkandroid

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.components.CyberPixelTransition
import com.example.cyberpunkandroid.components.CyberPixelTransitionState
import com.example.cyberpunkandroid.components.computePixelateStep
import com.example.cyberpunkandroid.components.pixelTileCovered
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Checks that a pixel veil fully covers, clears, and never re-covers tiles mid-reveal. */
@RunWith(AndroidJUnit4::class)
class CyberPixelTransitionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Use this function to verify deterministic tile coverage across animation endpoints and progress. */
    @Test
    fun pixelCoverageClearsMonotonically() {
        for (row in 0..12) {
            for (column in 0..8) {
                assertTrue(pixelTileCovered(column, row, 1f))
                assertFalse(pixelTileCovered(column, row, 0f))
                if (pixelTileCovered(column, row, 0.25f)) {
                    assertTrue(pixelTileCovered(column, row, 0.75f))
                }
            }
        }
    }

    /** Verifies that pixelate step scales monotonically and clamps to minimum 1px. */
    @Test
    fun computePixelateStep_scalesAndClamps() {
        assertEquals(24f, computePixelateStep(pixelSizePx = 24f, coverage = 1f), 0.001f)
        assertEquals(12f, computePixelateStep(pixelSizePx = 24f, coverage = 0.5f), 0.001f)
        assertEquals(1f, computePixelateStep(pixelSizePx = 24f, coverage = 0f), 0.001f)
        assertEquals(1f, computePixelateStep(pixelSizePx = 0.5f, coverage = 0.5f), 0.001f)
    }

    /** Verifies that transition state manages coverage and allows immediate snapping. */
    @Test
    fun transitionState_managesCoverage() = runBlocking {
        val state = CyberPixelTransitionState(initialCoverage = 1f)
        assertEquals(1f, state.coverage, 0.001f)
        state.snapTo(0.4f)
        assertEquals(0.4f, state.coverage, 0.001f)
        state.snapTo(0f)
        assertEquals(0f, state.coverage, 0.001f)
    }

    /** Verifies that composable renders content successfully within Compose hierarchy. */
    @Test
    fun composable_rendersContent() {
        composeTestRule.setContent {
            CyberPixelTransition(targetKey = "key_1") {
                Text("cyber_terminal")
            }
        }
        composeTestRule.onNodeWithText("cyber_terminal").assertExists()
    }
}
