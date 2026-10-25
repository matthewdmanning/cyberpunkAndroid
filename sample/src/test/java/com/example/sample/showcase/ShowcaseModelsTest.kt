package com.example.sample.showcase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Floating-point tolerance for model assertions. */
private const val TOLERANCE = 1e-4f

/** A time in seconds that is long enough for any placeholder rate to saturate the score. */
private const val LONG_DWELL_SECONDS = 1_000f

/**
 * JVM tests for the pure simulation models behind showcase cards 6 to 8.
 *
 * These tests check model behavior only. They do not render Compose UI.
 */
class ShowcaseModelsTest {

    /** The bus frame has one intensity per light-pipe, each in 0–1. */
    @Test
    fun busFrame_hasOneBoundedIntensityPerPipe() {
        val frame = busDiagnosticFrameAt(timeSeconds = 1.3f)
        assertEquals(BusDiagnosticSpec.LIGHT_PIPE_COUNT, frame.pipeIntensities.size)
        assertTrue(frame.pipeIntensities.all { it in 0f..1f })
        assertTrue(frame.centroidThread in 0 until BusDiagnosticSpec.LIGHT_PIPE_COUNT)
    }

    /** The load centroid moves over time, which is the "compute drift" state. */
    @Test
    fun busFrame_centroidDrifts() {
        val quarterCycle = BusDiagnosticSpec.DRIFT_CYCLE_SECONDS / 4f
        assertNotEquals(busDiagnosticFrameAt(0f).centroidThread, busDiagnosticFrameAt(quarterCycle).centroidThread)
    }

    /** A current spike starts at the dossier peak and decays, and never exceeds the rail maximum. */
    @Test
    fun busFrame_currentSpikesDecayFromPeak() {
        val atSpike = busDiagnosticFrameAt(0f).railCurrentAmps
        val afterDecay = busDiagnosticFrameAt(BusDiagnosticSpec.SPIKE_DECAY_SECONDS).railCurrentAmps
        assertEquals(BusDiagnosticSpec.COMBAT_PEAK_CURRENT_AMPS, atSpike, TOLERANCE)
        assertTrue(afterDecay < atSpike)
        assertTrue(atSpike <= BusDiagnosticSpec.MAX_SPIKE_CURRENT_AMPS)
    }

    /** The score follows f(tier) × (1 − stealth) × dwell from the dossier. */
    @Test
    fun iceScore_followsDossierEquation() {
        val state = IceMatrixState(tier = PenetrationTier.MemorySiphon, stealthEngaged = true, dwellSeconds = 10f)
        val expected = PenetrationTier.MemorySiphon.placeholderGrowthPercentPerSecond *
            (1f - IceMatrixSpec.PLACEHOLDER_STEALTH_EFFICIENCY) * 10f
        assertEquals(expected, state.scorePercent, TOLERANCE)
    }

    /** Stealth lowers the score for the same depth and dwell time. */
    @Test
    fun iceScore_stealthLowersScore() {
        val masked = IceMatrixState(tier = PenetrationTier.CatalogSearch, stealthEngaged = true, dwellSeconds = 20f)
        assertTrue(masked.scorePercent < masked.copy(stealthEngaged = false).scorePercent)
    }

    /** The trace locks only above the dossier threshold, and the score clamps at 100%. */
    @Test
    fun iceScore_locksAboveThresholdAndClamps() {
        val saturated = IceMatrixState(tier = PenetrationTier.RootExecution, stealthEngaged = false)
            .advancedBy(LONG_DWELL_SECONDS)
        assertEquals(IceMatrixSpec.MAX_SCORE_PERCENT, saturated.scorePercent, TOLERANCE)
        assertTrue(saturated.traceLocked)
        assertEquals(IceSirenState.Klaxon, saturated.sirenState)
        assertEquals(IceMatrixSpec.ICE_BAR_BLOCKS, saturated.litBlocks)
        assertEquals(IceClass.Black, saturated.activeIceClass)
        assertFalse(IceMatrixState().traceLocked)
    }

    /** A purge cuts the line and zeroes the score. A reconnect restores the default state. */
    @Test
    fun icePurge_cutsLineAndReconnectRestores() {
        val locked = IceMatrixState(tier = PenetrationTier.RootExecution).advancedBy(LONG_DWELL_SECONDS)
        val purged = locked.purged()
        assertFalse(purged.connected)
        assertEquals(0f, purged.scorePercent, TOLERANCE)
        assertEquals(purged, purged.advancedBy(LONG_DWELL_SECONDS))
        assertEquals(IceMatrixState(), purged.reconnected())
    }

    /** Block numbers map to the dossier ICE class ranges 1–8, 9–15, and 16–20. */
    @Test
    fun iceBlocks_mapToDossierClasses() {
        assertEquals(IceClass.White, iceClassOfBlock(IceMatrixSpec.WHITE_ICE_LAST_BLOCK))
        assertEquals(IceClass.Grey, iceClassOfBlock(IceMatrixSpec.WHITE_ICE_LAST_BLOCK + 1))
        assertEquals(IceClass.Grey, iceClassOfBlock(IceMatrixSpec.GREY_ICE_LAST_BLOCK))
        assertEquals(IceClass.Black, iceClassOfBlock(IceMatrixSpec.GREY_ICE_LAST_BLOCK + 1))
    }

    /** Every trace has the configured point count, and every point fits the screen. */
    @Test
    fun bioTrace_isBoundedForEveryStage() {
        BioStage.entries.forEach { stage ->
            val trace = bioTraceAt(stage, timeSeconds = 2.7f)
            assertEquals(BioMonitorSpec.TRACE_POINTS, trace.size)
            assertTrue("$stage out of range", trace.all { it in 0f..1f })
        }
    }

    /** Fibrillation swings over more of the screen than a calm patrol trace. */
    @Test
    fun bioTrace_fibrillationIsWiderThanPatrol() {
        fun span(stage: BioStage) = bioTraceAt(stage, 2.7f).let { it.max() - it.min() }
        assertTrue(span(BioStage.Fibrillation) > span(BioStage.Patrol))
    }

    /** The normal beat has its R-wave peak near the top of the signal scale. */
    @Test
    fun bioSignal_patrolHasRWavePeak() {
        val period = 60f / BioStage.Patrol.beatsPerMinute!!
        val rWavePhase = 0.4f
        assertTrue(bioSignalAt(BioStage.Patrol, period * rWavePhase) > 0.9f)
    }

    /** A cold-jack overrides any threat, stops the sparks, and a re-jack resets to patrol. */
    @Test
    fun bioState_coldJackIsolatesAndReJackResets() {
        val striking = BioMonitorState().withThreat(BioStage.Fibrillation)
        assertTrue(striking.sparksActive)
        val jacked = striking.coldJack()
        assertEquals(BioStage.Stabilized, jacked.shownStage)
        assertFalse(jacked.sparksActive)
        assertEquals(BioStage.Stabilized, jacked.withThreat(BioStage.Stress).shownStage)
        assertEquals(BioMonitorState(), jacked.reJack())
    }
}
