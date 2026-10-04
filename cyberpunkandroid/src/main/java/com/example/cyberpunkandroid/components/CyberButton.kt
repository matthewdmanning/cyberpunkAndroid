package com.example.cyberpunkandroid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberBorder
import com.example.cyberpunkandroid.effects.cyberOverload

import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * Dimension presets for [CyberButton].
 *
 * @property padding Inner horizontal and vertical content padding values.
 */
enum class CyberButtonSize(val padding: PaddingValues) {
    /** Compact padding suitable for toolbars and inline actions. */
    Small(PaddingValues(horizontal = CyberPrimitives.Spacing.dp12, vertical = CyberPrimitives.Spacing.dp4)),
    /** Standard padding for prominent primary actions. */
    Medium(PaddingValues(horizontal = CyberPrimitives.Spacing.dp16, vertical = CyberPrimitives.Spacing.dp8)),
    /** Expanded padding for hero banners and primary call-to-actions. */
    Large(PaddingValues(horizontal = CyberPrimitives.Spacing.dp24, vertical = CyberPrimitives.Spacing.dp12))
}

/**
 * Visual styling variants for [CyberButton].
 */
enum class CyberButtonStyle {
    /** Solid high-contrast neon fill with dark inverted content. */
    Primary,
    /** Transparent background framed by a neon border stroke. */
    Outline,
    /** Borderless button with subtle hover/press neon tinting. */
    Ghost,
    /** Solid neon fill that triggers chromatic overload distortion upon user interaction. */
    Overload
}

/**
 * Interactive futuristic action button with chamfered cut corners, neon border glow, and tactile press scaling.
 *
 * ### Interactive Behaviors:
 * - Listens to press events via [interactionSource] to apply dynamic 97% scale reduction.
 * - Activates real-time AGSL chromatic aberration overload distortion when [CyberButtonStyle.Overload] is pressed.
 * - Enforces disabled states via 50% alpha reduction and touch interception.
 *
 * @param onClick Invoked when the button is clicked.
 * @param modifier Composable modifier applied to the button layout.
 * @param style Visual presentation preset ([CyberButtonStyle.Primary], [CyberButtonStyle.Outline],
 *   [CyberButtonStyle.Ghost], [CyberButtonStyle.Overload]). Defaults to [CyberButtonStyle.Primary].
 * @param size Button dimension scale ([CyberButtonSize.Small], [CyberButtonSize.Medium], [CyberButtonSize.Large]). Defaults to [CyberButtonSize.Medium].
 * @param enabled Controls whether the button responds to user clicks.
 * @param interactionSource Stream tracking interaction events such as press and hover.
 * @param content Composable slot providing button label or icons.
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
@Composable
fun CyberButton(
    modifier: Modifier = Modifier,
    style: CyberButtonStyle = CyberButtonStyle.Primary,
    size: CyberButtonSize = CyberButtonSize.Medium,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    appendedA11y: String? = null,
    customA11y: String? = null,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = CyberTheme.shapes.cyberCutCornerShape

    val containerColor = when (style) {
        CyberButtonStyle.Primary, CyberButtonStyle.Overload -> CyberTheme.colors.primary
        CyberButtonStyle.Outline, CyberButtonStyle.Ghost -> Color.Transparent
    }

    val contentColor = when (style) {
        CyberButtonStyle.Primary, CyberButtonStyle.Overload -> CyberTheme.colors.background
        CyberButtonStyle.Outline, CyberButtonStyle.Ghost -> CyberTheme.colors.primary
    }

    val baseModifier = modifier.cyberComponentSemantics("CyberButton", appendedA11y, customA11y)
        .graphicsLayer {
            alpha = if (enabled) 1f else 0.5f
            scaleX = if (isPressed) 0.98f else 1f
            scaleY = if (isPressed) 0.98f else 1f
        }
        .clip(shape)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Button,
            onClick = onClick,
        )
        .background(containerColor)

    val styledModifier = when (style) {
        CyberButtonStyle.Primary -> baseModifier
        CyberButtonStyle.Outline -> baseModifier.cyberBorder(color = CyberTheme.colors.primary,
            width = CyberPrimitives.BorderWidths.dp2, shape = shape)
        CyberButtonStyle.Ghost -> baseModifier
        CyberButtonStyle.Overload -> baseModifier.cyberOverload(enabled = true)
    }

    Box(
        modifier = styledModifier.padding(size.padding),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(value = CyberTheme.typography.body) {
                content()
            }
        }
    }
}
