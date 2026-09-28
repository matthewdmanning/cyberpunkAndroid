package com.example.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberFeedbackFixture
import com.example.cyberpunkandroid.components.loadFeedbackConfigFromAssets
import com.example.cyberpunkandroid.components.resetFeedbackFiles
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberBackdropBlur
import com.example.cyberpunkandroid.effects.cyberDatastream
import com.example.cyberpunkandroid.effects.cyberFloat
import com.example.cyberpunkandroid.effects.cyberStripes

@Composable
fun FeedbackFormScreen() {
    val context = LocalContext.current
    var resetKey by remember { mutableIntStateOf(0) }
    
    val configs = remember(resetKey) {
        val allFiles = context.assets.list("")?.filter { it.startsWith("test_") && it.endsWith(".json") } ?: emptyList()
        allFiles.mapNotNull { loadFeedbackConfigFromAssets(context, it) }
    }

    if (configs.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ALL TESTS COMPLETED", color = CyberPrimitives.Colors.Cyan500, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            CyberButton(
                onClick = {
                    resetFeedbackFiles(context)
                    resetKey++
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("RESET EVERYTHING")
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { configs.size })

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Test ${pagerState.currentPage + 1} of ${configs.size}: ${configs[pagerState.currentPage].testName.replace('_', ' ')}",
                color = CyberPrimitives.Colors.Cyan500,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val config = configs[page]
            CyberFeedbackFixture(
                config = config,
            ) { value, iconSize ->
                val testName = config.testName
                
                val effectModifier = when {
                    testName.contains("doe_float") -> {
                        val run = DoEMatrices.floatRuns.getOrNull(value.toInt()) ?: DoEMatrices.floatRuns[0]
                        Modifier.cyberFloat(height = run.height, durationMillis = run.duration) // Float doesn't explicitly expose easing in signature, using duration
                    }
                    testName.contains("doe_blur") -> {
                        val run = DoEMatrices.blurRuns.getOrNull(value.toInt()) ?: DoEMatrices.blurRuns[0]
                        Modifier.cyberBackdropBlur(radius = run.radius, tint = Color(0x1AFFFFFF).copy(alpha = run.opacity))
                    }
                    testName.contains("doe_stripes") -> {
                        val run = DoEMatrices.stripesRuns.getOrNull(value.toInt()) ?: DoEMatrices.stripesRuns[0]
                        Modifier.cyberStripes(speed = run.speed, stripeWidth = run.size)
                    }
                    testName.contains("doe_datastream") -> {
                        val run = DoEMatrices.datastreamRuns.getOrNull(value.toInt()) ?: DoEMatrices.datastreamRuns[0]
                        Modifier.cyberDatastream(color = CyberPrimitives.Colors.Cyan500, maxAlpha = run.maxAlpha) // Assuming animationSpec mapping if we add it
                    }
                    else -> Modifier
                }
                
                                  val baseIconSize = iconSize
                  val actualIconSize = when {
                      testName.contains("doe_bounce") -> DoEMatrices.bounceRuns.getOrNull(value.toInt())?.size ?: baseIconSize
                      testName.contains("doe_float") -> DoEMatrices.floatRuns.getOrNull(value.toInt())?.size ?: baseIconSize
                      else -> baseIconSize
                  }
                  val isMacroEffect = testName.contains("overload") || testName.contains("scanline") || testName.contains("stripe") || testName.contains("noise") || testName.contains("datastream")
                
                if (isMacroEffect) {
                    Box(
                        modifier = Modifier
                            .size(iconSize * 2.5f)
                            .padding(4.dp)
                            .border(1.dp, CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f)) // Outer border
                            .then(effectModifier) // Shader
                            .background(Color(0xFF0F172A)), // Inner background
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SYSTEM", color = CyberPrimitives.Colors.Cyan500, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("ONLINE", color = CyberPrimitives.Colors.Green500, fontSize = 10.sp)
                        }
                    }
                } else if (testName.contains("backdrop_blur")) {
                    Box(
                        modifier = Modifier
                            .size(iconSize * 2f)
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "SYSTEM OFFLINE\nREBOOTING...\nDATASTREAM\nWARNING", 
                            color = CyberPrimitives.Colors.Green500, 
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .size(iconSize * 1.5f)
                                .cyberBackdropBlur(
                                    radius = if (testName.contains("radius")) value.dp else 16.dp, 
                                    tint = Color.Black.copy(alpha = if (testName.contains("opacity")) value else 0.5f)
                                )
                                .border(1.dp, CyberPrimitives.Colors.Cyan500.copy(alpha = 0.5f))
                        )
                    }
                } else if (testName.contains("glowborder")) {
                    Box(
                        modifier = Modifier
                            .size(iconSize * 1.5f)
                            .background(CyberPrimitives.Colors.Cyan500.copy(alpha = 0.1f), shape = CutCornerShape(8.dp))
                            .then(effectModifier)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(iconSize * 1.5f)
                            .background(CyberPrimitives.Colors.Cyan500.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = CyberPrimitives.Colors.Cyan500,
                            modifier = Modifier.size(actualIconSize).then(effectModifier)
                        )
                    }
                }
            }
        }
        
        CyberButton(
            onClick = {
                resetFeedbackFiles(context)
                resetKey++
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("FINISH ROUND & RESET")
        }
    }
}

