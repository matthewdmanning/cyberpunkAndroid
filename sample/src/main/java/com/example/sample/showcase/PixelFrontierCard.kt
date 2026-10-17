package com.example.sample.showcase

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberButtonSize
import com.example.cyberpunkandroid.components.CyberButtonStyle
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberPixelFrontierTransition
import com.example.cyberpunkandroid.components.CyberSwitch
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Catalogue Card: Pixel Frontier AGSL Shader Screen Transition.
 *
 * Demonstrates:
 * - [CyberPixelFrontierTransition] screen-to-screen shader transition
 * - Multi-scale pixelation blocks (largest at frontier, finer with distance)
 * - Directional sweep ([Orientation.Vertical] vs [Orientation.Horizontal])
 * - Block outline edge detection and chromatic color split
 */
@Composable
fun PixelFrontierCard(modifier: Modifier = Modifier) {
    var pageIndex by remember { mutableIntStateOf(0) }
    var isVertical by remember { mutableStateOf(true) }

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        header = {
            Column {
                Text(
                    text = "PIXEL FRONTIER TRANSITION",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
                Text(
                    text = "CyberPixelFrontierTransition • AGSL RuntimeShader • Multi-Scale Pixelation",
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
            // Interactive Preview Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(CyberTheme.shapes.cyberCutCornerShape)
                    .background(CyberTheme.colors.surfaceSecondary)
            ) {
                CyberPixelFrontierTransition(
                    targetState = pageIndex,
                    orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal,
                    blockSize = CyberPrimitives.Spacing.dp32,
                    animationSpec = tween(450),
                    modifier = Modifier.fillMaxSize()
                ) { state ->
                    val color = when (state % 3) {
                        0 -> CyberTheme.colors.primary
                        1 -> CyberTheme.colors.secondary
                        else -> CyberPrimitives.Colors.Green500
                    }
                    val label = when (state % 3) {
                        0 -> "SEC_SECTOR // NETWATCH 01"
                        1 -> "SEC_SECTOR // ARASAKA 02"
                        else -> "SEC_SECTOR // BIOTECHNICA 03"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CyberTheme.colors.surfaceTertiary)
                            .padding(CyberTheme.spacing.dp16),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = CyberTheme.typography.terminal,
                                color = color
                            )
                            Text(
                                text = "CH_0${(state % 3) + 1}",
                                style = CyberTheme.typography.terminal,
                                color = CyberTheme.colors.textSecondary
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)) {
                            Text(
                                text = "BUFFER DUMP: 0x${Integer.toHexString(state.hashCode()).takeLast(6).uppercase()}",
                                style = CyberTheme.typography.terminal,
                                color = CyberTheme.colors.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(color)
                            )
                        }

                        Text(
                            text = "STATUS: ENCRYPTED OPTICAL RELAY",
                            style = CyberTheme.typography.body,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                }
            }

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isVertical) "SWEEP: VERTICAL" else "SWEEP: HORIZONTAL",
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textPrimary
                    )
                    CyberSwitch(
                        checked = isVertical,
                        onCheckedChange = { isVertical = it }
                    )
                }

                CyberButton(
                    style = CyberButtonStyle.Primary,
                    size = CyberButtonSize.Medium,
                    onClick = { pageIndex++ }
                ) {
                    Text(text = "TRIGGER SWEEP", style = CyberTheme.typography.terminal)
                }
            }
        }
    }
}
