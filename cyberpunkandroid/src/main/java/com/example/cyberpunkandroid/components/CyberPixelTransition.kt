package com.example.cyberpunkandroid.components

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberShaders
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Use this function to resolve a changed screen from coarse square pixels into sharp content.
 * On API 33+, AGSL pixelates the screen layer; older devices use a staggered square-tile veil.
 *
 * @param targetKey Screen identity; changing it restarts the reveal.
 * @param modifier Layout modifier for the full-screen host.
 * @param pixelSize Largest pixel block or fallback tile edge length.
 * @param animationSpec Timing of the reveal from fully covered to clear.
 * @param color Screen background and fallback pixel-tile color.
 * @param appendedA11y Optional description appended to the transition.
 * @param customA11y Optional replacement description for the transition.
 * @param content Screen content revealed beneath the tiles.
 * Dependencies: CyberTheme and CyberPrimitives supply defaults; CyberShaders pixelates API 33+ layers.
 */
@Composable
fun CyberPixelTransition(
    targetKey: Any,
    modifier: Modifier = Modifier,
    pixelSize: Dp = CyberPrimitives.Spacing.dp24,
    animationSpec: AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms150),
    color: Color = CyberTheme.colors.background,
    appendedA11y: String? = null,
    customA11y: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val coverage = remember(targetKey) { Animatable(1f) }
    LaunchedEffect(coverage) { coverage.animateTo(0f, animationSpec) }

    val effectModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createPixelateShader() }
        Modifier.graphicsLayer {
            renderEffect = if (coverage.value > 0f && size.width > 0f && size.height > 0f) {
                CyberShaders.pixelateEffect(
                    shader,
                    size.width,
                    size.height,
                    (pixelSize.toPx() * coverage.value).coerceAtLeast(1f),
                )
            } else null
        }.background(color)
    } else {
        Modifier.drawWithContent {
            drawContent()
            if (coverage.value > 0f) {
                val tile = pixelSize.toPx().coerceAtLeast(1f)
                val columns = (size.width / tile).toInt() + 1
                val rows = (size.height / tile).toInt() + 1
                for (row in 0 until rows) {
                    for (column in 0 until columns) {
                        if (pixelTileCovered(column, row, coverage.value)) {
                            drawRect(color, Offset(column * tile, row * tile), Size(tile, tile))
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize().then(effectModifier),
        content = content,
    )
}

/** Use this function to decide if a tile remains covered during a pixel reveal.
 * Inputs: column and row identify a tile; coverage is the fraction still hidden.
 * Dependencies: None.
  * @param column TODO: document this
  * @param row TODO: document this
  * @param coverage TODO: document this
 */
internal fun pixelTileCovered(column: Int, row: Int, coverage: Float): Boolean {
    // Stable bit mixing prevents obvious horizontal bands without storing a tile map.
    val rank = ((column * 73) xor (row * 151)) and 0xFF
    return coverage > 0f && (coverage >= 1f || rank / 256f < coverage)
}
