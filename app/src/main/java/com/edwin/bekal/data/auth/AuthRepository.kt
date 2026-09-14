package com.edwin.bekal.data.auth

import android.content.Context
import android.net.Uri
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.core.network.requirePayload
import com.edwin.bekal.core.network.runApiCatching
import com.edwin.bekal.data.auth.remote.AuthApi
import com.edwin.bekal.data.customer.remote.CustomerApi
import com.edwin.bekal.data.dto.AuthSession
import com.edwin.bekal.data.dto.AuthUser
import com.edwin.bekal.data.dto.CreateBankAccountRequestDto
import com.edwin.bekal.data.dto.CustomerProfileDto
import com.edwin.bekal.data.dto.LoginRequestDto
import com.edwin.bekal.data.dto.RegisterRequestDto
import com.edwin.bekal.data.local.AuthSessionLocalDataSource
import com.edwin.bekal.utils.toMultipartBodyPart
import com.edwin.bekal.utils.toTextRequestBody
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    suspend fun logout() {
        withContext(ioDispatcher) {
            localDataSource.clear()
        }
    }

    fun observeSession(): Flow<AuthSession?> = localDataSource.observe()
        .map { session -> session?.takeUnless { it.isExpiredAt(clock()) } }

    companion object {
        private val FALLBACK_SESSION_LIFETIME_MILLIS = TimeUnit.HOURS.toMillis(1)
    }
}