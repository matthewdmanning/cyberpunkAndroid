package com.example.cyberpunkandroid.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Maps the three Cyber text styles onto all 15 Material 3 type-scale roles.
 *
 * Sizes, line heights and weights come from Material's own baseline [Typography] so the scale stays
 * standard. Each group then takes the look of its nearest Cyber style — three M3 roles line up exactly:
 * headlineLarge = [CyberTypography.display] (32sp), bodyLarge = [CyberTypography.body] (16sp),
 * labelLarge = [CyberTypography.terminal] (14sp).
 * - display* / headline*: display family, bold weight and wide tracking (big neon headings)
 * - title* / body*: body family and tracking, Material weights
 * - label*: terminal monospace family and tracking (buttons, chips and captions read as terminal text)
 */
fun cyberMaterialTypography(typography: CyberTypography = CyberTypography()): Typography {
    // Material 3 baseline type scale: source of every role's size and line height
    val base = Typography()

    fun TextStyle.asDisplay() = copy(
        fontFamily = typography.display.fontFamily,
        fontWeight = typography.display.fontWeight,
        letterSpacing = typography.display.letterSpacing
    )
    fun TextStyle.asBody() = copy(
        fontFamily = typography.body.fontFamily,
        letterSpacing = typography.body.letterSpacing
    )
    fun TextStyle.asTerminal() = copy(
        fontFamily = typography.terminal.fontFamily,
        letterSpacing = typography.terminal.letterSpacing
    )

    return Typography(
        displayLarge = base.displayLarge.asDisplay(),
        displayMedium = base.displayMedium.asDisplay(),
        displaySmall = base.displaySmall.asDisplay(),
        headlineLarge = base.headlineLarge.asDisplay(),
        headlineMedium = base.headlineMedium.asDisplay(),
        headlineSmall = base.headlineSmall.asDisplay(),
        titleLarge = base.titleLarge.asBody(),
        titleMedium = base.titleMedium.asBody(),
        titleSmall = base.titleSmall.asBody(),
        bodyLarge = base.bodyLarge.asBody(),
        bodyMedium = base.bodyMedium.asBody(),
        bodySmall = base.bodySmall.asBody(),
        labelLarge = base.labelLarge.asTerminal(),
        labelMedium = base.labelMedium.asTerminal(),
        labelSmall = base.labelSmall.asTerminal()
    )
}

/**
 * Maps the Cyber diagonal cut-corner style onto all 5 Material 3 shape roles: every shape has square
 * top-start and bottom-end corners and angled cuts on the top-end and bottom-start corners, like
 * [CyberShapes.cyberCutCornerShape].
 *
 * Cut sizes: small (8dp), medium (12dp) and extraLarge (20dp) match the existing Cyber shapes
 * (cyberCutCornerShapeSmall, cyberCutCornerShape, cardShape). extraSmall (4dp) and large (16dp) fill the
 * gaps and equal Material's own corner sizes for those roles.
 */
fun cyberMaterialShapes(): Shapes {
    fun diagonalCut(cut: Dp) = CutCornerShape(topStart = 0.dp, topEnd = cut, bottomEnd = 0.dp, bottomStart = cut)

    return Shapes(
        extraSmall = diagonalCut(4.dp),
        small = diagonalCut(8.dp),
        medium = diagonalCut(12.dp),
        large = diagonalCut(16.dp),
        extraLarge = diagonalCut(20.dp)
    )
}
