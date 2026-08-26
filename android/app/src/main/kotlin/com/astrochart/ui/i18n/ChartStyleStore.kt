package com.astrochart.ui.i18n

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.astrochart.core.models.ChartStyle

/** Persists the chosen [ChartStyle] in the shared app preferences. */
object ChartStyleStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_STYLE = "chart_style"

    fun load(context: Context): ChartStyle {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return ChartStyle.fromCode(prefs.getString(KEY_STYLE, ChartStyle.DEFAULT.code))
    }

    fun save(context: Context, style: ChartStyle) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STYLE, style.code)
            .apply()
    }
}

/** Current chart drawing style; defaults to the wheel until [MainActivity] provides the choice. */
val LocalChartStyle = staticCompositionLocalOf { ChartStyle.DEFAULT }
