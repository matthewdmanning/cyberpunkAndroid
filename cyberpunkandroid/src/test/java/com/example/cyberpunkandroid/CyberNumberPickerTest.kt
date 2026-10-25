package com.example.cyberpunkandroid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.components.CyberNumberPicker
import com.example.cyberpunkandroid.components.CyberNumberPickerOrientation
import com.example.cyberpunkandroid.components.CyberNumberPickerSize
import com.example.cyberpunkandroid.theme.CyberTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class CyberNumberPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `number picker clamps initial value and increments within range`() {
        var currentValue = 5
        composeTestRule.setContent {
            CyberTheme {
                CyberNumberPicker(
                    value = currentValue,
                    range = 0..10,
                    step = 1,
                    modifier = Modifier.testTag("picker"),
                    onValueChange = { currentValue = it },
                )
            }
        }

        // Verify initial value displayed
        composeTestRule.onNodeWithText("5").assertIsDisplayed()

        // Click increment
        composeTestRule.onNodeWithContentDescription("Increment").performClick()
        assertEquals(6, currentValue)
    }

    @Test
    fun `number picker decrements within range`() {
        var currentValue = 5
        composeTestRule.setContent {
            CyberTheme {
                CyberNumberPicker(
                    value = currentValue,
                    range = 0..10,
                    step = 2,
                    modifier = Modifier.testTag("picker"),
                    onValueChange = { currentValue = it },
                )
            }
        }

        // Click decrement
        composeTestRule.onNodeWithContentDescription("Decrement").performClick()
        assertEquals(3, currentValue)
    }

    @Test
    fun `number picker does not increment beyond range max`() {
        var currentValue = 10
        composeTestRule.setContent {
            CyberTheme {
                CyberNumberPicker(
                    value = currentValue,
                    range = 0..10,
                    step = 1,
                    onValueChange = { currentValue = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Increment").performClick()
        assertEquals(10, currentValue)
    }

    @Test
    fun `number picker does not decrement below range min`() {
        var currentValue = 0
        composeTestRule.setContent {
            CyberTheme {
                CyberNumberPicker(
                    value = currentValue,
                    range = 0..10,
                    step = 1,
                    onValueChange = { currentValue = it },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Decrement").performClick()
        assertEquals(0, currentValue)
    }

    @Test
    fun `number picker exposes progress bar and state semantics`() {
        var value by mutableIntStateOf(42)
        composeTestRule.setContent {
            CyberTheme {
                CyberNumberPicker(
                    value = value,
                    range = 0..100,
                    step = 1,
                    modifier = Modifier.testTag("picker"),
                    formatValue = { "VAL-$it" },
                    onValueChange = { value = it },
                )
            }
        }

        val semantics = composeTestRule.onNodeWithTag("picker").fetchSemanticsNode()
        assertTrue(semantics.config.contains(SemanticsProperties.ProgressBarRangeInfo))

        val progressInfo = semantics.config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(42f, progressInfo.current)

        val stateDesc = semantics.config[SemanticsProperties.StateDescription]
        assertEquals("VAL-42", stateDesc)

        // Test setProgress semantic action
        composeTestRule.onNodeWithTag("picker").performSemanticsAction(SemanticsActions.SetProgress) { action ->
            action(75f)
        }
        assertEquals(75, value)
    }

    @Test
    fun `vertical orientation renders label, prefix, suffix, and custom format`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberNumberPicker(
                    value = 7,
                    range = 0..15,
                    size = CyberNumberPickerSize.Large,
                    orientation = CyberNumberPickerOrientation.Vertical,
                    label = "Core Level",
                    prefix = "LVL:",
                    suffix = "GHz",
                    formatValue = { String.format("%02d", it) },
                    onValueChange = {},
                )
            }
        }

        composeTestRule.onNodeWithText("CORE LEVEL").assertIsDisplayed()
        composeTestRule.onNodeWithText("LVL:").assertIsDisplayed()
        composeTestRule.onNodeWithText("07").assertIsDisplayed()
        composeTestRule.onNodeWithText("GHz").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Increment").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Decrement").assertIsDisplayed()
    }
}
