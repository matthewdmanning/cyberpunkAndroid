package com.example.cyberpunkandroid.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberSectorRim
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

// TODO: document this
@Composable
fun CyberTime(
    timeText: String,
    modifier: Modifier = Modifier,
    rimThickness: Dp = 2.dp,
    rimSectorAngles: List<Float> = listOf(120f, 120f, 120f),
    size: Dp = 120.dp,
    color: Color = CyberTheme.colors.primary,
    appendedA11y: String? = null,
    customA11y: String? = null
) {
    Box(
        modifier = modifier.cyberComponentSemantics("CyberTime", appendedA11y, customA11y).size(size),
        contentAlignment = Alignment.Center
    ) {
        CyberSectorRim(
            color = color,
            thickness = rimThickness,
            sectorAngles = rimSectorAngles,
            gapAngle = 10f,
            size = size
        )

        Text(
            text = timeText,
            color = color,
            style = CyberTheme.typography.display,
            fontWeight = FontWeight.Bold
        )
    }
}
