package com.example.cyberpunkandroid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.components.CyberAlert
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberNavigationBar
import com.example.cyberpunkandroid.components.CyberNavStatus
import com.example.cyberpunkandroid.components.CyberSpinnerOverlay
import com.example.cyberpunkandroid.components.CyberTerminal
import com.example.cyberpunkandroid.theme.CyberTheme
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

        composeTestRule.onNodeWithTag("card").performClick()
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
}
