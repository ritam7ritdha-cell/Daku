package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.ResearchNoteEntity
import com.example.data.repository.DakuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ResearchTask(val label: String, val promptPrefix: String) {
    SUMMARY("Article & Document Summary", "Provide a comprehensive, high-yield summary of the following text with key takeaways, bullet points, and core message:"),
    NOTES("Thinking Notes", "Convert the following educational content into clear, structured study notes with headings, key terms, and bulleted concepts:"),
    MOCK_TEST("Mock Test & Quiz Generator", "Generate 5 multiple-choice questions with answer keys and explanations based on the following study material:"),
    SEARCH("Search & Fact Finding", "Search up-to-date real-time web information and analyze key insights on the following query:")
}

class ResearchStudioViewModel(private val repository: DakuRepository) : ViewModel() {

    val savedNotes: StateFlow<List<ResearchNoteEntity>> = repository.getAllResearchNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedTask = MutableStateFlow(ResearchTask.SUMMARY)
    val selectedTask: StateFlow<ResearchTask> = _selectedTask.asStateFlow()

    private val _documentText = MutableStateFlow("")
    val documentText: StateFlow<String> = _documentText.asStateFlow()

    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun setTask(task: ResearchTask) {
        _selectedTask.value = task
    }

    fun setDocumentText(text: String) {
        _documentText.value = text
    }

    fun processResearch() {
        val input = _documentText.value.trim()
        if (input.isBlank() || _isLoading.value) return

        val task = _selectedTask.value
        val apiKey = RetrofitClient.getApiKey()

        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Analyzing content with Gemini..."

            try {
                val fullPrompt = "${task.promptPrefix}\n\n$input"
                val toolsConfig = if (task == ResearchTask.SEARCH) {
                    listOf(mapOf("googleSearch" to emptyMap<String, Any>()))
                } else null

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = fullPrompt)))
                    ),
                    tools = toolsConfig,
                    systemInstruction = Content(
                        parts = listOf(Part(text = "You are DAKU Research & Education Engine (NotebookLM / Perplexity style). Generate precise, accurate, and highly educational summaries, notes, and quizzes."))
                    )
                )

                val response = RetrofitClient.service.generateContent(
                    model = "gemini-3.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                val resultText = response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: response.error?.message
                    ?: "Analysis failed."

                _analysisResult.value = resultText

                // Auto-save research note to Room
                val note = ResearchNoteEntity(
                    title = "${task.label}: ${input.take(30)}...",
                    sourceContent = input,
                    summary = resultText,
                    keyConcepts = if (task == ResearchTask.NOTES) resultText else "",
                    mockQuestions = if (task == ResearchTask.MOCK_TEST) resultText else ""
                )
                repository.saveResearchNote(note)
                _statusMessage.value = "Research saved to Notebook!"

            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteResearchNote(id)
        }
    }
}
