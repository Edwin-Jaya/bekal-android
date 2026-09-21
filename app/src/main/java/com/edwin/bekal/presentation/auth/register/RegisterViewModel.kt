package com.edwin.bekal.presentation.auth.register

import android.content.Context
import android.net.Uri
import android.util.Patterns
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

    fun prefillGoogleEmail(email: String) {
        _uiState.update { it.copy(
            email = email,
            isEmailFromGoogle = true  // ← untuk lock field di UI
        )}
    }

    // --- STEP 1: Akun & Data Diri ---
    fun onFullNameChange(fullName: String) {
        _uiState.update {
            it.copy(
                fullName = fullName,
                fullNameError = if (fullName.isNotBlank()) null else it.fullNameError
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = if (email.isNotBlank()) null else it.emailError
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = if (password.isNotBlank()) null else it.passwordError
            )
        }
    }

    fun onPhoneNumberChange(phone: String) {
        val digitsOnly = phone.filter { it.isDigit() }
        _uiState.update {
            it.copy(
                phoneNumber = digitsOnly,
                phoneNumberError = if (digitsOnly.isNotBlank()) null else it.phoneNumberError
            )
        }
    }

    fun onDateOfBirthChange(dob: String) {
        _uiState.update {
            it.copy(
                dateOfBirth = dob,
                dateOfBirthError = if (dob.isNotBlank()) null else it.dateOfBirthError
            )
        }
    }

    fun onGenderChange(gender: String) {
        _uiState.update {
            it.copy(
                gender = gender,
                genderError = if (gender.isNotBlank()) null else it.genderError
            )
        }
    }

    fun onAddressChange(address: String) {
        _uiState.update {
            it.copy(
                address = address,
                addressError = if (address.isNotBlank()) null else it.addressError
            )
        }
    }

    // --- STEP 2: NIK & KTP ---
    fun onNikChange(nik: String) {
        val digitsOnly = nik.filter { it.isDigit() }.take(16)
        _uiState.update {
            it.copy(
                nik = digitsOnly,
                nikError = if (digitsOnly.length == 16) null else it.nikError
            )
        }
    }

    fun onKtpImagePathChange(path: String?) {
        _uiState.update {
            it.copy(
                ktpImagePath = path,
                ktpImageError = if (!path.isNullOrBlank()) null else it.ktpImageError
            )
        }
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
        _uiState.update {
            it.copy(
                bankName = value,
                bankNameError = if (value.isNotBlank()) null else it.bankNameError
            )
        }
    }

    fun onBankAccountNumberChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.update {
            it.copy(
                bankAccountNumber = digitsOnly,
                bankAccountNumberError = if (digitsOnly.isNotBlank()) null else it.bankAccountNumberError
            )
        }
    }

    fun onPaySlipPathsChange(paths: List<String>) {
        _uiState.update {
            it.copy(
                paySlipPaths = paths,
                paySlipError = if (paths.isNotEmpty()) null else it.paySlipError
            )
        }
    }

    fun onTermsAgreedChange(isAgreed: Boolean) {
        _uiState.update {
            it.copy(
                isTermsAgreed = isAgreed,
                termsAgreedError = if (isAgreed) null else it.termsAgreedError
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- Validation Methods per Step ---
    fun validateStepAccount(): Boolean {
        val currentState = _uiState.value

        val fullNameError = if (currentState.fullName.isBlank()) "Nama lengkap wajib diisi" else null

        val isEmailPatternValid = Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()
        val isGmailDomain = currentState.email.lowercase().endsWith("@gmail.com")
        val emailError = when {
            currentState.email.isBlank() -> "Alamat email wajib diisi"
            !isGmailDomain || !isEmailPatternValid -> "Email harus berformat valid (contoh: user@gmail.com)"
            else -> null
        }

        val passwordError = when {
            currentState.password.isBlank() -> "Kata sandi wajib diisi"
            currentState.password.length < 8 -> "Kata sandi minimal 8 karakter"
            else -> null
        }

        val phoneNumberError = when {
            currentState.phoneNumber.isBlank() -> "Nomor handphone wajib diisi"
            currentState.phoneNumber.length < 9 -> "Nomor handphone tidak valid"
            else -> null
        }

        val dateOfBirthError = if (currentState.dateOfBirth.isBlank()) "Tanggal lahir wajib diisi" else null
        val genderError = if (currentState.gender.isBlank()) "Jenis kelamin wajib dipilih" else null
        val addressError = if (currentState.address.isBlank()) "Alamat domisili wajib diisi" else null

        val isValid = listOf(
            fullNameError, emailError, passwordError, phoneNumberError,
            dateOfBirthError, genderError, addressError
        ).all { it == null }

        _uiState.update {
            it.copy(
                fullNameError = fullNameError,
                emailError = emailError,
                passwordError = passwordError,
                phoneNumberError = phoneNumberError,
                dateOfBirthError = dateOfBirthError,
                genderError = genderError,
                addressError = addressError
            )
        }

        return isValid
    }

    fun validateStepIdentity(): Boolean {
        val currentState = _uiState.value

        val nikError = when {
            currentState.nik.isBlank() -> "Nomor NIK wajib diisi"
            currentState.nik.length != 16 -> "NIK harus terdiri dari 16 digit angka"
            else -> null
        }

        val ktpImageError = if (currentState.ktpImagePath.isNullOrBlank()) {
            "Foto fisik e-KTP wajib diunggah"
        } else null

        val isValid = listOf(nikError, ktpImageError).all { it == null }

        _uiState.update {
            it.copy(
                nikError = nikError,
                ktpImageError = ktpImageError
            )
        }

        return isValid
    }

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

    fun validateStepFinancial(): Boolean {
        val currentState = _uiState.value

        val bankNameError = if (currentState.bankName.isBlank()) "Nama bank penerima wajib diisi" else null
        val bankAccountNumberError = if (currentState.bankAccountNumber.isBlank()) "Nomor rekening bank wajib diisi" else null
        val paySlipError = if (currentState.paySlipPaths.isEmpty()) "Minimal 1 dokumen slip gaji wajib diunggah" else null
        val termsAgreedError = if (!currentState.isTermsAgreed) "Anda wajib menyetujui Syarat & Ketentuan Layanan" else null

        val isValid = listOf(
            bankNameError,
            bankAccountNumberError,
            paySlipError,
            termsAgreedError
        ).all { it == null }

        _uiState.update {
            it.copy(
                bankNameError = bankNameError,
                bankAccountNumberError = bankAccountNumberError,
                paySlipError = paySlipError,
                termsAgreedError = termsAgreedError
            )
        }

        return isValid
    }

    private fun validateCurrentStep(): Boolean {
        return when (_uiState.value.currentStep) {
            1 -> validateStepAccount()
            2 -> validateStepIdentity()
            3 -> validateJobStep()
            4 -> validateStepFinancial()
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
            val ktpUri = state.ktpImagePath.toSafeUri()
            repository.uploadDocument(context, customerId, "KTP", ktpUri)
                .onFailure { failure ->
                    _uiState.update { it.copy(loadingMessage = "Membatalkan registrasi...") }
                    repository.rollbackRegistration(customerId)
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = failure.toErrorMessage())
                    }
                    return@launch
                }

            // 3. Upload Berkas Slip Gaji
            if (state.paySlipPaths.isNotEmpty()) {
                _uiState.update { it.copy(loadingMessage = "Mengunggah berkas slip gaji...") }
                for (path in state.paySlipPaths) {
                    val slipUri = path.toSafeUri()
                    repository.uploadDocument(context, customerId, "SLIP_GAJI", slipUri)
                        .onFailure { failure ->
                            _uiState.update { it.copy(loadingMessage = "Membatalkan registrasi...") }
                            repository.rollbackRegistration(customerId)
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
                bankAccountNumber = state.bankAccountNumber,
                bankAccountHolder = state.fullName,
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
                    _uiState.update { it.copy(loadingMessage = "Membatalkan registrasi...") }
                    repository.rollbackRegistration(customerId)
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = failure.toErrorMessage())
                    }
                }
        }
    }
}