package com.example.cyberpunkandroid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberScanlines
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import java.text.BreakIterator
import kotlinx.coroutines.currentCoroutineContext

/**
 * Retro-futuristic command terminal emulator window with top title bar, traffic light window controls, and animated CRT scanlines.
 *
 * Implements the Cybercore CSS `.cyber-terminal` specification. Features three semantic colored dots in the header
 * and an overlay CRT scanline effect across child content.
 *
 * @param title Terminal window header title string.
 * @param modifier Composable modifier applied to the outer terminal frame.
 * @param content Body slot displaying terminal output or interactive input prompts.
 */
@Composable
fun CyberTerminal(
    title: String,
    modifier: Modifier = Modifier,
    appendedA11y: String? = null,
    customA11y: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.cyberComponentSemantics("CyberTerminal", appendedA11y, customA11y)
            .clip(CyberTheme.shapes.cyberCutCornerShape)
            .background(CyberTheme.colors.background)
    ) {
        // Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberTheme.colors.primary.copy(alpha = 0.2f))
                .padding(horizontal = CyberPrimitives.Spacing.dp12, vertical = CyberPrimitives.Spacing.dp8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cyber traffic dots
            Row(horizontalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp4)) {
                Box(modifier = Modifier.size(CyberPrimitives.Spacing.dp8).clip(CircleShape).background(CyberTheme.semantics.colors.danger))
                Box(modifier = Modifier.size(CyberPrimitives.Spacing.dp8).clip(CircleShape).background(CyberTheme.semantics.colors.warning))
                Box(modifier = Modifier.size(CyberPrimitives.Spacing.dp8).clip(CircleShape).background(CyberTheme.semantics.colors.success))
            }
            Spacer(modifier = Modifier.width(CyberPrimitives.Spacing.dp16))
            CompositionLocalProvider(LocalContentColor provides CyberTheme.colors.primary) {
                ProvideTextStyle(value = CyberTheme.typography.terminal) {
                    Text(text = title.lowercase())
                }
            }
        }

        // Body with CRT scanlines
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CyberPrimitives.Spacing.dp16)
                .cyberScanlines()
        ) {
            CompositionLocalProvider(LocalContentColor provides CyberTheme.semantics.colors.terminal) {
                ProvideTextStyle(value = CyberTheme.typography.terminal) {
                    content()
                }
            }
        }
    }
}


// --- Merged from source ---

/**
 * Use this function to place a borderless, full-screen typing terminal behind foreground UI.
 * Text is revealed one glyph at a time, with an underscore cursor; overflowing lines scroll upward.
 *
 * @param text Terminal output, including line breaks and any angular glyphs supplied by the caller.
 * @param modifier Layout and drawing modifiers for the background.
 * @param contentPadding Inset around the output, in addition to safe system-bar insets.
 * @param animated Disable typing to display a static background.
 * @param animationSpec Timing and repetition of a complete typing pass, from zero to one.
 * @param textStyle Typography, including a custom font if readable angular letters are wanted.
 * @param color Terminal output and cursor color.
 * @param backgroundColor Full-screen background color.
 * @param appendedA11y Optional description for this otherwise decorative background.
 * @param customA11y Complete replacement accessibility description.
 * Dependencies: CyberTheme and CyberPrimitives for default styling.
 */
@Composable
fun CyberTerminalBackground(
    text: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(CyberPrimitives.Spacing.dp16),
    animated: Boolean = true,
    // A slow linear pass reads as typing; the delay leaves a pause between repeated passes.
    animationSpec: AnimationSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = 12000, delayMillis = 1500, easing = LinearEasing),
        repeatMode = RepeatMode.Restart,
    ),
    textStyle: TextStyle = CyberTheme.typography.terminal,
    color: Color = CyberTheme.semantics.colors.terminal,
    backgroundColor: Color = CyberTheme.colors.background,
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val boundaries = remember(text) { terminalGlyphBoundaries(text) }
    val progress = remember(text, animated, animationSpec) { Animatable(if (animated) 0f else 1f) }
    val scrollState = rememberScrollState()

    LaunchedEffect(progress) {
        if (!animated || text.isEmpty() || currentCoroutineContext()[MotionDurationScale]?.scaleFactor == 0f) {
            progress.snapTo(1f)
        } else {
            progress.animateTo(1f, animationSpec)
        }
    }

    val glyphCount = boundaries.lastIndex
    val end = boundaries[(progress.value.coerceIn(0f, 1f) * glyphCount).toInt()]
    val output = text.substring(0, end) + if (text.isNotEmpty()) "_" else ""
    LaunchedEffect(end, scrollState.maxValue) {
        if (end == 0) scrollState.scrollTo(0) else scrollState.scrollTo(scrollState.maxValue)
    }

    Box(modifier = modifier.fillMaxSize().background(backgroundColor).clipToBounds()) {
        Text(
            text = output,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(contentPadding)
                .verticalScroll(scrollState)
                .clearAndSetSemantics {
                    // Decorative output must not announce each typed glyph through TalkBack.
                    val description = customA11y ?: appendedA11y
                    if (description != null) contentDescription = description
                },
            style = textStyle,
            color = color,
        )
    }
}

/**
 * Use this function to obtain safe substring boundaries for terminal output without splitting
 * surrogate pairs or combining marks. Input: text to reveal. Dependencies: None.
 */
internal fun terminalGlyphBoundaries(text: String): List<Int> {
    val iterator = BreakIterator.getCharacterInstance().apply { setText(text) }
    return buildList {
        add(iterator.first())
        var boundary = iterator.next()
        while (boundary != BreakIterator.DONE) {
            add(boundary)
            boundary = iterator.next()
        }
    }
}