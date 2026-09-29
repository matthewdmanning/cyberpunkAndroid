package com.example.cyberpunkandroid.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.config.CyberPrimitives

@Immutable
data class CyberTypography(
    // Display: Orbitron, wide geometric headings
    val display: TextStyle = TextStyle(
        fontFamily = CyberFonts.Orbitron,
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
    // Body: Rajdhani, narrow squared technical sans
    val body: TextStyle = TextStyle(
        fontFamily = CyberFonts.Rajdhani,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.1.em
    )
)
