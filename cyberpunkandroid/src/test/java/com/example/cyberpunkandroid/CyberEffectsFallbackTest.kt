package com.example.cyberpunkandroid

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cyberpunkandroid.effects.cyberOverload
import com.example.cyberpunkandroid.effects.cyberScanlines
import com.example.cyberpunkandroid.effects.cyberNeonBorderFlow
import com.example.cyberpunkandroid.effects.cyberNoise
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(AndroidJUnit4::class)
class CyberEffectsFallbackTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Config(sdk = [32])
    fun `cyberOverload on API less than 33 does not crash and renders fallback`() {
        // Force SDK INT for test if Robolectric config didn't catch it
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 32)
        
        var completed = false
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .cyberOverload(enabled = true),
            ) {
                completed = true
            }
        }

        composeTestRule.waitForIdle()
        assertTrue("Compose should complete rendering without crashing on API 32 fallback", completed)
    }

    @Test
    @Config(sdk = [33])
    fun `cyberScanlines on API 33+ renders without crashing using AGSL`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 33)
        
        var completed = false
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .cyberScanlines()
            ) {
                completed = true
            }
        }

        composeTestRule.waitForIdle()
        assertTrue("Compose should complete rendering without crashing on API 33 with AGSL", completed)
    }

    @Test
    @Config(sdk = [32])
    fun `cyberNoise on API less than 33 renders without crashing using non-shader fallback`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 32)

        var completed = false
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .cyberNoise(enabled = true, animated = false)
            ) {
                completed = true
            }
        }

        composeTestRule.waitForIdle()
        assertTrue("Compose should complete rendering without crashing on API 32 fallback", completed)
    }

    @Test
    @Config(sdk = [33])
    fun `cyberNoise on API 33+ renders without crashing using AGSL`() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 33)

        var completed = false
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .cyberNoise(enabled = true, animated = false)
            ) {
                completed = true
            }
        }

        composeTestRule.waitForIdle()
        assertTrue("Compose should complete rendering without crashing on API 33 with AGSL", completed)
    }

    @Test
    fun `cyberNeonBorderFlow renders without crashing`() {
        var completed = false
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .cyberNeonBorderFlow(enabled = true)
            ) {
                completed = true
            }
        }

        composeTestRule.waitForIdle()
        assertTrue("Compose should complete rendering cyberNeonBorderFlow without crashing", completed)
    }
}
