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

## 12. Open questions for the user

1. **One at a time, or several?** The tradition is emphatic that a sankalpa is
   singular. The plan above assumes one live at a time, with past ones kept.
   Allowing several would be more app-like and less faithful.
2. **Sanskrit transliteration style.** IAST with diacritics (*Kṛṣṇa pakṣa*) or
   plain (*Krishna paksha*)? The app currently uses plain elsewhere — matching
   that is probably right, but the framing is the one place diacritics would
   be defensible.
3. **Samvatsara epoch** — §4c. If you have a printed almanac to hand, two
   known years settles it and I will pin the test to them.
4. **Where does it live?** A home-screen entry of its own, or inside the
   panchangam, where the muhurta windows already are?
