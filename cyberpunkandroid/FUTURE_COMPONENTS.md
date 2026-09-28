# CyberpunkAndroid Future Components Roadmap

While the core 19 components of the Cybercore CSS specification are fully implemented, the library can be expanded with the following advanced interactive components. These will heavily utilize the newly modularized rendering utilities (`cyberDatastream`, `cyberNeonBorderFlow`, `cyberOverload`, etc.).

## Proposed Components

### 1. CyberSlider
A sci-fi range slider to control continuous values.
- **Track**: Uses a bounded `cyberDatastream` that acts as the fill gauge.
- **Thumb**: A holographic node/handle that applies `cyberIconPulse` or intensifies its `cyberTextGlow` while being dragged.
- **Tick Marks**: Optionally renders terminal-style discrete data points along the track.

### 2. CyberSwitch
A futuristic boolean toggle switch.
- **ON State**: The track illuminates using `cyberNeonBorderFlow` to indicate an active circuit.
- **Thumb**: A pill or hexagon that slides across the track.
- **Transitions**: Activating the switch triggers a brief `cyberOverload` or `cyberScanlines` flash to simulate hardware engagement.

### 3. CyberSnackbar / CyberToast
Transient, high-priority notification pop-ups.
- **Container**: Leverages `CyberAlert` styling but animated to slide in from the screen edges.
- **Visuals**: Wrapped in a `cyberScanlines` overlay for a retro-CRT display feel.
- **Typography**: Critical alerts use a heavy `cyberTextGlow` warning text with an accompanying pulse animation.

### 4. CyberAccordion (Expandable Section)
A collapsible terminal-style menu or data read-out.
- **Header**: Features a glowing chevron and a persistent low-opacity neon border.
- **Expansion**: Opening the accordion triggers a quick `cyberOverload` transition or a top-down scanning effect over the newly revealed content to simulate data decryption/loading.
- **Content**: Can house nested `CyberTable`s or `CyberTerminal` logs.

### 5. CyberHexGrid / CyberNodeMap
A honeycomb array of interlocking hexagons for displaying system diagnostics, server status, or complex topologies.
- **Nodes**: Hexagonal elements that can glow, remain dim, or display stats.
- **Data Links**: Connections between active nodes use `cyberNeonBorderFlow` or bounded `cyberDatastream` effects.
- **States**: Offline or hacked nodes periodically `cyberOverload`.

### 6. CyberBiometrics (Uncontained Waveform)
A raw, continuous waveform monitor (like an EKG or oscilloscope) designed to be rendered *without a container*, floating directly over backgrounds or running along edges.
- **Waveform**: A fast-scrolling jagged line graph utilizing a heavy `cyberTextGlow`.
- **Critical Spikes**: Sudden peaks trigger localized `cyberOverload` effects, shifting colors to warning red/yellow.
- **Integration**: Acts as an ambient data layer rather than a boxed UI element.

### 7. CyberDecrypter (Loading Matrix)
A cryptographic alternative to a standard progress bar, representing loading or authentication as an "ice-breaking" process.
- **Cipher Text**: Displays a block or line of random, rapidly cycling characters.
- **Lock-in**: As progress increases, characters sequentially "lock" into their final readable state with a bright flash.
- **Visuals**: The actively decrypting section utilizes `cyberOverload` to simulate computational effort.

### 8. CyberRim (Radial Progress & Long-Press)
A circular interface ring serving as both a radial progress bar and a specialized interaction control.
- **Long-Press Fill**: Supports horizontal or radial fill-up triggered by a continuous long-press (useful for confirming critical/destructive actions).
- **Visuals**: Uses `cyberDatastream` for the fill gauge, with the rim brightly illuminating via `cyberNeonBorderFlow` upon completion.

### 9. CyberDragDrop (Tactical Sorting)
Interactive drop-zones and draggable modules for reallocating resources, sorting lists, or equipping items.
- **Draggable Item**: Picking up a component intensifies its `cyberTextGlow` and applies a subtle `cyberScanlines` shadow.
- **Drop Zones**: Valid target areas pulse with `cyberIconPulse` and illuminate their borders to indicate a successful connection point.
- **Snapping**: Dropping an item triggers a quick `cyberOverload` to simulate hardware docking.
