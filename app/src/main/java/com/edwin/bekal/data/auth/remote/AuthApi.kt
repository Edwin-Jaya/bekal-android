package com.edwin.bekal.data.auth.remote

import DocumentResponseDto
import com.edwin.bekal.core.network.ApiEnvelope
import com.edwin.bekal.data.dto.CreateBankAccountRequestDto
import com.edwin.bekal.data.dto.CustomerProfileDto
import com.edwin.bekal.data.dto.ForgotPasswordRequestDto
import com.edwin.bekal.data.dto.GoogleLoginRequestDto
import com.edwin.bekal.data.dto.JwtResponse
import com.edwin.bekal.data.dto.LoginRequestDto
import com.edwin.bekal.data.dto.LoginResponseDto
import com.edwin.bekal.data.dto.RefreshTokenRequest
import com.edwin.bekal.data.dto.RegisterRequestDto
import com.edwin.bekal.data.dto.ResetPasswordRequestDto
import com.edwin.bekal.data.dto.UserCheckResponse
import com.edwin.bekal.data.dto.VerifyOtpRequestDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    @POST("api/v1/customers/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): ApiEnvelope<LoginResponseDto>

    @POST("api/v1/customers/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): ApiEnvelope<CustomerProfileDto>

    // Dipanggil kalau ada step SETELAH register() yang gagal (upload dokumen /
    // create bank account) — bersihkan customer + dokumen/employment yang
    // sudah sempat ke-insert, supaya user bisa retry registrasi dari awal
    // tanpa kena error "email/NIK sudah terdaftar".
    @DELETE("api/v1/customers/{customerId}/rollback")
    suspend fun rollbackRegistration(
        @Path("customerId") customerId: String
    ): Response<Unit>

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

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): ApiEnvelope<Unit>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequestDto): ApiEnvelope<Unit>

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): ApiEnvelope<Unit>

    @GET("api/v1/customers/check")
    suspend fun checkUserByEmail(
        @Query("email") email: String
    ): Response<UserCheckResponse>

    @POST("api/v1/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequestDto
    ): ApiEnvelope<LoginResponseDto>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): ApiEnvelope<JwtResponse>
}