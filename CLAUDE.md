# Halo — working notes for Claude

Halo is the Android app under `android/` (`com.techbyt.halo`, Kotlin/Compose):
a South-Indian Vedic astrology app — rasi koshtam, panchangam, porutham, Rasi
Palan. The rest of this repo is a personal GitHub Pages site plus the Firebase
`functions/`. Assume "the app" means Halo unless told otherwise.

## Build and test — read this before claiming anything passes

**There is no local Gradle build.** AGP cannot resolve dependencies through this
sandbox's proxy. **CI is the only compile and test path.** Never report a local
test pass, and never say "this compiles" — you have not compiled it.

What you *can* run locally:

```bash
python3 tools/ephemeris_oracle.py --check   # chart engine vs Swiss Ephemeris
```

CI (`.github/workflows/android-ci.yml`) runs, in order: `lint`, the Swiss
Ephemeris check, `core:test`, `app:testDebugUnitTest`, `app:assembleDebug`, and
an APK asset check. Push, then read the run — do not guess at the outcome.

Before pushing Kotlin, sanity-check it mechanically: comment- and string-aware
brace/paren balance, and unused imports. Property delegation (`by mutableStateOf`)
needs `getValue`/`setValue` imports that a naive scanner will call unused.

## Modules

- `android/core` — pure Kotlin, no Android deps. Ephemeris, panchangam,
  porutham, readings, i18n. Unit-testable off-device; put logic here.
- `android/app` — Compose UI, Room, Firebase, billing, ads.

## Invariants that have already been broken once

Each of these cost a real user-visible bug. Do not undo them.

- **The plain fields on `PlanetaryPosition` are sidereal** (Lahiri). Only
  `ChartStyle.WESTERN_WHEEL` is tropical. Display code reads `signFor(style)` /
  `labelFor(style)` / `longitudeFor(style)` — never `.sign` or `.tropicalSign`
  directly. Reading a sign without deciding which zodiac it belongs to is how
  two zodiacs ended up on one screen.
- **The koshtam, porutham, Rasi Palan and the panchangam are sidereal
  unconditionally.** A rasi chart has no tropical variant. Do not "finish the
  job" by style-gating them.
- **A chart is recomputed, never replayed.** `SavedChartEntity.chartJson` is
  written for `ProfileSync` and is not chart truth; read a saved chart through
  `ChartRepository.recomputeFromEntity`. Replaying a snapshot made saved charts
  immune to fixing the engine.
- **Nine grahas.** Uranus, Neptune and Pluto were computed from fabricated
  elements and were removed with them. Their names survive in `Translations`
  as vocabulary only.
- **The ascendant needs the obliquity term.** The formula without it answered
  seven signs away from the printed lagnam.

## Verifying astrology claims

Never assert a chart or almanac value from memory. The available oracles:

- `tools/ephemeris_oracle.py` (pyswisseph) for planetary longitudes, the
  ascendant and the ayanamsa; `EphemerisGolden.kt` is generated from it.
- `SolachiChartTest` — the reference handwritten jathagam.
- `docs/RECKONING.md` — drik vs vakya, the measured Swiss agreement, and the
  open Tamil month-boundary question. Read it before "fixing" a date rule.
- The `jyotishi` agent (`.claude/agents/`) audits charts and panchangam against
  classical rules. Run it before a release.

Where a printed almanac and the engine disagree, establish *why* before
changing code — the difference is often vakya vs drik, not a bug.

## Secrets

- Never commit: the release keystore or its passwords, `ANTHROPIC_API_KEY`,
  `PLAY_SERVICE_ACCOUNT_JSON`, payment account identifiers. These are set via
  GitHub secrets and `firebase functions:secrets:set`.
- Fine to commit: `google-services.json`, the Firebase API key, AdMob IDs —
  they ship in every APK.
- **Never hand-edit `google-services.json`** to add a fingerprint entry. Each
  needs a real OAuth client ID only Google can mint.

## Releasing

Bump `versionName` in `android/build.gradle` with a changelog line in the block
above it, every rollout. `versionCode` comes from CI. Steps are in
`docs/RELEASE_RUNBOOK.md`. Nothing reaches the user's phone until a build ships
— say so plainly rather than implying a merged fix is live.

## How to work here

- **Verify, don't guess.** If a claim can be checked — against Swiss, a test, a
  file, CI — check it before stating it. "I think" in a technical report is a
  signal to go and look instead.
- **Investigate before editing.** Read the actual call path. A bug reported on
  one screen has more than once turned out to live on another.
- **Report faithfully.** If a step was skipped, say so. If a test fails, show
  the output. Do not describe an unverified change as done.
- **Be economical.** Read the part of the file you need. Batch independent tool
  calls into one message. Do not re-read a file you just edited to confirm the
  edit landed, or re-derive facts already established in the conversation.
- Commit messages explain **why**, in prose, and name the evidence. The existing
  history is the style guide.
