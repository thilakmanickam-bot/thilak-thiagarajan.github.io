package com.astrochart.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astrochart.chat.ApiMessage
import com.astrochart.chat.ChatClient
import com.astrochart.chat.ChatRequest
import com.astrochart.core.i18n.Language
import com.astrochart.core.interpret.ChatPrompt
import com.astrochart.data.db.entities.SavedChartEntity
import com.astrochart.data.repository.ChartRepository
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

    private var systemPrompt: String = ""
    private var language: Language = Language.EN
    private val api by lazy { ChatClient.create() }

    /** Whether a proxy is configured; when false, chat cannot send. */
    fun isConfigured(): Boolean = ChatClient.isConfigured()

    fun suggestedQuestions(): List<String> = ChatPrompt.suggestedQuestions(language)

    /** Loads a saved chart, builds its system prompt, and opens the conversation. */
    fun selectChart(id: Long, lang: Language) {
        language = lang
        _error.value = null
        viewModelScope.launch {
            val entity = repository.getChartById(id)
            val chart = repository.getNatalChartById(id)
            if (entity == null || chart == null) {
                _error.value = "not_found"
                return@launch
            }
            val context = ChatPrompt.chartContext(chart, entity.name, lang)
            systemPrompt = ChatPrompt.systemPrompt(lang, context)
            _selectedChartName.value = entity.name
            _greeting.value = ChatPrompt.greeting(lang, entity.name)
            _messages.value = emptyList()
        }
    }

    /** Returns to the profile picker. */
    fun clearSelection() {
        _selectedChartName.value = null
        _greeting.value = null
        _messages.value = emptyList()
        _error.value = null
        systemPrompt = ""
    }

    fun clearError() { _error.value = null }

    /** Sends [text] as a user turn and appends Claude's reply. */
    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isSending.value) return
        if (!isConfigured() || systemPrompt.isEmpty()) {
            _error.value = "not_configured"
            return
        }

        _messages.value = _messages.value + ChatMessage(Role.USER, trimmed)
        _isSending.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Send the recent history (bounded) so context stays coherent
                // without unbounded token growth. Starts with a user turn.
                val history = _messages.value.takeLast(MAX_HISTORY).map {
                    ApiMessage(
                        role = if (it.role == Role.USER) "user" else "assistant",
                        content = it.text
                    )
                }
                val response = api.sendMessage(
                    ChatRequest(
                        model = MODEL,
                        maxTokens = MAX_TOKENS,
                        system = systemPrompt,
                        messages = history
                    )
                )
                val reply = response.text().trim()
                if (reply.isEmpty()) {
                    _error.value = "send_failed"
                } else {
                    _messages.value = _messages.value + ChatMessage(Role.ASSISTANT, reply)
                }
            } catch (t: Throwable) {
                _error.value = "send_failed"
            } finally {
                _isSending.value = false
            }
        }
    }

    private companion object {
        const val MODEL = "claude-haiku-4-5"
        const val MAX_TOKENS = 1024
        const val MAX_HISTORY = 20
    }
}
