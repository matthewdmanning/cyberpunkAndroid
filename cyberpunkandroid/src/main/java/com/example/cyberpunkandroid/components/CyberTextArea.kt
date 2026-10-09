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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Multi-line expanded text input field featuring dynamic line numbering gutter, chamfered cut corners, and focus animations.
 *
 * Implements the Cybercore CSS `.cyber-textarea` specification with integrated IDE-style line numbers.
 *
 * ### Interactive Behaviors:
 * - Line count dynamically calculated and rendered in a left gutter column.
 * - Focus transitions smoothly animate border stroke colors via [androidx.compose.animation.animateColorAsState].
 *
 * @param value Current text string content.
 * @param onValueChange Callback invoked with updated text upon user input.
 * @param modifier Composable modifier applied to the text area container.
 * @param enabled Controls whether the text area is interactive.
 * @param isError Highlights border and accents in danger red when true.
 * @param readOnly Disables text modification while permitting selection and scrolling.
 * @param placeholder Prompt string shown when [value] is empty.
 * @param minLines Minimum number of vertical text rows rendered. Defaults to `3`.
 * @param keyboardOptions Software keyboard configuration options.
 * @param keyboardActions Software keyboard action callbacks.
 * @param interactionSource Stream tracking focus and interaction events.
 * @param appendedA11y Optional text to append to the default accessibility label.
 * @param customA11y Optional text to completely override the accessibility label.
 */
@Composable
fun CyberTextArea(
    value: String,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    readOnly: Boolean = false,
    minLines: Int = 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colorAnimationSpec: androidx.compose.animation.core.AnimationSpec<androidx.compose.ui.graphics.Color> = tween(CyberPrimitives.Durations.ms300, easing = com.example.cyberpunkandroid.config.CyberConfig.Easings.OutExpoEasing),
    appendedA11y: String? = null,
    customA11y: String? = null,
    onValueChange: (String) -> Unit,
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    var lineCount by remember { mutableIntStateOf(minLines) }

    val targetColor = when {
        !enabled -> CyberTheme.colors.textSecondary
        isError -> CyberTheme.semantics.colors.danger
        isFocused -> CyberTheme.colors.primary
        else -> CyberTheme.colors.border
    }

    val color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = colorAnimationSpec,
        label = "CyberTextAreaColor"
    )

    val textColor = if (enabled) CyberTheme.colors.textPrimary else CyberTheme.colors.textSecondary
    val cornerSize = CyberPrimitives.Spacing.dp12.value

    val a11yDescription = customA11y ?: appendedA11y

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .then(
                if (a11yDescription != null) {
                    Modifier.semantics { contentDescription = a11yDescription }
                } else {
                    Modifier
                }
            )
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
            },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = CyberTheme.typography.terminal.copy(color = textColor),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(color),
        minLines = minLines,
        onTextLayout = { textLayoutResult ->
            val actualLines = textLayoutResult.lineCount
            lineCount = if (actualLines > minLines) actualLines else minLines
        },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(
                    vertical = CyberPrimitives.Spacing.dp12
                ),
                verticalAlignment = Alignment.Top
            ) {
                // Line numbers column
                Box(
                    modifier = Modifier
                        .width(CyberPrimitives.Spacing.dp32)
                        .padding(end = CyberPrimitives.Spacing.dp8),
                    contentAlignment = Alignment.TopEnd
                ) {
                    val lineNumbers = (1..lineCount).joinToString("\n") {
                        it.toString().padStart(2, '0')
                    }
                    Text(
                        text = lineNumbers,
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary.copy(alpha = 0.5f),
                        textAlign = TextAlign.End
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = CyberPrimitives.Spacing.dp16)
                ) {
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
