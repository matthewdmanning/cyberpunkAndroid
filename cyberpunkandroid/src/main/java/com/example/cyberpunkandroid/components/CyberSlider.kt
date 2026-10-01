package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.effects.cyberDatastream
import com.example.cyberpunkandroid.effects.cyberGlowBorder
import com.example.cyberpunkandroid.effects.cyberSemantics
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.roundToInt

/**
 * Controlled horizontal sci-fi range control with a bounded data-stream fill and holographic thumb.
 *
 * @param value Current value supplied by the caller.
 * @param valueRange Inclusive range accepted by the control.
 * @param modifier Modifier applied to the control.
 * @param enabled Whether dragging and semantic updates are enabled.
 * @param tickCount Number of terminal-style tick marks rendered below the track.
 * @param trackHeight Height of the neon track.
 * @param thumbSize Diameter of the glowing thumb.
 * @param animationSpec Animation used for the thumb and fill.
 * @param color Active fill and thumb color.
 * @param trackColor Inactive track color.
 * @param tickColor Tick mark color.
 * @param appendedA11y Optional text appended to the default accessibility name.
 * @param customA11y Optional custom accessibility name.
 * @param onValueChange Called with each clamped drag value.
 */
@Composable
fun CyberSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tickCount: Int = 0,
    trackHeight: Dp = 6.dp,
    thumbSize: Dp = 20.dp,
    animationSpec: AnimationSpec<Float> = spring(),
    color: Color = CyberTheme.colors.primary,
    trackColor: Color = CyberTheme.colors.surfaceSecondary,
    tickColor: Color = CyberTheme.colors.secondary,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onValueChange: (Float) -> Unit,
) {
    val lowerBound = minOf(valueRange.start, valueRange.endInclusive)
    val upperBound = maxOf(valueRange.start, valueRange.endInclusive)
    val boundedValue = value.coerceIn(lowerBound, upperBound)
    val fraction = if (upperBound == lowerBound) 0f else {
        (boundedValue - lowerBound) / (upperBound - lowerBound)
    }
    val animatedFraction by animateFloatAsState(fraction, animationSpec, label = "CyberSliderFraction")
    val density = LocalDensity.current
    val thumbWidthPx = with(density) { thumbSize.toPx() }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }
    var dragValue by remember { mutableFloatStateOf(boundedValue) }
    androidx.compose.runtime.LaunchedEffect(boundedValue) {
        dragValue = boundedValue
    }
    val dragState = rememberDraggableState { delta ->
        val deltaValue = if (trackWidthPx == 0f || upperBound == lowerBound) 0f else {
            delta / trackWidthPx * (upperBound - lowerBound)
        }
        dragValue = (dragValue + deltaValue).coerceIn(lowerBound, upperBound)
        onValueChange(dragValue)
    }
    BoxWithConstraints(
        modifier = modifier
            .cyberSemantics("CyberSlider", appendedA11y, customA11y)
            .semantics {
                progressBarRangeInfo = androidx.compose.ui.semantics.ProgressBarRangeInfo(
                    current = boundedValue,
                    range = lowerBound..upperBound,
                )
                if (enabled) {
                    setProgress { target ->
                        onValueChange(target.coerceIn(lowerBound, upperBound))
                        true
                    }
                }
            }
            .focusable(enabled)
            .onGloballyPositioned { coordinates ->
                trackWidthPx = (coordinates.size.width - thumbWidthPx).coerceAtLeast(1f)
            }
            .draggable(state = dragState, orientation = Orientation.Horizontal, enabled = enabled)
            .height(thumbSize + 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackShape = RoundedCornerShape(trackHeight / 2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = thumbSize / 2)
                .height(trackHeight)
                .clip(trackShape)
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(trackShape)
                    .background(color)
                    .cyberDatastream(color = color, maxAlpha = 0.7f),
            )
        }

        if (tickCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = thumbSize / 2)
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.Bottom,
            ) {
                repeat(tickCount) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 1.dp)
                            .height(4.dp)
                            .background(tickColor),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset((animatedFraction * trackWidthPx).roundToInt(), 0)
                }
                .size(thumbSize)
                .clip(CircleShape)
                .background(color)
                .cyberGlowBorder(color = color, shape = CircleShape, glowRadius = 6.dp, width = 2.dp),
        )
    }
}
