package com.example.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cyberpunkandroid.components.CyberTabs
import com.example.cyberpunkandroid.components.CyberPixelTransition
import com.example.cyberpunkandroid.components.CyberTerminalBackground
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.sample.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CyberTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route ?: "sandbox"

                    CyberPixelTransition(targetKey = currentRoute) {
                        if (currentRoute == "terminal") {
                            CyberTerminalBackground(text = TerminalBackgroundGlyphs)
                        }
                    Column(modifier = Modifier.safeDrawingPadding().fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            NavHost(navController = navController, startDestination = "sandbox") {
                                composable("sandbox") { SandboxScreen(onBack = {}) }
                                composable("showcase") { EffectsShowcaseScreen() }
                                composable("effects") { EffectsScreen() }
                                composable("command_center") { 
                                    CommandCenterScreen(onNavigateToSandbox = {
                                        navController.navigate("sandbox") {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }) 
                                }
                                composable("feedback") { com.example.sample.screens.FeedbackFormScreen() }
                                composable("glow_test") { com.example.sample.screens.GlowTestScreen() }
                                composable("terminal") { /* Background is drawn beneath the navigation layer. */ }
                                composable("paths") { com.example.sample.screens.PathEffectsScreen() }
                                composable("radial") { com.example.sample.screens.RadialEffectsScreen() }
                                composable("frontier") { com.example.sample.screens.PixelFrontierScreen() }
                            }
                        }

                        CyberTabs(
                            tabs = listOf("Sandbox", "Showcase", "Effects", "Command", "Glow", "Terminal", "Paths", "Radial", "Frontier"),
                            selectedTabIndex = when(currentRoute) {
                                "sandbox" -> 0
                                "showcase" -> 1
                                "effects" -> 2
                                "command_center" -> 3
                                "glow_test" -> 4
                                "terminal" -> 5
                                "paths" -> 6
                                "radial" -> 7
                                "frontier" -> 8
                                else -> 0
                            },
                            onTabSelected = { index ->
                                val route = when(index) {
                                    0 -> "sandbox"
                                    1 -> "showcase"
                                    2 -> "effects"
                                    3 -> "command_center"
                                    4 -> "glow_test"
                                    5 -> "terminal"
                                    6 -> "paths"
                                    7 -> "radial"
                                    8 -> "frontier"
                                    else -> "sandbox"
                                }
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        )
                    }
                    }
                }
            }
        }
    }
}

// Native angular strokes form non-readable script clusters without a downloaded font.
private val TerminalBackgroundGlyphs = """
    ╲┤╱├ ┬╲│┴╱ ┤╲┬ ├╱┴│
    ┴╱┤ ╲├│╱┬ ┤┴╲├ ╱│┬
    ├╲┬│ ╱┤┴ ╲│├╱ ┬┤╲
    ╱┴├ ╲┬┤│╱ ├│╲┴ ┤╱┬
    ┬╲├│ ┴┤╱ ╲│┬├ ┤╱┴╲
    ╲┤┬ ╱├│┴╲ ┤╱├ ┬│╲┴
    ├╱┴│ ╲┬┤ ╱│├╲ ┴┤┬
    ┴╲┤├ ╱│┬ ╲├┴│ ┤╱╲┬
    ╱┬├ ╲┤│┴ ├╱┬╲ │┤┴
    ┤╲│╱ ┬├┴╲ ╱┤│ ├┬╲┴
    ╲├┬┤ ╱┴│ ├╲┤╱ ┬│┴
    ┴┤╱│ ╲├┬ ╱│┴┤ ╲┬├╱
    ├╲┴ ╱┬┤│ ╲├│┬ ┴╱┤
    ╱│┤┬ ╲┴├ ╱┬│╲ ┤├┴╱
    ┬├╲│ ┤╱┴╲ ├│┬ ╱┤┴
    ╲┴├┤ ╱│┬ ╲┤├╱ ┴│┬
    ┤╱┬│ ╲├┴ ┬│╱┤ ╲┴├
    ╱├┤╲ ┴┬│ ╲┤╱├ │┬┴╲
    ├┬╲┤ ╱│┴ ╲├╱┬ ┤│┴
    ╲│┬├ ┴┤╱╲ ┬├│ ╱┴┤
""".trimIndent()
