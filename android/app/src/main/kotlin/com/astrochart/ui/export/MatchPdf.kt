package com.astrochart.ui.export

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.astrochart.R
import com.astrochart.core.i18n.Language
import com.astrochart.core.i18n.Translations
import com.astrochart.core.models.ChartStyle
import com.astrochart.core.models.NatalChart
import com.astrochart.core.utils.SouthIndianChart
import com.astrochart.ui.components.drawWheel
import com.astrochart.ui.components.spreadPlanets
import java.io.File

/**
 * Everything that appears on an exported match sheet, already localized.
 *
 * A plain data class rather than the porutham result itself: the PDF must not
 * decide what a koota is called or how a verdict reads, since the screen has
 * already resolved all of that for the user's language.
 */
data class MatchSheet(
    val title: String,
    val groom: Person,
    val bride: Person,
    val kootaHeading: String,
    val gainedHeading: String,
    val maxHeading: String,
    val rows: List<Row>,
    val totalLabel: String,
    val total: Int,
    val max: Int,
    val verdict: String,
    val language: Language,
    val style: ChartStyle
) {
    data class Person(
        val role: String,
        val name: String,
        val rasi: String,
        val nakshatra: String,
        /** Null when this person was matched without birth details. */
        val chart: NatalChart?
    )

    data class Row(val name: String, val gained: Int, val max: Int)
}

/**
 * Renders a [MatchSheet] to a one-page A4 PDF in the app's shared cache.
 *
 * Two deliberate choices worth stating:
 *
 * 1. **Print colours, not the app's palette.** The screen is light-on-dark
 *    celestial gold; the same values on white paper are close to invisible.
 *    [drawWheel] already takes its colours as parameters, so the wheel here is
 *    the *same* renderer with an ink-on-white set rather than a second one.
 * 2. **The South-Indian grid is drawn twice over.** That chart is built from
 *    layout composables, which cannot be drawn into a Canvas the way the wheel
 *    can, so this file carries its own small grid renderer. Both read the same
 *    [SouthIndianChart.cells], so what goes in each cell can never diverge —
 *    only the drawing does.
 *
 * Text is drawn with [Paint], which uses the platform typeface rather than the
 * app's bundled Noto fonts; Indic and CJK text therefore falls back to the
 * device's own fonts here, unlike on screen.
 */
object MatchPdf {

    // A4 at 72dpi, the unit PdfDocument works in.
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LOGO_SIDE = 46f

    private val Ink = android.graphics.Color.parseColor("#1A1A2E")
    private val Muted = android.graphics.Color.parseColor("#6B6B80")
    private val Rule = android.graphics.Color.parseColor("#CFCFDA")
    private val Gold = android.graphics.Color.parseColor("#8A6A1F")
    private val Absent = android.graphics.Color.parseColor("#B03A4A")

    // Compose colours for the reused wheel renderer, same intent as above.
    private val WheelGold = Color(0xFF8A6A1F)
    private val WheelGoldBright = Color(0xFFB08A2E)
    private val WheelFaint = Color(0xFFCFCFDA)
    private val WheelMuted = Color(0xFF6B6B80)
    private val WheelInk = Color(0xFF1A1A2E)
    private val WheelSoft = Color(0xFF5A8296)
    private val WheelHard = Color(0xFFB03A4A)

    /**
     * Writes the sheet and returns the file. Blocking: call it off the main
     * thread. The directory matches `res/xml/file_paths.xml`, so the result is
     * shareable through the app's FileProvider and nothing else is.
     */
    fun write(context: Context, sheet: MatchSheet): File {
        val directory = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(directory, fileName(sheet))

        val document = PdfDocument()
        try {
            val page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            )
            drawPage(context, page.canvas, sheet)
            document.finishPage(page)
            file.outputStream().use { document.writeTo(it) }
        } finally {
            document.close()
        }
        return file
    }

    /**
     * `groom-bride-match.pdf`, with anything a file system might object to
     * replaced. Names come from user input, so they are not trusted as a path.
     */
    internal fun fileName(sheet: MatchSheet): String {
        fun slug(value: String) = value.trim()
            // \p{M} keeps combining marks: without it a Tamil or Devanagari
            // name loses its vowel signs and can slug away to nothing.
            .replace(Regex("[^\\p{L}\\p{N}\\p{M}]+"), "-")
            .trim('-')
            .take(24)
            .ifBlank { "match" }
        return "${slug(sheet.groom.name)}-${slug(sheet.bride.name)}-match.pdf"
    }

    private fun drawPage(context: Context, canvas: android.graphics.Canvas, sheet: MatchSheet) {
        canvas.drawColor(android.graphics.Color.WHITE)

        val title = paint(18f, Ink, bold = true)
        val heading = paint(11f, Gold, bold = true)
        val body = paint(10f, Ink)
        val small = paint(9f, Muted)

        // Logo top right. The launcher icon is an adaptive icon on API 26+, so
        // it is rasterised from the Drawable rather than decoded as a bitmap.
        ContextCompat.getDrawable(context, R.mipmap.ic_launcher)?.let { logo ->
            val bitmap = logo.toBitmap(LOGO_SIDE.toInt(), LOGO_SIDE.toInt())
            canvas.drawBitmap(bitmap, PAGE_WIDTH - MARGIN - LOGO_SIDE, MARGIN - 8f, null)
        }

        var y = MARGIN + 14f
        canvas.drawText(sheet.title, MARGIN, y, title)
        y += 28f

        // Two people, side by side.
        val columnWidth = (PAGE_WIDTH - 2 * MARGIN) / 2f
        listOf(sheet.groom to MARGIN, sheet.bride to MARGIN + columnWidth).forEach { (person, x) ->
            var py = y
            canvas.drawText(person.role, x, py, heading); py += 15f
            canvas.drawText(person.name, x, py, paint(13f, Ink, bold = true)); py += 15f
            canvas.drawText(person.rasi, x, py, body); py += 13f
            canvas.drawText(person.nakshatra, x, py, small)
        }
        y += 66f

        // Charts, only for people whose birth details were given. A person
        // without them leaves their half of the row empty rather than being
        // given an invented chart.
        val chartSide = columnWidth - 18f
        val hasAnyChart = sheet.groom.chart != null || sheet.bride.chart != null
        if (hasAnyChart) {
            sheet.groom.chart?.let { drawChart(context, canvas, it, sheet, MARGIN, y, chartSide) }
            sheet.bride.chart?.let {
                drawChart(context, canvas, it, sheet, MARGIN + columnWidth, y, chartSide)
            }
            y += chartSide + 24f
        }

        // Koota table.
        canvas.drawText(sheet.kootaHeading, MARGIN, y, heading)
        canvas.drawText(sheet.gainedHeading, MARGIN + 300f, y, heading)
        canvas.drawText(sheet.maxHeading, MARGIN + 380f, y, heading)
        y += 6f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, rulePaint())
        y += 15f

        sheet.rows.forEach { row ->
            val gainedColour = if (row.gained > 0) Ink else Absent
            canvas.drawText(row.name, MARGIN, y, body)
            canvas.drawText("${row.gained}", MARGIN + 300f, y, paint(10f, gainedColour, bold = true))
            canvas.drawText("${row.max}", MARGIN + 380f, y, small)
            y += 16f
        }

        y += 6f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, rulePaint())
        y += 26f
        canvas.drawText("${sheet.total}/${sheet.max}", MARGIN, y, paint(22f, Ink, bold = true))
        canvas.drawText(sheet.totalLabel, MARGIN + 80f, y, heading)
        y += 24f

        // The verdict runs to a paragraph, so it wraps rather than running off
        // the right edge.
        wrap(sheet.verdict, PAGE_WIDTH - 2 * MARGIN, body::measureText).forEach { line ->
            canvas.drawText(line, MARGIN, y, body)
            y += 14f
        }
    }

    private fun drawChart(
        context: Context,
        canvas: android.graphics.Canvas,
        chart: NatalChart,
        sheet: MatchSheet,
        x: Float,
        y: Float,
        side: Float
    ) {
        when (sheet.style) {
            ChartStyle.WESTERN_WHEEL -> drawWesternWheel(context, canvas, chart, x, y, side)
            ChartStyle.SOUTH_INDIAN -> drawSouthIndianGrid(canvas, chart, sheet.language, x, y, side)
        }
    }

    private fun drawWesternWheel(
        context: Context,
        canvas: android.graphics.Canvas,
        chart: NatalChart,
        x: Float,
        y: Float,
        side: Float
    ) {
        val density = Density(1f)
        // Built here rather than remembered: this runs off the composition, so
        // there is no rememberTextMeasurer to borrow.
        val measurer = TextMeasurer(
            defaultFontFamilyResolver = createFontFamilyResolver(context),
            defaultDensity = density,
            defaultLayoutDirection = LayoutDirection.Ltr
        )
        canvas.save()
        canvas.translate(x, y)
        CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(canvas), Size(side, side)) {
            drawWheel(
                chart = chart,
                planets = spreadPlanets(chart.planets),
                measurer = measurer,
                gold = WheelGold,
                goldBright = WheelGoldBright,
                faint = WheelFaint,
                muted = WheelMuted,
                ink = WheelInk,
                soft = WheelSoft,
                hard = WheelHard
            )
        }
        canvas.restore()
    }

    private fun drawSouthIndianGrid(
        canvas: android.graphics.Canvas,
        chart: NatalChart,
        language: Language,
        x: Float,
        y: Float,
        side: Float
    ) {
        val cell = side / 4f
        val signPaint = paint(6.5f, Muted)
        val bodyPaint = paint(7.5f, Gold, bold = true)
        val border = rulePaint()

        SouthIndianChart.cells(chart, includeAscendant = true).forEach { c ->
            val left = x + cell * c.col
            val top = y + cell * c.row
            canvas.drawRect(left, top, left + cell, top + cell, border)
            canvas.drawText(
                Translations.signName(c.sign, language), left + 3f, top + 10f, signPaint
            )
            // Bodies wrap inside the cell; a sign holding four planets would
            // otherwise write straight through its neighbour.
            var lineY = top + 21f
            wrap(
                c.bodies.joinToString("  ") { Translations.bodyAbbr(it, language) },
                cell - 6f,
                bodyPaint::measureText
            ).forEach { line ->
                if (lineY < top + cell - 2f) {
                    canvas.drawText(line, left + 3f, lineY, bodyPaint)
                    lineY += 9f
                }
            }
        }
    }

    /**
     * Greedy word wrap to [maxWidth]. Takes a measure function rather than a
     * [Paint] so the wrapping decisions can be tested without depending on a
     * particular font's metrics — call sites pass `paint::measureText`.
     *
     * A single word wider than the column is kept on its own over-long line:
     * a line that runs wide is visible, and text silently dropped is not.
     */
    internal fun wrap(text: String, maxWidth: Float, measure: (String) -> Float): List<String> {
        if (text.isBlank()) return emptyList()
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        text.split(" ").filter { it.isNotEmpty() }.forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (current.isEmpty() || measure(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                lines += current.toString()
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines += current.toString()
        return lines
    }

    private fun paint(size: Float, colour: Int, bold: Boolean = false) = Paint().apply {
        isAntiAlias = true
        textSize = size
        color = colour
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    private fun rulePaint() = Paint().apply {
        isAntiAlias = true
        color = Rule
        style = Paint.Style.STROKE
        strokeWidth = 0.8f
    }
}
