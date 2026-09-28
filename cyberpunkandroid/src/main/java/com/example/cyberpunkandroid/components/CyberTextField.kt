package com.example.cyberpunkandroid.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberSemantics
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Single-line text input field featuring chamfered cut corners, animated focus/error border transitions, and terminal typography.
 *
 * Implements the Cybercore CSS `.cyber-input` specification with custom geometric path outlines.
 *
 * ### Interactive Behaviors:
 * - Focus state tracked via [interactionSource] to smoothly animate border and glow highlights between resting, focused, and error colors.
 * - Enforces disabled and read-only states, adjusting foreground and border contrast.
 *
 * @param value Current string contents of the text field.
 * @param onValueChange Callback invoked with updated text content on user input.
 * @param modifier Composable modifier applied to the text field container.
 * @param enabled Controls whether the text field is interactive.
 * @param isError Highlights the border and accents in semantic danger red when validation fails.
 * @param readOnly When true, displays the text as un-editable while remaining focusable/selectable.
 * @param placeholder Optional prompt text shown when [value] is empty.
 * @param keyboardOptions Software keyboard configuration options.
 * @param keyboardActions Software keyboard action callbacks.
 * @param interactionSource Stream tracking focus and interaction events.
 * @param appendedA11y Optional text to append to the default accessibility label.
 * @param customA11y Optional text to completely override the accessibility label.
 */
@Composable
fun CyberTextField(
    value: String,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colorAnimationSpec: androidx.compose.animation.core.AnimationSpec<androidx.compose.ui.graphics.Color> = tween(CyberPrimitives.Durations.ms300, easing = com.example.cyberpunkandroid.config.CyberConfig.Easings.OutExpoEasing),
    appendedA11y: String? = null,
    customA11y: String? = null,
    onValueChange: (String) -> Unit,
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    val targetColor = when {
        !enabled -> CyberTheme.colors.textSecondary
        isError -> CyberTheme.semantics.colors.danger
        isFocused -> CyberTheme.colors.primary
        else -> CyberTheme.colors.border
    }

    val color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = colorAnimationSpec,
        label = "CyberTextFieldColor",
    )

    val textColor = if (enabled) CyberTheme.colors.textPrimary else CyberTheme.colors.textSecondary

    val cornerSize = CyberPrimitives.Spacing.dp12.value

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .cyberSemantics("CyberTextField", appendedA11y, customA11y)
            .fillMaxWidth()
            .clip(CyberTheme.shapes.cyberCutCornerShape)
            .background(CyberTheme.colors.surface)
            .drawBehind {
                val path = Path().apply {
                    moveTo(0f, cornerSize)
                    lineTo(cornerSize, 0f)
                    lineTo(size.width - cornerSize, 0f)
                    lineTo(size.width, cornerSize)
                    lineTo(size.width, size.height - cornerSize)
                    lineTo(size.width - cornerSize, size.height)
                    lineTo(cornerSize, size.height)
                    lineTo(0f, size.height - cornerSize)
                    close()
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = if (isFocused) CyberPrimitives.BorderWidths.dp2.toPx() else CyberPrimitives.BorderWidths.dp1.toPx())
                )
                // Tech corner accents
                val accentLength = cornerSize * 1.5f
                if (isFocused) {
                    val accentPath = Path().apply {
                        // Top Left
                        moveTo(0f, cornerSize + accentLength)
                        lineTo(0f, cornerSize)
                        lineTo(cornerSize, 0f)
                        lineTo(cornerSize + accentLength, 0f)

                        // Bottom Right
                        moveTo(size.width, size.height - cornerSize - accentLength)
                        lineTo(size.width, size.height - cornerSize)
                        lineTo(size.width - cornerSize, size.height)
                        lineTo(size.width - cornerSize - accentLength, size.height)
                    }
                    drawPath(
                        path = accentPath,
                        color = color,
                        style = Stroke(width = CyberPrimitives.BorderWidths.dp4.toPx())
                    )
                }
            },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = CyberTheme.typography.terminal.copy(color = textColor),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(color),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    horizontal = CyberPrimitives.Spacing.dp16,
                    vertical = CyberPrimitives.Spacing.dp12
                )
            ) {
                Text(
                    text = ">_",
                    style = CyberTheme.typography.terminal,
                    color = color,
                    modifier = Modifier.padding(end = CyberPrimitives.Spacing.dp8)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && (placeholder != null)) {
                        Text(
                            text = placeholder,
                            style = CyberTheme.typography.terminal,
                            color = CyberTheme.colors.textSecondary
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}
