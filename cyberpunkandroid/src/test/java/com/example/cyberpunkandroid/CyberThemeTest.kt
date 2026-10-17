package com.example.cyberpunkandroid

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.config.CyberSemanticTokens
import com.example.cyberpunkandroid.theme.CyberColors
import com.example.cyberpunkandroid.theme.CyberElevation
import com.example.cyberpunkandroid.theme.CyberShapes
import com.example.cyberpunkandroid.theme.CyberSpacing
import com.example.cyberpunkandroid.theme.CyberStatusColors
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.theme.CyberTypography
import com.example.cyberpunkandroid.theme.LocalCyberColors
import com.example.cyberpunkandroid.theme.LocalCyberElevation
import com.example.cyberpunkandroid.theme.LocalCyberSemanticTokens
import com.example.cyberpunkandroid.theme.LocalCyberShapes
import com.example.cyberpunkandroid.theme.LocalCyberSpacing
import com.example.cyberpunkandroid.theme.LocalCyberStatusColors
import com.example.cyberpunkandroid.theme.LocalCyberTypography
import com.example.cyberpunkandroid.theme.cyberColors
import com.example.cyberpunkandroid.theme.cyberElevation
import com.example.cyberpunkandroid.theme.cyberShapes
import com.example.cyberpunkandroid.theme.cyberSpacing
import com.example.cyberpunkandroid.theme.cyberStatusColors
import com.example.cyberpunkandroid.theme.cyberTypography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class CyberThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `static compatibility for CyberElevation and CyberSpacing tokens`() {
        assertEquals(0.dp, CyberElevation.level0)
        assertEquals(1.dp, CyberElevation.level1)
        assertEquals(3.dp, CyberElevation.level2)
        assertEquals(6.dp, CyberElevation.level3)
        assertEquals(8.dp, CyberElevation.level4)
        assertEquals(12.dp, CyberElevation.level5)
        assertEquals(CyberElevation(), CyberElevation.Default)

        val elevationInstance = CyberElevation()
        assertEquals(0.dp, elevationInstance.level0)
        assertEquals(1.dp, elevationInstance.level1)
        assertEquals(3.dp, elevationInstance.level2)
        assertEquals(6.dp, elevationInstance.level3)
        assertEquals(8.dp, elevationInstance.level4)
        assertEquals(12.dp, elevationInstance.level5)

        val spacingInstance = CyberSpacing()
        assertEquals(CyberPrimitives.Spacing.dp2, spacingInstance.dp2)
        assertEquals(CyberPrimitives.Spacing.dp4, spacingInstance.dp4)
        assertEquals(CyberPrimitives.Spacing.dp8, spacingInstance.dp8)
        assertEquals(CyberPrimitives.Spacing.dp12, spacingInstance.dp12)
        assertEquals(CyberPrimitives.Spacing.dp16, spacingInstance.dp16)
        assertEquals(CyberPrimitives.Spacing.dp24, spacingInstance.dp24)
        assertEquals(CyberPrimitives.Spacing.dp32, spacingInstance.dp32)
        assertEquals(CyberPrimitives.Spacing.dp48, spacingInstance.dp48)
        assertEquals(CyberPrimitives.Spacing.dp64, spacingInstance.dp64)
    }

    @Test
    fun `fallback values resolve to defaults outside CyberTheme`() {
        var observedColors: CyberColors? = null
        var observedTypography: CyberTypography? = null
        var observedShapes: CyberShapes? = null
        var observedElevation: CyberElevation? = null
        var observedSpacing: CyberSpacing? = null
        var observedSemantics: CyberSemanticTokens? = null
        var observedStatus: CyberStatusColors? = null

        composeTestRule.setContent {
            observedColors = LocalCyberColors.current
            observedTypography = LocalCyberTypography.current
            observedShapes = LocalCyberShapes.current
            observedElevation = LocalCyberElevation.current
            observedSpacing = LocalCyberSpacing.current
            observedSemantics = LocalCyberSemanticTokens.current
            observedStatus = LocalCyberStatusColors.current
        }

        assertEquals(CyberColors(), observedColors)
        assertEquals(CyberTypography(), observedTypography)
        assertEquals(CyberShapes(), observedShapes)
        assertEquals(CyberElevation(), observedElevation)
        assertEquals(CyberSpacing(), observedSpacing)
        assertEquals(CyberSemanticTokens(), observedSemantics)
        assertEquals(cyberStatusColors(), observedStatus)
    }

    @Test
    fun `theme object and MaterialTheme extension accessors resolve inside CyberTheme`() {
        var themeColors: CyberColors? = null
        var themeTypography: CyberTypography? = null
        var themeShapes: CyberShapes? = null
        var themeElevation: CyberElevation? = null
        var themeSpacing: CyberSpacing? = null
        var themeSemantics: CyberSemanticTokens? = null
        var themeStatus: CyberStatusColors? = null

        var m3Colors: CyberColors? = null
        var m3Typography: CyberTypography? = null
        var m3Shapes: CyberShapes? = null
        var m3Elevation: CyberElevation? = null
        var m3Spacing: CyberSpacing? = null

        var contentColor: Color? = null
        var observedBodyFontFamily: androidx.compose.ui.text.font.FontFamily? = null
        var observedBodyFontSize: androidx.compose.ui.unit.TextUnit? = null

        composeTestRule.setContent {
            CyberTheme {
                themeColors = CyberTheme.colors
                themeTypography = CyberTheme.typography
                themeShapes = CyberTheme.shapes
                themeElevation = CyberTheme.elevation
                themeSpacing = CyberTheme.spacing
                themeSemantics = CyberTheme.semantics
                themeStatus = CyberTheme.status

                m3Colors = MaterialTheme.cyberColors
                m3Typography = MaterialTheme.cyberTypography
                m3Shapes = MaterialTheme.cyberShapes
                m3Elevation = MaterialTheme.cyberElevation
                m3Spacing = MaterialTheme.cyberSpacing

                contentColor = LocalContentColor.current
                val currentStyle = LocalTextStyle.current
                observedBodyFontFamily = currentStyle.fontFamily
                observedBodyFontSize = currentStyle.fontSize
            }
        }

        assertEquals(CyberColors(), themeColors)
        assertEquals(CyberTypography(), themeTypography)
        assertEquals(CyberShapes(), themeShapes)
        assertEquals(CyberElevation(), themeElevation)
        assertEquals(CyberSpacing(), themeSpacing)
        assertEquals(CyberSemanticTokens(), themeSemantics)
        assertEquals(cyberStatusColors(), themeStatus)

        assertEquals(themeColors, m3Colors)
        assertEquals(themeTypography, m3Typography)
        assertEquals(themeShapes, m3Shapes)
        assertEquals(themeElevation, m3Elevation)
        assertEquals(themeSpacing, m3Spacing)

        assertEquals(themeColors?.textPrimary, contentColor)
        assertEquals(themeTypography?.body?.fontFamily, observedBodyFontFamily)
        assertEquals(themeTypography?.body?.fontSize, observedBodyFontSize)
    }

    @Test
    fun `custom theme overrides cascade to CyberTheme accessors and extensions`() {
        val customColors = CyberColors(primary = Color.Green, background = Color.Black)
        val customElevation = CyberElevation(level1 = 4.dp, level5 = 24.dp)
        val customSpacing = CyberSpacing(dp4 = 6.dp, dp16 = 20.dp)

        var observedThemeColors: CyberColors? = null
        var observedM3Colors: CyberColors? = null
        var observedThemeElevation: CyberElevation? = null
        var observedM3Elevation: CyberElevation? = null
        var observedThemeSpacing: CyberSpacing? = null
        var observedM3Spacing: CyberSpacing? = null
        var observedContentColor: Color? = null

        composeTestRule.setContent {
            CyberTheme(
                colors = customColors,
                elevation = customElevation,
                spacing = customSpacing
            ) {
                observedThemeColors = CyberTheme.colors
                observedM3Colors = MaterialTheme.cyberColors
                observedThemeElevation = CyberTheme.elevation
                observedM3Elevation = MaterialTheme.cyberElevation
                observedThemeSpacing = CyberTheme.spacing
                observedM3Spacing = MaterialTheme.cyberSpacing
                observedContentColor = LocalContentColor.current
            }
        }

        assertEquals(customColors, observedThemeColors)
        assertEquals(customColors, observedM3Colors)
        assertEquals(customElevation, observedThemeElevation)
        assertEquals(customElevation, observedM3Elevation)
        assertEquals(customSpacing, observedThemeSpacing)
        assertEquals(customSpacing, observedM3Spacing)
        assertEquals(customColors.textPrimary, observedContentColor)
    }

    @Test
    fun `non-intrusive embedding with wrapMaterialTheme false does not overwrite outer MaterialTheme`() {
        val outerColorScheme: ColorScheme = darkColorScheme(
            primary = Color(0xFF123456),
            secondary = Color(0xFF654321)
        )
        var observedM3ColorScheme: ColorScheme? = null
        var observedCyberColors: CyberColors? = null
        var observedCyberElevation: CyberElevation? = null
        var observedCyberSpacing: CyberSpacing? = null

        composeTestRule.setContent {
            MaterialTheme(colorScheme = outerColorScheme) {
                CyberTheme(wrapMaterialTheme = false) {
                    observedM3ColorScheme = MaterialTheme.colorScheme
                    observedCyberColors = CyberTheme.colors
                    observedCyberElevation = CyberTheme.elevation
                    observedCyberSpacing = CyberTheme.spacing
                }
            }
        }

        assertEquals(outerColorScheme.primary, observedM3ColorScheme?.primary)
        assertEquals(outerColorScheme.secondary, observedM3ColorScheme?.secondary)
        assertEquals(CyberColors(), observedCyberColors)
        assertEquals(CyberElevation(), observedCyberElevation)
        assertEquals(CyberSpacing(), observedCyberSpacing)
    }
}
