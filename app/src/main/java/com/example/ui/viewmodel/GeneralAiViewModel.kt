package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.api.ThinkingConfig
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ChatSessionEntity
import com.example.data.repository.DakuRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class GeneralAiMode(val label: String, val systemPrompt: String, val defaultModel: String) {
    GENERAL("General Chat", "You are DAKU AI, a world-class general intelligence assistant. Be concise, insightful, and helpful.", "gemini-3.5-flash"),
    WRITING("Creative Writing", "You are DAKU AI Creative Master. Assist with essay writing, story drafting, copywriting, and content polishing.", "gemini-3.5-flash"),
    THINKING("Deep Reasoning", "You are DAKU AI Deep Thinker. Break down complex problems step by step with rigorous logic and analytical depth.", "gemini-3.1-pro-preview"),
    BRAINSTORMING("Brainstorming", "You are DAKU AI Innovation Partner. Provide creative ideas, strategic perspectives, and out-of-the-box suggestions.", "gemini-3.5-flash"),
    FAST("Quick Lite", "You are DAKU AI Express. Provide fast, precise, and direct answers without fluff.", "gemini-3.1-flash-lite-preview")
}

@OptIn(ExperimentalCoroutinesApi::class)
class GeneralAiViewModel(private val repository: DakuRepository) : ViewModel() {

    private val _currentSessionId = MutableStateFlow(generateSessionId())
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.getChatSessions("general")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val messages: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .flatMapLatest { sessionId ->
            repository.getMessagesBySession(sessionId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedMode = MutableStateFlow(GeneralAiMode.GENERAL)
    val selectedMode: StateFlow<GeneralAiMode> = _selectedMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Automatically restore the most recent chat session if available
        viewModelScope.launch {
            repository.getChatSessions("general").collect { sessionList ->
                if (sessionList.isNotEmpty() && _currentSessionId.value.startsWith("session_") && messages.value.isEmpty()) {
                    val latest = sessionList.firstOrNull()
                    if (latest != null && latest.sessionId != _currentSessionId.value && latest.messageCount > 0) {
                        _currentSessionId.value = latest.sessionId
                    }
                }
            }
        }
    }

    fun setMode(mode: GeneralAiMode) {
        _selectedMode.value = mode
    }

    fun startNewSession() {
        _currentSessionId.value = generateSessionId()
        _errorMessage.value = null
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        _errorMessage.value = null
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                startNewSession()
            }
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun toggleBookmark(id: Long, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleMessageBookmark(id, isBookmarked)
        }
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _isLoading.value) return

        val currentMode = _selectedMode.value
        val currentSession = _currentSessionId.value
        val apiKey = RetrofitClient.getApiKey()

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Ensure Session Entity exists in Room
            val existingSession = repository.getSessionById(currentSession)
            if (existingSession == null) {
                val sessionTitle = if (prompt.length > 32) prompt.take(32) + "..." else prompt
                repository.saveChatSession(
                    ChatSessionEntity(
                        sessionId = currentSession,
                        suite = "general",
                        title = sessionTitle,
                        createdAt = System.currentTimeMillis(),
                        lastModified = System.currentTimeMillis(),
                        previewText = prompt,
                        messageCount = 0
                    )
                )
            }

            // Save user message in Room
            val userMsg = ChatMessageEntity(
                sessionId = currentSession,
                suite = "general",
                role = "user",
                content = prompt,
                modelName = currentMode.defaultModel,
                timestamp = System.currentTimeMillis()
            )
            repository.saveChatMessage(userMsg)

            try {
                val modelName = currentMode.defaultModel
                val isThinking = currentMode == GeneralAiMode.THINKING

                val genConfig = if (isThinking) {
                    GenerationConfig(
                        thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH")
                    )
                } else null

                // Gather conversation history from Room for context
                val conversationHistory = messages.value.map { msg ->
                    Content(
                        role = if (msg.role == "user") "user" else "model",
                        parts = listOf(Part(text = msg.content))
                    )
                }

                val currentContent = Content(role = "user", parts = listOf(Part(text = prompt)))
                val requestContents = (conversationHistory + currentContent).takeLast(10)

                val request = GenerateContentRequest(
                    contents = requestContents,
                    systemInstruction = Content(
                        parts = listOf(Part(text = currentMode.systemPrompt))
                    ),
                    generationConfig = genConfig
                )

                val response = RetrofitClient.service.generateContent(
                    model = modelName,
                    apiKey = apiKey,
                    request = request
                )

                val responseText = response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: response.error?.message
                    ?: "No response received from DAKU AI."

                val aiMsg = ChatMessageEntity(
                    sessionId = currentSession,
                    suite = "general",
                    role = "model",
                    content = responseText,
                    modelName = modelName,
                    timestamp = System.currentTimeMillis(),
                    thinkingText = if (isThinking) "High Thinking Mode Activated (gemini-3.1-pro-preview)" else null
                )
                repository.saveChatMessage(aiMsg)

            } catch (e: Exception) {
                _errorMessage.value = "Failed to communicate with DAKU AI: ${e.localizedMessage}"
                val errorMsg = ChatMessageEntity(
                    sessionId = currentSession,
                    suite = "general",
                    role = "model",
                    content = "Error: Unable to connect. Please check your API key and connection.",
                    modelName = currentMode.defaultModel,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveChatMessage(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.deleteSession(_currentSessionId.value)
            startNewSession()
        }
    }

    private fun generateSessionId(): String {
        return "session_" + UUID.randomUUID().toString().take(8)
    }
}
