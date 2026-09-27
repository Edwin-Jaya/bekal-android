package com.edwin.bekal.core.security

/**
 * Result of root detection checks containing overall status and details of any detected threats.
 */
data class RootDetectionResult(
    val isRooted: Boolean,
    val detectedThreats: List<String> = emptyList()
)

/**
 * Interface defining device root detection capabilities.
 */
interface RootDetector {
    /**
     * Quickly checks whether the device is rooted or compromised.
     */
    fun isRooted(): Boolean

    /**
     * Performs a comprehensive check and returns detailed findings.
     */
    fun getRootDetectionResult(): RootDetectionResult
}
