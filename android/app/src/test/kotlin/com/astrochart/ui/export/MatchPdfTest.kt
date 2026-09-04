package com.astrochart.ui.export

import com.astrochart.core.i18n.Language
import com.astrochart.core.models.ChartStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The parts of the export that are decisions rather than drawing.
 *
 * The page itself is checked on a device, per docs/RELEASE_RUNBOOK.md: what a
 * PDF looks like is not something an assertion can tell you, and the drawing
 * is delegated to the same `drawWheel` the screen already uses.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MatchPdfTest {

    private fun sheet(groom: String, bride: String) = MatchSheet(
        title = "Marriage Match Making",
        groom = MatchSheet.Person("Groom", groom, "Aries", "Ashwini", null),
        bride = MatchSheet.Person("Bride", bride, "Taurus", "Bharani", null),
        kootaHeading = "Kuta",
        gainedHeading = "Gained",
        maxHeading = "Max",
        rows = emptyList(),
        totalLabel = "Total Compatibility Score",
        total = 26,
        max = 40,
        verdict = "A reasonable match.",
        language = Language.EN,
        style = ChartStyle.WESTERN_WHEEL
    )

    @Test
    fun theFileIsNamedAfterBothPeople() {
        assertEquals("Ravi-Meera-match.pdf", MatchPdf.fileName(sheet("Ravi", "Meera")))
    }

    @Test
    fun aNameThatWouldBreakAPathIsReducedToSomethingSafe() {
        // Names are user input and go straight into a file name, so anything
        // that could climb out of the directory has to stop being a name.
        val name = MatchPdf.fileName(sheet("../../etc", "a/b\\c"))

        assertTrue("no traversal in $name", !name.contains(".."))
        assertTrue("no separators in $name", !name.contains("/") && !name.contains("\\"))
        assertTrue(name.endsWith("-match.pdf"))
    }

    @Test
    fun aBlankNameStillProducesAUsableFileName() {
        assertEquals("match-match-match.pdf", MatchPdf.fileName(sheet("", "   ")))
    }

    @Test
    fun nonLatinNamesSurviveTheSlug() {
        // The slug strips punctuation, not scripts — a Tamil name must not
        // collapse to "match".
        val name = MatchPdf.fileName(sheet("ரவி", "மீரா"))

        assertEquals("ரவி-மீரா-match.pdf", name)
    }

    /** Six points per character — a stand-in for a font, so the wrapping
     *  decisions are what is under test rather than a typeface's metrics. */
    private val measure: (String) -> Float = { it.length * 6f }

    @Test
    fun wrappingBreaksOnWordsAndKeepsEveryWord() {
        val text = "Key astrological factors are well matched and a harmonious union is indicated"

        val lines = MatchPdf.wrap(text, maxWidth = 120f, measure = measure)

        assertTrue("expected more than one line, got $lines", lines.size > 1)
        assertTrue("no line exceeds the column", lines.all { measure(it) <= 120f })
        assertEquals("no word lost or reordered", text, lines.joinToString(" "))
    }

    @Test
    fun aWordWiderThanTheColumnIsKeptRatherThanDropped() {
        // Losing text silently is worse than one line running long.
        assertEquals(
            listOf("Sagittarius"),
            MatchPdf.wrap("Sagittarius", maxWidth = 4f, measure = measure)
        )
    }

    @Test
    fun emptyTextProducesNoLines() {
        assertEquals(emptyList<String>(), MatchPdf.wrap("   ", maxWidth = 100f, measure = measure))
    }
}
