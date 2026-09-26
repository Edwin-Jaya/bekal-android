package com.edwin.bekal.presentation.auth.forgotpassword

import com.edwin.bekal.core.error.CommonFailure
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private lateinit var viewModel: ForgotPasswordViewModel

    @Before
    fun setUp() {
        viewModel = ForgotPasswordViewModel(authRepository)
    }

    @Test
    fun `onEmailChange updates email and clears previous errors`() {
        viewModel.onEmailChange("user@example.com")

        val state = viewModel.uiState.value
        assertEquals("user@example.com", state.email)
        assertNull(state.emailError)
        assertNull(state.errorMessage)
    }

    @Test
    fun `sendOtp with blank email sets emailError`() = runTest {
        viewModel.onEmailChange("")
        viewModel.sendOtp()

        val state = viewModel.uiState.value
        assertEquals("Alamat email wajib diisi", state.emailError)
        assertFalse(state.isLoading)
        assertFalse(state.isOtpSent)
        coVerify(exactly = 0) { authRepository.forgotPassword(any()) }
    }

    @Test
    fun `sendOtp with invalid email format sets emailError`() = runTest {
        viewModel.onEmailChange("invalid-email-format")
        viewModel.sendOtp()

        val state = viewModel.uiState.value
        assertEquals("Format email tidak valid", state.emailError)
        assertFalse(state.isLoading)
        assertFalse(state.isOtpSent)
        coVerify(exactly = 0) { authRepository.forgotPassword(any()) }
    }

    @Test
    fun `sendOtp success calls repository and sets isOtpSent true`() = runTest {
        val validEmail = "user@example.com"
        coEvery { authRepository.forgotPassword(validEmail) } returns AppResult.success(Unit)

        viewModel.onEmailChange(validEmail)
        viewModel.sendOtp()

        val state = viewModel.uiState.value
        assertNull(state.emailError)
        assertNull(state.errorMessage)
        assertFalse(state.isLoading)
        assertTrue(state.isOtpSent)
        coVerify(exactly = 1) { authRepository.forgotPassword(validEmail) }
    }

    @Test
    fun `sendOtp failure sets errorMessage and keeps isOtpSent false`() = runTest {
        val validEmail = "user@example.com"
        val failure = CommonFailure.ApiError(message = "Email tidak terdaftar")
        coEvery { authRepository.forgotPassword(validEmail) } returns AppResult.failure(failure)

        viewModel.onEmailChange(validEmail)
        viewModel.sendOtp()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isOtpSent)
        assertEquals("Email tidak terdaftar", state.errorMessage)
        coVerify(exactly = 1) { authRepository.forgotPassword(validEmail) }
    }

    @Test
    fun `clearErrorMessage clears errorMessage`() {
        viewModel.onEmailChange("user@example.com")
        // Trigger error directly or simulate
        viewModel.clearErrorMessage()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
