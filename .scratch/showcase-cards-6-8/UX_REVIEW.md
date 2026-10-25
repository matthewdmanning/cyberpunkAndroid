# UX review of PROPOSAL.md

Reviewer: subagent (Opus), no prior context. Read-only review.
Status: Findings only. Not yet acted on.
Spot-checked by the main agent: X2 (`cyberSemantics` names), C7-7 (`CyberSlider` drag-only, height `thumbSize + 16.dp`). Both confirmed.

## Cross-card

- **X1** No reduce/stop motion control anywhere. WCAG 2.2.2 (A), 2.3.3 (AAA). No reduced-motion handling found in the library.
- **X2** `cyberCrt` and `cyberOverload` call `cyberSemantics("CyberCrt"/"CyberOverload", …)` (`effects/CyberSemantics.kt`). TalkBack would read the code name. Violates `accessibility-plan.md` "Zero Code Names in Semantics" and "Modifier Silence by Default". Proposal gives no `customA11y`.
- **X3** Guarded toggle, stealth rocker, purge button, cold-jack lever have no `Role`, `stateDescription`, `onClickLabel`. Critical alarms need a `liveRegion` (Assertive used sparingly). `accessibility-plan.md` §§2–4; Compose semantics docs.
- **X4** Color alone carries state in C6-3, C6-4, C7-4, C7-8, C8-3. WCAG 1.4.1 (A); Android "cues other than color".
- **X5** Continuous rumble at trace lock conflicts with Android haptics principles ("less is more", avoid long one-shot buzz, prefer `HapticFeedbackConstants`).

## Card 6

- **C6-1** Pipes always animate. WCAG 2.2.2. Roadmap says Level 1 is static, "zero distraction shaders"; research doc says Tier 1 non-distracting.
- **C6-2** Load shown only by brightness/position. WCAG 1.1.1; NN/g #1. Roadmap specifies 4 data channels, `CyberBadge` chips, fixed-width telemetry (bus clock, impedance, parity); proposal dropped them for 32 columns. NN/g #8.
- **C6-3** Barrier state by color/brightness only. WCAG 1.4.1.
- **C6-4** Reserve by color steps; vibrating needle with no text value. WCAG 1.4.1, 2.2.2, 1.1.1.
- **C6-5** 4 Hz strobe exceeds 3 flashes/s. WCAG 2.3.1 (A); passes only under area threshold. Needs device measurement. Strobe is "next to the port," but no port is drawn (NN/g #2).
- **C6-6** With no controls, breach/sever states have no driver, or run on a timer. WCAG 2.2.2; NN/g #1.
- **C6-7** Sever locks out "until manual reset," but no reset exists. NN/g #3. (Guarded toggle itself satisfies NN/g #5.)
- **C6-8** Rotary controls imply drag. WCAG 2.5.7 (AA); Android `customActions`; 48dp targets; no sizes given.

## Card 7

- **C7-1** `PrimaryTabRow` is for top-level tabs; in-content tabs should be secondary (Compose Tabs doc). `CyberTabs` exists (`Role.Tab`, 48dp), so no fallback is needed.
- **C7-2** Tab split: alarms on Tab 1, causes on Tab 2. Score rises with dwell while user is on Tab 2 and cannot see trace lock. NN/g #6, #1.
- **C7-3** Score grows with dwell unless stealth = 1, so trace lock is inevitable without user action. WCAG 2.2.2; NN/g #3.
- **C7-4** ICE class by color; roadmap text badge (`BLACK ICE` / `INTRUSION TRACE` / `HARDENED`) missing (WCAG 1.4.1). "Faint" LEDs need ≥3:1 (WCAG 1.4.11). **Red 8 Hz strobe: highest risk** (WCAG 2.3.1; red flash has a separate test).
- **C7-5** Distortion peaks when the trace-lock message must be read. NN/g #1; WCAG 2.3.3. `api-conventions.md` reserves glitch effects for non-blocking/idle states; research §4.4 forbids stacked heavy distortion; roadmap Card 7 lists only glow pulses.
- **C7-6** Reticle flash rate unspecified; no announcement. WCAG 2.3.1; `liveRegion`.
- **C7-7** `CyberSlider`: no stepping (`tickCount` only draws), drag-only (WCAG 2.5.7), height 36dp at default thumb (< 48dp).
- **C7-8** Stealth state by green glow only. WCAG 1.4.1; `stateDescription`. Latency cost hidden on Tab 2.
- **C7-9** "Tier" means slider input and score band. NN/g #4.
- **C7-10** Purge has no restore path. NN/g #3.
- **C7-11** Tab 2 lists siren frequencies that never play. NN/g #8, #2.

## Card 8

- **C8-1** `cyberCrt` barrel, vignette, fringing reduce legibility. Text ≥4.5:1; trace ≥3:1 (WCAG 1.4.11).
- **C8-2** Vitals only as trace shape; no live BPM/coherence on Tab 1. WCAG 1.1.1; NN/g #1, #6.
- **C8-3** Fibrillation zigzags and tearing may be a rapid image sequence. WCAG 2.3.1 (measure). Continuous trace with no pause: WCAG 2.2.2.
- **C8-4** Sparks mapped to wrong effect: `cyberOverload` is RGB split/slice; `cyberSpark` is the particle effect. Roadmap ties overload to holding the lever; proposal reverses it. NN/g #4.
- **C8-5** "Heavy pull" cannot be sensed on glass; drag gesture. WCAG 2.5.7; `customActions`; NN/g #5 needs a real mechanism (hold threshold, two-step guard). Roadmap says "holding" (long-press → `onLongClickLabel`).
- **C8-6** Escalation has no user input; runs on a timer, possibly while user is on Tab 2. WCAG 2.2.2; NN/g #1. Roadmap: Level 4 "paired with real tactile circuit actuators."
- **C8-7** Defib charge visible only on Tab 2; no discharge action. NN/g #1.
- **C8-8** Gain/sweep knobs: same as C6-8.

## Sources

- WCAG 2.2: https://www.w3.org/TR/WCAG22/
- Understanding 2.3.1: https://www.w3.org/WAI/WCAG22/Understanding/three-flashes-or-below-threshold.html
- Understanding 2.2.2: https://www.w3.org/WAI/WCAG22/Understanding/pause-stop-hide.html
- Understanding 1.4.1: https://www.w3.org/WAI/WCAG22/Understanding/use-of-color.html
- NN/g heuristics: https://www.nngroup.com/articles/ten-usability-heuristics/
- Android accessibility: https://developer.android.com/guide/topics/ui/accessibility/apps
- Android principles: https://developer.android.com/guide/topics/ui/accessibility/principles
- Compose semantics: https://developer.android.com/develop/ui/compose/accessibility/semantics
- Compose tabs: https://developer.android.com/develop/ui/compose/components/tabs
- Haptics principles: https://developer.android.com/develop/ui/views/haptics/haptics-principles

Not verified: Material Design tabs pages (JS-only). Nothing built or run. Flash findings need device measurement.
