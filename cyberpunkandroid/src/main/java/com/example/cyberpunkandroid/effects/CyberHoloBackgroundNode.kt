package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.LocalCyberColors
import kotlinx.coroutines.launch

class CyberHoloBackgroundElement(
    val animationSpec: InfiniteRepeatableSpec<Float>
) : ModifierNodeElement<CyberHoloBackgroundNode>() {
    override fun create(): CyberHoloBackgroundNode = CyberHoloBackgroundNode(animationSpec)

    override fun update(node: CyberHoloBackgroundNode) {
        node.animationSpec = animationSpec
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "cyberHoloBackground"
        properties["animationSpec"] = animationSpec
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CyberHoloBackgroundElement) return false
        return animationSpec == other.animationSpec
    }

    override fun hashCode(): Int = animationSpec.hashCode()
}

class CyberHoloBackgroundNode(
    var animationSpec: InfiniteRepeatableSpec<Float>
) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {
    
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
        val bg = currentValueOf(LocalCyberColors).secondary
        val c1 = CyberPrimitives.Colors.Cyan500.copy(alpha = 0.3f)
        val c2 = CyberPrimitives.Colors.Magenta500.copy(alpha = 0.3f)
        val c3 = CyberPrimitives.Colors.Green500.copy(alpha = 0.3f)
        
        val sweep = Brush.sweepGradient(
            0.0f to bg,
            0.25f to c1,
            0.5f to c2,
            0.75f to c3,
            1.0f to bg,
            center = Offset(size.width / 2, size.height / 2)
        )
        
        rotate(phase.value * 360f) {
            drawRect(
                brush = sweep,
                size = size.copy(width = size.width * 2, height = size.height * 2),
                topLeft = Offset(-size.width / 2, -size.height / 2)
            )
        }
        drawContent()
    }
}
