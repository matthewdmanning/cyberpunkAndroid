package com.example.cyberpunkandroid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberScanlines
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * Retro-futuristic command terminal emulator window with top title bar, traffic light window controls, and animated CRT scanlines.
 *
 * Implements the Cybercore CSS `.cyber-terminal` specification. Features three semantic colored dots in the header
 * and an overlay CRT scanline effect across child content.
 *
 * @param title Terminal window header title string.
 * @param modifier Composable modifier applied to the outer terminal frame.
 * @param content Body slot displaying terminal output or interactive input prompts.
 */
@Composable
fun CyberTerminal(
    title: String,
    modifier: Modifier = Modifier,
    appendedA11y: String? = null,
    customA11y: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.cyberComponentSemantics("CyberTerminal", appendedA11y, customA11y)
            .clip(CyberTheme.shapes.cyberCutCornerShape)
            .background(CyberTheme.colors.background)
    ) {
        // Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberTheme.colors.primary.copy(alpha = 0.2f))
                .padding(horizontal = CyberPrimitives.Spacing.dp12, vertical = CyberPrimitives.Spacing.dp8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cyber traffic dots
            Row(horizontalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp4)) {
                Box(modifier = Modifier.size(CyberPrimitives.Spacing.dp8).clip(CircleShape).background(CyberTheme.semantics.colors.danger))
                Box(modifier = Modifier.size(CyberPrimitives.Spacing.dp8).clip(CircleShape).background(CyberTheme.semantics.colors.warning))
                Box(modifier = Modifier.size(CyberPrimitives.Spacing.dp8).clip(CircleShape).background(CyberTheme.semantics.colors.success))
            }
            Spacer(modifier = Modifier.width(CyberPrimitives.Spacing.dp16))
            CompositionLocalProvider(LocalContentColor provides CyberTheme.colors.primary) {
                ProvideTextStyle(value = CyberTheme.typography.terminal) {
                    Text(text = title.lowercase())
                }
            }
        }
        
        // Body with CRT scanlines
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CyberPrimitives.Spacing.dp16)
        ) {
            CompositionLocalProvider(LocalContentColor provides CyberTheme.semantics.colors.terminal) {
                ProvideTextStyle(value = CyberTheme.typography.terminal) {
                    content()
                }
            }
        }
    }
}
