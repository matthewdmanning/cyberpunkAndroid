package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives

/**
 * Holds the previous key between compositions so that [rememberCyberChangePulse] can compare old and new values.
 *
 * @param value The last key that the pulse saw.
 */
private class CyberChangeMemory<T>(var value: T)

/**
 * Returns a one-shot change envelope. The value is `0f` at rest. When [key] changes and [shouldPulse] accepts the
 * change, the value jumps to `1f` and decays back to `0f` with [animationSpec].
 *
 * Use it to draw attention to data that has just updated (a new reading, an increased count) without a
 * continuous animation. Read the returned state only in the draw or layout phase (for example through
 * [cyberChangeFlash] or [cyberChangeBump]) so that the decay does not recompose the caller.
 *
 * The first composition never pulses: a value that is already present when the screen opens is not a change.
 *
 * @param key The observed data. A new value starts a pulse when [shouldPulse] returns `true`.
 * @param animationSpec Decay from `1f` to `0f`. Defaults to a 500 ms out-expo decay: a fast attack and a soft tail,
 *   which stays non-blocking because the data itself is already on screen.
 * @param shouldPulse Decides whether a change from the old key to the new key starts a pulse. The default
 *   pulses on every change. Use, for example, `{ old, new -> new > old }` to pulse only on increase.
 * @return State whose value is the current envelope level, from `0f` (rest) to `1f` (peak).
 */
@Composable
fun <T> rememberCyberChangePulse(
    key: T,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = CyberPrimitives.Durations.ms500,
        easing = CyberConfig.Easings.OutExpoEasing
    ),
    shouldPulse: (old: T, new: T) -> Boolean = { old, new -> old != new },
): State<Float> {
    val envelope = remember { Animatable(0f) }
    val memory = remember { CyberChangeMemory(key) }
    val currentSpec by rememberUpdatedState(animationSpec)
    val currentShouldPulse by rememberUpdatedState(shouldPulse)

    LaunchedEffect(key) {
        val previous = memory.value
        memory.value = key
        if (previous == key || !currentShouldPulse(previous, key)) return@LaunchedEffect
        envelope.snapTo(1f)
        envelope.animateTo(0f, currentSpec)
    }
    return envelope.asState()
}

/**
 * Draws a flat tint behind the content whose opacity follows [pulse]. At rest ([pulse] = `0f`) it draws nothing.
 *
 * Visual: a solid [color] fill in [shape], peaking at [maxAlpha] and fading out. It sits behind the content, so
 * text and icons stay fully readable during the flash. Pair it with [rememberCyberChangePulse] to mark a value
 * that has just changed.
 *
 * [pulse] is read only in the draw phase, so the decay redraws the element without recomposition or relayout.
 *
 * @param pulse Supplies the current envelope level, `0f` to `1f`.
 * @param color Tint color. Choose the role that the change represents (for example the primary accent for new
 *   data, or a status role color for a status change).
 * @param shape Outline of the tint. Match the shape of the element's background.
 * @param maxAlpha Tint opacity at the peak of the pulse, `0f` to `1f`.
 * @param appendedA11y Optional accessibility label. The flash is decorative, so no label is applied unless one
 *   of the accessibility parameters is supplied.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
fun Modifier.cyberChangeFlash(
    pulse: () -> Float,
    color: Color,
    shape: Shape = RectangleShape,
    maxAlpha: Float = CyberConfig.Effects.ChangeFlashAlpha,
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .cyberComponentSemantics("CyberChangeFlash", appendedA11y, customA11y)
    .drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        onDrawBehind {
            val level = pulse().coerceIn(0f, 1f)
            if (level > 0f) drawOutline(outline, color = color, alpha = level * maxAlpha)
        }
    }

/**
 * Scales the element up briefly while [pulse] is above zero, then settles back to its normal size.
 *
 * Visual: a single "bump" about the element's center. At the peak the element is `1 + scaleDelta` times its
 * size. Layout size does not change, so neighbors do not move. Use it on small indicators (a count badge) where
 * a tint alone would be too small to notice.
 *
 * [pulse] is read only in the layer block, so the bump does not recompose or relayout the element.
 *
 * @param pulse Supplies the current envelope level, `0f` to `1f`.
 * @param scaleDelta Extra scale at the peak. `0.15f` means 15 % larger.
 * @param appendedA11y Optional accessibility label. The bump is decorative, so no label is applied unless one
 *   of the accessibility parameters is supplied.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
fun Modifier.cyberChangeBump(
    pulse: () -> Float,
    scaleDelta: Float = CyberConfig.Effects.ChangeBumpScale,
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .cyberComponentSemantics("CyberChangeBump", appendedA11y, customA11y)
    .graphicsLayer {
        val scale = 1f + pulse().coerceIn(0f, 1f) * scaleDelta
        scaleX = scale
        scaleY = scale
    }
