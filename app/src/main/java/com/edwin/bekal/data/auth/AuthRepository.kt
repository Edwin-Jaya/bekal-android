package com.edwin.bekal.data.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import com.edwin.bekal.core.error.CommonFailure
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.core.network.requirePayload
import com.edwin.bekal.core.network.requireSuccess
import com.edwin.bekal.core.network.runApiCatching
import com.edwin.bekal.data.auth.remote.AuthApi
import com.edwin.bekal.data.customer.remote.CustomerApi
import com.edwin.bekal.data.dto.AuthSession
import com.edwin.bekal.data.dto.AuthUser
import com.edwin.bekal.data.dto.CreateBankAccountRequestDto
import com.edwin.bekal.data.dto.CustomerProfileDto
import com.edwin.bekal.data.dto.ForgotPasswordRequestDto
import com.edwin.bekal.data.dto.GoogleLoginRequestDto
import com.edwin.bekal.data.dto.LoginRequestDto
import com.edwin.bekal.data.dto.RegisterRequestDto
import com.edwin.bekal.data.dto.ResetPasswordRequestDto
import com.edwin.bekal.data.dto.VerifyOtpRequestDto
import com.edwin.bekal.data.home.HomeRepository
import com.edwin.bekal.data.local.AuthSessionLocalDataSource
import com.edwin.bekal.utils.toMultipartBodyPart
import com.edwin.bekal.utils.toTextRequestBody
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class AuthRepository internal constructor(
    private val localDataSource: AuthSessionLocalDataSource,
    private val remoteDataSource: AuthApi,
    private val customerApi: CustomerApi,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis,
    private val firebaseAuth: FirebaseAuth,
    private val homeRepository: HomeRepository
) {

    suspend fun register(request: RegisterRequestDto): AppResult<CustomerProfileDto> {
        return withContext(ioDispatcher) {
            runApiCatching(json) {
                val response = remoteDataSource.register(request)
                when (val result = response.requirePayload()) {
                    is AppResult.Success -> AppResult.success(result.data)
                    is AppResult.Failure -> result
                }
            }
        }
    }

    sealed class GoogleAuthResult {
        object ExistingUser : GoogleAuthResult()
        data class NewUser(val email: String) : GoogleAuthResult()
    }

    // Di AuthRepository:
    suspend fun loginWithGoogle(idToken: String, email: String): Result<GoogleAuthResult> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()

            val response = remoteDataSource.checkUserByEmail(email)

            if (!response.isSuccessful) {
                firebaseAuth.signOut()
                return Result.failure(Exception("Gagal memeriksa akun, periksa koneksi Anda"))
            }

            if (response.body()?.exists == true) {
                val firebaseToken = firebaseAuth.currentUser
                    ?.getIdToken(false)
                    ?.await()
                    ?.token
                    ?: run {
                        firebaseAuth.signOut()
                        return Result.failure(Exception("Gagal mendapatkan Firebase token"))
                    }

                val loginEnvelope = remoteDataSource.loginWithGoogle(
                    GoogleLoginRequestDto(idToken = firebaseToken)
                )
                val loginData = when (val result = loginEnvelope.requirePayload()) {
                    is AppResult.Success -> result.data
                    is AppResult.Failure -> return Result.failure(Exception(result.failure.toString()))
                }

                val tempSession = AuthSession(accessToken = loginData.token)
                localDataSource.save(tempSession)

                val profileData = runCatching { customerApi.getProfile() }
                    .getOrNull()
                    ?.requirePayload()
                    ?.let { result -> if (result is AppResult.Success) result.data else null }

                val finalSession = AuthSession(
                    user = AuthUser(
                        id = profileData?.id.orEmpty(),
                        name = profileData?.fullName ?: email.substringBefore("@"),
                        email = profileData?.email ?: email,
                        tipe = loginData.type,
                        roles = listOf("CUSTOMER")
                    ),
                    accessToken = loginData.token,
                    expiresAtMillis = clock() + FALLBACK_SESSION_LIFETIME_MILLIS
                )
                localDataSource.save(finalSession)

                Result.success(GoogleAuthResult.ExistingUser)
            } else {
                Result.success(GoogleAuthResult.NewUser(email))
            }
        } catch (e: Exception) {
            firebaseAuth.signOut()
            Result.failure(e)
        }
    }

    /**
     * Bersihkan customer + dokumen/employment yang sudah sempat ke-insert
     * kalau salah satu step registrasi SETELAH register() gagal (upload
     * dokumen / create bank account). Dipanggil dari RegisterViewModel.
     * Kegagalan rollback ini sendiri tidak dilempar ke pemanggil — cukup
     * di-log, supaya tidak menutupi pesan error asli yang ditampilkan ke user.
     */
    suspend fun rollbackRegistration(customerId: String) {
        withContext(ioDispatcher) {
            runCatching { remoteDataSource.rollbackRegistration(customerId) }
                .onFailure { e ->
                    Log.e("AuthRepository", "Rollback registrasi gagal untuk customerId=$customerId", e)
                }
        }
    }

    suspend fun uploadDocument(
        context: Context,
        customerId: String,
        documentType: String,
        uri: Uri
    ): AppResult<Unit> {
        return withContext(ioDispatcher) {
            runApiCatching(json) {
                val filePart = uri.toMultipartBodyPart(context, "file")
                    ?: error("Gagal mengolah file dokumen")

                val response = remoteDataSource.uploadDocument(
                    fileUrl = filePart,
                    customerId = customerId.toTextRequestBody(),
                    documentType = documentType.toTextRequestBody()
                )
                when (val result = response.requirePayload()) {
                    is AppResult.Success -> AppResult.success(Unit)
                    is AppResult.Failure -> result
                }
            }
        }
    }

    suspend fun createBankAccount(request: CreateBankAccountRequestDto): AppResult<Unit> {
        return withContext(ioDispatcher) {
            runApiCatching(json) {
                val response = remoteDataSource.createBankAccount(request)
                when (val result = response.requirePayload()) {
                    is AppResult.Success -> AppResult.success(Unit)
                    is AppResult.Failure -> result
                }
            }
        }
    }

    suspend fun login(credentials: LoginRequestDto): AppResult<AuthSession> {
        return withContext(ioDispatcher) {
            runApiCatching(json) {
                val loginEnvelope = remoteDataSource.login(credentials)
                val loginData = when (val result = loginEnvelope.requirePayload()) {
                    is AppResult.Success -> result.data
                    is AppResult.Failure -> return@runApiCatching result
                }

                val tempSession = AuthSession(accessToken = loginData.token)
                localDataSource.save(tempSession)

                val profileData = runCatching { customerApi.getProfile() }
                    .getOrNull()
                    ?.requirePayload()
                    ?.let { result -> if (result is AppResult.Success) result.data else null }

                val finalSession = AuthSession(
                    user = AuthUser(
                        id = profileData?.id.orEmpty(),
                        name = profileData?.fullName ?: credentials.email.substringBefore("@"),
                        email = profileData?.email ?: credentials.email,
                        tipe = loginData.type,
                        roles = listOf("CUSTOMER")
                    ),
                    accessToken = loginData.token,
                    expiresAtMillis = clock() + FALLBACK_SESSION_LIFETIME_MILLIS
                )

                localDataSource.save(finalSession)
                AppResult.success(finalSession)
            }
        }
    }

    // --- Manajemen Remember Me Email ---

    fun saveRememberedEmail(email: String) {
        localDataSource.saveRememberedEmail(email)
    }

    fun getRememberedEmail(): String? {
        return localDataSource.getRememberedEmail()
    }

    fun clearRememberedEmail() {
        localDataSource.clearRememberedEmail()
    }

    // ------------------------------------

    suspend fun forgotPassword(email: String): AppResult<Unit> {
        return withContext(ioDispatcher) {
            runApiCatching(json) {
                remoteDataSource.forgotPassword(ForgotPasswordRequestDto(email)).requireSuccess()
            }
        }
    }

    suspend fun verifyOtp(email: String, otp: String): AppResult<Unit> {
        return withContext(ioDispatcher) {
            runApiCatching(json) {
                remoteDataSource.verifyOtp(VerifyOtpRequestDto(email, otp)).requireSuccess()
            }
        }
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): AppResult<Unit> {
        return withContext(ioDispatcher) {
            runApiCatching(json) {
                remoteDataSource.resetPassword(ResetPasswordRequestDto(email, otp, newPassword)).requireSuccess()
            }
        }
    }

    suspend fun refreshProfile(): AppResult<Unit> {
        return withContext(ioDispatcher) {
            runApiCatching(json) {
                val current = localDataSource.observe().first() ?: return@runApiCatching AppResult.failure(
                    CommonFailure.Unexpected(IllegalStateException("Tidak ada sesion aktif"))
                )

                val profileResult = customerApi.getProfile()
                val profileData = when (val result = profileResult.requirePayload()) {
                    is AppResult.Success -> result.data
                    is AppResult.Failure -> return@runApiCatching result
                }

                val updatedSession = current.copy(
                    user = current.user?.copy(
                        name = profileData.fullName,
                        email = profileData.email
                    )
                )

                localDataSource.save(updatedSession)
                AppResult.success(Unit)
            }
        }
    }

    suspend fun updateLocalSession(profile: CustomerProfileDto) {
        withContext(ioDispatcher) {
            val current = localDataSource.observe().first() ?: return@withContext

            val updatedSession = current.copy(
                user = current.user?.copy(
                    name = profile.fullName,
                    email = profile.email
                )
            )

            localDataSource.save(updatedSession)
        }
    }

    suspend fun logout() {
        withContext(ioDispatcher) {
            homeRepository.clearCache()
            localDataSource.clear()
        }
    }

    fun observeSession(): Flow<AuthSession?> = localDataSource.observe()
        .map { session -> session?.takeUnless { it.isExpiredAt(clock()) } }

    companion object {
        private val FALLBACK_SESSION_LIFETIME_MILLIS = TimeUnit.HOURS.toMillis(1)
    }
}