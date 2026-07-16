# Releasing Halo to the Google Play Store

The `Release to Play Store` GitHub Actions workflow (`.github/workflows/release-play.yml`)
builds a **signed Android App Bundle** and uploads it to Play. This guide covers the
one-time setup and how to ship a build.

> The app id is **`com.astrochart`**. It must match the app you create in Play Console.

## 1. One-time: create the app in Play Console

The Play Developer API can upload new *versions*, but it cannot create the app or
complete its first-time setup. Do this once, by hand, at
<https://play.google.com/console>:

1. **Create app** → name "Halo", default language, App (not game), Free/Paid.
2. Complete the **initial content declarations**: privacy policy URL, ads, content
   rating questionnaire, target audience, **Data safety** (see §5), and the main
   store listing (short/full description, icon, feature graphic, screenshots).
3. Upload **one AAB manually** to the **Internal testing** track to establish the
   package and the app-signing key (accept Play App Signing). After that, the
   workflow can upload to that track automatically. You can build the AAB locally
   or download the `app-release-aab` artifact from a workflow run.

## 2. One-time: signing keystore

If you already have a release keystore, skip creation. To make one:

```bash
keytool -genkeypair -v -keystore halo-release.keystore \
  -alias halo -keyalg RSA -keysize 2048 -validity 10000
```

Keep this file and its passwords **safe and private** — losing them means you can no
longer update the app (unless you use Play App Signing, which is recommended). Never
commit the keystore.

Base64-encode it for the GitHub secret:

```bash
base64 -w0 halo-release.keystore   # Linux
base64 halo-release.keystore | tr -d '\n'   # macOS
```

## 3. One-time: Play service account (for automated upload)

1. In Play Console → **Setup → API access**, link a Google Cloud project.
2. Create a **service account** in Google Cloud, grant it access in Play Console
   (**Users & permissions**) with at least *Release to testing tracks* (and
   *Release to production* if you want the `production` track).
3. Create a **JSON key** for the service account and copy its full contents.

## 4. Add the GitHub repository secrets

Repo → **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | base64 of the keystore from §2 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore (store) password |
| `ANDROID_KEY_ALIAS` | key alias (e.g. `halo`) |
| `ANDROID_KEY_PASSWORD` | key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | the entire service-account JSON from §3 |

## 5. Data safety notes (fill in the Console form honestly)

The app is local-first, **except the "Ask the Universe" chatbot**, which sends the
selected chart summary and the user's typed messages to **Anthropic's API** using a
key the user enters in-app. Declare in Data safety that messages/derived data are
**sent to a third party (Anthropic)** for the chat feature, not sold, and used only
to provide the feature. The user's API key is stored on-device. No account, no ads,
no analytics are included.

## 6. Ship a build

- **Automatic version code:** the workflow sets `versionCode` to the run number, so
  every upload is unique and increasing. Bump the human-facing `versionName` in
  `android/build.gradle` (`ext.versionName`) when you want (e.g. `1.0.1`).
- **Run it:** Actions → **Release to Play Store** → *Run workflow* → pick a track
  (`internal` to start), or push a tag: `git tag v1.0.0 && git push origin v1.0.0`.
- The job builds `app:bundleRelease`, signs it, uploads the `.aab` as a build
  artifact, and pushes it to the chosen Play track with status `completed`.
- Promote from Internal → Production in the Play Console when you're ready.

## Troubleshooting

- **`Package not found` / `APK ... not allowed`** — the app isn't set up in Console
  yet, or the first manual upload (§1.3) hasn't happened.
- **`The caller does not have permission`** — the service account lacks the Play
  permission, or wasn't invited/accepted in Play Console.
- **`Version code N has already been used`** — re-run (run number increments), or set
  a higher `VERSION_CODE`.
