# NeonDeck: Showcase Strategy & Architectural Blueprint

**Status:** Showcase Architecture & Experience Design  
**Target:** `:sample` module (`NeonDeck`) for `:cyberpunkandroid`  
**Core Goal:** Impress the user by demonstrating what the UI library feels like in real production-level apps, across a full spectrum of visual complexity.

---

## 1. Why Showcases Fail vs. Why They Impress

### The "Encyclopedia" Trap (What to Avoid)
Most design system sample apps fail to impress because they treat the app like an interactive API reference:
- Grids of lonely buttons with "knobs" (sliders to change corner radius, toggles for boolean flags).
- Separate screens for "Buttons", "Text Fields", "Checkboxes", "Badges".
- Unrealistic dummy text (`"Lorem Ipsum"`, `"Button 1"`).
- Synthetic test toggles (e.g. fallback simulators, raw token swatches).

**Result:** It feels like QA software, not a product. It fails to show why someone would choose this library for their next app.

### The Production Showcase (What Impresses)
Great UI showcases (e.g. *Jetsnack*, *Reflect*, *Linear Mobile*, high-end creative decks) demonstrate **the art of the possible** in real product contexts:
1. **Showcases Whole Experiences, Not Isolated Widgets**: Components appear in their natural habitat, interacting with one another.
2. **Shows a Spectrum of Complexity**: Not everything in a real app is glowing at 100% intensity with scanlines and CRT distortion. A real production app balances clean, legible utility screens with dense, high-impact tactical dashboards.
3. **Tactile & Responsive**: Every surface reacts to touch, swipe, hold, and drag. Animations feel snappy, purposeful, and alive.
4. **Authentic Domain Storytelling**: Use evocative, grounded cyberpunk/netrunner themes rather than generic wireframes.

---

## 2. The 3-Tier Production Complexity Spectrum

The showcase must prove that `:cyberpunkandroid` can power an entire production application across its full range of needs:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        COMPLEXITY SPECTRUM                             │
├───────────────────┬────────────────────────────┬───────────────────────┤
│ TIER 1: UTILITY   │ TIER 2: BALANCED PRODUCT   │ TIER 3: PEAK IMMERSION│
│ Clean & Legible   │ Tactile & Structured       │ Maximalist & Kinetic  │
├───────────────────┼────────────────────────────┼───────────────────────┤
│ • Settings & Prefs│ • Inventory / Market Deck  │ • Netrunner Terminal  │
│ • Identity Profile│ • Mission / Job Board      │ • Biometric Breach HUD│
│ • Comms / Inbox   │ • System Telemetry Cards   │ • Tactical Diagnostics│
│                   │                            │                       │
│ Minimal decoration│ Balanced borders & glows   │ Active shaders (CRT,  │
│ High readability  │ Dynamic state feedback     │ scanlines, overload), │
│ Crisp form inputs │ Grouped card workflows     │ particles, scramblers │
└───────────────────┴────────────────────────────┴───────────────────────┘
```

---

### Tier 1: Clean Utility (Subtle, Highly Functional)
**Production Target:** Settings, Profile, Account, System Preferences, Clean Messaging.  
**Purpose:** Prove that the library can be sophisticated, elegant, and non-distracting for everyday utility tasks without visual clutter or legibility loss.

* **Visual Profile:**
  - Flat deep-void surfaces (`Void500`, `Void400`).
  - Subtle cut-corner framing (`cyberCutCornerShapeSmall`).
  - Restrained border strokes (`border` token); no heavy shader distortion or full-screen scanlines.
  - High-contrast typography optimized for rapid scanning (`body`, `terminal`).
* **Components in Action:**
  - `CyberTextField` & `CyberTextArea` (clean labels, focus tint).
  - `CyberSwitch` & `CyberCheckbox` (smooth toggling, crisp geometry).
  - `CyberDropdown` (clean popover selection).
  - `CyberNavLink` & `CyberBadge` (discrete status tags).
* **Sample Screen: "Agent Profile & Subsystem Config"**
  - Identity card with callsign, neural link status badge (`ONLINE`).
  - Clean settings toggles (Audio feedback, Haptics, Auto-handshake).
  - Encrypted note pad (`CyberTextArea`) with character count and clean cut-corner bounds.

---

### Tier 2: Balanced Product (Tactile, Structured, Interactive)
**Production Target:** Operations Deck, Marketplace / Inventory, Mission Dispatch, Activity Log.  
**Purpose:** Demonstrate the day-to-day sweet spot of production apps: rich card layouts, interactive controls, live progress indicators, and tactile press reactions.

* **Visual Profile:**
  - Layered surface tiers (`surfaceSecondary` → `surfaceElevated`).
  - Cut-corner hero containers (`cardShape`).
  - Focused neon accents: glowing borders on press/hover (`cyberGlowBorder`), status-tinted borders.
  - Interactive tactile feedback: subtle overload micro-glitch on critical taps (`cyberOverload(trigger = PRESS)`).
* **Components in Action:**
  - `CyberCard` (collapsible or expandable).
  - `CyberAccordion` (hierarchical mission briefings / gear specs).
  - `CyberSlider` (variable tuning, volume/frequency controls).
  - `CyberProgress` & `CyberSpinner` (active processing states).
  - `CyberTable` (tabular inventory / item stats).
  - `CyberSnackbar` & `CyberAlert` (contextual operational warnings).
* **Sample Screen: "Black Market / Cyberware Armory"**
  - Item catalog cards with item tier badges (Common, Rare, Black Market).
  - Accordion gear breakdown with stat bars and slider adjustments.
  - Primary CTA button (`CyberButton` + `cyberGlowBorder`) with purchase confirmation modal (`CyberModal`).

---

### Tier 3: Peak Immersion (Maximalist, Kinetic, "The Wow Factor")
**Production Target:** Netrunner Deck, Breach Matrix, Tactical Radar HUD, Reactor Diagnostics.  
**Purpose:** Blow the user away. Demonstrate the absolute technical peak of the library's custom rendering engine, GPU shaders, and cybernetic widgets.

* **Visual Profile:**
  - Active GPU distortion: gentle CRT barrel curve (`cyberCrt`), moving scanlines (`cyberScanlines`), film static (`cyberNoise`).
  - Interactive system glitching: violent overload tear on failure (`cyberOverload(intensity = 0.7f)`).
  - Emissive animations: pulsing beacons (`cyberPing`), particle bursts (`cyberSpark`), laser outlines (`cyberLaserOutliner`).
  - Kinetic typography (`CyberLaserText`, `GlowingText`).
* **Components in Action:**
  - `CyberTerminal` (live streaming command execution, interactive keyboard input).
  - `CyberDecrypter` (real-time text scrambling and password deciphering).
  - `CyberBiometrics` (touch-and-hold fingerprint authentication with scanning waves).
  - `CyberRim`, `CyberSectorRim`, `CyberDialTicks` (rotational radar sweep and gauge telemetry).
  - `CyberDragDrop` (tactical module socketing).
* **Sample Screen: "ICE-Breaker & Terminal Console"**
  - Full-screen CRT monitor framing a live hacking console.
  - Interactive minigame: user initiates breach → runs `CyberDecrypter` → triggers biometrics scan → terminal streams success logs.
  - Reactor status dial with sector rim progress and glowing spark alarms.

---

## 3. Experience Architecture: "NeonDeck OS"

Rather than a generic menu, NeonDeck is presented as a cohesive, tactile **Cyberdeck Operating System**:

```
┌────────────────────────────────────────────────────────────────────────┐
│ [NEON // DECK]                 v2.4 // ONLINE              [COMMS: OK] │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│                      [ ACTIVE WORKSPACE VIEWPORT ]                     │
│                                                                        │
│   Switches seamlessly between:                                         │
│   1. [TACTICAL HUD]   -> Tier 3 (Breach terminal, biometrics, radar)   │
│   2. [OPERATIONS]     -> Tier 2 (Contracts, inventory, sliders, cards) │
│   3. [SUBSYSTEMS]     -> Tier 1 (Clean settings, form inputs, profile) │
│                                                                        │
├────────────────────────────────────────────────────────────────────────┤
│ [HUD / BREACH]       │ [OPERATIONS]        │ [CONFIG / PROFILE]        │
│  (Tier 3: Intense)   │  (Tier 2: Balanced) │  (Tier 1: Clean)          │
└──────────────────────┴─────────────────────┴───────────────────────────┘
```

### Top Status Header (Global HUD Bar)
- Persistent header showing deck status, active callsign, live clock (`CyberTime`), and subtle battery / signal indicators.
- Seamlessly integrates `CyberNavStatus`.

### Bottom Navigation Deck (`CyberNavigationBar` / `CyberTabs`)
- **HUD / Breach** (Peak Immersion): The high-impact visual spectacle. The screen you show someone to instantly impress them.
- **Operations** (Balanced Product): The daily driver app experience. Shows practical, reusable UI architecture.
- **Subsystems** (Clean Utility): The refined, minimalist proof point. Proves the design system works for clean forms and settings.

### Micro-Interactions that Delight
- **Tactile Sound & Visual Bounces**: Buttons feel weighty; press states trigger deliberate visual contractions or momentary glow surges.
- **Live Scramble on Navigation**: Screen headers briefly scramble into place using `CyberDecrypter` mechanics when switching tabs.
- **Interactive Biometrics**: The biometric panel can be pressed and held, triggering a real scan animation that unlocks deck features.
- **Terminal Commands**: The terminal accepts actual touch inputs to trigger real visual reactions on the screen (e.g. typing `overload` triggers a deck-wide glitch).

---

## 4. Engineering Principles for the Showcase

### 1. Minimal Sandbox Architecture
- Keep the internal architecture lean and unencumbered.
- **No heavy frameworks**: No Hilt, Dagger, Room, or Ktor.
- In-memory mock data and standard Jetpack Compose state (`remember`, `mutableStateOf`).
- Standard `androidx.navigation.compose` for instant, predictable screen transitions.

### 2. Strict Token Integrity (Zero Magic Numbers)
- Every single color, dimension, font style, and elevation level comes directly from `CyberTheme`.
- Colors: `CyberTheme.colors.primary`, `surfaceSecondary`, `semantics.colors.danger`.
- Typography: `CyberTheme.typography.display`, `terminal`, `body`.
- Shapes: `CyberTheme.shapes.cardShape`, `cyberCutCornerShape`, `cyberCutCornerShapeSmall`.
- Spacing: `CyberTheme.spacing.dp8`, `dp16`, `dp24`.

### 3. Strict Modifier Ordering
Every component respects the established 8-step order:
`Size → Semantics → Glows → Clip → Background → Borders → Interaction → Padding`.

### 4. GPU Performance Budget
- Tier 1 and Tier 2 screens use zero distortion shaders to maintain absolute battery efficiency and rock-solid 120fps.
- Tier 3 concentrates shader effects on dedicated framing elements, strictly adhering to the rule of never stacking multiple heavy distortion shaders on a single surface.

---

## 5. Implementation Roadmap

### Step 1: Core Deck Shell (`NeonDeckApp`)
- Replace the stub in `MainActivity.kt` with the `NeonDeckApp` scaffold.
- Implement the persistent Cyberpunk top bar and 3-tab navigation deck (`CyberNavigationBar`).

### Step 2: Tier 1 Screen — Subsystems & Profile (Clean Utility)
- Build the clean agent profile and settings interface.
- Integrate `CyberTextField`, `CyberSwitch`, `CyberCheckbox`, `CyberDropdown`, `CyberTextArea`, `CyberBadge`.

### Step 3: Tier 2 Screen — Operations & Armory (Balanced Product)
- Build the inventory and mission operations deck.
- Integrate `CyberCard`, `CyberAccordion`, `CyberSlider`, `CyberTable`, `CyberProgress`, `CyberModal`, `CyberSnackbar`.

### Step 4: Tier 3 Screen — Tactical HUD & Netrunner Deck (Peak Immersion)
- Build the immersive hacking and telemetry console.
- Integrate `CyberTerminal`, `CyberDecrypter`, `CyberBiometrics`, `CyberRim`, `CyberSectorRim`, `cyberScanlines`, `cyberCrt`, and `cyberOverload`.

### Step 5: Verification & Polish
- Run Robolectric test suite in `sample/src/test/` to verify launch, tab switching, and interaction flows across all 3 tiers.
- Verify 60/120fps frame smoothness and edge-to-edge drawing paddings.
