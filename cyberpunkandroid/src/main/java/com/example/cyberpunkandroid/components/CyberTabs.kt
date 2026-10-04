package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberBorder
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Segmented futuristic navigation tab bar featuring a smooth sliding neon glow indicator.
 *
 * ### Interactive & Layout Behaviors:
 * - Smoothly translates an active glowing indicator box across tabs.
 * - Hardware-accelerated sliding via `graphicsLayer { translationX }` for zero composition overhead during animations.
 * - Renders tab labels in uppercase terminal monospace typography.
 *
 * @param tabs List of tab title strings.
 * @param selectedTabIndex Zero-based index of the currently active tab.
 * @param modifier Modifier applied to the tab bar container.
 * @param animationSpec TODO: document this
 * @param appendedA11y TODO: document this
 * @param customA11y TODO: document this
 * @param onTabSelected Callback invoked when a tab segment is tapped.
 */
@Composable
fun CyberTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Float> = spring(),
    appendedA11y: String? = null,
    customA11y: String? = null,
    onTabSelected: (Int) -> Unit,
) {
    val animatedIndex by animateFloatAsState(
        targetValue = selectedTabIndex.toFloat(),
        animationSpec = animationSpec,
        label = "IndicatorOffset",
    )

    Box(
        modifier = modifier
            .cyberComponentSemantics("CyberTabs", appendedA11y, customA11y)
            .fillMaxWidth()
            .height(48.dp) // Standard touch target height
            .background(CyberTheme.colors.surface)
    ) {
        // Glowing Indicator
        if (tabs.isNotEmpty() && selectedTabIndex in tabs.indices) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f / tabs.size)
                    .fillMaxHeight()
                    .graphicsLayer {
                        // size.width here is the width of exactly ONE tab
                        translationX = size.width * animatedIndex
                    }
                    .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
                    .background(CyberTheme.colors.primary.copy(alpha = 0.2f))
                    .cyberBorder(
                        color = CyberTheme.colors.primary,
                        width = CyberPrimitives.BorderWidths.dp2,
                        shape = CyberTheme.shapes.cyberCutCornerShapeSmall
                    )
            )
        }

        Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTabIndex
                val color = if (isSelected) CyberTheme.colors.primary else CyberTheme.colors.textSecondary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        color = color,
                        style = CyberTheme.typography.terminal
                    )
                }
            }
        }
    }
}
