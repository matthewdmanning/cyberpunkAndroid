package com.example.sample.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberBadgeVariant
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberButtonSize
import com.example.cyberpunkandroid.components.CyberButtonStyle
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberSwitch
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberWeld
import com.example.cyberpunkandroid.effects.cyberBoot
import com.example.cyberpunkandroid.effects.cyberGlowBorderFlow
import com.example.cyberpunkandroid.effects.cyberSpark
import com.example.cyberpunkandroid.effects.cyberTextGlow
import com.example.cyberpunkandroid.effects.cyberWeld
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlinx.coroutines.delay

/**
 * Catalogue Card: Sparks, Welds & High-Impact Visual Effects.
 *
 * Demonstrates:
 * - [Modifier.cyberGlowBorderFlow] circulating illuminated borders
 * - [Modifier.cyberWeld] traveling electrical weld arcs
 * - [Modifier.cyberSpark] kinetic particle discharge
 * - [Modifier.cyberBoot] cathode ray tube / cold startup flicker
 * - [CyberButton] and [CyberSwitch] interactive electrical coupling
 */
@Composable
fun ConduitTapCard(modifier: Modifier = Modifier) {
    var isTapConnected by remember { mutableStateOf(true) }
    var isTapSurging by remember { mutableStateOf(false) }

    LaunchedEffect(isTapSurging) {
        if (isTapSurging) {
            delay(2500L)
            isTapSurging = false
        }
    }

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        header = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONDUIT SIPHON",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.primary,
                        modifier = Modifier.cyberTextGlow(color = CyberTheme.colors.primary, radius = 6.dp)
                    )
                    Text(
                        text = "cyberWeld • cyberSpark • cyberGlowBorderFlow • cyberBoot",
                        style = CyberTheme.typography.body,
                        color = CyberTheme.colors.textSecondary
                    )
                }
                CyberBadge(
                    text = if (!isTapConnected) "OFFLINE"
                    else if (isTapSurging) "SURGING"
                    else "LIVE",
                    variant = if (!isTapConnected) CyberBadgeVariant.Caution
                    else if (isTapSurging) CyberBadgeVariant.Danger
                    else CyberBadgeVariant.Success
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
            // Plasma Containment Power Chamber with circulating cyberGlowBorderFlow and cyberWeld
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val chamberSize = (screenWidth * 0.75f).coerceAtMost(300.dp)

            Box(
                modifier = Modifier
                    .size(chamberSize)
                    .align(Alignment.CenterHorizontally)
                    .then(
                        if (isTapConnected) {
                            Modifier.cyberGlowBorderFlow(
                                colors = listOf(
                                    CyberTheme.colors.primary,
                                    if (isTapSurging) CyberTheme.semantics.colors.danger else CyberTheme.semantics.colors.warning
                                ),
                                shape = CyberTheme.shapes.cyberCutCornerShape,
                                glowRadius = 10.dp,
                                width = 2.dp,
                                speed = if (isTapSurging) 2.4f else 1.0f
                            )
                        } else Modifier
                    )
                    .then(
                        if (isTapConnected) {
                            Modifier.cyberWeld(
                                weld = CyberWeld(cycleMillis = 3500),
                                shape = CyberTheme.shapes.cyberCutCornerShape
                            )
                        } else Modifier
                    )
                    .then(
                        if (isTapSurging && isTapConnected) {
                            Modifier.cyberSpark(
                                sparkCount = 48,
                                intensity = 0.12f,
                                color = CyberTheme.colors.primary,
                                secondaryColor = CyberTheme.semantics.colors.warning
                            )
                        } else Modifier
                    )
                    .clip(CyberTheme.shapes.cyberCutCornerShape)
                    .background(CyberTheme.colors.surfaceSecondary)
                    .padding(CyberTheme.spacing.dp16)
            ) {
                // Top Telemetry Header with cyberBoot ignition flicker on energization
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTapConnected) "480V AC" else "0V (COLD)",
                        style = CyberTheme.typography.terminal,
                        color = if (isTapConnected) CyberTheme.colors.primary else CyberTheme.colors.textSecondary,
                        modifier = if (isTapConnected) {
                            Modifier
                                .cyberBoot()
                                .cyberTextGlow(color = CyberTheme.colors.primary, radius = 4.dp, intensity = 1.3f)
                        } else Modifier
                    )
                    Text(
                        text = if (!isTapConnected) "0.0A"
                        else if (isTapSurging) "38.6A PEAK"
                        else "14.2A LOAD",
                        style = CyberTheme.typography.terminal,
                        color = if (isTapSurging) CyberTheme.semantics.colors.warning else CyberTheme.colors.textPrimary
                    )
                }

                // Bottom Telemetry Status
                Text(
                    text = if (!isTapConnected) "LINE SEVERED // CLAMP OPEN"
                    else if (isTapSurging) "OVERDRIVE SURGE DISCHARGE"
                    else "POWER DRAW: 6.8 kW",
                    modifier = Modifier.align(Alignment.BottomCenter),
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.textSecondary
                )
            }

            // Dedicated Surge Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CyberTheme.spacing.dp4)
            ) {
                CyberButton(
                    modifier = Modifier.fillMaxWidth(),
                    style = if (isTapSurging) CyberButtonStyle.Outline else CyberButtonStyle.Primary,
                    size = CyberButtonSize.Medium,
                    enabled = isTapConnected && !isTapSurging,
                    onClick = { isTapSurging = true }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CyberIcon(
                            iconRes = CyberIcons.Zap,
                            contentDescription = null,
                            size = CyberPrimitives.IconSizes.dp24,
                            tint = LocalContentColor.current
                        )
                        Text(
                            text = if (isTapSurging) "DISCHARGING SURGE..." else "TRIGGER OVERDRIVE SURGE",
                            style = CyberTheme.typography.terminal
                        )
                    }
                }
            }

            // Heavy Busbar Decoupling Mechanism (OSHA Lockout / Tagout Interlock)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CyberTheme.shapes.cyberCutCornerShape)
                    .background(CyberTheme.colors.surfaceSecondary)
                    .padding(CyberTheme.spacing.dp16)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)) {
                        Text(
                            text = "BUS DECOUPLING CLAMP",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textPrimary
                        )
                        Text(
                            text = if (isTapConnected) "PLASMA CONDUIT ENERGIZED" else "PHYSICAL AIR-GAP ISOLATED",
                            style = CyberTheme.typography.terminal,
                            color = if (isTapConnected) CyberTheme.colors.primary else CyberTheme.semantics.colors.warning
                        )
                    }
                    CyberSwitch(
                        checked = isTapConnected,
                        onCheckedChange = {
                            isTapConnected = it
                            if (!it) isTapSurging = false
                        }
                    )
                }
            }
        }
    }
}
