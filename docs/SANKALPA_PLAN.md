# Sankalpa — design and build plan

Status: **plan only, nothing built.** Written against the code as it stands at
v1.2.0.

---

## 1. What a sankalpa is, and what it is not

*Saṅkalpa* — from *sam* ("well-formed", "together") and *kḷp* ("to arrange, to
be fit for"). Usually rendered **resolve**, **determination**, or **the
formed will**.

Two living traditions use the word, and the feature needs both:

**The ritual sense.** In orthodox practice nothing begins without a sankalpa:
a spoken declaration that fixes *where and when* the act is being done, *who is
doing it*, and *what for*. It walks from the largest unit of time down to the
smallest, then to place, then to the person, then to the intent — kalpa,
manvantara, yuga, samvatsara, ayana, ṛtu, māsa, pakṣa, tithi, vāra, nakṣatra,
yoga, karaṇa, then *deśa*, then "aham … kariṣye". The point is that an
intention is **located**: it belongs to a moment, not to nowhere.

**The practice sense.** In the yoga-nidra lineage (Satyananda and after), a
sankalpa is a short statement taken at the start and end of practice and
repeated *unchanged* until it bears fruit. The commonly taught rules are
narrow and worth honouring exactly:

- present tense, stated as already true;
- positively phrased — no "not", no "stop", no "less";
- short enough to say in one breath;
- **one**, not a list;
- **unchanged** until fulfilled — the repetition is the mechanism.

**What it is not.** A sankalpa is not a wish, a request, or an order placed
with the universe. It is a shaping of one's own will, declared in a specific
time and place and renewed. The feature must not drift into outcome promises —
see §8.

---

## 2. Why this belongs in Halo specifically

A sankalpa framing names the exact panchangam elements Halo already computes,
for the user's own place and moment. Almost every "manifestation" app can offer
a text box and a reminder. Halo can offer the **framing** — and it is the only
part of this feature that is hard to copy, because it needs a real panchangam
engine underneath.

That is the feature's centre of gravity. If it becomes a note-taking app with a
streak counter, it is not worth building.

---

## 3. What already exists

From `core/panchangam`, for any date and location:

| Element | Where |
|---|---|
| tithi (0–29) + its name | `DayPanchangam.tithi`, `PanchangamNames.tithiName` |
| pakṣa (Śukla / Kṛṣṇa) | `PanchangamNames.paksha(tithi0)` |
| vāra (weekday 0=Sun) | `DayPanchangam.weekdayIndex`, `PanchangamNames.weekdays` |
| nakṣatra | `DayPanchangam.nakshatra`, `PanchangamNames.nakshatras` |
| yoga | `DayPanchangam.yoga`, `PanchangamNames.yogas` |
| karaṇa | `DayPanchangam.karanaHalf0`, `PanchangamNames.karanaName` |
| māsa (Tamil month) + day | `DayPanchangam.tamilMonthIndex`, `.tamilDay` |
| brahma muhurta | `DayPanchangam.brahmaMuhurta` |
| abhijit muhurta | `DayPanchangam.abhijit` |
| place | `PanchangamLocationStore` (device-derived since v1.2.0) |

All ten observance keys, the moon marks and the reminder plumbing
(`VrathamReminderStore`, `VrathamReminderWorker`, its own notification channel)
also already exist and are the pattern to copy.

---

## 4. What the core is missing

Three elements of the traditional framing are not computed yet. All three are
cheap, pure, and testable, and all derive from the **sidereal solar sign**,
which `Panchangam.sidSunSignAtSunrise` already computes privately.

### 4a. Ayana — the sun's half-year course

Sidereal sun in Makara…Mithuna (signs 9,10,11,0,1,2) → **Uttarāyaṇa**;
Karka…Dhanus (3–8) → **Dakṣiṇāyana**.

Note for the doc comment: this is the *sidereal* reckoning used by the
panchangam, which is why Uttarāyaṇa begins at Makara Saṅkrānti (≈14 January)
and not at the December solstice. Both are called "ayana" in English writing
and conflating them is the obvious bug.

### 4b. Ṛtu — the season

> **Superseded — see §13.** Ritu follows the *lunar* month, not the solar
> sign. The pairing below is left for the record; do not implement it.

Six, two solar signs each: Vasanta (Mesha, Vrishabha), Grīṣma (Mithuna,
Karka), Varṣā (Simha, Kanya), Śarad (Tula, Vrischika), Hemanta (Dhanus,
Makara), Śiśira (Kumbha, Meena).

**A choice to record:** ṛtu can be reckoned by solar month (above) or by lunar
month. Solar is consistent with how this app already derives the Tamil month,
so use solar and say so in the KDoc.

### 4c. Samvatsara — the year in the sixty-year cycle

Sixty names, Prabhava … Akṣaya. The southern reckoning commonly places
Prabhava at 1987–88 CE, giving `index = (year − 1987) mod 60` for a date after
that year's solar new year.

⚠️ **Verify against a printed almanac before shipping.** Competing reckonings
exist (northern vs southern, and different epoch bases), they differ by a
whole-number offset, and a wrong offset is invisible in testing but wrong on
every screen. Do not ship this element on a derivation I worked out from a
formula alone — pin it to at least two known years from an almanac, the way
the existing panchangam tests are pinned to Chennai 2026-08-26.

### 4d. The fixed elements

Kalpa (Śveta-varāha), manvantara (Vaivasvata) and yuga (Kali, first pāda) are
constants for our era. They belong in the string tables, not in a calculation.

**New file:** `core/panchangam/SankalpaFraming.kt` — a pure object returning a
`SankalpaFraming` data class of indices, with names resolved by the UI, exactly
as `DayPanchangam` already works.

---

## 5. The feature

### 5a. Taking a sankalpa

A short guided composition, not a blank text box. Three steps:

1. **What is the resolve?** A single text field with a live check against the
   four rules — present tense, positive, short, singular. The check *advises*,
   it never blocks: the wording is the user's. A negation ("I no longer…")
   raises a hint offering the positive form.
2. **Read the framing.** The generated declaration for this moment and place,
   shown in full. This is the screen that justifies the feature.
3. **Take it.** Stored with the framing of the moment it was taken — the
   *taking* has a date and place, and that is the whole point.

### 5b. The framing itself

Rendered in the user's language, with the Sanskrit term and the local name
together, largest unit to smallest — the traditional order, which is also
genuinely informative:

> Śveta-varāha kalpa · Vaivasvata manvantara · Kali yuga
> **Viśvāvasu** samvatsara · **Dakṣiṇāyana** · **Varṣā** ṛtu
> **Āvaṇi** māsa · **Kṛṣṇa** pakṣa · **Saptamī** tithi
> **Bhānu** vāra · **Kṛttikā** nakṣatra · **Vyāghāta** yoga · **Viṣṭi** karaṇa
> at **Chennai, India**

### 5c. Renewal

The daily practice. Open, read the framing for *today*, restate the resolve.
One tap records the day.

**Counted, not streaked.** Show "observed 43 days" and a quiet calendar of
marked days — not a consecutive-day counter that resets to zero. A streak
turns a practice whose whole virtue is steadiness into something you can lose,
and the loss-aversion that makes streaks work in habit apps is exactly the
wrong pressure here. This is a deliberate product decision, not an oversight.

### 5d. Auspicious moments

Both traditional windows for taking or renewing a sankalpa are already
computed: **brahma muhurta** (before sunrise) and **abhijit muhurta** (around
midday). Show today's, and offer a reminder at brahma muhurta.

---

## 6. Data model

Room, database **v3 → v4**, following `SavedMatchEntity` exactly:

```
sankalpa
  id                INTEGER PK
  statement         TEXT NOT NULL      -- the resolve, unchanged once taken
  takenAt           TEXT NOT NULL      -- when it was taken
  takenLocationName TEXT NOT NULL      -- where
  takenLatitude     REAL NOT NULL
  takenLongitude    REAL NOT NULL
  takenTimeZone     TEXT NOT NULL
  fulfilledAt       TEXT               -- null while live
  releasedAt        TEXT               -- null unless abandoned

sankalpa_observance
  id          INTEGER PK
  sankalpaId  INTEGER NOT NULL
  observedOn  TEXT NOT NULL            -- a local date, one row per day
```

Store the **coordinates**, not just the display name, so the framing of the
taking can be recomputed exactly — the same reasoning as `saved_matches`, and
the same reason it stores inputs rather than results.

Unique index on `(sankalpaId, observedOn)` so a double tap cannot record a day
twice.

`MIGRATION_3_4` is mandatory — the database already holds users' charts and
matches, and Room throws at open time on a version bump without one. The
existing `SavedMatchMigrationTest` is the pattern: drive the SQL against a
hand-built v3 database and assert the column shape, since `exportSchema` is
false and `MigrationTestHelper` therefore cannot be used.

---

## 7. Reminders

Reuse `VrathamReminderWorker`'s shape exactly: one daily periodic worker that
asks "is there a live sankalpa, and has today been observed?" rather than
scheduling per-occurrence work. Fire at brahma muhurta rather than a fixed
hour, since that time is already computed per location and is the traditional
window.

Its own channel again, for the same reason the vratham reminders got one:
channel importance is immutable once created.

---

## 8. Integrity, and Play policy

The feature describes a **practice**, never an **outcome**. Concretely:

- no copy of the form "manifest", "attract", "the universe will", "your wish
  will come true";
- nothing that reads as a health, financial or medical claim — Play's
  policies bite here, and Halo already carries a subscription;
- the guidance is about *how the tradition says to phrase a resolve*, which is
  a factual claim about the tradition, not a claim about results;
- the framing is presented as what it is: the traditional way of locating an
  intention in time and place.

This constraint is the reason to build sankalpa rather than a generic
manifestation feature. It is defensible, it is culturally grounded, and it
suits an app whose panchangam is pinned to a published almanac.

---

## 9. Premium boundary

Consistent with the vratham reminders:

| | Basic | Premium |
|---|---|---|
| Take one sankalpa | ✅ | ✅ |
| See today's framing | ✅ | ✅ |
| Record the day | ✅ | ✅ |
| Brahma-muhurta reminder | greyed | ✅ |
| History beyond 30 days | greyed | ✅ |
| Export the framing as a PDF | greyed | ✅ |

The practice itself is never paywalled. Charging for the resolve would be
distasteful and would also be the wrong business call — the practice is what
brings someone back daily.

PDF export reuses `MatchPdf`'s machinery (`PdfDocument`, the FileProvider and
`cache/shared`, the `wrap` helper), which all exist since v1.2.0.

---

## 10. Phasing

Each phase ships CI-green and installable before the next begins, as the
matchmaking work did.

**Phase A — core.** `SankalpaFraming.kt`: ayana, ṛtu, samvatsara and the fixed
elements, plus their names in all eight languages. Pure, no UI. Tests pinned to
an almanac, including the samvatsara verification called out in §4c.
*Nothing user-visible ships in this phase.*

**Phase B — take and read.** Database v4 + migration, the composition flow, the
framing screen, storage. Entry point from the home screen.

**Phase C — renewal.** Observance recording, the counted history, today's
brahma and abhijit muhurta on the sankalpa screen.

**Phase D — reminders and Premium.** The worker, its channel, the greyed
controls for Basic, PDF export of the framing.

---

## 11. Testing

- **Core (pure, off-device):** ayana at both saṅkrānti boundaries; each ṛtu
  at its first and last solar day; samvatsara against almanac-known years;
  the framing assembled for the almanac-pinned date the existing
  `PanchangamTest` already uses, so it cross-checks the elements it shares.
- **Migration:** v3 → v4 against a hand-built v3 database, asserting existing
  charts *and* saved matches survive, and the new columns match the entity.
- **Store/DAO:** observance uniqueness — recording the same day twice leaves
  one row.
- **UI:** the guidance advises without blocking; a negated statement raises the
  hint; the Basic user sees the reminder control greyed and it reports nothing;
  a released sankalpa stops appearing as live.
- **On device:** the framing reads correctly in Tamil and in Hindi (the two
  scripts most likely to expose a font or ordering problem), and the
  brahma-muhurta reminder fires.

---

## 12. Decisions (answered)

All four are settled; recorded here so they are not reopened.

1. **Several sankalpas in parallel**, not one at a time. Less faithful to the
   tradition, which is emphatic that a sankalpa is singular, but the user's
   call and the friendlier app. The data model already allows it.
2. **Plain transliteration** — *Krishna paksha*, *ritu*, *samvatsara* — never
   IAST diacritics, matching the rest of the app. In the Indic scripts the terms
   are written natively; the question only ever applied to the Latin locales.
   Implemented in `SankalpaStrings.kt` across all eight languages.
3. **Samvatsara epoch: pinned and verified**, no almanac needed in the end. The
   cycle is fixed by four independent facts that must hold at once — Prabhava is
   1987, Parabhava is 2026, Krodhi and Vishvavasu are 2024 and 2025, and the
   traditional Brahma/Vishnu/Shiva grouping puts Vyaya at 20, Sarvajit at 21 and
   Parabhava at 40. `index = ((startYear − 66) mod 60)`, asserted in
   `HinduYearTest`.
4. **Its own destination**, reached from a bottom navigation bar carrying Home,
   Calendar, Sankalpa, Rasi Palan and Settings — Calendar and Settings moving
   down out of the top app bar. `AppBottomNav.kt` exists; wiring it into
   `MainActivity` is the remaining step.

## 13. Correction: ritu follows the *lunar* month

§4b originally paired ritu with solar signs. That was wrong, and the sources
disagree with each other on the solar pairing in a way that would have produced
a silently wrong season.

The classical definition every source agrees on is by **lunar month**, two at a
time: Vasanta = Chaitra + Vaishakha, Grishma = Jyeshtha + Ashadha, Varsha =
Shravana + Bhadrapada, Sharad = Ashvina + Kartika, Hemanta = Margashirsha +
Pausha, Shishira = Magha + Phalguna.

It is also the right choice on the merits: a sankalpa recites the **lunar**
month ("chaitre masi"), not the solar one, so the whole framing should rest on
the same reckoning. `HinduYear.ritu(lunarMonthIndex)` implements it.

Two further corrections landed with it, both caught by CI rather than by
reading:

- **Puthandu is not "the first sunrise with the Sun in Mesha".** Tamil Nadu
  begins a solar month on the day of the sankranti when it falls before sunset.
  In 2026 the ingress is 14 April 09:03 IST, so a sunrise test answers the 15th
  against a printed 14th.
- **Ugadi is not "the first sunrise after the Chaitra new moon".** In 2026 the
  new moon is at 06:54 IST, 38 minutes *after* sunrise, and the pratipada it
  opens ends before the next one — a kshaya tithi, touching no sunrise at all.
  The day it begins on takes the name, which is why the published date is the
  19th. `HinduYear.lunarMonth` carries the same rule, so Ugadi reads Chaitra
  rather than Phalguna.
