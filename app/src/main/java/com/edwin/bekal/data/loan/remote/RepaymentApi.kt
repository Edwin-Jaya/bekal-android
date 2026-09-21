package com.edwin.bekal.data.loan.remote

import com.edwin.bekal.utils.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Matches Spring Boot RepaymentRequest DTO
@Serializable
data class RepaymentRequest(
    @SerialName("loanId") val loanId: String,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("amountPaid") val amountPaid: BigDecimal,
    @SerialName("paymentMethod") val paymentMethod: String,
    @SerialName("transactionReference") val transactionReference: String
)

// Matches Spring Boot LoanBalanceResponse DTO
@Serializable
data class LoanBalanceResponse(
    @SerialName("loanId") val loanId: String,
    @SerialName("applicationNumber") val applicationNumber: String,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("totalRepayment") val totalRepayment: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("totalPaidSoFar") val totalPaidSoFar: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("remainingBalance") val remainingBalance: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("monthlyInstallment") val monthlyInstallment: BigDecimal,
    @SerialName("tenorMonths") val tenorMonths: Int,
    @SerialName("status") val status: String,
    @SerialName("isFullyPaid") val isFullyPaid: Boolean
)

// Matches Spring Boot PaymentHistoryResponse DTO
@Serializable
data class PaymentHistoryResponse(
    @SerialName("paymentId") val paymentId: String,
    @SerialName("loanId") val loanId: String,
    @SerialName("applicationNumber") val applicationNumber: String,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("amountPaid") val amountPaid: BigDecimal,
    @SerialName("paymentMethod") val paymentMethod: String,
    @SerialName("transactionReference") val transactionReference: String,
    @SerialName("paymentDate") val paymentDate: String,
    @SerialName("status") val status: String
)

interface RepaymentApi {
    @POST("api/v1/payments/repay")
    suspend fun submitRepayment(@Body request: RepaymentRequest): LoanBalanceResponse

    @GET("api/v1/payments/balance/{loanId}")
    suspend fun getLoanBalance(@Path("loanId") loanId: String): LoanBalanceResponse

    @GET("api/v1/payments/history/customer/{customerId}")
    suspend fun getPaymentHistory(@Path("customerId") customerId: String): List<PaymentHistoryResponse>
}