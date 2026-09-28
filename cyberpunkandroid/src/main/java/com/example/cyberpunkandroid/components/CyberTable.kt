package com.example.cyberpunkandroid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.example.cyberpunkandroid.effects.cyberBorder
import androidx.compose.ui.draw.clip
import com.example.cyberpunkandroid.config.CyberPrimitives

import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Monospace sci-fi data table featuring chamfered cut corners, neon border, tinted header row, and alternating zebra-striped rows.
 *
 * Distributes columns evenly across available width, styling cell content in monospace terminal typography.
 *
 * @param headers List of column header title strings.
 * @param rows List of data rows, where each row is a list of cell string values.
 * @param modifier Modifier applied to the outer table container.
 */
@Composable
fun CyberTable(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier,
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CyberTheme.shapes.cyberCutCornerShape)
            .background(CyberTheme.colors.surface)
            .cyberBorder(color = CyberTheme.colors.primary.copy(alpha = 0.5f),
                width = CyberPrimitives.BorderWidths.dp1, shape = CyberTheme.shapes.cyberCutCornerShape)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberTheme.colors.primary.copy(alpha = 0.2f))
                .padding(horizontal = CyberPrimitives.Spacing.dp16, vertical = CyberPrimitives.Spacing.dp8)
        ) {
            headers.forEach { header ->
                Box(modifier = Modifier.weight(1f)) {
                    CompositionLocalProvider(LocalContentColor provides CyberTheme.colors.primary) {
                        ProvideTextStyle(value = CyberTheme.typography.terminal) {
                            Text(text = header.uppercase())
                        }
                    }
                }
            }
        }

        // Body
        rows.forEachIndexed { index, row ->
            val isStriped = (index % 2 == 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isStriped) CyberTheme.colors.background else CyberTheme.colors.surface)
                    .padding(horizontal = CyberPrimitives.Spacing.dp16, vertical = CyberPrimitives.Spacing.dp8)
            ) {
                row.forEach { cell ->
                    Box(modifier = Modifier.weight(1f)) {
                        CompositionLocalProvider(LocalContentColor provides CyberTheme.colors.textPrimary) {
                            ProvideTextStyle(value = CyberTheme.typography.terminal) {
                                Text(text = cell)
                            }
                        }
                    }
                }
            }
        }
    }
}
