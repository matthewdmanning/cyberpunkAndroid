package com.example.sample.screens

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberConfig

object DoEMatrices {
    // 1. Bounce (Height, Size, Speed, Easing) - 2^(4-1) = 8 runs
    // Factors: Height(8, 32), Size(32, 96), Speed(500, 2000), Easing(Linear, Cyber)
    data class BounceRun(val height: Dp, val size: Dp, val duration: Int, val easing: Easing)
    val bounceRuns = listOf(
        BounceRun(8.dp, 32.dp, 500, LinearEasing),
        BounceRun(32.dp, 32.dp, 500, CyberConfig.Easings.BounceEasing),
        BounceRun(8.dp, 96.dp, 500, CyberConfig.Easings.BounceEasing),
        BounceRun(32.dp, 96.dp, 500, LinearEasing),
        BounceRun(8.dp, 32.dp, 2000, CyberConfig.Easings.BounceEasing),
        BounceRun(32.dp, 32.dp, 2000, LinearEasing),
        BounceRun(8.dp, 96.dp, 2000, LinearEasing),
        BounceRun(32.dp, 96.dp, 2000, CyberConfig.Easings.BounceEasing)
    )

    // 2. Float (Height, Size, Speed, Easing) - 2^(4-1) = 8 runs
    // Note: User says float needs a little more displacement. Height(16, 48)
    data class FloatRun(val height: Dp, val size: Dp, val duration: Int, val easing: Easing)
    val floatRuns = listOf(
        FloatRun(16.dp, 32.dp, 1000, LinearEasing),
        FloatRun(48.dp, 32.dp, 1000, CyberConfig.Easings.FloatEasing),
        FloatRun(16.dp, 96.dp, 1000, CyberConfig.Easings.FloatEasing),
        FloatRun(48.dp, 96.dp, 1000, LinearEasing),
        FloatRun(16.dp, 32.dp, 3000, CyberConfig.Easings.FloatEasing),
        FloatRun(48.dp, 32.dp, 3000, LinearEasing),
        FloatRun(16.dp, 96.dp, 3000, LinearEasing),
        FloatRun(48.dp, 96.dp, 3000, CyberConfig.Easings.FloatEasing)
    )

    // 3. Blur (Opacity, Radius) - 3^2 = 9 runs
    // Opacity(0.1, 0.4, 0.8), Radius(8, 16, 32)
    data class BlurRun(val opacity: Float, val radius: Dp)
    val blurRuns = listOf(
        BlurRun(0.1f, 8.dp), BlurRun(0.4f, 8.dp), BlurRun(0.8f, 8.dp),
        BlurRun(0.1f, 16.dp), BlurRun(0.4f, 16.dp), BlurRun(0.8f, 16.dp),
        BlurRun(0.1f, 32.dp), BlurRun(0.4f, 32.dp), BlurRun(0.8f, 32.dp)
    )

    // 7. Datastream (MaxAlpha, Easing) - 2^2 = 4 runs
    // MaxAlpha(0.2, 0.8), Easing(Linear, Cyber)
    data class DatastreamRun(val maxAlpha: Float, val easing: Easing)
    val datastreamRuns = listOf(
        DatastreamRun(0.7f, LinearEasing), DatastreamRun(0.7f, LinearOutSlowInEasing),
        DatastreamRun(0.7f, FastOutSlowInEasing), DatastreamRun(0.7f, FastOutLinearInEasing)
    )

    data class StripesRun(val speed: Float, val size: Dp)
    val stripesRuns = listOf(
        StripesRun(0.5f, 5.dp),
        StripesRun(1.0f, 10.dp)
    )
}
