package com.example.cyberpunkandroid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * Full-screen or container-filling blocking loading overlay with frosted glass styling and centered [CyberSpinner].
 *
 * Imitates the Cybercore CSS `.cyber-spinner-overlay` specification.
 *
 * ### Interactive & Layout Behaviors:
 * - Fills parent bounds and intercepts all touch/pointer input, preventing interactions with underlying UI elements.
 * - Renders a high-opacity translucent backdrop tint with frosted glass aesthetics.
 * - Features a centered dual-ring counter-rotating [CyberSpinner] accompanied by optional uppercase monospace status text.
 * - Supports optional [onDismissRequest] callback triggered when tapping the overlay backdrop.
 *
 * @param modifier Composable modifier applied to the overlay container.
 * @param text Optional loading or progress status copy (e.g. `"INITIALIZING..."`, `"DECRYPTING DATA"`).
 * @param color Primary neon tint color for the centered [CyberSpinner]. Defaults to [CyberTheme.colors.primary].
 * @param onDismissRequest Optional callback invoked when the user taps the overlay backdrop.
 */
@Composable
fun CyberSpinnerOverlay(
    modifier: Modifier = Modifier,
    text: String? = null,
    spinnerSize: androidx.compose.ui.unit.Dp = CyberPrimitives.IconSizes.dp32,
    color: Color = CyberTheme.colors.primary,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onDismissRequest: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.cyberComponentSemantics("CyberSpinnerOverlay", appendedA11y, customA11y)
            .fillMaxSize()
            .background(CyberPrimitives.Colors.Void500.copy(alpha = 0.90f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = if (onDismissRequest != null) Role.Button else null,
                onClick = { onDismissRequest?.invoke() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(CyberPrimitives.Spacing.dp16)
        ) {
            CyberSpinner(color = color, size = spinnerSize)

            if (text != null) {
                Spacer(modifier = Modifier.height(CyberPrimitives.Spacing.dp16))
                Text(
                    text = text.uppercase(),
                    style = CyberTheme.typography.terminal.copy(
                        color = CyberTheme.colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Layout wrapper that conditionally displays a blocking frosted glass [CyberSpinnerOverlay] over child [content].
 *
 * ### Interactive Behaviors:
 * - Wraps underlying composable [content] in a relative layout container.
 * - Smoothly fades [CyberSpinnerOverlay] in and out using [AnimatedVisibility] based on [visible].
 * - When [visible] is `true`, all touch/pointer gestures to [content] are blocked.
 *
 * @param visible Controls whether the loading overlay is currently active and visible.
 * @param modifier Modifier applied to the outer layout container.
 * @param text Optional monospace status description displayed beneath the spinner.
 * @param color Primary neon tint color for the centered spinner. Defaults to [CyberTheme.colors.primary].
 * @param onDismissRequest Optional callback invoked when tapping the backdrop.
 * @param content Base composable content obscured by the loading overlay while active.
 */
@Composable
fun CyberSpinnerOverlay(
    visible: Boolean,
    text: String? = null,
    modifier: Modifier = Modifier,
    spinnerSize: androidx.compose.ui.unit.Dp = CyberPrimitives.IconSizes.dp32,
    enter: androidx.compose.animation.EnterTransition = fadeIn(),
    exit: androidx.compose.animation.ExitTransition = fadeOut(),
    color: Color = CyberTheme.colors.primary,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.cyberComponentSemantics("CyberSpinnerOverlay", appendedA11y, customA11y)) {
        content()

        AnimatedVisibility(
            visible = visible,
            enter = enter,
            exit = exit
        ) {
            CyberSpinnerOverlay(
                text = text,
                color = color,
                spinnerSize = spinnerSize,
                onDismissRequest = onDismissRequest,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
