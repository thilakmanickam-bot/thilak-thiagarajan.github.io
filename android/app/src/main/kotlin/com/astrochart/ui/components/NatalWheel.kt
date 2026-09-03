package com.astrochart.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.astrochart.core.interpret.PlanetInfo
import com.astrochart.core.interpret.SignInfo
import com.astrochart.core.models.NatalChart
import com.astrochart.core.models.PlanetaryPosition
import com.astrochart.core.utils.ZodiacUtils
import com.astrochart.ui.theme.AstroError
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.GoldLight
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import kotlin.math.cos
import kotlin.math.sin

/** `internal` so the PDF export can lay planets out the same way. */
internal data class WheelPlanet(
    val name: String,
    val lon: Double,
    var disp: Double,
    val label: String
)

private fun norm(d: Double): Double = ((d % 360.0) + 360.0) % 360.0

/** Spread planet glyphs that sit within MIN_SEP degrees so they don't overlap. */
internal fun spreadPlanets(planets: List<PlanetaryPosition>): List<WheelPlanet> {
    val ps = planets.map { WheelPlanet(it.name, it.lon, it.lon, it.label) }
        .sortedBy { it.lon }
        .toMutableList()
    if (ps.size < 2) return ps
    val minSep = 9.0
    var pass = 0
    while (pass < 40) {
        var moved = false
        for (i in ps.indices) {
            val a = ps[i]
            val b = ps[(i + 1) % ps.size]
            val gap = norm(b.disp - a.disp)
            if (gap < minSep) {
                val push = (minSep - gap) / 2.0 + 0.05
                a.disp = norm(a.disp - push)
                b.disp = norm(b.disp + push)
                moved = true
            }
        }
        if (!moved) break
        pass++
    }
    return ps
}

/** A natal chart wheel drawn from the computed chart, in the celestial palette. */
@Composable
fun NatalWheel(chart: NatalChart, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val planets = remember(chart) { spreadPlanets(chart.planets) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        drawWheel(
            chart = chart,
            planets = planets,
            measurer = measurer,
            gold = GoldDeep,
            goldBright = GoldLight,
            faint = CardBorder,
            muted = TextMuted,
            ink = TextPrimary,
            soft = SoftAspect,
            hard = AstroError
        )
    }
}

internal val SoftAspect = Color(0xFF8FB8C8)

/**
 * The wheel itself, colours passed in rather than read from the theme.
 *
 * `internal` so the PDF export draws the *same* wheel onto a page canvas
 * through CanvasDrawScope instead of carrying a second implementation that
 * could drift from what the screen shows. The colour parameters are what makes
 * that possible: a PDF is printed on white, where this screen's light-on-dark
 * palette would be all but invisible.
 */
internal fun DrawScope.drawWheel(
    chart: NatalChart,
    planets: List<WheelPlanet>,
    measurer: TextMeasurer,
    gold: Color,
    goldBright: Color,
    faint: Color,
    muted: Color,
    ink: Color,
    soft: Color,
    hard: Color
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val half = size.minDimension / 2f
    val ascLon = chart.ascendant.lon

    val rOut = half * 0.86f
    val rSignIn = half * 0.76f
    val rPlanet = half * 0.60f
    val rHub = half * 0.44f

    fun pt(lon: Double, r: Float): Offset {
        val a = Math.toRadians(180.0 + (lon - ascLon))
        return Offset(cx + r * cos(a).toFloat(), cy - r * sin(a).toFloat())
    }

    fun ring(r: Float, color: Color, w: Float = 1f) =
        drawCircle(color = color, radius = r, center = Offset(cx, cy), style = Stroke(width = w))

    // Rings.
    ring(rOut, gold.copy(alpha = 0.7f))
    ring(rSignIn, faint)
    ring(rHub, faint)

    // Sign boundaries + glyphs.
    val signs = ZodiacUtils.getAllSigns()
    for (i in 0 until 12) {
        val p1 = pt(i * 30.0, rSignIn)
        val p2 = pt(i * 30.0, rOut)
        drawLine(faint, p1, p2, strokeWidth = 1f)
        val glyphCenter = pt(i * 30.0 + 15.0, (rOut + rSignIn) / 2f)
        drawGlyph(measurer, SignInfo.of(signs[i]).glyph, glyphCenter, 15.sp, gold)
    }

    // Degree ticks every 5° (skip sign boundaries).
    var d = 0
    while (d < 360) {
        if (d % 30 != 0) {
            val len = if (d % 10 == 0) half * 0.02f else half * 0.012f
            drawLine(faint.copy(alpha = 0.6f), pt(d.toDouble(), rSignIn), pt(d.toDouble(), rSignIn - len), strokeWidth = 0.8f)
        }
        d += 5
    }

    // ASC–DSC and MC–IC axes.
    val mcLon = chart.midheaven.lon
    for (axisLon in listOf(ascLon, norm(ascLon + 180.0), mcLon, norm(mcLon + 180.0))) {
        drawLine(gold.copy(alpha = 0.8f), pt(axisLon, rHub), pt(axisLon, rSignIn), strokeWidth = 1.4f)
    }
    drawGlyph(measurer, "ASC", pt(ascLon, rOut + half * 0.05f), 9.sp, ink)
    drawGlyph(measurer, "MC", pt(mcLon, rOut + half * 0.05f), 9.sp, ink)

    // Aspect lines (skip conjunction; soft dashed, hard solid).
    val lonByName = chart.planets.associate { it.name to it.lon }
    val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
    for (aspect in chart.aspects) {
        if (aspect.type == "Conjunction") continue
        val la = lonByName[aspect.bodyA] ?: continue
        val lb = lonByName[aspect.bodyB] ?: continue
        val isSoft = aspect.type == "Trine" || aspect.type == "Sextile"
        drawLine(
            color = (if (isSoft) soft else hard).copy(alpha = 0.55f),
            start = pt(la, rHub),
            end = pt(lb, rHub),
            strokeWidth = 1f,
            pathEffect = if (isSoft) dash else null
        )
    }

    // Planets: hub tick at true longitude, glyph at display longitude, degree label.
    planets.forEachIndexed { i, p ->
        drawLine(gold, pt(p.lon, rHub), pt(p.lon, rHub + half * 0.03f), strokeWidth = 1f)
        drawGlyph(measurer, PlanetInfo.glyph(p.name), pt(p.disp, rPlanet), 18.sp, goldBright)
        val degText = p.label.substringBefore(' ')
        val labelR = if (i % 2 == 0) rPlanet + half * 0.10f else rPlanet - half * 0.10f
        drawGlyph(measurer, degText, pt(p.disp, labelR), 8.sp, muted)
    }
}

private fun DrawScope.drawGlyph(
    measurer: TextMeasurer,
    text: String,
    center: Offset,
    fontSize: TextUnit,
    color: Color
) {
    val layout = measurer.measure(text, style = TextStyle(color = color, fontSize = fontSize))
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f)
    )
}
