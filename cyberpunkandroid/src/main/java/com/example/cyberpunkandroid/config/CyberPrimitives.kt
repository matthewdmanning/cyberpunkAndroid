package com.example.cyberpunkandroid.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object CyberPrimitives {
    object Colors {
        // Cyan Scale
        val Cyan300 = Color(0xFF87EAF2)
        val Cyan400 = Color(0xFF54D1DB)
        val Cyan500 = Color(0xFF00F0FF) // Base
        val Cyan600 = Color(0xFF00C4CC)
        val Cyan700 = Color(0xFF0097A7)
        val Cyan900 = Color(0xFF005D6A)

        // Magenta Scale
        val Magenta500 = Color(0xFFFF2A6D) // Base
        val Magenta700 = Color(0xFFB31248)

        // Yellow Scale
        val Yellow500 = Color(0xFFFCEE0A) // Base
        val Yellow700 = Color(0xFFACA406)

        // Green Scale
        val Green400 = Color(0xFF43FF83)
        val Green500 = Color(0xFF05FFA1) // Base
        val Green700 = Color(0xFF03A969)
        val Green900 = Color(0xFF015331)

        // Void Scale
        val Void100 = Color(0xFF2A2D3A)
        val Void200 = Color(0xFF1F2230)
        val Void300 = Color(0xFF181A25)
        val Void400 = Color(0xFF12141D)
        val Void500 = Color(0xFF0D0E14) // Base
        val Void700 = Color(0xFF07080C)
        val Void800 = Color(0xFF040508)
        val Void900 = Color(0xFF010204)

        // Chrome Scale
        val Chrome100 = Color(0xFFF0F1F5)
        val Chrome200 = Color(0xFFD1D5DC)
        val Chrome300 = Color(0xFFB2B7C7)
        val Chrome400 = Color(0xFF939AB0)
        val Chrome500 = Color(0xFF747D99) // Base
        val Chrome600 = Color(0xFF5C647A)
    }

    object Spacing {
        val dp4 = 4.dp
        val dp8 = 8.dp
        val dp12 = 12.dp
        val dp16 = 16.dp
        val dp24 = 24.dp
        val dp32 = 32.dp
    }

    object BorderWidths {
        val dp1 = 1.dp
        val dp2 = 2.dp
        val dp4 = 4.dp
    }

    object IconSizes {
        val dp16 = 16.dp // Small/Inline
        val dp24 = 24.dp // Standard/Medium
        val dp32 = 32.dp // Large
        val dp48 = 48.dp // Extra Large
        val dp64 = 64.dp // Massive
    }

    object Durations {
        const val ms150 = 150
        const val ms300 = 300
        const val ms500 = 500
    }

    object Shadows {
        fun neonGlow(color: Color, radius: Float = 8f) = androidx.compose.ui.graphics.Shadow(
            color = color,
            blurRadius = radius
        )
    }
}
