package com.edwin.bekal.presentation.auth.register

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.edwin.bekal.core.error.CommonFailure
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.dto.CustomerProfileDto
import com.edwin.bekal.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private lateinit var viewModel: RegisterViewModel
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val sampleCustomerProfile = CustomerProfileDto(
        id = "cust-register-123",
        fullName = "Edwin Jaya",
        email = "edwin@gmail.com",
        phoneNumber = "081234567890",
        address = "Jl. Merdeka No. 10",
        gender = "MALE",
        nik = "1234567890123456",
        status = "PENDING"
    )

    @Before
    fun setUp() {
        viewModel = RegisterViewModel(authRepository)
    }

    // ==========================================
    // FIELD INPUT & PREFILL TESTS
    // ==========================================

    @Test
    fun `prefillGoogleEmail updates email and locks field`() {
        viewModel.prefillGoogleEmail("googleuser@gmail.com")

        val state = viewModel.uiState.value
        assertEquals("googleuser@gmail.com", state.email)
        assertTrue(state.isEmailFromGoogle)
    }

    @Test
    fun `onPhoneNumberChange filters non-digit characters`() {
        viewModel.onPhoneNumberChange("0812-3456-7890")

        assertEquals("081234567890", viewModel.uiState.value.phoneNumber)
    }

    @Test
    fun `onNikChange filters non-digits and caps at 16 characters`() {
        viewModel.onNikChange("1234-5678-9012-3456-789")

        assertEquals("1234567890123456", viewModel.uiState.value.nik)
    }

    // ==========================================
    // STEP 1 VALIDATION TESTS (Akun & Data Diri)
    // ==========================================

    @Test
    fun `validateStepAccount returns false when required fields are empty`() {
        viewModel.onGenderChange("")
        val isValid = viewModel.validateStepAccount()

        assertFalse(isValid)
        val state = viewModel.uiState.value
        assertEquals("Nama lengkap wajib diisi", state.fullNameError)
        assertEquals("Alamat email wajib diisi", state.emailError)
        assertEquals("Kata sandi wajib diisi", state.passwordError)
        assertEquals("Nomor handphone wajib diisi", state.phoneNumberError)
        assertEquals("Tanggal lahir wajib diisi", state.dateOfBirthError)
        assertEquals("Jenis kelamin wajib dipilih", state.genderError)
        assertEquals("Alamat domisili wajib diisi", state.addressError)
    }

    @Test
    fun `validateStepAccount returns false for invalid email and short password`() {
        viewModel.onFullNameChange("Edwin Jaya")
        viewModel.onEmailChange("not-gmail@yahoo.com")
        viewModel.onPasswordChange("short")
        viewModel.onPhoneNumberChange("08123")
        viewModel.onDateOfBirthChange("15/08/1995")
        viewModel.onGenderChange("Laki-laki")
        viewModel.onAddressChange("Jl. Sudirman No. 1")

        val isValid = viewModel.validateStepAccount()

        assertFalse(isValid)
        val state = viewModel.uiState.value
        assertEquals("Email harus berformat valid (contoh: user@gmail.com)", state.emailError)
        assertEquals("Kata sandi minimal 8 karakter", state.passwordError)
        assertEquals("Nomor handphone tidak valid", state.phoneNumberError)
    }

    @Test
    fun `validateStepAccount returns true when all step 1 fields are valid`() {
        fillStep1ValidData()

        val isValid = viewModel.validateStepAccount()

        assertTrue(isValid)
        val state = viewModel.uiState.value
        assertNull(state.fullNameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.phoneNumberError)
        assertNull(state.dateOfBirthError)
        assertNull(state.genderError)
        assertNull(state.addressError)
    }

    // ==========================================
    // STEP 2 VALIDATION TESTS (NIK & KTP)
    // ==========================================

    @Test
    fun `validateStepIdentity returns false when NIK is not 16 digits or KTP is missing`() {
        viewModel.onNikChange("12345")
        viewModel.onKtpImagePathChange(null)

        val isValid = viewModel.validateStepIdentity()

        assertFalse(isValid)
        val state = viewModel.uiState.value
        assertEquals("NIK harus terdiri dari 16 digit angka", state.nikError)
        assertEquals("Foto fisik e-KTP wajib diunggah", state.ktpImageError)
    }

    @Test
    fun `validateStepIdentity returns true when NIK is 16 digits and KTP path is set`() {
        viewModel.onNikChange("1234567890123456")
        viewModel.onKtpImagePathChange("file:///storage/ktp.jpg")

        val isValid = viewModel.validateStepIdentity()

        assertTrue(isValid)
        val state = viewModel.uiState.value
        assertNull(state.nikError)
        assertNull(state.ktpImageError)
    }

    // ==========================================
    // STEP 3 VALIDATION TESTS (Pekerjaan)
    // ==========================================

    @Test
    fun `validateJobStep returns false when employment fields are invalid or empty`() {
        viewModel.onJobTypeChange("invalid_type")
        viewModel.onMonthlyIncomeChange("0")

        val isValid = viewModel.validateJobStep()

        assertFalse(isValid)
        val state = viewModel.uiState.value
        assertEquals("Tipe pekerjaan tidak valid", state.jobTypeError)
        assertEquals("Nama perusahaan wajib diisi", state.companyNameError)
        assertEquals("Bidang industri wajib diisi", state.industryError)
        assertEquals("Jabatan / posisi wajib diisi", state.positionError)
        assertEquals("Tanggal mulai bekerja wajib diisi", state.employmentStartDateError)
        assertEquals("Pendapatan harus lebih dari Rp 0", state.monthlyIncomeError)
    }

    @Test
    fun `validateJobStep returns true when all job fields are valid`() {
        fillStep3ValidData()

        val isValid = viewModel.validateJobStep()

        assertTrue(isValid)
        val state = viewModel.uiState.value
        assertNull(state.jobTypeError)
        assertNull(state.companyNameError)
        assertNull(state.industryError)
        assertNull(state.positionError)
        assertNull(state.employmentStartDateError)
        assertNull(state.monthlyIncomeError)
    }

    // ==========================================
    // STEP 4 VALIDATION TESTS (Finansial & Dokumen)
    // ==========================================

    @Test
    fun `validateStepFinancial returns false when financial fields or terms are missing`() {
        val isValid = viewModel.validateStepFinancial()

        assertFalse(isValid)
        val state = viewModel.uiState.value
        assertEquals("Nama bank penerima wajib diisi", state.bankNameError)
        assertEquals("Nomor rekening bank wajib diisi", state.bankAccountNumberError)
        assertEquals("Minimal 1 dokumen slip gaji wajib diunggah", state.paySlipError)
        assertEquals("Anda wajib menyetujui Syarat & Ketentuan Layanan", state.termsAgreedError)
    }

    @Test
    fun `validateStepFinancial returns true when all financial fields are valid`() {
        fillStep4ValidData()

        val isValid = viewModel.validateStepFinancial()

        assertTrue(isValid)
        val state = viewModel.uiState.value
        assertNull(state.bankNameError)
        assertNull(state.bankAccountNumberError)
        assertNull(state.paySlipError)
        assertNull(state.termsAgreedError)
    }

    // ==========================================
    // NAVIGATION TESTS
    // ==========================================

    @Test
    fun `onNextStep advances step when current step is valid`() {
        assertEquals(1, viewModel.uiState.value.currentStep)
        fillStep1ValidData()

        viewModel.onNextStep(context)

        assertEquals(2, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `onNextStep does not advance when validation fails`() {
        assertEquals(1, viewModel.uiState.value.currentStep)

        viewModel.onNextStep(context)

        assertEquals(1, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `onPreviousStep decrements step`() {
        fillStep1ValidData()
        viewModel.onNextStep(context)
        assertEquals(2, viewModel.uiState.value.currentStep)

        viewModel.onPreviousStep()
        assertEquals(1, viewModel.uiState.value.currentStep)
    }

    // ==========================================
    // SUBMISSION FLOW TESTS
    // ==========================================

    @Test
    fun `submitRegistration success flow creates account, uploads documents, creates bank, and sets isSuccess true`() = runTest {
        fillAllStepsValidData()

        coEvery { authRepository.register(any()) } returns AppResult.success(sampleCustomerProfile)
        coEvery { authRepository.uploadDocument(any(), "cust-register-123", "KTP", any()) } returns AppResult.success(Unit)
        coEvery { authRepository.uploadDocument(any(), "cust-register-123", "SLIP_GAJI", any()) } returns AppResult.success(Unit)
        coEvery { authRepository.createBankAccount(any()) } returns AppResult.success(Unit)

        viewModel.onNextStep(context) // Triggers submitRegistration because currentStep == 4

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertNull(state.errorMessage)

        coVerify(exactly = 1) { authRepository.register(any()) }
        coVerify(exactly = 1) { authRepository.uploadDocument(any(), "cust-register-123", "KTP", any()) }
        coVerify(exactly = 1) { authRepository.uploadDocument(any(), "cust-register-123", "SLIP_GAJI", any()) }
        coVerify(exactly = 1) { authRepository.createBankAccount(any()) }
        coVerify(exactly = 0) { authRepository.rollbackRegistration(any()) }
    }

    @Test
    fun `submitRegistration fails at register step updates errorMessage and does not upload documents`() = runTest {
        fillAllStepsValidData()

        coEvery { authRepository.register(any()) } returns AppResult.failure(
            CommonFailure.ApiError(message = "Email sudah terdaftar")
        )

        viewModel.onNextStep(context)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Email sudah terdaftar", state.errorMessage)

        coVerify(exactly = 1) { authRepository.register(any()) }
        coVerify(exactly = 0) { authRepository.uploadDocument(any(), any(), any(), any()) }
        coVerify(exactly = 0) { authRepository.createBankAccount(any()) }
        coVerify(exactly = 0) { authRepository.rollbackRegistration(any()) }
    }

    @Test
    fun `submitRegistration fails at upload KTP triggers rollback and sets errorMessage`() = runTest {
        fillAllStepsValidData()

        coEvery { authRepository.register(any()) } returns AppResult.success(sampleCustomerProfile)
        coEvery { authRepository.uploadDocument(any(), "cust-register-123", "KTP", any()) } returns AppResult.failure(
            CommonFailure.ApiError(message = "Gagal upload KTP")
        )

        viewModel.onNextStep(context)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Gagal upload KTP", state.errorMessage)

        coVerify(exactly = 1) { authRepository.register(any()) }
        coVerify(exactly = 1) { authRepository.uploadDocument(any(), "cust-register-123", "KTP", any()) }
        coVerify(exactly = 1) { authRepository.rollbackRegistration("cust-register-123") }
        coVerify(exactly = 0) { authRepository.createBankAccount(any()) }
    }

    @Test
    fun `submitRegistration fails at createBankAccount triggers rollback and sets errorMessage`() = runTest {
        fillAllStepsValidData()

        coEvery { authRepository.register(any()) } returns AppResult.success(sampleCustomerProfile)
        coEvery { authRepository.uploadDocument(any(), "cust-register-123", "KTP", any()) } returns AppResult.success(Unit)
        coEvery { authRepository.uploadDocument(any(), "cust-register-123", "SLIP_GAJI", any()) } returns AppResult.success(Unit)
        coEvery { authRepository.createBankAccount(any()) } returns AppResult.failure(
            CommonFailure.ApiError(message = "Nomor rekening tidak valid")
        )

        viewModel.onNextStep(context)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Nomor rekening tidak valid", state.errorMessage)

        coVerify(exactly = 1) { authRepository.rollbackRegistration("cust-register-123") }
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private fun fillStep1ValidData() {
        viewModel.onFullNameChange("Edwin Jaya")
        viewModel.onEmailChange("edwin@gmail.com")
        viewModel.onPasswordChange("Password123!")
        viewModel.onPhoneNumberChange("081234567890")
        viewModel.onDateOfBirthChange("15/08/1995")
        viewModel.onGenderChange("Laki-laki")
        viewModel.onAddressChange("Jl. Merdeka No. 10")
    }

    private fun fillStep2ValidData() {
        viewModel.onNikChange("1234567890123456")
        viewModel.onKtpImagePathChange("file:///sdcard/ktp.jpg")
    }

    private fun fillStep3ValidData() {
        viewModel.onJobTypeChange("karyawan_tetap")
        viewModel.onCompanyNameChange("PT BCA Finance")
        viewModel.onIndustryChange("Financial Services")
        viewModel.onPositionChange("Software Engineer")
        viewModel.onEmploymentStartDateChange("01/01/2020")
        viewModel.onMonthlyIncomeChange("15000000")
        viewModel.onOtherIncomeChange("2000000")
    }

    private fun fillStep4ValidData() {
        viewModel.onBankNameChange("BCA")
        viewModel.onBankAccountNumberChange("1234567890")
        viewModel.onPaySlipPathsChange(listOf("file:///sdcard/slip1.pdf"))
        viewModel.onTermsAgreedChange(true)
    }

    private fun fillAllStepsValidData() {
        fillStep1ValidData()
        fillStep2ValidData()
        fillStep3ValidData()
        fillStep4ValidData()

        // Advance to step 4
        viewModel.onNextStep(context) // to step 2
        viewModel.onNextStep(context) // to step 3
        viewModel.onNextStep(context) // to step 4
        assertEquals(4, viewModel.uiState.value.currentStep)
    }
}
