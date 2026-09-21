package com.edwin.bekal.presentation.auth.forgotpassword

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onOtpSent: (email: String) -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val extendedColors = BekalTheme.extendedColors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        onBackClick()
    }

    LaunchedEffect(uiState.isOtpSent) {
        if (uiState.isOtpSent) onOtpSent(uiState.email)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extendedColors.canvasBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Header Simpel & Presisi
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
            Text(
                text = "Lupa Kata Sandi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal,
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
                border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Text(
                        text = "Atur Ulang Kata Sandi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.deepCharcoal
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Text(
                        text = "Masukkan email terdaftar Anda. Kami akan mengirimkan 6-digit kode OTP verifikasi.",
                        fontSize = 13.sp,
                        color = extendedColors.textMuted,
                        modifier = Modifier.padding(horizontal = Spacing.xs)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
                border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        text = "Email Akun",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        enabled = !uiState.isLoading,
                        isError = uiState.emailError != null,
                        placeholder = {
                            Text(
                                text = "nama@email.com",
                                color = extendedColors.textMuted.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        supportingText = {
                            uiState.emailError?.let {
                                Text(it, color = extendedColors.dangerMain, fontSize = 11.sp)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.2f),
                            focusedBorderColor = extendedColors.electricViolet,
                            unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.3f),
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.sendOtp() },
                        enabled = !uiState.isLoading && uiState.email.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extendedColors.deepCharcoal,
                            contentColor = Color.White,
                            disabledContainerColor = extendedColors.textMuted.copy(alpha = 0.3f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Kirim Kode OTP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text("Gagal Mengirim Kode", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                    Text("OK", color = extendedColors.electricViolet, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}