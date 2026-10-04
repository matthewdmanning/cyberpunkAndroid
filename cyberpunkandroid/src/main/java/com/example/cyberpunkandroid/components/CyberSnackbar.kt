package com.example.cyberpunkandroid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberOverload
import com.example.cyberpunkandroid.effects.cyberScanlines
import com.example.cyberpunkandroid.effects.cyberTextGlow
import com.example.cyberpunkandroid.theme.CyberTheme

/** Screen edge from which a [CyberSnackbar] enters and exits. */
enum class CyberSnackbarEdge {
    Top,
    Bottom,
    Start,
    End,
}

/**
 * Caller-controlled transient alert that keeps [CyberAlert]'s visual language while adding edge motion and CRT scanlines.
 *
 * The component does not dismiss itself. Keep [visible] in the caller's state and set it to false when the notification
 * has been acknowledged or its lifecycle has ended.
  * @param title TODO: document this
  * @param message TODO: document this
  * @param visible TODO: document this
  * @param modifier TODO: document this
  * @param critical TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
@Composable
fun CyberSnackbar(
    title: String,
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    variant: CyberAlertVariant = CyberAlertVariant.Info,
    edge: CyberSnackbarEdge = CyberSnackbarEdge.Bottom,
    critical: Boolean = variant == CyberAlertVariant.Error,
    enterAnimationSpec: FiniteAnimationSpec<IntOffset> = spring(),
    exitAnimationSpec: FiniteAnimationSpec<IntOffset> = spring(),
    pulseAnimationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(CyberPrimitives.Durations.ms500),
        repeatMode = RepeatMode.Reverse,
    ),
    scanlinesAnimationSpec: AnimationSpec<Float> = spring(),
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val enter = edge.enter(enterAnimationSpec)
    val exit = edge.exit(exitAnimationSpec)
    val glowColor = CyberTheme.semantics.colors.error
    val pulse = if (critical) {
        rememberInfiniteTransition(label = "CyberSnackbarPulse").animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = pulseAnimationSpec,
            label = "CyberSnackbarCriticalGlow",
        ).value
    } else {
        1f
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
    ) {
        CyberAlert(
            title = title,
            message = message,
            modifier = Modifier
                .cyberScanlines(animationSpec = scanlinesAnimationSpec)
                .cyberOverload(enabled = critical)
                .then(
                    if (critical) {
                        Modifier.cyberTextGlow(
                            color = glowColor,
                            intensity = pulse,
                            appendedA11y = appendedA11y,
                            customA11y = customA11y,
                        )
                    } else {
                        Modifier
                    },
                ),
            variant = variant,
            appendedA11y = appendedA11y,
            customA11y = customA11y,
        )
    }
}

/**
 * Alias with toast terminology for callers that use a toast host.
 * @param title TODO: document this
 * @param message TODO: document this
 * @param visible TODO: document this
 * @param modifier TODO: document this
 * @param critical TODO: document this
 * @param appendedA11y TODO: document this
 * @param customA11y TODO: document this
 */
@Composable
fun CyberToast(
    title: String,
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    variant: CyberAlertVariant = CyberAlertVariant.Info,
    edge: CyberSnackbarEdge = CyberSnackbarEdge.Bottom,
    critical: Boolean = variant == CyberAlertVariant.Error,
    enterAnimationSpec: FiniteAnimationSpec<IntOffset> = spring(),
    exitAnimationSpec: FiniteAnimationSpec<IntOffset> = spring(),
    pulseAnimationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(CyberPrimitives.Durations.ms500),
        repeatMode = RepeatMode.Reverse,
    ),
    scanlinesAnimationSpec: AnimationSpec<Float> = spring(),
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    CyberSnackbar(
        title = title,
        message = message,
        visible = visible,
        modifier = modifier,
        variant = variant,
        edge = edge,
        critical = critical,
        enterAnimationSpec = enterAnimationSpec,
        exitAnimationSpec = exitAnimationSpec,
        pulseAnimationSpec = pulseAnimationSpec,
        scanlinesAnimationSpec = scanlinesAnimationSpec,
        appendedA11y = appendedA11y,
        customA11y = customA11y,
    )
}

private fun CyberSnackbarEdge.enter(spec: FiniteAnimationSpec<IntOffset>): EnterTransition = when (this) {
    CyberSnackbarEdge.Top -> slideInVertically(animationSpec = spec) { -it }
    CyberSnackbarEdge.Bottom -> slideInVertically(animationSpec = spec) { it }
    CyberSnackbarEdge.Start -> slideInHorizontally(animationSpec = spec) { -it }
    CyberSnackbarEdge.End -> slideInHorizontally(animationSpec = spec) { it }
}

private fun CyberSnackbarEdge.exit(spec: FiniteAnimationSpec<IntOffset>): ExitTransition = when (this) {
    CyberSnackbarEdge.Top -> slideOutVertically(animationSpec = spec) { -it }
    CyberSnackbarEdge.Bottom -> slideOutVertically(animationSpec = spec) { it }
    CyberSnackbarEdge.Start -> slideOutHorizontally(animationSpec = spec) { -it }
    CyberSnackbarEdge.End -> slideOutHorizontally(animationSpec = spec) { it }
}
