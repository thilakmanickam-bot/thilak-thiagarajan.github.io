package com.astrochart.core.utils

import com.astrochart.core.models.BirthData
import com.astrochart.core.models.PlanetaryPosition
import com.astrochart.core.panchangam.Ayanamsa
import com.astrochart.core.panchangam.SolarLunar
import kotlin.math.*

object EphemerisCalculator {

    fun calculatePlanetaryPositions(birthData: BirthData): Pair<List<PlanetaryPosition>, Pair<Double, Double>> {
        val utcDateTime = birthData.toUTC()
        val jd = julianDay(
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            utcDateTime.hour.toDouble() + utcDateTime.minute / 60.0
        )

        // The nine grahas. Uranus, Neptune and Pluto are gone: their formulas
        // were fabricated like the rest, and rather than ship three more wrong
        // positions they are dropped until real elements are added. They have
        // no place in a rasi koshtam anyway.
        val planets = listOf(
            "Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Rahu", "Ketu"
        )
        val positions = mutableListOf<PlanetaryPosition>()
        var sunLon = 0.0

        for (planet in planets) {
            val lon = calculatePlanetLongitude(planet, jd)
            if (planet == "Sun") sunLon = lon

            // The rasi is sidereal. Ayanamsa.lahiri wants a JDE and this is a
            // UT Julian Day, but the ayanamsa moves 1.396° per century, so the
            // ~70 seconds of deltaT between them shifts it by about 3e-8° —
            // some eight orders of magnitude below the arc-minute the model
            // itself is good for.
            val siderealLon = Ayanamsa.toSidereal(lon, jd)
            val sign = ZodiacUtils.getSignFromLongitude(siderealLon)
            val (degree, minute) = ZodiacUtils.formatPosition(siderealLon)
            val tropicalSign = ZodiacUtils.getSignFromLongitude(lon)
            val (tropicalDegree, tropicalMinute) = ZodiacUtils.formatPosition(lon)

            // Houses stay in the tropical frame on both sides. A house number
            // is where a planet falls between two cusps, so shifting planet and
            // cusps by the same ayanamsa leaves it unchanged — mixing the two
            // frames is the only way to get this wrong.
            val houseCusps = HouseCalculator.calculatePlacidusHouses(birthData, sunLon)
            val house = HouseCalculator.getHouseForLongitude(houseCusps, lon)

            positions.add(
                PlanetaryPosition(
                    name = planet,
                    lon = lon,
                    siderealLon = siderealLon,
                    sign = sign,
                    element = ZodiacUtils.getElement(sign),
                    modality = ZodiacUtils.getModality(sign),
                    degree = degree,
                    minute = minute,
                    label = "$degree°$minute' $sign",
                    tropicalSign = tropicalSign,
                    tropicalLabel = "$tropicalDegree°$tropicalMinute' $tropicalSign",
                    house = house
                )
            )
        }

        val ascendant = calculateAscendant(birthData)
        val midheaven = calculateMidheaven(birthData)

        return Pair(positions, Pair(ascendant, midheaven))
    }

    /**
     * The Lahiri sidereal longitude for a tropical [tropicalLon] at
     * [birthData]'s instant — so callers that hold an angle rather than a
     * planet (the ascendant and midheaven, computed separately) convert it the
     * same way the planet loop above does, instead of reimplementing the
     * julian-day arithmetic and drifting from it.
     */
    fun toSidereal(birthData: BirthData, tropicalLon: Double): Double {
        val utc = birthData.toUTC()
        val jd = julianDay(
            utc.year, utc.monthValue, utc.dayOfMonth,
            utc.hour.toDouble() + utc.minute / 60.0
        )
        return Ayanamsa.toSidereal(tropicalLon, jd)
    }

    private fun calculateAscendant(birthData: BirthData): Double {
        val utcDateTime = birthData.toUTC()
        val jd = julianDay(
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            utcDateTime.hour.toDouble() + utcDateTime.minute / 60.0
        )

        // The ascendant is where the ecliptic meets the eastern horizon, so the
        // obliquity of the ecliptic is not optional — the previous formula had
        // no obliquity term at all and was not the ascendant formula in any
        // form. On the reference jathagam it answered Thulam against a printed
        // Meenam, seven signs out.
        val ramc = Math.toRadians(localSiderealTime(jd, birthData.longitude))
        val lat = Math.toRadians(birthData.latitude)
        val obl = Math.toRadians(obliquity(jd))
        val result = (Math.toDegrees(
            atan2(cos(ramc), -(sin(ramc) * cos(obl) + tan(lat) * sin(obl)))
        ) + 360.0) % 360.0
        return if (result.isNaN() || result.isInfinite()) 0.0 else result
    }

    /** Mean obliquity of the ecliptic, degrees (Meeus 22.2). */
    private fun obliquity(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        return 23.439291 - 0.0130042 * t - 1.64e-7 * t * t + 5.04e-7 * t * t * t
    }

    private fun calculateMidheaven(birthData: BirthData): Double {
        val utcDateTime = birthData.toUTC()
        val jd = julianDay(
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            utcDateTime.hour.toDouble() + utcDateTime.minute / 60.0
        )

        val lst = localSiderealTime(jd, birthData.longitude)
        val result = lst % 360.0
        return if (result.isNaN() || result.isInfinite()) 0.0 else result
    }

    /**
     * Tropical geocentric ecliptic longitude, degrees.
     *
     * Sun and Moon come from [SolarLunar], the Meeus series the panchangam
     * already uses and which PanchangamTest pins to Meeus' own worked
     * examples — there is no reason for this file to carry a second, worse
     * copy. The planets come from [PlanetEphemeris]. The nodes are the mean
     * node and its opposite point.
     */
    private fun calculatePlanetLongitude(planet: String, jd: Double): Double {
        val year = 2000 + ((jd - 2451545.0) / 365.25).toInt()
        val jde = SolarLunar.toJde(jd, year)
        val lon = when (planet) {
            "Sun" -> SolarLunar.sunApparentLongitude(jde)
            "Moon" -> SolarLunar.moonLongitude(jde)
            "Mercury" -> PlanetEphemeris.geocentricLongitude(PlanetEphemeris.Body.MERCURY, jd)
            "Venus" -> PlanetEphemeris.geocentricLongitude(PlanetEphemeris.Body.VENUS, jd)
            "Mars" -> PlanetEphemeris.geocentricLongitude(PlanetEphemeris.Body.MARS, jd)
            "Jupiter" -> PlanetEphemeris.geocentricLongitude(PlanetEphemeris.Body.JUPITER, jd)
            "Saturn" -> PlanetEphemeris.geocentricLongitude(PlanetEphemeris.Body.SATURN, jd)
            "Rahu" -> PlanetEphemeris.meanNode(jd)
            "Ketu" -> PlanetEphemeris.meanNode(jd) + 180.0
            else -> 0.0
        }

        if (lon.isNaN() || lon.isInfinite()) return 0.0
        // Kotlin's % keeps the dividend's sign, so for dates far from J2000 the
        // raw longitude can be negative. Normalize into [0, 360) so downstream
        // aspect math (which compares raw longitudes) stays correct.
        return ((lon % 360.0) + 360.0) % 360.0
    }

    private fun julianDay(year: Int, month: Int, day: Int, hour: Double): Double {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        val jdn = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        return jdn + (hour - 12) / 24.0
    }

    private fun localSiderealTime(jd: Double, longitude: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val gst = 67310.54841 + (876600.0 * 3600.0 + 8640184.812866) * t + 0.093104 * t * t - 6.2e-6 * t * t * t
        val gst2 = ((gst / 86400.0) % 1.0 + 1.0) % 1.0
        val lst = (gst2 * 360.0 + longitude) % 360.0
        return if (lst < 0) lst + 360.0 else lst
    }
}
