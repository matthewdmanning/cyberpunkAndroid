package com.example.sample.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.CyberBarbedWire
import com.example.cyberpunkandroid.effects.CyberBarcode
import com.example.cyberpunkandroid.effects.CyberBracketLock
import com.example.cyberpunkandroid.effects.CyberBraid
import com.example.cyberpunkandroid.effects.CyberChain
import com.example.cyberpunkandroid.effects.CyberChargeMeter
import com.example.cyberpunkandroid.effects.CyberCircuitTrace
import com.example.cyberpunkandroid.effects.CyberCometTracer
import com.example.cyberpunkandroid.effects.CyberCornerBrackets
import com.example.cyberpunkandroid.effects.CyberCornerCharge
import com.example.cyberpunkandroid.effects.CyberDrawOn
import com.example.cyberpunkandroid.effects.CyberGraduatedTicks
import com.example.cyberpunkandroid.effects.CyberHazardBand
import com.example.cyberpunkandroid.effects.CyberInteractionTrigger
import com.example.cyberpunkandroid.effects.CyberLiveWire
import com.example.cyberpunkandroid.effects.CyberPacketStream
import com.example.cyberpunkandroid.effects.CyberPathEffect
import com.example.cyberpunkandroid.effects.CyberScanner
import com.example.cyberpunkandroid.effects.CyberSequencedLights
import com.example.cyberpunkandroid.effects.cyberPathBorder
import com.example.cyberpunkandroid.effects.cyberPathDivider
import com.example.cyberpunkandroid.theme.CyberTheme

/** Demo entry: an effect on a chamfered card (and optionally a ring), with its loop duration. */
private data class PathDemo(
    val name: String,
    val description: String,
    val effect: CyberPathEffect,
    val loopMillis: Int = 2400,
    val ring: Boolean = true,
)

private val tracerDemos = listOf(
    PathDemo("CyberCometTracer", "Hot head, fading tail, running the outline.", CyberCometTracer(), 2600),
    PathDemo("CyberCometTracer ×3", "Three shorter comets sharing the loop.", CyberCometTracer(count = 3, tail = 0.12f), 3600),
    PathDemo("CyberCometTracer (stutter)", "Stepped motion with afterimages.", CyberCometTracer(stutter = 16, tail = 0.05f), 2400),
    PathDemo("CyberCornerCharge", "Light grows from every corner, closes, flashes.", CyberCornerCharge(), 2600),
    PathDemo("CyberDrawOn", "The outline traces itself from the middle of its longest side.", CyberDrawOn(), 2600),
    PathDemo("CyberLiveWire", "A jittering electric arc crawling the edge.", CyberLiveWire(), 3200),
    PathDemo("CyberPacketStream", "Three packet lanes at 1x / 2x / 3x speed.", CyberPacketStream(), 4200),
    PathDemo("CyberChargeMeter", "Segments power on in order; leading one flickers.", CyberChargeMeter(), 3000),
    PathDemo("CyberSequencedLights", "A lit chevron steps round with an afterglow.", CyberSequencedLights(), 2800),
    PathDemo("CyberBracketLock", "Brackets snap in, flash on lock, then breathe.", CyberBracketLock(), 2600),
)

private val patternDemos = listOf(
    PathDemo("CyberCornerBrackets", "Targeting frame on any shape (static).", CyberCornerBrackets()),
    PathDemo("CyberGraduatedTicks", "Instrument scale; a dial on a circle.", CyberGraduatedTicks(), 8000),
    PathDemo("CyberBarcode", "Seeded bar/space band.", CyberBarcode(), 6000),
    PathDemo("CyberHazardBand", "Slanted stripes following the border.", CyberHazardBand(), 1200),
    PathDemo("CyberBraid", "Woven sine strands between rails.", CyberBraid(), 3000),
    PathDemo("CyberBarbedWire", "Twisted strands with crossed barbs.", CyberBarbedWire(), 3000),
    PathDemo("CyberCircuitTrace", "PCB trace with vias and pads.", CyberCircuitTrace(), 3000),
    PathDemo("CyberChain", "Face and edge links, bent round corners.", CyberChain(), 2000),
)

private val dividerDemos = listOf(
    PathDemo("Scanner divider", "KITT-style sweep.", CyberScanner(), 1800),
    PathDemo("Comet divider", "Comet enters and exits the line.", CyberCometTracer(tail = 0.35f), 1600),
    PathDemo("Live-wire divider", "Arc crawling a divider.", CyberLiveWire(length = 0.35f), 1400),
    PathDemo("Packet divider", "Packets on a divider.", CyberPacketStream(), 3000),
)

/**
 * Showcase of the path-effect system: tracers and box patterns on borders, and tracers on dividers.
 * The last card only runs its tracer while pressed.
 */
@Composable
fun PathEffectsScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberTheme.colors.background)
            .padding(CyberPrimitives.Spacing.dp16),
        verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp16)
    ) {
        item { SectionTitle("TRACERS") }
        items(tracerDemos) { PathDemoCard(it) }
        item { PressDemo() }
        item { SectionTitle("DIVIDERS") }
        items(dividerDemos) { DividerDemo(it) }
        item { SectionTitle("BOX PATTERNS") }
        items(patternDemos) { PathDemoCard(it) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = CyberTheme.typography.display.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
        color = CyberTheme.colors.primary
    )
}

@Composable
private fun DemoLabel(demo: PathDemo) {
    Column(verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp4)) {
        Text(demo.name, style = CyberTheme.typography.terminal, color = CyberTheme.colors.primary)
        Text(demo.description, style = CyberTheme.typography.body.copy(fontSize = 13.sp), color = CyberTheme.colors.textSecondary)
    }
}

private fun loop(millis: Int) = infiniteRepeatable<Float>(tween(millis, easing = LinearEasing), RepeatMode.Restart)

@Composable
private fun PathDemoCard(demo: PathDemo) {
    val card: Shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp)
    Column(verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp8)) {
        DemoLabel(demo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(CyberTheme.colors.surfaceSecondary, card)
                .cyberPathBorder(demo.effect, shape = card, animationSpec = loop(demo.loopMillis)),
            contentAlignment = Alignment.Center
        ) {
            if (demo.ring) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(CyberTheme.colors.surfaceTertiary, CircleShape)
                        .cyberPathBorder(demo.effect, shape = CircleShape, animationSpec = loop(demo.loopMillis))
                )
            }
        }
    }
}

@Composable
private fun DividerDemo(demo: PathDemo) {
    Column(verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp8)) {
        DemoLabel(demo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .cyberPathDivider(demo.effect, horizontalInset = 8.dp, animationSpec = loop(demo.loopMillis))
        )
    }
}

/** Tracer that only runs while the card is pressed; eased so each lap surges and settles. */
@Composable
private fun PressDemo() {
    val interaction = remember { MutableInteractionSource() }
    val card: Shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp)
    Column(verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp8)) {
        DemoLabel(PathDemo("Press to run", "Comet on PRESS with hideWhenIdle and an eased lap.", CyberCometTracer()))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(CyberTheme.colors.surfaceSecondary, card)
                .clickable(interactionSource = interaction, indication = null) {}
                .cyberPathBorder(
                    CyberCometTracer(count = 2, tail = 0.2f),
                    shape = card,
                    trigger = CyberInteractionTrigger.PRESS,
                    interactionSource = interaction,
                    hideWhenIdle = true,
                    animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Restart),
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("HOLD TO ENGAGE", style = CyberTheme.typography.terminal, color = CyberTheme.colors.primary)
        }
    }
}
