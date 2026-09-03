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
import com.astrochart.ads.Premium
import com.astrochart.core.panchangam.MonthPanchangam
import com.astrochart.ui.i18n.LanguageStore
import com.astrochart.ui.i18n.PanchangamLocationStore
import com.astrochart.ui.i18n.PanchangamStrings
import com.astrochart.ui.i18n.VrathamReminderStore
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * Posts a reminder on the morning of an observance the user has switched on.
 *
 * Runs daily and asks "is today one of the enabled days?" rather than
 * scheduling work per occurrence. That is what makes the reminder recur every
 * month with nothing to maintain: the dates are recomputed from the panchangam
 * for whatever month it currently is, so they follow the lunar calendar rather
 * than a fixed day number, and they follow the user's chosen location too —
 * an observance placed by sunrise or sunset moves with it. A toggle therefore
 * never needs re-scheduling, and there is no stale queue to migrate.
 */
class VrathamReminderWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    @SuppressLint("MissingPermission") // Guarded by the runtime permission check below.
    override suspend fun doWork(): Result {
        val context = applicationContext

        val enabled = VrathamReminderStore.enabled(context)
        if (enabled.isEmpty()) return Result.success()

        // Checked at delivery, not only at the switch: an entitlement can lapse
        // between enabling a reminder and the morning it would fire, and the
        // choice is deliberately left stored so it comes back on renewal.
        if (!Premium.isActive(context)) return Result.success()

        val location = PanchangamLocationStore.load(context)
        val zone = runCatching { ZoneId.of(location.zoneId) }.getOrNull() ?: ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val due = MonthPanchangam.vrathaDays(
            YearMonth.from(today), location.latitude, location.longitude, zone
        ).filter { it.key in enabled && today in it.dates }
        if (due.isEmpty()) return Result.success()

        val ps = PanchangamStrings.forLanguage(LanguageStore.load(context))
        val text = due.joinToString(" · ") { ps.vratha(it.key) }

        ensureChannel(context, ps)

        // Android 13+ requires POST_NOTIFICATIONS; if the user declined, skip
        // quietly rather than crashing on a notify the system would drop anyway.
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
            context, REQUEST_CODE, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(ps.reminderTitle)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID, notification)
        return Result.success()
    }

    companion object {
        /**
         * A channel of its own, separate from the daily reading's.
         *
         * Channel importance is immutable once created, and the daily-reading
         * channel is deliberately silent. A reminder the user went and switched
         * on should be able to make a sound, so it cannot share that channel.
         */
        const val CHANNEL_ID = "vratham_reminders_v1"
        private const val NOTIF_ID = 4300
        private const val REQUEST_CODE = 1

        fun ensureChannel(context: Context, ps: PanchangamStrings) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    CHANNEL_ID,
                    ps.reminderChannelName,
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                )
                context.getSystemService(android.app.NotificationManager::class.java)
                    ?.createNotificationChannel(channel)
            }
        }
    }
}
