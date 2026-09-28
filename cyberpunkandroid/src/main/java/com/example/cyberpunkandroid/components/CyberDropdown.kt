package com.example.cyberpunkandroid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.cyberpunkandroid.config.CyberPrimitives

import com.example.cyberpunkandroid.effects.cyberBorder
import com.example.cyberpunkandroid.effects.cyberSemantics
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Interactive sci-fi selection dropdown menu with chamfered cut-corner trigger, neon border glow, and popover menu options.
 *
 * Implements the Cybercore CSS `.cyber-dropdown` specification. Toggles menu visibility on trigger tap, displaying menu options in uppercase monospace typography
 * and highlighting the active selection with a primary-tinted background wash.
 *
 * @param items List of string options to display.
 * @param selectedIndex Index of the currently selected option.
 * @param onItemSelected Callback invoked with the newly selected option index.
 * @param modifier Modifier applied to the trigger container.
 * @param placeholder Default prompt displayed when [selectedIndex] is outside the bounds of [items]. Defaults to `"SELECT..."`.
 * @param appendedA11y Optional text to append to the default accessibility label.
 * @param customA11y Optional text to completely override the accessibility label.
 */
@Composable
fun CyberDropdown(
    items: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    placeholder: String = "SELECT...",
    appendedA11y: String? = null,
    customA11y: String? = null,
    onItemSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(value = false) }

    Box(modifier = modifier.cyberSemantics("CyberDropdown", appendedA11y, customA11y)) {
        // Trigger
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
                .background(CyberTheme.colors.surface)
                .cyberBorder(color = CyberTheme.colors.primary,
                    width = CyberPrimitives.BorderWidths.dp1, shape = CyberTheme.shapes.cyberCutCornerShapeSmall)
                .clickable { expanded = true }
                .padding(CyberPrimitives.Spacing.dp12)
        ) {
            val text = if (selectedIndex in items.indices) items[selectedIndex] else placeholder
            CompositionLocalProvider(LocalContentColor provides CyberTheme.colors.primary) {
                ProvideTextStyle(value = CyberTheme.typography.terminal) {
                    Text(text = text.uppercase())
                }
            }
        }

        // Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(CyberTheme.colors.background)
                .cyberBorder(color = CyberTheme.colors.primary,
                    width = CyberPrimitives.BorderWidths.dp1, shape = CyberTheme.shapes.cyberCutCornerShapeSmall)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemSelected(index)
                                expanded = false
                            }
                            .background(if (index == selectedIndex) CyberTheme.colors.primary.copy(alpha = 0.2f) else CyberTheme.colors.background)
                            .padding(horizontal = CyberPrimitives.Spacing.dp16, vertical = CyberPrimitives.Spacing.dp12)
                    ) {
                        CompositionLocalProvider(LocalContentColor provides if (index == selectedIndex) CyberTheme.colors.primary else CyberTheme.colors.textPrimary) {
                            ProvideTextStyle(value = CyberTheme.typography.terminal) {
                                Text(text = item.uppercase())
                            }
                        }
                    }
                }
            }
        }
    }
}
