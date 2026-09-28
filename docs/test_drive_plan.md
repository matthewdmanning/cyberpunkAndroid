# Physical Device Test Drive Plan

This plan encompasses every parameterized effect across the Cyberpunk UI design system, ensuring a complete top-to-bottom sweep of the aesthetic variants. 

## Phase 1: Core Physical Interactions (COMPLETED)
- **Glow Radius** (glow_radius): Extent of background halo (0-64dp).
- **Glow Opacity** (glow_opacity): Maximum brightness of the halo (0-1.0).
- **Glow Pulse Speed** (glow_pulse_speed): Sine wave duration for breathing state (500ms-3000ms).
- **Ping Scale** (ping_scale): Maximum physical expansion factor of the stroke (1.5x-3.5x).
- **Ping Decay** (ping_decay): Falloff curve aggressiveness (0.2-1.5).
- **Spin Bounce Amount** (spin_bounce): Elastic over-rotation (4dp-32dp).
- **Float Speed** (loat_speed): Idle hovering bob speed (1000ms-5000ms).
- **Bounce Offset** (ounce_offset): Vertical tap snap distance (4dp-32dp).
- **Backdrop Blur Radius** (ackdrop_blur_radius): Glassmorphism background blur intensity.

## Phase 2: Macro Shaders & Distortions (CURRENT - IN PROGRESS)
*Tested against the full Cyber Container card to ensure legibility.*

- **Overload Intensity** (overload_intensity): Glitch RGB split severity.
- **Overload Timescale** (overload_timescale): Frame-skip glitch stutter speed.
- **Scanlines Opacity** (scanlines_opacity): CRT overlay alpha.
- **Scanlines Spacing** (scanlines_spacing): Distance between CRT lines.
- **Scanlines Speed** (scanlines_speed): Velocity of the vertical CRT refresh band.
- **Stripes Width** (stripes_width): Thickness of the warning stripe hazard overlay.
- **Stripes Opacity** (stripes_opacity): Fade factor of the hazard background.
- **Noise Opacity** (
oise_opacity): Random static grain overlay intensity. (COMPLETED)
- **Datastream Speed** (datastream_speed): Matrix-style character fall velocity.
- **GlowBorder Width** (
eonborder_width): Core vector thickness of the container border.
- **GlowBorder GlowRadius** (
eonborder_glowradius): Ambient halo emission radius around the container.

## Phase 3: Composite Presets (UPCOMING)
Once individual components are dialed in, we will assemble "Themes":
- **Default Cyber**: Medium intensity, high contrast.
- **GlitchCore**: Maximum overload and noise, aggressive frame-skips.
- **Retro CRT**: Dense scanlines, high bloom, slow datastreams.
