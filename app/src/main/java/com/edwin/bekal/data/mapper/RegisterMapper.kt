package com.edwin.bekal.data.mapper

import com.edwin.bekal.data.dto.RegisterRequestDto
import com.edwin.bekal.presentation.auth.register.RegisterUiState

/**
 * Extension function untuk mengonversi State UI Registrasi
 * menjadi DTO Request Backend (`POST /api/v1/customers/register`).
 */
fun RegisterUiState.toRegisterRequest(): RegisterRequestDto {
    return RegisterRequestDto(
        email = this.email.trim(),
        password = this.password,
        fullName = this.fullName.trim(),
        phoneNumber = this.phoneNumber.trim(),
        nik = this.nik.trim(),
        dateOfBirth = this.dateOfBirth.trim(),
        gender = mapGenderToBackend(this.gender),
        address = this.address.trim(),
        employmentType = mapEmploymentTypeToBackend(this.jobType),
        companyName = this.companyName.trim().ifEmpty { null },
        jobTitle = this.position.trim().ifEmpty { null },
        industry = this.industry.trim().ifEmpty { null },
        declaredIncome = this.monthlyIncome.replace("[^0-9]".toRegex(), "").toDoubleOrNull(),
        otherIncome = this.otherIncome.replace("[^0-9]".toRegex(), "").toDoubleOrNull(),
        employmentStartDate = this.employmentStartDate.trim().ifEmpty { null }
    )
}

/**
 * Konversi pilihan gender dari UI ke format Enum String Backend ("MALE" / "FEMALE").
 */
private fun mapGenderToBackend(uiGender: String): String {
    return when (uiGender.lowercase()) {
        "laki-laki", "pria", "male" -> "MALE"
        "perempuan", "wanita", "female" -> "FEMALE"
        else -> "MALE"
    }
}

/**
 * Konversi tipe pekerjaan dari UI ke format Enum String Backend.
 */
private fun mapEmploymentTypeToBackend(uiJobType: String): String {
    return when (uiJobType.lowercase()) {
        "tetap", "permanent" -> "PERMANENT"
        "kontrak", "contract" -> "CONTRACT"
        "freelance", "profesional" -> "FREELANCE"
        else -> "PERMANENT"
    }
}