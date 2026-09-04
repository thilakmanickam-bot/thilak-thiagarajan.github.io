# How Halo computes, and why it may differ from your almanac

Written because the most likely support message this app will ever receive is
*"your chart doesn't match my panchangam"* — and the honest answer is usually
neither "we have a bug" nor "your almanac is wrong".

## Halo is drik

There are two living traditions for computing planetary positions in Indian
astrology.

**Drik ganita** ("observational") uses modern astronomy: positions that match
where the planets actually are, as a telescope would confirm. Every modern
almanac uses it — Drik Panchang, Prokerala, and the professional software that
astrologers themselves use.

**Vakya** ("sentence") uses medieval algorithms, memorised as verses, refined
over centuries but frozen before modern astronomy. Vakya panchangams are still
printed and still used, particularly in Tamil Nadu, and many hand-computed
jathagams are vakya.

**Halo is drik.** Everything below describes that choice and how far it is
verified.

Vakya may become a Settings option later. It is not a small job — it is a
second, per-planet algorithm set, not a constant to subtract — so it is not
promised, only kept open.

## The numbers

Halo computes positions on the device. There is no network call, no ephemeris
data file, and birth details never leave the phone. That is deliberate: a
panchangam has to work with no signal, and birth date, time and *place* is
about as personal as data gets.

Accuracy is verified in CI against the **Swiss Ephemeris**, the reference
professional astrology software agrees on. Over 891 sample dates from 1950 to
2050, the worst disagreement:

| body | worst error | as a fraction of a pada (3°20′) |
|---|---|---|
| Rahu / Ketu | 0.000° | 0.00 |
| Sun | 0.015° | 0.00 |
| Moon | 0.019° | 0.01 |
| Ascendant (lagnam) | 0.006° | 0.00 |
| Mercury | 0.708° | 0.21 |
| Venus | 0.710° | 0.21 |
| Jupiter | 0.712° | 0.21 |
| Mars | 0.721° | 0.22 |
| Saturn | 0.819° | 0.25 |

The Lahiri ayanamsa model tracks Swiss's to 0.0053°.

Read that table as: **the rasi is never in doubt.** The five planets can be up
to a quarter of a pada out, so a planet sitting within ~0.8° of a pada boundary
could be shown on the wrong side of it. The Moon, Sun, nodes and lagnam — which
between them determine your rasi, nakshatram, porutham and Vimshottari dasha —
are three orders of magnitude better than that.

`tools/ephemeris_oracle.py` regenerates the golden values; CI runs it in
`--check` mode, so they cannot be edited to make a failing test pass. Swiss
Ephemeris is a **development and CI tool only and is never bundled into the
app** — it is dual-licensed AGPL-3.0 or commercial, and bundling it would mean
either publishing Halo's source or buying a licence from Astrodienst.

## What Halo uses

- **Zodiac:** Nirayana (sidereal), **Lahiri / Chitra Paksha** ayanamsa.
- **Houses:** whole sign (rashi = bhava).
- **Chart:** South Indian koshtam — twelve fixed cells, clockwise from Meenam
  top-left. Nine grahas plus the lagnam; no Uranus, Neptune or Pluto, which have
  no place in a jathagam.
- **Nodes:** the **mean** node. Tamil almanacs tabulate the mean node; the true
  node oscillates around it by up to ~1.6°, half a pada.
- **Tamil calendar:** the month is taken from the Sun's sidereal sign at
  sunrise. **This is an open question** — see below.
- **Year:** both reckonings are supported, because South India genuinely uses
  two. The Tamil year turns at Mesha sankranti (Puthandu, ~14 April); the
  Telugu, Kannada and Marathi year turns at Chaitra shukla pratipada (Ugadi,
  which was 19 March in 2026). Between those dates the two disagree on the
  samvatsara name, and **both are right**.

### Settled: the Tamil month boundary is two rules, not one

**Resolved from a printed Tamil panchangam** (2026): Chithirai 1 = **14 April**,
Aavani 1 = **18 August**, Purattasi 1 = **18 September**. All three are
confirmed print, and together they rule out every single-threshold rule.

Measured against the Swiss Ephemeris at Chennai:

| month | sankranti (IST) | sunrise | after sunrise by | printed start | resolution |
|---|---|---|---|---|---|
| Chithirai | 14 Apr 09:33 | 05:58 | 3h 35m | 14 Apr | **same day** |
| Aavani | 17 Aug 07:59 | 05:58 | 2h 01m | 18 Aug | **next day** |
| Purattasi | 17 Sep 07:53 | 05:59 | 1h 54m | 18 Sep | **next day** |

Both transits are in the morning, after sunrise. The *later* one resolves to the
same day and the *earlier* one to the next day, so the outcome is anti-monotonic
in "time after sunrise" — **no cutoff, whether sunrise, sunset or aparahna, can
produce both.** A uniform sunrise rule gives 15 Apr (wrong); a uniform sunset
rule gives 17 Aug (wrong).

The reason is that they are not the same kind of boundary. **Puthandu is a civil
holiday fixed to 14 April** by the Tamil Nadu government, independent of the
astronomical rule; ordinary months follow the drik sunrise rule. Testing the two
rules against Mesha across 2020–2030 shows exactly that:

| | 2020 | 2021 | 2022 | 2023 | 2024 | 2025 | 2026 | 2027 | 2028 | 2029 | 2030 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| sunset rule | 14 | 14 | 14 | 14 | 14 | 14 | 14 | 14 | 14 | 14 | 14 |
| sunrise rule | 14 | 14 | **15** | **15** | 14 | 14 | **15** | **15** | 14 | 14 | **15** |

The sunset rule reproduces 14 April in every year — it works as a proxy for the
fixed civil date. The sunrise rule would move Puthandu to 15 April in five years
out of eleven.

So the two code paths differ **on purpose**:

- `Panchangam.tamilDate()` — sunrise rule, for ordinary month boundaries.
  Verified by Aavani 1 = 18 Aug and Purattasi 1 = 18 Sep 2026, two
  independent months whose sankrantis both land ~2h after sunrise.
- `HinduYear.meshaSankranti()` — sunset rule, for the Tamil year boundary.
  Verified by Puthandu = 14 Apr across eleven years.

**Do not "fix" the inconsistency between them.** Aligning them either way breaks
one of the two confirmed dates. `TamilBoundaryTest` pins both.

The ingress times themselves are not in doubt: Makara computes to 14 Jan 15:07
against a published 15:13, and Simha to 17 Aug 07:59 against a published
07:50–08:04.

Evidence now stands at three printed dates: two for the sunrise rule (Aavani,
Purattasi) and one for the sunset rule, that one backed by the eleven-year
Puthandu table above. `TamilBoundaryTest` pins all three.

### Where the two rules disagree, all twelve months of 2026

Scanned against the Swiss Ephemeris at Chennai. The two rules part company on
**7 of 12 months**, so this is not a corner case:

| month | sankranti (IST) | sunrise | sunset | sunrise rule | sunset rule | differ |
|---|---|---|---|---|---|---|
| Thai | 14 Jan 15:07 | 06:36 | 18:00 | 15 Jan | 14 Jan | **yes** |
| Maasi | 13 Feb 04:09 | 06:33 | 18:13 | 13 Feb | 13 Feb | — |
| Panguni | 15 Mar 01:03 | 06:17 | 18:18 | 15 Mar | 15 Mar | — |
| Chithirai | 14 Apr 09:33 | 05:58 | 18:21 | 15 Apr | **14 Apr** ✓ | **yes** |
| Vaikasi | 15 May 06:22 | 05:44 | 18:26 | 16 May | 15 May | **yes** |
| Aani | 15 Jun 12:53 | 05:44 | 18:35 | 16 Jun | 15 Jun | **yes** |
| Aadi | 16 Jul 23:39 | 05:51 | 18:39 | 17 Jul | 17 Jul | — |
| Aavani | 17 Aug 07:59 | 05:58 | 18:28 | **18 Aug** ✓ | 17 Aug | **yes** |
| Purattasi | 17 Sep 07:53 | 05:59 | 18:08 | **18 Sep** ✓ | 17 Sep | **yes** |
| Aippasi | 17 Oct 19:52 | 06:00 | 17:48 | 18 Oct | 18 Oct | — |
| Karthigai | 16 Nov 19:43 | 06:09 | 17:38 | 17 Nov | 17 Nov | — |
| Margazhi | 16 Dec 10:25 | 06:25 | 17:44 | 17 Dec | 16 Dec | **yes** |

✓ marks a date confirmed in print.

### Thai closes it: Chithirai is the only exception

**Thai 1 = 15 January in both 2026 and 2027**, confirmed in print. Thai 1 is
Pongal, so this was the case most likely to be a second civil exception, and it
is not one.

The two years carry different weight and it is worth not conflating them:

| year | Makara sankranti | sunrise | sunset | sunrise rule | sunset rule | printed |
|---|---|---|---|---|---|---|
| 2026 | 14 Jan **15:07** | 06:36 | 18:00 | 15 Jan | 14 Jan | **15 Jan** |
| 2027 | 14 Jan **21:10** | 06:36 | 18:00 | 15 Jan | 15 Jan | **15 Jan** |

**2026 is the discriminator.** Its sankranti falls between sunrise and sunset,
so the two rules split, and print sides with sunrise. It also kills the theory
that the year-opening months are all pinned to the 14th like Puthandu. The 2027
sankranti is after sunset, where both rules agree — a consistency check across a
year rollover, not independent evidence for the rule.

So the model is complete, on four printed dates spanning two years:

- **Every ordinary month** follows the drik sunrise rule — Thai, Aavani and
  Purattasi, each measured against a sankranti that falls after sunrise.
- **Chithirai alone** is civil-fixed to 14 April, which the sunset rule
  reproduces across 2020–2030.

That Pongal lands on 15 January 2026 while Puthandu never leaves 14 April is
the asymmetry the two rules encode, and the reason they must stay different.

The app has **no solar festivals at all** — the observances in
`MonthPanchangam` are tithi- and nakshatra-based — so nothing displays Pongal;
the panchangam screen simply shows Thai 1 on 15 January 2026.
## If your printed jathagam differs

Work down this list before assuming a bug.

1. **Different rasi for a graha?** That is a real disagreement worth reporting.
   Nothing in the tolerances above can move a planet a whole sign.
2. **Same rasi and nakshatram, different pada?** Most likely drik versus vakya.
   Vakya offsets run in *different directions* for different planets — which is
   exactly how they were identified in the reference chart used to test this
   engine, where the sheet's Moon was 1.8° behind, Venus 3.5° ahead, Saturn 5.6°
   behind and Mercury 7.2° behind.
3. **Lagnam differs?** Check the birth time first. The ascendant moves about one
   degree every four minutes, so a four-minute rounding is a whole degree, and a
   two-hour error is a whole sign. Halo's lagnam agrees with Swiss to 0.006°.
4. **Dasha balance differs?** That is a read on the Moon's longitude. Balance
   divided by the lord's years gives the fraction of the nakshatra remaining,
   which pins the Moon to a few arcminutes — a useful cross-check, and usually
   it traces back to the birth time or to vakya.
5. **Rahu and Ketu not opposite in your sheet?** They are always exactly 180°
   apart. If a sheet shows otherwise, the sheet has an error.

Please include the birth date, time and place, and the source of the sheet, in
any report. "Your Saturn is wrong" cannot be acted on; "Saturn shows P.Ashadha
pada 3, my vakya panchangam says pada 1" can.

## Verifying a change yourself

```
python3 -m pip install pyswisseph
python3 tools/ephemeris_oracle.py --check     # golden values still match Swiss
gradle -p android core:test                   # the engine still matches the golden
```

`SolachiChartTest` pins a real hand-computed jathagam;
`EphemerisInvariantsTest` pins the facts no ephemeris may violate — Mercury
never more than 28° from the Sun, Venus never more than 48°, the node always
opposite Ketu and always regressing. The `jyotishi` agent
(`.claude/agents/jyotishi.md`) audits the whole app against classical rules.
