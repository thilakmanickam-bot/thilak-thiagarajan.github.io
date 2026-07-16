package com.astrochart.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astrochart.chat.AnthropicApi
import com.astrochart.chat.ApiKeyStore
import com.astrochart.chat.ApiMessage
import com.astrochart.chat.ChatClient
import com.astrochart.chat.ChatRequest
import com.astrochart.core.i18n.Language
import com.astrochart.core.interpret.ChatPrompt
import com.astrochart.core.models.NatalChart
import com.astrochart.data.db.entities.SavedChartEntity
import com.astrochart.data.repository.ChartRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the astrologer chatbot: lets the user pick a saved chart, builds the
 * persona + chart system prompt via [ChatPrompt], and exchanges messages with
 * Claude through the [ChatClient] proxy. Non-streaming — replies are short.
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    enum class Role { USER, ASSISTANT }
    data class ChatMessage(val role: Role, val text: String)

    private val repository = ChartRepository(application.applicationContext)

    /** Saved charts to choose from, live from the database. */
    val savedCharts: StateFlow<List<SavedChartEntity>> =
        repository.getSavedCharts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedChartName = MutableStateFlow<String?>(null)
    val selectedChartName: StateFlow<String?> = _selectedChartName.asStateFlow()

    private val _greeting = MutableStateFlow<String?>(null)
    val greeting: StateFlow<String?> = _greeting.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val appContext = application.applicationContext
    private var systemPrompt: String = ""
    private var language: Language = Language.EN
    // The loaded chart is kept so the prompt/greeting can be rebuilt if the app
    // language changes before the conversation starts.
    private var currentChart: NatalChart? = null
    private var currentName: String = ""
    private var sendJob: Job? = null
    private var selecting: Boolean = false
    private var apiClient: AnthropicApi? = null

    /** True until an API key is entered, or when the user asks to change it. */
    private val _showKeyEntry = MutableStateFlow(ApiKeyStore.load(appContext).isBlank())
    val showKeyEntry: StateFlow<Boolean> = _showKeyEntry.asStateFlow()

    /** Whether an API key is stored, so chat can actually send. */
    fun hasApiKey(): Boolean = ApiKeyStore.load(appContext).isNotBlank()

    /** Stores the user's Anthropic API key and returns to the chat if non-blank. */
    fun saveApiKey(key: String) {
        ApiKeyStore.save(appContext, key)
        apiClient = null
        _showKeyEntry.value = key.isBlank()
    }

    /** Re-opens the key entry so the user can change or replace the key. */
    fun editApiKey() { _showKeyEntry.value = true }

    private fun api(): AnthropicApi =
        apiClient ?: ChatClient.create(ApiKeyStore.load(appContext)).also { apiClient = it }

    fun suggestedQuestions(): List<String> = ChatPrompt.suggestedQuestions(language)

    /** Loads a saved chart, builds its system prompt, and opens the conversation. */
    fun selectChart(id: Long, lang: Language) {
        if (selecting) return
        selecting = true
        // Any in-flight reply belongs to a previous chart; abandon it.
        sendJob?.cancel()
        sendJob = null
        _isSending.value = false
        language = lang
        _error.value = null
        viewModelScope.launch {
            try {
                val entity = repository.getChartById(id)
                val chart = repository.getNatalChartById(id)
                if (entity == null || chart == null) {
                    _error.value = "not_found"
                    return@launch
                }
                currentChart = chart
                currentName = entity.name
                buildForLanguage(lang)
                _selectedChartName.value = entity.name
                _messages.value = emptyList()
            } finally {
                selecting = false
            }
        }
    }

    /**
     * Rebuilds the system prompt, greeting, and suggestion language when the app
     * language changes — but only before the conversation has started, so an
     * in-progress exchange isn't switched mid-stream.
     */
    fun onLanguageChanged(lang: Language) {
        if (lang == language) return
        language = lang
        if (currentChart != null && _messages.value.isEmpty()) {
            buildForLanguage(lang)
        }
    }

    private fun buildForLanguage(lang: Language) {
        val chart = currentChart ?: return
        val context = ChatPrompt.chartContext(chart, currentName, lang)
        systemPrompt = ChatPrompt.systemPrompt(lang, context)
        _greeting.value = ChatPrompt.greeting(lang, currentName)
    }

    /** Returns to the profile picker. */
    fun clearSelection() {
        sendJob?.cancel()
        sendJob = null
        _isSending.value = false
        _selectedChartName.value = null
        _greeting.value = null
        _messages.value = emptyList()
        _error.value = null
        systemPrompt = ""
        currentChart = null
        currentName = ""
    }

    fun clearError() { _error.value = null }

    /** Sends [text] as a user turn and appends Claude's reply. */
    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isSending.value) return
        if (!hasApiKey() || systemPrompt.isEmpty()) {
            _error.value = "not_configured"
            return
        }

        _messages.value = _messages.value + ChatMessage(Role.USER, trimmed)
        _isSending.value = true
        _error.value = null

        sendJob = viewModelScope.launch {
            try {
                // Send a bounded, valid history: drop the oldest turns, then any
                // leading assistant turn so the array always starts with a user
                // message (the Messages API requires this and strict alternation).
                val history = _messages.value
                    .takeLast(MAX_HISTORY)
                    .dropWhile { it.role == Role.ASSISTANT }
                    .map {
                        ApiMessage(
                            role = if (it.role == Role.USER) "user" else "assistant",
                            content = it.text
                        )
                    }
                val response = api().sendMessage(
                    ChatRequest(
                        model = MODEL,
                        maxTokens = MAX_TOKENS,
                        system = systemPrompt,
                        messages = history
                    )
                )
                val reply = response.text().trim()
                if (reply.isEmpty()) {
                    rollbackLastUser()
                    _error.value = "send_failed"
                } else {
                    _messages.value = _messages.value + ChatMessage(Role.ASSISTANT, reply)
                }
            } catch (t: Throwable) {
                // Remove the optimistic user turn so a retry doesn't produce two
                // consecutive user messages (which the API rejects).
                rollbackLastUser()
                _error.value = "send_failed"
            } finally {
                _isSending.value = false
            }
        }
    }

    private fun rollbackLastUser() {
        val current = _messages.value
        if (current.isNotEmpty() && current.last().role == Role.USER) {
            _messages.value = current.dropLast(1)
        }
    }

    private companion object {
        const val MODEL = "claude-haiku-4-5"
        const val MAX_TOKENS = 1024
        const val MAX_HISTORY = 20
    }
}
