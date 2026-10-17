package com.example.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberButtonSize
import com.example.cyberpunkandroid.components.CyberButtonStyle
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.sample.showcase.BioMonitorCard
import com.example.sample.showcase.BreachCipherCard
import com.example.sample.showcase.BusDiagnosticCard
import com.example.sample.showcase.ConduitTapCard
import com.example.sample.showcase.CryoRegulatorCard
import com.example.sample.showcase.IceMatrixCard
import com.example.sample.showcase.SynapticGovernorCard
import com.example.sample.showcase.TelemetryBeaconsCard

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CyberTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CyberTheme.colors.background
                ) { innerPadding ->
                    SampleApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Filter categories for the UI component catalogue.
 */
enum class CatalogueCategory(val label: String) {
    ALL("ALL COMPONENTS"),
    ICONS_SENSORS("ICONS & SENSORS"),
    INPUTS_CONTROLS("INPUTS & CONTROLS"),
    SHADERS_EFFECTS("SHADERS & PARTICLES")
}

/**
 * UI Component Catalogue for the cyberpunkandroid library.
 *
 * Organized as a browsable catalogue showcasing the library's components,
 * shaders, borders, kinetic animations, and interactive assemblies.
 */
@Composable
fun SampleApp(modifier: Modifier = Modifier) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    var selectedCategory by remember { mutableStateOf(CatalogueCategory.ALL) }

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
        // Catalogue Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)
        ) {
            Text(
                text = "CYBERPUNK ANDROID",
                style = CyberTheme.typography.display,
                color = CyberTheme.colors.primary
            )
            Text(
                text = "UI COMPONENT CATALOGUE",
                style = CyberTheme.typography.terminal,
                color = CyberTheme.colors.textPrimary
            )
            Text(
                text = "Interactive showcase of components, effects, shaders & assemblies",
                style = CyberTheme.typography.body,
                color = CyberTheme.colors.textSecondary
            )
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
        ) {
            CatalogueCategory.entries.forEach { category ->
                CyberButton(
                    style = if (selectedCategory == category) CyberButtonStyle.Primary else CyberButtonStyle.Outline,
                    size = CyberButtonSize.Small,
                    onClick = { selectedCategory = category }
                ) {
                    Text(text = category.label, style = CyberTheme.typography.terminal)
                }
            }
        }

        // -------------------------------------------------------------
        // CATALOGUE ENTRIES
        // -------------------------------------------------------------

        // 1. Telemetry Beacons (Icons & Kinetic Modifiers)
        if (selectedCategory == CatalogueCategory.ALL || selectedCategory == CatalogueCategory.ICONS_SENSORS) {
            TelemetryBeaconsCard()
        }

        // 2. Mil-Spec Hardware Bus Diagnostic (Light-pipes, flux meters & barrier panel)
        if (selectedCategory == CatalogueCategory.ALL || selectedCategory == CatalogueCategory.ICONS_SENSORS) {
            BusDiagnosticCard()
        }

        // 3. Synaptic Governor (Radial dials, hold-to-charge actuator & badges)
        if (selectedCategory == CatalogueCategory.ALL || selectedCategory == CatalogueCategory.INPUTS_CONTROLS) {
            SynapticGovernorCard()
        }

        // 4. Breach Cipher Jack-In (Decrypter, datastream, progress & buttons)
        if (selectedCategory == CatalogueCategory.ALL || selectedCategory == CatalogueCategory.INPUTS_CONTROLS) {
            BreachCipherCard()
        }

        // 5. Cryo Thermal Regulator (Sliders, switches, sector rims & thermal glow)
        if (selectedCategory == CatalogueCategory.ALL || selectedCategory == CatalogueCategory.INPUTS_CONTROLS) {
            CryoRegulatorCard()
        }

        // 6. High-Voltage Conduit Siphon (Arc weld, sparks, glowing borders & surge button)
        if (selectedCategory == CatalogueCategory.ALL || selectedCategory == CatalogueCategory.SHADERS_EFFECTS) {
            ConduitTapCard()
        }

        // 7. Arasaka IDM-800 ICE Matrix (Topology vector canvas, perimeter LED bars, detents & purge)
        if (selectedCategory == CatalogueCategory.ALL || selectedCategory == CatalogueCategory.SHADERS_EFFECTS) {
            IceMatrixCard()
        }

        // 8. TT-CRT7 Biomonitor & Cold-Jack (Curved CRT shader, scanlines, EKG sweep & emergency lever)
        if (selectedCategory == CatalogueCategory.ALL || selectedCategory == CatalogueCategory.SHADERS_EFFECTS) {
            BioMonitorCard()
        }
    }
}
