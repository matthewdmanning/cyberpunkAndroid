package com.example.sample.showcase

import android.provider.Settings
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.TimeUnit

/** Default system animator duration scale, used when the setting has no stored value. */
private const val DEFAULT_ANIMATOR_DURATION_SCALE = 1f

/** Animator scale value that means the user has turned animations off. */
private const val ANIMATIONS_OFF_SCALE = 0f

/**
 * Reads the system "Animator duration scale" developer and accessibility setting.
 *
 * The showcase cards use this as their motion switch. When the user sets
 * "Remove animations" (scale 0), the cards stop their ambient motion and show
 * a static frame. This keeps the cards in line with WCAG 2.2.2 (Pause, Stop, Hide).
 *
 * @return `true` when the system lets animations play, `false` when the user turned them off.
 */
@Composable
fun rememberSystemMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            DEFAULT_ANIMATOR_DURATION_SCALE,
        ) > ANIMATIONS_OFF_SCALE
    }
}

/**
 * Supplies a frame-synchronized clock in seconds for draw-time simulations.
 *
 * The clock advances once per display frame through [withInfiniteAnimationFrameNanos].
 * That API marks the loop as an infinite animation, so test runners and other
 * infinite-animation policies can pause it. The clock stops advancing while
 * [running] is `false`, and it keeps its last value so the card shows a stable frame.
 *
 * @param running `true` to advance the clock each frame. `false` to hold the current value.
 * @return A [State] that holds the seconds that elapsed while the clock was running.
 */
@Composable
fun rememberShowcaseClockSeconds(running: Boolean): State<Float> {
    val seconds = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var lastFrameNanos = withInfiniteAnimationFrameNanos { it }
        while (true) {
            withInfiniteAnimationFrameNanos { frameNanos ->
                val deltaNanos = frameNanos - lastFrameNanos
                lastFrameNanos = frameNanos
                seconds.floatValue += deltaNanos.toFloat() / TimeUnit.SECONDS.toNanos(1)
            }
        }
    }
    return seconds
}
