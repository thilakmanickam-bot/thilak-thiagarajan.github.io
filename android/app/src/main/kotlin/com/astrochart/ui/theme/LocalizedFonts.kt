package com.astrochart.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.astrochart.R
import com.astrochart.core.i18n.Language

/**
 * Bundled Noto Sans fonts (Regular + Bold), one per script, under
 * `res/font/`. Bundling (rather than relying on the OS's own fallback font
 * for each script) guarantees the same glyphs render everywhere, regardless
 * of which fonts a given OEM ships as its system fallback chain.
 */
private val NotoSansLatin = FontFamily(
    Font(R.font.noto_sans_regular, FontWeight.Normal),
    Font(R.font.noto_sans_bold, FontWeight.Bold)
)
private val NotoSansTamil = FontFamily(
    Font(R.font.noto_sans_tamil_regular, FontWeight.Normal),
    Font(R.font.noto_sans_tamil_bold, FontWeight.Bold)
)
private val NotoSansDevanagari = FontFamily(
    Font(R.font.noto_sans_devanagari_regular, FontWeight.Normal),
    Font(R.font.noto_sans_devanagari_bold, FontWeight.Bold)
)
private val NotoSansTelugu = FontFamily(
    Font(R.font.noto_sans_telugu_regular, FontWeight.Normal),
    Font(R.font.noto_sans_telugu_bold, FontWeight.Bold)
)
private val NotoSansKannada = FontFamily(
    Font(R.font.noto_sans_kannada_regular, FontWeight.Normal),
    Font(R.font.noto_sans_kannada_bold, FontWeight.Bold)
)
private val NotoSansMalayalam = FontFamily(
    Font(R.font.noto_sans_malayalam_regular, FontWeight.Normal),
    Font(R.font.noto_sans_malayalam_bold, FontWeight.Bold)
)

/**
 * The bundled font for [language]'s own script. Android performs automatic
 * *system* font fallback when the active family lacks a glyph — which is why
 * every script rendered at all before this — so only explicitly selecting the
 * matching bundled family per language actually guarantees those glyphs come
 * from the app's own font rather than an OEM's system substitute.
 *
 * Chinese is deliberately excluded (kept on [FontFamily.SansSerif], i.e. the
 * system font, unchanged) — bundling Noto Sans SC would add several megabytes
 * for a single language, out of scope for this pass.
 */
fun fontFamilyForLanguage(language: Language): FontFamily = when (language) {
    Language.TA -> NotoSansTamil
    Language.HI, Language.MR -> NotoSansDevanagari
    Language.TE -> NotoSansTelugu
    Language.KN -> NotoSansKannada
    Language.ML -> NotoSansMalayalam
    Language.ZH -> FontFamily.SansSerif
    Language.EN -> NotoSansLatin
}
