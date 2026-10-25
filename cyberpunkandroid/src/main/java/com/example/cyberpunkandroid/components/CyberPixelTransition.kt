package com.example.cyberpunkandroid.components

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberShaders
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.theme.CyberTheme

private const val ComponentName = "CyberPixelTransition"

/**
 * State holder that manages the animated coverage fraction of a [CyberPixelTransition].
 */
@Stable
class CyberPixelTransitionState internal constructor(
    initialCoverage: Float = 1f,
) {
    internal val animatable = Animatable(initialCoverage)

    /** Fraction of the screen still covered by pixels (1f down to 0f). */
    val coverage: Float
        get() = animatable.value

    /** True when the transition is actively revealing content. */
    val isRunning: Boolean
        get() = animatable.isRunning

    /**
     * Sets the coverage immediately without animation.
     */
    suspend fun snapTo(targetCoverage: Float) {
        animatable.snapTo(targetCoverage)
    }

    /**
     * Runs the reveal animation from covered (1f) to clear (0f).
     */
    suspend fun animateReveal(animationSpec: AnimationSpec<Float>) {
        animatable.snapTo(1f)
        animatable.animateTo(0f, animationSpec)
    }
}

/**
 * Creates and remembers a [CyberPixelTransitionState] keyed on [targetKey].
 */
@Composable
fun rememberCyberPixelTransitionState(
    targetKey: Any,
    animationSpec: AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms150),
): CyberPixelTransitionState {
    val state = remember(targetKey) { CyberPixelTransitionState(initialCoverage = 1f) }
    LaunchedEffect(state, targetKey) {
        state.animateReveal(animationSpec)
    }
    return state
}

/**
 * Computes the pixelate block step in hardware pixels, scaled by current coverage.
 */
internal fun computePixelateStep(pixelSizePx: Float, coverage: Float): Float =
    (pixelSizePx * coverage).coerceAtLeast(1f)

/**
 * Draws the fallback pixel veil for devices below API 33.
 */
internal fun DrawScope.drawPixelVeil(
    tileSizePx: Float,
    coverage: Float,
    color: Color,
) {
    if (coverage <= 0f) return
    val tile = tileSizePx.coerceAtLeast(1f)
    val columns = (size.width / tile).toInt() + 1
    val rows = (size.height / tile).toInt() + 1
    for (row in 0 until rows) {
        for (column in 0 until columns) {
            if (pixelTileCovered(column, row, coverage)) {
                drawRect(color, Offset(column * tile, row * tile), Size(tile, tile))
            }
        }
    }
}

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
    pixelSize: Dp = CyberPrimitives.PixelSizes.dp24,
    animationSpec: AnimationSpec<Float> = tween(CyberPrimitives.Durations.ms150),
    color: Color = CyberTheme.colors.background,
    appendedA11y: String? = null,
    customA11y: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val transitionState = rememberCyberPixelTransitionState(targetKey, animationSpec)

    val effectModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createPixelateShader() }
        Modifier.graphicsLayer {
            val cov = transitionState.coverage
            renderEffect = if (cov > 0f && size.width > 0f && size.height > 0f) {
                CyberShaders.pixelateEffect(
                    shader,
                    size.width,
                    size.height,
                    computePixelateStep(pixelSize.toPx(), cov),
                )
            } else null
        }.background(color)
    } else {
        Modifier.drawWithContent {
            drawContent()
            drawPixelVeil(pixelSize.toPx(), transitionState.coverage, color)
        }
    }

    val hostModifier = modifier
        .cyberComponentSemantics(ComponentName, appendedA11y, customA11y)
        .fillMaxSize()
        .then(effectModifier)

    Box(
        modifier = hostModifier,
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
