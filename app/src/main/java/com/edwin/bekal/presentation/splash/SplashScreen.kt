package com.edwin.bekal.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.edwin.bekal.ui.theme.BekalAnimation
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Spacing
import com.edwin.bekal.ui.theme.Typography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val colors = BekalTheme.extendedColors

    val fullTitle = "BEKAL"
    var visibleTitleText by remember { mutableStateOf("") }

    // State untuk animasi subtitle
    val subtitleAlpha = remember { Animatable(0f) }
    val subtitleOffsetY = remember { Animatable(16f) }

    LaunchedEffect(Unit) {
        // 1. Efek menulis teks "BEKAL" huruf demi huruf
        for (i in 1..fullTitle.length) {
            visibleTitleText = fullTitle.substring(0, i)
            delay(120) // Kecepatan munculnya setiap huruf
        }

        delay(150)

        // 2. Subtitle muncul dengan efek Fade-in & Slide Up
        subtitleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
        subtitleOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = BekalAnimation.bounceSpring
        )

        // Tahan sebentar sebelum navigasi berpindah
        delay(1500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvasBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = Spacing.xxl)
        ) {
            // Title Teks BEKAL (Display Large ExtraBold)
            Text(
                text = visibleTitleText,
                style = Typography.displayLarge,
                color = colors.deepCharcoal
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Subtitle Tagline (Body Large Electric Violet)
            Text(
                text = "setiap langkah, ada bekal",
                style = Typography.bodyLarge,
                color = colors.electricViolet,
                modifier = Modifier
                    .offset(y = subtitleOffsetY.value.dp)
                    .alpha(subtitleAlpha.value)
            )
        }
    }
}