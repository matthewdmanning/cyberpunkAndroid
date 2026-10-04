package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch

class CyberStripesElement(
    val color: Color,
    val stripeWidth: Dp,
    val speed: Float,
    val animationSpec: InfiniteRepeatableSpec<Float>
) : ModifierNodeElement<CyberStripesNode>() {
    override fun create(): CyberStripesNode {
        return CyberStripesNode(color, stripeWidth, speed, animationSpec)
    }

    override fun update(node: CyberStripesNode) {
        node.color = color
        node.stripeWidth = stripeWidth
        node.speed = speed
        node.animationSpec = animationSpec
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberStripes"
        properties["color"] = color
        properties["stripeWidth"] = stripeWidth
        properties["speed"] = speed
        properties["animationSpec"] = animationSpec
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CyberStripesElement) return false
        return color == other.color &&
               stripeWidth == other.stripeWidth &&
               speed == other.speed &&
               animationSpec == other.animationSpec
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + stripeWidth.hashCode()
        result = 31 * result + speed.hashCode()
        result = 31 * result + animationSpec.hashCode()
        return result
    }
}

class CyberStripesNode(
    var color: Color,
    var stripeWidth: Dp,
    var speed: Float,
    var animationSpec: InfiniteRepeatableSpec<Float>
) : Modifier.Node(), DrawModifierNode {

    private val phase = Animatable(0f)

    override fun onAttach() {
        super.onAttach()
        coroutineScope.launch {
            phase.animateTo(
                targetValue = 1f,
                animationSpec = animationSpec
            )
        }
    }

    override fun ContentDrawScope.draw() {
        val widthPx = stripeWidth.toPx()
        
        onDrawWithContent {
            clipRect {
                val diagonalLength = size.width + size.height
                val numStripes = (diagonalLength / (widthPx * 2)).toInt() + 2
                val shift = phase.value * widthPx * 2

                for (i in -1..numStripes) {
                    val offset = i * widthPx * 2 + shift
                    val start = Offset(offset - size.height, size.height)
                    val end = Offset(offset, 0f)
                    drawLine(
                        color = color,
                        start = start,
                        end = end,
                        strokeWidth = widthPx
                    )
                }
            }
            drawContent()
        }
    }
    
    private inline fun ContentDrawScope.onDrawWithContent(block: ContentDrawScope.() -> Unit) {
        block()
    }
}
