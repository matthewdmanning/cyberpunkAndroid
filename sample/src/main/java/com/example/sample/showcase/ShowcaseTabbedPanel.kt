package com.example.sample.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cyberpunkandroid.components.CyberTabs
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * The two tabs that every Level 3 and Level 4 showcase card has.
 *
 * @property label The text that the tab bar shows for this tab.
 */
enum class ShowcaseTab(val label: String) {
    /** The user-facing surface, inside the lore of the equipment. */
    Display("DISPLAY"),

    /** The equipment and the hidden parts that operate the display. */
    Backbone("BACKBONE"),
}

/**
 * Lays out a Level 3 or Level 4 card body as a status strip, a tab bar, and one tab page.
 *
 * The status strip stays above the tab bar. Thus critical alarms stay visible on
 * both tabs. Both tab pages read the same caller-owned state, so they always agree.
 *
 * @param modifier Modifier applied to the outer column.
 * @param statusStrip Content that always shows above the tabs, for example the alarm badge.
 * @param display Content of the [ShowcaseTab.Display] page.
 * @param backbone Content of the [ShowcaseTab.Backbone] page.
 */
@Composable
fun ShowcaseTabbedPanel(
    modifier: Modifier = Modifier,
    statusStrip: @Composable () -> Unit,
    display: @Composable () -> Unit,
    backbone: @Composable () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(ShowcaseTab.Display.ordinal) }
    val tabs = ShowcaseTab.entries
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp12),
    ) {
        statusStrip()
        CyberTabs(
            tabs = tabs.map { it.label },
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
        )
        when (tabs[selectedTab]) {
            ShowcaseTab.Display -> display()
            ShowcaseTab.Backbone -> backbone()
        }
    }
}
