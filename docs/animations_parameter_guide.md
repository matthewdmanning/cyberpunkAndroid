# Animations Parameter Guide

| Animation Effect | Standard Spec (Non-Intrusive) | Sci-Fi / Cyber Variant | Key Adjustments (What Changes) |
| --- | --- | --- | --- |
| **Bouncing & Snapping** | **Speed:** 200–400 ms<br><br>**Timing:** Soft spring (k=200, c=15)<br><br>**Distance:** 4–12 dp translation<br><br>**Dropoff:** Smooth 2–3 cycle exponential decay | **Speed:** 120–250 ms<br><br>**Timing:** Stiff mechanical spring (k=450, c=10)<br><br>**Distance:** 8–16 dp with overshoot<br><br>**Dropoff:** Harsh oscillation stop (1–2 sharp rebounds) | • **Faster & stiffer:** Cuts duration nearly in half.<br><br>• **Hard snap:** Higher spring tension gives a robotic, tactile lock-in rather than a soft organic bounce. |
| **Pulsing & Breathing** | **Speed:** 1200–2400 ms cycle<br><br>**Timing:** Smooth sine curve<br><br>**Scale:** 1.00× to 1.08×<br><br>**Dropoff:** Opacity fades down to 0.3–0.0 | **Speed:** 600–1200 ms cycle<br><br>**Timing:** Sharp Ease-In-Out or \steps(4)\<br><br>**Scale:** 1.00× to 1.15× with outline expansion<br><br>**Dropoff:** Opacity drops to core, leaving a sharp wireframe border | • **Higher energy:** Double-time frequency.<br><br>• **Wireframe persistence:** Instead of completely fading out, the outer edge retains a crisp, high-contrast stroke. |
| **Glowing & Bloom** | **Speed:** 1000–2000 ms (Ambient)<br><br>**Timing:** Soft ease-out<br><br>**Distance:** 2–12 dp spread<br><br>**Dropoff:** Single soft Gaussian shadow blur (4–16 dp radius) | **Speed:** 400–800 ms (State reactive)<br><br>**Timing:** Rapid attack curve<br><br>**Distance:** 4–20 dp bloom spread<br><br>**Dropoff:** **Dual-layer bloom:** Core vector stroke (90% opacity, 2 dp blur) + ambient field (30% opacity, 20 dp blur) | • **Layered light:** Switches from a subtle background shadow to a high-intensity laser bloom.<br><br>• **High contrast:** Core stroke stays pin-sharp while light bleeds outward. |
| **Scan Lines, Shimmer & Sweeps** | **Speed:** 1200–1800 ms sweep<br><br>**Timing:** Soft ease (\cubic-bezier(0.4, 0, 0.6, 1)\)<br><br>**Angle:** 15–20° subtle gradient<br><br>**Dropoff:** Wide, faint highlight band (0% → 25% → 0% across 35% width) | **Speed:** 400–800 ms sweep<br><br>**Timing:** High-speed linear or CRT refresh pass<br><br>**Angle:** 0° (Horizontal) or 45° laser slice<br><br>**Dropoff:** Narrow, ultra-bright gradient band (0% → 80% peak brightness → 0% across 8% width) | • **Higher speed & contrast:** Moves 2–3× faster.<br><br>• **Narrow beam:** Fades the wide skeleton "shimmer" into a precise, high-intensity laser line pass. |
| **Movement, Opacity & Transitions** | **Speed:** 200–350 ms<br><br>**Timing:** Standard Ease-Out / Ease-In<br><br>**Distance:** 8–24 dp translation<br><br>**Dropoff:** Smooth alpha fade (0.0 ↔ 1.0) | **Speed:** 80–150 ms<br><br>**Timing:** **Glitch & Frame-Skip:** \steps(3, end)\ + RGB offset (2–4 dp)<br><br>**Distance:** 4–12 dp horizontal jitter<br><br>**Dropoff:** Strobe flicker alpha (1.0 → 0.1 → 0.85 → 1.0) | • **Discontinuous motion:** Replaces fluid spatial slides with frame skips, vector stroke building, or rapid RGB channel splits. |

---

### Core Rules for Blending Sci-Fi & Usability

#### 1. Keep Structure Standard, Style Sci-Fi

* **Spatial Layout:** Keep standard touch targets and layout distances. A primary button is still a primary button, but its feedback loop shifts from a soft scale-up to a wireframe lock or crisp vector outline draw.
* **Timing Hierarchy:** Functional feedback (button taps, state changes) must remain under **250 ms**. Reserve dramatic glitch or scanline effects for screen transitions, modal opens, or idle states so the UI stays responsive.

#### 2. The Sci-Fi Motion Spectrum

Use this formula to dial the aesthetic up or down without breaking app usability:

**Sci-Fi Dynamics = Stiffer Springs + Layered Bloom + Frame-Skipping**

* **Low Sci-Fi (Minimalist Utility):** Standard durations, standard smooth curves, but add **dual-layer neon bloom** and sharp high-contrast border strokes.
* **Medium Sci-Fi (Cyberpunk HUD):** Increase spring stiffness, drop durations by ~30%, shorten scanline band width, and use step-based transitions for data updates.
* **High Sci-Fi (Full Immersive/Game UI):** Add 40–80 ms chromatic aberration (RGB splits) on press, frame-skip exits, and wireframe stroke animations on entry.
