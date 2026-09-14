package com.edwin.bekal.core.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.edwin.bekal.BuildConfig
import com.edwin.bekal.data.auth.local.SessionAuthTokenProvider
import com.edwin.bekal.data.auth.remote.AuthApi
import com.edwin.bekal.data.customer.remote.CustomerApi
import com.edwin.bekal.data.loan.remote.BranchApi
import com.edwin.bekal.data.loan.remote.LoanApplicationApi
import com.edwin.bekal.data.loan.remote.PlafondApi
import com.edwin.bekal.data.local.AuthSessionLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 30L
private const val HEADER_AUTHORIZATION = "Authorization"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideAuthTokenProvider(
        localDataSource: AuthSessionLocalDataSource,
    ): AuthTokenProvider = SessionAuthTokenProvider(localDataSource)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        tokenProvider: AuthTokenProvider,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(AuthHeaderInterceptor(tokenProvider))
        // Di build release, artifact chucker-no-op membuat interceptor ini tidak melakukan apa pun.
        .addInterceptor(
            ChuckerInterceptor.Builder(context)
                .redactHeaders(HEADER_AUTHORIZATION)
                .alwaysReadResponseBody(true)
                .build()
        )
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideCustomerApi(retrofit: Retrofit): CustomerApi {
        return retrofit.create(CustomerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideLoanApplicationApi(retrofit: Retrofit): LoanApplicationApi {
        return retrofit.create(LoanApplicationApi::class.java)
    }

    @Provides
    @Singleton
    fun providePlafondApi(retrofit: Retrofit): PlafondApi {
        return retrofit.create(PlafondApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBranchApi(retrofit: Retrofit): BranchApi {
        return retrofit.create(BranchApi::class.java)
    }
}