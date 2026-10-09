package com.example.cyberpunkandroid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntSize
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.effects.cyberBorder
import com.example.cyberpunkandroid.effects.cyberOverload
import com.example.cyberpunkandroid.effects.cyberScanlines
import com.example.cyberpunkandroid.effects.cyberSemantics
import com.example.cyberpunkandroid.effects.cyberTextGlow
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Controlled terminal-style expandable section with a neon chevron and a decryption flash on opening.
 *
 * @param title Fallback header text used when [headerContent] is not provided.
 * @param expanded Whether the accordion body is currently visible.
 * @param headerContent Optional custom header content rendered inside the accordion's primary header box. When supplied,
 * the chevron remains the expansion control so interactive header content such as [CyberTextField] can receive input.
 * @param onExpandedChange Called when the header or chevron requests an expansion-state change.
 * @param content Expanded body content.
  * @param modifier TODO: document this
  * @param borderColor TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
@Composable
fun CyberAccordion(
    title: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    expansionAnimationSpec: FiniteAnimationSpec<IntSize> = spring(),
    overloadAnimationSpec: androidx.compose.animation.core.AnimationSpec<Float> = spring(),
    borderColor: Color = CyberTheme.colors.primary.copy(alpha = 0.45f),
    appendedA11y: String? = null,
    customA11y: String? = null,
    headerContent: (@Composable (expanded: Boolean) -> Unit)? = null,
    onExpandedChange: (Boolean) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val overloadAmount = remember { Animatable(0f) }
    LaunchedEffect(expanded) {
        if (expanded) {
            overloadAmount.snapTo(1f)
            overloadAmount.animateTo(0f, overloadAnimationSpec)
        } else {
            overloadAmount.snapTo(0f)
        }
    }

    Column(
        modifier = modifier
            .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
            .background(CyberTheme.colors.surface)
            .cyberBorder(
                width = CyberPrimitives.BorderWidths.dp1,
                color = borderColor,
                shape = CyberTheme.shapes.cyberCutCornerShapeSmall,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .cyberSemantics("CyberAccordion", appendedA11y, customA11y)
                .then(
                    if (headerContent == null) {
                        Modifier.toggleable(value = expanded, role = Role.Button, onValueChange = onExpandedChange)
                    } else {
                        Modifier
                    },
                )
                .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                .padding(CyberPrimitives.Spacing.dp12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (headerContent != null) {
                    headerContent(expanded)
                } else {
                    CompositionLocalProvider(LocalContentColor provides CyberTheme.colors.primary) {
                        ProvideTextStyle(value = CyberTheme.typography.terminal) {
                            Text(text = title.uppercase())
                        }
                    }
                }
            }
            val chevronA11y = if (headerContent != null) {
                customA11y ?: if (expanded) "Collapse section" else "Expand section"
            } else null

            Icon(
                painter = painterResource(id = if (expanded) CyberIcons.ChevronUp else CyberIcons.ChevronDown),
                contentDescription = null,
                tint = CyberTheme.colors.primary,
                modifier = Modifier
                    .size(CyberPrimitives.IconSizes.dp24)
                    .then(
                        if (headerContent != null) {
                            Modifier
                                .cyberSemantics(
                                    name = if (expanded) "Collapse section" else "Expand section",
                                    appendedA11y = appendedA11y,
                                    customA11y = chevronA11y,
                                )
                                .toggleable(
                                    value = expanded,
                                    role = Role.Button,
                                    onValueChange = onExpandedChange,
                                )
                        } else {
                            Modifier
                        },
                    )
                    .graphicsLayer { alpha = 0.95f }
                    .cyberTextGlow(
                        color = CyberTheme.colors.primary,
                        intensity = if (expanded) 1f else 0.75f,
                        customA11y = chevronA11y,
                    )
                    .then(
                        if (headerContent == null) {
                            Modifier.clearAndSetSemantics { }
                        } else {
                            Modifier
                        },
                    ),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = expansionAnimationSpec),
            exit = shrinkVertically(animationSpec = expansionAnimationSpec),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (overloadAmount.value > 0f) {
                            Modifier
                                .cyberScanlines(
                                    opacity = overloadAmount.value * 0.35f,
                                    animationSpec = overloadAnimationSpec,
                                )
                                .cyberOverload(
                                    intensity = overloadAmount.value,
                                    animationSpec = overloadAnimationSpec,
                                    exitAnimationSpec = overloadAnimationSpec,
                                )
                        } else {
                            Modifier
                        },
                    )
                    .padding(CyberPrimitives.Spacing.dp12),
            ) {
                content()
            }
        }
    }
}