import sys

content = """# Cyberpunk Android UI - Usage Notes

## General Tuning Rule
When alerted to parameter interactions, you MUST use **Design of Experiments (DoE)** to find a combination that is somewhat close for further tuning, rather than varying parameters blindly or one at a time.

## Effect-Specific Interactions
- **cyberScanlines**: Do not apply scanlines to the component border.
- **cyberDatastream**: The `maxAlpha` interacts with the easing/function. Use DoE.
- **cyberDatastream (Visual)**: The tail should be a "trailing line" (e.g., only one half of a gaussian or a very sharp "cusp" function). Currently it looks like a full gaussian.
- **cyberBackdropBlur**: `opacity` is a window, but it strongly needs the right `radius` to work effectively.
- **cyberStripes**: `speed` and `width/size` interact.
- **cyberNeonBorder**: It is only one color. `opacity` may interact with `speed` and gradient/colors.
- **Glow Effects**: Strong interaction between `opacity`/`alpha`, `radius`, and `decay`. These values should have some level of normalization.
"""

with open('.agents/rules/usage_notes.md', 'w') as f:
    f.write(content)
