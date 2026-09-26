package com.edwin.bekal.presentation.auth

import com.edwin.bekal.core.error.CommonFailure
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.dto.AuthSession
import com.edwin.bekal.data.dto.AuthUser
import com.edwin.bekal.data.dto.LoginRequestDto
import com.edwin.bekal.data.notification.DeviceTokenManager
import com.edwin.bekal.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val deviceTokenManager = mockk<DeviceTokenManager>(relaxed = true)
    private val sessionFlow = MutableStateFlow<AuthSession?>(null)

    private lateinit var viewModel: AuthViewModel

    private val sampleUser = AuthUser(
        id = "user-123",
        name = "Edwin Jaya",
        email = "edwin@example.com",
        tipe = "Bearer",
        roles = listOf("CUSTOMER")
    )

    private val sampleSession = AuthSession(
        user = sampleUser,
        accessToken = "mock-token",
        expiresAtMillis = System.currentTimeMillis() + 3600_000
    )

    @Before
    fun setUp() {
        every { authRepository.observeSession() } returns sessionFlow
        viewModel = AuthViewModel(authRepository, deviceTokenManager)
    }

    // ==========================================
    // LOGIN TESTS
    // ==========================================

    @Test
    fun `login with empty email and password sets validation error message`() = runTest {
        viewModel.login(email = "", password = "")

        val state = viewModel.uiState.value
        assertEquals("Email dan password tidak boleh kosong", state.errorMessage)
        assertFalse(state.isSubmitting)
        coVerify(exactly = 0) { authRepository.login(any()) }
    }

    @Test
    fun `login with empty password sets validation error message`() = runTest {
        viewModel.login(email = "test@example.com", password = "")

        val state = viewModel.uiState.value
        assertEquals("Email dan password tidak boleh kosong", state.errorMessage)
        assertFalse(state.isSubmitting)
        coVerify(exactly = 0) { authRepository.login(any()) }
    }

    @Test
    fun `login success updates state to AUTHENTICATED and syncs FCM token`() = runTest {
        val email = "edwin@example.com"
        val password = "Password123"
        coEvery {
            authRepository.login(LoginRequestDto(email = email, password = password))
        } returns AppResult.success(sampleSession)

        viewModel.login(email = email, password = password, rememberMe = false)

        val state = viewModel.uiState.value
        assertEquals(AuthStatus.AUTHENTICATED, state.status)
        assertNull(state.errorMessage)
        assertFalse(state.isSubmitting)
        coVerify(exactly = 1) { deviceTokenManager.syncFcmToken() }
        verify(exactly = 1) { authRepository.clearRememberedEmail() }
    }

    @Test
    fun `login success with rememberMe true saves email`() = runTest {
        val email = "edwin@example.com"
        val password = "Password123"
        coEvery {
            authRepository.login(LoginRequestDto(email = email, password = password))
        } returns AppResult.success(sampleSession)

        viewModel.login(email = email, password = password, rememberMe = true)

        verify(exactly = 1) { authRepository.saveRememberedEmail(email) }
    }

    @Test
    fun `login failure with ApiError updates state to UNAUTHENTICATED with error message`() = runTest {
        val email = "wrong@example.com"
        val password = "wrongpassword"
        val apiError = CommonFailure.ApiError(
            message = "Email atau password salah",
            details = listOf("Email atau password salah")
        )
        coEvery {
            authRepository.login(LoginRequestDto(email = email, password = password))
        } returns AppResult.failure(apiError)

        viewModel.login(email = email, password = password)

        val state = viewModel.uiState.value
        assertEquals(AuthStatus.UNAUTHENTICATED, state.status)
        assertEquals("Email atau password salah", state.errorMessage)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `login failure with Unauthorized sets session expired message`() = runTest {
        coEvery {
            authRepository.login(any())
        } returns AppResult.failure(CommonFailure.Unauthorized)

        viewModel.login(email = "test@example.com", password = "password")

        val state = viewModel.uiState.value
        assertEquals("Sesi telah berakhir, silakan login kembali.", state.errorMessage)
        assertEquals(AuthStatus.UNAUTHENTICATED, state.status)
    }

    @Test
    fun `login failure with Network error sets network connection message`() = runTest {
        coEvery {
            authRepository.login(any())
        } returns AppResult.failure(CommonFailure.Network(Exception("No Internet")))

        viewModel.login(email = "test@example.com", password = "password")

        val state = viewModel.uiState.value
        assertEquals("Koneksi internet bermasalah.", state.errorMessage)
        assertEquals(AuthStatus.UNAUTHENTICATED, state.status)
    }

    @Test
    fun `loginWithGoogle with ExistingUser sets status to AUTHENTICATED`() = runTest {
        coEvery {
            authRepository.loginWithGoogle("id-token", "edwin@gmail.com")
        } returns Result.success(AuthRepository.GoogleAuthResult.ExistingUser)

        viewModel.loginWithGoogle(idToken = "id-token", email = "edwin@gmail.com")

        val state = viewModel.uiState.value
        assertEquals(AuthStatus.AUTHENTICATED, state.status)
        assertNull(state.errorMessage)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `loginWithGoogle with NewUser sets pendingGoogleEmail`() = runTest {
        coEvery {
            authRepository.loginWithGoogle("id-token", "newuser@gmail.com")
        } returns Result.success(AuthRepository.GoogleAuthResult.NewUser("newuser@gmail.com"))

        viewModel.loginWithGoogle(idToken = "id-token", email = "newuser@gmail.com")

        val state = viewModel.uiState.value
        assertEquals("newuser@gmail.com", state.pendingGoogleEmail)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `loginWithGoogle failure sets errorMessage`() = runTest {
        coEvery {
            authRepository.loginWithGoogle("id-token", "user@gmail.com")
        } returns Result.failure(Exception("Google Sign-In Cancelled"))

        viewModel.loginWithGoogle(idToken = "id-token", email = "user@gmail.com")

        val state = viewModel.uiState.value
        assertEquals("Google Sign-In Cancelled", state.errorMessage)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `clearPendingGoogleEmail resets pendingGoogleEmail to null`() {
        viewModel.clearPendingGoogleEmail()
        assertNull(viewModel.uiState.value.pendingGoogleEmail)
    }

    @Test
    fun `clearErrorMessage resets errorMessage to null`() = runTest {
        viewModel.login(email = "", password = "")
        assertTrue(viewModel.uiState.value.errorMessage != null)

        viewModel.clearErrorMessage()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `savedEmail delegates to authRepository getRememberedEmail`() {
        every { authRepository.getRememberedEmail() } returns "saved@example.com"
        assertEquals("saved@example.com", viewModel.savedEmail)
    }

    // ==========================================
    // LOGOUT & SESSION OBSERVATION TESTS
    // ==========================================

    @Test
    fun `logout calls authRepository logout`() = runTest {
        viewModel.logout()
        coVerify(exactly = 1) { authRepository.logout() }
    }

    @Test
    fun `observing session emits AUTHENTICATED when session is present`() = runTest {
        sessionFlow.value = sampleSession

        val state = viewModel.uiState.value
        assertEquals(AuthStatus.AUTHENTICATED, state.status)
        assertEquals(sampleUser, state.user)
        coVerify { deviceTokenManager.syncFcmToken() }
    }

    @Test
    fun `observing session emits UNAUTHENTICATED when session is cleared`() = runTest {
        sessionFlow.value = sampleSession
        assertEquals(AuthStatus.AUTHENTICATED, viewModel.uiState.value.status)

        // User logs out, session becomes null
        sessionFlow.value = null
        val state = viewModel.uiState.value
        assertEquals(AuthStatus.UNAUTHENTICATED, state.status)
        assertNull(state.user)
    }
}
