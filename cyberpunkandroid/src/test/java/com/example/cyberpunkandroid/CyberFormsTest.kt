package com.example.cyberpunkandroid

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.components.CyberCheckbox
import com.example.cyberpunkandroid.components.CyberField
import com.example.cyberpunkandroid.components.CyberTextArea
import com.example.cyberpunkandroid.components.CyberTextField
import com.example.cyberpunkandroid.theme.CyberTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class CyberFormsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `CyberTextField handles input and displays text`() {
        var value = ""
        composeTestRule.setContent {
            CyberTheme {
                CyberTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.testTag("textField"),
                )
            }
        }

        composeTestRule.onNodeWithTag("textField").performTextInput("Hello Cyber")
        assert(value == "Hello Cyber")
    }

    @Test
    fun `CyberTextArea minLines is respected and handles text`() {
        var value = ""
        composeTestRule.setContent {
            CyberTheme {
                CyberTextArea(
                    value = value,
                    onValueChange = { value = it },
                    minLines = 5,
                    modifier = Modifier.testTag("textArea")
                )
            }
        }

        composeTestRule.onNodeWithTag("textArea").performTextInput("Line 1\nLine 2")
        assert(value == "Line 1\nLine 2")
    }

    @Test
    fun `CyberCheckbox toggles state on click`() {
        var checked = false
        composeTestRule.setContent {
            CyberTheme {
                CyberCheckbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    text = "Accept terms",
                    modifier = Modifier.testTag("checkbox")
                )
            }
        }

        composeTestRule.onNodeWithText("Accept terms").assertIsDisplayed()
        composeTestRule.onNodeWithTag("checkbox").performClick()
        
        assert(checked)
    }

    @Test
    fun `CyberField shows label, error state and helper text`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberField(
                    label = "Username",
                    helperText = "Enter your alias",
                    errorText = "Alias taken",
                    isRequired = true
                ) { isError ->
                    CyberTextField(
                        value = "Johnny",
                        onValueChange = {},
                        isError = isError,
                        modifier = Modifier.testTag("fieldInput")
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("USERNAME").assertIsDisplayed()
        composeTestRule.onNodeWithText(" *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alias taken").assertIsDisplayed()
    }
}
