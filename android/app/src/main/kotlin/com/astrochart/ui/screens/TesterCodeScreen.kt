package com.astrochart.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.astrochart.billing.TesterCodeClient
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.GoldButton
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * "Become a tester" — redeem a code for Premium, or ask for one.
 *
 * The code is checked by the `redeemTesterCode` Cloud Function, never here: a
 * constant compiled into the app can be read out of any published APK, and once
 * it leaks there is no way to revoke it without shipping a new build. See
 * [TesterCodeClient] and `functions/src/tester.ts`.
 *
 * Sign-in is required because redemption authenticates with a Firebase ID
 * token, so a signed-out visitor is told that plainly rather than being handed
 * a field that always fails.
 */
@Composable
fun TesterCodeScreen(onSignIn: () -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember(context) { TesterCodeClient(context) }

    var code by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showEmailField by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<TesterCodeClient.Result?>(null) }

    // Read once per composition rather than observed: this screen is opened
    // fresh from Premium, so there is no window in which it goes stale.
    val signedIn = remember { FirebaseAuth.getInstance().currentUser != null }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Icon(
            imageVector = if (result is TesterCodeClient.Result.Redeemed) {
                Icons.Filled.CheckCircle
            } else {
                Icons.Filled.Science
            },
            contentDescription = null,
            tint = GoldDeep,
            modifier = Modifier.height(52.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = strings.testerTitle,
            style = MaterialTheme.typography.headlineSmall,
            color = GoldDeep,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = strings.testerSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        // Success is terminal: the entitlement is cached and every Premium gate
        // already reads it, so offering the field again would only invite a
        // second redemption of a code that has already been spent.
        if (result is TesterCodeClient.Result.Redeemed) {
            CelestialCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.testerSuccess,
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldDeep,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            return@Column
        }

        if (!signedIn) {
            CelestialCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.chatSignInPrompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                Spacer(Modifier.height(16.dp))
                GoldButton(
                    text = strings.accountContinueGoogle,
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            return@Column
        }

        CelestialCard(modifier = Modifier.fillMaxWidth()) {
            Text(strings.testerCodeLabel, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            OutlinedTextField(
                value = code,
                onValueChange = { code = it; result = null },
                singleLine = true,
                // Codes are uppercase; the server compares case-insensitively
                // anyway, so this is a convenience rather than a requirement.
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldDeep,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = GoldDeep
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            GoldButton(
                text = strings.testerRedeem,
                enabled = code.isNotBlank() && !busy,
                loading = busy,
                onClick = {
                    busy = true
                    scope.launch {
                        result = client.redeem(code)
                        busy = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { showEmailField = !showEmailField; result = null }) {
            Text(strings.testerNoCode, color = GoldDeep, style = MaterialTheme.typography.bodyMedium)
        }

        if (showEmailField) {
            CelestialCard(modifier = Modifier.fillMaxWidth()) {
                Text(strings.testerEmailLabel, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; result = null },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldDeep,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = GoldDeep
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                GoldButton(
                    text = strings.testerRequestAccess,
                    enabled = email.isNotBlank() && !busy,
                    loading = busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            result = client.requestAccess(email)
                            busy = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        val message = when (result) {
            is TesterCodeClient.Result.RequestReceived -> strings.testerRequestSent
            is TesterCodeClient.Result.InvalidCode -> strings.testerInvalidCode
            is TesterCodeClient.Result.TooManyAttempts -> strings.testerTooManyAttempts
            is TesterCodeClient.Result.NotSignedIn -> strings.chatSignInPrompt
            is TesterCodeClient.Result.Unavailable -> strings.testerUnavailable
            else -> null
        }
        if (message != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (result is TesterCodeClient.Result.RequestReceived) GoldDeep else {
                    MaterialTheme.colorScheme.error
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
