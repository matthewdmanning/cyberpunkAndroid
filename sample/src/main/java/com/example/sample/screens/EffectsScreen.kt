package com.example.sample.screens

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberButtonSize
import com.example.cyberpunkandroid.components.CyberButtonStyle
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberTabs
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberInteractionTrigger
import com.example.cyberpunkandroid.effects.cyberDatastream
import com.example.cyberpunkandroid.effects.cyberGlowBorderFlow
import com.example.cyberpunkandroid.effects.cyberIconPulse
import com.example.cyberpunkandroid.effects.cyberIconSpin
import com.example.cyberpunkandroid.effects.cyberNoise
import com.example.cyberpunkandroid.effects.cyberOverload
import com.example.cyberpunkandroid.effects.cyberScanlines
import com.example.cyberpunkandroid.effects.cyberTextGlow
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIconVariant
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp

@Composable
fun EffectsScreen() {
    val row1 = listOf("Sizing", "Icons", "Overload")
    val row2 = listOf("Neon", "Scan", "Noise")
    val row3 = listOf("Data", "Glow", "Mix")
    var selectedTab by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberTheme.colors.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyberTabs(
                tabs = row1,
                selectedTabIndex = if (selectedTab in 0..2) selectedTab else -1,
                onTabSelected = { selectedTab = it }
            )
            CyberTabs(
                tabs = row2,
                selectedTabIndex = if (selectedTab in 3..5) selectedTab - 3 else -1,
                onTabSelected = { selectedTab = it + 3 }
            )
            CyberTabs(
                tabs = row3,
                selectedTabIndex = if (selectedTab in 6..8) selectedTab - 6 else -1,
                onTabSelected = { selectedTab = it + 6 }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> SemanticSizingTab()
                1 -> IconAnimationsTab()
                2 -> OverloadTab()
                3 -> NeonBorderTab()
                4 -> ScanlinesTab()
                5 -> NoiseOverlayTab()
                6 -> DatastreamTab()
                7 -> TextGlowTab()
                8 -> CombinedEffectsTab()
            }
        }
    }
}

/**
 * Reusable row demonstrating an effect scaling across all CyberPrimitives.IconSizes.
 */
@Composable
fun IconScalingRow(
    title: String,
    iconRes: Int = CyberIcons.Terminal,
    tint: Color = CyberTheme.colors.primary,
    variant: CyberIconVariant = CyberIconVariant.Outline,
    subtitle: String = "Scaling across CyberPrimitives.IconSizes (dp16 -> dp64)",
    iconModifier: @Composable (Dp) -> Modifier = { Modifier }
) {
    val sizes = listOf(
        "16dp" to CyberPrimitives.IconSizes.dp16,
        "24dp" to CyberPrimitives.IconSizes.dp24,
        "32dp" to CyberPrimitives.IconSizes.dp32,
        "48dp" to CyberPrimitives.IconSizes.dp48,
        "64dp" to CyberPrimitives.IconSizes.dp64,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, color = CyberTheme.colors.primary, style = CyberTheme.typography.display)
        Text(text = subtitle, color = CyberTheme.colors.textSecondary, style = CyberTheme.typography.terminal)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            sizes.forEach { (label, size) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.padding(6.dp)) {
                        CyberIcon(
                            iconRes = iconRes,
                            contentDescription = label,
                            tint = tint,
                            variant = variant,
                            size = size,
                            modifier = iconModifier(size)
                        )
                    }
                    Text(
                        text = label,
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Reusable row demonstrating icon animation speed variation across fixed 32.dp icons.
 */
@Composable
fun IconSpeedRow(
    title: String,
    iconRes: Int = CyberIcons.Loading,
    tint: Color = CyberTheme.colors.primary,
    variant: CyberIconVariant = CyberIconVariant.Outline,
    subtitle: String = "Speed progression across fixed 32.dp icons (500ms -> 3000ms)",
    iconModifier: @Composable (durationMillis: Int) -> Modifier
) {
    val speeds = listOf(
        "500ms" to 500,
        "1000ms" to 1000,
        "1500ms" to 1500,
        "2000ms" to 2000,
        "3000ms" to 3000,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, color = CyberTheme.colors.primary, style = CyberTheme.typography.display)
        Text(text = subtitle, color = CyberTheme.colors.textSecondary, style = CyberTheme.typography.terminal)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            speeds.forEach { (label, duration) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.padding(6.dp)) {
                        CyberIcon(
                            iconRes = iconRes,
                            contentDescription = label,
                            tint = tint,
                            variant = variant,
                            size = CyberPrimitives.IconSizes.dp32,
                            modifier = iconModifier(duration)
                        )
                    }
                    Text(
                        text = label,
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Tab 0: Semantic Sizing
// -------------------------------------------------------------------------

@Composable
fun SemanticSizingTab() {
    val semanticSizes = listOf(
        "16dp" to CyberPrimitives.IconSizes.dp16,
        "24dp" to CyberPrimitives.IconSizes.dp24,
        "32dp" to CyberPrimitives.IconSizes.dp32,
        "48dp" to CyberPrimitives.IconSizes.dp48,
        "64dp" to CyberPrimitives.IconSizes.dp64,
    )

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // First row: Render CyberIcons of sizes dp16, dp24, dp32, dp48, dp64
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "BASE SEMANTIC SIZES",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "CyberPrimitives.IconSizes: dp16, dp24, dp32, dp48, dp64",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                semanticSizes.forEach { (label, size) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CyberIcon(
                            iconRes = CyberIcons.Terminal,
                            contentDescription = label,
                            tint = CyberTheme.colors.primary,
                            size = size
                        )
                        Text(
                            text = label,
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        // Row 2: cyberTextGlow(radius) mapped across semantic sizes
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "TEXT GLOW RADIUS MAPPING",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "Base icon size: dp32 | radius: dp16, dp24, dp32, dp48, dp64",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                semanticSizes.forEach { (label, dim) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            CyberIcon(
                                iconRes = CyberIcons.Chip,
                                contentDescription = label,
                                tint = CyberTheme.colors.primary,
                                size = CyberPrimitives.IconSizes.dp32,
                                modifier = Modifier.cyberTextGlow(CyberTheme.colors.primary, radius = dim)
                            )
                        }
                        Text(
                            text = "r = $label",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        // Row 3: cyberGlowBorderFlow(glowRadius) mapped across semantic sizes
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "NEON BORDER FLOW GLOW RADIUS MAPPING",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "Base icon size: dp32 | glowRadius: dp16, dp24, dp32, dp48, dp64",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                semanticSizes.forEach { (label, dim) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            CyberIcon(
                                iconRes = CyberIcons.Circuit,
                                contentDescription = label,
                                tint = CyberTheme.colors.primary,
                                size = CyberPrimitives.IconSizes.dp32,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .cyberGlowBorderFlow(
                                        shape = CyberTheme.shapes.cyberCutCornerShapeSmall,
                                        glowRadius = dim
                                    )
                            )
                        }
                        Text(
                            text = "glow = $label",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        // Row 4: cyberScanlines(spacing) mapped across semantic sizes
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "SCANLINES SPACING MAPPING",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "Base icon size: dp32 | spacing: dp16, dp24, dp32, dp48, dp64",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                semanticSizes.forEach { (label, dim) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            CyberIcon(
                                iconRes = CyberIcons.Signal,
                                contentDescription = label,
                                tint = CyberTheme.semantics.colors.success,
                                size = CyberPrimitives.IconSizes.dp32,
                                modifier = Modifier.cyberScanlines(spacing = dim, speed = 0.5f)
                            )
                        }
                        Text(
                            text = "space = $label",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Tab 1: Icon Animations
// -------------------------------------------------------------------------

@Composable
fun IconAnimationsTab() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Spin Animation", color = CyberTheme.colors.primary, style = CyberTheme.typography.display)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CyberIcon(iconRes = CyberIcons.Loading, contentDescription = null, tint = CyberTheme.colors.primary, size = CyberPrimitives.IconSizes.dp32, modifier = Modifier.cyberTextGlow(CyberTheme.colors.primary).cyberIconSpin())
            CyberIcon(iconRes = CyberIcons.Settings, contentDescription = null, tint = CyberTheme.semantics.colors.danger, size = CyberPrimitives.IconSizes.dp32, modifier = Modifier.cyberTextGlow(CyberTheme.semantics.colors.danger).cyberIconSpin())
            CyberIcon(iconRes = CyberIcons.Progress, contentDescription = null, tint = CyberTheme.semantics.colors.success, size = CyberPrimitives.IconSizes.dp32, modifier = Modifier.cyberTextGlow(CyberTheme.semantics.colors.success).cyberIconSpin())
        }

        IconSpeedRow(
            title = "Spin Speed",
            iconRes = CyberIcons.Loading
        ) { duration ->
            Modifier.cyberIconSpin(animationSpec = infiniteRepeatable(animation = tween(duration, easing = LinearEasing), repeatMode = RepeatMode.Restart))
        }

        Text("Pulse Animation", color = CyberTheme.colors.primary, style = CyberTheme.typography.display)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CyberIcon(iconRes = CyberIcons.Signal, contentDescription = null, tint = CyberTheme.colors.primary, size = CyberPrimitives.IconSizes.dp32, modifier = Modifier.cyberTextGlow(CyberTheme.colors.primary).cyberIconPulse())
            CyberIcon(iconRes = CyberIcons.Wifi, contentDescription = null, tint = CyberTheme.semantics.colors.danger, size = CyberPrimitives.IconSizes.dp32, modifier = Modifier.cyberTextGlow(CyberTheme.semantics.colors.danger).cyberIconPulse())
        }

        IconSpeedRow(
            title = "Pulse Speed",
            iconRes = CyberIcons.Signal,
            tint = CyberTheme.semantics.colors.danger
        ) { duration ->
            Modifier.cyberIconPulse(animationSpec = infiniteRepeatable(animation = tween(duration / 2, easing = LinearEasing), repeatMode = RepeatMode.Reverse))
        }

        Text("Overload Animation", color = CyberTheme.colors.primary, style = CyberTheme.typography.display)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CyberIcon(iconRes = CyberIcons.Warning, contentDescription = null, tint = CyberTheme.semantics.colors.warning, variant = CyberIconVariant.Overload, size = CyberPrimitives.IconSizes.dp32, modifier = Modifier.cyberTextGlow(CyberTheme.semantics.colors.warning))
            CyberIcon(iconRes = CyberIcons.Error, contentDescription = null, tint = CyberTheme.semantics.colors.danger, variant = CyberIconVariant.Overload, size = CyberPrimitives.IconSizes.dp32, modifier = Modifier.cyberTextGlow(CyberTheme.semantics.colors.danger))
            CyberIcon(iconRes = CyberIcons.Terminal, contentDescription = null, tint = CyberTheme.colors.primary, variant = CyberIconVariant.Overload, size = CyberPrimitives.IconSizes.dp32, modifier = Modifier.cyberTextGlow(CyberTheme.colors.primary))
        }

        IconScalingRow(
            title = "Icon Sizes (dp16 - dp64)",
            iconRes = CyberIcons.Terminal
        )
    }
}

// -------------------------------------------------------------------------
// Tab 2: Overload Effect
// -------------------------------------------------------------------------

@Composable
fun OverloadEffectTab() {
    OverloadTab()
}

@Composable
fun OverloadTab() {
    var overloadActive by remember { mutableStateOf(true) }

    val animationSpecs: List<Pair<String, AnimationSpec<Float>>> = listOf(
        "Spring (Bouncy)" to spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        "Spring (Stiff)" to spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        "Tween (1s LinearOutSlowIn)" to tween(
            durationMillis = 1000,
            easing = LinearOutSlowInEasing
        ),
        "Tween (400ms FastOutSlowIn)" to tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        "Infinite (500ms Reverse)" to infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        "Infinite (800ms Restart)" to infiniteRepeatable(
            animation = tween(800, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart
        ),
        "Keyframes" to keyframes {
            durationMillis = 1000
            0.0f at 0
            0.8f at 250
            0.2f at 600
            0.5f at 1000
        },
        "Repeatable (3x Reverse)" to repeatable(
            iterations = 3,
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Row 1: Intensity Progression
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "INTENSITY PROGRESSIONS",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "cyberOverload(intensity): 1 -> 10",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                listOf(1f, 3f, 5f, 7f, 10f).forEach { intensityVal ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.padding(4.dp)) {
                            CyberIcon(
                                iconRes = CyberIcons.Warning,
                                contentDescription = "Intensity ${intensityVal.toInt()}",
                                tint = CyberTheme.colors.primary,
                                variant = CyberIconVariant.Overload,
                                size = CyberPrimitives.IconSizes.dp48,
                                modifier = Modifier.cyberOverload(intensity = intensityVal)
                            )
                        }
                        Text(
                            text = "${intensityVal.toInt()}",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        // Row 2: 8 Identical CyberIcons with intensity = 3f and reduced 4.dp spacing
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ANIMATION SPEC PROGRESSIONS (INTENSITY 3)",
                        color = CyberTheme.colors.primary,
                        style = CyberTheme.typography.display
                    )
                    Text(
                        text = "8 icons with intensity = 3f across Compose AnimationSpecs",
                        color = CyberTheme.colors.textSecondary,
                        style = CyberTheme.typography.terminal
                    )
                }
                CyberButton(
                    onClick = { overloadActive = !overloadActive },
                    style = CyberButtonStyle.Outline,
                    size = CyberButtonSize.Small
                ) {
                    Text(if (overloadActive) "PULSE: ON" else "PULSE: OFF")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                animationSpecs.forEach { (name, spec) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(modifier = Modifier.padding(2.dp)) {
                            CyberIcon(
                                iconRes = CyberIcons.Warning,
                                contentDescription = name,
                                tint = CyberTheme.colors.primary,
                                variant = CyberIconVariant.Overload,
                                size = CyberPrimitives.IconSizes.dp48,
                                modifier = Modifier.cyberOverload(
                                    intensity = 3f,
                                    animationSpec = spec,
                                    trigger = if (overloadActive) CyberInteractionTrigger.ALWAYS else CyberInteractionTrigger.NONE
                                )
                            )
                        }
                        Text(
                            text = name,
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        // Row 3: 8 Identical CyberIcons with intensity = 5f and 4.dp spacing
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "ANIMATION SPEC PROGRESSIONS (INTENSITY 5)",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "8 icons with intensity = 5f across Compose AnimationSpecs",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                animationSpecs.forEach { (name, spec) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(modifier = Modifier.padding(2.dp)) {
                            CyberIcon(
                                iconRes = CyberIcons.Warning,
                                contentDescription = name,
                                tint = CyberTheme.colors.primary,
                                variant = CyberIconVariant.Overload,
                                size = CyberPrimitives.IconSizes.dp48,
                                modifier = Modifier.cyberOverload(
                                    intensity = 5f,
                                    animationSpec = spec,
                                    trigger = if (overloadActive) CyberInteractionTrigger.ALWAYS else CyberInteractionTrigger.NONE
                                )
                            )
                        }
                        Text(
                            text = name,
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        // Row 4: 8 Identical CyberIcons with intensity = 7f and 4.dp spacing
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "ANIMATION SPEC PROGRESSIONS (INTENSITY 7)",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "8 icons with intensity = 7f across Compose AnimationSpecs",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                animationSpecs.forEach { (name, spec) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(modifier = Modifier.padding(2.dp)) {
                            CyberIcon(
                                iconRes = CyberIcons.Warning,
                                contentDescription = name,
                                tint = CyberTheme.colors.primary,
                                variant = CyberIconVariant.Overload,
                                size = CyberPrimitives.IconSizes.dp48,
                                modifier = Modifier.cyberOverload(
                                    intensity = 7f,
                                    animationSpec = spec,
                                    trigger = if (overloadActive) CyberInteractionTrigger.ALWAYS else CyberInteractionTrigger.NONE
                                )
                            )
                        }
                        Text(
                            text = name,
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        // Row 3: Scaling across IconSizes
        IconScalingRow(
            title = "OVERLOAD EFFECT SCALING (dp16 - dp64)",
            iconRes = CyberIcons.Warning,
            variant = CyberIconVariant.Overload
        ) {
            Modifier.cyberOverload(intensity = 0.5f)
        }
    }
}

// -------------------------------------------------------------------------
// Tab 3: Neon Border
// -------------------------------------------------------------------------

@Composable
fun NeonBorderTab() {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        CyberCard(
            modifier = Modifier.cyberGlowBorderFlow(
                colors = listOf(CyberTheme.colors.primary, CyberTheme.colors.secondary),
                shape = CyberTheme.shapes.cyberCutCornerShape,
                glowRadius = CyberPrimitives.Spacing.dp24
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                CyberIcon(iconRes = CyberIcons.Circuit, contentDescription = null, tint = CyberTheme.colors.primary, size = CyberPrimitives.IconSizes.dp24)
                Spacer(modifier = Modifier.width(16.dp))
                Text("NEON GLOW EFFECT", style = CyberTheme.typography.display, color = CyberTheme.colors.textPrimary)
            }
        }

        CyberCard(
            modifier = Modifier.cyberGlowBorderFlow(
                colors = listOf(CyberTheme.semantics.colors.danger, CyberPrimitives.Colors.Magenta500),
                shape = CyberTheme.shapes.cyberCutCornerShape
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                CyberIcon(iconRes = CyberIcons.Shield, contentDescription = null, tint = CyberTheme.semantics.colors.danger, size = CyberPrimitives.IconSizes.dp24, variant = CyberIconVariant.Duotone)
                Spacer(modifier = Modifier.width(16.dp))
                Text("MAGENTA VARIANT", style = CyberTheme.typography.display, color = CyberTheme.semantics.colors.danger)
            }
        }

        CyberCard(modifier = Modifier.cyberGlowBorderFlow(shape = CyberTheme.shapes.cyberCutCornerShape)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                CyberIcon(iconRes = CyberIcons.Chip, contentDescription = null, tint = CyberTheme.colors.primary, size = CyberPrimitives.IconSizes.dp24)
                Spacer(modifier = Modifier.width(16.dp))
                Text("ANIMATED FLOW VARIANT", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
            }
        }

        IconScalingRow(
            title = "NEON BORDER SCALING (dp16 - dp64)",
            iconRes = CyberIcons.Circuit
        ) {
            Modifier
                .padding(6.dp)
                .cyberGlowBorderFlow(
                    shape = CyberTheme.shapes.cyberCutCornerShapeSmall,
                    glowRadius = CyberPrimitives.Spacing.dp8
                )
        }
    }
}

// -------------------------------------------------------------------------
// Tab 4: Scanlines (CRT)
// -------------------------------------------------------------------------

@Composable
fun ScanlinesTab() {
    val infiniteTransition = rememberInfiniteTransition(label = "ScanlinesTransition")
    val x by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ScanlineX"
    )

    // Dynamic mapping functions over animated x: 0..1
    val linearOpacity = x.coerceIn(0f, 1f)
    val quadraticOpacity = (1f - x * x).coerceIn(0f, 1f)
    val diff = (x - 0.5f) * 4f
    val gaussianOpacity = exp(-diff * diff).coerceIn(0f, 1f)
    val cosineOpacity = abs(cos(x * PI.toFloat())).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "SCANLINES 2x2 DYNAMIC MATRIX",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "Progress x = ${String.format(Locale.US, "%.2f", x)} driving card opacity via mapping functions",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
        }

        // 2x2 Grid: Column of 2 Rows of CyberCards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Row 1: Linear & Quadratic
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberCard(
                    modifier = Modifier
                        .weight(1f)
                        .cyberScanlines(opacity = linearOpacity)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CyberIcon(
                            iconRes = CyberIcons.Terminal,
                            contentDescription = "Linear",
                            tint = CyberTheme.colors.primary,
                            size = CyberPrimitives.IconSizes.dp24
                        )
                        Text(
                            text = "Linear",
                            style = CyberTheme.typography.display,
                            color = CyberTheme.colors.primary
                        )
                        Text(
                            text = "f(x) = x",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                        Text(
                            text = "α = ${String.format(Locale.US, "%.2f", linearOpacity)}",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.primary
                        )
                    }
                }

                CyberCard(
                    modifier = Modifier
                        .weight(1f)
                        .cyberScanlines(opacity = quadraticOpacity)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CyberIcon(
                            iconRes = CyberIcons.Signal,
                            contentDescription = "Quadratic",
                            tint = CyberTheme.colors.primary,
                            size = CyberPrimitives.IconSizes.dp24
                        )
                        Text(
                            text = "Quadratic",
                            style = CyberTheme.typography.display,
                            color = CyberTheme.colors.primary
                        )
                        Text(
                            text = "1 - x²",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                        Text(
                            text = "α = ${String.format(Locale.US, "%.2f", quadraticOpacity)}",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.primary
                        )
                    }
                }
            }

            // Row 2: Gaussian & Cosine
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberCard(
                    modifier = Modifier
                        .weight(1f)
                        .cyberScanlines(opacity = gaussianOpacity)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CyberIcon(
                            iconRes = CyberIcons.Cpu,
                            contentDescription = "Gaussian",
                            tint = CyberTheme.colors.primary,
                            size = CyberPrimitives.IconSizes.dp24
                        )
                        Text(
                            text = "Gaussian",
                            style = CyberTheme.typography.display,
                            color = CyberTheme.colors.primary
                        )
                        Text(
                            text = "exp(-((x-0.5)*4)²)",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                        Text(
                            text = "α = ${String.format(Locale.US, "%.2f", gaussianOpacity)}",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.primary
                        )
                    }
                }

                CyberCard(
                    modifier = Modifier
                        .weight(1f)
                        .cyberScanlines(opacity = cosineOpacity)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CyberIcon(
                            iconRes = CyberIcons.Wifi,
                            contentDescription = "Cosine",
                            tint = CyberTheme.colors.primary,
                            size = CyberPrimitives.IconSizes.dp24
                        )
                        Text(
                            text = "Cosine",
                            style = CyberTheme.typography.display,
                            color = CyberTheme.colors.primary
                        )
                        Text(
                            text = "|cos(x · π)|",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                        Text(
                            text = "α = ${String.format(Locale.US, "%.2f", cosineOpacity)}",
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.primary
                        )
                    }
                }
            }
        }

        // Replicate of scanlines effect scaling across CyberPrimitives.IconSizes
        IconScalingRow(
            title = "SCANLINES EFFECT SCALING (dp16 - dp64)",
            iconRes = CyberIcons.Terminal
        ) {
            Modifier.cyberScanlines(spacing = CyberPrimitives.Spacing.dp4)
        }
    }
}

// -------------------------------------------------------------------------
// Tab 5: Noise Overlay
// -------------------------------------------------------------------------

@Composable
fun NoiseOverlayTab() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "NOISE PARAMETER MATRIX (48.DP ICONS)",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "Rows and columns showcasing varying frequency, speed, and opacity parameters",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
        }

        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Signal,
                        contentDescription = "Low Noise",
                        tint = CyberTheme.semantics.colors.danger,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberNoise(opacity = 0.2f, speed = 0.5f)
                    )
                    Text("Low Noise", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("α=0.2 spd=0.5", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Wifi,
                        contentDescription = "Standard Noise",
                        tint = CyberTheme.colors.primary,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberNoise(opacity = 0.4f, speed = 1.0f)
                    )
                    Text("Standard", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("α=0.4 spd=1.0", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Cpu,
                        contentDescription = "High Speed Noise",
                        tint = CyberTheme.semantics.colors.warning,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberNoise(opacity = 0.35f, speed = 2.5f)
                    )
                    Text("High Speed", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("α=0.35 spd=2.5", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Circuit,
                        contentDescription = "Static Noise",
                        tint = CyberTheme.semantics.colors.success,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberNoise(opacity = 0.5f, animated = false)
                    )
                    Text("Static Noise", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("α=0.5 animated=F", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Terminal,
                        contentDescription = "Heavy Grain Noise",
                        tint = CyberTheme.colors.primary,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberNoise(opacity = 0.7f, speed = 1.8f)
                    )
                    Text("Heavy Grain", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("α=0.7 spd=1.8", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Shield,
                        contentDescription = "Extreme Noise",
                        tint = CyberTheme.semantics.colors.danger,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberNoise(opacity = 0.85f, speed = 3.0f)
                    )
                    Text("Extreme Static", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("α=0.85 spd=3.0", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
        }

        IconScalingRow(
            title = "NOISE EFFECT SCALING (dp16 - dp64)",
            iconRes = CyberIcons.Signal,
            tint = CyberTheme.semantics.colors.danger
        ) {
            Modifier.cyberNoise()
        }
    }
}

// -------------------------------------------------------------------------
// Tab 6: Datastream
// -------------------------------------------------------------------------

@Composable
fun DatastreamTab() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "DATASTREAM PARAMETER MATRIX (48.DP ICONS)",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "Rows and columns showcasing varying speed, maxAlpha, and mirror parameters",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
        }

        // Row 1: Single streams
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Download,
                        contentDescription = "Slow Single",
                        tint = CyberTheme.semantics.colors.success,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberDatastream(
                            color = CyberTheme.semantics.colors.success,
                            speed = 0.5f,
                            maxAlpha = 0.35f,
                            mirror = false
                        )
                    )
                    Text("Slow Stream", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("spd=0.5 α=0.35", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Upload,
                        contentDescription = "Fast Single",
                        tint = CyberTheme.colors.primary,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberDatastream(
                            color = CyberTheme.colors.primary,
                            speed = 1.5f,
                            maxAlpha = 0.7f,
                            mirror = false
                        )
                    )
                    Text("Fast Stream", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("spd=1.5 α=0.7", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
        }

        // Row 2: Mirrored streams
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Sync,
                        contentDescription = "Standard Mirror",
                        tint = CyberTheme.semantics.colors.warning,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberDatastream(
                            color = CyberTheme.semantics.colors.warning,
                            speed = 1.0f,
                            maxAlpha = 0.5f,
                            mirror = true
                        )
                    )
                    Text("Mirror Stream", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("mirror=true spd=1.0", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Globe,
                        contentDescription = "Hyper Mirror",
                        tint = CyberTheme.semantics.colors.danger,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberDatastream(
                            color = CyberTheme.semantics.colors.danger,
                            speed = 2.0f,
                            maxAlpha = 0.85f,
                            mirror = true
                        )
                    )
                    Text("Hyper Mirror", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("mirror=true spd=2.0", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
        }

        // Row 3: Dual sweep & Magenta overclock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Signal,
                        contentDescription = "Slow Mirror",
                        tint = CyberTheme.semantics.colors.success,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberDatastream(
                            color = CyberTheme.semantics.colors.success,
                            speed = 0.7f,
                            maxAlpha = 0.6f,
                            mirror = true
                        )
                    )
                    Text("Dual Sweep", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("mirror=true spd=0.7", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Chip,
                        contentDescription = "Overclocked",
                        tint = CyberPrimitives.Colors.Magenta500,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberDatastream(
                            color = CyberPrimitives.Colors.Magenta500,
                            speed = 2.5f,
                            maxAlpha = 0.9f,
                            mirror = true
                        )
                    )
                    Text("Overclocked", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("mirror=true spd=2.5", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
        }

        IconScalingRow(
            title = "DATASTREAM SCALING (dp16 - dp64)",
            iconRes = CyberIcons.Download,
            tint = CyberTheme.semantics.colors.success
        ) {
            Modifier.cyberDatastream(color = CyberTheme.semantics.colors.success)
        }
    }
}

// -------------------------------------------------------------------------
// Tab 7: Text Glow
// -------------------------------------------------------------------------

@Composable
fun TextGlowTab() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "TEXT GLOW PARAMETER MATRIX (48.DP ICONS)",
                color = CyberTheme.colors.primary,
                style = CyberTheme.typography.display
            )
            Text(
                text = "Rows and columns showcasing varying radius, intensity, and color parameters",
                color = CyberTheme.colors.textSecondary,
                style = CyberTheme.typography.terminal
            )
        }

        // Row 1: Primary Cyan varying radius & intensity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Chip,
                        contentDescription = "Soft Glow",
                        tint = CyberTheme.colors.primary,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberTextGlow(
                            color = CyberTheme.colors.primary,
                            radius = 8.dp,
                            intensity = 1f
                        )
                    )
                    Text("Soft Bloom", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("r=8dp i=1", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Terminal,
                        contentDescription = "Medium Glow",
                        tint = CyberTheme.colors.primary,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberTextGlow(
                            color = CyberTheme.colors.primary,
                            radius = 16.dp,
                            intensity = 2f
                        )
                    )
                    Text("Medium Bloom", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("r=16dp i=2", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
        }

        // Row 2: Vibrant colors & higher intensity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Shield,
                        contentDescription = "Danger Glow",
                        tint = CyberTheme.semantics.colors.danger,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberTextGlow(
                            color = CyberTheme.semantics.colors.danger,
                            radius = 16.dp,
                            intensity = 3f
                        )
                    )
                    Text("Danger Neon", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("Magenta r=16dp i=3", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Warning,
                        contentDescription = "Warning Glow",
                        tint = CyberTheme.semantics.colors.warning,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberTextGlow(
                            color = CyberTheme.semantics.colors.warning,
                            radius = 20.dp,
                            intensity = 3f
                        )
                    )
                    Text("Warning Neon", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("Amber r=20dp i=3", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
        }

        // Row 3: Supercharged radiance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Lock,
                        contentDescription = "Success Glow",
                        tint = CyberTheme.semantics.colors.success,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberTextGlow(
                            color = CyberTheme.semantics.colors.success,
                            radius = 24.dp,
                            intensity = 4f
                        )
                    )
                    Text("Success Halo", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("Green r=24dp i=4", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
            CyberCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Circuit,
                        contentDescription = "Hyper Radiant",
                        tint = CyberTheme.colors.primary,
                        size = CyberPrimitives.IconSizes.dp48,
                        modifier = Modifier.cyberTextGlow(
                            color = CyberTheme.colors.primary,
                            radius = 32.dp,
                            intensity = 5f
                        )
                    )
                    Text("Hyper Radiant", style = CyberTheme.typography.display, color = CyberTheme.colors.primary)
                    Text("Cyan r=32dp i=5", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
                }
            }
        }

        IconScalingRow(
            title = "TEXT GLOW SCALING (dp16 - dp64)",
            iconRes = CyberIcons.Chip,
            tint = CyberTheme.colors.primary
        ) {
            Modifier.cyberTextGlow(CyberTheme.colors.primary, radius = CyberPrimitives.Spacing.dp16)
        }
    }
}

// -------------------------------------------------------------------------
// Tab 8: Combined Effects
// -------------------------------------------------------------------------

@Composable
fun CombinedEffectsTab() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        CyberCard(modifier = Modifier.height(280.dp).fillMaxWidth().cyberNoise().cyberScanlines()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                    CyberIcon(iconRes = CyberIcons.Terminal, contentDescription = null, tint = CyberTheme.colors.primary, variant = CyberIconVariant.Overload, size = CyberPrimitives.IconSizes.dp48, modifier = Modifier.cyberTextGlow(CyberTheme.colors.primary))
                    CyberIcon(iconRes = CyberIcons.Shield, contentDescription = null, tint = CyberTheme.semantics.colors.danger, size = CyberPrimitives.IconSizes.dp48, modifier = Modifier.cyberTextGlow(CyberTheme.semantics.colors.danger).cyberIconPulse())
                    CyberIcon(iconRes = CyberIcons.Loading, contentDescription = null, tint = CyberTheme.semantics.colors.success, size = CyberPrimitives.IconSizes.dp48, modifier = Modifier.cyberTextGlow(CyberTheme.semantics.colors.success).cyberIconSpin())
                }
                Text("SYSTEM ONLINE", style = CyberTheme.typography.display, color = CyberTheme.colors.primary, modifier = Modifier.cyberOverload(intensity = 1f).cyberTextGlow(CyberTheme.colors.primary))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Multiple effects layered together", style = CyberTheme.typography.terminal, color = CyberTheme.colors.primary)
            }
        }

        IconScalingRow(
            title = "COMBINED EFFECTS SCALING (dp16 - dp64)",
            iconRes = CyberIcons.Terminal,
            variant = CyberIconVariant.Overload
        ) {
            Modifier
                .cyberTextGlow(CyberTheme.colors.primary)
                .cyberNoise()
                .cyberScanlines()
        }
    }
}

