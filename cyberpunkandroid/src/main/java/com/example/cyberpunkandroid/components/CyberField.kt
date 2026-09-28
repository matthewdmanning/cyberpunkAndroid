package com.example.cyberpunkandroid.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Structural sci-fi form input container providing label, required indicator, animated helper/error messages, and input slots.
 *
 * Wraps arbitrary input controls (such as [CyberTextField], [CyberTextArea], or [CyberDropdown]) with standardized
 * monospace labeling and error message transitions.
 *
 * @param label Uppercase field title displayed above the input control.
 * @param modifier Composable modifier applied to the outer field column layout.
 * @param helperText Optional explanatory subtext displayed beneath the input.
 * @param errorText Optional validation error message. When present, switches field styling to semantic danger red.
 * @param isRequired When true, renders a highlighted cyan asterisk beside the label.
 * @param content Slot rendering the child input composable, receiving the current `isError` status.
 */
@Composable
fun CyberField(
    label: String,
    helperText: String? = null,
    errorText: String? = null,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    enter: androidx.compose.animation.EnterTransition = fadeIn() + expandVertically(),
    exit: androidx.compose.animation.ExitTransition = fadeOut() + shrinkVertically(),
    appendedA11y: String? = null,
    customA11y: String? = null,
    content: @Composable (isError: Boolean) -> Unit,
) {
    val isError = errorText != null

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = CyberPrimitives.Spacing.dp8)
        ) {
            Text(
                text = label.uppercase(),
                style = CyberTheme.typography.terminal,
                color = if (isError) CyberTheme.semantics.colors.danger else CyberTheme.colors.textPrimary
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary
                )
            }
        }

        content(isError)

        AnimatedVisibility(
            visible = isError || (helperText != null),
            enter = enter,
            exit = exit
        ) {
            Column {
                Spacer(modifier = Modifier.height(CyberPrimitives.Spacing.dp4))
                if (errorText != null) {
                    Text(
                        text = errorText,
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.semantics.colors.danger
                    )
                } else if (helperText != null) {
                    Text(
                        text = helperText,
                        style = CyberTheme.typography.terminal,
                        color = CyberTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}
