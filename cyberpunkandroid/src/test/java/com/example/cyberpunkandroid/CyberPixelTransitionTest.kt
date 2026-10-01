package com.example.cyberpunkandroid

import com.example.cyberpunkandroid.components.pixelTileCovered
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks that a pixel veil fully covers, clears, and never re-covers tiles mid-reveal. */
class CyberPixelTransitionTest {
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
}
