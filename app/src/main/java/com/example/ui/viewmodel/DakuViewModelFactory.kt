package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.DakuRepository

class DakuViewModelFactory(private val repository: DakuRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GeneralAiViewModel::class.java) -> {
                GeneralAiViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ResearchStudioViewModel::class.java) -> {
                ResearchStudioViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ImageStudioViewModel::class.java) -> {
                ImageStudioViewModel(repository) as T
            }
            modelClass.isAssignableFrom(VideoStudioViewModel::class.java) -> {
                VideoStudioViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AudioStudioViewModel::class.java) -> {
                AudioStudioViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TalkingAssistantViewModel::class.java) -> {
                TalkingAssistantViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
