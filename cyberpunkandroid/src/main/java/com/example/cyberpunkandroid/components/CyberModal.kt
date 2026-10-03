package com.example.cyberpunkandroid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.cyberpunkandroid.effects.cyberBorder
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cyberpunkandroid.config.CyberPrimitives

import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * High-priority popup modal dialog window featuring chamfered cut corners, intense neon perimeter glow, and uppercase title bar.
 *
 * Built on Compose [androidx.compose.ui.window.Dialog] with custom scrim padding. Features a highlighted header banner
 * and an intense 16dp neon glow border.
 *
 * @param onDismissRequest Invoked when the user taps outside the dialog or triggers the system back button.
 * @param title Uppercase dialog title displayed in display typography.
 * @param modifier Modifier applied to the inner modal surface.
 * @param content Composable body slot for dialog content and actions.
 */
@Composable
fun CyberModal(
    title: String,
    modifier: Modifier = Modifier,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(CyberPrimitives.Spacing.dp24),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier.cyberComponentSemantics("CyberModal", appendedA11y, customA11y)
                    .fillMaxWidth()
                    .clip(CyberTheme.shapes.cyberCutCornerShape)
                    .background(CyberTheme.colors.background)
                    .cyberBorder(color = CyberTheme.colors.primary,
                        width = CyberPrimitives.BorderWidths.dp2, shape = CyberTheme.shapes.cyberCutCornerShape)
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberTheme.colors.primary.copy(alpha = 0.2f))
                        .padding(CyberPrimitives.Spacing.dp16)
                ) {
                    CompositionLocalProvider(LocalContentColor provides CyberTheme.colors.primary) {
                        ProvideTextStyle(value = CyberTheme.typography.display) {
                            Text(text = title.uppercase())
                        }
                    }
                }
                
                // Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CyberPrimitives.Spacing.dp16),
                    content = content
                )
            }
        }
    }
}
