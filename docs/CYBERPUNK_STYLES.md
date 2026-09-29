# Cyberpunk 2077 Style Research (Reference Only)

Research notes on the four Night City fashion styles, gathered while planning path effects.
**Nothing here is implemented yet.** Factions/styles were set aside for the path-effects task.

## Sources

All four are press write-ups of CD Projekt Red's Night City Wire presentation, not CDPR's own text:

- AltChar: [Cyberpunk 2077's fashion styles explained](https://www.altchar.com/game-news/cyberpunk-2077s-fashion-styles-explained-a9rIZ6t3Pd3q)
- Dexerto: [All 4 fashion styles coming to Cyberpunk 2077](https://www.dexerto.com/cyberpunk-2077/all-4-fashion-styles-coming-to-cyberpunk-2077-1440611/)
- Shacknews: [Cyberpunk 2077 will feature four specific fashion styles](https://www.shacknews.com/article/120937/cyberpunk-2077-will-feature-four-specific-fashion-styles)
- EIP Gaming: [Night City Chic](https://eip.gg/cyberpunk-2077/news/night-city-chic-creating-your-own-style-in-cyberpunk-2077/)

## The four styles (as the sources describe them)

| Style | Motto | What the sources say | Associated groups (EIP Gaming) |
|---|---|---|---|
| **Kitsch** | "Style over substance" | Bright colors, neon hair, fluorescent jackets, illuminated tattoos, shiny chrome, gold-plated cyberware. "Form over function"; gaudy; grabs attention. | The Mox, Tyger Claws |
| **Entropism** | "Necessity over style" | Utilitarian and practical; "wear whatever they can find". T-shirts, hoodies, overalls. "No bright colors, crazy hairdos, and unwieldy cybernetics", but gadgets and implants that work. Came out of economic collapse. | 6th Street, Nomad lifepath |
| **Neomilitarism** | "Substance over style" | "Somewhere between military uniform and expensive business suit". Black fabrics, tailored suits, form-fitting black leather, structured shoulders, streamlined cuts. "Deadly elegance without ostentation"; "bold, dark colours designed to intimidate". Worn by the most affluent. | Arasaka, Militech, old-money elite |
| **Neokitsch** | "Style and substance" | Kitsch flash plus wealth. Rare animal skins, gold cyberware, expensive fabrics; wood and marble in cars and buildings; smooth lines, striking color combinations. Celebrities, braindance stars, executives, heirs. | Westbrook elite |

## Things to keep straight

- **"Military" ≠ "Neomilitarism."** Neomilitarism is corporate formalwear with military undertones. It is not real-world military symbology (map graphics, HUDs, camo).
- **"Corpo" and "Street Kid" are character lifepaths, not styles.** Corpo fashion maps to Neomilitarism. Street gangs span Kitsch (Mox, Tyger Claws) and Entropism (6th Street).
- **Neokitsch builds on Kitsch**, so they share flash. The difference is expensive materials and smooth lines versus cheap, loud shine.

## Proposed direction (not implemented)

Discussed with the maintainer, 2026-09-28:

- Path effects stay **style-neutral** geometry.
- A style is a **parameter preset** applied to icons and containers. A preset chooses which path effects to use and sets their parameters, colors, speed, and easing.
- Each preset should only use parameters that fit its style. Styles should not mix.

Open question: exact palettes and easing curves per style. The sources above give colors in words only ("black", "neon", "gold"), not values.
