package com.astrochart.core.utils

import com.astrochart.core.models.BirthData
import kotlin.math.*

object HouseCalculator {

    fun calculatePlacidusHouses(birthData: BirthData, sunLongitude: Double): List<Double> {
        val utcDateTime = birthData.toUTC()
        val jd = julianDay(utcDateTime.year, utcDateTime.monthValue, utcDateTime.dayOfMonth, utcDateTime.hour.toDouble() + utcDateTime.minute / 60.0 + utcDateTime.second / 3600.0)

        val lat = birthData.latitude
        val lon = birthData.longitude
        val lst = localSiderealTime(jd, lon)

        return calculatePlacidusHouseCusps(lat, lst)
    }

    fun getHouseForLongitude(housesCusps: List<Double>, longitude: Double): Int {
        val normalized = ((longitude % 360) + 360) % 360

        for (i in 0 until 12) {
            val cusp1 = ((housesCusps[i] % 360) + 360) % 360
            val cusp2 = ((housesCusps[(i + 1) % 12] % 360) + 360) % 360

            val isInHouse = if (cusp1 < cusp2) {
                normalized >= cusp1 && normalized < cusp2
            } else {
                normalized >= cusp1 || normalized < cusp2
            }

            if (isInHouse) {
                return i + 1
            }
        }
        return 1
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

    private fun calculatePlacidusHouseCusps(latitude: Double, lst: Double): List<Double> {
        val lat = Math.toRadians(latitude)
        val ascendant = lst

        val cusps = mutableListOf<Double>()
        cusps.add(ascendant)

        for (i in 1..11) {
            val ramc = Math.toRadians(lst)
            val f = i * 30.0
            val raHouses = calculateRAHouse(Math.toRadians(f), lat)
            val cusp = Math.toDegrees(raToLongitude(ramc + raHouses, lat)) % 360.0
            cusps.add(if (cusp < 0) cusp + 360.0 else cusp)
        }

        return cusps
    }

    private fun calculateRAHouse(f: Double, lat: Double): Double {
        val tanLatHalf = tan(lat / 2.0)
        return atan(tanLatHalf / cos(f))
    }

    private fun raToLongitude(ra: Double, lat: Double): Double {
        return atan2(sin(ra), cos(ra) * cos(lat))
    }
}
