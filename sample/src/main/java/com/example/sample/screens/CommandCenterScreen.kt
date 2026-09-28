package com.example.sample.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberBadgeVariant
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberDropdown
import com.example.cyberpunkandroid.components.CyberProgress
import com.example.cyberpunkandroid.components.CyberSpinner
import com.example.cyberpunkandroid.components.CyberTabs
import com.example.cyberpunkandroid.effects.cyberDatastream
import com.example.cyberpunkandroid.effects.cyberGlowBorder
import com.example.cyberpunkandroid.effects.cyberOverload
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

@Composable
fun CommandCenterScreen(
    onNavigateToSandbox: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CyberTabs(
                tabs = listOf("Overview", "Network", "Nodes"),
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Semantic Progress Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CyberCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("UPLINK", style = CyberTheme.typography.terminal, color = CyberTheme.semantics.colors.caution)
                        CyberSpinner(color = CyberTheme.semantics.colors.caution)
                        CyberProgress(progress = 0.75f, color = CyberTheme.semantics.colors.caution)
                    }
                }
                CyberCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("DEFENSE", style = CyberTheme.typography.terminal, color = CyberTheme.semantics.colors.success)
                        CyberSpinner(color = CyberTheme.semantics.colors.success)
                        CyberProgress(progress = 1.0f, color = CyberTheme.semantics.colors.success)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("NETWORK NODES", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
            Spacer(modifier = Modifier.height(16.dp))

            // Semantic Node List showing proper matching of color, text, icon, and effects
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Warning Node
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CyberIcon(
                                iconRes = CyberIcons.Wifi,
                                contentDescription = null,
                                tint = CyberTheme.semantics.colors.caution,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("192.168.1.42", style = CyberTheme.typography.body, color = CyberTheme.colors.textPrimary)
                        }
                        CyberBadge(text = "SCANNING", variant = CyberBadgeVariant.Caution)
                    }
                }

                // Success Node
                CyberCard(modifier = Modifier.fillMaxWidth().cyberDatastream(color = CyberTheme.semantics.colors.success, speed = 1f)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CyberIcon(
                                iconRes = CyberIcons.Shield,
                                contentDescription = null,
                                tint = CyberTheme.semantics.colors.success,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("10.0.0.99", style = CyberTheme.typography.body, color = CyberTheme.colors.textPrimary)
                        }
                        CyberBadge(text = "SECURED", variant = CyberBadgeVariant.Success)
                    }
                }

                // Danger Node (Overloaded!)
                CyberCard(
                    modifier = Modifier.fillMaxWidth()
                        .cyberGlowBorder(color = CyberTheme.semantics.colors.danger, shape = CyberTheme.shapes.cyberCutCornerShape)
                        .cyberOverload(intensity = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CyberIcon(
                                iconRes = CyberIcons.Zap,
                                contentDescription = null,
                                tint = CyberTheme.semantics.colors.danger,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("172.16.254.1", style = CyberTheme.typography.body, color = CyberTheme.colors.textPrimary)
                        }
                        CyberBadge(text = "BREACH", variant = CyberBadgeVariant.Danger)
                    }
                }
            }
        }
        
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.BottomEnd
        ) {
            CyberButton(
                onClick = { menuExpanded = true }
            ) {
                Text("Actions", color = CyberTheme.colors.background)
            }
            CyberDropdown(
                items = listOf("Component Sandbox"),
                selectedIndex = -1,
                onItemSelected = { index ->
                    menuExpanded = false
                    if (index == 0) onNavigateToSandbox()
                }
            )
        }
    }
}
