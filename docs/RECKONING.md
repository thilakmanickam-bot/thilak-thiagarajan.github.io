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

### Open: the Tamil month boundary

Halo currently starts a Tamil solar month at the first sunrise with the Sun in
the new sign. The commonly stated Tamil rule is different — the month begins on
the day of the sankranti when that falls before sunset — and the two disagree
for 7 of the 12 months in 2026.

The public evidence does not settle it, and it is worth setting out so nobody
"fixes" this from a half-memory:

| month | sankranti (IST) | published start | implies |
|---|---|---|---|
| Chithirai | 14 Apr 09:32 | 14 Apr (Puthandu) | same day |
| Aavani | 17 Aug 07:58 | 18 Aug | next day |
| Purattasi | 17 Sep 07:52 | 18 Sep | next day |

All three sankrantis are in the morning, and they imply opposite rules. No
cutoff — sunrise, sunset, or aparahna — produces both.

The ingress times themselves are not in doubt: Makara computes to 14 Jan 15:07
against a published 15:13, and Simha to 17 Aug 07:58 against a published
07:50–08:04.

The likely resolution is that **Puthandu and Pongal are civil holidays fixed by
the Tamil Nadu government** to 14 April and 14 January, independent of the
astronomical rule — in which case Aavani and Purattasi are the honest evidence,
the sunrise rule is right, and the app is already correct.

One printed Tamil panchangam settles all twelve months: if it gives **Aavani 1 =
17 August 2026** the sunset rule is right; **18 August** and the sunrise rule is.
Until then `Panchangam.tamilDate()` is left alone.

(`HinduYear.meshaSankranti` currently uses the sunset rule and so may disagree
with `tamilDate` by a day on the Chithirai boundary. It only shifts the
samvatsara name within that one-day window, and it is part of the same open
question.)
- **Year:** both reckonings are supported, because South India genuinely uses
  two. The Tamil year turns at Mesha sankranti (Puthandu, ~14 April); the
  Telugu, Kannada and Marathi year turns at Chaitra shukla pratipada (Ugadi,
  which was 19 March in 2026). Between those dates the two disagree on the
  samvatsara name, and **both are right**.

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
