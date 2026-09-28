package com.example.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberGlowIconPath
import com.example.cyberpunkandroid.effects.CyberSpark
import com.example.cyberpunkandroid.effects.GlowingText
import com.example.cyberpunkandroid.effects.cyberGlowBorder
import com.example.cyberpunkandroid.effects.cyberGlowBorderRounded
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Showcase object containing ONLY debug/unverified effects in a single-column, full-width layout
 * with 2x larger visual assets, rich inner content, and interactive controls.
 */
object EffectsShowcase {

    /**
     * Data class representing a showcase item.
     */
    data class EffectItem(
        val name: String,
        val description: String,
        val content: @Composable () -> Unit
    )

    /**
     * Items being debugged and verified.
     */
    @Composable
    fun getItems(): List<EffectItem> = listOf(
        EffectItem(
            name = "cyberSpark",
            description = "AGSL fine popcorn spark particles with parabolic downward gravity arcs & birth flashes"
        ) {
            CyberSpark(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(CyberTheme.colors.surfaceSecondary)
                    .padding(CyberPrimitives.Spacing.dp16),
                sparkCount = 16,
                intensity = 1.0f,
                speed = 1.2f,
                color = CyberTheme.colors.primary
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp8)
                ) {
                    CyberIcon(
                        iconRes = CyberIcons.Zap,
                        contentDescription = "Spark Energy",
                        tint = CyberTheme.colors.primary,
                        size = 80.dp,
                    )
                    Text(
                        text = "GRAVITY POPCORN SPARKS",
                        style = CyberTheme.typography.display.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = CyberTheme.colors.primary
                    )
                }
            }
        },
        EffectItem(
            name = "cyberGlowBorder",
            description = "CutCorner (20dp) and Rounded (24dp) multi-pass soft neon glow perimeters with layout padding buffer"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp16)) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .cyberGlowBorder(color = CyberTheme.colors.primary, shape = CutCornerShape(36.dp), glowRadius = 22.dp, width = 4.dp)
                            .background(CyberTheme.colors.surfaceSecondary, shape = CutCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("20dp CUT CORNER SOFT GLOW BORDER", style = CyberTheme.typography.terminal, color = CyberTheme.colors.primary)
                    }
                }

                Box(modifier = Modifier.padding(24.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .cyberGlowBorder(color = CyberTheme.colors.secondary, shape = CutCornerShape(36.dp), glowRadius = 24.dp, width = 2.dp)
                            .background(CyberTheme.colors.surfaceSecondary, shape = RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("24dp ROUNDED PROFILE SOFT GLOW BORDER", style = CyberTheme.typography.terminal, color = CyberTheme.colors.secondary)
                    }
                }
            }
        },
        EffectItem(
            name = "GlowingText",
            description = "Multi-stage font glyph bloom (emissive text glow following letter outlines with zero rectangular box)"
        ) {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    GlowingText(
                        text = "test",
                        fontSize = 56.sp,
                        glowRadius = 24.dp,
                        glowColor = CyberTheme.colors.primary,
                        textColor = Color.White
                    )
                }
            }
        },
        EffectItem(
            name = "CyberGlowIconPath",
            description = "100dp vector path outer contour glow icon in CyberGlowIcon.kt"
        ) {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CyberGlowIconPath(
                        painter = painterResource(id = CyberIcons.Zap),
                        contentDescription = "Glow Icon Path",
                        color = CyberTheme.colors.primary,
                        glowColor = CyberTheme.colors.secondary,
                        radius = 24.dp,
                        intensity = 3f,
                        modifier = Modifier.size(100.dp),

                    )
                }
            }
        }
    )

    /**
     * Renders the debug items in a single full-width vertical column.
     */
    @Composable
    fun Content(modifier: Modifier = Modifier) {
        val items = getItems()

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(CyberTheme.colors.background)
                .padding(CyberPrimitives.Spacing.dp16),
            verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp24)
        ) {
            items(items) { item ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp8)
                ) {
                    Text(
                        text = item.name,
                        style = CyberTheme.typography.display.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
                        color = CyberTheme.colors.primary
                    )
                    Text(
                        text = item.description,
                        style = CyberTheme.typography.body.copy(fontSize = 14.sp),
                        color = CyberTheme.colors.textSecondary
                    )
                    item.content()
                }
            }
        }
    }
}

/**
 * Screen composable wrapping [EffectsShowcase.Content].
 */
@Composable
fun EffectsShowcaseScreen(modifier: Modifier = Modifier) {
    EffectsShowcase.Content(modifier = modifier)
}
