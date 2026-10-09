package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.effects.cyberGlowBorderFlow
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.roundToInt

/**
 * Controlled neon circuit switch with a sliding thumb and short activation flash.
 *
 * @param checked Current boolean state supplied by the caller.
 * @param modifier Modifier applied to the switch.
 * @param enabled Whether the switch can be toggled.
 * @param width Total switch width.
 * @param height Total switch height.
 * @param thumbSize Diameter of the sliding thumb.
 * @param thumbAnimationSpec Animation used for thumb travel.
 * @param flashAnimationSpec Animation used for the activation flash.
 * @param activeColor Track and thumb color while enabled.
 * @param inactiveColor Track color while disabled.
 * @param appendedA11y Optional text appended to the default accessibility name.
 * @param customA11y Optional custom accessibility name.
 * @param onCheckedChange Called with the next checked state.
 */
@Composable
fun CyberSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 52.dp,
    height: Dp = 28.dp,
    thumbSize: Dp = 20.dp,
    thumbAnimationSpec: AnimationSpec<Float> = spring(),
    flashAnimationSpec: AnimationSpec<Float> = tween(180),
    activeColor: Color = CyberTheme.colors.primary,
    inactiveColor: Color = CyberTheme.colors.surfaceSecondary,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onCheckedChange: (Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val animatedFraction by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = thumbAnimationSpec,
        label = "CyberSwitchThumb",
    )
    val flash = remember { Animatable(0f) }
    var activationCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(activationCount) {
        if (activationCount > 0) {
            flash.snapTo(1f)
            flash.animateTo(0f, flashAnimationSpec)
        }
    }

    val actionLabel = if (checked) "Turn off" else "Turn on"
    val switchA11yLabel = customA11y ?: appendedA11y

    BoxWithConstraints(
        modifier = modifier
            .then(
                if (switchA11yLabel != null) {
                    Modifier.semantics { contentDescription = switchA11yLabel }
                } else {
                    Modifier
                }
            )
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = { next ->
                    if (next) activationCount++
                    onCheckedChange(next)
                },
            )
            .semantics {
                this.onClick(label = actionLabel) {
                    if (enabled) {
                        val next = !checked
                        if (next) activationCount++
                        onCheckedChange(next)
                        true
                    } else {
                        false
                    }
                }
            }
            .size(width, height),
    ) {
        val shape = RoundedCornerShape(height / 2)
        val trackModifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(shape)
            .background(if (checked) activeColor.copy(alpha = 0.35f) else inactiveColor)
        Box(
            modifier = if (checked) {
                trackModifier.cyberGlowBorderFlow(
                    colors = listOf(activeColor, CyberTheme.colors.secondary),
                    shape = shape,
                    glowRadius = 6.dp,
                    width = 2.dp,
                )
            } else {
                trackModifier
            },
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(activeColor.copy(alpha = flash.value * 0.4f)),
        )

        Box(
            modifier = Modifier
                .offset {
                    val travel = (constraints.maxWidth - with(density) { thumbSize.toPx() }).coerceAtLeast(0f)
                    IntOffset((animatedFraction * travel).roundToInt(), 0)
                }
                .size(thumbSize)
                .clip(CircleShape)
                .background(if (checked) activeColor else inactiveColor)
                .cyberGlowBorderFlow(
                    colors = listOf(activeColor, CyberTheme.colors.secondary),
                    shape = CircleShape,
                    glowRadius = 5.dp,
                    width = 2.dp,
                ),
        )
    }
}
