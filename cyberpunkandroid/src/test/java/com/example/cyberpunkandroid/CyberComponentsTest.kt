package com.example.cyberpunkandroid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import com.example.cyberpunkandroid.components.CyberAlert
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberBiometrics
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberDropdown
import com.example.cyberpunkandroid.components.CyberNavigationBar
import com.example.cyberpunkandroid.components.CyberNavLink
import com.example.cyberpunkandroid.components.CyberNavStatus
import com.example.cyberpunkandroid.components.CyberTabs
import com.example.cyberpunkandroid.components.CyberProgress
import com.example.cyberpunkandroid.components.CyberRim
import com.example.cyberpunkandroid.components.CyberSpinner
import com.example.cyberpunkandroid.components.CyberSpinnerOverlay
import com.example.cyberpunkandroid.components.CyberTerminal
import com.example.cyberpunkandroid.components.GlowingText
import com.example.cyberpunkandroid.theme.CyberTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class CyberComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `CyberButton handles clicks and displays text`() {
        var clicked = false
        composeTestRule.setContent {
            CyberTheme {
                CyberButton(
                    onClick = { clicked = true },
                    modifier = Modifier.testTag("button"),
                ) {
                    androidx.compose.material3.Text("CLICK ME")
                }
            }
        }

        composeTestRule.onNodeWithTag("button").performClick()
        assert(clicked)
        composeTestRule.onNodeWithText("CLICK ME").assertIsDisplayed()
    }

    @Test
    fun `CyberBadge displays uppercase text`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberBadge(text = "status active")
            }
        }

        composeTestRule.onNodeWithText("STATUS ACTIVE").assertIsDisplayed()
    }

    @Test
    fun `CyberAlert displays title and message`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberAlert(
                    title = "System Failure",
                    message = "Reboot required"
                )
            }
        }

        composeTestRule.onNodeWithText("SYSTEM FAILURE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reboot required").assertIsDisplayed()
    }

    @Test
    fun `CyberCard handles interactive click`() {
        var clicked = false
        composeTestRule.setContent {
            CyberTheme {
                CyberCard(
                    interactive = true,
                    onClick = { clicked = true },
                    modifier = Modifier.testTag("card")
                ) {
                    androidx.compose.material3.Text("Card Content")
                }
            }
        }

        val node = composeTestRule.onNodeWithTag("card")
        assertEquals(Role.Button, node.fetchSemanticsNode().config[SemanticsProperties.Role])
        node.performClick()
        assert(clicked)
    }

    @Test
    fun `CyberTerminal displays title in lowercase`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberTerminal(title = "CONSOLE") {
                    androidx.compose.material3.Text("Body")
                }
            }
        }

        composeTestRule.onNodeWithText("console").assertIsDisplayed()
        composeTestRule.onNodeWithText("Body").assertIsDisplayed()
    }

    @Test
    fun `CyberNavigationBar displays brand, links and handles selection`() {
        var selectedIndex = 0
        composeTestRule.setContent {
            CyberTheme {
                CyberNavigationBar(
                    brand = "CYBER",
                    items = listOf("Feed", "Nodes"),
                    selectedIndex = selectedIndex,
                    onItemSelected = { selectedIndex = it }
                )
            }
        }

        composeTestRule.onNodeWithText("//").assertIsDisplayed()
        composeTestRule.onNodeWithText("CYBER").assertIsDisplayed()
        composeTestRule.onNodeWithText("FEED").assertIsDisplayed()
        composeTestRule.onNodeWithText("NODES").assertIsDisplayed()

        composeTestRule.onNodeWithText("NODES").performClick()
        assert(selectedIndex == 1)
    }

    @Test
    fun `CyberNavStatus displays status text`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberNavStatus(text = "SYS_ONLINE")
            }
        }

        composeTestRule.onNodeWithText("SYS_ONLINE").assertIsDisplayed()
    }

    @Test
    fun `CyberSpinnerOverlay renders text and handles dismiss click`() {
        var dismissed = false
        composeTestRule.setContent {
            CyberTheme {
                CyberSpinnerOverlay(
                    text = "DECRYPTING MATRIX",
                    onDismissRequest = { dismissed = true },
                    modifier = Modifier.testTag("overlay")
                )
            }
        }

        composeTestRule.onNodeWithText("DECRYPTING MATRIX").assertIsDisplayed()
        composeTestRule.onNodeWithTag("overlay").performClick()
        assert(dismissed)
    }

    @Test
    fun `CyberDropdown exposes plain language action and state semantics`() {
        var selected = 0
        composeTestRule.setContent {
            CyberTheme {
                CyberDropdown(
                    items = listOf("Alpha", "Beta"),
                    selectedIndex = selected,
                    onItemSelected = { selected = it },
                )
            }
        }

        val triggerMatcher = hasText("ALPHA") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
        val triggerNode = composeTestRule.onNode(triggerMatcher)
        triggerNode.assertIsDisplayed()
        assertEquals(
            "Collapsed",
            triggerNode.fetchSemanticsNode().config[SemanticsProperties.StateDescription]
        )
        assertEquals(
            "Open options menu",
            triggerNode.fetchSemanticsNode().config[SemanticsActions.OnClick].label
        )
        assertEquals(
            Role.Button,
            triggerNode.fetchSemanticsNode().config[SemanticsProperties.Role]
        )
        assertFalse(
            triggerNode.fetchSemanticsNode().config.contains(SemanticsProperties.ContentDescription)
        )

        triggerNode.performClick()
        composeTestRule.waitForIdle()

        val expandedTrigger = composeTestRule.onNode(triggerMatcher)
        assertEquals(
            "Expanded",
            expandedTrigger.fetchSemanticsNode().config[SemanticsProperties.StateDescription]
        )
        assertEquals(
            "Close options menu",
            expandedTrigger.fetchSemanticsNode().config[SemanticsActions.OnClick].label
        )
    }

    @Test
    fun `GlowingText preserves visible text announcement and supports custom overrides`() {
        composeTestRule.setContent {
            CyberTheme {
                GlowingText(
                    text = "MATRIX_RELOADED",
                    modifier = Modifier.testTag("glowing_default"),
                )
                GlowingText(
                    text = "STATUS_OK",
                    appendedA11y = "Sensors active",
                    modifier = Modifier.testTag("glowing_appended"),
                )
            }
        }

        val defaultNode = composeTestRule.onNodeWithTag("glowing_default")
        composeTestRule.onNodeWithText("MATRIX_RELOADED").assertIsDisplayed()
        assertFalse(defaultNode.fetchSemanticsNode().config.contains(SemanticsProperties.ContentDescription))

        val appendedNode = composeTestRule.onNodeWithTag("glowing_appended")
        assertEquals(
            listOf("STATUS_OK - Sensors active"),
            appendedNode.fetchSemanticsNode().config[SemanticsProperties.ContentDescription]
        )
    }

    @Test
    fun `CyberProgress exposes standard range and state percentage without code names`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberProgress(
                    progress = 0.65f,
                    modifier = Modifier.testTag("progress"),
                )
            }
        }

        val node = composeTestRule.onNodeWithTag("progress")
        assertEquals(
            0.65f,
            node.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
        )
        assertEquals(
            "65%",
            node.fetchSemanticsNode().config[SemanticsProperties.StateDescription]
        )
        assertFalse(node.fetchSemanticsNode().config.contains(SemanticsProperties.ContentDescription))
    }

    @Test
    fun `CyberRim exposes standard radial progress and dynamic stateDescription`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberRim(
                    progress = 0.4f,
                    modifier = Modifier.testTag("rim"),
                )
            }
        }

        val node = composeTestRule.onNodeWithTag("rim")
        assertEquals(
            0.4f,
            node.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
        )
        assertEquals(
            "40%",
            node.fetchSemanticsNode().config[SemanticsProperties.StateDescription]
        )
        assertFalse(node.fetchSemanticsNode().config.contains(SemanticsProperties.ContentDescription))
    }

    @Test
    fun `CyberSpinner exposes indeterminate range info and plain-language loading description`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberSpinner(
                    modifier = Modifier.testTag("spinner"),
                )
            }
        }

        val node = composeTestRule.onNodeWithTag("spinner")
        assertEquals(
            ProgressBarRangeInfo.Indeterminate,
            node.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        )
        assertEquals(
            listOf("Loading"),
            node.fetchSemanticsNode().config[SemanticsProperties.ContentDescription]
        )
    }

    @Test
    fun `CyberTabs exposes Role Tab and selection state`() {
        var selected = 0
        composeTestRule.setContent {
            CyberTheme {
                CyberTabs(
                    tabs = listOf("System", "Net"),
                    selectedTabIndex = selected,
                    onTabSelected = { selected = it }
                )
            }
        }

        val tab0 = composeTestRule.onNodeWithText("SYSTEM")
        val tab1 = composeTestRule.onNodeWithText("NET")

        assertEquals(Role.Tab, tab0.fetchSemanticsNode().config[SemanticsProperties.Role])
        assertEquals(true, tab0.fetchSemanticsNode().config[SemanticsProperties.Selected])

        assertEquals(Role.Tab, tab1.fetchSemanticsNode().config[SemanticsProperties.Role])
        assertEquals(false, tab1.fetchSemanticsNode().config[SemanticsProperties.Selected])

        tab1.performClick()
        assertEquals(1, selected)
    }

    @Test
    fun `CyberNavLink exposes Role Tab and selection state`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberNavLink(
                    label = "Overview",
                    isSelected = true,
                    onClick = {}
                )
            }
        }

        val node = composeTestRule.onNodeWithText("OVERVIEW")
        assertEquals(Role.Tab, node.fetchSemanticsNode().config[SemanticsProperties.Role])
        assertEquals(true, node.fetchSemanticsNode().config[SemanticsProperties.Selected])
    }

    @Test
    fun `CyberDropdown menu items expose Role RadioButton and selection state`() {
        var selectedIndex = 0
        composeTestRule.setContent {
            CyberTheme {
                CyberDropdown(
                    items = listOf("Alpha", "Beta"),
                    selectedIndex = selectedIndex,
                    onItemSelected = { selectedIndex = it },
                    modifier = Modifier.testTag("dropdown")
                )
            }
        }

        // Open menu
        composeTestRule.onNodeWithTag("dropdown").performClick()

        val isRadioButton = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
        val item0 = composeTestRule.onNode(hasText("ALPHA") and isRadioButton)
        val item1 = composeTestRule.onNode(hasText("BETA") and isRadioButton)

        assertEquals(Role.RadioButton, item0.fetchSemanticsNode().config[SemanticsProperties.Role])
        assertEquals(true, item0.fetchSemanticsNode().config[SemanticsProperties.Selected])

        assertEquals(Role.RadioButton, item1.fetchSemanticsNode().config[SemanticsProperties.Role])
        assertEquals(false, item1.fetchSemanticsNode().config[SemanticsProperties.Selected])
    }

    @Test
    fun `CyberBiometrics respects customA11y and appendedA11y`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberBiometrics(
                    data = listOf(0.2f, 0.8f),
                    appendedA11y = "Heart Rate",
                    modifier = Modifier.testTag("bio-appended")
                )
                CyberBiometrics(
                    data = listOf(0.2f, 0.8f),
                    customA11y = "Neural Wave",
                    modifier = Modifier.testTag("bio-custom")
                )
            }
        }

        val bioAppended = composeTestRule.onNodeWithTag("bio-appended")
        assertEquals(
            listOf("Biometric data waveform - Heart Rate"),
            bioAppended.fetchSemanticsNode().config[SemanticsProperties.ContentDescription]
        )

        val bioCustom = composeTestRule.onNodeWithTag("bio-custom")
        assertEquals(
            listOf("Neural Wave"),
            bioCustom.fetchSemanticsNode().config[SemanticsProperties.ContentDescription]
        )
    }
}
