package com.astrochart.core.utils

import com.astrochart.core.models.BirthData
import com.astrochart.core.models.PlanetaryPosition
import com.astrochart.core.panchangam.Ayanamsa
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

        val planets = listOf("Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto")
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

        val lst = localSiderealTime(jd, birthData.longitude)
        val latRad = Math.toRadians(birthData.latitude)
        val lstRad = Math.toRadians(lst)

        val cosLat = cos(latRad)
        val sinLat = sin(latRad)

        val ascRa = atan2(-cos(lstRad), if (sinLat != 0.0) sin(lstRad) * cosLat / sinLat else 1.0)
        val result = (Math.toDegrees(ascRa) + 360) % 360.0
        return if (result.isNaN() || result.isInfinite()) lst else result
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

    private fun calculatePlanetLongitude(planet: String, jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0

        val lon = when (planet) {
            "Sun" -> calculateSunLongitude(t)
            "Moon" -> calculateMoonLongitude(t)
            "Mercury" -> calculateMercuryLongitude(t)
            "Venus" -> calculateVenusLongitude(t)
            "Mars" -> calculateMarsLongitude(t)
            "Jupiter" -> calculateJupiterLongitude(t)
            "Saturn" -> calculateSaturnLongitude(t)
            "Uranus" -> calculateUranusLongitude(t)
            "Neptune" -> calculateNeptuneLongitude(t)
            "Pluto" -> calculatePlutoLongitude(t)
            else -> 0.0
        }

        if (lon.isNaN() || lon.isInfinite()) return 0.0
        // Kotlin's % keeps the dividend's sign, so for dates far from J2000 the
        // raw longitude can be negative. Normalize into [0, 360) so downstream
        // aspect math (which compares raw longitudes) stays correct.
        return ((lon % 360.0) + 360.0) % 360.0
    }

    private fun calculateSunLongitude(t: Double): Double {
        val meanLon = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        val meanAnom = 357.52911 + 35999.05029 * t - 0.0001537 * t * t
        val meanAnomRad = Math.toRadians(meanAnom)
        val eq = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(meanAnomRad) +
                (0.019993 - 0.000101 * t) * sin(2 * meanAnomRad) +
                0.000029 * sin(3 * meanAnomRad)
        val result = (meanLon + eq) % 360.0
        return if (result.isNaN() || result.isInfinite()) 0.0 else result
    }

    private fun calculateMoonLongitude(t: Double): Double {
        val meanLon = 218.3164477 + 481267.88123421 * t - 0.0015786 * t * t + t * t * t / 538841.0 - t * t * t * t / 65194000.0
        val meanAnom = 134.9328645 + 477198.8693733 * t + 0.0069136 * t * t - t * t * t / 69699.0 - t * t * t * t / 14712000.0
        val meanElongation = 297.8501921 + 445267.1114034 * t - 0.0018819 * t * t + t * t * t / 545868.0 - t * t * t * t / 113065000.0

        val meanAnomRad = Math.toRadians(meanAnom)
        val meanElongRad = Math.toRadians(meanElongation)

        val correction = 6.28875 * sin(meanAnomRad) +
                1.27402 * sin(2 * meanElongRad - meanAnomRad) +
                0.65918 * sin(2 * meanElongRad)

        val result = (meanLon + correction) % 360.0
        return if (result.isNaN() || result.isInfinite()) 0.0 else result
    }

    private fun calculateMercuryLongitude(t: Double): Double {
        val meanLon = 252.25032 + 149472.67411 * t
        return (meanLon + 3.24587 * sin(Math.toRadians(163.69 + 149472.68580 * t))) % 360.0
    }

    private fun calculateVenusLongitude(t: Double): Double {
        val meanLon = 181.97973 + 58517.81560 * t
        return (meanLon + 2.96881 * sin(Math.toRadians(48.00 + 58517.80387 * t))) % 360.0
    }

    private fun calculateMarsLongitude(t: Double): Double {
        val meanLon = 319.93015 + 19139.43794 * t
        return (meanLon + 1.84023 * sin(Math.toRadians(312.32 + 19139.29189 * t))) % 360.0
    }

    private fun calculateJupiterLongitude(t: Double): Double {
        val meanLon = 225.32794 + 3034.69202 * t
        return (meanLon + 0.99091 * sin(Math.toRadians(38.87 + 3034.68521 * t))) % 360.0
    }

    private fun calculateSaturnLongitude(t: Double): Double {
        val meanLon = 175.46691 + 1223.50516 * t
        return (meanLon + 0.52560 * sin(Math.toRadians(337.37 + 1222.58631 * t))) % 360.0
    }

    private fun calculateUranusLongitude(t: Double): Double {
        val meanLon = 244.19747 + 429.86291 * t
        return (meanLon + 0.20669 * sin(Math.toRadians(25.32 + 429.95144 * t))) % 360.0
    }

    private fun calculateNeptuneLongitude(t: Double): Double {
        val meanLon = 331.46895 + 219.88184 * t
        return (meanLon + 0.12112 * sin(Math.toRadians(354.87 + 219.97356 * t))) % 360.0
    }

    private fun calculatePlutoLongitude(t: Double): Double {
        val meanLon = 14.53352 + 38.35125 * t
        return (meanLon + 0.06671 * sin(Math.toRadians(276.69 + 38.35211 * t))) % 360.0
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
