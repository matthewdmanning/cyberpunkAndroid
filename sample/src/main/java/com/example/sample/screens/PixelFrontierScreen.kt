package com.example.sample.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberPixelFrontierTransition
import com.example.cyberpunkandroid.components.CyberSlider
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlinx.coroutines.delay
import java.util.Locale

/** Number of test screens the demo cycles through. */
private const val PageCount = 3

/** Pause in milliseconds between the end of one sweep and the start of the next in auto mode. */
private const val AutoCyclePauseMs = 600L

// Slider ranges for the tuning controls. They bracket the library defaults so a device test can go both ways.
private val BlockSizeRangeDp = 8f..64f
private val BandBlocksRange = 1f..12f
private val EdgeThresholdRange = 0.02f..0.5f
private val SplitRangeDp = 0f..16f
private val JitterRangeDp = 0f..96f
private val DurationRangeMs = 150f..1500f

/**
 * PROTOTYPE test screen for [CyberPixelFrontierTransition].
 *
 * Use it on a physical device (for example a Pixel 7) to judge the look and to profile frame times.
 * The top area shows the transition. The buttons switch screens, start an automatic loop for
 * profiling, change the sweep axis, and hide the tuning sliders so the transition fills most of the screen.
 * The sliders change the transition settings live.
 */
@Composable
fun PixelFrontierScreen() {
    var page by remember { mutableIntStateOf(0) }
    var autoCycle by remember { mutableStateOf(false) }
    var vertical by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var blockDp by remember { mutableFloatStateOf(CyberPrimitives.Spacing.dp32.value) }
    var bandBlocks by remember { mutableFloatStateOf(CyberConfig.Shaders.PixelFrontierBandBlocks) }
    var edgeThreshold by remember { mutableFloatStateOf(CyberConfig.Shaders.PixelFrontierEdgeThreshold) }
    var splitDp by remember { mutableFloatStateOf(CyberPrimitives.Spacing.dp4.value) }
    var jitterDp by remember { mutableFloatStateOf(CyberPrimitives.Spacing.dp32.value) }
    var durationMs by remember { mutableFloatStateOf(CyberPrimitives.Durations.ms300.toFloat()) }

    // Auto mode: switch screens in a loop. The loop restarts when the duration changes.
    LaunchedEffect(autoCycle, durationMs) {
        while (autoCycle) {
            delay(durationMs.toLong() + AutoCyclePauseMs)
            page = (page + 1) % PageCount
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CyberPixelFrontierTransition(
                targetState = page,
                orientation = if (vertical) Orientation.Vertical else Orientation.Horizontal,
                blockSize = blockDp.dp,
                bandBlocks = bandBlocks,
                edgeThreshold = edgeThreshold,
                splitOffset = splitDp.dp,
                frontierJitter = jitterDp.dp,
                animationSpec = tween(durationMs.toInt()),
            ) { shownPage ->
                FrontierPage(index = shownPage)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(CyberPrimitives.Spacing.dp8),
            horizontalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp8),
        ) {
            CyberButton(onClick = { page = (page + 1) % PageCount }) { Text("NEXT") }
            CyberButton(onClick = { autoCycle = !autoCycle }) { Text(if (autoCycle) "AUTO: ON" else "AUTO: OFF") }
            CyberButton(onClick = { vertical = !vertical }) { Text(if (vertical) "AXIS: V" else "AXIS: H") }
            CyberButton(onClick = { showControls = !showControls }) { Text(if (showControls) "HIDE" else "TUNE") }
        }

        if (showControls) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = CyberPrimitives.Spacing.dp16),
            ) {
                TuningRow("BLOCK (dp)", blockDp, BlockSizeRangeDp) { blockDp = it }
                TuningRow("BAND (blocks)", bandBlocks, BandBlocksRange) { bandBlocks = it }
                TuningRow("EDGE THRESHOLD", edgeThreshold, EdgeThresholdRange) { edgeThreshold = it }
                TuningRow("SPLIT (dp)", splitDp, SplitRangeDp) { splitDp = it }
                TuningRow("RAGGEDNESS (dp)", jitterDp, JitterRangeDp) { jitterDp = it }
                TuningRow("DURATION (ms)", durationMs, DurationRangeMs) { durationMs = it }
            }
        }
    }
}

/**
 * Use this function to show one labeled slider for a tuning value.
 *
 * @param label Name of the value, shown above the slider.
 * @param value Current value.
 * @param range Smallest and largest allowed value.
 * @param onValueChange Called with the new value while the slider moves.
 */
@Composable
private fun TuningRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Text(
        text = "$label: ${String.format(Locale.ROOT, "%.2f", value)}",
        style = CyberTheme.typography.body,
        color = CyberTheme.colors.textSecondary,
    )
    CyberSlider(
        value = value,
        valueRange = range,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = onValueChange,
    )
}

/**
 * Use this function to draw one test screen. Each screen has an opaque background, large text, cards and a
 * solid color bar, so the shader has strong brightness differences to outline.
 *
 * @param index Which test screen to draw. The accent color changes with the index.
 */
@Composable
private fun FrontierPage(index: Int) {
    val accent = when (index % PageCount) {
        0 -> CyberTheme.colors.primary
        1 -> CyberTheme.colors.secondary
        else -> CyberPrimitives.Colors.Green500
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberTheme.colors.background)
            .padding(CyberPrimitives.Spacing.dp16),
        verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp16),
    ) {
        Text(text = "SCREEN ${index + 1}", style = CyberTheme.typography.display, color = accent)
        repeat(PageCount) { row ->
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PANEL ${index + 1}.${row + 1}",
                    style = CyberTheme.typography.body,
                    color = CyberTheme.colors.textPrimary,
                    modifier = Modifier.padding(CyberPrimitives.Spacing.dp16),
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(CyberPrimitives.IconSizes.dp64).background(accent))
    }
}
