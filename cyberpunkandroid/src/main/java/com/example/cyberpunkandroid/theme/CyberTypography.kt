package com.example.cyberpunkandroid.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.R

/**
 * Oare Sans Black Oblique for Material 3 display roles.
 */
val OareSansDisplayFontFamily = FontFamily(
    Font(
        resId = R.font.oare_sans_black_oblique,
        weight = FontWeight.Black,
        style = FontStyle.Italic
    )
)

/**
 * Custom Fastup font family for body and regular text.
 */
val FastupFontFamily = FontFamily(
    Font(resId = R.font.fastup_regular, weight = FontWeight.Normal),
    Font(resId = R.font.fastup_bold, weight = FontWeight.Bold)
)

@Immutable
data class CyberTypography(
    val display: TextStyle = TextStyle(
        fontFamily = OareSansDisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontStyle = FontStyle.Italic,
        fontSize = 32.sp,
        letterSpacing = 0.1.em
    ),
    val terminal: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.1.em
    ),
    val body: TextStyle = TextStyle(
        fontFamily = FastupFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.1.em
    )
)

/**
 * Applies the Cyber display face to the standard Material 3 display scale while
 * preserving Material 3's size, line-height and tracking defaults.
 */
fun cyberMaterialTypography(base: Typography = Typography()): Typography = base.copy(
    displayLarge = base.displayLarge.copy(
        fontFamily = OareSansDisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontStyle = FontStyle.Italic
    ),
    displayMedium = base.displayMedium.copy(
        fontFamily = OareSansDisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontStyle = FontStyle.Italic
    ),
    displaySmall = base.displaySmall.copy(
        fontFamily = OareSansDisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontStyle = FontStyle.Italic
    )
)
