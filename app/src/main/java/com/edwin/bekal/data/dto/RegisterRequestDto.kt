package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    // Personal Details (Customer Table)
    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String,

    @SerialName("fullName")
    val fullName: String,

    @SerialName("phoneNumber")
    val phoneNumber: String,

    @SerialName("nik")
    val nik: String,

    @SerialName("dateOfBirth")
    val dateOfBirth: String, // Format: YYYY-MM-DD

    @SerialName("gender")
    val gender: String, // "MALE" or "FEMALE"

    @SerialName("address")
    val address: String,

    // Employment Details (CustomerEmployment Table)
    @SerialName("employmentType")
    val employmentType: String? = null,

    @SerialName("companyName")
    val companyName: String? = null,

    @SerialName("jobTitle")
    val jobTitle: String? = null,

    @SerialName("industry")
    val industry: String? = null,

    @SerialName("declaredIncome")
    val declaredIncome: Double? = null,

    @SerialName("otherIncome")
    val otherIncome: Double? = null,

    @SerialName("employmentStartDate")
    val employmentStartDate: String? = null // Format: YYYY-MM-DD
)