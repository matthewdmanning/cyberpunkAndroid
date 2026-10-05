package com.example.cyberpunkandroid.effects

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

internal sealed class CyberEffectData {
    
    // We can add data classes here for each effect
    
    data class Overload(
        val intensity: Float,
        val timeScale: Float,
        val bounceAmount: Dp
    ) : CyberEffectData()
    
    // Scanlines is already migrated but we can move it here later if we want consistency
}
