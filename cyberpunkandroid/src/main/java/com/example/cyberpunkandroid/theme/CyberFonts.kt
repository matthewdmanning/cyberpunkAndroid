package com.example.cyberpunkandroid.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.example.cyberpunkandroid.R

/** Bundled optional cyberpunk typefaces. Material role assignment is defined separately. */
object CyberFonts {
    val Rajdhani: FontFamily = FontFamily(
        Font(R.font.rajdhani_regular, FontWeight.Normal),
        Font(R.font.rajdhani_medium, FontWeight.Medium),
        Font(R.font.rajdhani_bold, FontWeight.Bold)
    )

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
