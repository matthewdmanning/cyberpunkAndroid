package com.example.cyberpunkandroid

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.cyberpunkandroid.components.CyberAlertVariant
import com.example.cyberpunkandroid.components.CyberBanner
import com.example.cyberpunkandroid.components.CyberBreadcrumbs
import com.example.cyberpunkandroid.components.CyberCountBadge
import com.example.cyberpunkandroid.components.CyberDataEntry
import com.example.cyberpunkandroid.components.CyberDataList
import com.example.cyberpunkandroid.components.CyberDeltaSentiment
import com.example.cyberpunkandroid.components.CyberNavRail
import com.example.cyberpunkandroid.components.CyberNavRailItem
import com.example.cyberpunkandroid.components.CyberReadout
import com.example.cyberpunkandroid.components.CyberStepper
import com.example.cyberpunkandroid.components.CyberTimeoutBar
import com.example.cyberpunkandroid.components.CyberTrend
import com.example.cyberpunkandroid.effects.rememberCyberCountdown
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Renders each navigation, feedback, and information component on a standard phone screen (411 × 891 dp at
 * 420 dpi, a Pixel-class size) and writes PNG files to `build/screenshots/nav-feedback-info/`.
 *
 * Components with an effect get a second `_effect` frame captured while the effect is visible.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-port-420dpi")
class CyberNavFeedbackInfoScreenshots {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Output folder, relative to the module directory that Gradle uses as the test working directory. */
    private val outDir = File("build/screenshots/nav-feedback-info").apply { mkdirs() }

    /**
     * Writes the current root frame to a PNG file.
     *
     * @param name File name without extension.
     */
    private fun capture(name: String) {
        composeTestRule.waitForIdle()
        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        File(outDir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    /**
     * Screen frame: theme background, full screen, with the content placed by [alignment].
     *
     * @param alignment Content position on the screen.
     * @param padded `true` to add the standard 16 dp screen gutter.
     * @param content Component under test.
     */
    @Composable
    private fun Screen(
        alignment: Alignment = Alignment.Center,
        padded: Boolean = true,
        content: @Composable BoxScope.() -> Unit,
    ) {
        CyberTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberTheme.colors.background)
                    .then(if (padded) Modifier.padding(CyberTheme.spacing.dp16) else Modifier),
                contentAlignment = alignment,
                content = content
            )
        }
    }

    /** Advances the paused test clock by [millis]. */
    private fun advance(millis: Long) = composeTestRule.mainClock.advanceTimeBy(millis)

    @Test
    fun breadcrumbs() {
        composeTestRule.setContent {
            Screen(Alignment.TopStart) {
                CyberBreadcrumbs(
                    segments = listOf("Root", "Grid", "Sector", "Block", "Node"),
                    onSegmentClick = {}
                )
            }
        }
        capture("01_breadcrumbs")
    }

    @Test
    fun navRail() {
        var selected by mutableIntStateOf(0)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            Screen(Alignment.TopStart, padded = false) {
                CyberNavRail(
                    items = listOf(
                        CyberNavRailItem("Map", CyberIcons.ChartLine),
                        CyberNavRailItem("Comms", CyberIcons.Bell, badgeCount = 3),
                        CyberNavRailItem("Files", CyberIcons.File),
                        CyberNavRailItem("Logs", CyberIcons.Terminal),
                    ),
                    selectedIndex = selected,
                    onItemSelected = { selected = it }
                )
            }
        }
        advance(1_000)
        capture("02_nav_rail")
        selected = 2
        advance(64) // Mid-slide: the indicator is between slots.
        capture("02_nav_rail_effect")
    }

    @Test
    fun stepper() {
        var step by mutableIntStateOf(1)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            Screen {
                CyberStepper(steps = listOf("Scan", "Verify", "Upload", "Done"), currentStep = step)
            }
        }
        advance(1_000)
        capture("03_stepper")
        step = 2
        advance(150) // Mid-fill: the connector is halfway to the next node.
        capture("03_stepper_effect")
    }

    @Test
    fun banner() {
        var message by mutableStateOf("Uplink lost")
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            Screen(Alignment.TopCenter, padded = false) {
                Column(verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)) {
                    CyberBanner(
                        message = message,
                        visible = true,
                        variant = CyberAlertVariant.Warning,
                        onDismiss = {},
                        actions = { TextButton(onClick = {}) { Text("RETRY") } }
                    )
                    CyberBanner(message = "Synced", visible = true, variant = CyberAlertVariant.Success)
                    CyberBanner(message = "Update ready", visible = true, variant = CyberAlertVariant.Info)
                    CyberBanner(message = "Breach", visible = true, variant = CyberAlertVariant.Error, onDismiss = {})
                }
            }
        }
        advance(1_000)
        capture("04_banner")
        message = "Uplink weak"
        advance(64) // Near the flash peak.
        capture("04_banner_effect")
    }

    @Test
    fun timeoutBar() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            Screen {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp24)
                ) {
                    val full = rememberCyberCountdown(durationMillis = 10_000)
                    val low = rememberCyberCountdown(durationMillis = 1_250)
                    CyberTimeoutBar(remainingFraction = { full.value })
                    CyberTimeoutBar(remainingFraction = { low.value })
                }
            }
        }
        advance(1_000) // First bar at 90 %, second at 20 % (warning color).
        capture("05_timeout_bar")
    }

    @Test
    fun readout() {
        var value by mutableStateOf("42")
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            Screen {
                Row(horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16)) {
                    CyberReadout(
                        label = "Ping",
                        value = value,
                        unit = "ms",
                        delta = "+3",
                        trend = CyberTrend.Up,
                        deltaSentiment = CyberDeltaSentiment.Negative
                    )
                    CyberReadout(
                        label = "Rate",
                        value = "9.6",
                        unit = "Gb/s",
                        delta = "+1.2",
                        trend = CyberTrend.Up,
                        deltaSentiment = CyberDeltaSentiment.Positive
                    )
                }
            }
        }
        advance(1_000)
        capture("06_readout")
        value = "45"
        advance(64) // Near the flash peak.
        capture("06_readout_effect")
    }

    @Test
    fun dataList() {
        composeTestRule.setContent {
            Screen {
                CyberDataList(
                    modifier = Modifier.fillMaxWidth(),
                    entries = listOf(
                        CyberDataEntry("Core", "v9.2"),
                        CyberDataEntry("Temp", "41 °C"),
                        CyberDataEntry("Link", "OK", valueColor = CyberTheme.status.success.color),
                        CyberDataEntry("Ice", "Low", valueColor = CyberTheme.status.warning.color),
                    )
                )
            }
        }
        capture("07_data_list")
    }

    @Test
    fun countBadge() {
        var count by mutableIntStateOf(3)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            Screen {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp24),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CyberCountBadge(count = count)
                    CyberCountBadge(count = 42)
                    CyberCountBadge(count = 128)
                }
            }
        }
        advance(1_000)
        capture("08_count_badge")
        count = 4
        advance(64) // Near the bump peak.
        capture("08_count_badge_effect")
    }
}
