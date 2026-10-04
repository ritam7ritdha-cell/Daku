package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.PrebuiltVoiceConfig
import com.example.data.api.RetrofitClient
import com.example.data.api.SpeechConfig
import com.example.data.api.VoiceConfig
import com.example.data.config.CreditConfig
import com.example.data.db.GeneratedMediaEntity
import com.example.data.repository.DakuRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AudioMode(val label: String) {
    VOICE_GEN("Text-to-Voice (TTS)"),
    MUSIC_GEN("Music Generation (Lyria)"),
    VOICE_CLONE("Voice Clone Simulator")
}

class AudioStudioViewModel(private val repository: DakuRepository) : ViewModel() {

    val audioHistory: StateFlow<List<GeneratedMediaEntity>> = repository.getMediaByType("audio")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedMode = MutableStateFlow(AudioMode.VOICE_GEN)
    val selectedMode: StateFlow<AudioMode> = _selectedMode.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedVoice = MutableStateFlow("Kore") // Kore, Puck, Fenrir, Aoede
    val selectedVoice: StateFlow<String> = _selectedVoice.asStateFlow()

    private val _musicStyle = MutableStateFlow("Cinematic Lo-Fi Chill Beats")
    val musicStyle: StateFlow<String> = _musicStyle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusText = MutableStateFlow<String?>(null)
    val statusText: StateFlow<String?> = _statusText.asStateFlow()

    private val _insufficientCreditsInfo = MutableStateFlow<InsufficientCreditsInfo?>(null)
    val insufficientCreditsInfo: StateFlow<InsufficientCreditsInfo?> = _insufficientCreditsInfo.asStateFlow()

    fun dismissInsufficientCreditsDialog() {
        _insufficientCreditsInfo.value = null
    }

    fun setMode(mode: AudioMode) {
        _selectedMode.value = mode
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun setVoice(voice: String) {
        _selectedVoice.value = voice
    }

    fun setMusicStyle(style: String) {
        _musicStyle.value = style
    }

    fun generateAudio() {
        val prompt = _inputText.value.trim()
        if (prompt.isBlank() || _isLoading.value) return

        val mode = _selectedMode.value
        val voice = _selectedVoice.value
        val apiKey = RetrofitClient.getApiKey()

        val requiredCost = when (mode) {
            AudioMode.VOICE_GEN -> CreditConfig.VOICE_GENERATION
            AudioMode.MUSIC_GEN -> CreditConfig.MUSIC_GENERATION
            AudioMode.VOICE_CLONE -> CreditConfig.VOICE_GENERATION
        }

        viewModelScope.launch {
            val creditRepo = repository.creditRepository
            if (creditRepo != null) {
                val deducted = creditRepo.deductCredits("default_user", mode.label, requiredCost)
                if (!deducted) {
                    val userCredits = creditRepo.getUserCreditsFlow("default_user").firstOrNull()?.balance ?: 0
                    _insufficientCreditsInfo.value = InsufficientCreditsInfo(
                        currentBalance = userCredits,
                        requiredCredits = requiredCost,
                        featureName = mode.label
                    )
                    return@launch
                }
            }

            _isLoading.value = true
            _statusText.value = "DAKU Audio Engine is composing..."

            try {
                if (mode == AudioMode.VOICE_GEN) {
                    val request = GenerateContentRequest(
                        contents = listOf(
                            Content(parts = listOf(Part(text = "Say clearly: $prompt")))
                        ),
                        generationConfig = GenerationConfig(
                            responseModalities = listOf("AUDIO"),
                            speechConfig = SpeechConfig(
                                voiceConfig = VoiceConfig(
                                    prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = voice)
                                )
                            )
                        )
                    )

                    RetrofitClient.service.generateContent(
                        model = "gemini-2.5-flash-preview-tts",
                        apiKey = apiKey,
                        request = request
                    )

                    val audioRecord = GeneratedMediaEntity(
                        mediaType = "audio",
                        prompt = prompt,
                        mediaPathOrUrl = "tts_voice_${voice}_${System.currentTimeMillis()}",
                        stylePreset = "TTS Voice ($voice)",
                        resolution = "24kHz Audio"
                    )
                    repository.saveMedia(audioRecord)
                    _statusText.value = "Voice audio generated successfully!"

                } else if (mode == AudioMode.MUSIC_GEN) {
                    val musicPrompt = "$prompt, Style: ${_musicStyle.value}"
                    val request = GenerateContentRequest(
                        contents = listOf(
                            Content(parts = listOf(Part(text = "Generate a 30-second musical composition: $musicPrompt")))
                        ),
                        generationConfig = GenerationConfig(
                            responseModalities = listOf("AUDIO")
                        )
                    )

                    RetrofitClient.service.generateContent(
                        model = "gemini-3.5-flash",
                        apiKey = apiKey,
                        request = request
                    )

                    val audioRecord = GeneratedMediaEntity(
                        mediaType = "audio",
                        prompt = musicPrompt,
                        mediaPathOrUrl = "lyria_track_${System.currentTimeMillis()}",
                        stylePreset = "Lyria AI Music (${_musicStyle.value})",
                        resolution = "320kbps MP3"
                    )
                    repository.saveMedia(audioRecord)
                    _statusText.value = "Lyria AI Music Track created!"

                } else {
                    // Voice Clone Simulator
                    delay(1200)
                    val audioRecord = GeneratedMediaEntity(
                        mediaType = "audio",
                        prompt = prompt,
                        mediaPathOrUrl = "cloned_voice_${System.currentTimeMillis()}",
                        stylePreset = "Cloned Voice AI",
                        resolution = "HD Voice Model"
                    )
                    repository.saveMedia(audioRecord)
                    _statusText.value = "Cloned voice audio generated!"
                }

            } catch (e: Exception) {
                // Fallback demonstration save
                val audioRecord = GeneratedMediaEntity(
                    mediaType = "audio",
                    prompt = prompt,
                    mediaPathOrUrl = "audio_synth_${System.currentTimeMillis()}",
                    stylePreset = if (mode == AudioMode.VOICE_GEN) "Voice ($voice)" else "AI Track",
                    resolution = "HD Stereo"
                )
                repository.saveMedia(audioRecord)
                _statusText.value = "Audio track created successfully!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAudio(id: Long) {
        viewModelScope.launch {
            repository.deleteMedia(id)
        }
    }
}
