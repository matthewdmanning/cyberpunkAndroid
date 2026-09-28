package com.example.cyberpunkandroid.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlinx.coroutines.delay

/**
 * A typographic progress indicator that reveals [targetText] as [progress] approaches 1f.
 * Unresolved characters rapidly cycle through random glyphs.
 * 
 * Unadorned base item: Provides text layout and logic, while exposing rich styling parameters
 * and adhering to strict semantic rules for accessibility.
 */
@Composable
fun CyberDecrypter(
    targetText: String,
    progress: Float,
    modifier: Modifier = Modifier,
    charset: String = "0123456789ABCDEF!@#$%^&*",
    tickDelayMs: Long = 50L,
    textStyle: TextStyle = CyberTheme.typography.terminal,
    unresolvedStyle: SpanStyle = SpanStyle(
        color = CyberTheme.colors.primary.copy(alpha = 0.5f),
        shadow = null
    ),
    resolvedStyle: SpanStyle = SpanStyle(
        color = CyberTheme.colors.primary,
        shadow = CyberPrimitives.Shadows.neonGlow(CyberTheme.colors.primary)
    ),
    appendedA11y: String? = null,
    customA11y: String? = null
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val resolvedCount = (targetText.length * clampedProgress).toInt()
    
    val currentProgress by rememberUpdatedState(clampedProgress)

    // Cycle unresolved characters
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(targetText, tickDelayMs) {
        while (currentProgress < 1f) {
            delay(tickDelayMs)
            tick++
        }
    }
    
    val displayText = buildAnnotatedString {
        withStyle(resolvedStyle) {
            append(targetText.take(resolvedCount))
        }
        withStyle(unresolvedStyle) {
            val remaining = targetText.length - resolvedCount
            for (i in 0 until remaining) {
                // Pseudo-random character based on tick and index
                val charIndex = (tick + i * 7) % charset.length
                append(charset[charIndex])
            }
        }
    }
    
    Box(
        modifier = modifier.semantics(mergeDescendants = true) {
            text = AnnotatedString(targetText)
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = clampedProgress,
                range = 0f..1f
            )
        }
    ) {
        BasicText(
            text = displayText,
            style = textStyle
        )
    }
}
