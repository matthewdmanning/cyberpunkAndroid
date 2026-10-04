package com.example.cyberpunkandroid

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.effects.CyberInteractionTrigger
import com.example.cyberpunkandroid.effects.CyberParticleShower
import com.example.cyberpunkandroid.utils.CyberPulseStyle
import com.example.cyberpunkandroid.effects.CyberRadarSweep
import com.example.cyberpunkandroid.utils.CyberRadialDirection
import com.example.cyberpunkandroid.effects.CyberRadialPulse
import com.example.cyberpunkandroid.effects.CyberRadialRegion
import com.example.cyberpunkandroid.utils.CyberRadialSector
import com.example.cyberpunkandroid.utils.CyberSweepMode
import com.example.cyberpunkandroid.effects.cyberPathAlong
import com.example.cyberpunkandroid.effects.cyberPathBorder
import com.example.cyberpunkandroid.effects.cyberRadarSweep
import com.example.cyberpunkandroid.effects.cyberRadialIllumination
import com.example.cyberpunkandroid.effects.cyberRadialPulse
import com.example.cyberpunkandroid.effects.geometry
import com.example.cyberpunkandroid.effects.rememberCyberRadarSweep
import com.example.cyberpunkandroid.effects.rememberCyberRadialPulse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import kotlin.math.cos
import kotlin.math.sin

/**
 * Render and behavior tests for the radial effects. The math itself is covered on the plain JVM by
 * [CyberRadialMathTest]; these tests check that the Compose layer composes, draws and answers brightness queries.
 */
@RunWith(AndroidJUnit4::class)
class CyberRadialEffectsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Composes every sweep, pulse, illumination and shower variant; returns true when composition finished. */
    private fun renderAll(): Boolean {
        var completed = false
        composeTestRule.setContent {
            Column {
                val sweeps = listOf(
                    rememberCyberRadarSweep(),
                    rememberCyberRadarSweep(fadeSteps = 5, clockwise = false),
                    rememberCyberRadarSweep(tailDegrees = 0f),
                    rememberCyberRadarSweep(sector = CyberRadialSector(300f, 60f), mode = CyberSweepMode.BOUNCE, fadeSteps = 4),
                    rememberCyberRadarSweep(sector = CyberRadialSector(0f, 180f), mode = CyberSweepMode.HOLD, edgeFadeDegrees = 20f),
                    rememberCyberRadarSweep(trigger = CyberInteractionTrigger.NONE, hideWhenIdle = true),
                )
                for (sweep in sweeps) {
                    Box(Modifier.size(120.dp).cyberRadarSweep(sweep, region = CyberRadialRegion(clipShape = CircleShape))) {
                        Box(Modifier.size(24.dp).cyberRadialIllumination(sweep))
                    }
                }
                Box(Modifier.size(120.dp).cyberRadarSweep(sweeps[0], region = CyberRadialRegion(innerRadius = 20.dp, outerRadius = 50.dp, clipShape = null), showWedge = false))

                val pulses = listOf(
                    rememberCyberRadialPulse(style = CyberPulseStyle.RING),
                    rememberCyberRadialPulse(style = CyberPulseStyle.DISC, fadeSteps = 4),
                    rememberCyberRadialPulse(style = CyberPulseStyle.SONAR),
                    rememberCyberRadialPulse(direction = CyberRadialDirection.INWARD),
                    rememberCyberRadialPulse(style = CyberPulseStyle.SONAR, sector = CyberRadialSector(0f, 90f)),
                )
                for (pulse in pulses) {
                    Box(Modifier.size(120.dp).cyberRadialPulse(pulse, region = CyberRadialRegion(clipShape = CircleShape))) {
                        Box(Modifier.size(24.dp).cyberRadialIllumination(pulse))
                    }
                }

                Box(Modifier.size(120.dp).cyberPathBorder(CyberParticleShower(), shape = CircleShape))
                Box(Modifier.size(120.dp).cyberPathAlong(CyberParticleShower(speeds = listOf(2)), path = { size ->
                    Path().apply { addArc(Rect(Offset(size.width / 2f, size.height / 2f), size.minDimension / 2f), 150f, 240f) }
                }))
                completed = true
            }
        }
        composeTestRule.waitForIdle()
        return completed
    }

    @Test
    @Config(sdk = [33])
    fun `every radial effect renders on API 33 with blurred glow`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 33)
        assertTrue("All radial effects should compose and draw without crashing", renderAll())
    }

    @Test
    @Config(sdk = [30])
    fun `every radial effect renders on API 30 with stroke glow fallback`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 30)
        assertTrue("All radial effects should compose and draw without crashing on the fallback path", renderAll())
    }

    /** Offset of the point [radius] pixels from the sweep origin at [degrees] clockwise from 12 o'clock. */
    private fun pointAt(origin: Offset, radius: Float, degrees: Float): Offset {
        val radians = Math.toRadians(degrees.toDouble())
        return Offset(origin.x + radius * sin(radians).toFloat(), origin.y - radius * cos(radians).toFloat())
    }

    @Test
    @Config(sdk = [33])
    fun `a resting sweep is bright at its head, fading behind it, dark ahead of it`() {
        lateinit var sweep: CyberRadarSweep
        composeTestRule.setContent {
            sweep = rememberCyberRadarSweep(mode = CyberSweepMode.HOLD, tailDegrees = 90f, trigger = CyberInteractionTrigger.NONE)
            Box(Modifier.size(200.dp).cyberRadarSweep(sweep))
        }
        composeTestRule.waitForIdle()
        val placement = sweep.placement
        assertNotNull("the modifier publishes its placement after layout", placement)
        val origin = placement!!.geometry().originInRoot
        assertEquals(1f, sweep.intensityAt(pointAt(origin, 50f, 0f)), 1e-3f)     // on the head
        assertEquals(0.5f, sweep.intensityAt(pointAt(origin, 50f, 315f)), 1e-3f) // halfway down the trail (45 of 90 degrees behind)
        assertEquals(0f, sweep.intensityAt(pointAt(origin, 50f, 90f)), 1e-3f)    // not in the trail (on a full circle the trail wraps, so everything else is dark)
        assertEquals(0f, sweep.intensityAt(pointAt(origin, 50f, 200f)), 1e-3f)   // beyond the end of the trail
    }

    @Test
    @Config(sdk = [33])
    fun `a sweep only lights points inside its sector`() {
        lateinit var sweep: CyberRadarSweep
        composeTestRule.setContent {
            sweep = rememberCyberRadarSweep(sector = CyberRadialSector(0f, 90f), mode = CyberSweepMode.HOLD, trigger = CyberInteractionTrigger.NONE)
            Box(Modifier.size(200.dp).cyberRadarSweep(sweep))
        }
        composeTestRule.waitForIdle()
        val origin = sweep.placement!!.geometry().originInRoot
        assertEquals(1f, sweep.intensityAt(pointAt(origin, 50f, 0f)), 1e-3f)
        assertEquals(0f, sweep.intensityAt(pointAt(origin, 50f, 270f)), 1e-3f) // outside the sector, although inside the trail's reach
    }

    @Test
    @Config(sdk = [33])
    fun `a sweep with no trail still lights a point under its head line`() {
        lateinit var sweep: CyberRadarSweep
        composeTestRule.setContent {
            sweep = rememberCyberRadarSweep(mode = CyberSweepMode.HOLD, tailDegrees = 0f, trigger = CyberInteractionTrigger.NONE)
            Box(Modifier.size(200.dp).cyberRadarSweep(sweep, showWedge = false))
        }
        composeTestRule.waitForIdle()
        val origin = sweep.placement!!.geometry().originInRoot
        assertTrue(sweep.intensityAt(pointAt(origin, 50f, 0f)) > 0.5f)
    }

    @Test
    @Config(sdk = [33])
    fun `a hidden idle sweep lights nothing`() {
        lateinit var sweep: CyberRadarSweep
        composeTestRule.setContent {
            sweep = rememberCyberRadarSweep(mode = CyberSweepMode.HOLD, trigger = CyberInteractionTrigger.NONE, hideWhenIdle = true)
            Box(Modifier.size(200.dp).cyberRadarSweep(sweep))
        }
        composeTestRule.waitForIdle()
        val origin = sweep.placement!!.geometry().originInRoot
        assertEquals(0f, sweep.intensityAt(pointAt(origin, 50f, 0f)), 1e-3f)
    }

    @Test
    @Config(sdk = [33])
    fun `an outward ring is bright at its head and dark outside it`() {
        lateinit var pulse: CyberRadialPulse
        composeTestRule.setContent {
            pulse = rememberCyberRadialPulse(trigger = CyberInteractionTrigger.NONE)
            Box(Modifier.size(200.dp).cyberRadialPulse(pulse))
        }
        composeTestRule.waitForIdle()
        val origin = pulse.placement!!.geometry().originInRoot
        // at rest the ring head is at the inner radius (the center): the center is lit, a point further out is not
        assertEquals(1f, pulse.intensityAt(origin), 1e-3f)
        assertEquals(0f, pulse.intensityAt(pointAt(origin, 80f, 30f)), 1e-3f)
    }

    @Test
    @Config(sdk = [33])
    fun `an inward ring at rest has not reached the interior`() {
        lateinit var pulse: CyberRadialPulse
        composeTestRule.setContent {
            pulse = rememberCyberRadialPulse(direction = CyberRadialDirection.INWARD, trigger = CyberInteractionTrigger.NONE)
            Box(Modifier.size(200.dp).cyberRadialPulse(pulse, region = CyberRadialRegion(outerRadius = 90.dp)))
        }
        composeTestRule.waitForIdle()
        val g = pulse.placement!!.geometry()
        // the head starts at the outer radius, and everything inside it is ahead of the head, so it is dark
        assertEquals(0f, pulse.intensityAt(g.originInRoot), 1e-3f)
        assertEquals(0f, pulse.intensityAt(pointAt(g.originInRoot, 40f, 40f)), 1e-3f)
    }
}
