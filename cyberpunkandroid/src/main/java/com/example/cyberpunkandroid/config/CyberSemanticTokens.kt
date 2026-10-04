package com.example.cyberpunkandroid.config

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// TODO: document this
@Immutable
data class CyberSemanticColors(
    val caution: Color = CyberPrimitives.Colors.Yellow500,
    val warning: Color = CyberPrimitives.Colors.Yellow500,
    val danger: Color = CyberPrimitives.Colors.Magenta500,
    val error: Color = CyberPrimitives.Colors.Magenta500,
    val success: Color = CyberPrimitives.Colors.Green500,
    val info: Color = CyberPrimitives.Colors.Cyan500,
    val terminal: Color = CyberPrimitives.Colors.Cyan500
)

// TODO: document this
@Immutable
data class CyberSemanticDurations(
    val warningPulse: Int = CyberPrimitives.Durations.ms500
)

// TODO: document this
@Immutable
data class CyberSemanticTokens(
    val colors: CyberSemanticColors = CyberSemanticColors(),
    val durations: CyberSemanticDurations = CyberSemanticDurations()
)
