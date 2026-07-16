package com.astrochart.chat

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for the Halo chat proxy, which forwards to the Anthropic
 * Messages API. The proxy injects the API key and pins the model server-side;
 * the app only sends the conversation. See [ChatClient] for wiring.
 */
interface AnthropicApi {
    @POST("v1/messages")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
}

/** Request body for `POST /v1/messages` (non-streaming). */
data class ChatRequest(
    val model: String,
    @SerializedName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<ApiMessage>
)

/** One turn in the conversation sent to the API. `role` is "user" or "assistant". */
data class ApiMessage(
    val role: String,
    val content: String
)

/** Response body; we only need the text from the first content block. */
data class ChatResponse(
    val content: List<ContentBlock> = emptyList()
) {
    /** Concatenated text of all text blocks, or empty if none. */
    fun text(): String = content.filter { it.type == "text" }.joinToString("") { it.text.orEmpty() }
}

data class ContentBlock(
    val type: String = "",
    val text: String? = null
)
