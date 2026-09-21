package com.edwin.bekal.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwin.bekal.core.error.CommonFailure
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.core.network.onFailure
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.dto.LoginRequestDto
import com.edwin.bekal.data.notification.DeviceTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val deviceTokenManager: DeviceTokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Membaca email tersimpan dari local storage (DataStore/SharedPreferences via Repository)
     */
    val savedEmail: String?
        get() = authRepository.getRememberedEmail()

    init {
        observeSession()
    }

    fun refreshSession() {
        viewModelScope.launch {
            authRepository.refreshProfile()
                .onFailure { failure ->
                    // Silent fail
                }
        }
    }

    fun loginWithGoogle(idToken: String, email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val result = authRepository.loginWithGoogle(idToken = idToken, email = email)

            result.onSuccess { googleResult ->
                when (googleResult) {
                    is AuthRepository.GoogleAuthResult.ExistingUser -> {
                        _uiState.update { it.copy(
                            isSubmitting = false,
                            status = AuthStatus.AUTHENTICATED
                        )}
                    }
                    is AuthRepository.GoogleAuthResult.NewUser -> {
                        // ✅ Simpan email, trigger navigate to register
                        _uiState.update { it.copy(
                            isSubmitting = false,
                            pendingGoogleEmail = googleResult.email
                        )}
                    }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(
                    isSubmitting = false,
                    errorMessage = error.localizedMessage ?: "Google Login Failed"
                )}
            }
        }
    }

    // ✅ Tambah fungsi clear setelah navigate
    fun clearPendingGoogleEmail() {
        _uiState.update { it.copy(pendingGoogleEmail = null) }
    }

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        if (_uiState.value.isSubmitting) return

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update { state ->
                state.copy(
                    errorMessage = "Email dan password tidak boleh kosong",
                )
            }
            return
        }

        val credentials = LoginRequestDto(email = email, password = password)

        viewModelScope.launch {
            markSubmitting()
            when (val result = authRepository.login(credentials)) {
                is AppResult.Success -> {
                    // Kelola opsi "Ingat saya"
                    if (rememberMe) {
                        authRepository.saveRememberedEmail(email)
                    } else {
                        authRepository.clearRememberedEmail()
                    }

                    _uiState.update { state ->
                        state.copy(
                            errorMessage = null,
                            status = AuthStatus.AUTHENTICATED
                        )
                    }
                    // Registrasi token FCM setelah manual login berhasil
                    deviceTokenManager.syncFcmToken()
                }
                is AppResult.Failure -> {
                    val message = when (val appFailure = result.failure) {
                        is CommonFailure.ApiError -> {
                            appFailure.details.joinToString(", ").ifBlank {
                                appFailure.message ?: "Terjadi kesalahan pada API"
                            }
                        }
                        is CommonFailure.Unauthorized -> "Sesi telah berakhir, silakan login kembali."
                        is CommonFailure.Network -> "Koneksi internet bermasalah."
                        is CommonFailure.Unexpected -> "Terjadi kesalahan tidak terduga."
                        else -> "Login gagal: ${appFailure.javaClass.simpleName}"
                    }

                    _uiState.update { state ->
                        state.copy(
                            errorMessage = message,
                            status = AuthStatus.UNAUTHENTICATED
                        )
                    }
                }
            }

            markIdle()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun markSubmitting() {
        _uiState.update { it.copy(isSubmitting = true, failure = null, errorMessage = null) }
    }

    private fun markIdle() {
        _uiState.update { it.copy(isSubmitting = false) }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                if (session != null) {
                    // Registrasi/sinkronkan token saat app dibuka ulang dalam kondisi tersimpan sesi (auto-login)
                    deviceTokenManager.syncFcmToken()
                }
                _uiState.update { state ->
                    state.copy(
                        status = if (session == null) {
                            AuthStatus.UNAUTHENTICATED
                        } else {
                            AuthStatus.AUTHENTICATED
                        },
                        user = session?.user,
                    )
                }
            }
        }
    }
}