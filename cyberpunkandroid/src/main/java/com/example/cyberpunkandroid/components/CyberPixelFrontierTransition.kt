package com.example.cyberpunkandroid.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
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
 * old screen disappears ahead of it. Across a wide stochastic transition band, both screens dissolve into a
 * fine micro-pixel matrix with stochastic density fade-out.
 * The transition width and pixel density fluctuate via a pre-calculated Fourier series model that creates
 * spatial and temporal autocorrelation (coherent undulating dissolution waves rather than white noise flicker).
 * Neon outlines follow the block sides at the frontier and wherever two
 * neighbor blocks differ in brightness. A red/blue color split and an emissive glow add to the cyberpunk look.
 *
 * Behavior by Android version:
 * - API 33 and higher: the effect described above. Each screen runs an AGSL shader on its own layer.
 * - API 32 and lower: the default [AnimatedContent] transition (fade and scale). No pixel effect.
 *
 * @param targetState The screen to show. When it changes, the transition runs from the old to the new value.
 * @param modifier Layout modifier for the host.
 * @param orientation [Orientation.Vertical] sweeps the frontier from top to bottom.
 *   [Orientation.Horizontal] sweeps it from left to right.
 * @param reverse True to reverse the sweep direction (e.g. Right-to-Left or Bottom-to-Top).
 * @param blockSize Edge length of the largest pixel block in [Dp].
 * @param blockSizePx Explicit edge length in hardware device pixels (defaults to `0.5f` for micro-pixel dissolution).
 *   When specified, overrides [blockSize], bypassing [Dp] rounding to target exact hardware pixel resolution.
 * @param bandBlocks Width of the pixelated band, counted in blocks. Values below 1 are raised to 1.
 *   Defaults to `120f` to create a wide spatial transition wave across the display.
 * @param edgeWidth Thickness of the neon outlines in [Dp].
 * @param edgeWidthPx Explicit outline thickness in hardware device pixels (e.g. `0.5f`).
 * @param edgeThreshold Smallest brightness difference (0..1) between two neighbor blocks that draws an
 *   outline. Lower values draw more outlines.
 * @param splitOffset Peak red/blue shift along the sweep axis, strongest at the frontier. 0 turns it off.
 * @param splitOffsetPx Explicit chromatic aberration offset in hardware device pixels.
 * @param frontierJitter Total random shift of the frontier per block column in [Dp]. 0 gives a straight frontier.
 * @param frontierJitterPx Explicit random jitter in hardware device pixels.
 * @param glowStrength Peak opacity (0..1) of the soft glow along the frontier. 0 turns the glow off.
 * @param animationSpec Timing of the sweep from start (0) to end (1). It must be a finite animation, such as
 *   [tween] or a spring.
 * @param edgeColor Color of the outlines and of the glow.
 * @param stochasticMode Selector for pre-calculated Fourier series harmonics (0: smooth rolling wave, 1: cyber glitch/telemetry, 2: harmonic surge).
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
    reverse: Boolean = false,
    blockSize: Dp = CyberPrimitives.Spacing.dp32,
    blockSizePx: Float? = CyberConfig.Shaders.PixelFrontierBlockSizePx,
    bandBlocks: Float = CyberConfig.Shaders.PixelFrontierBandBlocks,
    edgeWidth: Dp = CyberPrimitives.BorderWidths.dp2,
    edgeWidthPx: Float? = null,
    edgeThreshold: Float = CyberConfig.Shaders.PixelFrontierEdgeThreshold,
    splitOffset: Dp = CyberPrimitives.Spacing.dp4,
    splitOffsetPx: Float? = null,
    frontierJitter: Dp = CyberPrimitives.Spacing.dp32,
    frontierJitterPx: Float? = null,
    glowStrength: Float = CyberConfig.Shaders.PixelFrontierGlowStrength,
    animationSpec: FiniteAnimationSpec<Float> = tween(CyberPrimitives.Durations.ms300),
    edgeColor: Color = CyberTheme.colors.primary,
    stochasticMode: Int = 0,
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
            reverse = reverse,
            blockSize = blockSize,
            blockSizePx = blockSizePx,
            bandBlocks = bandBlocks,
            edgeWidth = edgeWidth,
            edgeWidthPx = edgeWidthPx,
            edgeThreshold = edgeThreshold,
            splitOffset = splitOffset,
            splitOffsetPx = splitOffsetPx,
            frontierJitter = frontierJitter,
            frontierJitterPx = frontierJitterPx,
            glowStrength = glowStrength,
            animationSpec = animationSpec,
            edgeColor = edgeColor,
            stochasticMode = stochasticMode,
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
    reverse: Boolean,
    blockSize: Dp,
    blockSizePx: Float?,
    bandBlocks: Float,
    edgeWidth: Dp,
    edgeWidthPx: Float?,
    edgeThreshold: Float,
    splitOffset: Dp,
    splitOffsetPx: Float?,
    frontierJitter: Dp,
    frontierJitterPx: Float?,
    glowStrength: Float,
    animationSpec: FiniteAnimationSpec<Float>,
    edgeColor: Color,
    stochasticMode: Int = 0,
    content: @Composable AnimatedContentScope.(S) -> Unit,
) {
    var outgoingState by remember { mutableStateOf<S?>(null) }
    var incomingState by remember { mutableStateOf(targetState) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(targetState) {
        if (targetState != incomingState) {
            outgoingState = incomingState
            incomingState = targetState
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec)
            outgoingState = null
        }
    }

    val shaderOutgoing = remember { CyberShaders.createPixelFrontierShader() }
    val shaderIncoming = remember { CyberShaders.createPixelFrontierShader() }

    val containerModifier = modifier
        .clipToBounds()
        .drawWithContent {
            drawContent()
            val p = progress.value
            if (glowStrength > 0f && p < 1f && outgoingState != null) {
                val blockPx = (blockSizePx ?: blockSize.toPx()).coerceAtLeast(MinBlockPx)
                val jitterPx = frontierJitterPx ?: frontierJitter.toPx()
                val bandPx = (blockPx * bandBlocks).coerceAtLeast(blockPx)
                val axisLength = if (vertical) size.height else size.width
                val frontier = pixelFrontierPosition(p, axisLength, bandPx + jitterPx / 2, reverse)
                drawFrontierGlow(
                    frontier = frontier,
                    reach = blockPx.coerceAtLeast(16.dp.toPx()),
                    vertical = vertical,
                    color = edgeColor.copy(alpha = glowStrength),
                )
            }
        }

    Box(modifier = containerModifier) {
        val currentOutgoing = outgoingState
        val isTransitioning = progress.value < 1f && currentOutgoing != null

        if (isTransitioning) {
            // Outgoing screen layer (eroding ahead of frontier)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = progress.value
                        renderEffect = if (size.width > 0f && size.height > 0f) {
                            val blockPx = (blockSizePx ?: blockSize.toPx()).coerceAtLeast(MinBlockPx)
                            val jitterPx = frontierJitterPx ?: frontierJitter.toPx()
                            val bandPx = (blockPx * bandBlocks).coerceAtLeast(blockPx)
                            val edgePx = edgeWidthPx ?: edgeWidth.toPx()
                            val splitPx = splitOffsetPx ?: splitOffset.toPx()
                            val axisLength = if (vertical) size.height else size.width
                            val frontierPos = pixelFrontierPosition(p, axisLength, bandPx + jitterPx / 2, reverse)
                            val shaderFrontier = if (reverse) -frontierPos else frontierPos
                            CyberShaders.pixelFrontierEffect(
                                shaderOutgoing,
                                CyberShaders.PixelFrontierSpec(
                                    width = size.width,
                                    height = size.height,
                                    vertical = vertical,
                                    reverse = reverse,
                                    frontier = shaderFrontier,
                                    revealBehind = false,
                                    blockSize = blockPx,
                                    bandWidth = bandPx,
                                    edgeWidth = edgePx,
                                    edgeThreshold = edgeThreshold,
                                    splitOffset = splitPx,
                                    jitter = jitterPx,
                                    edgeColorArgb = edgeColor.toArgb(),
                                    time = p,
                                    stochasticMode = stochasticMode.toFloat(),
                                ),
                            )
                        } else null
                    }
            ) {
                AnimatedContent(
                    targetState = currentOutgoing,
                    transitionSpec = { ContentTransform(EnterTransition.None, ExitTransition.None) },
                    label = "$ComponentName.outgoing"
                ) { screen ->
                    content(screen)
                }
            }

            // Incoming screen layer (materializing behind frontier)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = progress.value
                        renderEffect = if (size.width > 0f && size.height > 0f) {
                            val blockPx = (blockSizePx ?: blockSize.toPx()).coerceAtLeast(MinBlockPx)
                            val jitterPx = frontierJitterPx ?: frontierJitter.toPx()
                            val bandPx = (blockPx * bandBlocks).coerceAtLeast(blockPx)
                            val edgePx = edgeWidthPx ?: edgeWidth.toPx()
                            val splitPx = splitOffsetPx ?: splitOffset.toPx()
                            val axisLength = if (vertical) size.height else size.width
                            val frontierPos = pixelFrontierPosition(p, axisLength, bandPx + jitterPx / 2, reverse)
                            val shaderFrontier = if (reverse) -frontierPos else frontierPos
                            CyberShaders.pixelFrontierEffect(
                                shaderIncoming,
                                CyberShaders.PixelFrontierSpec(
                                    width = size.width,
                                    height = size.height,
                                    vertical = vertical,
                                    reverse = reverse,
                                    frontier = shaderFrontier,
                                    revealBehind = true,
                                    blockSize = blockPx,
                                    bandWidth = bandPx,
                                    edgeWidth = edgePx,
                                    edgeThreshold = edgeThreshold,
                                    splitOffset = splitPx,
                                    jitter = jitterPx,
                                    edgeColorArgb = edgeColor.toArgb(),
                                    time = p,
                                    stochasticMode = stochasticMode.toFloat(),
                                ),
                            )
                        } else null
                    }
            ) {
                AnimatedContent(
                    targetState = incomingState,
                    transitionSpec = { ContentTransform(EnterTransition.None, ExitTransition.None) },
                    label = "$ComponentName.incoming"
                ) { screen ->
                    content(screen)
                }
            }
        } else {
            // Idle: display only incoming target screen
            AnimatedContent(
                targetState = incomingState,
                transitionSpec = { ContentTransform(EnterTransition.None, ExitTransition.None) },
                label = "$ComponentName.idle"
            ) { screen ->
                content(screen)
            }
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
 * @param reverse True to sweep backward (e.g. right-to-left or bottom-to-top).
 * @return Frontier position in pixels.
 */
internal fun pixelFrontierPosition(
    progress: Float,
    axisLength: Float,
    margin: Float,
    reverse: Boolean = false,
): Float =
    if (reverse) lerp(axisLength + margin, -margin, progress)
    else lerp(-margin, axisLength + margin, progress)

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
