package com.edwin.bekal.presentation.auth.resetpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing

@Composable
fun ResetPasswordScreen(
    onBackClick: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val extendedColors = BekalTheme.extendedColors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onResetSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extendedColors.canvasBackground)
            .statusBarsPadding()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar Navigation
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(extendedColors.surfaceCard)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = extendedColors.deepCharcoal
            )
        }

        Spacer(Modifier.height(Spacing.lg))

        // Header Section (Bento Style Icon + Title)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(Radius.lg))
                    .background(extendedColors.deepCharcoal.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = extendedColors.deepCharcoal,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "Atur Ulang Sandi",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.deepCharcoal
                )
                Text(
                    text = "Kode OTP dikirim ke ${uiState.email}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.textMuted
                )
            }
        }

        Spacer(Modifier.height(Spacing.xl))

        // Bento Card Container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = extendedColors.deepCharcoal.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(Radius.xl)
                ),
            shape = RoundedCornerShape(Radius.xl),
            color = extendedColors.surfaceCard,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg)
            ) {
                OutlinedTextField(
                    value = uiState.otp,
                    onValueChange = viewModel::onOtpChange,
                    label = { Text("Kode OTP (6 digit)") },
                    enabled = !uiState.isLoading,
                    isError = uiState.otpError != null,
                    supportingText = { uiState.otpError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(Radius.lg),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Spacing.xs))

                OutlinedTextField(
                    value = uiState.newPassword,
                    onValueChange = viewModel::onNewPasswordChange,
                    label = { Text("Kata Sandi Baru") },
                    enabled = !uiState.isLoading,
                    isError = uiState.newPasswordError != null,
                    supportingText = { uiState.newPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val icon = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(imageVector = icon, contentDescription = null, tint = extendedColors.textMuted)
                        }
                    },
                    shape = RoundedCornerShape(Radius.lg),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Spacing.xs))

                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = { Text("Konfirmasi Kata Sandi Baru") },
                    enabled = !uiState.isLoading,
                    isError = uiState.confirmPasswordError != null,
                    supportingText = { uiState.confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val icon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = icon, contentDescription = null, tint = extendedColors.textMuted)
                        }
                    },
                    shape = RoundedCornerShape(Radius.lg),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Spacing.md))

                Button(
                    onClick = { viewModel.submit() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.deepCharcoal,
                        contentColor = Color.White
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Ubah Kata Sandi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text("Gagal Mengubah Kata Sandi") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) { Text("OK") }
            }
        )
    }
}