package com.astrochart.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.astrochart.ui.i18n.ChartStyleStore
import com.astrochart.MainActivity
import com.astrochart.R
import com.astrochart.core.i18n.Translations
import com.astrochart.core.interpret.DailyReading
import com.astrochart.core.interpret.RasiPalanText
import com.astrochart.core.interpret.RasiPeriod
import com.astrochart.core.utils.ZodiacUtils
import com.astrochart.data.db.AstroChartDatabase
import com.astrochart.data.util.ChartJson
import com.astrochart.ui.i18n.LanguageStore
import com.astrochart.ui.i18n.PrimaryProfileStore
import com.astrochart.ui.i18n.UiStrings
import java.time.LocalDate

/**
 * Builds the current day's reading in the user's chosen language (flavoured by the
 * most recently saved chart's Sun sign, if any) and posts it as a notification.
 * Runs daily via [NotificationScheduler].
 */
class DailyReadingWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    @SuppressLint("MissingPermission") // Guarded by the runtime permission check below.
    override suspend fun doWork(): Result {
        val context = applicationContext
        val lang = LanguageStore.load(context)
        val strings = UiStrings.forLanguage(lang)

        // If the user has chosen a primary profile, the daily notification is
        // that person's rasi-palan; otherwise fall back to the generic daily
        // reading flavoured by the most recently saved chart's Sun sign.
        val primary = PrimaryProfileStore.load(context)
        val notifTitle: String
        val notifText: String
        if (primary != null) {
            val rasiName = Translations.signName(ZodiacUtils.getAllSigns()[primary.rasi], lang)
            notifTitle = primary.name.ifBlank { rasiName } + " · " + rasiName
            notifText = RasiPalanText.horoscope(primary.rasi, RasiPeriod.DAY, LocalDate.now(), lang)
                .firstOrNull() ?: DailyReading.build(LocalDate.now(), lang, null).summary
        } else {
            // Whichever zodiac the reader has chosen — the notification should
            // not name a different sign from the chart screen.
            val sign = runCatching {
                AstroChartDatabase.getInstance(context).savedChartDao().getLatestChart()
                    ?.chartJson?.let { ChartJson.fromJson(it) }
                    ?.planets?.firstOrNull { it.name == "Sun" }
                    ?.signFor(ChartStyleStore.load(context))
            }.getOrNull()
            notifTitle = strings.notifTitle
            notifText = DailyReading.build(LocalDate.now(), lang, sign).summary
        }

        NotificationScheduler.ensureChannel(context)

        // On Android 13+ we must hold POST_NOTIFICATIONS; if the user declined, skip quietly.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notifTitle)
            .setContentText(notifText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notifText))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID, notification)
        return Result.success()
    }

    companion object {
        private const val NOTIF_ID = 4200
    }
}
