package com.example.cyberpunkandroid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.components.CyberLaserText
import com.example.cyberpunkandroid.effects.cyberLaserOutliner
import com.example.cyberpunkandroid.theme.CyberTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class CyberLaserOutlinerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `cyberLaserOutliner on text renders and attaches semantics`() {
        composeTestRule.setContent {
            CyberTheme {
                Box(
                    modifier = Modifier
                        .size(200.dp, 60.dp)
                        .testTag("laser_text_box")
                        .cyberLaserOutliner(text = "NEON", fontSize = 24.sp)
                )
            }
        }

        val node = composeTestRule.onNodeWithTag("laser_text_box").fetchSemanticsNode()
        val desc = node.config[SemanticsProperties.ContentDescription].first()
        assertEquals("CyberLaserOutliner", desc)
    }

    @Test
    fun `cyberLaserOutliner on shape outline renders without text`() {
        composeTestRule.setContent {
            CyberTheme {
                Box(
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                        .testTag("laser_shape_box")
                        .cyberLaserOutliner(
                            text = null,
                            shape = CutCornerShape(12.dp)
                        )
                )
            }
        }

        val node = composeTestRule.onNodeWithTag("laser_shape_box").fetchSemanticsNode()
        val desc = node.config[SemanticsProperties.ContentDescription].first()
        assertEquals("CyberLaserOutliner", desc)
    }

    @Test
    fun `CyberLaserText composable renders with correct semantics`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberLaserText(
                    text = "CYBERPUNK",
                    fontSize = 28.sp,
                    modifier = Modifier.testTag("cyber_laser_text"),
                    appendedA11y = "status terminal"
                )
            }
        }

        val node = composeTestRule.onNodeWithTag("cyber_laser_text").fetchSemanticsNode()
        val desc = node.config[SemanticsProperties.ContentDescription].first()
        assertTrue(desc.contains("CyberLaserText"))
        assertTrue(desc.contains("status terminal"))
    }

    @Test
    fun `CyberLaserText handles empty and blank string gracefully`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberLaserText(
                    text = "   ",
                    modifier = Modifier.testTag("blank_laser_text")
                )
            }
        }

        composeTestRule.onNodeWithTag("blank_laser_text").assertExists()
    }
}
