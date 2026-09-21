package com.edwin.bekal.presentation.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwin.bekal.R
import com.edwin.bekal.presentation.shared.sharedActivityViewModel
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing

@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = sharedActivityViewModel(),
    onForgotPasswordClick: () -> Unit = {},
    onRegisterClick: (prefillEmail: String) -> Unit = {},
    onGoogleLoginClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    BackHandler {
        onBackClick()
    }

    LaunchedEffect(Unit) {
        viewModel.savedEmail?.let { saved ->
            if (saved.isNotBlank()) {
                email = saved
                rememberMe = true
            }
        }
    }

    LaunchedEffect(uiState.status) {
        if (uiState.status == AuthStatus.AUTHENTICATED) {
            onLoginSuccess()
        }
    }

    // ✅ Fix: LaunchedEffect ditutup dengan benar
    LaunchedEffect(uiState.pendingGoogleEmail) {
        uiState.pendingGoogleEmail?.let { googleEmail ->
            viewModel.clearPendingGoogleEmail()
            onRegisterClick(googleEmail)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.canvasBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Navigation Top Bar
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
        }

        // Scrollable Bento Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Bento Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.xl),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = extendedColors.accentSoft,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(id = R.drawable.bekal_logo),
                                        contentDescription = "bekal_logo",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Text(
                                text = "BEKAL",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.deepCharcoal
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        Text(
                            text = "Selamat Datang!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = extendedColors.deepCharcoal
                        )
                        Spacer(modifier = Modifier.height(Spacing.xxs))
                        Text(
                            text = "Masukkan informasi akun Anda untuk masuk",
                            fontSize = 13.sp,
                            color = extendedColors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Bento Form Card Utama
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.xl),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Tombol Google Sign-In
                        OutlinedButton(
                            onClick = onGoogleLoginClick,
                            enabled = !uiState.isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = CircleShape,
                            border = BorderStroke(
                                1.dp,
                                extendedColors.textMuted.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = extendedColors.electricViolet
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Text(
                                    text = "Masuk dengan Google",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = extendedColors.deepCharcoal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        // Pembatas "Atau"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = extendedColors.textMuted.copy(alpha = 0.15f)
                            )
                            Text(
                                text = "Atau",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = extendedColors.textMuted,
                                modifier = Modifier.padding(horizontal = Spacing.md)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = extendedColors.textMuted.copy(alpha = 0.15f)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        // Input Email
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Alamat Email",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = extendedColors.deepCharcoal
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                enabled = !uiState.isSubmitting,
                                placeholder = {
                                    Text(
                                        text = "nama@email.com",
                                        color = extendedColors.textMuted.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.15f),
                                    focusedBorderColor = extendedColors.electricViolet,
                                    unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.5f),
                                    focusedContainerColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        // Input Password
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Kata Sandi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = extendedColors.deepCharcoal
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                enabled = !uiState.isSubmitting,
                                placeholder = {
                                    Text(
                                        text = "Masukkan kata sandi",
                                        color = extendedColors.textMuted.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = extendedColors.textMuted
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.15f),
                                    focusedBorderColor = extendedColors.electricViolet,
                                    unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.5f),
                                    focusedContainerColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // Opsi Remember Me & Forgot Password
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = extendedColors.electricViolet,
                                        uncheckedColor = extendedColors.textMuted.copy(alpha = 0.5f)
                                    )
                                )
                                Text(
                                    text = "Ingat saya",
                                    fontSize = 12.sp,
                                    color = extendedColors.deepCharcoal
                                )
                            }

                            Text(
                                text = "Lupa Kata Sandi?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.electricViolet,
                                modifier = Modifier.clickable { onForgotPasswordClick() }
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        // Tombol Utama Masuk
                        val isFormValid = email.isNotBlank() && password.isNotBlank()
                        Button(
                            onClick = { viewModel.login(email, password, rememberMe) },
                            enabled = !uiState.isSubmitting && isFormValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = extendedColors.electricViolet,
                                contentColor = Color.White,
                                disabledContainerColor = extendedColors.electricViolet.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.7f)
                            )
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Masuk",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        // Navigation Link Register
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Belum punya akun? ",
                                fontSize = 13.sp,
                                color = extendedColors.textMuted
                            )
                            Text(
                                text = "Daftar sekarang",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.electricViolet,
                                // ✅ Fix: pass empty string untuk register manual
                                modifier = Modifier.clickable { onRegisterClick("") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Footer Sertifikasi Keamanan
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = extendedColors.electricViolet,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xxs))
                    Text(
                        text = "Berizin & Diawasi OJK • Terdaftar AFPI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = extendedColors.deepCharcoal
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.xxs))
                Text(
                    text = "Enkripsi standar perbankan ISO/IEC 27001",
                    fontSize = 10.sp,
                    color = extendedColors.textMuted
                )
            }
        }
    }

    // Dialog Error Handling
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text(text = "Login Gagal", fontWeight = FontWeight.Bold) },
            text = { Text(text = error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                    Text("OK", color = extendedColors.electricViolet, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}