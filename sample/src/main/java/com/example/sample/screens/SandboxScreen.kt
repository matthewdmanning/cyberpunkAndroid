package com.example.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberAccordion
import com.example.cyberpunkandroid.components.CyberAlertVariant
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberCheckbox
import com.example.cyberpunkandroid.components.CyberSlider
import com.example.cyberpunkandroid.components.CyberSnackbar
import com.example.cyberpunkandroid.components.CyberSwitch
import com.example.cyberpunkandroid.components.CyberTabs
import com.example.cyberpunkandroid.components.CyberTime
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberDatastream
import com.example.cyberpunkandroid.effects.cyberGlowBorder
import com.example.cyberpunkandroid.effects.cyberIconPulse
import com.example.cyberpunkandroid.effects.cyberTextGlow
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIconVariant
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

@Composable
fun SandboxScreen(onBack: () -> Unit) {
    var forceFallback by remember { mutableStateOf(false) }
    var selectedMainTab by remember { mutableStateOf(0) }

    // State variables for new components
    var shieldEnabled by remember { mutableStateOf(true) }
    var outputLevel by remember { mutableFloatStateOf(0.75f) }
    var accordionExpanded by remember { mutableStateOf(false) }
    var alertBannerVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Radial Icons & Components", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Raw CyberDialTicks with Glow Modifier
            com.example.cyberpunkandroid.components.CyberDialTicks(
                modifier = Modifier.cyberTextGlow(color = CyberTheme.colors.secondary, radius = 8.dp),
                color = CyberTheme.colors.secondary,
                size = 120.dp
            )

            // Raw CyberSectorRim with Pulse Effect
            com.example.cyberpunkandroid.components.CyberSectorRim(
                modifier = Modifier.cyberIconPulse(),
                color = CyberTheme.colors.primary,
                sectorAngles = listOf(180f, 45f, 45f), // Asymmetrical sectors
                size = 120.dp
            )
        }

        // Composite CyberTime component
        CyberTime(
            timeText = "14:02:45",
            color = CyberTheme.colors.primary,
            rimSectorAngles = listOf(90f, 90f, 90f, 90f),
            size = 160.dp,
            modifier = Modifier.cyberTextGlow(color = CyberTheme.colors.primary, radius = 12.dp)
        )

        CyberButton(onClick = onBack) {
            Text("Back", color = CyberTheme.colors.background)
        }

        // Top-level Navigation Tabs
        CyberTabs(
            tabs = listOf("Core UI", "Visual Effects", "Telemetry"),
            selectedTabIndex = selectedMainTab,
            onTabSelected = { selectedMainTab = it },
            modifier = Modifier.fillMaxWidth()
        )

        if (selectedMainTab == 0) {
            // --- CORE UI TAB ---
            
            // Rendering Toggle
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Render Settings", style = CyberTheme.typography.display, color = CyberPrimitives.Colors.Yellow500)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CyberCheckbox(
                            checked = forceFallback,
                            onCheckedChange = { forceFallback = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Force Procedural Fallbacks", color = CyberTheme.colors.textPrimary)
                    }
                }
            }

            // Interactive Cyber Controls Showcase (CyberSwitch & CyberSlider)
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Interactive Controls", style = CyberTheme.typography.display, color = CyberPrimitives.Colors.Cyan500)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (shieldEnabled) "DEFENSE SHIELD: ARMED" else "DEFENSE SHIELD: STANDBY",
                            color = if (shieldEnabled) CyberTheme.semantics.colors.success else CyberTheme.colors.textSecondary,
                            style = CyberTheme.typography.body
                        )
                        CyberSwitch(
                            checked = shieldEnabled,
                            onCheckedChange = { shieldEnabled = it }
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "PRIMARY POWER OUTPUT: ${(outputLevel * 100).toInt()}%",
                            color = CyberTheme.colors.primary,
                            style = CyberTheme.typography.body
                        )
                        CyberSlider(
                            value = outputLevel,
                            onValueChange = { outputLevel = it },
                            tickCount = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Cyber Accordion Section
            CyberAccordion(
                title = "DIAGNOSTIC TELEMETRY LOGS",
                expanded = accordionExpanded,
                onExpandedChange = { accordionExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("[00:12:04] NODE_01: CORE_TEMP NORMAL (312 K)", style = CyberTheme.typography.terminal, color = CyberTheme.colors.primary)
                    Text("[00:12:18] NODE_04: ENCRYPTION HANDSHAKE ACK", style = CyberTheme.typography.terminal, color = CyberTheme.colors.secondary)
                    Text("[00:12:35] NODE_07: OVERLOAD WARNING BREACH PREVENTED", style = CyberTheme.typography.terminal, color = CyberPrimitives.Colors.Yellow500)
                }
            }

            // Transient Cyber Snackbar Banner Showcase
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("HUD Toast Notifications", style = CyberTheme.typography.display, color = CyberTheme.semantics.colors.warning)
                    
                    CyberButton(
                        onClick = { alertBannerVisible = !alertBannerVisible },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (alertBannerVisible) "DISMISS ALERT BANNER" else "TRIGGER SYSTEM ALERT",
                            color = CyberTheme.colors.background
                        )
                    }

                    CyberSnackbar(
                        title = "INTRUSION WARNING",
                        message = "UNAUTHORIZED DECRYPTION DETECTED ON SECTOR 09",
                        visible = alertBannerVisible,
                        variant = CyberAlertVariant.Warning,
                        critical = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Icon Matrix
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Icon Matrix", style = CyberTheme.typography.display, color = CyberPrimitives.Colors.Yellow500)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberTheme.colors.background.copy(alpha = 0.8f))
                            .cyberGlowBorder(CyberPrimitives.Colors.Cyan700, shape = CyberTheme.shapes.cyberCutCornerShape)
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val iconModifier = Modifier.size(56.dp)
                        CyberIcon(iconRes = CyberIcons.Zap, contentDescription = "Zap", variant = CyberIconVariant.Outline, modifier = iconModifier, tint = CyberPrimitives.Colors.Cyan500)
                        CyberIcon(iconRes = CyberIcons.Wifi, contentDescription = "Wifi", variant = CyberIconVariant.Outline, modifier = iconModifier, tint = CyberPrimitives.Colors.Magenta500)
                        CyberIcon(iconRes = CyberIcons.Shield, contentDescription = "Shield", variant = CyberIconVariant.Outline, modifier = iconModifier, tint = CyberPrimitives.Colors.Yellow500)
                        CyberIcon(iconRes = CyberIcons.Cpu, contentDescription = "Cpu", variant = CyberIconVariant.Outline, modifier = iconModifier, tint = CyberPrimitives.Colors.Green500)
                    }
                }
            }
        } else if (selectedMainTab == 1) {
            // --- SEMANTIC PAIRINGS SHOWCASE ---
            
            // Success / Datastream
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Secure / Datastream", style = CyberTheme.typography.display, color = CyberTheme.semantics.colors.success)
                    
                    CyberButton(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .cyberDatastream(color = CyberTheme.semantics.colors.success.copy(alpha=0.5f), speed = 2f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CyberIcon(iconRes = CyberIcons.Shield, contentDescription = null, variant = CyberIconVariant.Outline, tint = CyberTheme.colors.background)
                            Text("SYSTEM SECURED", color = CyberTheme.colors.background)
                        }
                    }
                }
            }

            // Danger / Overload
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Danger / Overload", style = CyberTheme.typography.display, color = CyberTheme.semantics.colors.danger)
                    
                    CyberButton(
                        onClick = {},
                        style = com.example.cyberpunkandroid.components.CyberButtonStyle.Overload,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CyberIcon(iconRes = CyberIcons.Zap, contentDescription = null, variant = CyberIconVariant.Outline, tint = CyberTheme.colors.background)
                            Text("CRITICAL BREACH", color = CyberTheme.colors.background)
                        }
                    }
                }
            }

            // Warning / Neon
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Warning / Neon", style = CyberTheme.typography.display, color = CyberTheme.semantics.colors.caution)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberTheme.colors.background)
                            .cyberGlowBorder(color = CyberTheme.semantics.colors.caution, shape = CyberTheme.shapes.cyberCutCornerShape)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CyberIcon(iconRes = CyberIcons.Wifi, contentDescription = null, variant = CyberIconVariant.Outline, tint = CyberTheme.semantics.colors.caution)
                            Text(
                                text = "PROCESSING", 
                                style = CyberTheme.typography.display, 
                                color = CyberTheme.semantics.colors.caution,
                                modifier = Modifier.cyberTextGlow(CyberTheme.semantics.colors.caution)
                            )
                        }
                    }
                }
            }
        } else {
            // --- TELEMETRY SHOWCASE ---
            var simulatedStress by remember { mutableFloatStateOf(0f) }

            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Network Uplink Stress", style = CyberTheme.typography.display, color = CyberPrimitives.Colors.Cyan500)
                    
                    // CyberSlider for telemetry stress control
                    CyberSlider(
                        value = simulatedStress,
                        onValueChange = { simulatedStress = it },
                        valueRange = 0f..1f,
                        tickCount = 4,
                        color = CyberPrimitives.Colors.Cyan500,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Example 1: Continuous Rotation (Spinner)
                    val rotation by com.example.cyberpunkandroid.effects.rememberDrivenFloatState(
                        telemetry = simulatedStress,
                        initialState = 0f,
                        updateFrequencyMs = 16L,
                        operation = { currentRot, stress ->
                            val baseSpeed = 1f
                            val stressSpeed = stress * 15f
                            (currentRot + baseSpeed + stressSpeed) % 360f
                        }
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        com.example.cyberpunkandroid.components.CyberSpinner(
                            modifier = Modifier.graphicsLayer { rotationZ = rotation }
                        )
                        Text(
                            text = "Drive state rotating at ${(1f + simulatedStress * 15f).toInt()}x speed", 
                            color = CyberTheme.colors.textPrimary
                        )
                    }

                    // Example 2: Capacitance / Heat Decay
                    val systemHeat by com.example.cyberpunkandroid.effects.rememberDrivenFloatState(
                        telemetry = simulatedStress,
                        initialState = 0f,
                        updateFrequencyMs = 50L,
                        operation = { currentHeat, stress ->
                            val heatAccumulation = stress * 0.05f
                            val coolingRate = 0.01f
                            (currentHeat + heatAccumulation - coolingRate).coerceIn(0f, 1f)
                        }
                    )
                    
                    // Generate pseudo-waveform using the heat
                    val biometricsData = remember(systemHeat) {
                        List(30) { Math.random().toFloat() * systemHeat }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "System Core Heat (Capacitance): ${(systemHeat * 100).toInt()}%", 
                            color = if (systemHeat > 0.8f) CyberTheme.semantics.colors.danger else CyberTheme.colors.textPrimary
                        )
                        
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(CyberTheme.colors.surface)) {
                            com.example.cyberpunkandroid.components.CyberBiometrics(
                                data = biometricsData,
                                modifier = Modifier.fillMaxSize(),
                                color = if (systemHeat > 0.8f) CyberTheme.semantics.colors.danger else CyberTheme.colors.primary,
                                strokeWidth = 4f
                            )
                        }
                    }
                }
            }
        }
    }
}
