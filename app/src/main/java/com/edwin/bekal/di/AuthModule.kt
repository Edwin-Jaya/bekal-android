package com.edwin.bekal.di

import android.content.Context
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.auth.remote.AuthApi
import com.edwin.bekal.data.customer.remote.CustomerApi
import com.edwin.bekal.data.local.AuthSessionLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideAuthSessionLocalDataSource(
        @ApplicationContext context: Context,
    ): AuthSessionLocalDataSource = AuthSessionLocalDataSource(context)

    @Provides
    @Singleton
    fun provideAuthRepository(
        localDataSource: AuthSessionLocalDataSource,
        authApi: AuthApi,
        customerApi: CustomerApi, // 1. Tambahkan parameter CustomerApi di sini
        json: Json,
    ): AuthRepository = AuthRepository(
        localDataSource = localDataSource,
        remoteDataSource = authApi,
        customerApi = customerApi, // 2. Oper ke konstruktor AuthRepository
        json = json,
    )
}