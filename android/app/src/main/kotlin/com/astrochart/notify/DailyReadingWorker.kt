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
import com.astrochart.MainActivity
import com.astrochart.R
import com.astrochart.core.interpret.DailyReading
import com.astrochart.data.db.AstroChartDatabase
import com.astrochart.data.util.ChartJson
import com.astrochart.ui.i18n.LanguageStore
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

        val sign = runCatching {
            AstroChartDatabase.getInstance(context).savedChartDao().getLatestChart()
                ?.chartJson?.let { ChartJson.fromJson(it) }
                ?.planets?.firstOrNull { it.name == "Sun" }?.sign
        }.getOrNull()

        val data = DailyReading.build(LocalDate.now(), lang, sign)

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
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(strings.notifTitle)
            .setContentText(data.summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(data.summary))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID, notification)
        return Result.success()
    }

    companion object {
        private const val NOTIF_ID = 4200
    }
}
