package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoanReviewDetail(
    @SerialName("loanApplicationResponse")
    val loanApplicationResponse: LoanApplicationResponse? = null
)