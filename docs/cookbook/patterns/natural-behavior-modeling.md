# Pattern: Natural Behavior Modeling & Transient Dynamics

## Pattern Overview

High-fidelity cybernetic and industrial interfaces feel authentic when their digital behavior reflects the physical laws of the simulated hardware. A common antipattern in UI engineering is applying sudden, jarring visual snaps to physical phenomena (such as snapping temperature color from cyan to red in 0 milliseconds, or toggling glitch shaders on/off with an instantaneous boolean).

In the physical world, thermal dissipation, fluid dynamics, turbine spooling, and electrical breakdown are **gradual, continuous, and mass-damped**. Connecting digital component behavior with natural physical behavior anchors user intuition and elevates visual polish.

---

## The Natural Behavior Design Framework

When designing or animating any component, ask two fundamental questions:
1. **How does it work in the real world?**
   - What are the physical inputs, forces, and constraints (thermal mass, inertia, friction, fluid flow)?
   - How does energy enter and leave the system?
2. **How can the parameters reflect that behavior?**
   - How do control states (switches, sliders, buttons) map to dynamic physical equations?
   - How can transitions animate with realistic latency, momentum, and continuity?

---

## Core Principles

### 1. Thermal Mass & Inertia
Physical objects cannot change temperature instantaneously. Thermal mass introduces lag: heating takes time as thermal energy ($Q_{in}$) exceeds dissipation ($Q_{out}$), and cooling takes time as heat conducts into cryogenic coolant.
- **Rule:** Never snap numerical values or visual states instantly when a load switch or coolant valve changes.
- **Compose API:** Model thermal inertia using `animateFloatAsState` with physics-appropriate easing (`FastOutSlowInEasing` or damping specs) across 1.5s–3.0s.

### 2. Continuous Radiance (Blackbody Spectrum)
Thermal radiation follows a continuous analog spectrum (blackbody radiation):
- **Cryogenic / Baseline (20°C–40°C):** Deep cryogenic cyan.
- **Thermal Flux / Warmup (40°C–65°C):** Smoothly transitions through amber-gold.
- **Redline / Thermal Runaway (65°C–96°C+):** Melts into incandescent orange and burning crimson.
- **Rule:** Never use discrete `when { temp > 75 -> Danger; else -> Primary }` threshold jumps. Use continuous color interpolation (`lerp`) along the thermal gradient.

### 3. Mechanical Coupling (Velocity vs Flow Rate)
Moving mechanical parts (pumps, turbine impellers, centrifugal fans) have variable angular velocity governed by input throttle.
- **Rule:** Tie rotation period directly to flow rate or throttle. Avoid fixed-rate looping animations (`cyberIconSpin()`) on variable-rate mechanisms.
- **Implementation:** Scale `durationMillis` inversely with input: from 2200ms (idle trickle) down to 220ms (screaming 36,000 RPM cryo flood).

### 4. Proportional Stress & Breakdown
Electronic degradation (dielectric breakdown, chromatic jitter, CRT distortion) is an analog curve that scales with overload.
- **Rule:** Do not toggle shaders with binary booleans (`intensity = 0.04f` on/off). Modulate `intensity` proportionally as a function of thermal stress: `val stress = ((temp - 55f) / 35f).coerceIn(0f, 1f)`.

---

## Canonical Assembly: The Thermodynamic Cryo-Manifold

```kotlin
@Composable
fun ThermodynamicCryoManifold(
    isOverclocked: Boolean,
    coolantRate: Float,
    modifier: Modifier = Modifier
) {
    // 1. Thermodynamic Equilibrium Calculation:
    // Base 3.2 GHz: 37°C. Overclock (5.8 GHz) adds 52°C. Coolant extracts up to 48°C.
    val targetTemp = (37f + (if (isOverclocked) 52f else 0f) - (coolantRate * 48f)).coerceIn(24f, 96f)

    // 2. Thermal Mass & Inertia (Gradual Rise & Fall):
    val animatedTemp by animateFloatAsState(
        targetValue = targetTemp,
        animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
        label = "ThermalInertia"
    )

    // 3. Continuous Radiance Spectrum (Smooth Blackbody lerp):
    val thermalFraction = ((animatedTemp - 24f) / (96f - 24f)).coerceIn(0f, 1f)
    val tempColor = if (thermalFraction < 0.45f) {
        lerp(CyberTheme.colors.primary, CyberTheme.semantics.colors.warning, thermalFraction / 0.45f)
    } else {
        lerp(CyberTheme.semantics.colors.warning, CyberTheme.semantics.colors.danger, (thermalFraction - 0.45f) / 0.55f)
    }

    // 4. Proportional Thermal Stress (Gradual Jitter Escalation):
    val thermalStress = ((animatedTemp - 55f) / 35f).coerceIn(0f, 1f)
    val overloadIntensity = thermalStress * 0.08f

    // 5. Mechanical Impeller Coupling (Turbine RPM tied to pump flow rate):
    val turbineCycleMs = (2200 - (coolantRate * 1980)).toInt().coerceIn(220, 2400)
    val turbineTransition = rememberInfiniteTransition(label = "TurbineSpool")
    val turbineRotation by turbineTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = turbineCycleMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ImpellerRotation"
    )

    // Gauge Display Assembly
    Box(
        modifier = modifier
            .size(96.dp)
            .cyberOverload(enabled = thermalStress > 0.05f, intensity = overloadIntensity),
        contentAlignment = Alignment.Center
    ) {
        CyberSectorRim(
            size = 96.dp,
            color = tempColor,
            sectorAngles = listOf(70f, 70f, 70f, 70f),
            thickness = if (thermalStress > 0.6f) CyberPrimitives.BorderWidths.dp4 else CyberPrimitives.BorderWidths.dp2
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${animatedTemp.toInt()}°C",
                style = CyberTheme.typography.terminal,
                color = tempColor
            )
            CyberIcon(
                iconRes = CyberIcons.Sync,
                contentDescription = "Coolant Pump Impeller",
                size = CyberPrimitives.IconSizes.dp24,
                tint = tempColor,
                modifier = Modifier.graphicsLayer { rotationZ = turbineRotation }
            )
        }
    }
}
```
