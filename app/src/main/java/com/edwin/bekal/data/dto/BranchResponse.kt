package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BranchResponse(
    val id: String,

    @SerialName("branchCode")
    val branchCode: String,

    @SerialName("branchName")
    val branchName: String,

    @SerialName("branchCity")
    val branchCity: String? = null
)