# Halo — Play Store listing pack

Everything to paste into the Play Console. Assets are in this folder.

## Identity
- **App name (title, ≤30 chars):** `Halo: Astrology & Charts`
- **Package name:** `com.techbyt.halo`
- **Default language:** English (United States)
- **App or game:** App
- **Free or paid:** Free
- **Category:** Lifestyle
- **Contact email:** thilak.manickam@gmail.com  *(shown publicly on the listing — change if you prefer another)*
- **Privacy policy URL:** `https://thilakmanickam-bot.github.io/thilak-thiagarajan.github.io/privacy-policy.html`
  *(confirm the exact base in repo → Settings → Pages; if a custom domain is set, use that + `/privacy-policy.html`)*

## Short description (≤80 chars)
```
Your cosmic guide: natal charts, daily readings, and a gentle astrologer chat.
```

## Full description (≤4000 chars)
```
Halo is your calm, modern companion for astrology and self-reflection.

Enter your birth details and Halo computes your complete natal chart on your
device — Sun, Moon, and rising signs, all planetary placements, houses, aspects,
and your elemental and modality balance — with clear, friendly interpretations.

✦ Your natal chart
Get an accurate birth chart with an interactive wheel, planetary placements, and
major aspects. Save charts for yourself, friends, and family.

✦ Daily reading
A short, uplifting daily reflection flavoured by your chart, with an optional
gentle morning notification.

✦ Ask the Universe (AI astrologer chat)
Have a warm, thoughtful conversation with an astrologer guide that reflects on
your chosen chart. It offers reflection and encouragement — never fear or fixed
predictions — and suggests small, practical steps. This optional feature uses
your own Anthropic API key and is powered by Claude.

✦ Three languages
Fully available in English, Tamil, and Chinese.

✦ Private by design
Halo is local-first. Your charts are stored on your device. There are no ads, no
trackers, and no account to create. The chat feature only sends what you choose
to share, and only when you use it.

Whether you're new to astrology or a lifelong enthusiast, Halo helps you slow
down, reflect, and see possibilities — gently.

Built by Techbyt.
```

## Graphics (in this folder / to capture)
- **App icon (512×512):** `play-icon-512.png` ✅ provided
- **Feature graphic (1024×500):** `feature-graphic-1024x500.png` ✅ provided
- **Phone screenshots (2–8, required):** capture on a device/emulator after
  installing the app — recommended shots: Home ("Explore your natal chart"),
  Chart wheel, Placements, the Reading tab, and the "Ask the Universe" chat.
  (These need the running app, so they can't be generated here.)

## Content rating questionnaire
- Category: **Reference, News, or Educational** (or Lifestyle).
- Violence / sexual / profanity / drugs / gambling: **No** to all.
- User-generated content shared with others: **No** (the chat is private to the
  user; nothing is shared between users).
- Expected result: **Everyone / PEGI 3**.

## Data safety form
Declare honestly:
- **Does the app collect or share user data?** Yes (for the chat feature only).
- **Data types:**
  - *Personal info → Name* and *App activity / other (birth details you enter)* —
    used for **App functionality** (computing your chart). Stored on-device.
  - *Messages / other user content* (chat messages + chart summary) — **shared**
    with a third party (**Anthropic**) to provide the chat, only when you use it.
- **Is data encrypted in transit?** Yes (HTTPS to Anthropic).
- **Can users request deletion?** Data is on-device; deleting a chart or
  uninstalling removes it.
- **Sold to third parties?** No. **Used for ads?** No. **Analytics?** No.
- The user's Anthropic API key is stored on-device and sent only to Anthropic.

## App access
- All functionality is available without an account/login. (Note: the chat
  requires the user's own Anthropic API key, which is not a login to your app.)

## Ads
- **Contains ads:** No.

## Release notes (first version — "What's new")
```
First release of Halo:
• Compute and save your natal chart on your device
• Daily reading with an optional morning notification
• "Ask the Universe" — a gentle AI astrologer chat (uses your Anthropic key)
• English, Tamil, and Chinese
```

## First-time click path (Play Console)
1. **Create app** with the identity above.
2. **Dashboard → Set up your app:** App access, Ads (No), Content rating,
   Target audience (13+), News app (No), Data safety, Government apps (No),
   Financial features (No), Privacy policy URL.
3. **Grow → Store presence → Main store listing:** paste the copy + upload the
   three graphic types + screenshots.
4. **Test and release → Testing → Internal testing → Create new release:** upload
   the signed `.aab` (from the `Release to Play Store` workflow artifact), add the
   release notes, roll out.
5. When happy, **promote Internal → Production** and submit for review.
