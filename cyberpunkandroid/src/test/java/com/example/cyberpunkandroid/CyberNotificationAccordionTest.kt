package com.example.cyberpunkandroid

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.components.CyberAccordion
import com.example.cyberpunkandroid.components.CyberAlertVariant
import com.example.cyberpunkandroid.components.CyberSnackbar
import com.example.cyberpunkandroid.theme.CyberTheme
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.semantics.SemanticsProperties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class CyberNotificationAccordionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `snackbar is caller controlled and renders critical message`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberSnackbar(
                    title = "Critical",
                    message = "Reactor offline",
                    visible = true,
                    variant = CyberAlertVariant.Error,
                    critical = true,
                )
            }
        }

        composeTestRule.onNodeWithText("CRITICAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reactor offline").assertIsDisplayed()
    }

    @Test
    fun `accordion exposes controlled expansion and nested content`() {
        var expanded by mutableStateOf(false)
        composeTestRule.setContent {
            CyberTheme {
                CyberAccordion(
                    title = "Diagnostics",
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    Text("Nested terminal output")
                }
            }
        }

        composeTestRule.onNodeWithText("Nested terminal output").assertDoesNotExist()
        val headerNode = composeTestRule.onNodeWithText("DIAGNOSTICS")
        headerNode.assertIsDisplayed()
        assertEquals(
            "Collapsed",
            headerNode.fetchSemanticsNode().config[SemanticsProperties.StateDescription]
        )

        headerNode.performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()
        assertTrue(expanded)
        composeTestRule.onNodeWithText("Nested terminal output").assertIsDisplayed()
    }

    @Test
    fun `accordion suppresses inner decorative chevron semantics when header handles clicks`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberAccordion(
                    title = "Diagnostics",
                    expanded = false,
                ) {
                    Text("Details")
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("CyberAccordionChevron", useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("CyberTextGlow", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun `accordion chevron exposes plain language action when custom header is used`() {
        var expanded by mutableStateOf(false)
        composeTestRule.setContent {
            CyberTheme {
                CyberAccordion(
                    title = "Ignored",
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    headerContent = { Text("Custom Header") },
                ) {
                    Text("Expanded Content")
                }
            }
        }

        val toggleChevron = composeTestRule.onNodeWithContentDescription("Expand section", useUnmergedTree = true)
        toggleChevron.assertIsDisplayed()
        assertEquals(
            "Collapsed",
            toggleChevron.fetchSemanticsNode().config[SemanticsProperties.StateDescription]
        )
        toggleChevron.performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()
        assertTrue(expanded)
    }

    @Test
    fun `accordion supports custom and appended accessibility labels without class name prefixes`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberAccordion(
                    title = "Diagnostics",
                    expanded = false,
                    appendedA11y = "Sensors",
                ) {
                    Text("Sensors output")
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Diagnostics - Sensors").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("CyberAccordion - Sensors").assertDoesNotExist()
    }
}
