package com.astrochart.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.astrochart.Features
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.OnGold
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import com.astrochart.ui.viewmodel.AccountViewModel

/**
 * Account screen: sign in with Google (Facebook is shown disabled until enabled)
 * to back up the primary profile and saved charts to the cloud. Reachable from
 * Settings only when [Features.AUTH_ENABLED] is true.
 */
@Composable
fun AccountScreen(modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val viewModel: AccountViewModel = viewModel()
    val account by viewModel.account.collectAsState()
    val status by viewModel.status.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = null,
            tint = GoldDeep,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))

        val signedIn = account
        if (signedIn == null) {
            SignedOut(
                blurb = strings.accountSignInBlurb,
                googleLabel = strings.accountContinueGoogle,
                facebookLabel = strings.accountContinueFacebook,
                comingSoon = strings.accountComingSoon,
                errorText = strings.accountSignInError,
                working = status is AccountViewModel.Status.Working,
                workingNote = (status as? AccountViewModel.Status.Working)?.note,
                showError = status is AccountViewModel.Status.Error,
                errorDetail = (status as? AccountViewModel.Status.Error)?.message,
                onGoogle = { viewModel.signInWithGoogle(context) }
            )
        } else {
            Text(
                text = strings.accountSignedInAs,
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = signedIn.displayName.ifBlank { signedIn.email },
                style = MaterialTheme.typography.titleLarge,
                color = GoldDeep,
                textAlign = TextAlign.Center
            )
            if (signedIn.email.isNotBlank() && signedIn.displayName.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = signedIn.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Spacer(Modifier.height(20.dp))
            CelestialCard {
                Text(
                    text = strings.accountSyncNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { viewModel.signOut(context) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(strings.accountSignOut, color = GoldDeep)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SignedOut(
    blurb: String,
    googleLabel: String,
    facebookLabel: String,
    comingSoon: String,
    errorText: String,
    working: Boolean,
    workingNote: String? = null,
    showError: Boolean,
    errorDetail: String? = null,
    onGoogle: () -> Unit
) {
    Text(
        text = blurb,
        style = MaterialTheme.typography.bodyLarge,
        color = TextMuted,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    Button(
        onClick = onGoogle,
        enabled = !working,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep, contentColor = OnGold)
    ) {
        Text(googleLabel)
    }
    Spacer(Modifier.height(12.dp))
    OutlinedButton(
        onClick = { /* Facebook enabled via Features.FACEBOOK_LOGIN_ENABLED */ },
        enabled = Features.FACEBOOK_LOGIN_ENABLED && !working,
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(facebookLabel, color = if (Features.FACEBOOK_LOGIN_ENABLED) GoldDeep else TextMuted)
    }
    if (!Features.FACEBOOK_LOGIN_ENABLED) {
        Spacer(Modifier.height(6.dp))
        Text(comingSoon, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
    if (working) {
        Spacer(Modifier.height(20.dp))
        CircularProgressIndicator(color = GoldDeep)
        // A sign-in that is still running after 15s is itself a finding, and the
        // request is deliberately not cancelled, so say so rather than leaving a
        // silent spinner that looks identical to a request that has died.
        if (!workingNote.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = workingNote,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
    if (showError) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = errorText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        // The underlying exception message, shown verbatim. Sign-in failures
        // here are almost always environmental (an unregistered signing
        // certificate, a disabled provider, no Play Services) rather than
        // something the user did, and the generic line above gives a tester
        // nothing to report back. See also the Log.e in AuthManager for the
        // full stack trace via logcat.
        if (!errorDetail.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = errorDetail,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
