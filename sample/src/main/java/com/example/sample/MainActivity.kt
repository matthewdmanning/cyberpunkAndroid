package com.example.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.rememberCoroutineScope
import com.example.cyberpunkandroid.effects.cyberCrt
import com.example.cyberpunkandroid.effects.cyberScanlines
import com.example.cyberpunkandroid.effects.cyberStripes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberBadgeVariant
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberButtonSize
import com.example.cyberpunkandroid.components.CyberButtonStyle
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberDecrypter
import com.example.cyberpunkandroid.components.CyberDialTicks
import com.example.cyberpunkandroid.components.CyberProgress
import com.example.cyberpunkandroid.components.CyberProgressVariant
import com.example.cyberpunkandroid.components.CyberRim
import com.example.cyberpunkandroid.components.CyberSectorRim
import com.example.cyberpunkandroid.components.CyberSlider
import com.example.cyberpunkandroid.components.CyberSwitch
import com.example.cyberpunkandroid.components.cyberLongPressFill
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberWeld
import com.example.cyberpunkandroid.effects.cyberBoot
import com.example.cyberpunkandroid.effects.cyberDatastream
import com.example.cyberpunkandroid.effects.cyberGlowBorderFlow
import com.example.cyberpunkandroid.effects.cyberHoloBackground
import com.example.cyberpunkandroid.effects.cyberIconPulse
import com.example.cyberpunkandroid.effects.cyberIconSpin
import com.example.cyberpunkandroid.effects.cyberNoise
import com.example.cyberpunkandroid.effects.cyberOverload
import com.example.cyberpunkandroid.effects.cyberPing
import com.example.cyberpunkandroid.effects.cyberSpark
import com.example.cyberpunkandroid.effects.cyberTextGlow
import com.example.cyberpunkandroid.effects.cyberWeld
import com.example.cyberpunkandroid.components.CyberGlowIcon
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CyberTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberTheme.colors.background
                ) {
                    SampleApp()
                }
            }
        }
    }
}

@Composable
fun SampleApp(modifier: Modifier = Modifier) {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = CyberTheme.spacing.dp16,
                end = CyberTheme.spacing.dp16,
                top = insets.calculateTopPadding() + CyberTheme.spacing.dp16,
                bottom = insets.calculateBottomPadding() + CyberTheme.spacing.dp16
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp24)
    ) {
        // =========================================================================
        // LEVEL 1: Clean utility with semantically matched singular effects
        // Base Item: Transceiver beacon array
        // Function: Continuous background monitoring with singular kinetic feedback
        // Styling: Warning pulse, radar ping expansion, continuous sync spin
        // =========================================================================
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            header = {
                Text(
                    text = "TELEMETRY BEACONS",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
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

        // =========================================================================
        // LEVEL 2 (CARD 1): Interactive tactile input linking state to other icons
        // Base Item: Radial reflex limiter bypass actuator
        // Function: Hold-to-charge capacitive input triggering reflex override
        // Styling: 64dp actuator, radial rim fill, Zap/Lock flip, Hot-Sim badge
        // =========================================================================
        var bypassProgress by remember { mutableFloatStateOf(0f) }
        var isBypassed by remember { mutableStateOf(false) }

        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            header = {
                Text(
                    text = "SYNAPTIC GOVERNOR",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CyberTheme.spacing.dp16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tactile radial actuator core with accessible touch target (Hero Highlight Icon scaled to dp64)
                Box(
                    modifier = Modifier.size(144.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CyberDialTicks(
                        size = 144.dp,
                        color = CyberTheme.colors.primary.copy(alpha = 0.75f),
                        tickCount = 48,
                        majorTickInterval = 6,
                        strokeWidth = CyberPrimitives.BorderWidths.dp2,
                        tickLength = 6.dp,
                        majorTickLength = 12.dp
                    )
                    CyberRim(
                        progress = bypassProgress,
                        modifier = Modifier.size(122.dp),
                        strokeWidth = 6f,
                        color = if (isBypassed) CyberTheme.semantics.colors.danger else CyberTheme.colors.primary,
                        backgroundColor = CyberTheme.colors.surfaceSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                if (isBypassed) CyberTheme.semantics.colors.danger.copy(alpha = 0.25f)
                                else CyberTheme.colors.surfaceTertiary
                            )
                            .cyberLongPressFill(
                                durationMillis = 1200L,
                                onProgressUpdate = { bypassProgress = it },
                                onComplete = {
                                    isBypassed = !isBypassed
                                    bypassProgress = 0f
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CyberIcon(
                            iconRes = if (isBypassed) CyberIcons.Zap else CyberIcons.Lock,
                            contentDescription = null,
                            size = CyberPrimitives.IconSizes.dp64,
                            tint = if (isBypassed) CyberTheme.semantics.colors.danger else CyberTheme.colors.primary
                        )
                    }
                }

                // Supporting semantic readouts and status badge
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = CyberTheme.spacing.dp16),
                    verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8),
                    horizontalAlignment = Alignment.Start
                ) {
                    CyberBadge(
                        text = if (isBypassed) "OVERRIDE" else "ENGAGED",
                        variant = if (isBypassed) CyberBadgeVariant.Danger else CyberBadgeVariant.Success
                    )

                    Text(
                        text = if (bypassProgress > 0f) {
                            "CHARGE: ${(bypassProgress * 100).toInt()}%"
                        } else {
                            if (isBypassed) "LIMITER: OFF" else "LIMITER: ON"
                        },
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textPrimary
                    )

                    Text(
                        text = "HOLD TO ${if (isBypassed) "ENABLE" else "BYPASS"}",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )
                }
            }
        }

        // =========================================================================
        // LEVEL 2 (CARD 2): Interactive state linkage with code-breaking descrambler
        // Base Item: Neural jack-in terminal tap with cryptographic authorization lock
        // Function: Tap button to trigger animated decryption progress resolving key
        // Styling: CyberDecrypter cycling glyphs, segmented CyberProgress, Lock/Unlock flip
        // =========================================================================
        var isBreached by remember { mutableStateOf(false) }
        val breachProgress by animateFloatAsState(
            targetValue = if (isBreached) 1f else 0f,
            animationSpec = tween(durationMillis = 1400, easing = LinearEasing),
            label = "BreachProgress"
        )

        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            header = {
                Text(
                    text = "BREACH CIPHER // JACK-IN",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CyberTheme.spacing.dp16),
                verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16)
            ) {
                // Header status row: authorization badge and port indicator (Inline status icon: dp16)
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

        // =========================================================================
        // LEVEL 3: Thermodynamic closed-loop thermal regulator panel
        // Natural Behavior Best Practice:
        // 1. Thermal Inertia: Physical objects heat up and cool down progressively;
        //    sudden visual jumps in overheating are unnatural.
        // 2. Continuous Radiance: Heat spectrum glides from cryogenic cyan -> amber -> crimson.
        // 3. Mechanical Coupling: Impeller RPM directly reflects coolant pump flow rate.
        // 4. Progressive Stress: Overload jitter intensifies proportionally as heat mounts.
        // =========================================================================
        var isOverclocked by remember { mutableStateOf(false) }
        var coolantRate by remember { mutableFloatStateOf(0.35f) }

        // Thermodynamic equilibrium calculation:
        // Baseline 3.2 GHz: 37°C. Overclock (5.8 GHz) adds 52°C. Coolant extraction: up to 48°C.
        val targetTemp = (37f + (if (isOverclocked) 52f else 0f) - (coolantRate * 48f)).coerceIn(24f, 96f)

        // Natural Thermal Lag & Inertia: Temperature approaches equilibrium over ~2.4 seconds
        val animatedCoreTemp by animateFloatAsState(
            targetValue = targetTemp,
            animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            label = "CoreThermalInertia"
        )

        // Continuous Blackbody Radiance: Smoothly glides across thermal spectrum
        // 24°C (cryo cyan) -> 52°C (amber warning) -> 96°C (incandescent crimson)
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

        // Progressive Thermal Stress & Overload Jitter
        // Silicon timing jitter begins above 55°C and intensifies up to 96°C
        val thermalStress = ((animatedCoreTemp - 55f) / 35f).coerceIn(0f, 1f)
        val overloadIntensity = thermalStress * 0.08f

        // Mechanical Turbine Impeller Dynamics:
        // Impeller RPM scales directly with coolant slider: from 2200ms (idle trickle) to 220ms (36k RPM cryo flood)
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
            modifier = Modifier.fillMaxWidth(),
            header = {
                Text(
                    text = "CRYO REGULATOR // OVERCLOCK",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
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
                    // Natural Silicon Behavior: Electronic Johnson-Nyquist thermal noise grain instead of software glitching
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

                    // Coupled input controls with clear functional context and ample touch padding
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16)
                    ) {
                        // Control 1: Clock multiplier switch with generous touch padding
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

                        // Control 2: Coolant pump rate slider with clear functional context
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

        // =========================================================================
        // LEVEL 4: High-impact visual spectacle linked with interactivity
        // Base Item: High-voltage power conduit tap with plasma containment and decoupling interlock
        // Function: Autonomous cyberWeld border, circulating cyberGlowBorderFlow plasma containment,
        //           incandescent cyberSpark burst, and cold-energizing cyberBoot flicker
        // Styling: cyberGlowBorderFlow rotating perimeter, cyberBoot ignition, emissive cyberTextGlow
        // =========================================================================
        var isTapConnected by remember { mutableStateOf(true) }
        var isTapSurging by remember { mutableStateOf(false) }

        LaunchedEffect(isTapSurging) {
            if (isTapSurging) {
                delay(2500L)
                isTapSurging = false
            }
        }

        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            header = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONDUIT SIPHON",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.primary,
                        modifier = Modifier.cyberTextGlow(color = CyberTheme.colors.primary, radius = 6.dp)
                    )
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
                val chamberSize = screenWidth * 0.75f

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

                    // Center is intentionally left open for spark particle trajectories and plasma flow

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

                // Dedicated Surge Action Button (Full-width, prominent hero action icon dp24)
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
                // Replaces low-semantic redundant circular hold dial with dedicated tactile bus decoupler
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

        // -------------------------------------------------------------
        // Level 5 - Card 6: TT-CRT7 Dumpshock Cold-Jack Console & Biometric Neural Monitor
        // Inspiration: docs/inspirations/crt-biomonitor-dumpshock-cold-jack-console.md
        // -------------------------------------------------------------
        var dumpshockThreat by remember { mutableFloatStateOf(0.15f) }
        var isColdJackSevered by remember { mutableStateOf(false) }
        var isArcDischarging by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        // Physiological natural modeling: Continuous Black ICE threat escalation
        // Operative cruising low-threat subnets (72 BPM) -> escalating stress -> violent ventricular fibrillation (198 BPM)
        val targetBpm = if (isColdJackSevered) 0f else 72f + (dumpshockThreat * 126f)
        val animatedBpm by animateFloatAsState(
            targetValue = targetBpm,
            animationSpec = tween(durationMillis = 1200),
            label = "BpmAnimation"
        )

        val targetCoherence = if (isColdJackSevered) 0f else (98f - (dumpshockThreat * 84f)).coerceAtLeast(10f)
        val animatedCoherence by animateFloatAsState(
            targetValue = targetCoherence,
            animationSpec = tween(durationMillis = 1200),
            label = "CoherenceAnimation"
        )

        val isFibrillating = dumpshockThreat > 0.70f && !isColdJackSevered

        val phosphorSweepPhase by rememberInfiniteTransition(label = "EkgSweep").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (isFibrillating) 600 else 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "PhosphorPhase"
        )

        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            header = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TT-CRT7 // BIOMONITOR",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.primary
                        )
                    }
                    CyberBadge(
                        text = if (isColdJackSevered) "ISOLATED"
                        else if (isFibrillating) "DUMPSHOCK"
                        else "P31 STABLE",
                        variant = if (isColdJackSevered) CyberBadgeVariant.Outline
                        else if (isFibrillating) CyberBadgeVariant.Danger
                        else CyberBadgeVariant.Success
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16)
            ) {
                // CRT Phosphor Display with Barrel Distortion, Vignette, and Scanlines
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .cyberCrt()
                        .cyberScanlines(spacing = 6.dp, opacity = 0.25f, color = Color(0xFF39FF14))
                        .clip(CyberTheme.shapes.cyberCutCornerShape)
                        .background(Color(0xFF041006))
                        .drawBehind {
                            val midY = size.height / 2f
                            val gridColor = Color(0x1A39FF14)
                            val phosphorGreen = Color(0xFF39FF14)
                            val dangerRed = Color(0xFFFF003C)

                            // Phosphor CRT reticle grid lines
                            drawLine(gridColor, Offset(0f, midY), Offset(size.width, midY), strokeWidth = 1.dp.toPx())
                            drawLine(gridColor, Offset(0f, midY - 30.dp.toPx()), Offset(size.width, midY - 30.dp.toPx()), strokeWidth = 1.dp.toPx())
                            drawLine(gridColor, Offset(0f, midY + 30.dp.toPx()), Offset(size.width, midY + 30.dp.toPx()), strokeWidth = 1.dp.toPx())

                            val stepPx = 36.dp.toPx()
                            var xTick = 0f
                            while (xTick <= size.width) {
                                drawLine(gridColor, Offset(xTick, 0f), Offset(xTick, size.height), strokeWidth = 1.dp.toPx())
                                xTick += stepPx
                            }

                            // Live continuous physiological EKG & synaptic coherence waveform
                            val ekgPath = Path()
                            val numSamples = 90
                            for (i in 0..numSamples) {
                                val px = size.width * (i.toFloat() / numSamples)
                                val waveTime = (i.toFloat() / numSamples) * 3f + phosphorSweepPhase * 2f
                                val cycleFraction = waveTime % 1f

                                val yOffset = if (isColdJackSevered) {
                                    0f // Flatline when neural bus is severed
                                } else if (isFibrillating) {
                                    // Chaotic ventricular fibrillation under Black ICE strike
                                    (sin(waveTime * 18f) * 30f * (animatedBpm / 150f) + sin(waveTime * 41f) * 14f)
                                } else {
                                    // Crisp cardiac rhythm (P-wave, QRS complex, T-wave) + micro-neural firing
                                    when {
                                        cycleFraction in 0.40f..0.44f -> -10f
                                        cycleFraction in 0.45f..0.48f -> 5f
                                        cycleFraction in 0.48f..0.52f -> -42f
                                        cycleFraction in 0.52f..0.55f -> 12f
                                        cycleFraction in 0.60f..0.66f -> -8f
                                        else -> sin(waveTime * 30f) * 2f
                                    }
                                }

                                val py = midY + yOffset
                                if (i == 0) ekgPath.moveTo(px, py) else ekgPath.lineTo(px, py)
                            }

                            val traceColor = if (isFibrillating) dangerRed else phosphorGreen
                            drawPath(
                                path = ekgPath,
                                color = traceColor,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        .padding(CyberTheme.spacing.dp12)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "VENT: ${animatedBpm.toInt()} BPM",
                            style = CyberTheme.typography.terminal,
                            color = if (isFibrillating) CyberTheme.semantics.colors.danger else Color(0xFF39FF14)
                        )
                        Text(
                            text = "COH: ${animatedCoherence.toInt()}%",
                            style = CyberTheme.typography.terminal,
                            color = if (animatedCoherence < 45f) CyberTheme.semantics.colors.warning else Color(0xFF39FF14)
                        )
                    }

                    Text(
                        text = if (isColdJackSevered) "NEURAL BUS GROUNDED"
                        else if (isFibrillating) "VENTRICULAR FIBRILLATION"
                        else "P31 PHOSPHOR // 15.75 kHz",
                        modifier = Modifier.align(Alignment.BottomCenter),
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )
                }

                // Continuous Subnet Intrusion Threat Slider (Natural Physical Modeling)
                Column(
                    verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SUBNET INTRUSION THREAT",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textPrimary
                        )
                        Text(
                            text = "${(dumpshockThreat * 100).toInt()}%",
                            style = CyberTheme.typography.terminal,
                            color = if (isFibrillating) CyberTheme.semantics.colors.danger else CyberTheme.colors.primary
                        )
                    }

                    // Tactical Mission Threat Stage Profiles (CRUISE // STRAIN // BLACK ICE)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
                    ) {
                        listOf(
                            "CRUISE" to 0.15f,
                            "STRAIN" to 0.50f,
                            "BLACK ICE" to 0.90f
                        ).forEach { (label, value) ->
                            val isSelected = (dumpshockThreat - value).let { it >= -0.15f && it <= 0.15f }
                            CyberButton(
                                modifier = Modifier.weight(1f),
                                style = if (isSelected) CyberButtonStyle.Primary else CyberButtonStyle.Outline,
                                size = CyberButtonSize.Small,
                                onClick = { if (!isColdJackSevered) dumpshockThreat = value }
                            ) {
                                Text(
                                    text = label,
                                    style = CyberTheme.typography.terminal
                                )
                            }
                        }
                    }

                    CyberSlider(
                        value = dumpshockThreat,
                        onValueChange = { if (!isColdJackSevered) dumpshockThreat = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = when {
                            dumpshockThreat > 0.70f -> "BLACK ICE SURGE (>70% DUMPSHOCK)"
                            dumpshockThreat > 0.30f -> "FIREWALL STRAIN (ELEVATED ARRHYTHMIA)"
                            else -> "LOW-THREAT SUBNET CRUISE"
                        },
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )
                }

                // Cold-Jack Knife Switch (Heavy Mechanical Throw Actuator with Hazard Stripes & Arc Chute Discharge)
                Column(
                    verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
                ) {
                    Text(
                        text = "EMERGENCY COLD-JACK ACTUATOR",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CyberTheme.shapes.cyberCutCornerShape)
                            .background(
                                if (isColdJackSevered) CyberTheme.semantics.colors.danger.copy(alpha = 0.15f)
                                else Color(0xFF161212)
                            )
                            .cyberStripes(
                                color = if (isColdJackSevered) Color(0x33FF003C) else Color(0x22FFFFFF),
                                stripeWidth = 8.dp
                            )
                            .then(
                                if (isArcDischarging) {
                                    Modifier.cyberSpark(
                                        sparkCount = 36,
                                        intensity = 0.9f,
                                        color = Color(0xFF00E5FF)
                                    )
                                } else Modifier
                            )
                            .padding(CyberTheme.spacing.dp12)
                    ) {
                        CyberButton(
                            modifier = Modifier.fillMaxWidth(),
                            style = if (isColdJackSevered) CyberButtonStyle.Outline else CyberButtonStyle.Primary,
                            size = CyberButtonSize.Medium,
                            onClick = {
                                isColdJackSevered = !isColdJackSevered
                                if (isColdJackSevered) {
                                    isArcDischarging = true
                                    coroutineScope.launch {
                                        delay(600)
                                        isArcDischarging = false
                                    }
                                }
                            }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CyberIcon(
                                    iconRes = if (isColdJackSevered) CyberIcons.Refresh else CyberIcons.Zap,
                                    contentDescription = null,
                                    size = CyberPrimitives.IconSizes.dp24,
                                    tint = LocalContentColor.current
                                )
                                Text(
                                    text = if (isColdJackSevered) "RESET BUS RELAYS" else "THROW COLD-JACK LEVER",
                                    style = CyberTheme.typography.terminal
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isColdJackSevered) "NEURAL BUS ISOLATED // GROUNDED"
                        else "KNIFE SWITCH ARMED // 2.5 kV READY",
                        style = CyberTheme.typography.terminal,
                        color = if (isColdJackSevered) CyberTheme.semantics.colors.success else CyberTheme.semantics.colors.danger
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // Level 5 - Card 7: Arasaka / NetWatch IDM-800 Intrusion Detection Matrix & ICE Overwatch Console
        // Inspiration: docs/inspirations/ice-detection-matrix-countermeasures.md
        // -------------------------------------------------------------
        var penetrationTier by remember { mutableIntStateOf(1) }
        var isStealthActive by remember { mutableStateOf(true) }
        var overwatchScore by remember { mutableFloatStateOf(0.12f) }
        var isPurging by remember { mutableStateOf(false) }

        // Real-world dynamic simulation loop (Natural dwell-time accumulation):
        // Overwatch score accumulates over time based on penetration tier and stealth dampening.
        // Selecting a tier doesn't instantly snap; the trace builds naturally over dwell time!
        LaunchedEffect(penetrationTier, isStealthActive, isPurging) {
            while (true) {
                delay(100L)
                if (isPurging) {
                    overwatchScore = (overwatchScore - 0.08f).coerceAtLeast(0f)
                    if (overwatchScore <= 0f) {
                        isPurging = false
                        penetrationTier = 1
                    }
                } else {
                    val targetLevel = when (penetrationTier) {
                        1 -> 0.12f
                        2 -> 0.38f
                        3 -> 0.65f
                        else -> 0.95f
                    }
                    val rate = if (isStealthActive) 0.008f else 0.016f
                    if (overwatchScore < targetLevel) {
                        overwatchScore = (overwatchScore + rate).coerceAtMost(targetLevel)
                    } else if (overwatchScore > targetLevel) {
                        overwatchScore = (overwatchScore - 0.012f).coerceAtLeast(targetLevel)
                    }
                }
            }
        }

        val iceColor = if (overwatchScore > 0.75f) {
            Color(0xFFFF003C) // Black ICE Blood-Crimson
        } else if (overwatchScore > 0.40f) {
            Color(0xFFFF9100) // Grey ICE Amber
        } else {
            Color(0xFF00E5FF) // White ICE Crisp Cyan
        }

        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            header = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ARASAKA IDM-800",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.primary
                        )
                    }
                    CyberBadge(
                        text = if (overwatchScore > 0.75f) "BLACK ICE"
                        else if (overwatchScore > 0.40f) "GREY ICE"
                        else "WHITE ICE",
                        variant = if (overwatchScore > 0.75f) CyberBadgeVariant.Danger
                        else if (overwatchScore > 0.40f) CyberBadgeVariant.Caution
                        else CyberBadgeVariant.Success
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16)
            ) {
                // Section 1: 20-Segment Perimeter LED Activation Bar
                Column(
                    verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GRID OVERWATCH",
                            style = CyberTheme.typography.terminal,
                            color = iceColor
                        )
                        Text(
                            text = "${(overwatchScore * 100).toInt()}% " + if (overwatchScore > 0.75f) "BLACK"
                            else if (overwatchScore > 0.40f) "GREY"
                            else "WHITE",
                            style = CyberTheme.typography.terminal,
                            color = iceColor
                        )
                    }
                    CyberProgress(
                        progress = overwatchScore,
                        variant = CyberProgressVariant.Segmented,
                        segments = 20,
                        color = iceColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section 2: Central Intrusion Topology Display with bidirectional network datastream
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .cyberOverload(
                            enabled = overwatchScore > 0.75f,
                            intensity = ((overwatchScore - 0.75f) * 4f).coerceAtMost(1f)
                        )
                        .cyberDatastream(
                            color = Color(0xFFFF9100),
                            speed = 1.8f,
                            maxAlpha = 0.25f,
                            mirror = true
                        )
                        .clip(CyberTheme.shapes.cyberCutCornerShape)
                        .background(Color(0xFF0A0802))
                        .drawBehind {
                            val amberWireframe = Color(0xFFFF9100)
                            val cyanNode = Color(0xFF00E5FF)
                            val crimsonLock = Color(0xFFFF003C)

                            // Vector matrix grid lines
                            val gridSpacing = 28.dp.toPx()
                            var gy = 0f
                            while (gy <= size.height) {
                                drawLine(
                                    Color(0x1AFF9100),
                                    Offset(0f, gy),
                                    Offset(size.width, gy),
                                    strokeWidth = 1.dp.toPx()
                                )
                                gy += gridSpacing
                            }
                            var gx = 0f
                            while (gx <= size.width) {
                                drawLine(
                                    Color(0x1AFF9100),
                                    Offset(gx, 0f),
                                    Offset(gx, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                                gx += gridSpacing
                            }

                            // Subnet nodes and vector paths
                            val nodeA = Offset(size.width * 0.15f, size.height * 0.65f)
                            val nodeB = Offset(size.width * 0.40f, size.height * 0.35f)
                            val nodeC = Offset(size.width * 0.65f, size.height * 0.70f)
                            val nodeTarget = Offset(size.width * 0.85f, size.height * 0.30f)

                            val pathColor = if (overwatchScore > 0.75f) crimsonLock else amberWireframe
                            drawLine(pathColor, nodeA, nodeB, strokeWidth = 2.dp.toPx())
                            drawLine(pathColor, nodeB, nodeC, strokeWidth = 2.dp.toPx())
                            drawLine(pathColor, nodeC, nodeTarget, strokeWidth = 2.dp.toPx())

                            drawCircle(cyanNode, radius = 4.dp.toPx(), center = nodeA)
                            drawCircle(cyanNode, radius = 4.dp.toPx(), center = nodeB)
                            drawCircle(cyanNode, radius = 4.dp.toPx(), center = nodeC)

                            // Target node with active perimeter pulse ring
                            val targetRingColor = if (overwatchScore > 0.75f) crimsonLock else cyanNode
                            drawCircle(targetRingColor, radius = 7.dp.toPx(), center = nodeTarget)
                            drawCircle(
                                targetRingColor,
                                radius = 12.dp.toPx(),
                                center = nodeTarget,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                        .padding(CyberTheme.spacing.dp8)
                ) {
                    Text(
                        text = "SUBNET: ARA-GRID-8802",
                        style = CyberTheme.typography.terminal,
                        color = iceColor
                    )

                    if (overwatchScore > 0.75f) {
                        Text(
                            text = "HOSTILE BLACK ICE LOCKED",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .cyberTextGlow(color = Color(0xFFFF003C), radius = 8.dp, intensity = 2.0f),
                            style = CyberTheme.typography.terminal,
                            color = Color(0xFFFF003C)
                        )
                    }

                    Text(
                        text = "ACTIVE DAEMONS: ${penetrationTier * 3}",
                        modifier = Modifier.align(Alignment.BottomStart),
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )
                }

                // Section 3: Stepped Detent Penetration Depth Selector
                Column(
                    verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
                ) {
                    Text(
                        text = "PENETRATION DEPTH (DETENTS)",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
                    ) {
                        listOf(1 to "T1", 2 to "T2", 3 to "T3", 4 to "T4").forEach { (tier, label) ->
                            CyberButton(
                                modifier = Modifier.weight(1f),
                                style = if (penetrationTier == tier) CyberButtonStyle.Primary else CyberButtonStyle.Outline,
                                size = CyberButtonSize.Medium,
                                onClick = { penetrationTier = tier }
                            ) {
                                Text(text = label, style = CyberTheme.typography.terminal)
                            }
                        }
                    }

                    Text(
                        text = "DETENT: " + when (penetrationTier) {
                            1 -> "T1 // PASSIVE PROBE"
                            2 -> "T2 // CATALOG SCAN"
                            3 -> "T3 // MEMORY SIPHON"
                            else -> "T4 // ROOT TAKEOVER"
                        },
                        style = CyberTheme.typography.terminal,
                        color = iceColor
                    )
                }

                // Section 4: Hardware Packet Stealth Filter Panel (Generous Padding & Multi-row Layout)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CyberTheme.shapes.cyberCutCornerShape)
                        .background(CyberTheme.colors.surfaceSecondary)
                        .padding(CyberTheme.spacing.dp16)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PACKET STEALTH FILTER",
                                style = CyberTheme.typography.terminal,
                                color = CyberTheme.colors.textPrimary
                            )
                            CyberSwitch(
                                checked = isStealthActive,
                                onCheckedChange = { isStealthActive = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isStealthActive) "STATUS: MASKED" else "STATUS: EXPOSED",
                                style = CyberTheme.typography.terminal,
                                color = if (isStealthActive) CyberTheme.semantics.colors.success else CyberTheme.semantics.colors.warning
                            )
                            Text(
                                text = if (isStealthActive) "-40% TRACE" else "100% TRACE",
                                style = CyberTheme.typography.terminal,
                                color = CyberTheme.colors.textSecondary
                            )
                        }
                    }
                }

                // Section 5: Master Trace Purge with Video Game Hinged Safety Cover Interlock
                var isSafetyCoverOpen by remember { mutableStateOf(false) }

                Column(
                    verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
                ) {
                    // Tactical Safety Interlock Switch Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSafetyCoverOpen) "SAFETY COVER: ARMED" else "SAFETY COVER: GUARDED",
                            style = CyberTheme.typography.terminal,
                            color = if (isSafetyCoverOpen) CyberTheme.semantics.colors.warning else CyberTheme.colors.textSecondary
                        )
                        CyberSwitch(
                            checked = isSafetyCoverOpen,
                            onCheckedChange = { isSafetyCoverOpen = it }
                        )
                    }

                    CyberButton(
                        modifier = Modifier.fillMaxWidth(),
                        style = if (isSafetyCoverOpen) {
                            if (overwatchScore > 0.75f) CyberButtonStyle.Primary else CyberButtonStyle.Outline
                        } else CyberButtonStyle.Ghost,
                        size = CyberButtonSize.Medium,
                        enabled = isSafetyCoverOpen,
                        onClick = {
                            isPurging = true
                            isSafetyCoverOpen = false
                        }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CyberIcon(
                                iconRes = if (overwatchScore > 0.75f) CyberIcons.Warning else CyberIcons.ShieldX,
                                contentDescription = null,
                                size = CyberPrimitives.IconSizes.dp24,
                                tint = LocalContentColor.current
                            )
                            Text(
                                text = "SLAM MASTER TRACE PURGE",
                                style = CyberTheme.typography.terminal
                            )
                        }
                    }

                    Text(
                        text = if (isPurging) "SEVERING ALL OPTICAL RELAYS..."
                        else if (isSafetyCoverOpen) "READY // SLAM TO SEVER MATRIX"
                        else "FLIP SAFETY COVER TO ARM PURGE",
                        style = CyberTheme.typography.terminal,
                        color = if (isSafetyCoverOpen) CyberTheme.semantics.colors.warning else CyberTheme.colors.textSecondary
                    )
                }
            }
        }
        }
    }
