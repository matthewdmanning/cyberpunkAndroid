# Pixel frontier transition: device test plan

**Status:** prototype, not yet run on a device.
**Branch:** `feature/pixel-frontier-transition`
**Target device:** Google Pixel 7 (profiled before on Android 17, API 37, Vulkan)
**Code under test:** `CyberPixelFrontierTransition`, `CyberShaders.PixelFrontierShader`
**Where to run it:** `:sample`, tab **Frontier** (`PixelFrontierScreen`)

## What was checked already (not on a device)

| Check | Result | Where |
| --- | --- | --- |
| Library and sample compile | Pass | Gradle `compileDebugKotlin` |
| Unit tests (6): position math, uniform names match shader, both API paths compose | Pass | Robolectric, JVM |
| AGSL text compiles | Pass | Skia m144 runtime-effect compiler, desktop |
| Old and new layers never overlap and never leave a gap | 0 bad pixels at 6 progress values | Skia desktop render at half Pixel 7 size |

The Robolectric test does not run a real GPU shader. The desktop render does not use a phone GPU. Neither one gives frame times.

## Frame-time test

1. Install the sample on the Pixel 7. Open the **Frontier** tab.
2. Find the refresh rate: `adb shell dumpsys display | grep -i "refreshRate\|fps"`. The frame budget is `1000 / refresh rate` ms. I believe the Pixel 7 panel runs at up to 90 Hz (11.1 ms), but this was not verified. The older testing plan uses 16.6 ms.
3. Reset counters: `adb shell dumpsys gfxinfo com.example.sample reset`.
4. Tap **AUTO** and wait 30 seconds. Hide the sliders (**HIDE**) so the transition is as large as possible.
5. Read counters: `adb shell dumpsys gfxinfo com.example.sample`. Record total frames, janky frames, and the 50th, 90th, 95th and 99th percentile frame and GPU times.
6. Repeat for each row below. Change one setting at a time.

| Run | Block (dp) | Band (blocks) | Edge threshold | Split (dp) | Ragged (dp) | Why |
| --- | --- | --- | --- | --- | --- | --- |
| A | 32 | 6 | 0.12 | 4 | 32 | Defaults |
| B | 32 | 6 | 0.50 | 0 | 32 | Edges and split nearly off: cost of the block pass alone |
| C | 32 | 12 | 0.12 | 4 | 32 | Widest band: worst case |
| D | 8 | 6 | 0.12 | 4 | 32 | Smallest blocks: most outline work |
| E | 32 | 6 | 0.12 | 4 | 0 | Straight frontier: cost of the ragged edge |

Also run the existing `CyberPixelTransition` tab switch as a comparison.

**Pass rule (proposed, change if you disagree):** 0 janky frames in run A, and a 99th percentile frame time under the frame budget from step 2. If run C fails and run A passes, lower the default `bandBlocks`.

For a trace, record Perfetto during step 4 and look at GPU time per frame and at `RenderEffect` work on the render thread.

## Look test (by eye)

- [ ] The new screen appears behind the frontier. The old screen leaves ahead of it. No gap and no double image.
- [ ] Blocks are largest at the frontier and get smaller away from it.
- [ ] The frontier edge is ragged. At 0 ragged, it is a straight line.
- [ ] Outlines follow block sides. The outline color matches the theme primary color.
- [ ] Red and blue edges show at the frontier and go away at a split of 0.
- [ ] The glow is soft and does not look detached from the frontier.
- [ ] Both axes work (**AXIS** button).
- [ ] The sweep ends cleanly: the new screen is sharp, with no leftover outline or glow.
- [ ] The first frame is clean: the old screen is sharp before the sweep starts.
- [ ] Text on the test screens is readable after the sweep.

## Behavior test

- [ ] Tap **NEXT** fast, 5 times in 1 second. Write down what happens (the prototype restarts the sweep and may jump).
- [ ] Set Developer options > Animator duration scale to 0. The screens must switch with no stuck effect.
- [ ] Turn on TalkBack. The host announces its name only if an accessibility label is passed. The screen content must still be readable by TalkBack after the sweep.
- [ ] Rotate the device in the middle of a sweep.
- [ ] Run on an emulator below API 33. The default fade and scale transition must show, with no crash.

## Open questions for the result

1. Is the per-frame `RenderEffect` allocation a problem in the traces? If yes, test a version that keeps one effect and changes uniforms.
2. Do two full-screen off-screen layers at the same time cause GPU time spikes at the start of the sweep?
3. Does the sine-based hash in the shader look the same on the Pixel 7 GPU as in the desktop render?
