import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocumentResponseDto(
    @SerialName("id")
    val id: String,

    @SerialName("customer")
    val customer: CustomerSummaryDto? = null,

    @SerialName("documentType")
    val documentType: String,

    @SerialName("fileUrl")
    val fileUrl: String,

    @SerialName("fileHash")
    val fileHash: String? = null,

    @SerialName("status")
    val status: String,

    @SerialName("verifiedBy")
    val verifiedBy: InternalUserDto? = null,

    @SerialName("verifiedAt")
    val verifiedAt: String? = null,

    @SerialName("rejectionReason")
    val rejectionReason: String? = null,

    @SerialName("isLatest")
    val isLatest: Boolean,

    @SerialName("uploadedAt")
    val uploadedAt: String,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null
)

/**
 * DTO ringkas untuk relasi Customer pada response dokumen
 */
@Serializable
data class CustomerSummaryDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("fullName")
    val fullName: String? = null
)

/**
 * DTO ringkas untuk relasi InternalUser (admin/verifier)
 */
@Serializable
data class InternalUserDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("username")
    val username: String? = null,

    @SerialName("email")
    val email: String? = null
)