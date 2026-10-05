package com.example.cyberpunkandroid.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object CyberRadius {
    val default: Shape = CutCornerShape(
        topStart = 0.dp,
        topEnd = 12.dp,
        bottomEnd = 0.dp,
        bottomStart = 12.dp
    )
}

@Immutable
data class CyberShapes(
    val cyberCutCornerShape: Shape = CyberRadius.default,
    val cyberCutCornerShapeSmall: Shape = CutCornerShape(
        topStart = 0.dp,
        topEnd = 8.dp,
        bottomEnd = 0.dp,
        bottomStart = 8.dp
    ),
    val cardShape: Shape = CutCornerShape(
        topStart = 0.dp,
        topEnd = 20.dp,
        bottomEnd = 0.dp,
        bottomStart = 20.dp
    ),
    val roundedSm: Shape = RoundedCornerShape(2.dp),
    val roundedMd: Shape = RoundedCornerShape(4.dp),
    val roundedLg: Shape = RoundedCornerShape(8.dp),
    val roundedXl: Shape = RoundedCornerShape(16.dp),
    val roundedFull: Shape = RoundedCornerShape(9999.dp)
)

