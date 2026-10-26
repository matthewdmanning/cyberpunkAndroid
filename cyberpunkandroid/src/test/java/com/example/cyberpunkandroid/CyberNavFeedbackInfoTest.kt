package com.example.cyberpunkandroid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import com.example.cyberpunkandroid.components.CyberStepState
import com.example.cyberpunkandroid.components.CyberStepper
import com.example.cyberpunkandroid.components.CyberTimeoutBar
import com.example.cyberpunkandroid.components.CyberTrend
import com.example.cyberpunkandroid.components.breadcrumbSlots
import com.example.cyberpunkandroid.components.connectorFill
import com.example.cyberpunkandroid.components.countBadgeText
import com.example.cyberpunkandroid.components.stepState
import com.example.cyberpunkandroid.effects.rememberCyberChangePulse
import com.example.cyberpunkandroid.effects.rememberCyberCountdown
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class CyberNavFeedbackInfoTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ---- Pure logic -------------------------------------------------------------------------

    @Test
    fun `breadcrumb slots keep root and tail when collapsed`() {
        assertEquals(listOf(0, 1, 2), breadcrumbSlots(count = 3, maxVisible = 4, expanded = false))
        assertEquals(listOf(0, null, 4, 5, 6), breadcrumbSlots(count = 7, maxVisible = 4, expanded = false))
        assertEquals((0 until 7).toList(), breadcrumbSlots(count = 7, maxVisible = 4, expanded = true))
    }

    @Test
    fun `stepper state and connector fill follow the current step`() {
        assertEquals(CyberStepState.Completed, stepState(index = 0, currentStep = 1))
        assertEquals(CyberStepState.Current, stepState(index = 1, currentStep = 1))
        assertEquals(CyberStepState.Upcoming, stepState(index = 2, currentStep = 1))
        assertEquals(1f, connectorFill(segment = 0, progress = 1.5f))
        assertEquals(0.5f, connectorFill(segment = 1, progress = 1.5f))
        assertEquals(0f, connectorFill(segment = 2, progress = 1.5f))
    }

    @Test
    fun `count badge text caps at the maximum`() {
        assertEquals("7", countBadgeText(7, 99))
        assertEquals("99+", countBadgeText(120, 99))
    }

    // ---- Navigation -------------------------------------------------------------------------

    @Test
    fun `breadcrumbs navigate to ancestors and expand the collapsed middle`() {
        var clicked = -1
        composeTestRule.setContent {
            CyberTheme {
                CyberBreadcrumbs(
                    segments = listOf("Root", "Grid", "Sector", "Block", "Node"),
                    maxVisibleSegments = 3,
                    onSegmentClick = { clicked = it }
                )
            }
        }
        composeTestRule.onNodeWithText("GRID").assertDoesNotExist()
        composeTestRule.onNodeWithText("ROOT").performClick()
        assertEquals(0, clicked)

        val current = composeTestRule.onNodeWithText("NODE").fetchSemanticsNode()
        assertEquals("Current location", current.config[SemanticsProperties.StateDescription])

        composeTestRule.onNodeWithText("…").performClick()
        composeTestRule.onNodeWithText("GRID").assertIsDisplayed()
    }

    @Test
    fun `nav rail selects destinations and shows badges`() {
        var selected by mutableIntStateOf(0)
        composeTestRule.setContent {
            CyberTheme {
                CyberNavRail(
                    items = listOf(
                        CyberNavRailItem("Map", CyberIcons.ChartLine),
                        CyberNavRailItem("Comms", CyberIcons.Bell, badgeCount = 3),
                    ),
                    selectedIndex = selected,
                    onItemSelected = { selected = it }
                )
            }
        }
        composeTestRule.onNodeWithText("MAP").assertIsSelected()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
        composeTestRule.onNodeWithText("COMMS").performClick()
        assertEquals(1, selected)
    }

    @Test
    fun `stepper reports step states and only completed steps are clickable`() {
        var clicked = -1
        composeTestRule.setContent {
            CyberTheme {
                CyberStepper(
                    steps = listOf("Scan", "Verify", "Upload"),
                    currentStep = 1,
                    onStepClick = { clicked = it }
                )
            }
        }
        val scan = composeTestRule.onNodeWithText("SCAN", useUnmergedTree = true)
        composeTestRule.onNodeWithText("VERIFY").fetchSemanticsNode().let {
            assertEquals("Current step", it.config[SemanticsProperties.StateDescription])
        }
        scan.assertIsDisplayed()
        composeTestRule.onNodeWithText("SCAN").performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(0, clicked)
        assertTrue(SemanticsActions.OnClick !in composeTestRule.onNodeWithText("UPLOAD").fetchSemanticsNode().config)
    }

    // ---- Feedback ---------------------------------------------------------------------------

    @Test
    fun `banner is a live region with a dismiss action`() {
        var visible by mutableStateOf(true)
        composeTestRule.setContent {
            CyberTheme {
                CyberBanner(
                    message = "Uplink degraded",
                    visible = visible,
                    title = "Network",
                    variant = CyberAlertVariant.Warning,
                    onDismiss = { visible = false }
                )
            }
        }
        composeTestRule.onNodeWithText("Uplink degraded").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Dismiss").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Uplink degraded").assertDoesNotExist()
    }

    @Test
    fun `countdown reaches zero and fires timeout once`() {
        var timeouts = 0
        var fraction = 1f
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            CyberTheme {
                val remaining = rememberCyberCountdown(durationMillis = 1_000, onTimeout = { timeouts++ })
                fraction = remaining.value
                CyberTimeoutBar(remainingFraction = { remaining.value })
            }
        }
        composeTestRule.mainClock.advanceTimeBy(500)
        assertTrue(fraction in 0.3f..0.7f)
        composeTestRule.mainClock.advanceTimeBy(1_000)
        assertEquals(0f, fraction)
        assertEquals(1, timeouts)
    }

    @Test
    fun `countdown holds its value while paused`() {
        var running by mutableStateOf(true)
        var fraction = 1f
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            val remaining = rememberCyberCountdown(durationMillis = 1_000, running = running)
            fraction = remaining.value
        }
        composeTestRule.mainClock.advanceTimeBy(400)
        running = false
        composeTestRule.mainClock.advanceTimeBy(100)
        val held = fraction
        composeTestRule.mainClock.advanceTimeBy(2_000)
        assertEquals(held, fraction)
        assertTrue(held > 0f)
    }

    // ---- Information ------------------------------------------------------------------------

    @Test
    fun `change pulse fires on change but not on first value`() {
        var key by mutableIntStateOf(1)
        var peak = 0f
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            val pulse = rememberCyberChangePulse(key = key, shouldPulse = { old, new -> new > old })
            peak = maxOf(peak, pulse.value)
        }
        composeTestRule.mainClock.advanceTimeBy(100)
        assertEquals(0f, peak)
        key = 0 // A decrease is rejected by shouldPulse.
        composeTestRule.mainClock.advanceTimeBy(100)
        assertEquals(0f, peak)
        key = 5
        composeTestRule.mainClock.advanceTimeBy(32)
        assertTrue(peak > 0f)
    }

    @Test
    fun `readout, data list and count badge render their content`() {
        composeTestRule.setContent {
            CyberTheme {
                CyberReadout(
                    label = "Latency",
                    value = "42",
                    unit = "ms",
                    delta = "+3",
                    trend = CyberTrend.Up,
                    deltaSentiment = CyberDeltaSentiment.Negative
                )
                CyberDataList(entries = listOf(CyberDataEntry("Firmware", "v9.2.1")))
                CyberCountBadge(count = 0)
            }
        }
        composeTestRule.onNodeWithText("42", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("v9.2.1", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }
}
