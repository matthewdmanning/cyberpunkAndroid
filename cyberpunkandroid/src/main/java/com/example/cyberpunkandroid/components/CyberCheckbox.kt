package com.example.cyberpunkandroid.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberSemantics
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Interactive futuristic checkbox featuring chamfered cut corners, animated vector checkmark drawing, and optional label text.
 *
 * Implements the Cybercore CSS `.cyber-checkbox` specification with custom graphics rendering.
 *
 * ### Interactive Behaviors:
 * - Animates border and checkmark tint transition using [androidx.compose.animation.animateColorAsState].
 * - Animates checkmark vector drawing stroke progression from 0% to 100% via [androidx.compose.animation.core.animateFloatAsState].
 * - Supports disabled and validation error states, altering border and glow colors accordingly.
 *
 * @param checked Whether the checkbox is currently checked.
 * @param onCheckedChange Callback invoked when the checkbox toggle state changes, or null for read-only presentation.
 * @param modifier Composable modifier applied to the outer checkbox layout.
 * @param enabled Controls whether the checkbox can be toggled.
 * @param isError Highlights the checkbox in error red if validation fails.
 * @param text Optional label text displayed beside the checkbox box.
 * @param interactionSource Stream tracking user interaction states.
 * @param appendedA11y Optional text to append to the default accessibility label.
 * @param customA11y Optional text to completely override the accessibility label.
 */
@Composable
fun CyberCheckbox(
    checked: Boolean,
    text: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    size: androidx.compose.ui.unit.Dp = CyberPrimitives.IconSizes.dp24,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colorAnimationSpec: androidx.compose.animation.core.AnimationSpec<androidx.compose.ui.graphics.Color> = tween(CyberPrimitives.Durations.ms300, easing = com.example.cyberpunkandroid.config.CyberConfig.Easings.OutExpoEasing),
    checkAnimationSpec: androidx.compose.animation.core.AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms150, easing = com.example.cyberpunkandroid.config.CyberConfig.Easings.OutExpoEasing),
    appendedA11y: String? = null,
    customA11y: String? = null,
    onCheckedChange: ((Boolean) -> Unit)?,
) {
    val targetColor = when {
        !enabled -> CyberTheme.colors.textSecondary
        isError -> CyberTheme.semantics.colors.danger
        checked -> CyberTheme.colors.primary
        else -> CyberTheme.colors.border
    }

    val color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = colorAnimationSpec,
        label = "CyberCheckboxColor"
    )

    val checkProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = checkAnimationSpec,
        label = "CyberCheckboxProgress"
    )

    val toggleableModifier = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            onValueChange = onCheckedChange,
            enabled = enabled,
            role = Role.Checkbox,
            interactionSource = interactionSource,
            indication = null
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .cyberSemantics("CyberCheckbox", appendedA11y, customA11y)
            .then(toggleableModifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val graphicsSize = this.size
                        val strokeWidth = CyberPrimitives.BorderWidths.dp2.toPx()
                        val corner = CyberPrimitives.Spacing.dp4.toPx()
                        
                        // Box path
                        val boxPath = Path().apply {
                            moveTo(0f, corner)
                            lineTo(corner, 0f)
                            lineTo(graphicsSize.width, 0f)
                            lineTo(graphicsSize.width, graphicsSize.height - corner)
                            lineTo(graphicsSize.width - corner, graphicsSize.height)
                            lineTo(0f, graphicsSize.height)
                            close()
                        }

                        drawPath(
                            path = boxPath,
                            color = color,
                            style = Stroke(width = strokeWidth, join = StrokeJoin.Miter)
                        )

                        // Tick
                        if (checkProgress > 0f) {
                            val tickPath = Path().apply {
                                val start = Offset(graphicsSize.width * 0.25f, graphicsSize.height * 0.5f)
                                val mid = Offset(graphicsSize.width * 0.45f, graphicsSize.height * 0.7f)
                                val end = Offset(graphicsSize.width * 0.8f, graphicsSize.height * 0.25f)
                                
                                moveTo(start.x, start.y)
                                
                                if (checkProgress < 0.5f) {
                                    val p = checkProgress * 2f
                                    lineTo(
                                        start.x + (mid.x - start.x) * p,
                                        start.y + (mid.y - start.y) * p
                                    )
                                } else {
                                    lineTo(mid.x, mid.y)
                                    val p = (checkProgress - 0.5f) * 2f
                                    lineTo(
                                        mid.x + (end.x - mid.x) * p,
                                        mid.y + (end.y - mid.y) * p
                                    )
                                }
                            }
                            
                            drawPath(
                                path = tickPath,
                                color = color,
                                style = Stroke(
                                    width = CyberPrimitives.BorderWidths.dp2.toPx(),
                                    cap = StrokeCap.Square,
                                    join = StrokeJoin.Miter
                                )
                            )
                        }
                    }
            )
        }
        
        if (text != null) {
            Text(
                text = text,
                style = CyberTheme.typography.body,
                color = if (enabled) CyberTheme.colors.textPrimary else CyberTheme.colors.textSecondary,
                modifier = Modifier.padding(start = CyberPrimitives.Spacing.dp12)
            )
        }
    }
}
