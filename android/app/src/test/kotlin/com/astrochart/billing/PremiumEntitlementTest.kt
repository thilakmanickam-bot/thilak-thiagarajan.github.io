package com.astrochart.billing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.astrochart.ads.Premium
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The entitlement cache, from the angle a tester exposes.
 *
 * A tester's Premium is time-limited and has no Play purchase behind it, which
 * is a shape the cache never had to hold before — a subscriber's row was
 * written and rewritten by Play at every launch. Both tests below fail against
 * the code as it stood before tester codes existed.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PremiumEntitlementTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PremiumStore.save(context, active = false, expiresAtMillis = 0L)
    }

    @Test
    fun aGrantInsideItsWindowIsActive() {
        val week = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000
        PremiumStore.save(context, active = true, expiresAtMillis = week)
        assertTrue(Premium.isActive(context))
    }

    @Test
    fun anExpiredGrantIsNotActive() {
        // Premium.isActive used to read only the boolean, so a tester grant on
        // a device that never came back online would have stayed premium for
        // good on a cache written once, 90 days earlier.
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        PremiumStore.save(context, active = true, expiresAtMillis = yesterday)
        assertFalse(Premium.isActive(context))
    }

    @Test
    fun anEntitlementWithNoRecordedExpiryStaysActive() {
        // 0L is what a subscriber's row looked like before expiries were
        // honoured. Reading it as "expired at the epoch" would have switched
        // off Premium for every existing payer on upgrade.
        PremiumStore.save(context, active = true, expiresAtMillis = 0L)
        assertTrue(Premium.isActive(context))
    }

    @Test
    fun anInactiveEntitlementIsNeverActive() {
        val week = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000
        PremiumStore.save(context, active = false, expiresAtMillis = week)
        assertFalse(Premium.isActive(context))
    }

    @Test
    fun theCacheRoundTripsBothFields() {
        // refreshEntitlement's server fallback writes through this, so a
        // dropped expiry here would silently become an unbounded grant.
        val expiry = 1_800_000_000_000L
        PremiumStore.save(context, active = true, expiresAtMillis = expiry)
        val loaded = PremiumStore.load(context)
        assertTrue(loaded.active)
        kotlin.test.assertEquals(expiry, loaded.expiresAtMillis)
    }
}
