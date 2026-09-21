package com.edwin.bekal.data.loan.remote

import com.edwin.bekal.data.dto.CreateLoanApplicationRequest
import com.edwin.bekal.data.dto.LoanApplicationResponse
import com.edwin.bekal.data.dto.LoanReviewDetail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.*

interface LoanApplicationApi {
    @POST("api/v1/loan-applications")
    suspend fun createLoanApplication(
        @Body request: CreateLoanApplicationRequest
    ): LoanApplicationResponse

    @GET("api/v1/loan-applications/customer/{customerId}")
    suspend fun getLoanApplicationHistory(
        @Path("customerId") customerId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): LoanApplicationHistoryResponse

    @GET("api/v1/loan-applications/{id}/detail")
    suspend fun getLoanApplicationDetail(
        @Path("id") id: String
    ): LoanReviewDetail

}



// Matches Spring's default Page<T> JSON shape (PageImpl serialization).
@Serializable
data class LoanApplicationHistoryResponse(
    val content: List<LoanApplicationResponse>,
    val totalElements: Long,
    val totalPages: Int,

    @SerialName("number")
    val currentPage: Int? = 0
)
