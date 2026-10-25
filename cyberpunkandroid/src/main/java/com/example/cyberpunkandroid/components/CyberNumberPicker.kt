package com.example.cyberpunkandroid.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Dimension and proportion presets for [CyberNumberPicker].
 *
 * Implements golden-ratio (~1.618:1) proportions between central readout display and flanking buttons
 * while maintaining accessible touch targets and standard spacing tokens.
 *
 * @property height Overall height of the picker in horizontal orientation.
 * @property buttonWidth Width of each increment/decrement button in horizontal orientation.
 * @property readoutMinWidth Minimum width of the central readout display in horizontal orientation.
 * @property iconSize Dimension of the increment and decrement vector icons.
 * @property cornerCut Chamfer cut distance for cut-corner styling.
 * @property horizontalSpacing Spacing between the stepper buttons and the readout display.
 * @property verticalButtonHeight Height of each stepper button in vertical orientation.
 * @property verticalReadoutHeight Height of the readout display in vertical orientation.
 * @property verticalWidth Overall width of the picker in vertical orientation.
 */
enum class CyberNumberPickerSize(
    val height: Dp,
    val buttonWidth: Dp,
    val readoutMinWidth: Dp,
    val iconSize: Dp,
    val cornerCut: Dp,
    val horizontalSpacing: Dp,
    val verticalButtonHeight: Dp,
    val verticalReadoutHeight: Dp,
    val verticalWidth: Dp,
) {
    /** Compact size for high-density HUD panels and tight layouts. */
    Small(
        height = CyberPrimitives.Spacing.dp32 + CyberPrimitives.Spacing.dp4, // 36.dp
        buttonWidth = CyberPrimitives.Spacing.dp32 + CyberPrimitives.Spacing.dp4, // 36.dp
        readoutMinWidth = CyberPrimitives.Spacing.dp64, // 64.dp (~1.77:1 ratio)
        iconSize = CyberPrimitives.IconSizes.dp16,
        cornerCut = CyberPrimitives.Spacing.dp4 + CyberPrimitives.Spacing.dp2, // 6.dp
        horizontalSpacing = CyberPrimitives.Spacing.dp4,
        verticalButtonHeight = CyberPrimitives.Spacing.dp32,
        verticalReadoutHeight = CyberPrimitives.Spacing.dp48,
        verticalWidth = CyberPrimitives.Spacing.dp48 + CyberPrimitives.Spacing.dp8, // 56.dp
    ),

    /** Standard size complying with 48dp accessible touch target standards and ~1.67:1 golden ratio display. */
    Medium(
        height = CyberPrimitives.Spacing.dp48, // 48.dp
        buttonWidth = CyberPrimitives.Spacing.dp48, // 48.dp
        readoutMinWidth = CyberPrimitives.Spacing.dp64 + CyberPrimitives.Spacing.dp16, // 80.dp (~1.67:1 golden ratio)
        iconSize = CyberPrimitives.IconSizes.dp24,
        cornerCut = CyberPrimitives.Spacing.dp8, // 8.dp
        horizontalSpacing = CyberPrimitives.Spacing.dp8,
        verticalButtonHeight = CyberPrimitives.Spacing.dp48 - CyberPrimitives.Spacing.dp8, // 40.dp
        verticalReadoutHeight = CyberPrimitives.Spacing.dp48 + CyberPrimitives.Spacing.dp8, // 56.dp
        verticalWidth = CyberPrimitives.Spacing.dp64 + CyberPrimitives.Spacing.dp4, // 68.dp
    ),

    /** Prominent hero size for primary data adjusters and focal controls. */
    Large(
        height = CyberPrimitives.Spacing.dp48 + CyberPrimitives.Spacing.dp8, // 56.dp
        buttonWidth = CyberPrimitives.Spacing.dp48 + CyberPrimitives.Spacing.dp8, // 56.dp
        readoutMinWidth = CyberPrimitives.Spacing.dp64 + CyberPrimitives.Spacing.dp32, // 96.dp (~1.71:1 ratio)
        iconSize = CyberPrimitives.IconSizes.dp24,
        cornerCut = CyberPrimitives.Spacing.dp12, // 12.dp
        horizontalSpacing = CyberPrimitives.Spacing.dp12,
        verticalButtonHeight = CyberPrimitives.Spacing.dp48,
        verticalReadoutHeight = CyberPrimitives.Spacing.dp64,
        verticalWidth = CyberPrimitives.Spacing.dp64 + CyberPrimitives.Spacing.dp16 + CyberPrimitives.Spacing.dp4, // 84.dp
    ),
}

/**
 * Layout orientation for [CyberNumberPicker].
 */
enum class CyberNumberPickerOrientation {
    /** Buttons positioned horizontally on the left (-) and right (+) flanks of the central readout. */
    Horizontal,

    /** Buttons positioned vertically above (+) and below (-) the central readout. */
    Vertical,
}

/**
 * Interactive futuristic number picker with tactile stepping buttons, chamfered geometry, HUD telemetry
 * brackets, and animated odometer-style digit transitions.
 *
 * Implements cyberdeck hardware console styling with ergonomic touch targets and golden-ratio proportions.
 *
 * ### Interactive & Visual Features:
 * - **Chamfered Framing**: Outer buttons feature complementary cut-corner profiles framing the central readout.
 * - **HUD Telemetry Accents**: Readout frame renders illuminated sci-fi corner bracket tick marks.
 * - **Auto-Repeat Stepping**: Holding down either step button auto-repeats after [initialRepeatDelayMillis],
 *   stepping continuously every [repeatIntervalMillis].
 * - **Tactile Feedback**: Pressing a step button animates a 94% scale reduction and intensifies neon glow.
 * - **Animated Digit Transition**: Numbers roll smoothly with directional vertical slide and cross-fade animations.
 * - **Accessibility**: Exposes [ProgressBarRangeInfo], TalkBack increment/decrement actions, and custom accessibility labels.
 *
 * @param value Current numeric value supplied by the caller.
 * @param range Inclusive acceptable range for the value. Defaults to 0..100.
 * @param step Amount added or subtracted with each step increment/decrement. Defaults to 1.
 * @param modifier Composable modifier applied to the picker container.
 * @param size Dimension scale preset ([CyberNumberPickerSize.Small], [CyberNumberPickerSize.Medium],
 *   [CyberNumberPickerSize.Large]). Defaults to [CyberNumberPickerSize.Medium].
 * @param orientation Visual layout direction ([CyberNumberPickerOrientation.Horizontal] or
 *   [CyberNumberPickerOrientation.Vertical]). Defaults to [CyberNumberPickerOrientation.Horizontal].
 * @param enabled Controls whether the buttons respond to user input and whether semantic actions are available.
 * @param label Optional uppercase monospace header text displayed above the control.
 * @param prefix Optional prefix string displayed before the numeric value (e.g., "> " or "LVL:").
 * @param suffix Optional suffix string displayed after the numeric value (e.g., " %" or " MHz").
 * @param textStyle Optional typography style override for the central value readout.
 * @param decrementIconRes Optional drawable resource ID for the decrement icon. Defaults to
 *   [CyberIcons.Minus] in horizontal orientation and [CyberIcons.ChevronDown] in vertical orientation.
 * @param incrementIconRes Optional drawable resource ID for the increment icon. Defaults to
 *   [CyberIcons.Plus] in horizontal orientation and [CyberIcons.ChevronUp] in vertical orientation.
 * @param initialRepeatDelayMillis Milliseconds before auto-repeat begins when holding a step button. Defaults to 400ms.
 * @param repeatIntervalMillis Interval in milliseconds between successive steps during auto-repeat. Defaults to 100ms.
 * @param formatValue Lambda formatting the integer value into a display string. Defaults to decimal representation.
 * @param animationSpec Animation spec applied to the stepping transition.
 * @param color Primary neon accent color for buttons, borders, and active indicators. Defaults to [CyberTheme.colors.primary].
 * @param contentColor Emissive color for the value readout text. Defaults to [CyberTheme.colors.textPrimary].
 * @param backgroundColor Surface background fill for the readout and buttons. Defaults to [CyberTheme.colors.surfaceSecondary].
 * @param borderColor Inactive container stroke color. Defaults to [CyberTheme.colors.border].
 * @param appendedA11y Optional text appended to the default accessibility description.
 * @param customA11y Optional custom accessibility description completely overriding default text.
 * @param onValueChange Callback invoked when the number changes, receiving the clamped target integer.
 */
@Composable
fun CyberNumberPicker(
    value: Int,
    range: IntRange = 0..100,
    step: Int = 1,
    modifier: Modifier = Modifier,
    size: CyberNumberPickerSize = CyberNumberPickerSize.Medium,
    orientation: CyberNumberPickerOrientation = CyberNumberPickerOrientation.Horizontal,
    enabled: Boolean = true,
    label: String? = null,
    prefix: String? = null,
    suffix: String? = null,
    textStyle: TextStyle? = null,
    @DrawableRes decrementIconRes: Int? = null,
    @DrawableRes incrementIconRes: Int? = null,
    initialRepeatDelayMillis: Long = 400L,
    repeatIntervalMillis: Long = 100L,
    formatValue: (Int) -> String = { it.toString() },
    animationSpec: AnimationSpec<Float> = spring(),
    color: Color = CyberTheme.colors.primary,
    contentColor: Color = CyberTheme.colors.textPrimary,
    backgroundColor: Color = CyberTheme.colors.surfaceSecondary,
    borderColor: Color = CyberTheme.colors.border,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onValueChange: (Int) -> Unit,
) {
    val boundedValue = value.coerceIn(range.first, range.last)
    val canDecrement = enabled && boundedValue > range.first
    val canIncrement = enabled && boundedValue < range.last

    val resolvedDecrementIcon = decrementIconRes ?: when (orientation) {
        CyberNumberPickerOrientation.Horizontal -> CyberIcons.Minus
        CyberNumberPickerOrientation.Vertical -> CyberIcons.ChevronDown
    }

    val resolvedIncrementIcon = incrementIconRes ?: when (orientation) {
        CyberNumberPickerOrientation.Horizontal -> CyberIcons.Plus
        CyberNumberPickerOrientation.Vertical -> CyberIcons.ChevronUp
    }

    val effectiveTextStyle = textStyle ?: when (size) {
        CyberNumberPickerSize.Small -> CyberTheme.typography.terminal.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
        CyberNumberPickerSize.Medium -> CyberTheme.typography.terminal.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        CyberNumberPickerSize.Large -> CyberTheme.typography.display.copy(
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
        )
    }

    val outerModifier = modifier
        .cyberComponentSemantics("CyberNumberPicker", appendedA11y, customA11y)
        .semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = boundedValue.toFloat(),
                range = range.first.toFloat()..range.last.toFloat(),
                steps = if (step > 0) ((range.last - range.first) / step) else 0,
            )
            stateDescription = formatValue(boundedValue)
            if (enabled) {
                setProgress { target ->
                    val clamped = target.toInt().coerceIn(range.first, range.last)
                    onValueChange(clamped)
                    true
                }
            }
        }

    Column(modifier = outerModifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label.uppercase(),
                style = CyberTheme.typography.terminal,
                color = if (enabled) CyberTheme.colors.textSecondary else CyberTheme.colors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = CyberTheme.spacing.dp4),
            )
        }

        when (orientation) {
            CyberNumberPickerOrientation.Horizontal -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(size.horizontalSpacing),
                ) {
                    // Decrement Button (Left flank with cut outer corners)
                    CyberNumberPickerStepButton(
                        iconRes = resolvedDecrementIcon,
                        contentDescription = "Decrement",
                        onClickLabel = "Decrement",
                        enabled = canDecrement,
                        shape = CutCornerShape(
                            topStart = size.cornerCut,
                            bottomStart = size.cornerCut,
                            topEnd = 0.dp,
                            bottomEnd = 0.dp,
                        ),
                        width = size.buttonWidth,
                        height = size.height,
                        iconSize = size.iconSize,
                        color = color,
                        backgroundColor = backgroundColor,
                        borderColor = borderColor,
                        initialRepeatDelayMillis = initialRepeatDelayMillis,
                        repeatIntervalMillis = repeatIntervalMillis,
                        onStep = {
                            val next = (boundedValue - step).coerceIn(range.first, range.last)
                            if (next != boundedValue) onValueChange(next)
                        },
                    )

                    // Central Readout Display
                    CyberNumberPickerReadout(
                        value = boundedValue,
                        formatValue = formatValue,
                        prefix = prefix,
                        suffix = suffix,
                        minWidth = size.readoutMinWidth,
                        height = size.height,
                        cornerCut = size.cornerCut,
                        enabled = enabled,
                        color = color,
                        contentColor = contentColor,
                        backgroundColor = backgroundColor,
                        borderColor = borderColor,
                        textStyle = effectiveTextStyle,
                    )

                    // Increment Button (Right flank with cut outer corners)
                    CyberNumberPickerStepButton(
                        iconRes = resolvedIncrementIcon,
                        contentDescription = "Increment",
                        onClickLabel = "Increment",
                        enabled = canIncrement,
                        shape = CutCornerShape(
                            topStart = 0.dp,
                            bottomStart = 0.dp,
                            topEnd = size.cornerCut,
                            bottomEnd = size.cornerCut,
                        ),
                        width = size.buttonWidth,
                        height = size.height,
                        iconSize = size.iconSize,
                        color = color,
                        backgroundColor = backgroundColor,
                        borderColor = borderColor,
                        initialRepeatDelayMillis = initialRepeatDelayMillis,
                        repeatIntervalMillis = repeatIntervalMillis,
                        onStep = {
                            val next = (boundedValue + step).coerceIn(range.first, range.last)
                            if (next != boundedValue) onValueChange(next)
                        },
                    )
                }
            }

            CyberNumberPickerOrientation.Vertical -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(size.horizontalSpacing),
                ) {
                    // Increment Button (Top cap with cut top corners)
                    CyberNumberPickerStepButton(
                        iconRes = resolvedIncrementIcon,
                        contentDescription = "Increment",
                        onClickLabel = "Increment",
                        enabled = canIncrement,
                        shape = CutCornerShape(
                            topStart = size.cornerCut,
                            topEnd = size.cornerCut,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp,
                        ),
                        width = size.verticalWidth,
                        height = size.verticalButtonHeight,
                        iconSize = size.iconSize,
                        color = color,
                        backgroundColor = backgroundColor,
                        borderColor = borderColor,
                        initialRepeatDelayMillis = initialRepeatDelayMillis,
                        repeatIntervalMillis = repeatIntervalMillis,
                        onStep = {
                            val next = (boundedValue + step).coerceIn(range.first, range.last)
                            if (next != boundedValue) onValueChange(next)
                        },
                    )

                    // Central Readout Display
                    CyberNumberPickerReadout(
                        value = boundedValue,
                        formatValue = formatValue,
                        prefix = prefix,
                        suffix = suffix,
                        minWidth = size.verticalWidth,
                        height = size.verticalReadoutHeight,
                        cornerCut = size.cornerCut,
                        enabled = enabled,
                        color = color,
                        contentColor = contentColor,
                        backgroundColor = backgroundColor,
                        borderColor = borderColor,
                        textStyle = effectiveTextStyle,
                    )

                    // Decrement Button (Bottom cap with cut bottom corners)
                    CyberNumberPickerStepButton(
                        iconRes = resolvedDecrementIcon,
                        contentDescription = "Decrement",
                        onClickLabel = "Decrement",
                        enabled = canDecrement,
                        shape = CutCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = size.cornerCut,
                            bottomEnd = size.cornerCut,
                        ),
                        width = size.verticalWidth,
                        height = size.verticalButtonHeight,
                        iconSize = size.iconSize,
                        color = color,
                        backgroundColor = backgroundColor,
                        borderColor = borderColor,
                        initialRepeatDelayMillis = initialRepeatDelayMillis,
                        repeatIntervalMillis = repeatIntervalMillis,
                        onStep = {
                            val next = (boundedValue - step).coerceIn(range.first, range.last)
                            if (next != boundedValue) onValueChange(next)
                        },
                    )
                }
            }
        }
    }
}

/**
 * Tactile step button with press scaling, neon highlight on press, and long-press auto-repeat.
 */
@Composable
private fun CyberNumberPickerStepButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClickLabel: String,
    enabled: Boolean,
    shape: Shape,
    width: Dp,
    height: Dp,
    iconSize: Dp,
    color: Color,
    backgroundColor: Color,
    borderColor: Color,
    initialRepeatDelayMillis: Long,
    repeatIntervalMillis: Long,
    onStep: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var didRepeat by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed, enabled) {
        if (isPressed && enabled) {
            didRepeat = false
            delay(initialRepeatDelayMillis)
            didRepeat = true
            while (isActive && enabled) {
                onStep()
                delay(repeatIntervalMillis)
            }
        } else {
            didRepeat = false
        }
    }

    val buttonAlpha = if (enabled) 1f else 0.35f
    val buttonBg = if (isPressed && enabled) color.copy(alpha = 0.22f) else backgroundColor
    val buttonStroke = if (isPressed && enabled) color else borderColor.copy(alpha = if (enabled) 0.6f else 0.25f)

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(width = width, height = height)
            .graphicsLayer {
                // Tactile 6% scale reduction on touch down
                scaleX = if (isPressed && enabled) 0.94f else 1f
                scaleY = if (isPressed && enabled) 0.94f else 1f
                alpha = buttonAlpha
            }
            .clip(shape)
            .background(buttonBg)
            .border(width = CyberPrimitives.BorderWidths.dp1, color = buttonStroke, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel = onClickLabel,
                onClick = {
                    if (!didRepeat) {
                        onStep()
                    }
                },
            )
            .semantics {
                this.contentDescription = contentDescription
                this.onClick(label = onClickLabel) {
                    onStep()
                    true
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        CyberIcon(
            iconRes = iconRes,
            contentDescription = null,
            size = iconSize,
            tint = if (enabled) color else color.copy(alpha = 0.35f),
        )
    }
}

/**
 * Centered HUD readout display featuring illuminated sci-fi corner bracket tick marks and animated digit roll.
 */
@Composable
private fun CyberNumberPickerReadout(
    value: Int,
    formatValue: (Int) -> String,
    prefix: String?,
    suffix: String?,
    minWidth: Dp,
    height: Dp,
    cornerCut: Dp,
    enabled: Boolean,
    color: Color,
    contentColor: Color,
    backgroundColor: Color,
    borderColor: Color,
    textStyle: TextStyle,
) {
    // Readout shape features subtle complementary chamfer
    val readoutShape = CutCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 0.dp)

    Box(
        modifier = Modifier
            .widthIn(min = minWidth)
            .height(height)
            .clip(readoutShape)
            .background(CyberTheme.colors.surfacePrimary)
            .border(
                width = CyberPrimitives.BorderWidths.dp1,
                color = borderColor.copy(alpha = if (enabled) 0.5f else 0.2f),
                shape = readoutShape,
            )
            .drawBehind {
                // Draw 4 corner HUD telemetry bracket marks
                val tickLength = (cornerCut / 1.5f).toPx()
                val tickStroke = CyberPrimitives.BorderWidths.dp1.toPx()
                val bracketColor = color.copy(alpha = if (enabled) 0.7f else 0.25f)

                // Top-Left bracket
                drawLine(bracketColor, Offset(0f, 0f), Offset(tickLength, 0f), tickStroke)
                drawLine(bracketColor, Offset(0f, 0f), Offset(0f, tickLength), tickStroke)

                // Top-Right bracket
                drawLine(bracketColor, Offset(size.width - tickLength, 0f), Offset(size.width, 0f), tickStroke)
                drawLine(bracketColor, Offset(size.width, 0f), Offset(size.width, tickLength), tickStroke)

                // Bottom-Left bracket
                drawLine(bracketColor, Offset(0f, size.height), Offset(tickLength, size.height), tickStroke)
                drawLine(bracketColor, Offset(0f, size.height - tickLength), Offset(0f, size.height), tickStroke)

                // Bottom-Right bracket
                drawLine(bracketColor, Offset(size.width - tickLength, size.height), Offset(size.width, size.height), tickStroke)
                drawLine(bracketColor, Offset(size.width, size.height - tickLength), Offset(size.width, size.height), tickStroke)
            }
            .padding(horizontal = CyberTheme.spacing.dp8),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (!prefix.isNullOrBlank()) {
                Text(
                    text = prefix,
                    style = CyberTheme.typography.terminal.copy(fontSize = (textStyle.fontSize.value * 0.75f).sp),
                    color = if (enabled) CyberTheme.colors.textSecondary else CyberTheme.colors.textSecondary.copy(alpha = 0.4f),
                )
                Spacer(modifier = Modifier.width(CyberTheme.spacing.dp2))
            }

            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    // Directional digit odometry transition: slides down when increasing, slides up when decreasing
                    if (targetState > initialState) {
                        (slideInVertically(animationSpec = tween(durationMillis = CyberPrimitives.Durations.ms150)) { h -> -h } +
                            fadeIn(animationSpec = tween(durationMillis = CyberPrimitives.Durations.ms150))).togetherWith(
                            slideOutVertically(animationSpec = tween(durationMillis = CyberPrimitives.Durations.ms150)) { h -> h } +
                                fadeOut(animationSpec = tween(durationMillis = CyberPrimitives.Durations.ms150)),
                        )
                    } else {
                        (slideInVertically(animationSpec = tween(durationMillis = CyberPrimitives.Durations.ms150)) { h -> h } +
                            fadeIn(animationSpec = tween(durationMillis = CyberPrimitives.Durations.ms150))).togetherWith(
                            slideOutVertically(animationSpec = tween(durationMillis = CyberPrimitives.Durations.ms150)) { h -> -h } +
                                fadeOut(animationSpec = tween(durationMillis = CyberPrimitives.Durations.ms150)),
                        )
                    }
                },
                label = "CyberNumberPickerValueRoll",
            ) { targetValue ->
                Text(
                    text = formatValue(targetValue),
                    style = textStyle,
                    color = if (enabled) contentColor else contentColor.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                )
            }

            if (!suffix.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(CyberTheme.spacing.dp2))
                Text(
                    text = suffix,
                    style = CyberTheme.typography.terminal.copy(fontSize = (textStyle.fontSize.value * 0.75f).sp),
                    color = if (enabled) CyberTheme.colors.textSecondary else CyberTheme.colors.textSecondary.copy(alpha = 0.4f),
                )
            }
        }
    }
}
