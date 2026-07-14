package com.astrochart.core.utils

import org.junit.Test
import kotlin.test.assertEquals

class ZodiacUtilsTest {

    @Test
    fun testGetSignFromLongitude_AriesStart() {
        val sign = ZodiacUtils.getSignFromLongitude(0.0)
        assertEquals("Aries", sign)
    }

    @Test
    fun testGetSignFromLongitude_TaurusStart() {
        val sign = ZodiacUtils.getSignFromLongitude(30.0)
        assertEquals("Taurus", sign)
    }

    @Test
    fun testGetSignFromLongitude_PiscesMid() {
        val sign = ZodiacUtils.getSignFromLongitude(350.0)
        assertEquals("Pisces", sign)
    }

    @Test
    fun testGetElement_Fire() {
        assertEquals("Fire", ZodiacUtils.getElement("Aries"))
        assertEquals("Fire", ZodiacUtils.getElement("Leo"))
        assertEquals("Fire", ZodiacUtils.getElement("Sagittarius"))
    }

    @Test
    fun testGetElement_Earth() {
        assertEquals("Earth", ZodiacUtils.getElement("Taurus"))
        assertEquals("Earth", ZodiacUtils.getElement("Virgo"))
        assertEquals("Earth", ZodiacUtils.getElement("Capricorn"))
    }

    @Test
    fun testGetElement_Air() {
        assertEquals("Air", ZodiacUtils.getElement("Gemini"))
        assertEquals("Air", ZodiacUtils.getElement("Libra"))
        assertEquals("Air", ZodiacUtils.getElement("Aquarius"))
    }

    @Test
    fun testGetElement_Water() {
        assertEquals("Water", ZodiacUtils.getElement("Cancer"))
        assertEquals("Water", ZodiacUtils.getElement("Scorpio"))
        assertEquals("Water", ZodiacUtils.getElement("Pisces"))
    }

    @Test
    fun testGetModality_Cardinal() {
        assertEquals("Cardinal", ZodiacUtils.getModality("Aries"))
        assertEquals("Cardinal", ZodiacUtils.getModality("Cancer"))
        assertEquals("Cardinal", ZodiacUtils.getModality("Libra"))
        assertEquals("Cardinal", ZodiacUtils.getModality("Capricorn"))
    }

    @Test
    fun testGetModality_Fixed() {
        assertEquals("Fixed", ZodiacUtils.getModality("Taurus"))
        assertEquals("Fixed", ZodiacUtils.getModality("Leo"))
        assertEquals("Fixed", ZodiacUtils.getModality("Scorpio"))
        assertEquals("Fixed", ZodiacUtils.getModality("Aquarius"))
    }

    @Test
    fun testGetModality_Mutable() {
        assertEquals("Mutable", ZodiacUtils.getModality("Gemini"))
        assertEquals("Mutable", ZodiacUtils.getModality("Virgo"))
        assertEquals("Mutable", ZodiacUtils.getModality("Sagittarius"))
        assertEquals("Mutable", ZodiacUtils.getModality("Pisces"))
    }

    @Test
    fun testFormatPosition() {
        val (degrees, minutes) = ZodiacUtils.formatPosition(27.5)
        assertEquals(27, degrees)
        assertEquals(30, minutes)
    }

    @Test
    fun testFormatPosition_Boundary() {
        val (degrees, minutes) = ZodiacUtils.formatPosition(0.0)
        assertEquals(0, degrees)
        assertEquals(0, minutes)
    }

    @Test
    fun testGetAllSigns() {
        val signs = ZodiacUtils.getAllSigns()
        assertEquals(12, signs.size)
        assertEquals("Aries", signs[0])
        assertEquals("Pisces", signs[11])
    }
}
