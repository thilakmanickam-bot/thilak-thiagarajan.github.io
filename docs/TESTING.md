# Testing strategy

Halo's automated tests split into four categories, each with its own CI
job in `.github/workflows/qa-gate.yml` so a failure in one never masks a
failure in another. All four run in the JVM (Robolectric where an Android
context is needed) — there is no emulator/instrumented-device testing in
this repo; `androidTest` directories don't exist and won't be added for
this gate (see the tradeoffs section of the QA-gate plan this doc
accompanies).

## The four categories

| Category | Package | What it covers | Runs on |
|---|---|---|---|
| **Modules** | everything not listed below (`android/core/src/test`, and `android/app/src/test` outside `features`/`uitest`) | Pure business logic: chart calculation, panchangam, porutham/compatibility, chat prompt building, i18n strings, data parsing | Every PR + release gate |
| **Features** | `com.astrochart.features.*` | Cross-class scenario tests for a feature end to end — e.g. "does the entitlement cache round-trip correctly", "does the onboarding wizard gate on version code" | Every PR + release gate |
| **UI** | `com.astrochart.uitest.*` (excluding `screenshot/`) | Compose screen/component render + interaction correctness, via `createComposeRule()` + Robolectric — no emulator needed | Every PR + release gate |
| **Design integrity** | `com.astrochart.uitest.screenshot.*` (Roborazzi) + an LLM diff review | Visual regression (golden-image screenshots) and adherence to `docs/DESIGN_SYSTEM.md` | Release gate only (report-only initially) |

Design-integrity is release-gate-only, not every-PR: screenshot tests and
an LLM review step are slower and noisier than plain JVM assertions, and
routine merges to `main` shouldn't pay that cost.

## How to add a test in each category

**Modules** — a plain JUnit test next to the code it covers, same
convention already used throughout `core/src/test` and `app/src/test`
(e.g. `PanchangamTest.kt`, `PoruthamTest.kt`). No Robolectric needed
unless the code under test touches an Android API.

**Features** — a new file under
`android/app/src/test/kotlin/com/astrochart/features/`, one file per
feature scenario (`XyzFlowTest.kt`). Use `@RunWith(RobolectricTestRunner::class)`
+ `@Config(sdk = [33])` when the code under test needs a real `Context`
(e.g. `SharedPreferences`) — get it via `RuntimeEnvironment.getApplication()`,
not `androidx.test.core.app.ApplicationProvider` (that dependency isn't
declared in this project; Robolectric's own `RuntimeEnvironment` covers
the same need without adding one). Keep these tests to what's genuinely
reachable without heavy mocking of third-party SDK classes (Play Billing
model classes, Firebase) — prefer testing the pure logic your own code
adds around those SDKs (e.g. `PremiumStore`'s cache) over mocking the SDK
itself; if a scenario truly needs that, mock via `mockito-core`/
`mockito-kotlin` (already declared; Mockito 5's default inline mock maker
can mock final classes).

**UI** — a new file under
`android/app/src/test/kotlin/com/astrochart/uitest/`, one file per screen
or reusable component (`ScreenNameTest.kt`). Use
`createComposeRule()` inside a `@RunWith(RobolectricTestRunner::class)`
class; the `testOptions.unitTests.includeAndroidResources = true` flag in
`android/app/build.gradle` is what lets Robolectric load real resources
for these. Prefer testing one composable in isolation over a whole screen
when the bug risk is localized (e.g. `SearchableLocationField` on its
own, not the full `BirthInputScreen`) — smaller surface, faster, and the
failure message points straight at the broken component.

**Design integrity (screenshots)** — a new file under
`android/app/src/test/kotlin/com/astrochart/uitest/screenshot/`, using
Roborazzi's `@GraphicsMode` + `captureRoboImage()` against a composable
wrapped in `AstroChartTheme`. Golden images live alongside the test
(Roborazzi's default output path) and are committed to the repo. To
intentionally update a golden after a real design change, run
`gradle -p android app:recordRoborazziDebug` locally and commit the
regenerated images — `verifyRoborazziDebug` (what CI runs) only compares
against what's committed, it never records.

## Extending coverage over time

This is a from-scratch foundation, not a complete suite — the first
increment covers the four highest-risk areas (billing, onboarding,
location search, chat sign-in) and four highest-visibility screens for
screenshots (Home, Onboarding, Subscription, Chat). When touching a
feature or screen with no existing coverage, add a test in the matching
category above rather than skipping it — that's how this grows into real
coverage instead of staying frozen at the first increment.

## Running locally

```
gradle -p android core:test                                          # modules (core module)
gradle -p android app:testDebugUnitTest --tests "com.astrochart.features.*"
gradle -p android app:testDebugUnitTest --tests "com.astrochart.uitest.*" --tests "!com.astrochart.uitest.screenshot.*"
gradle -p android app:verifyRoborazziDebug --tests "com.astrochart.uitest.screenshot.*"
gradle -p android app:recordRoborazziDebug                            # regenerate goldens after an intentional design change
```
