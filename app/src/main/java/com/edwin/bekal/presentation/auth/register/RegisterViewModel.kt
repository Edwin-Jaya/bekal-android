package com.edwin.bekal.presentation.auth.register

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwin.bekal.core.error.toErrorMessage
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.core.network.onFailure
import com.edwin.bekal.core.network.onSuccess
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.dto.CreateBankAccountRequestDto
import com.edwin.bekal.data.dto.RegisterRequestDto
import com.edwin.bekal.utils.clearPendingUploads
import com.edwin.bekal.utils.toIsoDateString
import com.edwin.bekal.utils.toSafeUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val validEmploymentTypes = setOf(
        "karyawan_tetap",
        "karyawan_kontrak",
        "wiraswasta",
        "profesional",
        "lainnya"
    )

    // --- STEP 1: Akun & Data Diri ---
    fun onFullNameChange(fullName: String) {
        _uiState.update { it.copy(fullName = fullName) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onPhoneNumberChange(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun onDateOfBirthChange(dob: String) {
        _uiState.update { it.copy(dateOfBirth = dob) }
    }

    fun onGenderChange(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    // --- STEP 2: NIK & KTP ---
    fun onNikChange(nik: String) {
        _uiState.update { it.copy(nik = nik) }
    }

    fun onKtpImagePathChange(path: String?) {
        _uiState.update { it.copy(ktpImagePath = path) }
    }

    // --- STEP 3: Profesi & Pekerjaan ---
    fun onJobTypeChange(value: String) {
        _uiState.update {
            it.copy(
                jobType = value,
                jobTypeError = if (value in validEmploymentTypes) null else it.jobTypeError
            )
        }
    }

    fun onCompanyNameChange(value: String) {
        _uiState.update {
            it.copy(
                companyName = value,
                companyNameError = if (value.isNotBlank()) null else it.companyNameError
            )
        }
    }

    fun onIndustryChange(value: String) {
        _uiState.update {
            it.copy(
                industry = value,
                industryError = if (value.isNotBlank()) null else it.industryError
            )
        }
    }

    fun onPositionChange(value: String) {
        _uiState.update {
            it.copy(
                position = value,
                positionError = if (value.isNotBlank()) null else it.positionError
            )
        }
    }

    fun onEmploymentStartDateChange(value: String) {
        _uiState.update {
            it.copy(
                employmentStartDate = value,
                employmentStartDateError = if (value.isNotBlank()) null else it.employmentStartDateError
            )
        }
    }

    fun onMonthlyIncomeChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.update {
            it.copy(
                monthlyIncome = digitsOnly,
                monthlyIncomeError = if (digitsOnly.isNotBlank()) null else it.monthlyIncomeError
            )
        }
    }

    fun onOtherIncomeChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.update { it.copy(otherIncome = digitsOnly) }
    }

    // --- STEP 4: Finansial & Dokumen ---
    fun onBankNameChange(value: String) {
        _uiState.update { it.copy(bankName = value) }
    }

    fun onBankAccountNumberChange(value: String) {
        _uiState.update { it.copy(bankAccountNumber = value) }
    }

    fun onPaySlipPathsChange(paths: List<String>) {
        _uiState.update { it.copy(paySlipPaths = paths) }
    }

    fun onTermsAgreedChange(isAgreed: Boolean) {
        _uiState.update { it.copy(isTermsAgreed = isAgreed) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- Validation Methods ---
    fun validateJobStep(): Boolean {
        val currentState = _uiState.value

        val jobTypeError = when {
            currentState.jobType.isBlank() -> "Pilihan tipe pekerjaan wajib diisi"
            currentState.jobType !in validEmploymentTypes -> "Tipe pekerjaan tidak valid"
            else -> null
        }

        val companyNameError = if (currentState.companyName.isBlank()) {
            "Nama perusahaan wajib diisi"
        } else null

        val industryError = if (currentState.industry.isBlank()) {
            "Bidang industri wajib diisi"
        } else null

        val positionError = if (currentState.position.isBlank()) {
            "Jabatan / posisi wajib diisi"
        } else null

        val employmentStartDateError = if (currentState.employmentStartDate.isBlank()) {
            "Tanggal mulai bekerja wajib diisi"
        } else null

        val incomeAmount = currentState.monthlyIncome.toLongOrNull() ?: 0L
        val monthlyIncomeError = when {
            currentState.monthlyIncome.isBlank() -> "Pendapatan bersih bulanan wajib diisi"
            incomeAmount <= 0 -> "Pendapatan harus lebih dari Rp 0"
            else -> null
        }

        val isValid = listOf(
            jobTypeError,
            companyNameError,
            industryError,
            positionError,
            employmentStartDateError,
            monthlyIncomeError
        ).all { it == null }

        _uiState.update {
            it.copy(
                jobTypeError = jobTypeError,
                companyNameError = companyNameError,
                industryError = industryError,
                positionError = positionError,
                employmentStartDateError = employmentStartDateError,
                monthlyIncomeError = monthlyIncomeError
            )
        }

        return isValid
    }

    private fun validateCurrentStep(): Boolean {
        return when (_uiState.value.currentStep) {
            3 -> validateJobStep()
            4 -> {
                val currentState = _uiState.value
                if (currentState.bankName.isBlank() || currentState.bankAccountNumber.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Data rekening bank wajib diisi") }
                    return false
                }
                if (!currentState.isTermsAgreed) {
                    _uiState.update { it.copy(errorMessage = "Anda harus menyetujui Syarat & Ketentuan") }
                    return false
                }
                true
            }
            else -> true
        }
    }

    // --- Navigation & Submit Flow ---
    fun onNextStep(context: Context) {
        if (!validateCurrentStep()) return

        if (_uiState.value.currentStep < 4) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1, errorMessage = null) }
        } else {
            submitRegistration(context)
        }
    }

    fun onPreviousStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1, errorMessage = null) }
        }
    }

    // --- Sequential API Registration Submission ---
    private fun submitRegistration(context: Context) {
        val state = _uiState.value

        if (state.ktpImagePath.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Foto e-KTP wajib diunggah") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    loadingMessage = "Membuat akun pengguna..."
                )
            }

            // Standardisasi format gender untuk Backend (MALE / FEMALE)
            val formattedGender = when (state.gender.trim().lowercase()) {
                "laki-laki", "l", "male" -> "MALE"
                "perempuan", "p", "female" -> "FEMALE"
                else -> state.gender.uppercase()
            }

            // Standardisasi format tanggal ke ISO (yyyy-MM-dd)
            val formattedDob = state.dateOfBirth.toIsoDateString()
            val formattedStartDate = state.employmentStartDate.toIsoDateString()

            // 1. Registrasi Identitas & Detail Pekerjaan
            val registerDto = RegisterRequestDto(
                email = state.email,
                password = state.password,
                fullName = state.fullName,
                phoneNumber = state.phoneNumber,
                nik = state.nik,
                dateOfBirth = formattedDob,
                gender = formattedGender,
                address = state.address,
                employmentType = state.jobType,
                companyName = state.companyName,
                jobTitle = state.position,
                industry = state.industry,
                declaredIncome = state.monthlyIncome.toDoubleOrNull(),
                otherIncome = state.otherIncome.toDoubleOrNull(),
                employmentStartDate = formattedStartDate
            )

            var customerId = ""
            repository.register(registerDto)
                .onSuccess { customerProfile -> customerId = customerProfile.id }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = failure.toErrorMessage())
                    }
                    return@launch
                }

            // 2. Upload foto KTP
            _uiState.update { it.copy(loadingMessage = "Mengunggah dokumen e-KTP...") }
            val ktpUri = state.ktpImagePath.toSafeUri() // Gunakan toSafeUri() bukan Uri.parse()
            repository.uploadDocument(context, customerId, "KTP", ktpUri)
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = failure.toErrorMessage())
                    }
                    return@launch
                }

            // 3. Upload Berkas Slip Gaji (opsional / jika ada)
            if (state.paySlipPaths.isNotEmpty()) {
                _uiState.update { it.copy(loadingMessage = "Mengunggah berkas slip gaji...") }
                for (path in state.paySlipPaths) {
                    val slipUri = path.toSafeUri() // Gunakan toSafeUri()
                    repository.uploadDocument(context, customerId, "SLIP_GAJI", slipUri)
                        .onFailure { failure ->
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = failure.toErrorMessage())
                            }
                            return@launch
                        }
                }
            }

            // 4. Daftarkan Rekening Bank
            _uiState.update { it.copy(loadingMessage = "Menyimpan rekening bank...") }
            val bankDto = CreateBankAccountRequestDto(
                customerId = customerId,
                bankName = state.bankName,
                bankAccountNumber = state.bankAccountNumber,   // sebelumnya: accountNumber
                bankAccountHolder = state.fullName,             // sebelumnya: accountHolderName
                isPrimary = true
            )

            repository.createBankAccount(bankDto)
                .onSuccess {
                    clearPendingUploads(context)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            loadingMessage = ""
                        )
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = failure.toErrorMessage())
                    }
                }
        }
    }
}