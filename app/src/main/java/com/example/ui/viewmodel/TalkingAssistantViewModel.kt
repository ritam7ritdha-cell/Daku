package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.ChatMessageEntity
import com.example.data.repository.DakuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AssistantFeature(val title: String, val desc: String, val quickPrompt: String) {
    PERSONAL_ASSISTANT("Personal Companion", "Knowledge Q&A, scheduling & general assistance", "Hello DAKU! How can you assist me today?"),
    MOOD_FRESHENER("Mood Freshener", "Lighthearted banter, jokes, motivational talks & relaxing stories", "I need a mood booster! Tell me an uplifting story and a funny joke."),
    MOBILE_CONTROL("Mobile Control", "Hands-free voice action commands without touching phone", "DAKU, execute voice command: Open study notes and set a focus timer.")
}

class TalkingAssistantViewModel(private val repository: DakuRepository) : ViewModel() {

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.getChatMessages("talking")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeFeature = MutableStateFlow(AssistantFeature.PERSONAL_ASSISTANT)
    val activeFeature: StateFlow<AssistantFeature> = _activeFeature.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _statusText = MutableStateFlow("DAKU Assistant is ready. Tap mic to talk.")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    fun selectFeature(feature: AssistantFeature) {
        _activeFeature.value = feature
        sendVoiceCommand(feature.quickPrompt)
    }

    fun toggleListening() {
        if (_isListening.value) {
            _isListening.value = false
            _statusText.value = "Listening paused."
        } else {
            _isListening.value = true
            _statusText.value = "DAKU is listening..."
            // Simulate voice capture
            sendVoiceCommand("Hey DAKU, recommend a quick 5-minute study technique and freshen my mood!")
        }
    }

    fun sendVoiceCommand(spokenText: String) {
        if (spokenText.isBlank()) return

        val feature = _activeFeature.value
        val apiKey = RetrofitClient.getApiKey()

        viewModelScope.launch {
            _isListening.value = false
            _isSpeaking.value = false
            _statusText.value = "Processing command..."

            val userMsg = ChatMessageEntity(
                suite = "talking",
                role = "user",
                content = spokenText,
                modelName = "gemini-3.5-flash"
            )
            repository.saveChatMessage(userMsg)

            try {
                val systemInstructionText = """
                    You are DAKU, the user's personal talking AI companion and mobile assistant.
                    Speak naturally, concisely, warmly, and interactively.
                    If the user asks for mood freshening, be enthusiastic, humorous, and uplifting.
                    If the user asks for mobile control, confirm execution of the hands-free action with high confidence.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = spokenText)))),
                    systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
                )

                val response = RetrofitClient.service.generateContent(
                    model = "gemini-3.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                val aiText = response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: "I'm right here with you! How else can I assist?"

                _isSpeaking.value = true
                _statusText.value = "DAKU is speaking..."

                val aiMsg = ChatMessageEntity(
                    suite = "talking",
                    role = "model",
                    content = aiText,
                    modelName = "gemini-3.5-flash"
                )
                repository.saveChatMessage(aiMsg)

            } catch (e: Exception) {
                _statusText.value = "DAKU Assistant ready."
                val aiMsg = ChatMessageEntity(
                    suite = "talking",
                    role = "model",
                    content = "Hey! I'm here to help you stay productive and refreshed. What would you like to do?",
                    modelName = "gemini-3.5-flash"
                )
                repository.saveChatMessage(aiMsg)
            } finally {
                _isSpeaking.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat("talking")
        }
    }
}
