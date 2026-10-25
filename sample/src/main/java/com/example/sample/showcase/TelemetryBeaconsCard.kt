package com.example.sample.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberIconPulse
import com.example.cyberpunkandroid.effects.cyberIconSpin
import com.example.cyberpunkandroid.effects.cyberPing
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Catalogue Card: Vector Icons & Kinetic Animation Modifiers.
 *
 * Demonstrates:
 * - [CyberIcon] rendering vector icons with semantically matched sizing
 * - [Modifier.cyberIconPulse] for pulsing emergency beacons
 * - [Modifier.cyberPing] for radar beacon ring expansions
 * - [Modifier.cyberIconSpin] for continuous state rotation
 */
@Composable
fun TelemetryBeaconsCard(modifier: Modifier = Modifier) {
    CyberCard(
        modifier = modifier.fillMaxWidth(),
        header = {
            Column {
                Text(
                    text = "TELEMETRY BEACONS",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
                Text(
                    text = "CyberIcon • cyberIconPulse • cyberPing • cyberIconSpin",
                    style = CyberTheme.typography.body,
                    color = CyberTheme.colors.textSecondary
                )
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = CyberTheme.spacing.dp16),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Warning Alert: Warning pulse effect semantically matched to Warning icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
            ) {
                CyberIcon(
                    iconRes = CyberIcons.Warning,
                    contentDescription = null,
                    size = CyberPrimitives.IconSizes.dp32,
                    tint = CyberTheme.semantics.colors.warning,
                    modifier = Modifier.cyberIconPulse(
                        durationMillis = CyberTheme.semantics.durations.warningPulse
                    )
                )
                Text(
                    text = "ALERT",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.semantics.colors.warning
                )
            }

            // 2. Broadcast Signal: Radar ping expansion semantically matched to Signal icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
            ) {
                CyberIcon(
                    iconRes = CyberIcons.Signal,
                    contentDescription = null,
                    size = CyberPrimitives.IconSizes.dp32,
                    tint = CyberTheme.colors.primary,
                    modifier = Modifier.cyberPing(
                        color = CyberTheme.colors.primary
                    )
                )
                Text(
                    text = "BEACON",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
            }

            // 3. Sync Cycle: Continuous rotation effect semantically matched to Sync icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
            ) {
                CyberIcon(
                    iconRes = CyberIcons.Sync,
                    contentDescription = null,
                    size = CyberPrimitives.IconSizes.dp32,
                    tint = CyberTheme.semantics.colors.info,
                    modifier = Modifier.cyberIconSpin()
                )
                Text(
                    text = "SYNC",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.semantics.colors.info
                )
            }
        }
    }
}
