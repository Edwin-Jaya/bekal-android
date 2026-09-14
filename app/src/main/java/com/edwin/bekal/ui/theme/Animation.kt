package com.edwin.bekal.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object BekalAnimation {
    // Durasi standar
    const val DURATION_FAST = 150
    const val DURATION_MEDIUM = 300

    // Tween Spec Standar
    val defaultTween = tween<Float>(
        durationMillis = DURATION_MEDIUM,
        easing = FastOutSlowInEasing
    )

    // Spring Spec untuk Efek Membal (Bounce)
    val bounceSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
}