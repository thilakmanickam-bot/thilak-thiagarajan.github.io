#!/usr/bin/env python3
"""Generate and verify golden ephemeris values from the Swiss Ephemeris.

Halo computes planetary positions itself, on device, with no network and no
data files (see docs/RECKONING.md). That is the right call for a panchangam
app, but it means nothing outside the app checks the arithmetic — and the
formulas this replaced were wrong by up to 135 degrees and shipped for months.

So the Swiss Ephemeris is used here as an *oracle*: it never ships, it is not a
dependency of the app, it runs in CI to produce numbers the Kotlin must match.

    python3 tools/ephemeris_oracle.py --generate   # rewrite the golden file
    python3 tools/ephemeris_oracle.py --check      # fail if it is stale

`--check` regenerates into memory and diffs against the committed file. That is
the property worth having: the golden values cannot be quietly edited to make a
failing test pass, because CI would then find the file disagrees with Swiss.

Licensing: the Swiss Ephemeris is dual-licensed AGPL-3.0 or commercial. It is
used strictly as a development and CI verification tool and is never bundled
into the APK, so neither obligation attaches to Halo. Do not add it as an app
dependency without resolving that first.
"""

import argparse
import datetime
import pathlib
import sys

try:
    import swisseph as swe
except ImportError:  # pragma: no cover - the CI step installs it
    sys.exit("pyswisseph is not installed. Run: pip install pyswisseph")

REPO = pathlib.Path(__file__).resolve().parent.parent
GOLDEN = (
    REPO / "android/core/src/test/kotlin/com/astrochart/core/utils/EphemerisGolden.kt"
)

# Swiss body handles, keyed by the names the app uses. Ketu is not requested
# from Swiss: it is defined as the point opposite Rahu, and asserting that
# relation in Kotlin is stronger than asserting a number.
BODIES = [
    ("Sun", swe.SUN),
    ("Moon", swe.MOON),
    ("Mercury", swe.MERCURY),
    ("Venus", swe.VENUS),
    ("Mars", swe.MARS),
    ("Jupiter", swe.JUPITER),
    ("Saturn", swe.SATURN),
    ("Rahu", swe.MEAN_NODE),
]

# Located cases. Every case carries a place, so the ascendant is checked too —
# it is the part of the chart that was most wrong (seven signs) and it is
# location-dependent, so a date-only sweep would not cover it.
#
# The first is the reference jathagam that drove the whole rebuild
# (see SolachiChartTest). The rest span latitude deliberately: a high northern
# latitude, a southern-hemisphere birth, and the equator, because the ascendant
# formula is where latitude bites.
CHARTS = [
    ("Solachi, Karaikkudi", 1989, 12, 21, 7.5333, 10.0730, 78.7833),
    ("Chennai, midday", 2000, 1, 1, 6.5, 13.0827, 80.2707),
    ("Delhi, near midnight", 1975, 6, 30, 18.5, 28.6139, 77.2090),
    ("London, winter", 1960, 12, 31, 23.0, 51.5074, -0.1278),
    ("Sydney, southern hemisphere", 2010, 9, 15, 2.0, -33.8688, 151.2093),
    ("Singapore, equatorial", 2019, 6, 13, 5.1333, 1.3521, 103.8198),
]

# The sweep is placed at Chennai so its ascendant is meaningful too.
SWEEP_LAT, SWEEP_LON = 13.0827, 80.2707

SWEEP_START = datetime.date(1950, 1, 1)
SWEEP_END = datetime.date(2050, 1, 1)
SWEEP_STEP = 307


def sidereal(jd: float, body: int) -> float:
    """Lahiri sidereal ecliptic longitude, degrees."""
    return swe.calc_ut(jd, body, swe.FLG_SWIEPH | swe.FLG_SIDEREAL)[0][0]


def case(label: str, y: int, mo: int, d: int, hour: float, lat: float, lon: float):
    """One golden case: every body plus the ascendant, sidereal."""
    jd = swe.julday(y, mo, d, hour)
    longitudes = [f"{sidereal(jd, handle):.6f}" for _, handle in BODIES]
    _, ascmc = swe.houses_ex(jd, lat, lon, b"W", swe.FLG_SIDEREAL)
    return (label, jd, y, mo, d, hour, lat, lon, longitudes, ascmc[0])


def build() -> str:
    swe.set_sid_mode(swe.SIDM_LAHIRI)

    cases = [case(*c) for c in CHARTS]
    d = SWEEP_START
    while d < SWEEP_END:
        cases.append(
            case(d.isoformat(), d.year, d.month, d.day, 12.0, SWEEP_LAT, SWEEP_LON)
        )
        d += datetime.timedelta(days=SWEEP_STEP)

    names = ", ".join(f'"{n}"' for n, _ in BODIES)
    version = swe.version
    ayan = swe.get_ayanamsa_ut(swe.julday(2000, 1, 1, 12.0))

    out = [
        "package com.astrochart.core.utils",
        "",
        "/**",
        " * GENERATED — do not edit by hand.",
        " *",
        " * Golden sidereal longitudes from the Swiss Ephemeris (Lahiri), the",
        f" * reference professional packages agree on. Swiss version {version}; its",
        f" * Lahiri ayanamsa at J2000 is {ayan:.4f}°.",
        " *",
        " * Regenerate with `python3 tools/ephemeris_oracle.py --generate`. CI runs",
        " * `--check`, which fails if this file disagrees with Swiss — so it cannot",
        " * be edited to make a failing test pass.",
        " *",
        " * Swiss Ephemeris is a dev/CI tool only and is never bundled into the app;",
        " * see docs/RECKONING.md and the header of the generator script.",
        " */",
        "internal object EphemerisGolden {",
        "",
        "    /** Body order for every row's `longitudes`. */",
        f"    val bodies: List<String> = listOf({names})",
        "",
        "    /**",
        "     * One instant at one place. [hourUt] is UT decimal hours; [longitudes]",
        "     * and [ascendant] are sidereal (Lahiri), degrees.",
        "     */",
        "    data class Case(",
        "        val label: String,",
        "        val jd: Double,",
        "        val year: Int,",
        "        val month: Int,",
        "        val day: Int,",
        "        val hourUt: Double,",
        "        val latitude: Double,",
        "        val longitude: Double,",
        "        val longitudes: List<Double>,",
        "        val ascendant: Double",
        "    )",
        "",
        f"    /** {len(CHARTS)} located charts, then {len(cases) - len(CHARTS)} dates",
        f"     *  {SWEEP_START.year}–{SWEEP_END.year} at {SWEEP_STEP}-day steps (a prime, so the",
        "     *  sampling never locks to any planet's period). */",
        "    val cases: List<Case> = listOf(",
    ]
    for label, jd, y, mo, dy, hour, lat, lon, longitudes, asc in cases:
        out.append(
            f'        Case("{label}", {jd:.6f}, {y}, {mo}, {dy}, {hour}, '
            f'{lat}, {lon}, listOf({", ".join(longitudes)}), {asc:.6f}),'
        )
    out += ["    )", "}", ""]
    return "\n".join(out)


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    g = ap.add_mutually_exclusive_group(required=True)
    g.add_argument("--generate", action="store_true", help="rewrite the golden file")
    g.add_argument("--check", action="store_true", help="fail if the golden file is stale")
    args = ap.parse_args()

    fresh = build()

    if args.generate:
        GOLDEN.parent.mkdir(parents=True, exist_ok=True)
        GOLDEN.write_text(fresh, encoding="utf-8")
        n = fresh.count("        Case(")
        print(f"wrote {GOLDEN.relative_to(REPO)} — {n} cases")
        return 0

    if not GOLDEN.exists():
        print(f"missing {GOLDEN.relative_to(REPO)}; run --generate", file=sys.stderr)
        return 1

    committed = GOLDEN.read_text(encoding="utf-8")
    if committed == fresh:
        print(f"{GOLDEN.relative_to(REPO)} matches the Swiss Ephemeris")
        return 0

    import difflib

    diff = list(
        difflib.unified_diff(
            committed.splitlines(),
            fresh.splitlines(),
            fromfile="committed",
            tofile="swiss ephemeris",
            lineterm="",
            n=1,
        )
    )
    print(
        f"{GOLDEN.relative_to(REPO)} disagrees with the Swiss Ephemeris.\n"
        "Either it was hand-edited, or the sample set changed and it needs\n"
        "regenerating with --generate. First 40 diff lines:\n",
        file=sys.stderr,
    )
    print("\n".join(diff[:40]), file=sys.stderr)
    return 1


if __name__ == "__main__":
    sys.exit(main())
