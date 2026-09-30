package com.example.cyberpunkandroid

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.effects.CyberBarbedWire
import com.example.cyberpunkandroid.effects.CyberBarcode
import com.example.cyberpunkandroid.effects.CyberBracketLock
import com.example.cyberpunkandroid.effects.CyberBraid
import com.example.cyberpunkandroid.effects.CyberChain
import com.example.cyberpunkandroid.effects.CyberChargeMeter
import com.example.cyberpunkandroid.effects.CyberCircuitTrace
import com.example.cyberpunkandroid.effects.CyberCometTracer
import com.example.cyberpunkandroid.effects.CyberCornerBrackets
import com.example.cyberpunkandroid.effects.CyberCornerCharge
import com.example.cyberpunkandroid.effects.CyberDrawOn
import com.example.cyberpunkandroid.effects.CyberGraduatedTicks
import com.example.cyberpunkandroid.effects.CyberHazardBand
import com.example.cyberpunkandroid.effects.CyberLiveWire
import com.example.cyberpunkandroid.effects.CyberPacketStream
import com.example.cyberpunkandroid.effects.CyberPathEffect
import com.example.cyberpunkandroid.effects.CyberScanner
import com.example.cyberpunkandroid.effects.CyberSequencedLights
import com.example.cyberpunkandroid.effects.CyberWeld
import com.example.cyberpunkandroid.effects.colorAtKelvin
import com.example.cyberpunkandroid.effects.cyberWeld
import com.example.cyberpunkandroid.config.CyberPathDefaults
import com.example.cyberpunkandroid.effects.cyberPathBorder
import com.example.cyberpunkandroid.effects.cyberPathDivider
import com.example.cyberpunkandroid.utils.CyberPathGeometry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(AndroidJUnit4::class)
class CyberPathEffectsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val all: List<CyberPathEffect> = listOf(
        CyberCornerBrackets(), CyberGraduatedTicks(), CyberBarcode(), CyberHazardBand(), CyberBraid(),
        CyberBarbedWire(), CyberCircuitTrace(), CyberChain(),
        CyberCometTracer(), CyberCometTracer(count = 3, tail = 0.12f), CyberCometTracer(stutter = 16, tail = 0.05f),
        CyberCornerCharge(), CyberDrawOn(), CyberScanner(), CyberLiveWire(), CyberPacketStream(),
        CyberChargeMeter(), CyberSequencedLights(), CyberBracketLock(), CyberWeld(),
    )

    private fun renderAll(): Boolean {
        var completed = false
        composeTestRule.setContent {
            Column {
                for (effect in all) {
                    Box(Modifier.size(160.dp, 96.dp).cyberPathBorder(effect, shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp)))
                    Box(Modifier.size(72.dp).cyberPathBorder(effect, shape = CircleShape))
                    Box(Modifier.fillMaxWidth().height(16.dp).cyberPathDivider(effect))
                }
                Box(Modifier.size(160.dp, 96.dp).cyberWeld(shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp)))
                completed = true
            }
        }
        composeTestRule.waitForIdle()
        return completed
    }

    @Test
    @Config(sdk = [33])
    fun `every path effect renders on API 33 with blurred glow`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 33)
        assertTrue("All path effects should compose and draw without crashing", renderAll())
    }

    @Test
    @Config(sdk = [30])
    fun `every path effect renders on API 30 with stroke glow fallback`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 30)
        assertTrue("All path effects should compose and draw without crashing on the fallback path", renderAll())
    }

    @Test
    fun `fit divides the length exactly and never returns zero stamps`() {
        assertEquals(100f / 14f, CyberPathGeometry.fit(100f, 7f), 1e-4f)
        assertEquals(10f, CyberPathGeometry.fit(10f, 30f), 1e-6f) // shorter than one period -> one stamp
    }

    @Test
    fun `barcode pattern is stable for a seed`() {
        // Same sequence as the Skia reference prototype that the visuals were tuned on.
        val expected = intArrayOf(1, 2, 3, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 4, 1, 1, 2, 1, 1, 1, 3, 1, 3, 1, 2, 3, 3)
        assertArrayEquals(expected, CyberBarcode().units())
    }

    @Test
    fun `frame hash is deterministic and in range`() {
        assertEquals(0.5582226f, CyberPathGeometry.hash01(3.0), 1e-5f)
        for (i in 0 until 500) {
            val h = CyberPathGeometry.hash01(i.toDouble())
            assertTrue(h >= 0f && h < 1f)
        }
    }

    @Test
    fun `blackbody scale returns table colors at table temperatures and clamps outside`() {
        val scale = CyberPathDefaults.Weld.Blackbody
        assertEquals(scale.first().second, colorAtKelvin(scale, 500f))   // below the table: 1000 K red
        assertEquals(scale.last().second, colorAtKelvin(scale, 6000f))   // above the table: 3000 K
        val atStop = colorAtKelvin(scale, 2000f)                          // exact stop (via Oklab lerp at t = 1)
        assertEquals(scale[3].second.red, atStop.red, 1e-3f)
        assertEquals(scale[3].second.green, atStop.green, 1e-3f)
        assertEquals(scale[3].second.blue, atStop.blue, 1e-3f)
    }
}
