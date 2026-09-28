package com.example.sample

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class SampleAppScreensTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppNavigationAndScreens() {
        // MainActivity launches with "sandbox" as the start destination
        composeTestRule.onNodeWithText("Render Settings", ignoreCase = true).assertExists()
        
        // Navigate to Sandbox using the global nav tabs
        composeTestRule.onNodeWithText("Sandbox", ignoreCase = true).performClick()



        // Navigate to Effects using the global nav tabs
        composeTestRule.onNodeWithText("Effects", ignoreCase = true).performClick()
        
        // Now on Effects screen, verify the default "Icons" tab renders
        composeTestRule.onNodeWithText("Spin Animation", ignoreCase = true).assertExists()

        // Switch to Overload Tab in Effects screen
        composeTestRule.onNodeWithText("Overload", ignoreCase = true).performClick()

        // Switch to Neon Tab
        composeTestRule.onNodeWithText("Neon", ignoreCase = true).performClick()
        composeTestRule.onNodeWithText("NEON GLOW EFFECT", ignoreCase = true).assertExists()

        // Switch to Mix Tab
        composeTestRule.onNodeWithText("Mix", ignoreCase = true).performClick()
        composeTestRule.onNodeWithText("SYSTEM ONLINE", ignoreCase = true).assertExists()
    }
}
