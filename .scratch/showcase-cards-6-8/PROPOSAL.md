# Proposal: Showcase cards 6 to 8, subparts and functions

Status: Draft for review. Not approved.
Branch: `feature/showcase-cards-6-8`, from `feature/sample-m3-adaptive-showcase`.
Sources: `docs/inspirations/mil-spec-hardware-bus-diagnostic.md`,
`docs/inspirations/ice-detection-matrix-countermeasures.md`,
`docs/inspirations/crt-biomonitor-dumpshock-cold-jack-console.md`.

## Scope

- Card 6: Mil-Spec Bus Diagnostic (Level 1). One panel.
- Card 7: ICE Detection Matrix (Level 3). Two tabs.
- Card 8: CRT Biomonitor (Level 4). Two tabs.

Level 3 and 4 cards have two tabs:

- **Tab 1, Display:** The user-facing surface, inside the lore.
- **Tab 2, Backbone:** The equipment and the hidden parts.
- Both tabs read one state holder.
- The tab bar uses the library's tab component, if one exists.
  If not, it uses Material 3 `PrimaryTabRow`.

## Card 6: Mil-Spec Bus Diagnostic (Level 1, one panel)

**Optical bus light-pipes.** A bank of 32 vertical glass light-pipes.
- Function: Taps the fiber bus with no signal loss. Each pipe shows one compute thread.
- Match: Green packets move as waves across the columns. The light centroid moves to where load moves. At idle, pipes breathe slowly at 0.5 Hz. At high load, they glow almost solid. Thus brightness and position show load.

**Firewall barrier panel.** An electroluminescent plane under armored glass.
- Function: A hardware gatekeeper that blocks intrusion packets.
- Match: Cobalt-blue brightness is proportional to barrier integrity. A steady, even glow means "no attack." Pings make local ripples where they hit. A breach puts violet and white streaks on the panel. When the bus is cut, the panel goes black.

**Power rail flux meter.** Two segmented LED bars and an analog needle.
- Function: Monitors fast current spikes on four rails, up to 80 A.
- Match: Each spike makes a fast pulse on the bars. Amber-to-green steps show capacitor reserve. The needle rests at +12.0 V. On a breach, it vibrates between 11.2 V and 13.8 V. Thus unstable power looks unstable.

**Amber warning strobe.** A lamp next to the spinal port.
- Function: Warns of an active breach.
- Match: It flashes at 4 Hz next to the port that is under attack.

**Controls.** A 6-position mode selector, a guarded isolation toggle, and a calibration knob.
- Function: The toggle cuts all bus lines. The red guard prevents accidental use.

## Card 7: ICE Detection Matrix (Level 3)

### Tab 1: Display

**Intrusion topology screen.** An amber vector wireframe on a CRT-style screen.
- Function: Shows subnet nodes, daemon threads, and hostile tracers.
- Match: Calm amber grid lines mean a safe probe. As the score approaches 100%, jitter, RGB split, and barrel distortion increase. Thus damage on the screen tracks the danger.

**Perimeter ICE bars.** Three LED clusters, 20 blocks each.
- Function: Show which ICE class is active.
- Match: Blocks 1–8 are ice blue for White ICE (passive monitors). Blocks 9–15 are amber for Grey ICE (barriers). Blocks 16–20 are crimson for Black ICE (lethal feedback). Color becomes hotter as lethality increases. Above trace lock, the red blocks strobe at 8 Hz.

**Penetration depth slider.** A linear slider with four detents.
- Function: Selects Tier 1 to Tier 4.
- Match: Hard detents give a "thunk" for each tier. The operator can feel the tier without looking.

**Stealth rocker.** A two-position lit switch.
- Function: Masks the packets. This decreases the score, but adds latency.
- Match: The switch glows green when masking is on.

**Trace purge button.** A large button under a clear flip cover.
- Function: Cuts all lines in 350 µs.
- Match: The cover prevents an accidental purge. After a purge, the alarms stop and the screen clears.

**Trace-lock reticle.** A flashing crimson reticle with a lock message.
- Function: Tells the operator that the trace is complete.

### Tab 2: Backbone

**Security coprocessor.** It calculates the overwatch score:
`score = f(tier) × (1 − stealth efficiency) × dwell time`.
- Match: Show each input and the product live. The operator can see why the score changes.

**Tier detection bands.** Tier 1 is below 20%. Tier 2 is 20–45%. Tier 3 is 45–75%. Tier 4 is above 75%.
- Match: A band diagram shows the current score in its tier.

**Stealth filter hardware.** It scrambles headers, forges MAC signatures, and routes through proxies.
- Match: Show the masking stages and the latency cost.

**Piezo sirens.** Alarm tones of 400 Hz, 800–1,600 Hz, and 2.2 kHz.
**Haptic motors.** Two motors that shake when ICE locks on.
**Optical relays.** These cut the lines when you purge.
**Decryption shunt.** Its heat increases from 23°C to 52°C.
- Match: Show each part as a state readout. This tells which part is active.

## Card 8: CRT Biomonitor (Level 4)

### Tab 1: Display

**Curved CRT faceplate.** Barrel curvature, a corner vignette, and green phosphor.
- Function: Shows the neural EKG trace.
- Match: Use `cyberCrt`. Scanlines drift slowly when the state is stable. Raster jitter and color fringing increase with stress.

**EKG and synaptic trace.** A green line that draws from left to right, with an afterglow.
- Function: Shows heart rhythm and neural firing density together.
- Match: Stable: a smooth trace at 68–74 BPM. Stress: jagged bursts at 135 BPM. Fibrillation: zigzags fill the full tube height at more than 210 BPM. Thus the trace shape shows the patient state.

**Bezel safe-zone markings.** Red lines with BPM, mV, and coherence labels.
- Function: Show safe limits on the trace.

**Bezel corona sparks.** Blue-white sparks between the glass frame and the screws.
- Function: Show excess voltage that leaks during overload.
- Match: Use `cyberOverload`. Sparks start at critical failure and stop at disconnect.
- Note: The dossier says that these sparks come from the overload. They do not come from the defibrillator.

**Cold-jack lever.** A red lever with white stripes that you pull down.
- Function: Cuts the spinal link and grounds the feedback.
- Match: The stripes mark it as an emergency control. A heavy pull prevents accidental use. After the pull, the sparks stop, and a stable baseline trace shows.

**Gain and sweep knobs, defibrillator rocker.** Stepped knobs and a guarded switch.
- Function: The knobs set trace gain and sweep speed. The rocker charges the defibrillator coils.

### Tab 2: Backbone

**Neural interlink tap.** Differential amplifiers that read 0.1–50 µV signals.
**Cardiac-neural coupling model.** It sets heart rate and coherence for each stage: 96%, 62%, then less than 15%.
**Capacitor bank and busbars.** These absorb up to 2.5 kV in 50 µs.
**Knife switch and arc chute.** These show an arc flash when the feedback dumps.
**Defibrillator coils.** These show the charge level.
**Flyback, sounder, and fan.** The flyback tone is 15.75 kHz. A "pock" sounds for each heartbeat.
- Match: Show the signal path from the tap to the screen. Show the feedback path from the lever to the capacitors.

## Open decisions

1. **Card 6 controls:** The roadmap says "passive," but the dossier has controls.
   Default: show the display only. Add a `TODO` for each control.
2. **Audio:** All three dossiers specify sounds.
   Default: show alarm state as visual only, and use Android haptics. Add a `TODO` for each sound.
3. **ICE score function:** The dossier gives no constants for `f(tier)` or dwell rate.
   Default: named placeholders with a `TODO`. No invented values.
