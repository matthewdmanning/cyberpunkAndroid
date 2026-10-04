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

/** Oare Sans Black Oblique for prominent Cyber display, headline, and title roles. */
val OareSansDisplayFontFamily = FontFamily(
    Font(
        resId = R.font.oare_sans_black_oblique,
        weight = FontWeight.Black,
        style = FontStyle.Italic
    )
)

/** Fastup font family for Cyber body and label roles. */
val FastupFontFamily = FontFamily(
    Font(resId = R.font.fastup_regular, weight = FontWeight.Normal),
    Font(resId = R.font.fastup_bold, weight = FontWeight.Bold)
)

// TODO: document this
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
 * Maps every Material 3 text role to Cyberpunk Android font families while preserving the caller's role-specific
 * size, line-height, weight, and tracking. Prominent roles use Oare Sans; body and label roles use Fastup.
 *
 * @param base Material typography whose role metrics should be retained.
 */
fun cyberMaterialTypography(base: Typography = Typography()): Typography = base.copy(
    displayLarge = base.displayLarge.copy(fontFamily = OareSansDisplayFontFamily, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic),
    displayMedium = base.displayMedium.copy(fontFamily = OareSansDisplayFontFamily, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic),
    displaySmall = base.displaySmall.copy(fontFamily = OareSansDisplayFontFamily, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic),
    headlineLarge = base.headlineLarge.copy(fontFamily = OareSansDisplayFontFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = OareSansDisplayFontFamily),
    headlineSmall = base.headlineSmall.copy(fontFamily = OareSansDisplayFontFamily),
    titleLarge = base.titleLarge.copy(fontFamily = OareSansDisplayFontFamily),
    titleMedium = base.titleMedium.copy(fontFamily = OareSansDisplayFontFamily),
    titleSmall = base.titleSmall.copy(fontFamily = OareSansDisplayFontFamily),
    bodyLarge = base.bodyLarge.copy(fontFamily = FastupFontFamily),
    bodyMedium = base.bodyMedium.copy(fontFamily = FastupFontFamily),
    bodySmall = base.bodySmall.copy(fontFamily = FastupFontFamily),
    labelLarge = base.labelLarge.copy(fontFamily = FastupFontFamily),
    labelMedium = base.labelMedium.copy(fontFamily = FastupFontFamily),
    labelSmall = base.labelSmall.copy(fontFamily = FastupFontFamily),
)