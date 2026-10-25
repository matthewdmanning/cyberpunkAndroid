package com.example.sample

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SampleAppUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppLaunchAndConduitSurge() {
        composeTestRule.onNodeWithText("TELEMETRY BEACONS", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("CONDUIT SIPHON", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("TRIGGER OVERDRIVE SURGE", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("TRIGGER OVERDRIVE SURGE", ignoreCase = true).performClick()
        composeTestRule.onNodeWithText("DISCHARGING SURGE...", ignoreCase = true).assertExists()
    }
}
