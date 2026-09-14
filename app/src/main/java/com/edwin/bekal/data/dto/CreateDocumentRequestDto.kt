package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateDocumentRequestDto(
    @SerialName("customerId")
    val customerId: String,

    @SerialName("documentType")
    val documentType: String, // "KTP", "SLIP_GAJI"

    @SerialName("fileUrl")
    val fileUrl: String,

    @SerialName("fileHash")
    val fileHash: String? = null
)