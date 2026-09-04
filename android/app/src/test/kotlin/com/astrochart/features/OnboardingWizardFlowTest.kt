package com.astrochart.features

import com.astrochart.ui.i18n.OnboardingStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * [OnboardingStore] gates whether [OnboardingWizard] shows on launch — a
 * regression here either traps every user behind the wizard forever, or
 * (the more dangerous direction) silently skips it for a fresh install.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OnboardingWizardFlowTest {

    @Test
    fun freshInstall_showsTheWizard() {
        val context = RuntimeEnvironment.getApplication()

        assertTrue(OnboardingStore.shouldShow(context))
    }

    @Test
    fun afterCompletion_wizardDoesNotShowAgainForTheSameVersion() {
        val context = RuntimeEnvironment.getApplication()

        OnboardingStore.markCompleted(context)

        assertFalse(OnboardingStore.shouldShow(context))
    }
}
