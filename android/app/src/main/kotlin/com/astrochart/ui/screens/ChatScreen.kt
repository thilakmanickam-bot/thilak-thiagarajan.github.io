package com.astrochart.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.astrochart.ui.components.CelestialCard
import com.astrochart.ui.components.GoldButton
import com.astrochart.ui.components.OutlineGoldButton
import com.astrochart.ui.i18n.LocalLanguage
import com.astrochart.ui.i18n.LocalStrings
import com.astrochart.ui.theme.CardBorder
import com.astrochart.ui.theme.CardFill
import com.astrochart.ui.theme.GoldDeep
import com.astrochart.ui.theme.OnGold
import com.astrochart.ui.theme.TextMuted
import com.astrochart.ui.theme.TextPrimary
import com.astrochart.ui.viewmodel.ChatViewModel

/**
 * The AI astrologer chat. First the user chooses a saved chart (or is guided to
 * create one); then they converse with the persona, which reflects on the chosen
 * chart. Suggested questions are offered as tap-to-send chips at the start.
 */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToBirthInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    if (!viewModel.isConfigured()) {
        Box(modifier = modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
            CelestialCard {
                Text(
                    text = strings.chatNotConfigured,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted
                )
            }
        }
        return
    }

    val selectedName by viewModel.selectedChartName.collectAsState()
    if (selectedName == null) {
        ChartPicker(viewModel = viewModel, onNavigateToBirthInput = onNavigateToBirthInput, modifier = modifier)
    } else {
        Conversation(viewModel = viewModel, chartName = selectedName!!, modifier = modifier)
    }
}

@Composable
private fun ChartPicker(
    viewModel: ChatViewModel,
    onNavigateToBirthInput: () -> Unit,
    modifier: Modifier
) {
    val strings = LocalStrings.current
    val language = LocalLanguage.current
    val charts by viewModel.savedCharts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        if (charts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.chatEmptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    GoldButton(
                        text = strings.chatCreateChart,
                        onClick = onNavigateToBirthInput,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Text(
                text = strings.chatChoosePrompt,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(charts, key = { it.id }) { chart ->
                    CelestialCard(
                        modifier = Modifier.clickable { viewModel.selectChart(chart.id, language) }
                    ) {
                        Text(
                            text = chart.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = chart.locationName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Conversation(
    viewModel: ChatViewModel,
    chartName: String,
    modifier: Modifier
) {
    val strings = LocalStrings.current
    val greeting by viewModel.greeting.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val error by viewModel.error.collectAsState()

    val listState = rememberLazyListState()
    val total = (if (greeting != null) 1 else 0) + messages.size + (if (isSending) 1 else 0)
    LaunchedEffect(total) {
        if (total > 0) listState.animateScrollToItem(total - 1)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header: which chart, with a way back to the picker.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chartName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = strings.chatChangeChart,
                style = MaterialTheme.typography.labelLarge,
                color = GoldDeep,
                modifier = Modifier.clickable { viewModel.clearSelection() }
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            greeting?.let { item { MessageBubble(text = it, fromUser = false) } }

            // Suggested questions only at the very start of a conversation.
            if (messages.isEmpty() && !isSending) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.chatSuggestionsHeader,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextMuted
                        )
                        viewModel.suggestedQuestions().forEach { q ->
                            OutlineGoldButton(
                                text = q,
                                onClick = { viewModel.send(q) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            items(messages) { msg ->
                MessageBubble(text = msg.text, fromUser = msg.role == ChatViewModel.Role.USER)
            }

            if (isSending) {
                item { ThinkingBubble(label = strings.chatThinking) }
            }
        }

        error?.let {
            val text = if (it == "not_configured") strings.chatNotConfigured else strings.chatError
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
        }

        MessageInput(
            hint = strings.chatInputHint,
            sendDescription = strings.chatSend,
            enabled = !isSending,
            onSend = { viewModel.send(it) }
        )
    }
}

@Composable
private fun MessageBubble(text: String, fromUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (fromUser) GoldDeep else CardFill.copy(alpha = 0.7f),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (fromUser) OnGold else TextPrimary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun ThinkingBubble(label: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(shape = RoundedCornerShape(18.dp), color = CardFill.copy(alpha = 0.7f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                CircularProgressIndicator(
                    color = GoldDeep,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }
    }
}

@Composable
private fun MessageInput(
    hint: String,
    sendDescription: String,
    enabled: Boolean,
    onSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val submit = {
        if (text.isNotBlank()) {
            onSend(text)
            text = ""
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(hint, color = TextMuted) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { submit() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardFill.copy(alpha = 0.5f),
                unfocusedContainerColor = CardFill.copy(alpha = 0.5f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = CardBorder,
                unfocusedIndicatorColor = CardBorder,
                cursorColor = GoldDeep
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = submit, enabled = enabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = sendDescription,
                tint = if (enabled) GoldDeep else TextMuted
            )
        }
    }
}
