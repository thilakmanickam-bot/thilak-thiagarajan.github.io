package com.astrochart.ui.i18n

import android.content.Context
import com.astrochart.core.panchangam.MonthPanchangam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * The reminder choices outlive the screen that sets them, and the worker that
 * reads them runs days later in another process lifetime — so what is actually
 * worth testing is that a choice survives, and that turning one off really
 * removes it rather than leaving it to fire forever.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VrathamReminderStoreTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        // Shared prefs persist across tests in the same Robolectric app.
        MonthPanchangam.KEYS.forEach { VrathamReminderStore.setEnabled(context, it, false) }
    }

    @Test
    fun nothingIsRemindedAboutUntilAskedFor() {
        assertEquals(emptySet<String>(), VrathamReminderStore.enabled(context))
    }

    @Test
    fun aChoiceSurvivesToBeReadBackLater() {
        VrathamReminderStore.setEnabled(context, "amavasai", true)

        assertTrue(VrathamReminderStore.isEnabled(context, "amavasai"))
        assertEquals(setOf("amavasai"), VrathamReminderStore.enabled(context))
    }

    @Test
    fun turningOneOffLeavesTheOthersOn() {
        VrathamReminderStore.setEnabled(context, "amavasai", true)
        VrathamReminderStore.setEnabled(context, "pournami", true)

        VrathamReminderStore.setEnabled(context, "amavasai", false)

        assertFalse(VrathamReminderStore.isEnabled(context, "amavasai"))
        assertTrue(VrathamReminderStore.isEnabled(context, "pournami"))
        assertEquals(setOf("pournami"), VrathamReminderStore.enabled(context))
    }

    @Test
    fun enablingTwiceDoesNotDuplicate() {
        VrathamReminderStore.setEnabled(context, "ekadasi", true)
        VrathamReminderStore.setEnabled(context, "ekadasi", true)

        assertEquals(setOf("ekadasi"), VrathamReminderStore.enabled(context))
    }

    @Test
    fun everyObservanceTheCalendarShowsCanBeSwitchedOn() {
        // The calendar groups by MonthPanchangam.KEYS, so a key the store could
        // not hold would be a switch that silently forgot itself.
        MonthPanchangam.KEYS.forEach { VrathamReminderStore.setEnabled(context, it, true) }

        assertEquals(MonthPanchangam.KEYS.toSet(), VrathamReminderStore.enabled(context))
    }

    @Test
    fun theReturnedSetCanBeHeldAcrossALaterEdit() {
        // SharedPreferences.getStringSet hands back an instance whose contents
        // are undefined after a subsequent edit; the store copies for exactly
        // this reason, and Compose holds the previous set to diff against.
        VrathamReminderStore.setEnabled(context, "sashti", true)
        val before = VrathamReminderStore.enabled(context)

        VrathamReminderStore.setEnabled(context, "pradosham", true)

        assertEquals("the earlier snapshot must not mutate underneath", setOf("sashti"), before)
    }
}
