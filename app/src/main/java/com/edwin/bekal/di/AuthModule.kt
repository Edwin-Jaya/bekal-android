package com.edwin.bekal.di

import android.content.Context
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.auth.remote.AuthApi
import com.edwin.bekal.data.customer.remote.CustomerApi
import com.edwin.bekal.data.home.HomeRepository
import com.edwin.bekal.data.local.AuthSessionLocalDataSource
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

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
        customerApi: CustomerApi,
        json: Json,
        firebaseAuth: FirebaseAuth, // 1. Tambahkan parameter FirebaseAuth di sini
        homeRepository: HomeRepository,
    ): AuthRepository = AuthRepository(
        localDataSource = localDataSource,
        remoteDataSource = authApi,
        customerApi = customerApi,
        json = json,
        firebaseAuth = firebaseAuth, // 2. Oper ke konstruktor AuthRepository
        homeRepository = homeRepository,
    )
}