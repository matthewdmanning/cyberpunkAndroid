package com.example.cyberpunkandroid.effects

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

/**
 * Core engine for data-driven cyberpunk UI effects.
 * Processes raw [telemetry] through a mathematical [operation] at a specific [updateFrequencyMs].
 *
 * This architecture treats UI effects as continuous state updates rather than static mapped values.
 * It allows for:
 * 1. Simple Mapping: `operation = { _, t -> t * 2 }`
 * 2. Temporal Smoothing: `operation = { c, t -> c + (t - c) * 0.1f }`
 * 3. Continuous Addition (e.g. Rotation): `operation = { c, t -> c + t }`
 * 4. Capacitance/Decay: `operation = { c, t -> (c + t) * 0.95f }`
 */
@Composable
fun <T, R> rememberDrivenState(
    telemetry: T,
    initialState: R,
    updateFrequencyMs: Long = 16L, // Defaults to ~60Hz
    operation: (currentState: R, telemetryValue: T) -> R
): State<R> {
    val state = remember { mutableStateOf(initialState) }
    
    val currentTelemetry by rememberUpdatedState(telemetry)
    val currentOperation by rememberUpdatedState(operation)

    LaunchedEffect(updateFrequencyMs) {
        while (true) {
            state.value = currentOperation(state.value, currentTelemetry)
            delay(updateFrequencyMs)
        }
    }
    
    return state
}

/**
 * Float-optimized telemetry processor for high-performance animation parameters
 * like rotation, scale, or alpha, avoiding autoboxing overhead.
  * @param telemetry TODO: document this
  * @param initialState TODO: document this
  * @param updateFrequencyMs TODO: document this
 */
@Composable
fun rememberDrivenFloatState(
    telemetry: Float,
    initialState: Float = 0f,
    updateFrequencyMs: Long = 16L,
    operation: (currentState: Float, telemetryValue: Float) -> Float
): State<Float> {
    val state = remember { mutableFloatStateOf(initialState) }
    
    val currentTelemetry by rememberUpdatedState(telemetry)
    val currentOperation by rememberUpdatedState(operation)

    LaunchedEffect(updateFrequencyMs) {
        while (true) {
            state.floatValue = currentOperation(state.floatValue, currentTelemetry)
            delay(updateFrequencyMs)
        }
    }
    
    return state
}
