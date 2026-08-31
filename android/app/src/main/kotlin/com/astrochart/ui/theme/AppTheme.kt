package com.astrochart.ui.theme

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Selectable background "mood" for the celestial backdrop. Each keeps the same
 * light text and gold accent (so every screen stays readable) and only re-tints
 * the gradient and glow. [code] is the persisted key; [labelEn]/[labelTa]/[labelZh]
 * name it in the settings picker.
 */
enum class AppTheme(
    val code: String,
    val top: Color,
    val mid: Color,
    val bottom: Color,
    val glow: Color,
    val labelEn: String,
    val labelTa: String,
    val labelZh: String,
    val labelHi: String,
    val labelTe: String,
    val labelKn: String,
    val labelMl: String,
    val labelMr: String
) {
    MIDNIGHT("midnight", Color(0xFF241A54), Color(0xFF141138), Color(0xFF08061C), Color(0xFF3A2A78),
        "Midnight", "நள்ளிரவு", "午夜", "मध्यरात्रि", "అర్ధరాత్రి", "ಮಧ್ಯರಾತ್ರಿ", "അർദ്ധരാത്രി", "मध्यरात्र"),
    TWILIGHT("twilight", Color(0xFF3B2A5A), Color(0xFF2A1B44), Color(0xFF140C24), Color(0xFF6B4AA0),
        "Twilight", "அந்தி", "暮色", "संध्या", "సంధ్య", "ಸಂಜೆಗತ್ತಲು", "സന്ധ്യ", "संधिप्रकाश"),
    DEEP_SPACE("deep_space", Color(0xFF11131A), Color(0xFF080A10), Color(0xFF000000), Color(0xFF1E2A45),
        "Deep space", "ஆழ்வெளி", "深空", "गहन अंतरिक्ष", "గాఢ అంతరిక్షం", "ಆಳವಾದ ಅಂತರಿಕ್ಷ", "അഗാധ ബഹിരാകാശം", "खोल अंतराळ"),
    OCEAN("ocean", Color(0xFF123A4A), Color(0xFF0B2634), Color(0xFF041018), Color(0xFF1E6B7A),
        "Ocean", "கடல்", "海洋", "महासागर", "సముద్రం", "ಸಾಗರ", "സമുദ്രം", "महासागर"),
    ROSE("rose", Color(0xFF4A1F35), Color(0xFF321226), Color(0xFF180814), Color(0xFF8A3A5A),
        "Rose", "ரோஜா", "玫瑰", "गुलाब", "గులాబీ", "ಗುಲಾಬಿ", "പനിനീർ", "गुलाब");

    companion object {
        val DEFAULT = MIDNIGHT
        fun fromCode(code: String?): AppTheme = entries.firstOrNull { it.code == code } ?: DEFAULT
    }
}

/** Persists the chosen [AppTheme] in the shared app preferences. */
object ThemeStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_THEME = "app_theme"

    fun load(context: Context): AppTheme {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return AppTheme.fromCode(prefs.getString(KEY_THEME, AppTheme.DEFAULT.code))
    }

    fun save(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, theme.code)
            .apply()
    }
}

/** Current background theme; the celestial background reads this. */
val LocalAppTheme = staticCompositionLocalOf { AppTheme.DEFAULT }
