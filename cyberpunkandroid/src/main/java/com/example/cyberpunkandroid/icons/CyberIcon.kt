package com.example.cyberpunkandroid.icons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.res.vectorResource
import com.example.cyberpunkandroid.config.CyberConfig
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Vector iconography component supporting multiple sci-fi visual rendering variants.
 *
 * ### Rendering Variants:
 * - [CyberIconVariant.Outline]: Standard geometric outline stroke rendering.
 * - [CyberIconVariant.Solid]: Dynamically converts vector stroke paths into filled solid geometry.
 * - [CyberIconVariant.Duotone]: Dual-layer rendering combining a translucent fill wash under an outline stroke.
 * - [CyberIconVariant.Overload]: Multi-layer chromatic aberration effect with offset cyan and magenta ghost channels.
 *
 * @param iconRes Android drawable vector asset resource ID.
 * @param contentDescription Accessibility description for screen readers.
 * @param modifier Composable modifier applied to the icon.
 * @param tint Primary vector stroke or fill tint color. Defaults to [androidx.compose.material3.LocalContentColor].
 * @param variant Visual rendering mode ([CyberIconVariant.Outline], [CyberIconVariant.Solid],
 *   [CyberIconVariant.Duotone], [CyberIconVariant.Overload]). Defaults to [CyberIconVariant.Outline].
 */
@Composable
fun CyberIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = com.example.cyberpunkandroid.config.CyberPrimitives.IconSizes.dp24,
    tint: Color = LocalContentColor.current,
    variant: CyberIconVariant = CyberIconVariant.Outline
) {
    val baseVector = ImageVector.vectorResource(id = iconRes)
    val iconModifier = modifier.size(size)
    
    when (variant) {
        CyberIconVariant.Outline -> {
            Icon(
                imageVector = baseVector,
                contentDescription = contentDescription,
                modifier = iconModifier,
                tint = tint
            )
        }
        CyberIconVariant.Solid -> {
            val solidVector = remember(baseVector) {
                baseVector.toSolid()
            }
            Icon(
                imageVector = solidVector,
                contentDescription = contentDescription,
                modifier = iconModifier,
                tint = tint
            )
        }
        CyberIconVariant.Duotone -> {
            val solidVector = remember(baseVector) {
                baseVector.toSolid()
            }
            Box(modifier = iconModifier) {
                Icon(
                    imageVector = solidVector,
                    contentDescription = null,
                    tint = tint.copy(alpha = CyberConfig.Icon.DuotoneAlpha),
                    modifier = Modifier.matchParentSize()
                )
                Icon(
                    imageVector = baseVector,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
        CyberIconVariant.Overload -> {
            // Overload: Dual-layer RGB offset rendering
            val primaryShift = CyberTheme.colors.primary
            val secondaryShift = CyberTheme.colors.secondary
            Box(modifier = iconModifier) {
                // Cyan shift layer (left/down)
                Icon(
                    imageVector = baseVector,
                    contentDescription = null,
                    tint = primaryShift,
                    modifier = Modifier.matchParentSize().graphicsLayer {
                        translationX = -CyberConfig.Icon.OverloadOffset
                        translationY = CyberConfig.Icon.OverloadOffset
                        alpha = CyberConfig.Icon.OverloadAlpha
                    }
                )
                // Magenta shift layer (right/up)
                Icon(
                    imageVector = baseVector,
                    contentDescription = null,
                    tint = secondaryShift,
                    modifier = Modifier.matchParentSize().graphicsLayer {
                        translationX = CyberConfig.Icon.OverloadOffset
                        translationY = -CyberConfig.Icon.OverloadOffset
                        alpha = CyberConfig.Icon.OverloadAlpha
                    }
                )
                // Base layer
                Icon(
                    imageVector = baseVector,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

private fun ImageVector.toSolid(): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = defaultWidth,
        defaultHeight = defaultHeight,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        tintColor = tintColor,
        tintBlendMode = tintBlendMode
    )
    
    fun traverse(node: VectorNode) {
        when (node) {
            is VectorGroup -> {
                builder.addGroup(
                    name = node.name,
                    rotate = node.rotation,
                    pivotX = node.pivotX,
                    pivotY = node.pivotY,
                    scaleX = node.scaleX,
                    scaleY = node.scaleY,
                    translationX = node.translationX,
                    translationY = node.translationY,
                    clipPathData = node.clipPathData
                )
                for (child in node) {
                    traverse(child)
                }
                builder.clearGroup()
            }
            is VectorPath -> {
                builder.addPath(
                    pathData = node.pathData,
                    pathFillType = node.pathFillType,
                    name = node.name,
                    fill = SolidColor(Color.White), // Fill with white (will be tinted)
                    fillAlpha = node.fillAlpha,
                    stroke = null, // Remove stroke
                    strokeAlpha = node.strokeAlpha,
                    strokeLineWidth = 0f,
                    strokeLineCap = node.strokeLineCap,
                    strokeLineJoin = node.strokeLineJoin,
                    strokeLineMiter = node.strokeLineMiter,
                    trimPathStart = node.trimPathStart,
                    trimPathEnd = node.trimPathEnd,
                    trimPathOffset = node.trimPathOffset
                )
            }
        }
    }
    
    for (node in root) {
        traverse(node)
    }
    
    return builder.build()
}
