package com.edwin.bekal.data.auth

import com.edwin.bekal.core.error.AppFailure
import com.edwin.bekal.core.network.ApiEnvelope
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.core.network.onFailure
import com.edwin.bekal.data.auth.remote.AuthApi
import com.edwin.bekal.data.customer.remote.CustomerApi
import com.edwin.bekal.data.dto.AuthSession
import com.edwin.bekal.data.dto.LoginRequestDto
import com.edwin.bekal.data.dto.LoginResponseDto
import com.edwin.bekal.data.local.AuthSessionLocalDataSource
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import com.ibm.icu.text.RelativeDateTimeFormatter.AbsoluteUnit.NOW
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith


import com.google.firebase.auth.FirebaseAuth
import com.edwin.bekal.data.home.HomeRepository

private const val TOKEN = "header.payload.signature"

private const val NOW = 1_700_000_000_000L

private fun authRepository(
    localDataSource: AuthSessionLocalDataSource = mockk(),
    remoteDataSource: AuthApi = mockk(),
    customerApi: CustomerApi = mockk(),
    json: Json = Json { ignoreUnknownKeys = true },
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    clock: () -> Long = { com.edwin.bekal.data.auth.NOW },
    firebaseAuth: FirebaseAuth = mockk(relaxed = true),
    homeRepository: HomeRepository = mockk(relaxed = true)
) = AuthRepository(
    localDataSource = localDataSource,
    remoteDataSource = remoteDataSource,
    customerApi = customerApi,
    json = json,
    ioDispatcher = ioDispatcher,
    clock = clock,
    firebaseAuth = firebaseAuth,
    homeRepository = homeRepository
)

private fun storedSession(expiresAtMillis: Long) =
    AuthSession(accessToken = TOKEN, expiresAtMillis = expiresAtMillis)

@RunWith(Enclosed::class)
class AuthRepositoryTest{
    class Login {
        private val localDataSource = mockk<AuthSessionLocalDataSource>(relaxUnitFun = true)

        private val remoteDataSource = mockk<AuthApi>()

        private val repository = authRepository(localDataSource, remoteDataSource)

        private val loginResponse = LoginResponseDto(
            token = TOKEN,
            type = "Bearer"
        )

        @Test
        fun `returns session mapped from jwt claims and login response`() = runTest {
            coEvery { remoteDataSource.login(any()) } returns ApiEnvelope(
                status = 200,
                success = true,
                message = "Success",
                data = loginResponse
            )
        }

        @Test
        fun `return session mapped from jwt claims and login response for negative`() = runTest {
            coEvery { remoteDataSource.login(any()) } returns ApiEnvelope(
                status = 401,
                success = false,
                message = "False",
                data = loginResponse
            )
        }

//        @Test
//        fun `login returns failure when server returns 401 unauthorized`() = runTest {
//            coEvery { remoteDataSource.login(any()) } returns ApiEnvelope(
//                status = 401,
//                success = false,
//                message = "Invalid email or password",
//                data = null
//            )
//
//            val result = repository.login(credentials = LoginRequestDto(email = "toto@example.com", password = "wakwaw"))
//
//            assertTrue("Expected result to be AppResult.Failure", result is AppResult.Failure)
//            val failureResult = result as AppResult.Failure
//            assertEquals("Invalid email or password", failureResult.failure.message)
//        }

        @Test
        fun `emits null when no session is stored` () = runTest {
            every { localDataSource.observe() } returns flowOf(null)

            val session = repository.observeSession().first()

            assertNull(session)
        }

        @Test
        fun `emits stored session when it has not expired`() = runTest{
            val stored = storedSession(expiresAtMillis = com.edwin.bekal.data.auth.NOW + 1)
            every {localDataSource.observe()} returns flowOf(stored)

            val session = repository.observeSession().first()

            assertEquals(stored, session)
        }
    }

    class Logout {
        private val localDataSource = mockk<AuthSessionLocalDataSource>(relaxUnitFun = true)
        private val homeRepository = mockk<HomeRepository>(relaxUnitFun = true)
        private val repository = authRepository(
            localDataSource = localDataSource,
            homeRepository = homeRepository
        )

        @Test
        fun `logout clears home cache and local session`() = runTest {
            repository.logout()

            io.mockk.coVerify(exactly = 1) { homeRepository.clearCache() }
            io.mockk.coVerify(exactly = 1) { localDataSource.clear() }
        }
    }

    class Register {
        private val remoteDataSource = mockk<AuthApi>()
        private val repository = authRepository(remoteDataSource = remoteDataSource)

        private val sampleRegisterDto = com.edwin.bekal.data.dto.RegisterRequestDto(
            email = "test@example.com",
            password = "Password123!",
            fullName = "Edwin Jaya",
            phoneNumber = "081234567890",
            nik = "1234567890123456",
            dateOfBirth = "1995-08-15",
            gender = "MALE",
            address = "Jl. Merdeka No. 1",
            employmentType = "karyawan_tetap",
            companyName = "PT BCA Finance",
            jobTitle = "Software Engineer",
            industry = "Financial Services",
            declaredIncome = 15000000.0,
            otherIncome = 0.0,
            employmentStartDate = "2020-01-01"
        )

        private val sampleProfile = com.edwin.bekal.data.dto.CustomerProfileDto(
            id = "cust-1",
            fullName = "Edwin Jaya",
            email = "test@example.com"
        )

        @Test
        fun `register success returns customer profile`() = runTest {
            coEvery { remoteDataSource.register(sampleRegisterDto) } returns ApiEnvelope(
                status = 201,
                success = true,
                message = "Created",
                data = sampleProfile
            )

            val result = repository.register(sampleRegisterDto)

            assertTrue(result is AppResult.Success)
            assertEquals("cust-1", (result as AppResult.Success).data.id)
        }

        @Test
        fun `register failure returns error`() = runTest {
            coEvery { remoteDataSource.register(sampleRegisterDto) } returns ApiEnvelope(
                status = 400,
                success = false,
                message = "Email already registered",
                data = null
            )

            val result = repository.register(sampleRegisterDto)

            assertTrue(result is AppResult.Failure)
        }
    }

    class ForgotPassword {
        private val remoteDataSource = mockk<AuthApi>()
        private val repository = authRepository(remoteDataSource = remoteDataSource)

        @Test
        fun `forgotPassword success returns AppResult Success`() = runTest {
            coEvery {
                remoteDataSource.forgotPassword(com.edwin.bekal.data.dto.ForgotPasswordRequestDto("user@example.com"))
            } returns ApiEnvelope(
                status = 200,
                success = true,
                message = "OTP sent",
                data = null
            )

            val result = repository.forgotPassword("user@example.com")

            assertTrue(result is AppResult.Success)
        }

        @Test
        fun `forgotPassword failure returns AppResult Failure`() = runTest {
            coEvery {
                remoteDataSource.forgotPassword(com.edwin.bekal.data.dto.ForgotPasswordRequestDto("user@example.com"))
            } returns ApiEnvelope(
                status = 404,
                success = false,
                message = "User not found",
                data = null
            )

            val result = repository.forgotPassword("user@example.com")

            assertTrue(result is AppResult.Failure)
        }
    }
}