# Showcase Roadmap: NeonDeck Developer Experience

**Status:** Living Design & Implementation Plan  
**Target Module:** `:sample` (`NeonDeck`)  
**Scope Focus:** Modern Android (API 33+), Jetpack Compose, AGSL Runtime Shaders, and Tactile Cyberpunk Design Tokens.  
*(Note: Legacy pre-API 33 canvas fallbacks are explicitly out of scope and excluded from this showcase.)*

---

## 1. Executive Summary & Goals

The `:sample` app demonstrates the capabilities of the `:cyberpunkandroid` library to Android developers. It avoids synthetic, isolated component catalogs (grids of lone buttons or generic text fields) in favor of **cohesive, real-world cyberpunk equipment models**.

The showcase follows an **8-Card Complexity Spectrum** (2 cards per level):
- **Level 1: Clean Singular Effects (Utility)** — Static telemetry, minimal visual clutter, high readability, single-effect pairings.
- **Level 2: Tactile Linked State (Interactive)** — Primary touch controls (e.g. hold-to-charge, tap triggers) coupling state changes between multiple indicators.
- **Level 3: Multi-Variable Coupled Loops (Complex Reactive)** — 2+ inputs (e.g., switches, sliders) continuously driving mathematical simulation and multi-tier alert thresholds.
- **Level 4: Kinetic High-Impact Spectacle (Peak Immersion)** — Full-scale visual spectacles (AGSL overload, CRT shaders, traveling arc welds, 10x incandescent spark eruptions) paired with real tactile circuit actuators.

---

## 2. UI Container & Viewport Standards (Modern UI Guidelines)

To maintain imposing visual weight and avoid cramped "mail slot" containers, all showcase cards must adhere to the following Compose UI design rules:

### A. Hero Effect Chambers & Viewports
- **Minimum Height:** `140dp` – `180dp` for any container hosting active shaders, perimeter welds, or spark emitters.
- **Particle Throw Clearance:** `Modifier.cyberSpark` requires at least `60dp` of clear vertical clearance to arc, decay, and bloom without clipping into neighboring UI components.
- **Terminal & Cipher Buffers:** Minimum height of `72dp` – `96dp` with dedicated address headers (`0x00_ADDR`), monospace styling, and optical padding (`CyberTheme.spacing.dp12` to `dp16`).

### B. Tactile Controls & Touch Targets
- **Primary Actuator Dials:** Outer rims between `72dp` and `108dp`; inner capacitive buttons minimum `56dp` – `64dp` with `CyberTheme.spacing.dp16` perimeter breathing room.
- **Action Buttons:** Standardize on full-width `CyberButton(modifier = Modifier.fillMaxWidth(), size = CyberButtonSize.Medium)` for primary CTAs to prevent vertical word wrapping.
- **Slider Touch Channels:** Minimum `CyberTheme.spacing.dp8` to `dp12` vertical padding above and below `CyberSlider` to ensure comfortable thumb manipulation.

### C. Icon Hierarchy by Functional Importance
- **Hero Centerpiece Icons:** `CyberPrimitives.IconSizes.dp32` (Actuators, gauge cluster anchors).
- **Primary Action & Circuit Icons:** `CyberPrimitives.IconSizes.dp24` (CTA triggers, clamp disconnects, gauge turbines).
- **Inline Status Badges:** `CyberPrimitives.IconSizes.dp16` (Status tags, lock state dots).

---

## 3. Current Implementation Status (Cards 1–8)

| Card | Level | Base Physical Item | Visual & Interactive Mechanics | Status |
| :--- | :---: | :--- | :--- | :---: |
| **Telemetry Beacons** | 1 | Subdermal uplink beacon cluster | 3 hero beacons (`dp32`): Alert pulse, Sonar radar ping, Sync rotation. | **Live on Pixel 7** |
| **Synaptic Governor** | 2 | Reflex limiter bypass dial | 108dp dial, 64dp capacitive actuator, `cyberLongPressFill`, `Zap`/`Lock` state flip. | **Live on Pixel 7** |
| **Breach Cipher** | 2 | Neural jack-in port terminal | `CyberDecrypter` string descrambler, 8-segment `CyberProgress`, lock indicator. | **Live on Pixel 7** |
| **Cryo Regulator** | 3 | Dual-channel thermal governor | 5.8 GHz overclock switch + LN2 coolant slider driving coupled temperature on 92dp `CyberSectorRim` gauge. | **Live on Pixel 7** |
| **Conduit Siphon** | 4 | High-voltage tap & weld splice | Welded chamber with traveling arc (`cyberWeld`), 10x spark eruption (`cyberSpark`), full-width surge trigger, decoupling clamp. | **Live on Pixel 7** |
| **Mil-Spec Bus Diagnostic** | 1 | Optical hardware bus tap | 32 light-pipe compute-drift bank, steady firewall bar, segmented booster-current bar, rail readouts. Controls are `TODO`. | **Built; emulator only** |
| **ICE Detection Matrix** | 3 | Intrusion overwatch console | Display and Backbone tabs. Stepped depth slider + stealth switch drive the dossier overwatch equation; 20-block ICE bar; two-step purge. `f(tier)` and stealth efficiency are placeholders. | **Built; emulator only** |
| **CRT Biomonitor** | 4 | Dumpshock cold-jack console | Display and Backbone tabs. `cyberCrt` EKG trace, `cyberSpark` bezel corona in fibrillation, hold-to-pull cold-jack lever that swaps the CRT shader for `cyberOverload` while held. | **Built; emulator only** |

---

## 4. Card Design Notes (Cards 6–8)

Cards 6–8 are built on `feature/showcase-cards-6-8`. Design and review notes are in `.scratch/showcase-cards-6-8/` (`PROPOSAL.md`, `UX_REVIEW.md`). Level 3 and 4 cards use a Display tab (the lore surface) and a Backbone tab (the hidden equipment). The original plans follow.

### Card 6 — Level 1 (Card 2): Mil-Spec Hardware Bus Diagnostic
- **Source:** [`docs/inspirations/mil-spec-hardware-bus-diagnostic.md`](docs/inspirations/mil-spec-hardware-bus-diagnostic.md)
- **Base Physical Item:** Ruggedized avionics bus tap with optical multi-channel status LEDs.
- **Function:** Multi-channel passive hardware monitor displaying bus clock, line impedance, and parity status across 4 parallel data channels.
- **Styling:** Deep void card with discrete `CyberBadge` chips (nominal/standby/sync), fixed-width signal telemetry, and zero distraction shaders.

### Card 7 — Level 3 (Card 2): ICE Detection Matrix & Countermeasures
- **Source:** [`docs/inspirations/ice-detection-matrix-countermeasures.md`](docs/inspirations/ice-detection-matrix-countermeasures.md)
- **Base Physical Item:** Neural defensive grid matrix processor with automated ICE countermeasure valves.
- **Function:** Multi-variable defensive simulation where firewall depth, probe velocity, and ICE countermeasure throttle dynamically link 3 separate `CyberProgress` bars and trigger cascading alarm color shifts.
- **Styling:** Coupled slider and switch controls, multi-tier threat level badge (`BLACK ICE` / `INTRUSION TRACE` / `HARDENED`), and synchronized perimeter glow warning pulses.

### Card 8 — Level 4 (Card 2): CRT Biomonitor // Dumpshock Cold-Jack Console
- **Source:** [`docs/inspirations/crt-biomonitor-dumpshock-cold-jack-console.md`](docs/inspirations/crt-biomonitor-dumpshock-cold-jack-console.md)
- **Base Physical Item:** Trauma-rated field biomonitor console with manual forced-ejection cold-jack lever.
- **Function:** High-spectacle terminal running active `Modifier.cyberCrt` barrel curvature and scanlines; holding emergency disconnect lever triggers a severe optical dumpshock glitch (`Modifier.cyberOverload`), resetting vital signs into stabilization mode.
- **Styling:** Curved CRT monitor bezel, phosphor green/amber telemetry readout, chromatic aberration jitter on critical disconnect.

---

## 5. Showcase Shell Strategy

### Current Mode
- **Progressive Single-Feed Scroll:** All cards render in a vertically scrollable list on `MainActivity.kt`, allowing rapid physical verification and end-to-end audit on Google Pixel 7.

### Future Transition (Post-Review)
- Once all 8 cards are approved and container sizing is expanded, the app will transition into the structured 3-tab **NeonDeck OS** architecture:
  - `[TACTICAL / HUD]` — Level 4 spectacle cards (Conduit Siphon, CRT Biomonitor).
  - `[OPERATIONS]` — Level 2 & 3 interactive cards (Breach Cipher, Cryo Regulator, ICE Matrix, Synaptic Governor).
  - `[SUBSYSTEMS]` — Level 1 utility cards (Telemetry Beacons, Mil-Spec Bus Diagnostic).
