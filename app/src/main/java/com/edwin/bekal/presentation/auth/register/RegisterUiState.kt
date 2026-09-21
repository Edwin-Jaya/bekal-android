package com.edwin.bekal.presentation.auth.register

data class RegisterUiState(
    val currentStep: Int = 1, // 1, 2, 3, 4

    // --- Step 1: Akun & Data Diri ---
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: String = "",
    val gender: String = "Laki-laki",
    val address: String = "",
    val isEmailFromGoogle: Boolean = false,

    // Step 1 Validation Errors
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val phoneNumberError: String? = null,
    val dateOfBirthError: String? = null,
    val genderError: String? = null,
    val addressError: String? = null,

    // --- Step 2: NIK & KTP ---
    val nik: String = "",
    val ktpImagePath: String? = null,

    // Step 2 Validation Errors
    val nikError: String? = null,
    val ktpImageError: String? = null,

    // --- Step 3: Profesi & Pekerjaan ---
    val jobType: String = "",
    val companyName: String = "",
    val industry: String = "",
    val position: String = "",
    val employmentStartDate: String = "",
    val monthlyIncome: String = "",
    val otherIncome: String = "",

    // Step 3 Validation Errors
    val jobTypeError: String? = null,
    val companyNameError: String? = null,
    val industryError: String? = null,
    val positionError: String? = null,
    val employmentStartDateError: String? = null,
    val monthlyIncomeError: String? = null,

    // --- Step 4: Finansial & Agreement ---
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val paySlipPaths: List<String> = emptyList(),
    val isTermsAgreed: Boolean = false,

    // Step 4 Validation Errors
    val bankNameError: String? = null,
    val bankAccountNumberError: String? = null,
    val paySlipError: String? = null,
    val termsAgreedError: String? = null,

    // --- Submission & Status States ---
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)