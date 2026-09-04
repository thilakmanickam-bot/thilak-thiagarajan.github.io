# Halo Design System

Reference spec for the QA gate's `design-integrity` check
(`.github/workflows/qa-gate.yml`) and for anyone reviewing a UI change.
Halo commits to a single **celestial dark** aesthetic — there is no light
theme, no per-screen palette (see `Theme.kt`'s comment: "the app commits
to the celestial night aesthetic").

## Color tokens (`android/app/src/main/kotlin/com/astrochart/ui/theme/Color.kt`)

| Token | Value | Use |
|---|---|---|
| `AstroBgTop` | `#241A54` | Background gradient top (indigo glow) |
| `AstroBgMid` | `#141138` | Background gradient middle |
| `AstroBgBottom` | `#08061C` | Background gradient bottom / MaterialTheme `background` |
| `AstroGlow` | `#3A2A78` | Purple radial glow accents |
| `GoldLight` | `#F6DFA0` | Gold gradient light stop / MaterialTheme `secondary` |
| `GoldDeep` | `#D9A94E` | Gold gradient dark stop / MaterialTheme `primary` — headers, sub-headers, icons, primary actions |
| `TextPrimary` | `#F5F3EE` | Warm near-white — primary text on dark backgrounds |
| `TextMuted` | `#BFB9D4` | Lavender-gray — secondary/body text, hints |
| `OnGold` | `#1A1330` | Dark text/icon color used **on top of** gold surfaces (buttons, badges) |
| `CardFill` | `#1B1747` (often at ~55% alpha) | Translucent indigo card surface |
| `CardBorder` | `#D9A94E` at ~35% alpha (`0x59D9A94E`) | Card/field border — always gold, never a neutral gray |
| `AstroError` | `#E5837A` | Soft rose — the only error color; never red (`#F44336`-style) |

**Rules a diff should not violate:**
1. No new hardcoded hex colors in Composables — every color reference should resolve to one of the tokens above (via `MaterialTheme.colorScheme.*` or a direct import from `Color.kt`).
2. Text on a gold surface (`GoldButton`, badges) uses `OnGold`, never `TextPrimary`/black/white ad hoc.
3. Errors use `AstroError`, not a new red.
4. Borders on cards/fields use `CardBorder` (translucent gold), not a plain gray/white stroke.

## Typography (`Type.kt`)

A single `buildTypography(fontFamily)` scale, parameterized by the active
language's bundled font (`LocalizedFonts.kt`) — never a hardcoded font
family per-screen. Display/headline weights are `Bold`; label styles
(`labelLarge`/`labelSmall`) use wide letter-spacing (`1.5.sp`) for the
gold "eyebrow" labels and button text. A new screen should reuse an
existing `MaterialTheme.typography.*` style, not define a one-off
`TextStyle` with a magic font size.

## Surfaces & shape language

- Cards: `RoundedCornerShape(22.dp)`, `CardFill` at ~55% alpha, 1dp
  `CardBorder` stroke — see `CelestialCard.kt`, the canonical card
  component. A new card-like surface should use `CelestialCard`, not a
  hand-rolled `Surface`/`Box` with different corner radius or fill.
- Buttons: gold-gradient pill (`GoldLight` → `GoldDeep`), `OnGold` text —
  see `GoldButton` in `Buttons.kt`.
- The app is edge-to-edge with a transparent status bar and light
  (non-dark) status bar icons (`Theme.kt`'s `AstroChartTheme`) — a new
  top-level screen should not reintroduce an opaque status bar.

### Onboarding (`OnboardingScreens.kt`)

The first-run wizard has its own layout rules, deliberately different
from the rest of the app. They are documented here so the design-integrity
review grades against them rather than flagging them:

- **Every step is vertically centred**, and scrolls only once its content
  is taller than the viewport (`OnboardingStepScaffold`). Steps must not
  be top-aligned — a short step stranded against the top of a tall screen
  is what this replaced.
- **The step indicator is full-bleed**: a 4dp track pinned to the bottom
  of the screen, running edge to edge with no horizontal padding, carrying
  a single gold-gradient dash one step wide that slides as steps advance
  (`StepProgressBar`). It is intentionally **not** rounded and
  intentionally **not** inset — a pill would read as a floating control
  rather than as the bottom edge of the screen. This is the one place a
  full-bleed unrounded element is correct.
- **Skip lives in the footer**, above the indicator, not inside step
  content — one skip affordance per screen, in a fixed position.
- Icon-led steps use `HaloHero`: the icon inside a gold-gradient ring over
  a soft radial glow, rather than a bare tinted `Icon`.

## What "correct" vs "violates the system" looks like

**Correct** — reusing tokens and existing components:
```kotlin
Text(
    text = strings.premiumHeadline,
    style = MaterialTheme.typography.headlineSmall,
    color = GoldDeep
)
CelestialCard {
    Text(text = description, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
}
```

**Violates the system** — new hardcoded values instead of tokens:
```kotlin
Text(
    text = strings.premiumHeadline,
    fontSize = 24.sp,                      // should be a MaterialTheme.typography.* style
    color = Color(0xFFFFD700)               // new gold, not GoldDeep — will drift over time
)
Surface(
    color = Color(0xFF2A2A2A),              // neutral gray card, breaks the indigo/gold system
    shape = RoundedCornerShape(8.dp)         // inconsistent corner radius vs. CelestialCard's 22.dp
) { ... }
```

## How the QA gate uses this doc

The `design-integrity` job reads the diff under `ui/theme/` and
`ui/screens/` and checks it against the rules above — flagging new
hardcoded colors/fonts/shapes that bypass the existing tokens/components,
not subjective taste. It ships in report-only mode first (see
`docs/TESTING.md`); until its false-positive rate is proven acceptable
over real PRs, treat its verdict as a strong hint to double-check, not an
automatic blocker.
