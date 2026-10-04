package com.example.cyberpunkandroid.effects

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * Defines the interaction states that can trigger a Cyberpunk UI effect.
 */
enum class CyberInteractionTrigger {
    ALWAYS,
    HOVER,
    PRESS,
    FOCUS,
    NONE
}

/**
 * Resolves a [CyberInteractionTrigger] against an [InteractionSource] to determine 
 * if the effect should currently be active.
 */
@Composable
fun CyberInteractionTrigger.isActive(interactionSource: InteractionSource?): Boolean {
    if (this == CyberInteractionTrigger.ALWAYS) return true
    if (this == CyberInteractionTrigger.NONE) return false
    
    // If an interaction source is required but missing, we fall back to false.
    if (interactionSource == null) return false

    return when (this) {
        CyberInteractionTrigger.HOVER -> interactionSource.collectIsHoveredAsState().value
        CyberInteractionTrigger.PRESS -> interactionSource.collectIsPressedAsState().value
        CyberInteractionTrigger.FOCUS -> interactionSource.collectIsFocusedAsState().value
        else -> true
    }
}
