package com.example.sample

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
    fun testTelemetryBeaconsCardExists() {
        composeTestRule.onNodeWithText("TELEMETRY BEACONS", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("ALERT", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("BEACON", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("SYNC", ignoreCase = true).assertExists()
    }

    @Test
    fun testSynapticGovernorCardExists() {
        composeTestRule.onNodeWithText("SYNAPTIC GOVERNOR", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("ENGAGED", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("LIMITER: ON", ignoreCase = true).assertExists()
    }

    @Test
    fun testNeuralJackInCardExists() {
        composeTestRule.onNodeWithText("BREACH CIPHER // JACK-IN", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("AIR-GAPPED", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("RUN CIPHER", ignoreCase = true).assertExists()
    }

    @Test
    fun testSubdermalCryoCardExists() {
        composeTestRule.onNodeWithText("CRYO REGULATOR // OVERCLOCK", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("5.8 GHz CLOCK", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("COOLANT", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("NOMINAL", ignoreCase = true).assertExists()
    }

    @Test
    fun testConduitTapCardExists() {
        composeTestRule.onNodeWithText("CONDUIT SIPHON", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("480V AC", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("LIVE", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("TRIGGER OVERDRIVE SURGE", ignoreCase = true).assertExists()
    }
}
