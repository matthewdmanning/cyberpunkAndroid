package com.example.cyberpunkandroid.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Interactive individual navigation link with animated bottom glow border and hover/press tactile scaling.
 *
 * @param label Nav link text copy.
 * @param isSelected Whether this link represents the currently active route.
 * @param modifier Composable modifier for link placement.
 * @param colorAnimationSpec Animation specification for state color transition.
 * @param scaleAnimationSpec Animation specification for tactile press scale.
 * @param indicatorAnimationSpec Animation specification for bottom indicator bar transition.
 * @param appendedA11y Optional text to append to accessibility description.
 * @param customA11y Optional text to override accessibility description.
 * @param onClick Invoked when the link is tapped.
 */
@Composable
fun CyberNavLink(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    colorAnimationSpec: AnimationSpec<Color> = tween(CyberPrimitives.Durations.ms150, easing = CyberConfig.Easings.OutExpoEasing),
    scaleAnimationSpec: AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms150, easing = CyberConfig.Easings.OutExpoEasing),
    indicatorAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = CyberPrimitives.Durations.ms300,
        easing = CyberConfig.Easings.CyberEasing
    ),
    appendedA11y: String? = null,
    customA11y: String? = null,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetColor = if (isSelected || isPressed) CyberTheme.colors.primary else CyberTheme.colors.textSecondary
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = colorAnimationSpec,
        label = "NavLinkColor"
    )

    val scaleValue by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = scaleAnimationSpec,
        label = "NavLinkScale"
    )

    val indicatorProgress by animateFloatAsState(
        targetValue = if (isSelected || isPressed) 1.0f else 0.0f,
        animationSpec = indicatorAnimationSpec,
        label = "NavLinkIndicatorProgress"
    )

    Column(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .scale(scaleValue)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(
                horizontal = CyberPrimitives.Spacing.dp8,
                vertical = CyberPrimitives.Spacing.dp4
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val glowModifier = if (isSelected || isPressed) {
            Modifier
        } else {
            Modifier
        }

        Text(
            text = label.uppercase(),
            style = CyberTheme.typography.terminal.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = animatedColor
            ),
            modifier = glowModifier
        )

        Spacer(modifier = Modifier.height(CyberPrimitives.Spacing.dp4))

        // Sliding bottom highlight line
        Box(
            modifier = Modifier
                .height(CyberPrimitives.BorderWidths.dp2)
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = indicatorProgress
                    alpha = indicatorProgress
                }
                .background(CyberTheme.colors.primary)
        )
    }
}
