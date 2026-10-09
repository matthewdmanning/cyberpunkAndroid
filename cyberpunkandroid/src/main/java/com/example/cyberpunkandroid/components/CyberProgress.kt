package com.example.cyberpunkandroid.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.cyberpunkandroid.config.CyberPrimitives

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.example.cyberpunkandroid.effects.cyberBorder
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Visual presentation style for [CyberProgress].
 */
enum class CyberProgressVariant {
    /** Smooth continuous horizontal progress fill bar. */
    Continuous,
    /** Discrete multi-segment meter consisting of indexed LED-style tick blocks. */
    Segmented
}

/**
 * Sci-fi horizontal progress gauge supporting smooth fill and discrete multi-segment LED meter modes.
 *
 * Implements the Cybercore CSS `.cyber-progress` specification with neon glow borders and segmented steps.
 *
 * @param progress Normalized completion value between `0.0f` and `1.0f`. Values outside range are coerced automatically.
 * @param modifier Composable modifier applied to the progress gauge container.
 * @param variant Visual mode ([CyberProgressVariant.Continuous] or [CyberProgressVariant.Segmented]). Defaults to [CyberProgressVariant.Continuous].
 * @param color Neon accent tint applied to the active progress fill. Defaults to [CyberTheme.colors.primary].
 * @param segments Total number of discrete blocks rendered when [variant] is [CyberProgressVariant.Segmented]. Defaults to `10`.
 * @param appendedA11y Optional text to append to the default component accessibility name.
 * @param customA11y Optional custom accessibility description that completely overrides the default name.
 */
@Composable
fun CyberProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    variant: CyberProgressVariant = CyberProgressVariant.Continuous,
    segments: Int = 10,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Float> = androidx.compose.animation.core.spring(),
    color: Color = CyberTheme.colors.primary,
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = animationSpec,
        label = "ProgressAnimation"
    )

    val boundedProgress = progress.coerceIn(0f, 1f)
    val a11yDescription = customA11y ?: if (!appendedA11y.isNullOrBlank()) "Progress - $appendedA11y" else null

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = boundedProgress,
                    range = 0f..1f
                )
                stateDescription = "${(boundedProgress * 100).toInt()}%"
                if (a11yDescription != null) {
                    contentDescription = a11yDescription
                }
            }
            .fillMaxWidth()
            .height(CyberPrimitives.Spacing.dp12)
            .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
            .background(CyberTheme.colors.surface)
            .cyberBorder(color = color,
                width = CyberPrimitives.BorderWidths.dp1, shape = CyberTheme.shapes.cyberCutCornerShapeSmall)
            .padding(CyberPrimitives.BorderWidths.dp2)
    ) {
        when (variant) {
            CyberProgressVariant.Continuous -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
                        .background(color)
                )
            }
            CyberProgressVariant.Segmented -> {
                Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                    val filledSegments = (animatedProgress * segments).toInt()
                    for (i in 0 until segments) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (i < filledSegments) color else Color.Transparent)
                        )
                        if (i < (segments - 1)) {
                            Spacer(modifier = Modifier.width(CyberPrimitives.BorderWidths.dp2))
                        }
                    }
                }
            }
        }
    }
}
