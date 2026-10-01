package com.example.cyberpunkandroid.effects

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Electrical sparking effect component displaying popcorn energy sparks with parabolic downward gravity arcs,
 * wide upward burst launch, plasma colorscale interpolation, and a small ember core that shrinks to nothing over its lifetime.
 *
 * @param modifier Composable modifier applied to the spark container.
 * @param sparkCount Number of spark rays rendered. Defaults to `32`.
 * @param intensity Brightness and energy length multiplier.
 * @param speed Frequency multiplier for particle movement.
 * @param color Primary spark color. Defaults to [CyberTheme.colors.primary].
 * @param secondaryColor Secondary plasma color. Defaults to [CyberTheme.colors.secondary].
 * @param warningColor Warning/Caution color for initial birth flash. Defaults to [CyberTheme.semantics.colors.warning].
 * @param appendedA11y Optional text to append to accessibility description.
 * @param customA11y Optional text to override accessibility description.
 * @param content Optional content slot rendered within the sparking container.
 */
@Composable
fun CyberSpark(
    modifier: Modifier = Modifier,
    sparkCount: Int = 32,
    intensity: Float = 1.0f,
    speed: Float = 1.0f,
    color: Color = CyberTheme.colors.primary,
    secondaryColor: Color = CyberTheme.colors.secondary,
    warningColor: Color = CyberTheme.semantics.colors.warning,
    appendedA11y: String? = null,
    customA11y: String? = null,
    content: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier.cyberSpark(
            color = color,
            secondaryColor = secondaryColor,
            warningColor = warningColor,
            sparkCount = sparkCount,
            intensity = intensity,
            speed = speed,
            appendedA11y = appendedA11y,
            customA11y = customA11y
        ),
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        }
    }
}
