package com.example.sample.showcase

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberBadgeVariant
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberNumberPicker
import com.example.cyberpunkandroid.components.CyberNumberPickerSize
import com.example.cyberpunkandroid.components.CyberSectorRim
import com.example.cyberpunkandroid.components.CyberSlider
import com.example.cyberpunkandroid.components.CyberSwitch
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberNoise
import com.example.cyberpunkandroid.effects.cyberTextGlow
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Catalogue Card: Sliders, Switches & Thermodynamic Feedback.
 *
 * Demonstrates:
 * - [CyberSlider] continuous range selection
 * - [CyberSwitch] binary state toggle
 * - [CyberSectorRim] multi-sector radial gauge
 * - [Modifier.cyberNoise] electronic thermal grain
 * - [Modifier.cyberTextGlow] emissive typography glow
 */
@Composable
fun CryoRegulatorCard(modifier: Modifier = Modifier) {
    var isOverclocked by remember { mutableStateOf(false) }
    var coreMultiplier by remember { mutableIntStateOf(4) }
    var coolantRate by remember { mutableFloatStateOf(0.35f) }

    // Baseline 3.2 GHz: 37°C. Overclock (5.8 GHz) adds 52°C. Core multiplier scaling. Coolant extraction: up to 48°C.
    val targetTemp = (37f + (if (isOverclocked) 52f else 0f) + ((coreMultiplier - 4) * 3f) - (coolantRate * 48f)).coerceIn(24f, 96f)

    // Natural Thermal Lag & Inertia
    val animatedCoreTemp by animateFloatAsState(
        targetValue = targetTemp,
        animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
        label = "CoreThermalInertia"
    )

    // Blackbody Radiance Glide: 24°C (cyan) -> 52°C (warning amber) -> 96°C (danger red)
    val thermalFraction = ((animatedCoreTemp - 24f) / (96f - 24f)).coerceIn(0f, 1f)
    val tempColor = if (thermalFraction < 0.45f) {
        lerp(
            start = CyberTheme.colors.primary,
            stop = CyberTheme.semantics.colors.warning,
            fraction = thermalFraction / 0.45f
        )
    } else {
        lerp(
            start = CyberTheme.semantics.colors.warning,
            stop = CyberTheme.semantics.colors.danger,
            fraction = (thermalFraction - 0.45f) / 0.55f
        )
    }

    val thermalStress = ((animatedCoreTemp - 55f) / 35f).coerceIn(0f, 1f)

    val turbineCycleMillis = (2200 - (coolantRate * 1980)).toInt().coerceIn(220, 2400)
    val turbineTransition = rememberInfiniteTransition(label = "TurbineSpool")
    val turbineRotation by turbineTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = turbineCycleMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TurbineRotation"
    )

    val isCriticalTemp = animatedCoreTemp >= 75f
    val isElevatedTemp = animatedCoreTemp >= 52f

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        header = {
            Column {
                Text(
                    text = "CRYO REGULATOR // OVERCLOCK",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
                Text(
                    text = "CyberNumberPicker • CyberSlider • CyberSwitch • CyberSectorRim • cyberNoise",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hero Gauge Cluster: Sector rim + Net Temperature + Mechanical Turbine
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .cyberNoise(
                            enabled = thermalStress > 0.05f,
                            opacity = (thermalStress * 0.16f).coerceAtMost(0.20f),
                            speed = 1.4f
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CyberSectorRim(
                        size = 92.dp,
                        color = tempColor,
                        sectorAngles = listOf(70f, 70f, 70f, 70f),
                        thickness = if (thermalStress > 0.6f) CyberPrimitives.BorderWidths.dp4 else CyberPrimitives.BorderWidths.dp2
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${animatedCoreTemp.toInt()}°C",
                            style = CyberTheme.typography.terminal,
                            color = tempColor,
                            modifier = if (isElevatedTemp) {
                                Modifier.cyberTextGlow(
                                    color = tempColor,
                                    radius = if (isCriticalTemp) 8.dp else 4.dp,
                                    intensity = if (isCriticalTemp) 2.0f else 1.2f
                                )
                            } else Modifier
                        )
                        CyberIcon(
                            iconRes = CyberIcons.Sync,
                            contentDescription = "Turbine Impeller",
                            size = CyberPrimitives.IconSizes.dp24,
                            tint = tempColor,
                            modifier = Modifier.graphicsLayer { rotationZ = turbineRotation }
                        )
                    }
                }

                // Controls
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "5.8 GHz CLOCK",
                            style = CyberTheme.typography.terminal,
                            color = if (isOverclocked) CyberTheme.semantics.colors.warning else CyberTheme.colors.textPrimary
                        )
                        CyberSwitch(
                            checked = isOverclocked,
                            onCheckedChange = { isOverclocked = it }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = CyberTheme.spacing.dp8),
                        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COOLANT",
                                style = CyberTheme.typography.terminal,
                                color = CyberTheme.colors.textSecondary
                            )
                            Text(
                                text = "${(coolantRate * 100).toInt()}%",
                                style = CyberTheme.typography.terminal,
                                color = CyberTheme.colors.primary
                            )
                        }
                        CyberSlider(
                            value = coolantRate,
                            onValueChange = { coolantRate = it }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = CyberTheme.spacing.dp4),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE CORES",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                        CyberNumberPicker(
                            value = coreMultiplier,
                            range = 1..16,
                            step = 1,
                            size = CyberNumberPickerSize.Small,
                            prefix = "#",
                            onValueChange = { coreMultiplier = it }
                        )
                    }
                }
            }

            // Thermodynamic status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberBadge(
                    text = if (isCriticalTemp) "THERMAL RUNAWAY"
                    else if (isElevatedTemp) "HIGH FLUX"
                    else "NOMINAL",
                    variant = if (isCriticalTemp) CyberBadgeVariant.Danger
                    else if (isElevatedTemp) CyberBadgeVariant.Caution
                    else CyberBadgeVariant.Success
                )
                Text(
                    text = "PEAK: 96°C",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.textSecondary
                )
            }
        }
    }
}
