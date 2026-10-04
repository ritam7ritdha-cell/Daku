package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.CreditHistoryEntity
import com.example.data.db.UserCreditsEntity
import com.example.data.repository.CreditRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String = "Cyber Agent",
    val email: String = "user@daku.ai",
    val plan: String = "DAKU Pro Ultra",
    val isLoggedIn: Boolean = true
)

class AuthViewModel(val creditRepository: CreditRepository? = null) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _isSignUpMode = MutableStateFlow(false)
    val isSignUpMode: StateFlow<Boolean> = _isSignUpMode.asStateFlow()

    val userCredits: StateFlow<UserCreditsEntity?> = creditRepository?.getUserCreditsFlow("default_user")
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserCreditsEntity(balance = 50)
        ) ?: MutableStateFlow(UserCreditsEntity(balance = 50))

    val creditHistory: StateFlow<List<CreditHistoryEntity>> = creditRepository?.getCreditHistory("default_user")
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ) ?: MutableStateFlow(emptyList())

    init {
        viewModelScope.launch {
            creditRepository?.ensureUserCreditsInitialized("default_user")
        }
    }

    fun openAuthDialog(signUp: Boolean = false) {
        _isSignUpMode.value = signUp
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
    }

    fun toggleAuthMode() {
        _isSignUpMode.value = !_isSignUpMode.value
    }

    fun login(emailInput: String, nameInput: String = "") {
        val nameToUse = if (nameInput.isNotBlank()) nameInput else emailInput.substringBefore("@").replaceFirstChar { it.uppercase() }
        _userProfile.value = UserProfile(
            name = nameToUse,
            email = emailInput.ifBlank { "agent@daku.ai" },
            plan = "DAKU Pro Ultra",
            isLoggedIn = true
        )
        _showAuthDialog.value = false
    }

    fun logout() {
        _userProfile.value = UserProfile(isLoggedIn = false)
    }
}
