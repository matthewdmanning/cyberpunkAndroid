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
    fun testAppLaunchAndScaffold() {
        composeTestRule.onNodeWithText("NEON DECK", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("INITIALIZE", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("INITIALIZE", ignoreCase = true).performClick()
    }
}
