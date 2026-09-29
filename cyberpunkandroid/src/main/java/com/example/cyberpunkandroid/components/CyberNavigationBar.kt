package com.example.cyberpunkandroid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * Top-level sci-fi header navigation bar featuring sticky layout styling, glowing brand typography,
 * interactive glowing nav links with sliding bottom indicator bars, and optional status indicator chips.
 *
 * Imitates the Cybercore CSS `.cyber-nav` specification.
 *
 * ### Interactive & Visual Behaviors:
 * - Brand heading includes a distinctive `//` prefix in secondary magenta followed by uppercase display text
 *   with an emissive cyan drop-shadow bloom.
 * - Interactive navigation links track press and active states via [androidx.compose.foundation.interaction.MutableInteractionSource] and [androidx.compose.foundation.interaction.collectIsPressedAsState].
 * - When tapped or active, links smoothly animate text color to [CyberTheme.colors.primary], project text glow,
 *   scale slightly for tactile feedback, and translate a 2dp bottom neon highlight line into view.
 * - Displays an optional [statusText] pill styled with semantic green borders, translucent fill, and a pulsating LED beacon.
 *
 * @param brand Brand title string displayed in the navbar header. Defaults to `"CYBERCORE"`.
 * @param items List of navigation item label strings.
 * @param selectedIndex Zero-based index of the currently active navigation item, or `-1` for none.
 * @param onItemSelected Callback invoked with the selected item index when a nav item is tapped.
 * @param modifier Modifier applied to the outer navigation bar container.
 * @param statusText Optional status copy (e.g. `"SYS_OK"`, `"ONLINE"`) rendered in an LED status badge.
 * @param brandContent Optional custom composable slot overriding the default [brand] text presentation.
 * @param actions Optional trailing composable slot for auxiliary controls or action icons.
 */
@Composable
fun CyberNavigationBar(
    items: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    brand: String = "CYBERCORE",
    statusText: String? = null,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onItemSelected: (Int) -> Unit,
    brandContent: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.cyberComponentSemantics("CyberNavigationBar", appendedA11y, customA11y)
            .fillMaxWidth()
            .background(CyberTheme.colors.surface.copy(alpha = 0.90f))
            .border(
                width = CyberPrimitives.BorderWidths.dp1,
                color = CyberTheme.colors.border
            )
            .padding(
                horizontal = CyberPrimitives.Spacing.dp16,
                vertical = CyberPrimitives.Spacing.dp12
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Brand / Logo section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp8)
        ) {
            if (brandContent != null) {
                brandContent()
            } else {
                Text(
                    text = "//",
                    style = CyberTheme.typography.display.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberTheme.colors.secondary
                    )
                )
                Text(
                    text = brand.uppercase(),
                    style = CyberTheme.typography.display.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberTheme.colors.primary
                    ),
                    modifier = Modifier
                )
            }
        }

        // Navigation links and actions section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp12)
        ) {
            // Interactive nav links
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp4)
            ) {
                items.forEachIndexed { index, label ->
                    val isSelected = index == selectedIndex
                    CyberNavLink(
                        label = label,
                        isSelected = isSelected,
                        onClick = { onItemSelected(index) }
                    )
                }
            }

            // Optional status pill indicator
            if (statusText != null) {
                CyberNavStatus(text = statusText)
            }

            // Trailing actions slot
            if (actions != null) {
                actions()
            }
        }
    }
}
