package com.example.cyberpunkandroid.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Maps the Cyber diagonal cut-corner style onto Material 3 shape roles. */
fun cyberMaterialShapes(): Shapes {
    fun diagonalCut(cut: Dp) = CutCornerShape(
        topStart = 0.dp,
        topEnd = cut,
        bottomEnd = 0.dp,
        bottomStart = cut
    )

    return Shapes(
        extraSmall = diagonalCut(4.dp),
        small = diagonalCut(8.dp),
        medium = diagonalCut(12.dp),
        large = diagonalCut(16.dp),
        extraLarge = diagonalCut(20.dp)
    )
}

/** Material 3 elevation levels. */
object CyberElevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
    val level5: Dp = 12.dp
}
