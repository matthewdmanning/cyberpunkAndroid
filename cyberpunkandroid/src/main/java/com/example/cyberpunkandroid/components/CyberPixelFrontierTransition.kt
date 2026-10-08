package com.example.cyberpunkandroid.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.lerp
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberShaders
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.theme.CyberTheme

/** Name used for the accessibility label and for animation labels in tooling. */
private const val ComponentName = "CyberPixelFrontierTransition"

/**
 * Gradient stop of the glow peak. The gradient runs from transparent, to the glow color, to transparent,
 * so the peak sits at the middle, on the frontier.
 */
private const val GlowPeakStop = 0.5f

/**
 * PROTOTYPE. Use this function to switch between two full screens with a pixel-block sweep.
 *
 * Visual: a frontier line sweeps across the screen. The new screen appears behind the frontier and the
 * old screen disappears ahead of it. Near the frontier, both screens break into square pixel blocks.
 * The blocks are largest at the frontier and get smaller (3 steps) farther from it. The frontier is a
 * ragged edge made of whole blocks: each block column shifts it by its own random amount ([frontierJitter]).
 * Neon outlines follow the block sides at the frontier and wherever two
 * neighbor blocks differ in brightness. A red/blue color split and a soft glow add to the look.
 *
 * Behavior by Android version:
 * - API 33 and higher: the effect described above. Each screen runs an AGSL shader on its own layer.
 * - API 32 and lower: the default [AnimatedContent] transition (fade and scale). No pixel effect.
 *
 * Known limits of this prototype:
 * - If [targetState] changes again before a transition ends, the transition restarts and may show a jump.
 * - The two screens must fill the same area. Each screen is given the full size of this host.
 * - Not yet measured on a physical device. See `docs/test-drives/` for the test plan.
 *
 * @param targetState The screen to show. When it changes, the transition runs from the old to the new value.
 * @param modifier Layout modifier for the host.
 * @param orientation [Orientation.Vertical] sweeps the frontier from top to bottom.
 *   [Orientation.Horizontal] sweeps it from left to right.
 * @param blockSize Edge length of the largest pixel block, at the frontier. Larger values look more
 *   dramatic. The smaller blocks are 1/2 and 1/4 of this size.
 * @param bandBlocks Width of the pixelated band, counted in largest blocks. Values below 1 are raised to 1.
 *   Wider bands cost more GPU time.
 * @param edgeWidth Thickness of the neon outlines.
 * @param edgeThreshold Smallest brightness difference (0..1) between two neighbor blocks that draws an
 *   outline. Lower values draw more outlines.
 * @param splitOffset Peak red/blue shift along the sweep axis, strongest at the frontier. 0 turns it off.
 * @param frontierJitter Total random shift of the frontier per block column. 0 gives a straight frontier.
 *   Larger values give a more ragged edge.
 * @param glowStrength Peak opacity (0..1) of the soft glow along the frontier. 0 turns the glow off.
 * @param animationSpec Timing of the sweep from start (0) to end (1). It must be a finite animation, such as
 *   [tween] or a spring.
 * @param edgeColor Color of the outlines and of the glow.
 * @param appendedA11y Optional text appended to the accessibility label.
 * @param customA11y Optional text that replaces the accessibility label.
 * @param content The screen for the given state. It receives the state it must draw. Draw the same
 *   state that the lambda receives, not the outer [targetState], so the old screen stays visible while it leaves.
 * Dependencies: [CyberShaders.PixelFrontierShader] (API 33 and higher), [CyberTheme] for the default color.
 */
@Composable
fun <S> CyberPixelFrontierTransition(
    targetState: S,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Vertical,
    blockSize: Dp = CyberPrimitives.Spacing.dp32,
    bandBlocks: Float = CyberConfig.Shaders.PixelFrontierBandBlocks,
    edgeWidth: Dp = CyberPrimitives.BorderWidths.dp2,
    edgeThreshold: Float = CyberConfig.Shaders.PixelFrontierEdgeThreshold,
    splitOffset: Dp = CyberPrimitives.Spacing.dp4,
    frontierJitter: Dp = CyberPrimitives.Spacing.dp32,
    glowStrength: Float = CyberConfig.Shaders.PixelFrontierGlowStrength,
    animationSpec: FiniteAnimationSpec<Float> = tween(CyberPrimitives.Durations.ms300),
    edgeColor: Color = CyberTheme.colors.primary,
    appendedA11y: String? = null,
    customA11y: String? = null,
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    val hostModifier = modifier.cyberComponentSemantics(ComponentName, appendedA11y, customA11y)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PixelFrontierHost(
            targetState = targetState,
            modifier = hostModifier,
            vertical = orientation == Orientation.Vertical,
            blockSize = blockSize,
            bandBlocks = bandBlocks,
            edgeWidth = edgeWidth,
            edgeThreshold = edgeThreshold,
            splitOffset = splitOffset,
            frontierJitter = frontierJitter,
            glowStrength = glowStrength,
            animationSpec = animationSpec,
            edgeColor = edgeColor,
            content = content,
        )
    } else {
        // The default screen transition is the fallback below API 33.
        AnimatedContent(
            targetState = targetState,
            modifier = hostModifier,
            label = ComponentName,
            content = content,
        )
    }
}

/**
 * Use this function to run the shader version of the transition (API 33 and higher).
 *
 * One transition-wide value, `settled`, drives both screens, so the old and the new screen always cut the
 * same blocks and never overlap or leave a gap. `settled` is 0 when a change starts and 1 when it ends.
 * It is read only in the draw phase, so the sweep does not recompose the screens.
 *
 * @param targetState Screen to show.
 * @param modifier Modifier for the host container.
 * @param vertical True to sweep top to bottom, false to sweep left to right.
 * @param blockSize Edge length of the largest block.
 * @param bandBlocks Band width in largest blocks.
 * @param edgeWidth Outline thickness.
 * @param edgeThreshold Smallest brightness difference that draws an outline.
 * @param splitOffset Peak red/blue shift.
 * @param frontierJitter Total random shift of the frontier per block column.
 * @param glowStrength Peak glow opacity.
 * @param animationSpec Timing of the sweep.
 * @param edgeColor Outline and glow color.
 * @param content Screen content for a state.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun <S> PixelFrontierHost(
    targetState: S,
    modifier: Modifier,
    vertical: Boolean,
    blockSize: Dp,
    bandBlocks: Float,
    edgeWidth: Dp,
    edgeThreshold: Float,
    splitOffset: Dp,
    frontierJitter: Dp,
    glowStrength: Float,
    animationSpec: FiniteAnimationSpec<Float>,
    edgeColor: Color,
    content: @Composable AnimatedContentScope.(S) -> Unit,
) {
    val transition = updateTransition(targetState, label = ComponentName)
    // 1 when the shown screen is the target (idle), 0 at the start of a change. Animated on the parent
    // transition, so AnimatedContent keeps the old screen until this animation ends.
    val settled = transition.animateFloat(
        transitionSpec = { animationSpec },
        label = "$ComponentName.settled",
    ) { state -> if (state == transition.targetState) 1f else 0f }

    val containerModifier = modifier
        .clipToBounds()
        .drawWithContent {
            drawContent()
            val progress = settled.value
            if (glowStrength > 0f && progress < 1f) {
                val blockPx = blockSize.toPx().coerceAtLeast(MinBlockPx)
                val bandPx = (blockPx * bandBlocks).coerceAtLeast(blockPx)
                val axisLength = if (vertical) size.height else size.width
                drawFrontierGlow(
                    frontier = pixelFrontierPosition(progress, axisLength, bandPx + frontierJitter.toPx() / 2),
                    reach = blockPx,
                    vertical = vertical,
                    color = edgeColor.copy(alpha = glowStrength),
                )
            }
        }

    transition.AnimatedContent(
        modifier = containerModifier,
        // Both enter and exit are empty. The shader does the work, and the animation above keeps the
        // old screen alive until it ends. No size animation, because both screens fill the host.
        transitionSpec = { ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null) },
    ) { screen ->
        // The screen for the target state is entering. Any other screen is leaving.
        val entering = screen == transition.targetState
        val shader = remember { CyberShaders.createPixelFrontierShader() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val progress = settled.value
                    renderEffect = if (progress < 1f && size.width > 0f && size.height > 0f) {
                        val blockPx = blockSize.toPx().coerceAtLeast(MinBlockPx)
                        val bandPx = (blockPx * bandBlocks).coerceAtLeast(blockPx)
                        val axisLength = if (vertical) size.height else size.width
                        CyberShaders.pixelFrontierEffect(
                            shader,
                            CyberShaders.PixelFrontierSpec(
                                width = size.width,
                                height = size.height,
                                vertical = vertical,
                                frontier = pixelFrontierPosition(progress, axisLength, bandPx + frontierJitter.toPx() / 2),
                                revealBehind = entering,
                                blockSize = blockPx,
                                bandWidth = bandPx,
                                edgeWidth = edgeWidth.toPx(),
                                edgeThreshold = edgeThreshold,
                                splitOffset = splitOffset.toPx(),
                                jitter = frontierJitter.toPx(),
                                edgeColorArgb = edgeColor.toArgb(),
                            ),
                        )
                    } else {
                        null
                    }
                },
        ) {
            content(screen)
        }
    }
}

/** Smallest block edge in pixels. A block cannot be smaller than one pixel. */
private const val MinBlockPx = 1f

/**
 * Use this function to find where the frontier is, in pixels along the sweep axis.
 * At progress 0 the whole axis, plus the margin, is ahead of the frontier. At progress 1 the whole axis,
 * plus the margin, is behind it. So the band, and the random column shifts, are fully off screen at both ends.
 *
 * @param progress Sweep progress from 0 (start) to 1 (end).
 * @param axisLength Length of the layer along the sweep axis, in pixels.
 * @param margin Distance the frontier travels beyond each end of the layer, in pixels. Use the band
 *   width plus half of the frontier jitter.
 * @return Frontier position in pixels. It is negative before the sweep enters the layer.
 */
internal fun pixelFrontierPosition(progress: Float, axisLength: Float, margin: Float): Float =
    lerp(-margin, axisLength + margin, progress)

/**
 * Use this function to draw a soft glow centered on the frontier. It is one gradient rectangle, so it
 * costs one draw call. The glow is soft, so it does not need to match the staircase edge of the shader.
 *
 * @param frontier Frontier position in pixels along the sweep axis.
 * @param reach Distance in pixels from the frontier to each fading end of the glow.
 * @param vertical True when the frontier moves top to bottom (the glow is a horizontal strip).
 * @param color Glow color at its peak, with the peak opacity in its alpha.
 */
private fun DrawScope.drawFrontierGlow(frontier: Float, reach: Float, vertical: Boolean, color: Color) {
    val start = frontier - reach
    val end = frontier + reach
    val stops = arrayOf(
        0f to color.copy(alpha = 0f),
        GlowPeakStop to color,
        1f to color.copy(alpha = 0f),
    )
    if (vertical) {
        drawRect(
            brush = Brush.verticalGradient(colorStops = stops, startY = start, endY = end),
            topLeft = Offset(0f, start),
            size = Size(size.width, end - start),
        )
    } else {
        drawRect(
            brush = Brush.horizontalGradient(colorStops = stops, startX = start, endX = end),
            topLeft = Offset(start, 0f),
            size = Size(end - start, size.height),
        )
    }
}
