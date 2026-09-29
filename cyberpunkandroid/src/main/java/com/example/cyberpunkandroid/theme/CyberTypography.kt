package com.example.cyberpunkandroid.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.R

/**
 * Custom Neusharp sci-fi display font family.
 */
val NeusharpFontFamily = FontFamily(
    Font(resId = R.font.neusharp_bold, weight = FontWeight.Bold),
    Font(resId = R.font.neusharp_bold, weight = FontWeight.Normal),
    Font(resId = R.font.neusharp_bold, weight = FontWeight.W700)
)

/**
 * Custom Fastup font family for body and regular text.
 */
val FastupFontFamily = FontFamily(
    Font(resId = R.font.fastup_regular, weight = FontWeight.Normal),
    Font(resId = R.font.fastup_bold, weight = FontWeight.Bold),
    Font(resId = R.font.fastup_bold, weight = FontWeight.W700)
)

@Immutable
data class CyberTypography(
    val display: TextStyle = TextStyle(
        fontFamily = NeusharpFontFamily,
        fontWeight = FontWeight.W700,
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
