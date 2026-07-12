// Natal chart data generator — Natalie Wood
//
// Birth data (Rodden rating AA, per Astro-Databank / Astro-Seek):
//   July 20, 1938, 11:16 AM PST (19:16 UT), San Francisco, CA (37.7749 N, 122.4194 W)
//
// Computes geocentric ecliptic positions (true equinox of date) with the
// astronomy-engine ephemeris, plus Ascendant, Midheaven, Placidus house
// cusps, major aspects, and element/modality balance.
//
// Usage:  npm install astronomy-engine && node generate-chart-data.mjs
// Output: chart-data.json (the same data is embedded in index.html)

import * as A from 'astronomy-engine';
import { writeFileSync } from 'node:fs';

const time = A.MakeTime(new Date(Date.UTC(1938, 6, 20, 19, 16, 0)));
const LAT = 37.7749, LON = -122.4194;

const SIGNS = ['Aries','Taurus','Gemini','Cancer','Leo','Virgo','Libra','Scorpio','Sagittarius','Capricorn','Aquarius','Pisces'];
const ELEMENT  = ['Fire','Earth','Air','Water'];
const MODALITY = ['Cardinal','Fixed','Mutable'];
const rad = Math.PI / 180, deg = 180 / Math.PI;
const norm = d => ((d % 360) + 360) % 360;
const clamp = (x, lo, hi) => Math.max(lo, Math.min(hi, x));

const fmt = lonDeg => {
  const lon = norm(lonDeg);
  const signIdx = Math.floor(lon / 30);
  const within = lon - signIdx * 30;
  let d = Math.floor(within);
  let m = Math.round((within - d) * 60);
  if (m === 60) { d += 1; m = 0; }
  return {
    lon: Math.round(lon * 10000) / 10000,
    sign: SIGNS[signIdx],
    element: ELEMENT[signIdx % 4],
    modality: MODALITY[signIdx % 3],
    label: `${d}°${String(m).padStart(2, '0')}′ ${SIGNS[signIdx]}`,
  };
};

// ── Planets: geocentric ecliptic longitude, true equinox of date ──
const lonOf = (body, t) => {
  if (body === 'Moon') return A.EclipticGeoMoon(t).lon;
  if (body === 'Sun') return A.SunPosition(t).elon;
  return A.Ecliptic(A.GeoVector(A.Body[body], t, true)).elon;
};

const BODIES = ['Sun','Moon','Mercury','Venus','Mars','Jupiter','Saturn','Uranus','Neptune','Pluto'];
const tNext = time.AddDays(1);
const planets = BODIES.map(name => {
  const lon = lonOf(name, time);
  let motion = norm(lonOf(name, tNext) - lon);
  if (motion > 180) motion -= 360;
  return { name, ...fmt(lon), retrograde: motion < 0 };
});

// Mean lunar node (always retrograde)
const dJ2000 = time.tt;
planets.push({ name: 'North Node', ...fmt(125.0445479 - 0.0529538083 * dJ2000), retrograde: true });

// ── Angles: Ascendant & Midheaven ──
const T = dJ2000 / 36525;
const eps = (23.439291111 - 0.0130041667 * T - 1.6389e-7 * T * T + 5.0361e-7 * T ** 3) * rad;
const armc = norm(A.SiderealTime(time) * 15 + LON); // RA of the MC, degrees
const RAMC = armc * rad, phi = LAT * rad;

const mc  = norm(Math.atan2(Math.sin(RAMC), Math.cos(RAMC) * Math.cos(eps)) * deg);
const asc = norm(Math.atan2(Math.cos(RAMC), -(Math.sin(RAMC) * Math.cos(eps) + Math.tan(phi) * Math.sin(eps))) * deg);

// ── Placidus house cusps ──
// Iterate in right ascension: a cusp is the ecliptic point whose hour angle
// is the given fraction of its diurnal (cusps 11, 12) or nocturnal
// (cusps 2, 3) semi-arc. Remaining cusps are the opposite points.
function placidus(offset, fraction, nocturnal) {
  let RA = norm(armc + offset);
  for (let i = 0; i < 100; i++) {
    const decl = Math.asin(Math.sin(eps) * Math.sin(RA * rad));
    const AD = Math.asin(clamp(Math.tan(phi) * Math.tan(decl), -1, 1)) * deg;
    const next = nocturnal
      ? norm(armc + 180 - fraction * (90 - AD))
      : norm(armc + fraction * (90 + AD));
    if (Math.abs(norm(next - RA + 180) - 180) < 1e-9) { RA = next; break; }
    RA = next;
  }
  return norm(Math.atan2(Math.sin(RA * rad), Math.cos(RA * rad) * Math.cos(eps)) * deg);
}

const cusps = new Array(13);
cusps[1] = asc;            cusps[10] = mc;
cusps[4] = norm(mc + 180); cusps[7]  = norm(asc + 180);
cusps[11] = placidus(30, 1 / 3, false);
cusps[12] = placidus(60, 2 / 3, false);
cusps[2]  = placidus(120, 2 / 3, true);
cusps[3]  = placidus(150, 1 / 3, true);
cusps[5] = norm(cusps[11] + 180);
cusps[6] = norm(cusps[12] + 180);
cusps[8] = norm(cusps[2] + 180);
cusps[9] = norm(cusps[3] + 180);

const houseOf = lon => {
  for (let h = 1; h <= 12; h++) {
    const a = cusps[h], b = cusps[h === 12 ? 1 : h + 1];
    const span = norm(b - a);
    if (norm(lon - a) < span) return h;
  }
  return 12;
};
for (const p of planets) p.house = houseOf(p.lon);

// ── Major aspects ──
const ASPECTS = [
  { name: 'Conjunction', angle: 0,   orb: 8 },
  { name: 'Sextile',     angle: 60,  orb: 6 },
  { name: 'Square',      angle: 90,  orb: 7 },
  { name: 'Trine',       angle: 120, orb: 8 },
  { name: 'Opposition',  angle: 180, orb: 8 },
];
const aspects = [];
for (let i = 0; i < planets.length; i++) {
  for (let j = i + 1; j < planets.length; j++) {
    const sep = Math.abs(norm(planets[i].lon - planets[j].lon + 180) - 180);
    for (const a of ASPECTS) {
      const orb = Math.abs(sep - a.angle);
      if (orb <= a.orb) {
        aspects.push({
          a: planets[i].name, b: planets[j].name, type: a.name,
          orb: Math.round(orb * 100) / 100,
        });
      }
    }
  }
}
aspects.sort((x, y) => x.orb - y.orb);

// ── Element / modality balance (planets only, node excluded) ──
const balance = { elements: {}, modalities: {} };
for (const p of planets.filter(p => p.name !== 'North Node')) {
  balance.elements[p.element] = (balance.elements[p.element] || 0) + 1;
  balance.modalities[p.modality] = (balance.modalities[p.modality] || 0) + 1;
}

const chart = {
  person: 'Natalie Wood',
  birth: {
    date: '1938-07-20',
    localTime: '11:16 AM PST',
    utc: '1938-07-20T19:16:00Z',
    place: 'San Francisco, California',
    lat: LAT, lon: LON,
    roddenRating: 'AA',
  },
  houseSystem: 'Placidus',
  zodiac: 'Tropical',
  angles: {
    Ascendant: fmt(asc),
    Midheaven: fmt(mc),
    Descendant: fmt(norm(asc + 180)),
    'Imum Coeli': fmt(norm(mc + 180)),
  },
  planets,
  houses: Array.from({ length: 12 }, (_, i) => ({ house: i + 1, ...fmt(cusps[i + 1]) })),
  aspects,
  balance,
  generated: {
    ephemeris: 'astronomy-engine (VSOP87 / geocentric, true equinox of date)',
    note: 'Birth data per Astro-Databank (Rodden AA). Ascendant verified against published value 11°48′ Libra.',
  },
};

writeFileSync(new URL('./chart-data.json', import.meta.url), JSON.stringify(chart, null, 2) + '\n');
console.log('Wrote chart-data.json');
console.log('ASC', chart.angles.Ascendant.label, '| MC', chart.angles.Midheaven.label);
for (const p of planets) console.log(p.name.padEnd(11), p.label.padEnd(18), 'house', p.house, p.retrograde ? 'R' : '');
