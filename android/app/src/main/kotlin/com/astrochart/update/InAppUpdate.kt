package com.astrochart.update

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Wraps Google Play's flexible in-app update flow. On launch it asks Play
 * whether a newer version of the app is live; if so it shows Play's built-in
 * update prompt, and once the update has downloaded in the background it is
 * installed. This needs no backend — Play itself is the source of truth — and
 * is a silent no-op for installs that did not come from Play (e.g. sideloaded
 * debug builds), so it is always safe to call.
 */
class InAppUpdate(activity: ComponentActivity) {

    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            runCatching { manager.completeUpdate() }
        }
    }

    /** Check for an available update and, if one exists, start the flexible flow. */
    fun checkForUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        manager.registerListener(listener)
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                if (available && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    runCatching {
                        manager.startUpdateFlowForResult(
                            info,
                            launcher,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                        )
                    }
                }
            }
            .addOnFailureListener { Log.d(TAG, "update check failed: ${it.message}") }
    }

    /** Finish installing an update that finished downloading while away. */
    fun onResume() {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                runCatching { manager.completeUpdate() }
            }
        }
    }

    fun unregister() {
        runCatching { manager.unregisterListener(listener) }
    }

    private companion object {
        const val TAG = "InAppUpdate"
    }
}
