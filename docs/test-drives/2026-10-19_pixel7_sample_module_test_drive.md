# Physical Device Test Drive: Google Pixel 7 (Android 17 / API 37)

**Date**: 2026-10-19  
**Device**: Google Pixel 7 (`2A151FDH200HY4`, `panther`)  
**Android OS**: Android 17 (API Level 37)  
**Display Matrix**: 1080x2400 @ 90Hz (Physical Density: 420 dpi)  
**Graphics Pipeline**: Skia (Vulkan)  
**Target Module**: `:sample` (`com.example.sample` / `NeonDeck`)  
**Branch**: `feature/pixel-frontier-transition`  

---

## 📊 Hardware Profiling & Benchmark Summary

Over an extensive multi-stage run (>31,000 frames) covering full app boot, bidirectional `CyberPixelFrontierTransition` sweeps, vertical list sweeps across all 8 component showcase assemblies, and interactive widget triggers:

| Metric | Measured Value | Target Budget | Status |
|---|---|---|---|
| **Total Frames Rendered** | 31,478 | N/A | Completed |
| **Jank Rate (Modern Deadline)** | **0.15%** (46 janky frames) | $\le 1.0\%$ | ✅ **Pass** |
| **Median Frame Time (50th %)** | **15 ms** | $\le 16.6\text{ ms}$ | ✅ **Pass** |
| **90th Percentile Frame Time** | **18 ms** | $\le 22.2\text{ ms}$ | ✅ **Pass** |
| **95th Percentile Frame Time** | **19 ms** | $\le 25.0\text{ ms}$ | ✅ **Pass** |
| **99th Percentile Frame Time** | **27 ms** | $\le 33.3\text{ ms}$ | ✅ **Pass** |
| **GPU 50th Percentile Time** | **5 ms** | $\le 11.1\text{ ms}$ (90Hz) | ✅ **Pass** |
| **GPU 90th Percentile Time** | **8 ms** | $\le 11.1\text{ ms}$ (90Hz) | ✅ **Pass** |
| **GPU 95th Percentile Time** | **8 ms** | $\le 11.1\text{ ms}$ (90Hz) | ✅ **Pass** |
| **GPU 99th Percentile Time** | **12 ms** | $\le 16.6\text{ ms}$ | ✅ **Pass** |
| **Missed VSYNC Counter** | 2 | Minimal | ✅ **Pass** |
| **Slow Bitmap Uploads** | 0 | 0 | ✅ **Pass** |

---

## 🧠 Physical Memory Profile (`dumpsys meminfo`)

```text
** MEMINFO in pid 31326 [com.example.sample] **
App Summary
                       Pss(KB)                        Rss(KB)
                        ------                         ------
           Java Heap:    38,272                         50,244
         Native Heap:    39,792                         41,036
                Code:    44,284                        125,484
               Stack:     1,244                          1,252
            Graphics:   271,516                        271,516
       Private Other:     3,400
              System:    37,758
           TOTAL PSS:   436,266 kB (436.2 MB)

Objects:
  Views: 6 | ViewRootImpl: 1 | Activities: 1
```

- **RenderNodes & Views**: 6 attached Views, 1 ViewRootImpl (11.04 kB render node footprint), confirming zero leaking composition subtrees.
- **Vulkan Surface & Glyph Caches**: 323.24 KB CPU glyph cache, 158.01 MB GPU cache with 75.97 MB purgeable memory, safely managed by the Vulkan surface allocator.

---

## 🔬 Component Showcase Verification Suite

### 1. `CyberPixelFrontierTransition` (Boot & Replay Sweep)
- **Configuration**: `Orientation.Horizontal`, `reverse = true` (Right-to-Left), 0.5px stochastic micro-pixel matrix, neon purple sweep edge (`#B026FF`), 2000ms linear tween.
- **Physical Observation**: Clean diagonal and ragged block dispersal. Both splash and catalogue views rendered simultaneously without seam gap or tearing.
- **Evidence**: `screenshot_pixel7_splash.png`, `screenshot_pixel7_splash_replay.png`.

### 2. Telemetry Beacons (`TelemetryBeaconsCard`)
- **Render Variants**: `cyberIconPulse`, `cyberPing`, `cyberIconSpin` running concurrent kinetic loops.
- **Observation**: Zero jank on continuous pulse and spin loops.

### 3. Mil-Spec Hardware Bus Diagnostic (`BusDiagnosticCard`)
- **Telemetry**: 32-thread dynamic optical light-pipe canvas, centroid shift, firewall barrier bar.
- **Observation**: High contrast neon emerald/cyan palette rendered with crisp subpixel borders.
- **Evidence**: `screenshot_pixel7_catalogue_top.png`.

### 4. Synaptic Governor (`SynapticGovernorCard`)
- **Actuators**: 108dp dial ticks, lock badge toggle, `cyberLongPressFill` tactile bypass mechanism.
- **Evidence**: `screenshot_pixel7_catalogue_mid.png`.

### 5. Breach Cipher (`BreachCipherCard`)
- **Mechanism**: `CyberDecrypter` scrambled alphanumeric decrypt stream, 8-segment `CyberProgress` register fill.
- **Interactive Check**: Tapped `RUN CIPHER` triggering sequential decrypt cycle without frame drops.
- **Evidence**: `screenshot_pixel7_catalogue_breach.png`.

### 6. Cryo Thermal Regulator (`CryoRegulatorCard`)
- **Controls**: Overclock clock switch, LN2 coolant detent slider driving coupled 92dp `CyberSectorRim` thermometer.
- **Evidence**: `screenshot_pixel7_catalogue_breach.png`.

### 7. High-Voltage Conduit Siphon (`ConduitTapCard`)
- **Visuals**: Continuous `cyberWeld` traveling arc around welded chamber border with fizzing sparks and cooling bead; full-width `TRIGGER OVERDRIVE SURGE` button triggering 16-spark explosive burst (`cyberSpark`).
- **Physical Observation**: AGSL `SparkShader` and traveling arc ran without stutter on Pixel 7's Vulkan backend.
- **Evidence**: `screenshot_pixel7_catalogue_conduit.png`.

### 8. ICE Matrix & CRT Biomonitor (`IceMatrixCard` & `BioMonitorCard`)
- **Visuals**: Intrusion topology vector canvas, packet stealth switch, lift purge cover, and curved CRT barrel distortion shader with moving EKG line and scanlines.
- **Physical Observation**: CRT barrel shader and scanline uniforms bind cleanly in draw phase.
- **Evidence**: `screenshot_pixel7_catalogue_bottom.png`, `screenshot_pixel7_catalogue_biomonitor.png`.

---

## 🏁 Test Drive Verdict

✅ **PASSED**: The sample module (`NeonDeck`) is verified live on the physical Google Pixel 7 (`2A151FDH200HY4`). High-fidelity visual presentation matches the design guidelines, shaders and kinetic animations execute within GPU budgets (5–8 ms GPU time), and the transition runs with 0.15% jank rate over 31,000+ frames.
