package com.astrochart.features

import com.astrochart.billing.PremiumStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * [PremiumStore] is the local cache [BillingManager] writes after every
 * server-verified purchase/refresh, and what [Premium.isActive] (ad-gating,
 * the Subscription screen's premium state) reads back — a regression here
 * silently breaks entitlement for every launch, not just at purchase time.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BillingPurchaseFlowTest {

    @Test
    fun freshInstall_hasNoEntitlement() {
        val context = RuntimeEnvironment.getApplication()

        val entitlement = PremiumStore.load(context)

        assertFalse(entitlement.active)
        assertEquals(0L, entitlement.expiresAtMillis)
    }

    @Test
    fun savedEntitlement_isReadBackExactly() {
        val context = RuntimeEnvironment.getApplication()

        PremiumStore.save(context, active = true, expiresAtMillis = 1_800_000_000_000L)
        val entitlement = PremiumStore.load(context)

        assertTrue(entitlement.active)
        assertEquals(1_800_000_000_000L, entitlement.expiresAtMillis)
    }

    @Test
    fun expiredEntitlement_canBeOverwrittenToInactive() {
        val context = RuntimeEnvironment.getApplication()
        PremiumStore.save(context, active = true, expiresAtMillis = 1_800_000_000_000L)

        // refreshEntitlement() writes this when Play no longer reports an
        // active subscription — must actually flip the cache, not just no-op.
        PremiumStore.save(context, active = false, expiresAtMillis = 0L)
        val entitlement = PremiumStore.load(context)

        assertFalse(entitlement.active)
        assertEquals(0L, entitlement.expiresAtMillis)
    }
}
