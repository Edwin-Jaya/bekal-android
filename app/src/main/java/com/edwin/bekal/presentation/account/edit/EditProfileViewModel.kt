package com.edwin.bekal.presentation.account.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwin.bekal.core.error.toErrorMessage
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.core.network.onFailure
import com.edwin.bekal.core.network.onSuccess
import com.edwin.bekal.core.network.requirePayload
import com.edwin.bekal.core.network.runApiCatching
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.customer.remote.CustomerApi
import com.edwin.bekal.data.dto.CustomerProfileDto
import com.edwin.bekal.data.dto.UpdateCustomerRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

sealed interface EditProfileUiState {
    data object Loading : EditProfileUiState
    data class Success(val profile: CustomerProfileDto) : EditProfileUiState
    data class Error(val message: String) : EditProfileUiState
}

sealed interface SaveUiState {
    data object Idle : SaveUiState
    data object Loading : SaveUiState
    data object Success : SaveUiState
    data class Error(val message: String) : SaveUiState
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val customerApi: CustomerApi,
    private val json: Json,
    val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState: StateFlow<EditProfileUiState> = _uiState

    private val _saveState = MutableStateFlow<SaveUiState>(SaveUiState.Idle)
    val saveState: StateFlow<SaveUiState> = _saveState

    // Form fields
    val fullName = MutableStateFlow("")
    val phoneNumber = MutableStateFlow("")
    val address = MutableStateFlow("")
    val gender = MutableStateFlow("")

    private var customerId: String? = null

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            val result: AppResult<CustomerProfileDto> = runApiCatching(json) {
                when (val r = customerApi.getProfile().requirePayload()) {
                    is AppResult.Success -> r
                    is AppResult.Failure -> r
                }
            }
            result
                .onSuccess { profile ->
                    customerId = profile.id
                    fullName.value = profile.fullName
                    phoneNumber.value = profile.phoneNumber.orEmpty()
                    address.value = profile.address.orEmpty()
                    gender.value = profile.gender.orEmpty()
                    _uiState.value = EditProfileUiState.Success(profile)
                }
                .onFailure { failure ->
                    _uiState.value = EditProfileUiState.Error(failure.toErrorMessage())
                }
        }
    }

    fun saveProfile() {
        val id = customerId ?: return

        viewModelScope.launch {
            _saveState.value = SaveUiState.Loading
            val request = UpdateCustomerRequestDto(
                fullName = fullName.value.ifBlank { null },
                phoneNumber = phoneNumber.value.ifBlank { null },
                address = address.value.ifBlank { null },
                gender = gender.value.ifBlank { null }
            )

            val result: AppResult<CustomerProfileDto> = runApiCatching(json) {
                when (val r = customerApi.updateProfile(id, request).requirePayload()) {
                    is AppResult.Success -> r
                    is AppResult.Failure -> r
                }
            }
            result
                .onSuccess { updatedProfile ->
                    authRepository.updateLocalSession(updatedProfile)  // ⬅️ tambahan
                    _saveState.value = SaveUiState.Success
                }
                .onFailure { failure ->
                    _saveState.value = SaveUiState.Error(failure.toErrorMessage())
                }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveUiState.Idle
    }
}