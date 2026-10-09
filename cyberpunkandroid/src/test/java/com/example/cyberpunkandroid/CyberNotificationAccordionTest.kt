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
        composeTestRule.onNodeWithContentDescription("CyberAccordion", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()
        assertTrue(expanded)
        composeTestRule.onNodeWithText("Nested terminal output").assertIsDisplayed()
    }

    @Test
    fun `accordion with headerContent exposes actionable chevron without CyberTextGlow collision`() {
        var expanded by mutableStateOf(false)
        composeTestRule.setContent {
            CyberTheme {
                CyberAccordion(
                    title = "Settings",
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    headerContent = { Text("Custom Header") },
                ) {
                    Text("Expanded Settings Body")
                }
            }
        }

        // Chevron must expose "Expand section" and must NOT be swallowed by "CyberTextGlow"
        composeTestRule.onNodeWithContentDescription("CyberTextGlow", useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Expand section", useUnmergedTree = true)
            .assertIsDisplayed()
            .performSemanticsAction(SemanticsActions.OnClick)

        composeTestRule.waitForIdle()
        assertTrue(expanded)

        // After expansion, chevron changes to "Collapse section"
        composeTestRule.onNodeWithContentDescription("Collapse section", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun `accordion with default header marks chevron decorative and clears redundant nodes`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberAccordion(
                    title = "Default Header",
                    expanded = false,
                    onExpandedChange = {},
                ) {
                    Text("Body")
                }
            }
        }

        // Chevron should be decorative (clearAndSetSemantics) when the row itself is the button
        composeTestRule.onNodeWithContentDescription("CyberTextGlow", useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("CyberAccordionChevron", useUnmergedTree = true)
            .assertDoesNotExist()
    }
}
