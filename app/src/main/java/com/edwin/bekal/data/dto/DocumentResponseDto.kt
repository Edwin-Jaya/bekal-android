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

    @SerialName("isLatest")
    val isLatest: Boolean,

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