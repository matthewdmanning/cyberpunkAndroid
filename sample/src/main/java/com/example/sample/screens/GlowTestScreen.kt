package com.example.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.effects.CyberGlowContainer
import com.example.cyberpunkandroid.effects.GlowingText
import com.example.cyberpunkandroid.effects.cyberTextGlow
import com.example.cyberpunkandroid.theme.CyberTheme

@Composable
fun GlowTestScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GLOW TEST SCREEN",
            style = CyberTheme.typography.display,
            color = CyberTheme.colors.primary
        )

        // CARD 1: RAW MODIFIER APPROACH
        CyberCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NEON GLOW",
                    style = CyberTheme.typography.display,
                    color = Color.White,
                    modifier = Modifier.cyberTextGlow(
                        color = Color.Cyan,
                        radius = 16.dp,
                        intensity = 2f
                    )
                )
            }
        }

        // CARD 2: GLOWING TEXT COMPONENT
        CyberCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                GlowingText(
                    text = "GLOWING TEXT",
                    glowColor = Color.Magenta,
                    textColor = Color.White,
                    glowRadius = 16.dp
                )
            }
        }

        // CARD 3: CONTAINER APPROACH
        CyberCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CyberGlowContainer(
                    color = Color.Green,
                    radius = 24.dp,
                    intensity = 2f
                ) {
                    Text(
                        text = "CONTAINER GLOW",
                        style = CyberTheme.typography.display,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlowTestScreenPreview() {
    CyberTheme {
        GlowTestScreen()
    }
}
