package com.example.cyberpunkandroid.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.example.cyberpunkandroid.R

/**
 * Bundled open-licensed (SIL OFL 1.1) cyberpunk typefaces; license texts ship in assets/licenses/.
 *
 * - [Orbitron]: wide, geometric, squared-off display face for large headings.
 * - [Rajdhani]: narrow, squared technical sans for headlines, body and UI text.
 *
 * Refinery and Blender (the commercial faces used in Cyberpunk 2077) are not bundled because their
 * licenses don't allow redistribution. To use licensed copies, add the files to your app's res/font and
 * pass them through [CyberTypography], e.g. `CyberTypography(display = CyberTypography().display.copy(
 * fontFamily = FontFamily(Font(R.font.refinery_bold, FontWeight.Bold))))`.
 */
object CyberFonts {
    // Only the weights the theme uses are bundled (~380 KB each, as Rajdhani includes Devanagari);
    // other weights are synthesized from the nearest one
    val Rajdhani: FontFamily = FontFamily(
        Font(R.font.rajdhani_regular, FontWeight.Normal),
        Font(R.font.rajdhani_medium, FontWeight.Medium),
        Font(R.font.rajdhani_bold, FontWeight.Bold)
    )

    // One variable-weight file covers Regular (400) through Black (900). Weight variation needs API 26+;
    // on API 24–25 every weight renders as Regular
    @OptIn(ExperimentalTextApi::class)
    val Orbitron: FontFamily = FontFamily(
        listOf(400, 500, 600, 700, 800, 900).map { weight ->
            Font(
                R.font.orbitron_variable,
                FontWeight(weight),
                variationSettings = FontVariation.Settings(FontVariation.weight(weight))
            )
        }
    )
}
