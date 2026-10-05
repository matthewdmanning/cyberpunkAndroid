package com.example.cyberpunkandroid.effects

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.utils.CyberPathGeometry
import com.example.cyberpunkandroid.utils.CyberPathGeometry.hash01
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Laser Outliner effect: a glowing laser beam creates a high-temperature weld effect around the border
 * of the text or shape, progressing from left to right, one letter (or segment) at a time.
 *
 * Meant primarily for one-shot entrance reveals (defaults to a single-pass `tween`), but also supports
 * repeating specs or interaction triggers.
 *
 * ### Visual Sequence
 * 1. **Laser Beam**: A brilliant vertical ray descending onto the active contact point.
 * 2. **Molten Arc**: A white-hot plasma core and saturated laser flare at the point of welding.
 * 3. **Sparks**: Physics-driven fizzing sparks spraying outward and falling under gravity.
 * 4. **Cooling Weld Bead**: Behind the laser, the newly laid seam transitions from white-hot to molten gold,
 *    cooling to cherry-red and settling into the final outline color.
 * 5. **Left-to-Right Progression**: Letters before the current progress sit fully welded; the active letter is
 *    being cut and welded; upcoming letters wait as faint blueprint guides.
 *
 * @param text Optional text to extract vector contours from. When provided, letters are welded one-by-one from left to right.
 * @param shape Fallback shape outline when [text] is null.
 * @param fontSize Font size for text contour extraction.
 * @param laserColor Color of the piercing laser beam and its optical halo.
 * @param weldColor Hot molten temperature color of the newly deposited weld bead.
 * @param coolColor Settled color of the fully cooled weld seam. Defaults to `CyberTheme.colors.primary`.
 * @param guideAlpha Alpha transparency of the unwelded blueprint guide trace.
 * @param strokeWidth Thickness of the core laser ray and welded outline.
 * @param glowRadius Spread of the optical bloom around the laser and molten pool.
 * @param sparkCount Number of fizzing weld sparks emitted from the contact point.
 * @param durationMillis Duration of one complete left-to-right pass.
 * @param trigger When the effect runs; defaults to [CyberInteractionTrigger.ALWAYS].
 * @param interactionSource Source for interaction-driven triggers.
 * @param animationSpec Drives progress 0 -> 1. Defaults to a finite one-shot `tween`.
 * @param appendedA11y Optional accessibility text appended to the default description.
 * @param customA11y Optional replacement accessibility description.
 */
fun Modifier.cyberLaserOutliner(
    text: String? = null,
    shape: Shape = RectangleShape,
    fontSize: TextUnit = 32.sp,
    laserColor: Color = Color.Cyan,
    weldColor: Color = Color(0xFFFFB800),
    coolColor: Color = Color.Unspecified,
    guideAlpha: Float = 0.08f,
    strokeWidth: Dp = 1.5.dp,
    glowRadius: Dp = 8.dp,
    sparkCount: Int = 10,
    durationMillis: Int = 2400,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = tween(durationMillis, easing = LinearEasing),
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this.cyberSemantics("CyberLaserOutliner", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    val resolvedCoolColor = if (coolColor == Color.Unspecified) CyberTheme.colors.primary else coolColor
    val progressState = rememberLaserProgress(isActive, animationSpec)
    val density = LocalDensity.current

    drawWithCache {
        val strokeWidthPx = strokeWidth.toPx()
        val glowPx = glowRadius.toPx()

        // 1. Prepare outlines
        val outlines: List<LaserContour> = if (!text.isNullOrEmpty()) {
            extractTextContours(text, size, density, fontSize)
        } else {
            val path = CyberPathGeometry.outlinePath(shape, size, layoutDirection, density, 0f)
            val measure = PathMeasure().apply { setPath(path, false) }
            val len = measure.length
            if (len > 0f) listOf(LaserContour(listOf(ContourSegment(path, len)), len)) else emptyList()
        }

        val glowLayer = obtainGraphicsLayer()

        onDrawWithContent {
            drawContent()

            if (outlines.isEmpty()) return@onDrawWithContent

            val p = progressState.value.coerceIn(0f, 1f)
            val totalCount = outlines.size

            // 2. Draw blueprint guide for unwelded portions
            if (guideAlpha > 0f) {
                for (outline in outlines) {
                    for (seg in outline.segments) {
                        drawPath(
                            path = seg.path,
                            color = resolvedCoolColor.copy(alpha = guideAlpha),
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // 3. Process each letter/outline in sequence from left to right
            for (index in outlines.indices) {
                val outline = outlines[index]
                val slotStart = index.toFloat() / totalCount
                val slotEnd = (index + 1).toFloat() / totalCount

                when {
                    // Fully welded letter
                    p >= slotEnd -> {
                        for (seg in outline.segments) {
                            drawPath(
                                path = seg.path,
                                color = resolvedCoolColor,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Actively welding letter
                    p in slotStart..slotEnd -> {
                        val localProgress = ((p - slotStart) / (slotEnd - slotStart)).coerceIn(0f, 1f)
                        drawActiveWeld(
                            outline = outline,
                            localProgress = localProgress,
                            globalProgress = p,
                            laserColor = laserColor,
                            weldColor = weldColor,
                            coolColor = resolvedCoolColor,
                            strokeWidthPx = strokeWidthPx,
                            glowPx = glowPx,
                            glowLayer = glowLayer,
                            sparkCount = sparkCount,
                            density = this
                        )
                    }

                    // Not yet reached: only the blueprint guide is visible
                    else -> Unit
                }
            }
        }
    }
}

/** Segment of a path with a known length. */
internal class ContourSegment(val path: Path, val length: Float)

/** Represents a single character or shape contour made up of one or more sub-paths. */
internal class LaserContour(val segments: List<ContourSegment>, val totalLength: Float)

/**
 * Extracts vector contours for each non-empty character in [text], sorted from left to right.
 */
private fun extractTextContours(
    text: String,
    size: Size,
    density: Density,
    fontSize: TextUnit
): List<LaserContour> {
    val paint = Paint().apply {
        textSize = with(density) { fontSize.toPx() }
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    val widths = FloatArray(text.length)
    paint.getTextWidths(text, widths)
    val totalTextWidth = widths.sum()

    val fontMetrics = paint.fontMetrics
    val textHeight = fontMetrics.descent - fontMetrics.ascent
    val baselineY = (size.height - textHeight) / 2f - fontMetrics.ascent
    val startX = max(0f, (size.width - totalTextWidth) / 2f)

    val outlines = mutableListOf<LaserContour>()
    var curX = startX

    for (i in text.indices) {
        val w = widths[i]
        val charStr = text.substring(i, i + 1)
        if (charStr.isNotBlank()) {
            val androidPath = android.graphics.Path()
            paint.getTextPath(text, i, i + 1, curX, baselineY, androidPath)
            val composePath = androidPath.asComposePath()

            val androidMeasure = android.graphics.PathMeasure(androidPath, false)
            val segments = mutableListOf<ContourSegment>()
            var totalLen = 0f

            do {
                val len = androidMeasure.length
                if (len > 0f) {
                    val segPath = android.graphics.Path()
                    androidMeasure.getSegment(0f, len, segPath, true)
                    segments.add(ContourSegment(segPath.asComposePath(), len))
                    totalLen += len
                }
            } while (androidMeasure.nextContour())

            if (totalLen > 0f) {
                outlines.add(LaserContour(segments, totalLen))
            }
        }
        curX += w
    }
    return outlines
}

/**
 * Draws the active weld seam, cooling tail, laser beam, and sparks for a single contour.
 */
private fun DrawScope.drawActiveWeld(
    outline: LaserContour,
    localProgress: Float,
    globalProgress: Float,
    laserColor: Color,
    weldColor: Color,
    coolColor: Color,
    strokeWidthPx: Float,
    glowPx: Float,
    glowLayer: GraphicsLayer,
    sparkCount: Int,
    density: Density
) {
    val targetDist = localProgress * outline.totalLength
    var accumulated = 0f
    var contactPoint: Offset? = null

    val measure = PathMeasure()

    for (seg in outline.segments) {
        val segStart = accumulated
        val segEnd = accumulated + seg.length
        accumulated = segEnd

        when {
            // Segment fully welded
            targetDist >= segEnd -> {
                drawPath(
                    path = seg.path,
                    color = coolColor,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }

            // Segment actively welding
            targetDist in segStart..segEnd -> {
                val d = targetDist - segStart
                measure.setPath(seg.path, false)

                // Welded bead so far
                val weldedSegment = Path()
                measure.getSegment(0f, d, weldedSegment, true)

                // Base cooled line
                drawPath(
                    path = weldedSegment,
                    color = coolColor,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                // Active molten bead trail (recent 25% of the stroke)
                val hotTrailDist = max(0f, d - seg.length * 0.25f)
                val hotSegment = Path()
                measure.getSegment(hotTrailDist, d, hotSegment, true)

                drawPath(
                    path = hotSegment,
                    color = weldColor,
                    style = Stroke(width = strokeWidthPx * 1.8f, cap = StrokeCap.Round)
                )

                // White-hot tip
                val whiteTipDist = max(0f, d - seg.length * 0.06f)
                val whiteSegment = Path()
                measure.getSegment(whiteTipDist, d, whiteSegment, true)

                drawPath(
                    path = whiteSegment,
                    color = Color.White,
                    style = Stroke(width = strokeWidthPx * 2.4f, cap = StrokeCap.Round)
                )

                contactPoint = measure.getPosition(d)
            }

            // Segment not yet reached
            else -> Unit
        }
    }

    // Draw Laser Beam and Arc at contact point
    contactPoint?.let { pt ->
        val laserOrigin = Offset(pt.x, 0f)

        // 1. Outer laser beam halo
        drawLine(
            color = laserColor.copy(alpha = 0.45f),
            start = laserOrigin,
            end = pt,
            strokeWidth = strokeWidthPx * 4f,
            cap = StrokeCap.Round
        )

        // 2. Core piercing laser beam
        drawLine(
            color = Color.White,
            start = laserOrigin,
            end = pt,
            strokeWidth = strokeWidthPx * 1.2f,
            cap = StrokeCap.Round
        )

        // 3. Contact arc flare
        drawCircle(
            color = laserColor.copy(alpha = 0.70f),
            radius = strokeWidthPx * 4.5f,
            center = pt
        )
        drawCircle(
            color = Color.White,
            radius = strokeWidthPx * 2.0f,
            center = pt
        )

        // 4. Welding Sparks
        drawWeldSparks(
            contactPoint = pt,
            globalProgress = globalProgress,
            sparkCount = sparkCount,
            weldColor = weldColor,
            laserColor = laserColor,
            density = density
        )
    }
}

/**
 * Draws physics-driven sparks spraying outward and falling under gravity from the contact point.
 */
private fun DrawScope.drawWeldSparks(
    contactPoint: Offset,
    globalProgress: Float,
    sparkCount: Int,
    weldColor: Color,
    laserColor: Color,
    density: Density
) {
    val time = globalProgress * 10f // particle animation time
    val gravity = with(density) { 320.dp.toPx() }

    for (k in 0 until sparkCount) {
        val seed = k * 13.7 + floor(time * 3f)
        val life = 0.35f + 0.30f * hash01(seed * 2.1)
        val age = (time % life)
        val progress = age / life

        val angle = -PI.toFloat() * (0.15f + 0.70f * hash01(seed * 3.3)) // spray upward and outward
        val speed = with(density) { (80.dp.toPx() + 140.dp.toPx() * hash01(seed * 4.7)) }

        val vx = cos(angle) * speed
        val vy = sin(angle) * speed

        val sx = contactPoint.x + vx * age
        val sy = contactPoint.y + vy * age + 0.5f * gravity * age * age

        val prevAge = max(0f, age - 0.025f)
        val px = contactPoint.x + vx * prevAge
        val py = contactPoint.y + vy * prevAge + 0.5f * gravity * prevAge * prevAge

        val sparkColor = when {
            progress < 0.25f -> Color.White
            progress < 0.65f -> weldColor
            else -> lerp(weldColor, Color(0xFFFF3B30), (progress - 0.65f) / 0.35f)
        }

        drawLine(
            color = sparkColor.copy(alpha = (1f - progress).coerceIn(0f, 1f)),
            start = Offset(px, py),
            end = Offset(sx, sy),
            strokeWidth = with(density) { 1.2.dp.toPx() },
            cap = StrokeCap.Round
        )
    }
}

/**
 * Manages one-shot progress animation 0 -> 1 driven by [animationSpec].
 */
@Composable
private fun rememberLaserProgress(
    isActive: Boolean,
    animationSpec: AnimationSpec<Float>
): State<Float> {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(isActive, animationSpec) {
        if (isActive) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec)
        } else {
            progress.snapTo(0f)
        }
    }
    return progress.asState()
}
