package com.edwin.bekal.data.auth.remote

import com.edwin.bekal.data.dto.RefreshTokenRequest
import com.edwin.bekal.data.local.AuthSessionLocalDataSource
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val localDataSource: AuthSessionLocalDataSource,
    private val refreshTokenApiProvider: Provider<AuthApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Mencegah infinite loop jika request yang sama sudah pernah dicoba ulang
        if (responseCount(response) >= 2) {
            return null
        }

        val refreshToken = runBlocking { localDataSource.getRefreshToken() }
        if (refreshToken.isNullOrEmpty()) {
            runBlocking { localDataSource.clear() }
            return null
        }

        // Jalankan suspend function dari AuthApi menggunakan runBlocking & try-catch
        val envelope = try {
            runBlocking {
                refreshTokenApiProvider.get().refreshToken(RefreshTokenRequest(refreshToken))
            }
        } catch (e: Exception) {
            null
        }

        val body = envelope?.data
        val newAccessToken = body?.token
        val newRefreshToken = body?.refreshToken ?: refreshToken

        if (!newAccessToken.isNullOrEmpty()) {
            runBlocking {
                localDataSource.updateTokens(newAccessToken, newRefreshToken)
            }

            // Retry original request dengan access token baru
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }

        // Jika refresh token sudah tidak valid / expired / API melempar error
        runBlocking { localDataSource.clear() }
        return null
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}