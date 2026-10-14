package com.example.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import com.example.cyberpunkandroid.effects.cyberIconPulse
import com.example.cyberpunkandroid.effects.cyberIconSpin
import com.example.cyberpunkandroid.effects.cyberOverload
import com.example.cyberpunkandroid.effects.cyberPing
import com.example.cyberpunkandroid.effects.cyberSpark
import com.example.cyberpunkandroid.effects.cyberWeld
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.sample.showcase.BioMonitorCard
import com.example.sample.showcase.BusDiagnosticCard
import com.example.sample.showcase.IceMatrixCard
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
    Column(
        modifier = modifier
            .safeDrawingPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(CyberTheme.spacing.dp16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16)
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

        // LEVEL 1 (CARD 2): Roadmap card 6, Mil-Spec Hardware Bus Diagnostic.
        BusDiagnosticCard()

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
                // Tactile radial actuator core with accessible touch target (Hero Icon scaled to dp32)
                Box(
                    modifier = Modifier.size(108.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CyberDialTicks(
                        size = 108.dp,
                        color = CyberTheme.colors.border.copy(alpha = 0.4f),
                        tickCount = 40,
                        majorTickInterval = 5
                    )
                    CyberRim(
                        progress = bypassProgress,
                        modifier = Modifier.size(90.dp),
                        color = if (isBypassed) CyberTheme.semantics.colors.danger else CyberTheme.colors.primary,
                        backgroundColor = CyberTheme.colors.surfaceSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (isBypassed) CyberTheme.semantics.colors.danger.copy(alpha = 0.2f)
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
                            size = CyberPrimitives.IconSizes.dp32,
                            tint = if (isBypassed) CyberTheme.semantics.colors.danger else CyberTheme.colors.primary
                        )
                    }
                }

                // Supporting semantic readouts and status badge
                Column(
                    modifier = Modifier.padding(start = CyberTheme.spacing.dp16),
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
                        CyberIcon(
                            iconRes = if (breachProgress >= 0.95f) CyberIcons.Unlock else CyberIcons.Lock,
                            contentDescription = null,
                            size = CyberPrimitives.IconSizes.dp16,
                            tint = if (breachProgress >= 0.95f) CyberTheme.semantics.colors.success else CyberTheme.semantics.colors.warning
                        )
                        Text(
                            text = if (breachProgress >= 0.95f) "UNLOCKED" else "LOCKED",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textPrimary
                        )
                    }
                }

                // Dedicated decrypter terminal display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CyberTheme.shapes.roundedMd)
                        .background(CyberTheme.colors.surfaceSecondary)
                        .padding(horizontal = CyberTheme.spacing.dp12, vertical = CyberTheme.spacing.dp12)
                ) {
                    CyberDecrypter(
                        targetText = "KEY: 0x9E_ARASAKA_ROOT",
                        progress = breachProgress
                    )
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
        // LEVEL 3: Multi-variable linked reactive loop
        // Base Item: Subdermal dual-channel thermal regulator panel
        // Function: Switch (clock multiplier) + Slider (LN2 pump) driving coupled temp
        // Styling: CyberSectorRim temp gauge, spinning turbine, cyberOverload jitter
        // =========================================================================
        var isOverclocked by remember { mutableStateOf(false) }
        var coolantRate by remember { mutableFloatStateOf(0.35f) }

        // Thermodynamic calculation:
        // Nominal body temp: 37°C. Overclock adds 52°C heat. Coolant extracts up to 48°C.
        val coreTemp = (37f + (if (isOverclocked) 52f else 0f) - (coolantRate * 48f)).coerceIn(24f, 96f)
        val isCriticalTemp = coreTemp >= 75f
        val isElevatedTemp = coreTemp >= 50f

        val tempColor = when {
            isCriticalTemp -> CyberTheme.semantics.colors.danger
            isElevatedTemp -> CyberTheme.semantics.colors.warning
            else -> CyberTheme.colors.primary
        }

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
                    // Hero Gauge Cluster: Sector rim + Net Temperature + Turbine (Gauge centerpiece icon: dp24)
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .cyberOverload(enabled = isCriticalTemp, intensity = 0.04f),
                        contentAlignment = Alignment.Center
                    ) {
                        CyberSectorRim(
                            size = 92.dp,
                            color = tempColor,
                            sectorAngles = listOf(70f, 70f, 70f, 70f),
                            thickness = 3.dp
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${coreTemp.toInt()}°C",
                                style = CyberTheme.typography.terminal,
                                color = tempColor
                            )
                            CyberIcon(
                                iconRes = CyberIcons.Sync,
                                contentDescription = null,
                                size = CyberPrimitives.IconSizes.dp24,
                                tint = tempColor,
                                modifier = Modifier.cyberIconSpin()
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
                                .padding(vertical = CyberTheme.spacing.dp4),
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
                                onValueChange = { coolantRate = it },
                                trackHeight = 6.dp
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
                        else if (isOverclocked) "HIGH FLUX"
                        else "NOMINAL",
                        variant = if (isCriticalTemp) CyberBadgeVariant.Danger
                        else if (isOverclocked) CyberBadgeVariant.Caution
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

        // LEVEL 3 (CARD 2): Roadmap card 7, ICE Detection Matrix (Display and Backbone tabs).
        IceMatrixCard()

        // =========================================================================
        // LEVEL 4: High-impact visual spectacle linked with interactivity
        // Base Item: High-voltage power conduit tap with active arc weld and decoupling clamp
        // Function: Autonomous cyberWeld border, button triggers 10x cyberSpark + cyberOverload,
        //           hold-to-charge clamp disconnects tap and quenches arc
        // Styling: cyberWeld outline, incandescent cyberSpark burst, AGSL chromatic overload
        // =========================================================================
        var isTapConnected by remember { mutableStateOf(true) }
        var isTapSurging by remember { mutableStateOf(false) }
        var severProgress by remember { mutableFloatStateOf(0f) }

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
                        color = CyberTheme.colors.primary
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
                // Welded Power Chamber: Autonomous perimeter arc weld + 10x spark eruption
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(CyberTheme.shapes.cyberCutCornerShape)
                        .background(CyberTheme.colors.surfaceSecondary)
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
                                Modifier
                                    .cyberSpark(
                                        sparkCount = 36,
                                        intensity = 1.0f,
                                        color = CyberTheme.colors.primary,
                                        secondaryColor = CyberTheme.semantics.colors.warning
                                    )
                                    .cyberOverload(intensity = 0.05f)
                            } else Modifier
                        )
                        .padding(horizontal = CyberTheme.spacing.dp16, vertical = CyberTheme.spacing.dp12),
                    contentAlignment = Alignment.CenterStart
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
                                text = if (isTapConnected) "480V AC" else "0V (COLD)",
                                style = CyberTheme.typography.terminal,
                                color = if (isTapConnected) CyberTheme.colors.primary else CyberTheme.colors.textSecondary
                            )
                            Text(
                                text = if (!isTapConnected) "0.0A"
                                else if (isTapSurging) "38.6A PEAK"
                                else "14.2A LOAD",
                                style = CyberTheme.typography.terminal,
                                color = if (isTapSurging) CyberTheme.semantics.colors.warning else CyberTheme.colors.textPrimary
                            )
                        }

                        Text(
                            text = if (!isTapConnected) "LINE SEVERED // CLAMP OPEN"
                            else if (isTapSurging) "OVERDRIVE SURGE DISCHARGE"
                            else "POWER DRAW: 6.8 kW",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
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

                // Decoupling Safety Clamp Bar with generous padding and touch target
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = CyberTheme.spacing.dp4),
                    horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CyberRim(
                            progress = severProgress,
                            modifier = Modifier.size(56.dp),
                            color = if (isTapConnected) CyberTheme.semantics.colors.danger else CyberTheme.colors.primary,
                            backgroundColor = CyberTheme.colors.surfaceTertiary
                        )
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (!isTapConnected) CyberTheme.semantics.colors.danger.copy(alpha = 0.2f)
                                    else CyberTheme.colors.surfaceSecondary
                                )
                                .cyberLongPressFill(
                                    durationMillis = 1000L,
                                    onProgressUpdate = { severProgress = it },
                                    onComplete = {
                                        isTapConnected = !isTapConnected
                                        isTapSurging = false
                                        severProgress = 0f
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CyberIcon(
                                iconRes = if (isTapConnected) CyberIcons.Unlink else CyberIcons.Check,
                                contentDescription = null,
                                size = CyberPrimitives.IconSizes.dp24,
                                tint = if (isTapConnected) CyberTheme.semantics.colors.danger else CyberTheme.semantics.colors.success
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)
                    ) {
                        Text(
                            text = if (severProgress > 0f) "SEVERING: ${(severProgress * 100).toInt()}%"
                            else if (isTapConnected) "SAFETY CLAMP" else "LINE DISCONNECTED",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textPrimary
                        )
                        Text(
                            text = "HOLD TO ${if (isTapConnected) "SEVER" else "RESTORE"}",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        // LEVEL 4 (CARD 2): Roadmap card 8, CRT Biomonitor (Display and Backbone tabs).
        BioMonitorCard()
    }
}
