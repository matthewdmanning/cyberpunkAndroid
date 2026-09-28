package com.example.sample

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SampleAppUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testFullUserJourney() {
        // 1. Verify we start on the Gateway Screen
        composeTestRule.onNodeWithText("LOGIN").assertExists()
        composeTestRule.onNodeWithText("HACK").assertExists()

        // 3. Click the Hack button to bypass login instantly
        composeTestRule.onNodeWithText("HACK").performClick()
        
        // Wait for Compose to settle after navigation (Hack delay is 800ms)
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.waitForIdle()

        // 4. Verify we are now on the Command Center Screen
        // "Actions" button should be visible on the Command Center
        composeTestRule.onNodeWithText("Actions").assertExists()
        
        // Check for specific cyber tables or tabs (now node lists)
        composeTestRule.onNodeWithText("192.168.1.42", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("SCANNING", ignoreCase = true).assertExists()

        // 5. Open the Actions menu and navigate to Uplink Terminal
        // The dropdown trigger says "SELECT..." (or we can just click "SELECT..." ignore case)
        composeTestRule.onNodeWithText("SELECT...", ignoreCase = true).performClick()
        
        // The dropdown should now show the terminal option
        composeTestRule.onNodeWithText("Uplink Terminal", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("Uplink Terminal", ignoreCase = true).performClick()
        
        composeTestRule.waitForIdle()

        // 6. Verify we are on the Terminal Screen
        // Look for terminal-specific text or the back button
        composeTestRule.onNodeWithText(">_").assertExists()
        composeTestRule.onNodeWithText("ESTABLISHING SECURE CONNECTION...", substring = true).assertExists()

        // 7. Click back to return to the Command Center
        composeTestRule.onNodeWithText("Back").performClick()
        composeTestRule.waitForIdle()

        // 8. Verify we are back on the Command Center Screen
        composeTestRule.onNodeWithText("SELECT...", ignoreCase = true).assertExists()
        
        // 9. Navigate to Sandbox Screen
        composeTestRule.onNodeWithText("SELECT...", ignoreCase = true).performClick()
        composeTestRule.onNodeWithText("Component Sandbox", ignoreCase = true).performClick()
        
        composeTestRule.waitForIdle()
        
        // 10. Verify we are on the Sandbox Screen
        composeTestRule.onNodeWithText("Render Settings").assertExists()
        composeTestRule.onNodeWithText("Icon Matrix").assertExists()
    }
}
