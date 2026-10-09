package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Visual intent variants for [CyberBadge].
 */
enum class CyberBadgeVariant {
    /** Cautionary yellow status pill with pulsing indicator. */
    Caution,
    /** Critical red danger pill with pulsing indicator. */
    Danger,
    /** Positive green success pill with pulsing indicator. */
    Success,
    /** Informational cyan status pill with pulsing indicator. */
    Info,
    /** Stroked outline badge without filled background tint. */
    Outline
}

/**
 * Compact sci-fi status indicator pill featuring pulsating LED beacon and uppercase monospace label.
 *
 * Implements the Cybercore CSS `.cyber-badge` specification with dynamic color variants and pulsing LED animation.
 *
 * @param text Uppercase status string displayed inside the badge.
 * @param modifier Modifier applied to the outer badge container.
 * @param variant Visual and semantic status variant ([CyberBadgeVariant.Caution], [CyberBadgeVariant.Danger],
 *   [CyberBadgeVariant.Success], [CyberBadgeVariant.Info], [CyberBadgeVariant.Outline]). Defaults to [CyberBadgeVariant.Info].
 * @param appendedA11y Optional text to append to the default component accessibility name.
 * @param customA11y Optional custom accessibility description that completely overrides the default name.
  * @param minAlpha TODO: document this
 */
@Composable
fun CyberBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: CyberBadgeVariant = CyberBadgeVariant.Info,
    minAlpha: Float = 0.3f,
    pulseAnimationSpec: androidx.compose.animation.core.InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(CyberTheme.semantics.durations.warningPulse, easing = com.example.cyberpunkandroid.config.CyberConfig.Easings.OutExpoEasing),
        repeatMode = RepeatMode.Reverse
    ),
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val color = when (variant) {
        CyberBadgeVariant.Caution -> CyberTheme.semantics.colors.caution
        CyberBadgeVariant.Danger -> CyberTheme.semantics.colors.danger
        CyberBadgeVariant.Success -> CyberTheme.semantics.colors.success
        CyberBadgeVariant.Info, CyberBadgeVariant.Outline -> CyberTheme.semantics.colors.info
    }

    val backgroundColor = if (variant == CyberBadgeVariant.Outline) Color.Transparent else color.copy(alpha = 0.1f)

    val infiniteTransition = rememberInfiniteTransition(label = "BadgePulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = 1.0f,
        animationSpec = pulseAnimationSpec,
        label = "PulseAlpha"
    )

    val a11yDescription = customA11y ?: if (!appendedA11y.isNullOrBlank()) "$text - $appendedA11y" else null

    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                if (a11yDescription != null) {
                    contentDescription = a11yDescription
                }
            }
            .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
            .background(backgroundColor)
            .border(
                width = CyberPrimitives.BorderWidths.dp1,
                color = color,
                shape = CyberTheme.shapes.cyberCutCornerShapeSmall
            )
            .padding(horizontal = CyberPrimitives.Spacing.dp8, vertical = CyberPrimitives.Spacing.dp4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp8)
    ) {
        // Pulsating LED dot
        Box(
            modifier = Modifier
                .size(CyberPrimitives.Spacing.dp8)
                .alpha(alpha)
                .background(color, CircleShape)
        )
        CompositionLocalProvider(LocalContentColor provides color) {
            ProvideTextStyle(value = CyberTheme.typography.terminal) {
                androidx.compose.material3.Text(text = text.uppercase())
            }
        }
    }
}
