package com.example.cyberpunkandroid

import com.example.cyberpunkandroid.components.biometricValueAt
import org.junit.Assert.assertEquals
import org.junit.Test

class CyberBiometricsTest {
    @Test
    fun waveformScrollInterpolatesAndWraps() {
        val data = listOf(0f, 1f, 0.5f)
        assertEquals(0.5f, biometricValueAt(data, 0.5f), 0.0001f)
        assertEquals(0.25f, biometricValueAt(data, 2.5f), 0.0001f)
        assertEquals(0.5f, biometricValueAt(data, 3.5f), 0.0001f)
    }

    @Test
    fun waveformRejectsInvalidReadings() {
        assertEquals(0f, biometricValueAt(emptyList(), 2f), 0.0001f)
        assertEquals(0.5f, biometricValueAt(listOf(Float.NaN, 1f), 0.5f), 0.0001f)
        assertEquals(1f, biometricValueAt(listOf(2f), 0f), 0.0001f)
    }
}
