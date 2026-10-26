package com.example.cyberpunkandroid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberBeam
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.effects.cyberMaterialize
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.icons.SemanticIcons
import com.example.cyberpunkandroid.theme.CyberStatusRole
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Persistent notice that projects onto the screen: the same job as [CyberBanner], in a holographic style.
 *
 * ### Visual behavior
 * - No box. The notice floats between two horizon lines (top and bottom) in the status color. Each line is
 *   brightest at the center and fades to transparent at both ends, like a scan field with no hard frame.
 * - A faint beam of the status color falls from the top line ([cyberBeam]) over a translucent container.
 * - When the banner appears, it is projected from the start edge to the end edge behind a bright scan line
 *   ([cyberMaterialize], [materializeSpec]). This runs once per appearance; there is no idle motion.
 * - Icon, optional title, message, optional actions, and optional dismiss button, as in [CyberBanner].
 *
 * ### Accessibility
 * Polite live region, or assertive for [CyberAlertVariant.Error].
 *
 * @param message Body text.
 * @param visible `true` to show the banner. The caller owns this state.
 * @param modifier Modifier applied to the banner.
 * @param title Optional short heading.
 * @param variant Intent of the notice. It selects the status color role and the icon.
 * @param lineWidth Stroke width of the horizon lines.
 * @param iconSize Variant icon size.
 * @param enterTransition Transition used when [visible] becomes `true`. The scan reveal runs on top of it.
 * @param exitTransition Transition used when [visible] becomes `false`.
 * @param materializeSpec Timing of the scan reveal from the start edge to the end edge.
 * @param role Status color role. Defaults to the role of [variant].
 * @param containerAlpha Opacity of the container fill, `0f` to `1f`. Low values let the screen show through.
 * @param dismissLabel Spoken label of the dismiss button.
 * @param appendedA11y Optional accessibility label.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 * @param onDismiss Called when the dismiss button is tapped. `null` hides the button.
 * @param actions Optional trailing actions.
 */
@Composable
fun CyberHoloBanner(
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    title: String? = null,
    variant: CyberAlertVariant = CyberAlertVariant.Info,
    lineWidth: Dp = CyberPrimitives.BorderWidths.dp1,
    iconSize: Dp = CyberPrimitives.IconSizes.dp24,
    enterTransition: EnterTransition = fadeIn(tween(CyberPrimitives.Durations.ms150)),
    exitTransition: ExitTransition = fadeOut(tween(CyberPrimitives.Durations.ms150)),
    materializeSpec: AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms500, easing = CyberConfig.Easings.DecelEasing),
    role: CyberStatusRole = cyberStatusRoleFor(variant),
    containerAlpha: Float = 0.5f, // Half-transparent, so the projection reads as light over the screen.
    dismissLabel: String = "Dismiss",
    appendedA11y: String? = null,
    customA11y: String? = null,
    onDismiss: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val liveMode = if (variant == CyberAlertVariant.Error) LiveRegionMode.Assertive else LiveRegionMode.Polite
    AnimatedVisibility(visible = visible, modifier = modifier, enter = enterTransition, exit = exitTransition) {
        val reveal = remember { Animatable(0f) }
        LaunchedEffect(Unit) { reveal.animateTo(1f, materializeSpec) }
        Row(
            modifier = Modifier
                .cyberComponentSemantics("CyberHoloBanner", appendedA11y, customA11y)
                .semantics { liveRegion = liveMode }
                .fillMaxWidth()
                .cyberMaterialize(progress = { reveal.value }, color = role.color)
                .background(role.container.copy(alpha = role.container.alpha * containerAlpha))
                .cyberBeam(color = role.color)
                .horizonLines(role.color, lineWidth)
                .padding(horizontal = CyberTheme.spacing.dp16, vertical = CyberTheme.spacing.dp12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp12)
        ) {
            CyberIcon(iconRes = holoVariantIcon(variant), contentDescription = null, size = iconSize, tint = role.color)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp2)) {
                if (title != null) Text(text = title.uppercase(), color = role.color, style = CyberTheme.typography.terminal)
                Text(text = message, color = role.onContainer, style = CyberTheme.typography.body)
            }
            actions?.invoke(this)
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    CyberIcon(iconRes = CyberIcons.X, contentDescription = dismissLabel, size = iconSize, tint = role.onContainer)
                }
            }
        }
    }
}

/**
 * Draws top and bottom lines in [color] that are solid at the center and fade to transparent at both ends.
 *
 * @param color Line color at the center.
 * @param width Line stroke width.
 */
private fun Modifier.horizonLines(color: Color, width: Dp): Modifier = drawBehind {
    val stroke = width.toPx()
    val brush = Brush.horizontalGradient(listOf(Color.Transparent, color, Color.Transparent))
    drawLine(brush, Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
    drawLine(brush, Offset(0f, size.height - stroke / 2f), Offset(size.width, size.height - stroke / 2f), stroke)
}

/**
 * Returns the icon that matches an alert variant.
 *
 * @param variant Alert intent.
 */
private fun holoVariantIcon(variant: CyberAlertVariant): Int = when (variant) {
    CyberAlertVariant.Info -> SemanticIcons.Info
    CyberAlertVariant.Success -> SemanticIcons.Success
    CyberAlertVariant.Warning -> SemanticIcons.Caution
    CyberAlertVariant.Error -> SemanticIcons.Danger
}
