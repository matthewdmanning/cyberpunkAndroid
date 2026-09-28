package com.example.cyberpunkandroid

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIconVariant
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class CyberIconTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifyIconVariantTransformationsDoNotCrash() {
        val testIcons = listOf(
            CyberIcons.Terminal,
            CyberIcons.Warning,
            CyberIcons.Cpu,
        )
        
        val variants = listOf(
            CyberIconVariant.Outline,
            CyberIconVariant.Solid,
            CyberIconVariant.Duotone,
            CyberIconVariant.Overload
        )

        composeTestRule.setContent {
            CyberTheme {
                testIcons.forEach { iconRes ->
                    variants.forEach { variant ->
                        CyberIcon(
                            iconRes = iconRes,
                            contentDescription = "Test $variant",
                            variant = variant
                        )
                    }
                }
            }
        }
        
        // Wait for idle to ensure no rendering crashes occurred during composition.
        composeTestRule.waitForIdle()
    }
}
