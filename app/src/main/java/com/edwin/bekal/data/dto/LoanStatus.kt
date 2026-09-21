package com.edwin.bekal.data.dto

enum class LoanStatus(val rawValue: String) {
    IN_REVIEW("in_review"),
    REVIEW_REJECTED("review_rejected"),
    IN_APPROVAL("in_approval"),
    APPROVAL_REJECTED("approval_rejected"),
    IN_DISBURSEMENT("in_disbursement"),
    DISBURSED("disbursed"),
    CLOSED("closed"),
    CANCELLED("cancelled");

    companion object {
        fun fromValue(value: String?): LoanStatus {
            return entries.find { it.rawValue.equals(value, ignoreCase = true) } ?: IN_REVIEW
        }
    }
}