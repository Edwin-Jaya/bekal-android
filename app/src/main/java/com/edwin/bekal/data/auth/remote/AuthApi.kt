package com.edwin.bekal.data.auth.remote

import DocumentResponseDto
import com.edwin.bekal.core.network.ApiEnvelope
import com.edwin.bekal.data.dto.CreateBankAccountRequestDto
import com.edwin.bekal.data.dto.CustomerProfileDto
import com.edwin.bekal.data.dto.LoginRequestDto
import com.edwin.bekal.data.dto.LoginResponseDto
import com.edwin.bekal.data.dto.RegisterRequestDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {

    @POST("api/v1/customers/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): ApiEnvelope<LoginResponseDto>

    @POST("api/v1/customers/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): ApiEnvelope<CustomerProfileDto>

    @Multipart
    @POST("api/v1/document/upload")
    suspend fun uploadDocument(
        @Part("customerId") customerId: RequestBody,
        @Part("documentType") documentType: RequestBody,
        @Part fileUrl: MultipartBody.Part
    ): ApiEnvelope<DocumentResponseDto>

    @POST("api/v1/bank-accounts")
    suspend fun createBankAccount(
        @Body request: CreateBankAccountRequestDto
    ): ApiEnvelope<Unit>
}