package com.example.sample.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberBadgeVariant
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberButtonSize
import com.example.cyberpunkandroid.components.CyberButtonStyle
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberTextGlow
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Full-screen Cyberpunk Splash & Boot Screen.
 *
 * Displays an unhurried, atmospheric boot sequence with AGSL runtime telemetry.
 * Automatically initiates the [com.example.cyberpunkandroid.components.CyberPixelFrontierTransition]
 * after an unhurried boot duration (1200ms), or immediately on tap.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onEnterCatalogue: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2500L)
        onEnterCatalogue()
    }

    val insets = WindowInsets.systemBars.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberTheme.colors.background)
            .padding(
                start = CyberTheme.spacing.dp24,
                end = CyberTheme.spacing.dp24,
                top = insets.calculateTopPadding() + CyberTheme.spacing.dp24,
                bottom = insets.calculateBottomPadding() + CyberTheme.spacing.dp24
            )
    ) {
        // Top telemetry header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEON_DECK // OS v2.4",
                style = CyberTheme.typography.terminal,
                color = CyberTheme.colors.textSecondary
            )
            CyberBadge(
                text = "SYS OK",
                variant = CyberBadgeVariant.Outline
            )
        }

        val cyberPurple = androidx.compose.ui.graphics.Color(0xFFB026FF)

        // Center Hero Block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp24)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
            ) {
                Text(
                    text = "CYBERPUNK",
                    style = CyberTheme.typography.display,
                    color = cyberPurple,
                    modifier = Modifier.cyberTextGlow(
                        color = cyberPurple,
                        radius = 16.dp,
                        intensity = 2.5f
                    )
                )
                Text(
                    text = "ANDROID UI SYSTEM",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.textPrimary
                )
            }

            // Terminal Diagnostic Buffer
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CyberTheme.spacing.dp16),
                    verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
                ) {
                    Text(
                        text = ">>> INITIALIZING HARDWARE AGSL RUNTIME...",
                        style = CyberTheme.typography.terminal,
                        color = cyberPurple
                    )
                    Text(
                        text = ">>> PIXEL FRONTIER: 1dp MICRO-PIXEL MATRIX",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )
                    Text(
                        text = ">>> VECTOR: RIGHT-TO-LEFT // PURPLE SWEEP",
                        style = CyberTheme.typography.terminal,
                        color = cyberPurple
                    )
                    Text(
                        text = ">>> DISPLAY MATRIX: 1080x2400 @ 90Hz",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )
                    Text(
                        text = ">>> AUTO-LAUNCHING UI CATALOGUE...",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.semantics.colors.warning
                    )
                }
            }

            // Primary Enter Action (or instant skip)
            CyberButton(
                modifier = Modifier.fillMaxWidth(),
                style = CyberButtonStyle.Primary,
                size = CyberButtonSize.Large,
                onClick = onEnterCatalogue
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp12),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Terminal,
                        contentDescription = null,
                        size = CyberPrimitives.IconSizes.dp24,
                        tint = LocalContentColor.current
                    )
                    Text(
                        text = "INITIALIZE INTERFACE // ENTER",
                        style = CyberTheme.typography.terminal
                    )
                }
            }

            Text(
                text = "AUTO-BOOTING IN 2.5s • TAP TO SKIP",
                style = CyberTheme.typography.body,
                color = CyberTheme.colors.textSecondary
            )
        }

        // Bottom Footer
        Text(
            text = "HARDWARE ACCELERATED • ZERO GAP COHERENCE • DUAL-LAYER AGSL",
            modifier = Modifier.align(Alignment.BottomCenter),
            style = CyberTheme.typography.terminal,
            color = CyberTheme.colors.textSecondary
        )
    }
}
