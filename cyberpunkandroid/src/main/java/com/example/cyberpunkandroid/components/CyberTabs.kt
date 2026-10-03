package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.cyberpunkandroid.effects.cyberBorder
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import com.example.cyberpunkandroid.config.CyberPrimitives

import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.roundToInt
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * Segmented futuristic navigation tab bar featuring dynamic width measurement and a smooth sliding neon glow indicator.
 *
 * ### Interactive & Layout Behaviors:
 * - Measures overall component width via [androidx.compose.ui.layout.onSizeChanged] to calculate per-tab dimensions.
 * - Smoothly translates an active glowing indicator box (`indicatorOffset`) across tabs via [androidx.compose.animation.core.animateFloatAsState].
 * - Renders tab labels in uppercase terminal monospace typography.
 *
 * @param tabs List of tab title strings.
 * @param selectedTabIndex Zero-based index of the currently active tab.
 * @param onTabSelected Callback invoked when a tab segment is tapped.
 * @param modifier Modifier applied to the tab bar container.
 */
@Composable
fun CyberTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Float> = androidx.compose.animation.core.spring(),
    appendedA11y: String? = null,
    customA11y: String? = null,
    onTabSelected: (Int) -> Unit,
) {
    var totalWidth by remember { mutableIntStateOf(0) }
    val tabWidth = if (tabs.isNotEmpty() && (totalWidth > 0)) totalWidth / tabs.size else 0

    val indicatorOffset by animateFloatAsState(
        targetValue = (selectedTabIndex * tabWidth).toFloat(),
        animationSpec = animationSpec,
        label = "IndicatorOffset",
    )

    Box(
        modifier = modifier.cyberComponentSemantics("CyberTabs", appendedA11y, customA11y)
            .fillMaxWidth()
            .height(CyberPrimitives.Spacing.dp32 + CyberPrimitives.Spacing.dp16)
            .background(CyberTheme.colors.surface)
            .onSizeChanged { totalWidth = it.width }
    ) {
        // Glowing Indicator
        if (tabWidth > 0 && selectedTabIndex in tabs.indices) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(indicatorOffset.roundToInt(), 0) }
                    .fillMaxWidth(1f / tabs.size)
                    .fillMaxHeight()
                    .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
                    .background(CyberTheme.colors.primary.copy(alpha = 0.2f))
                    .cyberBorder(color = CyberTheme.colors.primary,
                        width = CyberPrimitives.BorderWidths.dp2, shape = CyberTheme.shapes.cyberCutCornerShapeSmall)
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
                    CompositionLocalProvider(LocalContentColor provides color) {
                        ProvideTextStyle(value = CyberTheme.typography.terminal) {
                            Text(text = title.uppercase())
                        }
                    }
                }
            }
        }
    }
}
