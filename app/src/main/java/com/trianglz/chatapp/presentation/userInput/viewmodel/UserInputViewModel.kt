package com.trianglz.chatapp.presentation.userInput.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trianglz.chatapp.domain.repository.UserRepository
import com.trianglz.chatapp.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for username setup screen
 * Follows MVI pattern with unidirectional data flow
 */
@HiltViewModel
class UserInputViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UsernameState())
    val state: StateFlow<UsernameState> = _state.asStateFlow()

    fun onEvent(event: UsernameEvent) {
        when (event) {
            is UsernameEvent.UsernameChanged -> {
                _state.update { it.copy(username = event.username, error = null) }
            }
            is UsernameEvent.SaveUsername -> {
                saveUsername(event.deviceId)
            }
        }
    }

    private fun saveUsername(deviceId: String) {
        val username = _state.value.username.trim()

        // Validation
        when {
            username.isEmpty() -> {
                _state.update { it.copy(error = "Username cannot be empty") }
                return
            }
            username.length < 2 -> {
                _state.update { it.copy(error = "Username must be at least 2 characters") }
                return
            }
            username.length > 20 -> {
                _state.update { it.copy(error = "Username must be less than 20 characters") }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val user = User(id = deviceId, name = username)
                userRepository.saveUser(user)
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to save username: ${e.message}"
                    )
                }
            }
        }
    }
}

/**
 * State for username screen
 */
data class UsernameState(
    val username: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

/**
 * Events that can be triggered on username screen
 */
sealed class UsernameEvent {
    data class UsernameChanged(val username: String) : UsernameEvent()
    data class SaveUsername(val deviceId: String) : UsernameEvent()
}