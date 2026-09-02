# Halo Cloud Functions

Server-side companions to the Halo Android app, deployed together from this
project:

- **`chatProxy`** (`src/chat.ts`) — backs "Ask the Universe". Verifies the
  caller's Firebase ID token, enforces a daily message cap per signed-in user
  (20/day, 200/day for verified premium subscribers — see
  `DAILY_MESSAGE_LIMIT_BASIC`/`DAILY_MESSAGE_LIMIT_PREMIUM` in `src/chat.ts`),
  then forwards the request to the Anthropic Messages API using a key held
  server-side and never shipped in the app.
- **`verifyPurchase`** (`src/billing.ts`) — backs Halo Premium. Verifies a
  Google Play Billing purchase token server-side against the Play Developer
  API, then writes the result to the caller's `users/{uid}` Firestore
  document so `chatProxy` and the app can trust it without re-verifying.

Both share `src/common.ts` (Firebase Admin init, ID token verification).

This only needs to be set up once, by whoever owns the `halo-2b942` Firebase
project — none of these steps can be done from the app's source code.

## One-time setup — chat proxy

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
   through the app — the daily caps in `src/chat.ts`
   (`DAILY_MESSAGE_LIMIT_BASIC`/`DAILY_MESSAGE_LIMIT_PREMIUM`) bound that,
   but raise or lower them to taste before deploying.
4. **Install dependencies and deploy:**
   ```
   cd functions
   npm install
   npm run deploy
   ```
   The command deploys both functions and prints their URLs, e.g.
   `https://us-central1-halo-2b942.cloudfunctions.net/chatProxy` and
   `.../verifyPurchase`.

## Wiring the chat proxy into the app

1. Set the `chatProxy` URL as `CHAT_PROXY_BASE_URL` when building the app —
   either in `android/gradle.properties` (`CHAT_PROXY_BASE_URL=https://...`)
   or as a `CHAT_PROXY_BASE_URL` environment variable in CI, the same way
   `ADMOB_APP_ID` is already supplied (see `android/app/build.gradle`).
   **Include the trailing function path but not a trailing slash** — the app
   appends `v1/messages` to it, so the value should end in `.../chatProxy/`.
2. Flip `Features.CHAT_ENABLED` to `true` in
   `android/app/src/main/kotlin/com/astrochart/Features.kt` and rebuild —
   only do this once step 1's URL actually resolves, or every request will
   404 for real users.

## One-time setup — Halo Premium (Google Play Billing)

1. **Create the two subscription products** in Play Console → your app →
   Monetize → Subscriptions, with product IDs `halo_premium_monthly` and
   `halo_premium_yearly` (must match `BillingManager.kt` exactly). Chosen
   pricing: **$4.99/month**, **$49.99/year**. Pricing lives entirely in Play
   Console and is never hardcoded in the app — this is just where the
   decision is recorded; enter it as each base plan's price when creating
   the products (Play converts to local currency per region automatically).
2. **Grant the existing release service account "View financial data"
   access.** This project already has a Play Console service account used
   for `release-play.yml` (release uploads); in Play Console → Users and
   permissions, find that same account and add the financial-data
   permission — it's a separate scope from the upload permission it already
   has, and is required for `purchases.subscriptionsv2.get`.
3. **Set the Firebase secret** (a different store from the GitHub Actions
   secret of the same name used by `release-play.yml` — set it separately
   even though it's the same JSON):
   ```
   firebase functions:secrets:set PLAY_SERVICE_ACCOUNT_JSON
   ```
   Paste the same service-account JSON already used for
   `PLAY_SERVICE_ACCOUNT_JSON` in the repo's GitHub secrets.
4. **Install dependencies and deploy** (same command as above —
   `npm run deploy` deploys both functions):
   ```
   cd functions
   npm install
   npm run deploy
   ```
5. Set the `verifyPurchase` URL as `BILLING_VERIFY_BASE_URL` when building
   the app, the same way as `CHAT_PROXY_BASE_URL` above — **no trailing
   path segment**, `BillingManager.kt` posts directly to this URL.
6. Flip `Features.BILLING_ENABLED` to `true` in `Features.kt` and rebuild —
   only once steps 1-5 are done, for the same "fails obviously vs. silently"
   reason `CHAT_ENABLED` documents.

iOS is on the roadmap but out of scope for now: the app is Android-only, so
payment goes exclusively through Google Play Billing today. The Firestore
fields this writes (`premiumActive`/`premiumExpiresAt`/`premiumProductId`)
are store-agnostic, so a future iOS build would only need its own
verification function against Apple's App Store Server API writing to the
same fields — no schema change here.

## Local testing

```
cd functions
npm install
npm run serve   # builds, then starts the Functions + Firestore emulators
```
The emulator prints local URLs for `chatProxy` and `verifyPurchase`. `curl`
`<chatProxy URL>/v1/messages` with a POST body shaped like `AnthropicApi.kt`'s
`ChatRequest` (`model`, `max_tokens`, `system`, `messages`) and an
`Authorization: Bearer <a real Firebase ID token>` header to exercise the
full auth → rate-limit → forward path. A local run still calls the real
Anthropic API using whichever key `ANTHROPIC_API_KEY` resolves to for the
emulator (see the Firebase docs on `.secret.local` for testing without
touching the deployed secret). `verifyPurchase` similarly needs a real
Play Billing sandbox/test purchase token (via a license-tester account in
Play Console) to exercise end to end.

## Changing the daily cap or the premium gate later

`tryConsumeQuota` in `src/chat.ts` is the only place the access rule lives —
it already reads `premiumActive` off `users/{uid}` to pick between
`DAILY_MESSAGE_LIMIT_BASIC` and `DAILY_MESSAGE_LIMIT_PREMIUM`.
