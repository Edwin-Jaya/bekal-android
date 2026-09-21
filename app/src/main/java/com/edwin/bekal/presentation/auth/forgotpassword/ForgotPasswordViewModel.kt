package com.edwin.bekal.presentation.auth.forgotpassword

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwin.bekal.core.error.toErrorMessage
import com.edwin.bekal.core.network.onFailure
import com.edwin.bekal.core.network.onSuccess
import com.edwin.bekal.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun sendOtp() {
        val email = _uiState.value.email

        val emailError = when {
            email.isBlank() -> "Alamat email wajib diisi"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Format email tidak valid"
            else -> null
        }

        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.forgotPassword(email)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isOtpSent = true) }
                }
                .onFailure { failure ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = failure.toErrorMessage()) }
                }
        }
    }
}