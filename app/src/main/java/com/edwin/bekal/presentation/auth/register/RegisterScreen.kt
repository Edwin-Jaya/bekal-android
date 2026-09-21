package com.edwin.bekal.presentation.auth.register

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edwin.bekal.presentation.auth.register.steps.StepAccountContent
import com.edwin.bekal.presentation.auth.register.steps.StepFinancialContent
import com.edwin.bekal.presentation.auth.register.steps.StepIdentityContent
import com.edwin.bekal.presentation.auth.register.steps.StepJobContent
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing

@Composable
fun RegisterScreen(
    onNavigateBackToLogin: () -> Unit,
    prefillEmail: String = "",
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val extendedColors = BekalTheme.extendedColors
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBackToLogin()
        }
    }
    LaunchedEffect(prefillEmail) {
        if (prefillEmail.isNotEmpty()) {
            viewModel.prefillGoogleEmail(prefillEmail)
        }
    }

    val stepTitles = listOf(
        "Kredensial & Kontak",
        "Identitas e-KTP",
        "Pekerjaan & Penghasilan",
        "Dokumen & Rekening"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(extendedColors.canvasBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        containerColor = extendedColors.canvasBackground,
        topBar = {
            RegisterHeaderNavbar(
                currentStep = uiState.currentStep,
                totalSteps = 4,
                stepTitle = stepTitles.getOrElse(uiState.currentStep - 1) { "" },
                onBackClick = {
                    if (uiState.currentStep == 1) onNavigateBackToLogin()
                    else viewModel.onPreviousStep()
                }
            )
        },
        bottomBar = {
            RegisterBottomBar(
                currentStep = uiState.currentStep,
                isLoading = uiState.isLoading,
                onBackClicked = {
                    if (uiState.currentStep == 1) onNavigateBackToLogin()
                    else viewModel.onPreviousStep()
                },
                onNextClicked = { viewModel.onNextStep(context) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "RegisterStepAnimation"
            ) { step ->
                when (step) {
                    1 -> StepAccountContent(uiState = uiState, viewModel = viewModel)
                    2 -> StepIdentityContent(uiState = uiState, viewModel = viewModel)
                    3 -> StepJobContent(uiState = uiState, viewModel = viewModel)
                    4 -> StepFinancialContent(uiState = uiState, viewModel = viewModel)
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.medium)
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.xl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = extendedColors.electricViolet,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text(
                                text = uiState.loadingMessage,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = extendedColors.deepCharcoal
                            )
                        }
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearErrorMessage() },
                    title = { Text("Terjadi Kesalahan", fontWeight = FontWeight.Bold) },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearErrorMessage() }) {
                            Text("OK", color = extendedColors.electricViolet, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RegisterHeaderNavbar(
    currentStep: Int,
    totalSteps: Int,
    stepTitle: String,
    onBackClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors
    val progressAnimated by animateFloatAsState(
        targetValue = currentStep.toFloat() / totalSteps.toFloat(),
        label = "ProgressAnimation"
    )

    Surface(
        color = Color.White,
        shadowElevation = Elevation.none,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = extendedColors.deepCharcoal
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.xs))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Langkah $currentStep dari $totalSteps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.electricViolet
                        )
                        Text(
                            text = "${(progressAnimated * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extendedColors.textMuted
                        )
                    }
                    Text(
                        text = stepTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.deepCharcoal
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.md))
            }

            LinearProgressIndicator(
                progress = { progressAnimated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = extendedColors.electricViolet,
                trackColor = extendedColors.accentSoft,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun RegisterBottomBar(
    currentStep: Int,
    isLoading: Boolean,
    onBackClicked: () -> Unit,
    onNextClicked: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Surface(
        color = Color.White,
        shadowElevation = Elevation.low,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBackClicked,
                enabled = !isLoading,
                shape = CircleShape,
                modifier = Modifier
                    .height(48.dp)
                    .weight(0.35f)
            ) {
                Text("Kembali", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = extendedColors.deepCharcoal)
            }

            Button(
                onClick = onNextClicked,
                enabled = !isLoading,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = extendedColors.electricViolet,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .height(48.dp)
                    .weight(0.65f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val label = when (currentStep) {
                        1 -> "Lanjut ke KTP"
                        2 -> "Lanjut ke Pekerjaan"
                        3 -> "Lanjut ke Dokumen"
                        else -> "Selesaikan Pendaftaran"
                    }
                    Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Icon(
                        imageVector = if (currentStep == 4) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}