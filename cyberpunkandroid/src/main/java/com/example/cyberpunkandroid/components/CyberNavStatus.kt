package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * Sci-fi status indicator chip with pulsating LED beacon and semantic green monospace typography.
 *
 * Imitates the `.cyber-status` specification.
 *
 * @param text Status label (e.g. `"SYS_OK"`, `"ONLINE"`).
 * @param modifier Composable modifier for badge placement.
 * @param beaconAnimationSpec Infinite repeatable animation spec for the LED beacon pulsation.
 * @param appendedA11y Optional text to append to accessibility description.
 * @param customA11y Optional text to override accessibility description.
 */
@Composable
fun CyberNavStatus(
    text: String,
    modifier: Modifier = Modifier,
    beaconAnimationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(CyberPrimitives.Durations.ms500 * 2, easing = CyberConfig.Easings.OutExpoEasing),
        repeatMode = RepeatMode.Reverse
    ),
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NavStatusBeaconTransition")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = beaconAnimationSpec,
        label = "NavStatusBeaconAlpha"
    )

    Row(
        modifier = modifier.cyberComponentSemantics("CyberNavStatus", appendedA11y, customA11y)
            .border(
                width = CyberPrimitives.BorderWidths.dp1,
                color = CyberPrimitives.Colors.Green500
            )
            .background(CyberPrimitives.Colors.Green500.copy(alpha = 0.15f))
            .padding(
                horizontal = CyberPrimitives.Spacing.dp8,
                vertical = CyberPrimitives.Spacing.dp4
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp4)
    ) {
        // Pulsating LED beacon
        Box(
            modifier = Modifier
                .size(CyberPrimitives.Spacing.dp8)
                .clip(CircleShape)
                .background(CyberPrimitives.Colors.Green500.copy(alpha = dotAlpha))
        )

        Text(
            text = text.uppercase(),
            style = CyberTheme.typography.terminal.copy(
                color = CyberPrimitives.Colors.Green500
            )
        )
    }
}
