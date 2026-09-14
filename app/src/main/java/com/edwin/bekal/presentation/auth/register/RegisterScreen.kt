package com.edwin.bekal.presentation.auth.register

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edwin.bekal.presentation.auth.register.steps.*

@Composable
fun RegisterScreen(
    onNavigateBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Navigasi jika registrasi berhasil
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBackToLogin()
        }
    }

    Scaffold(
        topBar = {
            // Reusable Top Header
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
                label = "RegisterStepAnimation"
            ) { step ->
                when (step) {
                    1 -> StepAccountContent(uiState = uiState, viewModel = viewModel)
                    2 -> StepIdentityContent(uiState = uiState, viewModel = viewModel)
                    3 -> StepJobContent(uiState = uiState, viewModel = viewModel)
                    4 -> StepFinancialContent(uiState = uiState, viewModel = viewModel)
                }
            }

            // Overlay Loading Dialog
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = uiState.loadingMessage)
                        }
                    }
                }
            }

            // Error Dialog
            uiState.errorMessage?.let { error ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearErrorMessage() },
                    title = { Text("Terjadi Kesalahan") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearErrorMessage() }) {
                            Text("OK")
                        }
                    }
                )
            }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onBackClicked,
            enabled = !isLoading
        ) {
            Text("<")
        }
        Button(
            onClick = onNextClicked,
            enabled = !isLoading
        ) {
            val label = when (currentStep) {
                1 -> "Lanjut ke KTP →"
                2 -> "Lanjut ke Profesi →"
                3 -> "Lanjut ke Finansial →"
                else -> "Kirim →"
            }
            Text(label)
        }
    }
}