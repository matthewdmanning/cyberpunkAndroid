package com.example.cyberpunkandroid

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.SemanticsMatcher
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.components.CyberSlider
import com.example.cyberpunkandroid.components.CyberSwitch
import com.example.cyberpunkandroid.theme.CyberTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class CyberControlsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `slider clamps controlled value and exposes range semantics`() {
        var value = 4f
        composeTestRule.setContent {
            CyberTheme {
                CyberSlider(
                    value = value,
                    valueRange = 0f..1f,
                    modifier = Modifier.testTag("slider"),
                    onValueChange = { value = it },
                )
            }
        }

        assertTrue(
            composeTestRule.onNodeWithTag("slider").fetchSemanticsNode().config.contains(SemanticsProperties.ProgressBarRangeInfo),
        )
        assertEquals(1f, composeTestRule.onNodeWithTag("slider").fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current)
        composeTestRule.onNodeWithTag("slider").performSemanticsAction(SemanticsActions.SetProgress) { action ->
            action(0.5f)
        }
        assertEquals(0.5f, value)
    }

    @Test
    fun `switch toggles and exposes switch semantics`() {
        var checked by mutableStateOf(false)
        composeTestRule.setContent {
            CyberTheme {
                CyberSwitch(
                    checked = checked,
                    modifier = Modifier.testTag("switch"),
                    onCheckedChange = { checked = it },
                )
            }
        }

        composeTestRule.onNodeWithTag("switch").assertIsDisplayed().performClick()
        assertTrue(checked)
        composeTestRule.onNodeWithTag("switch").assertIsOn()
    }
}
