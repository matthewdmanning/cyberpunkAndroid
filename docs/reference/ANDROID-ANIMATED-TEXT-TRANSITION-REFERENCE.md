# Android Animated Text Transition Reference

## Goal
Create a text transition that begins large and prominent, then scales and moves naturally into its final location.

## Primary Android Pattern
Use Compose shared-element/shared-bounds transitions. Android's official examples demonstrate text moving along an arc while its bounds change. For text, `ScaleToBounds` is particularly relevant because it scales already-laid-out text rather than repeatedly reflowing it during the transition.

## References
- Android Developers — Customize shared-element transitions: https://developer.android.com/develop/ui/compose/animation/shared-elements/customize
- Android Developers — Common shared-element use cases: https://developer.android.com/develop/ui/compose/animation/shared-elements
- Android Developers — Compose animation quick guide: https://developer.android.com/develop/ui/compose/animation/quick-guide
- Android Developers — Choose an animation API: https://developer.android.com/develop/ui/compose/animation/choose-api

## Design Direction
Prefer transform/bounds-based scaling over continuously changing font size. Use a gentle arc and appropriate easing when lateral movement accompanies the scale change. This should remain a styling/animation concern rather than domain behavior.
