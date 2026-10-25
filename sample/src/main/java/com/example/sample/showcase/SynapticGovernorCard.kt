package com.example.sample.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberDialTicks
import com.example.cyberpunkandroid.components.CyberRim
import com.example.cyberpunkandroid.components.cyberLongPressFill
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Catalogue Card: Interactive Tactile Hold-to-Charge Input.
 *
 * Demonstrates:
 * - [CyberDialTicks] and [CyberRim] for radial dials
 * - [Modifier.cyberLongPressFill] for hold-to-charge capacitive inputs
 * - Dynamic icon morphing (Zap vs Lock) and [CyberBadge] status states
 */
@Composable
fun SynapticGovernorCard(modifier: Modifier = Modifier) {
    var bypassProgress by remember { mutableFloatStateOf(0f) }
    var isBypassed by remember { mutableStateOf(false) }

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        header = {
            Column {
                Text(
                    text = "SYNAPTIC GOVERNOR",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
                Text(
                    text = "CyberDialTicks • CyberRim • cyberLongPressFill • CyberBadge",
                    style = CyberTheme.typography.body,
                    color = CyberTheme.colors.textSecondary
                )
            }
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
}
