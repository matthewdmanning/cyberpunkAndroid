package com.example.cyberpunkandroid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberChangeFlash
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.effects.rememberCyberChangePulse
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.icons.SemanticIcons
import com.example.cyberpunkandroid.theme.CyberStatusRole
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Returns the status color role that matches an alert variant.
 *
 * @param variant Alert intent.
 */
@Composable
fun cyberStatusRoleFor(variant: CyberAlertVariant): CyberStatusRole = when (variant) {
    CyberAlertVariant.Info -> CyberTheme.status.info
    CyberAlertVariant.Success -> CyberTheme.status.success
    CyberAlertVariant.Warning -> CyberTheme.status.warning
    CyberAlertVariant.Error -> CyberTheme.status.danger
}

/**
 * Persistent, full-width notice for a condition that stays true until it is resolved or dismissed (offline mode,
 * a pending update, a degraded service). Unlike [CyberSnackbar], it does not float or time out; it pushes the
 * content below it down, and it can offer actions.
 *
 * ### Visual behavior
 * - A flat bar in the variant's status `container` color, with a solid status-color accent stripe on the start
 *   edge, the variant icon, the [message] (and optional [title]) in the status `onContainer` color, an optional
 *   [actions] row, and an optional dismiss button.
 * - The shape is a plain rectangle, because a banner spans the screen edge to edge.
 * - Show and hide expand and collapse the banner vertically ([enterTransition], [exitTransition]).
 * - When [message] changes while the banner is visible, the bar flashes once in the status color
 *   ([cyberChangeFlash]), so a user who already saw the banner notices the new text. Set
 *   [highlightMessageChange] to `false` to turn this off.
 *
 * ### Accessibility
 * The banner is a polite live region, or an assertive one for [CyberAlertVariant.Error], so screen readers
 * announce it when it appears or its text changes.
 *
 * @param message Body text.
 * @param visible `true` to show the banner. The caller owns this state.
 * @param modifier Modifier applied to the banner.
 * @param title Optional short heading shown above [message].
 * @param variant Intent of the notice. It selects the status color role and the icon.
 * @param accentWidth Width of the start-edge accent stripe.
 * @param iconSize Size of the variant icon.
 * @param highlightMessageChange `true` to flash the bar once when [message] changes.
 * @param enterTransition Transition used when [visible] becomes `true`.
 * @param exitTransition Transition used when [visible] becomes `false`.
 * @param flashAnimationSpec Decay of the message-change flash.
 * @param role Status color role. Defaults to the role of [variant].
 * @param dismissLabel Spoken label of the dismiss button.
 * @param appendedA11y Optional accessibility label. Without it, the banner text is read.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 * @param onDismiss Called when the dismiss button is tapped. `null` hides the dismiss button.
 * @param actions Optional trailing actions, such as a "Retry" button.
 */
@Composable
fun CyberBanner(
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    title: String? = null,
    variant: CyberAlertVariant = CyberAlertVariant.Info,
    accentWidth: Dp = CyberPrimitives.BorderWidths.dp4,
    iconSize: Dp = CyberPrimitives.IconSizes.dp24,
    highlightMessageChange: Boolean = true,
    enterTransition: EnterTransition = expandVertically(
        animationSpec = tween(CyberPrimitives.Durations.ms300, easing = CyberConfig.Easings.DecelEasing)
    ) + fadeIn(tween(CyberPrimitives.Durations.ms150)),
    exitTransition: ExitTransition = shrinkVertically(
        animationSpec = tween(CyberPrimitives.Durations.ms150, easing = CyberConfig.Easings.AccelEasing)
    ) + fadeOut(tween(CyberPrimitives.Durations.ms150)),
    flashAnimationSpec: AnimationSpec<Float> = tween(
        durationMillis = CyberPrimitives.Durations.ms500,
        easing = CyberConfig.Easings.OutExpoEasing
    ),
    role: CyberStatusRole = cyberStatusRoleFor(variant),
    dismissLabel: String = "Dismiss",
    appendedA11y: String? = null,
    customA11y: String? = null,
    onDismiss: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val flash by rememberCyberChangePulse(
        key = message,
        animationSpec = flashAnimationSpec,
        shouldPulse = { old, new -> highlightMessageChange && old != new }
    )
    val liveMode = if (variant == CyberAlertVariant.Error) LiveRegionMode.Assertive else LiveRegionMode.Polite

    AnimatedVisibility(visible = visible, modifier = modifier, enter = enterTransition, exit = exitTransition) {
        Row(
            modifier = Modifier
                .cyberComponentSemantics("CyberBanner", appendedA11y, customA11y)
                .semantics { liveRegion = liveMode }
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(role.container)
                .cyberChangeFlash(pulse = { flash }, color = role.color, shape = RectangleShape),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(accentWidth)
                    .fillMaxHeight()
                    .background(role.color)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = CyberTheme.spacing.dp12, vertical = CyberTheme.spacing.dp8),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp12)
            ) {
                CyberIcon(
                    iconRes = variantIcon(variant),
                    contentDescription = null,
                    size = iconSize,
                    tint = role.color
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp2)
                ) {
                    if (title != null) {
                        Text(text = title.uppercase(), color = role.color, style = CyberTheme.typography.terminal)
                    }
                    Text(text = message, color = role.onContainer, style = CyberTheme.typography.body)
                }
                actions?.invoke(this)
            }
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    CyberIcon(
                        iconRes = CyberIcons.X,
                        contentDescription = dismissLabel,
                        size = iconSize,
                        tint = role.onContainer
                    )
                }
            }
        }
    }
}

/**
 * Returns the icon that matches an alert variant.
 *
 * @param variant Alert intent.
 */
private fun variantIcon(variant: CyberAlertVariant): Int = when (variant) {
    CyberAlertVariant.Info -> SemanticIcons.Info
    CyberAlertVariant.Success -> SemanticIcons.Success
    CyberAlertVariant.Warning -> SemanticIcons.Caution
    CyberAlertVariant.Error -> SemanticIcons.Danger
}
