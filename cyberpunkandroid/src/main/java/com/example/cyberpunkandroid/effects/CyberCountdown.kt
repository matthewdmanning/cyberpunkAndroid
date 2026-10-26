package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlin.math.roundToInt

/**
 * Holds the restart key that the countdown has already applied, so a new key resets the countdown once.
 *
 * @param key The last restart key that was applied.
 */
private class CyberCountdownMemory(var key: Any?)

/**
 * Returns the remaining fraction of a pausable countdown, from `1f` (full time left) to `0f` (expired).
 *
 * The countdown runs while [running] is `true` and holds its value while [running] is `false`; it resumes from
 * the held value. When it reaches `0f`, [onTimeout] is called once. A new [restartKey] resets it to `1f`.
 *
 * Use it to drive a [com.example.cyberpunkandroid.components.CyberTimeoutBar], to auto-dismiss a
 * caller-controlled [com.example.cyberpunkandroid.components.CyberSnackbar], or to time any other expiring state.
 * Pause it while the user interacts with the element it times (for example while a notice has focus).
 *
 * Read the returned state only in the draw phase (as [com.example.cyberpunkandroid.components.CyberTimeoutBar]
 * does) to avoid recomposing the caller on every frame.
 *
 * @param durationMillis Total countdown length, in milliseconds.
 * @param running `true` to count down, `false` to pause.
 * @param restartKey Any value. When it changes, the countdown resets to full and starts again (if [running]).
 * @param easing Shape of the countdown over time. The default is linear, so the fraction is proportional to the
 *   remaining time.
 * @param onTimeout Called once when the countdown reaches zero.
 * @return State whose value is the remaining fraction, `0f` to `1f`.
 */
@Composable
fun rememberCyberCountdown(
    durationMillis: Int,
    running: Boolean = true,
    restartKey: Any? = Unit,
    easing: Easing = LinearEasing,
    onTimeout: () -> Unit = {},
): State<Float> {
    val remaining = remember { Animatable(1f) }
    val memory = remember { CyberCountdownMemory(restartKey) }
    val currentOnTimeout by rememberUpdatedState(onTimeout)

    LaunchedEffect(running, restartKey, durationMillis, easing) {
        if (memory.key != restartKey) {
            memory.key = restartKey
            remaining.snapTo(1f)
        }
        if (!running || remaining.value <= 0f) return@LaunchedEffect
        // Resume from the held fraction, so a pause does not extend or shorten the total time.
        val millisLeft = (remaining.value * durationMillis).roundToInt()
        remaining.animateTo(0f, tween(durationMillis = millisLeft, easing = easing))
        currentOnTimeout()
    }
    return remaining.asState()
}
