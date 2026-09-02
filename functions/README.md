# Halo chat proxy

Server-side companion to the Android app's "Ask the Universe" chat. Verifies
the caller's Firebase ID token, enforces a 20-messages/day cap per signed-in
user (see `DAILY_MESSAGE_LIMIT` in `src/index.ts`), then forwards the request
to the Anthropic Messages API using a key that's held server-side and never
shipped in the app.

This only needs to be set up once, by whoever owns the `halo-2b942` Firebase
project — none of these steps can be done from the app's source code.

## One-time setup

1. **Put the project on the Blaze (pay-as-you-go) plan.** Cloud Functions 2nd
   gen isn't available on the free Spark plan. In the
   [Firebase console](https://console.firebase.google.com/project/halo-2b942/usage/details),
   Usage and billing → Modify plan → Blaze.
2. **Install the Firebase CLI** if you don't have it: `npm install -g firebase-tools`,
   then `firebase login`.
3. **Set your Anthropic API key as a secret** (never committed to the repo):
   ```
   firebase functions:secrets:set ANTHROPIC_API_KEY
   ```
   Paste your own key (from https://console.anthropic.com/) when prompted.
   You're billed by Anthropic directly for whatever signed-in users send
   through the app — the 20/day cap in `src/index.ts` bounds that, but raise
   or lower `DAILY_MESSAGE_LIMIT` to taste before deploying.
4. **Install dependencies and deploy:**
   ```
   cd functions
   npm install
   npm run deploy
   ```
   The command prints the deployed function's URL, e.g.
   `https://us-central1-halo-2b942.cloudfunctions.net/chatProxy`.

## Wiring it into the app

1. Set that URL as `CHAT_PROXY_BASE_URL` when building the app — either in
   `android/gradle.properties` (`CHAT_PROXY_BASE_URL=https://...`) or as a
   `CHAT_PROXY_BASE_URL` environment variable in CI, the same way
   `ADMOB_APP_ID` is already supplied (see `android/app/build.gradle`).
   **Include the trailing function path but not a trailing slash** — the app
   appends `v1/messages` to it, so the value should end in `.../chatProxy/`.
2. Flip `Features.CHAT_ENABLED` to `true` in
   `android/app/src/main/kotlin/com/astrochart/Features.kt` and rebuild —
   only do this once step 1's URL actually resolves, or every request will
   404 for real users.

## Local testing

```
cd functions
npm install
npm run serve   # builds, then starts the Functions + Firestore emulators
```
The emulator prints a local URL for `chatProxy`. `curl` `<that URL>/v1/messages`
with a POST body shaped like `AnthropicApi.kt`'s `ChatRequest` (`model`,
`max_tokens`, `system`, `messages`) and an `Authorization: Bearer <a real
Firebase ID token>` header to exercise the full auth → rate-limit → forward
path. A local run still
calls the real Anthropic API using whichever key `ANTHROPIC_API_KEY` resolves
to for the emulator (see the Firebase docs on `.secret.local` for testing
without touching the deployed secret).

## Changing the daily cap or moving to a real subscription gate later

`tryConsumeQuota` in `src/index.ts` is the only place the access rule lives.
When Play Billing ships, swap (or add to) that check for a read of the
`premiumActive` field on the caller's `users/{uid}` Firestore document — the
same document `ProfileSync.kt` already writes the primary profile to.
