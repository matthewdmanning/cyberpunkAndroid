package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.cyberpunkandroid.effects.cyberBorder
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.example.cyberpunkandroid.config.CyberPrimitives

import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * Asymmetric cut-corner surface container with neon border glow, interactive tactile states, and slot architecture.
 *
 * Implements the Cybercore CSS `.cyber-card` specification.
 *
 * ### Interactive & Visual Behaviors:
 * - When [interactive] is true, tracks touch events to animate a tactile scale down to 98% and intensifies neon border opacity from 50% to 100%.
 * - Supports [holo] holographic styling via scanlines and surface alpha washes.
 * - Provides discrete slot regions for [header], [content], and [footer].
 *
 * @param modifier Composable modifier applied to the outer card container.
 * @param header Optional composable banner slot positioned above primary content.
 * @param footer Optional composable action bar slot pinned beneath primary content.
 * @param interactive When true, enables touch interaction, press animations, and click handling.
 * @param holo Renders subtle animated holographic scanlines across the card surface.
 * @param onClick Optional callback invoked when the card is clicked. Requires [interactive] to be true.
 * @param interactionSource Stream tracking interaction events.
 * @param content Primary composable content layout slot.
 */
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    holo: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    scaleAnimationSpec: androidx.compose.animation.core.AnimationSpec<Float> = androidx.compose.animation.core.spring(),
    alphaAnimationSpec: androidx.compose.animation.core.AnimationSpec<Float> = androidx.compose.animation.core.spring(),
    appendedA11y: String? = null,
    customA11y: String? = null,
    onClick: (() -> Unit)? = null,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    footer: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (interactive && isPressed) 0.98f else 1f, animationSpec = scaleAnimationSpec, label = "CardScale")
    val borderAlpha by animateFloatAsState(targetValue = if (interactive && isPressed) 1f else 0.5f, animationSpec = alphaAnimationSpec, label = "CardBorderAlpha")

    var cardModifier = modifier.cyberComponentSemantics("CyberCard", appendedA11y, customA11y)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(CyberTheme.shapes.cyberCutCornerShape)
        .background(CyberTheme.colors.surface)
    
    if (interactive && (onClick != null)) {
        cardModifier = cardModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }

    cardModifier = cardModifier.cyberBorder(color = CyberTheme.colors.primary.copy(alpha = borderAlpha),
        width = CyberPrimitives.BorderWidths.dp1, shape = CyberTheme.shapes.cyberCutCornerShape)

    Box(modifier = cardModifier) {
        if (holo) {
            CyberSkeleton(
                modifier = Modifier.matchParentSize(),
                baseColor = Color.Transparent,
                shimmerColor = CyberTheme.colors.primary.copy(alpha = 0.05f)
            )
        }
        
        Column {
            if (header != null) {
                Box(
                    modifier = Modifier
                        .background(CyberTheme.colors.primary.copy(alpha = 0.1f))
                        .padding(CyberPrimitives.Spacing.dp12)
                ) {
                    Column(content = header)
                }
            }
            Box(modifier = Modifier.padding(CyberPrimitives.Spacing.dp16)) {
                Column(content = content)
            }
            if (footer != null) {
                Box(
                    modifier = Modifier
                        .background(CyberTheme.colors.surface)
                        .padding(CyberPrimitives.Spacing.dp12)
                ) {
                    Column(content = footer)
                }
            }
        }
    }
}
