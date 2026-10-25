package com.example.sample.showcase

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberBadgeVariant
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberButtonSize
import com.example.cyberpunkandroid.components.CyberButtonStyle
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberDecrypter
import com.example.cyberpunkandroid.components.CyberGlowIcon
import com.example.cyberpunkandroid.components.CyberProgress
import com.example.cyberpunkandroid.components.CyberProgressVariant
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberDatastream
import com.example.cyberpunkandroid.effects.cyberHoloBackground
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Catalogue Card: Terminal Decryption & Progress Controls.
 *
 * Demonstrates:
 * - [CyberDecrypter] text deciphering animation
 * - [Modifier.cyberHoloBackground] and [Modifier.cyberDatastream] for digital terminal displays
 * - [CyberProgress] with segmented variant
 * - [CyberGlowIcon] with emissive glow radius
 * - [CyberButton] with Primary/Outline styling
 */
@Composable
fun BreachCipherCard(modifier: Modifier = Modifier) {
    var isBreached by remember { mutableStateOf(false) }
    val breachProgress by animateFloatAsState(
        targetValue = if (isBreached) 1f else 0f,
        animationSpec = tween(durationMillis = 1400, easing = LinearEasing),
        label = "BreachProgress"
    )

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        header = {
            Column {
                Text(
                    text = "BREACH CIPHER // JACK-IN",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
                Text(
                    text = "CyberDecrypter • cyberDatastream • CyberProgress • CyberGlowIcon",
                    style = CyberTheme.typography.body,
                    color = CyberTheme.colors.textSecondary
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CyberTheme.spacing.dp16),
            verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16)
        ) {
            // Header status row: authorization badge and port indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberBadge(
                    text = if (breachProgress >= 0.95f) "ROOT ACCESS" else if (breachProgress > 0f) "DECRYPTING" else "AIR-GAPPED",
                    variant = if (breachProgress >= 0.95f) CyberBadgeVariant.Success else if (breachProgress > 0f) CyberBadgeVariant.Caution else CyberBadgeVariant.Outline
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberGlowIcon(
                        iconRes = if (breachProgress >= 0.95f) CyberIcons.Unlock else CyberIcons.Lock,
                        contentDescription = null,
                        color = if (breachProgress >= 0.95f) CyberTheme.semantics.colors.success else CyberTheme.semantics.colors.warning,
                        glowColor = if (breachProgress >= 0.95f) CyberTheme.semantics.colors.success else CyberTheme.semantics.colors.warning,
                        radius = 4.dp,
                        intensity = 1.6f,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (breachProgress >= 0.95f) "UNLOCKED" else "LOCKED",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textPrimary
                    )
                }
            }

            // Dedicated decrypter terminal display with cyberDatastream and cyberHoloBackground
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .clip(CyberTheme.shapes.roundedMd)
                    .cyberHoloBackground()
                    .cyberDatastream(
                        color = CyberTheme.colors.primary,
                        speed = 1.2f,
                        maxAlpha = 0.22f
                    )
                    .background(CyberTheme.colors.surfaceSecondary.copy(alpha = 0.85f))
                    .padding(CyberTheme.spacing.dp12),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)) {
                    Text(
                        text = "0x00_CIPHER_BUFFER",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )
                    CyberDecrypter(
                        targetText = "KEY: 0x9E_ARASAKA_ROOT",
                        progress = breachProgress
                    )
                }
            }

            CyberProgress(
                progress = breachProgress,
                variant = CyberProgressVariant.Segmented,
                segments = 8,
                color = if (breachProgress >= 0.95f) CyberTheme.semantics.colors.success else CyberTheme.colors.primary
            )

            // Footer row: Registers resolved + Action Button with generous padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = CyberTheme.spacing.dp4),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REGISTERS: ${(breachProgress * 8).toInt()}/8",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.textSecondary
                )
                CyberButton(
                    style = if (isBreached) CyberButtonStyle.Outline else CyberButtonStyle.Primary,
                    size = CyberButtonSize.Medium,
                    onClick = { isBreached = !isBreached }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CyberIcon(
                            iconRes = CyberIcons.Terminal,
                            contentDescription = null,
                            size = CyberPrimitives.IconSizes.dp16,
                            tint = LocalContentColor.current
                        )
                        Text(
                            text = if (isBreached) "RESET LINK" else "RUN CIPHER",
                            style = CyberTheme.typography.terminal
                        )
                    }
                }
            }
        }
    }
}
