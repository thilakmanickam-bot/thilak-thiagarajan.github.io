package com.astrochart.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astrochart.Features
import com.astrochart.auth.Account
import com.astrochart.auth.AccountStore
import com.astrochart.auth.AuthManager
import com.astrochart.auth.ProfileSync
import com.astrochart.data.repository.ChartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Drives the Account screen: exposes the signed-in [Account] (if any) and the
 * in-flight [Status], and runs sign-in / sign-out. On a successful sign-in it
 * kicks off a one-shot [ProfileSync] so the primary profile and saved charts are
 * reconciled with the cloud. Everything is gated on [Features.AUTH_ENABLED].
 */
class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChartRepository(application.applicationContext)

    private val _account = MutableStateFlow(
        if (Features.AUTH_ENABLED) (AuthManager.currentAccount() ?: AccountStore.load(application))
        else null
    )
    val account: StateFlow<Account?> = _account

    private val _status = MutableStateFlow<Status>(Status.Idle)
    val status: StateFlow<Status> = _status

    /** Google sign-in. [context] must be an Activity context (Credential Manager UI). */
    fun signInWithGoogle(context: Context) {
        if (!Features.AUTH_ENABLED || _status.value == Status.Working) return
        _status.value = Status.Working
        viewModelScope.launch {
            try {
                val account = AuthManager.signInWithGoogle(context)
                AccountStore.save(getApplication(), account)
                _account.value = account
                runCatching { ProfileSync.syncAll(getApplication(), repository) }
                _status.value = Status.Idle
            } catch (e: Exception) {
                _status.value = Status.Error(e.message ?: "sign-in failed")
            }
        }
    }

    fun signOut(context: Context) {
        if (!Features.AUTH_ENABLED) return
        viewModelScope.launch {
            runCatching { AuthManager.signOut(context) }
            AccountStore.clear(getApplication())
            _account.value = null
            _status.value = Status.Idle
        }
    }

    /** Manually clear a shown error (e.g. when the user dismisses/retries). */
    fun clearError() {
        if (_status.value is Status.Error) _status.value = Status.Idle
    }

    sealed class Status {
        object Idle : Status()
        object Working : Status()
        data class Error(val message: String) : Status()
    }
}
