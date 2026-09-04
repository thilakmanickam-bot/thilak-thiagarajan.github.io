# Halo — owner setup checklist

Everything **you** have to do by hand, that cannot be done from the app's source
code. Written against the repo as of version **1.2.1**, branch
`claude/natalie-astro-chart-fetch-9h63kf`.

Package name: **`com.techbyt.halo`** · Firebase project: **`halo-2b942`**

Work top to bottom. Part 0 is urgent; Parts 2 and 3 are the Premium edition and
can wait. Nothing here needs a network connection to *read* — keep this file or
its `.docx` copy on the machine you're doing the setup from.

---

## Status snapshot — what is already done

Do **not** redo these. Each is confirmed from the repo or from CI history.

| Item | Evidence |
|---|---|
| Release keystore created, 4 signing secrets set in GitHub | 32 successful signed `.aab` builds in "Release to Play Store" |
| Play Console app exists, first manual upload done | same — the API refuses uploads to an app that has never been uploaded by hand |
| Play service account created, invited, has upload permission | same |
| `PLAY_SERVICE_ACCOUNT_JSON` set as a **GitHub** secret | same |
| AdMob app ID | `ADMOB_APP_ID=ca-app-pub-2807860736270523~8908912294` in `android/gradle.properties` |
| AdMob **real** ad units (banner + interstitial) | pasted in `ads/Ads.kt` — not test units |
| Firebase project `halo-2b942` wired, 2 Android OAuth clients registered | `android/app/google-services.json` |
| Google Sign-In live | `Features.AUTH_ENABLED = true` |
| In-app updates live | `Features.IN_APP_UPDATE_ENABLED = true` |
| Ads live | `Features.ADS_ENABLED = true` |

### What is off, and why

| Flag | State | Blocked on |
|---|---|---|
| `CHAT_ENABLED` | `false` | Part 2 — the chat proxy is not deployed |
| `BILLING_ENABLED` | `false` | Part 3 — no subscription products, `verifyPurchase` not deployed |
| `FACEBOOK_LOGIN_ENABLED` | `false` | Part 5 — Facebook app + their review |

---

## PART 0 — Ship 1.2.1 (do this first, ~15 min)

**Why this is urgent.** The build on your phone is **1.2.0**, cut three commits
before the chart engine was fixed. Every chart it draws is wrong: the planets
came from fabricated formulas (Mercury was 98° out, Venus 123°, Saturn 135°),
the ascendant formula had no obliquity term and landed seven signs away from the
printed lagnam, and the koshtam carried Uranus, Neptune and Pluto. **None of the
fixes are on any device until you run this.**

### 0.1 Merge or ship from the branch

The fixes are on `claude/natalie-astro-chart-fetch-9h63kf` (draft PR #19), not on
`main`. Either mark PR #19 ready and merge it, or release straight from the
branch — the workflow builds whatever branch you run it on.

> PR #19: <https://github.com/thilakmanickam-bot/thilak-thiagarajan.github.io/pull/19>

### 0.2 Run the release workflow

1. Go to **Actions → "Release to Play Store" → Run workflow**
   <https://github.com/thilakmanickam-bot/thilak-thiagarajan.github.io/actions/workflows/release-play.yml>
2. **Use workflow from:** pick the branch (or `main` if you merged).
3. **Track:** `internal-app-sharing` — it skips Google review entirely and hands
   you a private install link in minutes. (`internal` and above wait on review.)
4. **`run_qa_gate`:** uncheck it for this first pass. It has never been run
   successfully and you do not want to debug the gate and the release at once.
5. **Run workflow.** ~5 minutes.
6. Open the finished run → **Summary** → copy the **Install link**.

> The link only installs for accounts on the Internal App Sharing tester list:
> Play Console → **Setup → Internal app sharing**.

### 0.3 Verify on the device — two checks that must both pass

Open **Marriage Match Making**, enter **21 Dec 1989, 13:02, Kāraikkudi**:

- Input card must read **Rasi: Virgo · Nakshatram: Hasta**
- Koshtam below it must read **Rising: Meenam (Pisces)**, Moon in **Kanni
  (Virgo)**, and contain **nine bodies with no Pl / Ne / Ur**

Then open a chart you **saved on 1.2.0**. It must now also read Meenam, not
Libra. That is the second fix — saved charts used to replay a stored snapshot and
would have stayed wrong forever otherwise.

If either check fails, stop and report exactly what the koshtam shows.

### 0.4 Promote when happy

Re-run the same workflow with track `internal`, then `production`. Production
**always** runs the QA gate — see Part 4 before you get there.

---

## PART 1 — Basic edition, remaining items

The basic (free, ad-supported) edition is functionally complete. These are the
gaps that will bite you at Play review or in production.

### 1.1 Firestore security rules — do this before any public release

**Priority: high.** `AUTH_ENABLED` is on, so signed-in users' charts sync to
Firestore at `users/{uid}` and `users/{uid}/charts/{id}`. There is **no
`firestore.rules` file in this repo**, which means the rules are whatever the
console has. If the project was created in **test mode**, those rules either
already expired (sync silently fails) or are wide open (anyone can read every
user's birth data). Birth date, time and place are personal data.

1. Open <https://console.firebase.google.com/project/halo-2b942/firestore/rules>
2. Replace the contents with:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read: if request.auth != null && request.auth.uid == uid;
      // Entitlement fields are server-written ONLY. Without this clause any
      // signed-in user can write premiumActive:true to their own document
      // from the client SDK and take Premium without paying or redeeming.
      allow write: if request.auth != null && request.auth.uid == uid
        && !request.resource.data.diff(resource.data).affectedKeys()
             .hasAny(['premiumActive','premiumExpiresAt','premiumProductId','testerRedeemedAt']);
      match /charts/{chartId} {
        allow read, write: if request.auth != null && request.auth.uid == uid;
      }
    }
    // Tester access requests, reviewed by you in the console.
    match /testerRequests/{uid} {
      allow create, update: if request.auth != null && request.auth.uid == uid;
      allow read: if false;
    }
    // Server-only bookkeeping: quota counters and redemption attempts.
    match /chatUsage/{doc}      { allow read, write: if false; }
    match /testerAttempts/{doc} { allow read, write: if false; }
  }
}
```

3. **Publish**, then sign in on the device and confirm a saved chart still syncs.

> The Cloud Functions use the Firebase Admin SDK, which bypasses rules
> entirely — so `verifyPurchase` and `redeemTesterCode` still write the
> entitlement fields, and the quota collections above still work, even though
> the rules deny every client.
>
> **An earlier version of this section granted blanket `write` on
> `users/{uid}`**, which would have let any signed-in user grant themselves
> Premium. If you already published that, replace it with the above.

### 1.2 Fix the Play Store listing copy — it currently describes the app wrongly

`docs/play-store/STORE_LISTING.md` is stale on two points. Pasting it as-is risks
a Play policy rejection, because the listing would describe features that do not
work as stated:

| It says | Reality |
|---|---|
| "This optional feature uses **your own Anthropic API key**" | No longer true. Chat goes through a server-side proxy; users never enter a key. And chat is **off** in the shipped build. |
| "Fully available in **English, Tamil, and Chinese**" | There are **8** languages: English, Tamil, Hindi, Telugu, Kannada, Malayalam, Marathi, Chinese. |

**Action:** before you publish the listing, delete the whole "Ask the Universe"
paragraph (the feature is disabled — do not advertise it), and change the
languages line to name all eight. Re-add the chat paragraph only when Part 2 is
done and `CHAT_ENABLED` is `true` in a shipped build.

### 1.3 Confirm the privacy policy URL resolves

The listing points at
<https://thilakmanickam-bot.github.io/thilak-thiagarajan.github.io/privacy-policy.html>

Open it in a browser. If it 404s, check **repo → Settings → Pages** for the real
base URL and use that. Play requires a working privacy policy URL, and this one
is also where you must disclose the Firestore sync of birth data and AdMob's
advertising ID use.

### 1.4 Complete the Play Console "Set up your app" tasks

Play Console → your app → **Dashboard**. Every item must be green before a
production release is accepted: App access, Ads (answer **Yes** — you serve real
AdMob units), Content rating, Target audience, **Data safety**, News app, COVID,
Data privacy, Government apps.

**Data safety** is the one people get wrong. You collect and transmit:
birth date/time/place, name, email (Google Sign-In), and the advertising ID.
Declare all of them, and declare that data is encrypted in transit and that users
can request deletion.

---

## PART 2 — Premium edition, step 1 of 2: the AI chat ("Ask the Universe")

Everything in the app is written and tested. What is missing is the server.
About 30 minutes. **Do Part 2 before Part 3** — the same deploy command ships
both functions, so doing chat first means Part 3 is only Play Console work.

### 2.1 Put the Firebase project on the Blaze plan

Cloud Functions 2nd gen does not exist on the free Spark plan.

1. <https://console.firebase.google.com/project/halo-2b942/usage/details>
2. **Modify plan → Blaze (pay as you go)**, attach a billing account.
3. **Set a budget alert** while you are there. Blaze is uncapped by default.

> Cost in practice: the functions themselves sit inside the free tier for a small
> user base. The real cost is the Anthropic API, billed separately by Anthropic —
> bounded by the daily caps in 2.3.

### 2.2 Install the Firebase CLI

```bash
npm install -g firebase-tools
firebase login
firebase use halo-2b942
```

### 2.3 Decide the daily message caps *before* deploying

Open `functions/src/chat.ts` and look at `DAILY_MESSAGE_LIMIT_BASIC` (20/day) and
`DAILY_MESSAGE_LIMIT_PREMIUM` (200/day). These are the **only** thing standing
between a signed-in user and your Anthropic bill. Raise or lower them now; they
apply per signed-in user per day.

### 2.4 Set the Anthropic API key as a Firebase secret

```bash
firebase functions:secrets:set ANTHROPIC_API_KEY
```

Paste your key from <https://console.anthropic.com/> when prompted.

**Never put this key in the repo, in `gradle.properties`, or in a chat message.**
It is stored in Google Secret Manager and injected at runtime only.

### 2.5 Deploy

```bash
cd functions
npm install
npm run deploy
```

Deploys **both** `chatProxy` and `verifyPurchase` and prints their URLs. Copy
both. They will look like:

```
https://us-central1-halo-2b942.cloudfunctions.net/chatProxy
https://us-central1-halo-2b942.cloudfunctions.net/verifyPurchase
```

### 2.6 Wire the URL into the build

Add to `android/gradle.properties`:

```
CHAT_PROXY_BASE_URL=https://us-central1-halo-2b942.cloudfunctions.net/chatProxy/
```

**The trailing slash matters.** The app appends `v1/messages` to this value.
`.../chatProxy` without the slash produces `.../chatProxyv1/messages` and every
request 404s.

### 2.7 Flip the flag and ship

In `android/app/src/main/kotlin/com/astrochart/Features.kt`:

```kotlin
const val CHAT_ENABLED = true
```

Bump `versionName` in `android/build.gradle` (1.2.1 → **1.3.0**, a new feature),
add a changelog line, commit, then run the release workflow as in Part 0.

### 2.8 Verify

On the device: sign in with Google, open **Ask the Universe**, send a message.

- A reply means the whole chain works.
- "Sign in to continue" means `AUTH_ENABLED` or your sign-in is the problem.
- A network/404 error means the URL in 2.6 is wrong — check the trailing slash.
- A 429 means you hit the daily cap from 2.3.

Watch the logs live with `firebase functions:log --only chatProxy`.

---

## PART 3 — Premium edition, step 2 of 2: subscriptions (Google Play Billing)

Product IDs must match `BillingManager.kt` **exactly** or purchases fail.

### 3.1 Create the two subscription products

Play Console → your app → **Monetize → Subscriptions → Create subscription**.

| Product ID | Base plan price | Notes |
|---|---|---|
| `halo_premium_monthly` | **$4.99 / month** | auto-renewing |
| `halo_premium_yearly` | **$49.99 / year** | auto-renewing |

Price lives entirely in Play Console — it is never hardcoded in the app. Play
converts to local currency per region automatically. **Activate** both products;
a draft product is invisible to the app.

### 3.2 Give the service account financial-data permission

This is the step that is easy to miss. The service account already used for
release uploads needs an **additional, separate** scope to read subscription
state via `purchases.subscriptionsv2.get`.

1. Play Console → **Users and permissions**
2. Find the service-account email (ends `@...iam.gserviceaccount.com`)
3. **Account permissions** → tick **View financial data, orders, and
   cancellation survey responses**
4. **Apply / Save changes**

Without this, every purchase verifies as failed and no one gets Premium.

### 3.3 Set the service-account JSON as a *Firebase* secret

This is a **different store** from the GitHub Actions secret of the same name.
Setting one does not set the other. Same JSON file, two places.

```bash
firebase functions:secrets:set PLAY_SERVICE_ACCOUNT_JSON
```

Paste the entire contents of the service-account JSON file.

### 3.4 Redeploy

```bash
cd functions
npm run deploy
```

### 3.5 Wire the URL into the build

Add to `android/gradle.properties`:

```
BILLING_VERIFY_BASE_URL=https://us-central1-halo-2b942.cloudfunctions.net/verifyPurchase
```

**No trailing slash here** — `BillingManager.kt` posts directly to this URL.
(Yes, the opposite of 2.6. That is why they are separate steps.)

### 3.6 Add a licence tester so you can test without paying

Play Console → **Setup → License testing** → add your own Google account.
Licence testers get real purchase flows with test payment instruments — no money
moves, and subscriptions renew on an accelerated timer.

### 3.7 Flip the flag and ship

```kotlin
const val BILLING_ENABLED = true
```

Bump `versionName`, commit, release. **Billing only works from a build installed
through Play** — a sideloaded APK cannot complete a purchase. Use
`internal-app-sharing` or the `internal` track, not a locally built APK.

### 3.8 Verify

1. Open **Halo Premium** in the app → both plans appear with correct prices.
   (No prices = product IDs do not match, or the products are not Activated.)
2. Buy the monthly plan with the licence-tester account → purchase completes.
3. Ads disappear, and the Premium-gated features unlock.
4. Firebase console → Firestore → `users/{your uid}` → confirm
   `premiumActive: true`, `premiumExpiresAt`, `premiumProductId`.
5. Force-close and reopen → Premium survives (entitlement is cached and
   re-checked at launch).

If step 4 shows nothing, the failure is 3.2 or 3.3. Check
`firebase functions:log --only verifyPurchase`.

---

## PART 3B — Tester access (Settings → Go Premium → Become a tester)

Lets testers unlock every Premium feature with a code, before billing is live.
Needs Part 2 done (Blaze + Firebase CLI), but **not** Part 3 — testers work
without any Play subscription products existing.

### 3B.1 Set the code as a Firebase secret

```bash
firebase functions:secrets:set TESTER_CODE
```

Paste `TEST2026HALO` when prompted.

The code is **never** in the repo or the APK. That is the point: a constant
compiled into the app can be read out of any published build with
`strings classes.dex`, and once one tester posts it publicly, every install has
free Premium with no way to revoke it short of shipping a new version. Held
here, rotating it is this command plus a redeploy.

### 3B.2 Deploy

```bash
cd functions && npm run deploy
```

Copy the printed `redeemTesterCode` URL.

### 3B.3 Wire it into the build

Add to `android/gradle.properties` — **no trailing slash**, the app appends
`/redeem` and `/request`:

```
TESTER_REDEEM_BASE_URL=https://us-central1-halo-2b942.cloudfunctions.net/redeemTesterCode
```

### 3B.4 Approving people who have no code

A tester who taps "I don't have a code" and submits their email writes a row to
**`testerRequests`** in Firestore, keyed by their user id (repeat taps update
the same row rather than piling up). Review them at
<https://console.firebase.google.com/project/halo-2b942/firestore/data/~2FtesterRequests>
and mail `TEST2026HALO` to whoever you approve, from `dev.techbyt@gmail.com`.

Nothing notifies you — check the console when you expect requests.

### 3B.5 What a redeemed code does

Writes `premiumActive: true`, `premiumExpiresAt: now + 90 days` and
`premiumProductId: "tester_code"` to `users/{uid}` — the same fields a real
subscription writes, so every Premium gate in the app treats a tester exactly
like a payer. The distinct product id is how you tell them apart in Firestore.

Grants last **90 days** and then lapse on their own. To cut one short, set
`premiumActive: false` on that user's document in the console.

Wrong codes are limited to **10 attempts per user per day** — a short fixed
code is guessable at machine speed otherwise.

---

## PART 4 — The QA gate (needed before your first production release)

`production` forces the QA gate on regardless of the checkbox — it cannot be
skipped. **The gate has never been run successfully**, so budget time to debug it
the first time rather than discovering it during a production push.

1. Confirm the `ANTHROPIC_API_KEY` **GitHub Actions** secret exists (a third
   place, separate from Firebase and from your local key):
   repo → **Settings → Secrets and variables → Actions**
   <https://github.com/thilakmanickam-bot/thilak-thiagarajan.github.io/settings/secrets/actions>
2. Run the release workflow to `internal` with **`run_qa_gate` checked** and see
   whether it passes.
3. Only when it is green, attempt `production`.

> The design-integrity job is report-only and does not block a release; without
> the key you simply get no design feedback. The module/feature/UI test jobs do
> block.

---

## PART 5 — Optional, later

### 5.1 Facebook login (`FACEBOOK_LOGIN_ENABLED`)

Needs a Facebook Developer app, the Firebase Facebook provider, the Facebook SDK
plus manifest entries, and Facebook's app review. The button currently renders
disabled with a "coming soon" hint, which is honest — leave it off unless you
actually want it.

### 5.2 The Tamil month boundary — **I need an answer from you**

There are two competing rules for when a Tamil month starts, and they disagree by
one day. I could not resolve it from first principles and refused to guess. The
contradiction is written up in `docs/RECKONING.md`.

**What to do:** open a printed Tamil panchangam (or a trusted almanac app) and
find **which date Aavani 1 falls on in 2026**.

- **17 August** → the sunset rule is right, and the app needs a fix.
- **18 August** → the sunrise rule is right, and the app is already correct.

Tell me which, and I will close it out.

---

## Appendix A — every console link

| What | URL |
|---|---|
| Play Console | <https://play.google.com/console> |
| Firebase project | <https://console.firebase.google.com/project/halo-2b942> |
| Firestore rules | <https://console.firebase.google.com/project/halo-2b942/firestore/rules> |
| Firebase billing plan | <https://console.firebase.google.com/project/halo-2b942/usage/details> |
| Functions logs | <https://console.firebase.google.com/project/halo-2b942/functions/logs> |
| Anthropic console (API keys) | <https://console.anthropic.com/> |
| AdMob | <https://apps.admob.com/> |
| GitHub Actions secrets | <https://github.com/thilakmanickam-bot/thilak-thiagarajan.github.io/settings/secrets/actions> |
| Release workflow | <https://github.com/thilakmanickam-bot/thilak-thiagarajan.github.io/actions/workflows/release-play.yml> |
| PR #19 | <https://github.com/thilakmanickam-bot/thilak-thiagarajan.github.io/pull/19> |

## Appendix B — the three places a secret can live

A recurring source of confusion. The same value often has to be set more than
once, in stores that do not talk to each other.

| Secret | GitHub Actions | Firebase Secret Manager | Local `gradle.properties` |
|---|---|---|---|
| `ANDROID_KEYSTORE_BASE64` + 3 signing secrets | ✅ set | — | — |
| `PLAY_SERVICE_ACCOUNT_JSON` | ✅ set (uploads) | ❌ **Part 3.3** (billing) | — |
| `ANTHROPIC_API_KEY` | ⚠️ verify (QA gate) | ❌ **Part 2.4** (chat) | — |
| `ADMOB_APP_ID` | — | — | ✅ set (not secret) |
| `CHAT_PROXY_BASE_URL` | — | — | ❌ **Part 2.6** (not secret) |
| `BILLING_VERIFY_BASE_URL` | — | — | ❌ **Part 3.5** (not secret) |

**Never commit:** the keystore file, any keystore password, the Anthropic key,
the Play service-account JSON.
**Safe to commit:** `google-services.json`, the Firebase API key, AdMob IDs —
they ship inside every APK and are not secrets.
**Never hand-edit `google-services.json`** to add a certificate fingerprint. Each
entry needs a real OAuth client ID that only Google can mint; a fabricated one
breaks sign-in in a way that is very hard to diagnose.

## Appendix C — troubleshooting

| Symptom | Cause |
|---|---|
| `Version code N has already been used` | Re-run the workflow; CI uses the run number, which increments. |
| `The caller does not have permission` (upload) | Service-account invite not accepted, or missing the track permission. |
| Signing errors in "Build signed AAB" | Wrong keystore password / alias / key password secret. |
| Chat returns 404 | Missing trailing slash on `CHAT_PROXY_BASE_URL` (Part 2.6). |
| Chat returns 429 | Daily cap hit (Part 2.3). |
| Subscription prices don't show | Product IDs mismatched, or products still in Draft (Part 3.1). |
| Purchase completes but no Premium | Financial-data permission (3.2) or the Firebase secret (3.3). |
| Purchase flow won't start at all | Build was sideloaded. Install through Play (3.7). |
| Saved charts sync stops working | Firestore rules expired or too strict (Part 1.1). |
| Chart disagrees with a printed almanac | Read `docs/RECKONING.md` first — it is usually vakya vs drik, not a bug. |
