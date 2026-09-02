# Halo — Automated GitHub → Play Store release runbook

How to go from a git push to a live Play Store release. The code/CI side is already
built (`.github/workflows/release-play.yml`); the steps below are the one-time
account setup plus how to trigger releases.

**Flow in one line:** push/tag → GitHub Actions builds & signs the `.aab` → uploads
it to Play via a service account → you promote to production.

---

## PART A — One-time setup (do once)

### 1. Create the release keystore (your signing key)
On your computer (needs Java's `keytool`):

```bash
keytool -genkeypair -v -keystore halo-release.keystore \
  -alias halo -keyalg RSA -keysize 2048 -validity 10000
```

It asks for a **keystore password**, a **key password**, and your name/org. **Save the
file and both passwords safely** — without them (and without Play App Signing) you
can't update the app later. The alias here is `halo`.

### 2. Create the app in Play Console
1. <https://play.google.com/console> → **Create app** → name **Halo**, App, Free.
2. **Dashboard → "Set up your app"**: App access, Ads (No), Content rating, Target
   audience, **Data safety**, **Privacy policy URL** — use the answers in
   [`play-store/STORE_LISTING.md`](play-store/STORE_LISTING.md). The privacy URL is
   the GitHub Pages URL for `privacy-policy.html`.
3. **Main store listing**: paste the copy and upload the icon + feature graphic from
   `play-store/`, plus 2–8 screenshots.

### 3. First upload is manual (this unlocks the API)
Google requires the **very first** bundle for a new app to be uploaded by hand;
after that the API can take over.
1. Build a signed bundle (add the keystore secrets in step 5, then run the workflow
   in Part B once — it produces an `app-release-aab` **artifact** even before the API
   upload works). Download the `.aab`.
2. Play Console → **Testing → Internal testing → Create new release** → upload the
   `.aab` → **accept Play App Signing** → save & roll out.

### 4. Create the Play service account (for automated upload)
1. Play Console → **Setup → API access** → accept terms → **link a Google Cloud
   project**.
2. **Create service account** (opens Google Cloud) → create it → copy its email
   (`…@…iam.gserviceaccount.com`).
3. Google Cloud → **IAM & Admin → Service Accounts → (account) → Keys → Add key →
   Create new key → JSON**. The downloaded file is your `PLAY_SERVICE_ACCOUNT_JSON`.
4. Play Console → **Users & permissions → Invite user** → paste the service-account
   email → grant **Release to testing tracks** (and *production* if you'll auto-ship
   there) → send.

### 5. Add the 6 GitHub secrets
Repo → **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 halo-release.keystore` (macOS: `base64 halo-release.keystore \| tr -d '\n'`) |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password (step 1) |
| `ANDROID_KEY_ALIAS` | `halo` |
| `ANDROID_KEY_PASSWORD` | key password (step 1) |
| `PLAY_SERVICE_ACCOUNT_JSON` | the entire contents of the JSON file (step 4) |
| `ANTHROPIC_API_KEY` | an Anthropic API key (from <https://console.anthropic.com/>) — used by `qa-gate.yml`'s `design-integrity` job to review UI diffs against `docs/DESIGN_SYSTEM.md`. Without this secret the QA gate's modules/features/UI jobs still work; only the design-integrity step fails to authenticate (it's report-only, so this doesn't block a release, but you'll see no design-integrity feedback until it's set). |

---

## PART B — Ship a release (every time)

1. *(optional)* bump `ext.versionName` in `android/build.gradle` (e.g. `1.0.1`). The
   `versionCode` auto-increments from the CI run number.
2. GitHub → **Actions → "Release to Play Store" → Run workflow** → pick a track
   (`internal` first), leave **`run_qa_gate` checked** (the QA gate — modules/features/
   UI tests + a design-integrity review, see `docs/TESTING.md`) → **Run**. *Or* push a
   tag: `git tag v1.0.1 && git push origin v1.0.1` (tag pushes always run the gate).
   - Uncheck `run_qa_gate` only to skip the gate for a quick internal/alpha/beta
     iteration — **it cannot be skipped for `production`**: `determine-gate` forces it
     on regardless of the checkbox once that track is selected.
3. The workflow runs the QA gate (if required), builds a signed `.aab`, saves it as an
   artifact, and uploads it to the chosen Play track via the service account — `publish`
   only runs if the gate passed (or wasn't required).
4. In Play Console, **promote Internal → Production** when ready. Production releases
   go through Google review (hours to a few days).

---

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `Package not found` / first upload rejected | The manual first upload (A.3) hasn't happened yet. |
| `The caller does not have permission` | Service account not invited/accepted in Play Console, or missing the track permission (A.4). |
| `Version code N has already been used` | Re-run the workflow (run number increments), or set a higher `VERSION_CODE`. |
| Signing errors in the "Build signed AAB" step | Wrong keystore password/alias/key password secret. |

## Notes
- The AI chatbot is currently disabled via `Features.CHAT_ENABLED` (in
  `android/app/src/main/kotlin/com/astrochart/Features.kt`); flip it to `true` to
  re-enable "Ask the Universe" in a future release.
- See also [`PLAY_RELEASE.md`](PLAY_RELEASE.md) for the same setup in reference form
  and [`play-store/STORE_LISTING.md`](play-store/STORE_LISTING.md) for listing copy.
