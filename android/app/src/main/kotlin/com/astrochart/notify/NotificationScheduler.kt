package com.astrochart.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.astrochart.ui.i18n.LanguageStore
import com.astrochart.ui.i18n.UiStrings
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Schedules the daily-reading notification for ~6:00 AM local time using
 * WorkManager, which persists across reboots and needs no exact-alarm permission.
 */
object NotificationScheduler {
    // New id: channels are immutable once created, so a fresh id guarantees the
    // silent (low-importance) settings apply even where the old channel exists.
    const val CHANNEL_ID = "daily_reading_v2"
    private const val WORK_NAME = "daily_reading"
    private const val VRATHAM_WORK_NAME = "vratham_reminders"
    private const val NOTIFY_HOUR = 6

    /**
     * Creates a **silent** notification channel (required on API 26+; minSdk is 26):
     * low importance with no sound or vibration, so the daily reading only appears
     * quietly in the shade — no alarm.
     */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = UiStrings.forLanguage(LanguageStore.load(context)).notifChannelName
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null)
                enableVibration(false)
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    /** Enqueues a daily periodic worker whose first run lands on the next 6:00 AM. */
    fun scheduleDaily(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyReadingWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(minutesUntilNextNotifyHour(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Enqueues the vratham reminder check, also daily at ~6:00 AM.
     *
     * Always enqueued rather than added and cancelled as switches are flipped:
     * [VrathamReminderWorker] reads the stored choices itself and returns
     * immediately when there are none, so there is no window in which a toggle
     * and the queue disagree, and nothing to repair if a cancel is missed.
     */
    fun scheduleVrathamReminders(context: Context) {
        val request = PeriodicWorkRequestBuilder<VrathamReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(minutesUntilNextNotifyHour(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            VRATHAM_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun minutesUntilNextNotifyHour(): Long {
        val now = LocalDateTime.now()
        var next = LocalDateTime.of(now.toLocalDate(), LocalTime.of(NOTIFY_HOUR, 0))
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMinutes()
    }
}
