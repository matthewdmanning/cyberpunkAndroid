package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * Shimmering cut-corner skeleton placeholder surface for loading state mockups.
 *
 * Implements the Cybercore CSS `.cyber-skeleton` specification with a continuous angled linear gradient sweep.
 *
 * @param modifier Composable modifier providing skeleton dimensions and positioning.
 * @param baseColor Dark resting surface fill color. Defaults to [CyberTheme.colors.surface].
 * @param shimmerColor Translucent neon highlight sweep color. Defaults to 3% alpha [CyberTheme.colors.primary].
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
@Composable
fun CyberSkeleton(
    modifier: Modifier = Modifier,
    animationSpec: androidx.compose.animation.core.InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(1200, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    baseColor: Color = CyberTheme.colors.surface,
    shimmerColor: Color = CyberTheme.colors.primary.copy(alpha = 0.3f),
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SkeletonTransition")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = animationSpec,
        label = "ShimmerAnimation"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            shimmerColor,
            baseColor
        ),
        start = Offset(shimmerTranslate - 200f, shimmerTranslate - 200f),
        end = Offset(shimmerTranslate, shimmerTranslate)
    )

    Box(
        modifier = modifier.cyberComponentSemantics("CyberSkeleton", appendedA11y, customA11y)
            .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
            .background(brush)
    )
}
