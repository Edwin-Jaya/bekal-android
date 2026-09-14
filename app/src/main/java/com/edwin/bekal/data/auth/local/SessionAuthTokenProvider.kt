package com.edwin.bekal.data.auth.local

import com.edwin.bekal.core.network.AuthTokenProvider
import com.edwin.bekal.data.local.AuthSessionLocalDataSource
import kotlinx.coroutines.flow.first

class SessionAuthTokenProvider(
    private val localDataSource: AuthSessionLocalDataSource,
) : AuthTokenProvider {

    override suspend fun currentToken(): String? =
        localDataSource.observe().first()?.accessToken
}