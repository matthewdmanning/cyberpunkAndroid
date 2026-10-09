# Accessibility Plan and Standards

Use this guide when authoring, refactoring, or auditing UI components and modifiers in this library. This document defines authoritative accessibility policies and Jetpack Compose semantics principles.

---

## Core Philosophy

Accessibility services (such as TalkBack) serve end users, not framework developers. The semantic tree must describe **what an element is, what state it is in, and what action it performs** in plain, user-facing language.

1. **Zero Code Names in Semantics**: Never expose Kotlin class names, internal widget symbols, or library prefixes to the semantic tree. A screen reader user should hear the human purpose of the control, never implementation details.
2. **Preserve Visible Text**: If a component renders visible text, that text is the primary semantic announcement. Never eclipse visible text with synthetic container labels.
3. **Strict Separation of Concerns**:
   - **Label**: What the element represents (e.g., visible text or descriptive content).
   - **Role**: What type of control it is (conveyed by `Role`, never embedded in text).
   - **State**: The dynamic condition of the control (conveyed by `stateDescription`, never string-concatenated).
   - **Action**: What will happen when activated (conveyed by action semantics such as `onClickLabel`).

---

## Jetpack Compose Semantics Principles

### 1. Natural Content Description
* **Rule**: Composables displaying visible text automatically expose that text to accessibility services.
* **Anti-Pattern**: Setting `contentDescription` to the component's type name or re-stating visible text on an enclosing container.
* **Guideline**: Do not set `contentDescription` on text-bearing containers unless the caller explicitly supplies a custom accessibility override.

```kotlin
// ❌ Anti-Pattern: Synthetic label replaces visible text
Row(modifier = Modifier.semantics { contentDescription = "CustomCheckbox" }) {
    Text("Remember me")
}

// ✅ Correct: Allow natural text to surface
Row(modifier = Modifier.semantics(mergeDescendants = true) { }) {
    CheckboxGraphic()
    Text("Remember me") // TalkBack announces: "Remember me, Checkbox, Not checked"
}
```

---

### 2. Standard Accessibility Roles (`Role.*`)
* **Rule**: Use Jetpack Compose's native `Role` enum (`Role.Button`, `Role.Switch`, `Role.Checkbox`, `Role.Tab`, `Role.DropdownList`, `Role.RadioButton`).
* **Anti-Pattern**: Embedding words like `"button"`, `"switch"`, or `"tab"` into textual descriptions (e.g., `"Save button"`). TalkBack announces this redundantly as *"Save button, button"*.
* **Guideline**: Pass `role = Role.Button` to `Modifier.clickable` or `Modifier.toggleable`. The platform automatically localizes and vocalizes the control type.

---

### 3. Dynamic State via `stateDescription`
* **Rule**: Dynamic states (e.g., expanded/collapsed, active/inactive) must be communicated via `stateDescription`.
* **Anti-Pattern**: Concatenating state into `contentDescription` (e.g., `contentDescription = "$title - Expanded"`). Accessibility services cannot recognize state transitions or localize strings when states are baked into static text.
* **Guideline**: Assign standard state strings or boolean toggles using `stateDescription`:

```kotlin
// ❌ Anti-Pattern: Concatenating state into description
Modifier.semantics { contentDescription = "$title (Expanded)" }

// ✅ Correct: Leverage stateDescription
Modifier.semantics {
    stateDescription = if (expanded) "Expanded" else "Collapsed"
}
```

---

### 4. Action Semantics via `onClickLabel`
* **Rule**: Screen readers announce action hints to the user (e.g., *"Double-tap to collapse section"*). The action label must describe the resulting action using a plain-language verb phrase.
* **Anti-Pattern**: Embedding instructions in labels (e.g., `"Tap here to close"`).
* **Guideline**: Supply `onClickLabel` to `Modifier.clickable` or assign `onClick(label = ...)` in `Modifier.semantics`:

```kotlin
Modifier.clickable(
    onClickLabel = if (expanded) "Collapse section" else "Expand section",
    role = Role.Button,
    onClick = { onExpandedChange(!expanded) }
)
```

---

### 5. Suppression of Decorative Child Nodes
* **Rule**: Visual elements that do not provide independent meaning or interaction must not generate isolated focus targets.
* **Anti-Pattern**: Leaving inner chevrons, glyphs, animated glow layers, or border shapes exposed to the accessibility hierarchy inside an already interactive row.
* **Guideline**:
  * For decorative standalone icons/images: set `contentDescription = null`.
  * For inner sub-elements and glyphs inside an interactive container: apply `Modifier.clearAndSetSemantics { }`.

```kotlin
// Inside an interactive container:
Text(
    text = if (expanded) "⌃" else "⌄",
    modifier = Modifier.clearAndSetSemantics { } // Prevents redundant focus target
)
```

---

### 6. Semantic Grouping (`mergeDescendants`)
* **Rule**: Group closely related visual elements into a single accessibility node when they together represent one logical item.
* **Guideline**: Use `Modifier.semantics(mergeDescendants = true)` on compound rows, cards, and list items. This presents a single unified focus rectangle to TalkBack, rather than forcing the user to swipe through fragmented pieces.

---

## Library Architecture & Parameter Conventions

When exposing accessibility controls across reusable design-system components and modifiers, adhere to these architectural rules:

### 1. Developer Overrides Contract
Every component accepting accessibility parameters should support two standardized arguments:
* `customA11y: String? = null`: When provided, completely overrides the component's accessibility announcement with the caller's text.
* `appendedA11y: String? = null`: When provided, appends contextual domain information to the natural display text (e.g., `"$title - $appendedA11y"`). It must **never** be prefixed with library names.

```kotlin
val accessibilityDescription = customA11y ?: if (!appendedA11y.isNullOrBlank()) {
    if (visibleText != null) "$visibleText - $appendedA11y" else appendedA11y
} else {
    null // Fall back to natural child text
}
```

### 2. Modifier Silence by Default
Visual effect modifiers (such as glows, borders, shaders, and animations) must be **silent by default**. Applying an aesthetic effect to a composable must never stamp a default `contentDescription` onto the node, which would clobber the underlying component's semantic label.

---

## Component Traversal & Audit Rubric

When reviewing or refactoring components in this module, apply this verification checklist to each file:

1. **Text Preservation**: Does the component render visible text? If yes, ensure no synthetic container description overwrites or masks it.
2. **Dynamic State**: Does the component manage state (expanded, checked, selected)? If yes, is `stateDescription` used instead of string concatenation?
3. **Action Announcement**: Is the interaction accompanied by a state-aware action label (e.g. `"Expand section"` vs `"Collapse section"`, `"Turn on"` vs `"Turn off"`)?
4. **Decorative Pruning**: Are inner glyphs, indicators, and decorative icons stripped of semantics via `clearAndSetSemantics { }` when the parent container handles the action?
5. **Role Integrity**: Does the interaction specify an appropriate native `Role` without embedding control words into static labels?
6. **Effect Isolation**: Do any applied visual modifiers inject default descriptions that mask the component? Ensure visual modifiers remain silent by default.
