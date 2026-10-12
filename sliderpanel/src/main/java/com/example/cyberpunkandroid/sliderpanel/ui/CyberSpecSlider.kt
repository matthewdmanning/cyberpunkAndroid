package com.example.cyberpunkandroid.sliderpanel.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.sliderpanel.state.SliderUiState
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.roundToInt

/**
 * A labeled horizontal slider whose range, scale, step and hash marks come from its JSON spec.
 * The label and formatted value sit above a thin neon track; the filled part runs to a round
 * thumb, major and minor hash marks hang below the track, and major marks carry labels.
 *
 * Stateless: it draws [slider] and reports thumb moves through [onPositionChange] as a track
 * position from 0 to 1, by drag, tap, or accessibility action. The owner maps the position to a
 * value and snaps it. Accessibility services read the formatted value with its unit.
 *
 * @param slider What to draw.
 * @param modifier Modifier for the whole slider.
 * @param trackHeight Thickness of the track.
 * @param thumbSize Diameter of the thumb; the track is inset by half of it at each end.
 * @param markHeight Length of a major hash mark; minor marks are half as long.
 * @param color Filled track, thumb and major marks.
 * @param trackColor The track's unfilled part.
 * @param markColor Minor marks.
 * @param textColor Label, value and mark labels.
 * @param onPositionChange Called with the new track position.
 */
@Composable
fun CyberSpecSlider(
    slider: SliderUiState,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 3.dp,
    thumbSize: Dp = 18.dp,
    markHeight: Dp = 8.dp,
    color: Color = CyberTheme.colors.primary,
    trackColor: Color = CyberTheme.colors.surfaceSecondary,
    markColor: Color = CyberTheme.colors.border,
    textColor: Color = CyberTheme.colors.textSecondary,
    onPositionChange: (Float) -> Unit
) {
    val onChange by rememberUpdatedState(onPositionChange)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val labelStyle = CyberTheme.typography.terminal.copy(color = textColor)
    val markStyle = labelStyle.copy(fontSize = labelStyle.fontSize * 0.75f)

    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            BasicText(slider.label, style = labelStyle)
            Spacer(Modifier.weight(1f))
            BasicText(slider.valueText, style = labelStyle.copy(color = color))
        }
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(thumbSize + markHeight)
                .semantics {
                    contentDescription = slider.label
                    stateDescription = slider.valueText
                    progressBarRangeInfo = ProgressBarRangeInfo(slider.position, 0f..1f)
                    setProgress { target ->
                        onChange(target.coerceIn(0f, 1f))
                        true
                    }
                }
                .pointerInput(rtl, thumbSize) {
                    detectTapGestures { offset -> onChange(trackPosition(offset.x, size.width.toFloat(), thumbSize.toPx(), rtl)) }
                }
                .pointerInput(rtl, thumbSize) {
                    var raw = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            raw = trackPosition(offset.x, size.width.toFloat(), thumbSize.toPx(), rtl)
                            onChange(raw)
                        }
                    ) { change, dx ->
                        change.consume()
                        val track = (size.width - thumbSize.toPx()).coerceAtLeast(1f)
                        // Keep the unsnapped position here so small drags add up across steps.
                        raw = (raw + (if (rtl) -dx else dx) / track).coerceIn(0f, 1f)
                        onChange(raw)
                    }
                }
                .drawWithCache {
                    val inset = thumbSize.toPx() / 2f
                    val track = (size.width - 2f * inset).coerceAtLeast(0f)
                    val y = inset
                    fun x(position: Float) = inset + track * (if (rtl) 1f - position else position)
                    val trackPx = trackHeight.toPx()
                    val markTop = y + trackPx / 2f + 2.dp.toPx()
                    val major = markHeight.toPx()
                    onDrawBehind {
                        drawLine(trackColor, Offset(inset, y), Offset(inset + track, y), trackPx, StrokeCap.Round)
                        drawLine(color, Offset(x(0f), y), Offset(x(slider.position), y), trackPx, StrokeCap.Round)
                        for (m in slider.marks) {
                            val mx = x(m.position)
                            val length = if (m.major) major else major / 2f
                            drawLine(if (m.major) color else markColor, Offset(mx, markTop), Offset(mx, markTop + length), 1.dp.toPx())
                        }
                        drawCircle(color.copy(alpha = 0.25f), radius = inset, center = Offset(x(slider.position), y))
                        drawCircle(color, radius = inset * 0.6f, center = Offset(x(slider.position), y))
                    }
                }
        )
        if (slider.marks.any { it.label != null }) MarkLabels(slider, thumbSize, rtl, markStyle)
    }
}

/** Track position under x, with the thumb's half-width inset at each end. */
private fun trackPosition(x: Float, width: Float, thumb: Float, rtl: Boolean): Float {
    val track = (width - thumb).coerceAtLeast(1f)
    val t = ((x - thumb / 2f) / track).coerceIn(0f, 1f)
    return if (rtl) 1f - t else t
}

/** Major-mark labels centered under their marks, kept inside the slider's width. */
@Composable
private fun MarkLabels(slider: SliderUiState, thumbSize: Dp, rtl: Boolean, style: TextStyle) {
    val labeled = slider.marks.filter { it.label != null }
    Layout(
        content = { labeled.forEach { BasicText(it.label!!, style = style) } },
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        val width = constraints.maxWidth
        val inset = thumbSize.toPx() / 2f
        val track = width - 2f * inset
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placeables.forEachIndexed { i, p ->
                val position = labeled[i].position
                val center = inset + track * (if (rtl) 1f - position else position)
                val left = (center - p.width / 2f).roundToInt().coerceIn(0, (width - p.width).coerceAtLeast(0))
                p.place(left, 0) // center is already mirrored for RTL
            }
        }
    }
}
