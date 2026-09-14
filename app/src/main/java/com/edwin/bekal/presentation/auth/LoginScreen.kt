package com.edwin.bekal.presentation.auth

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    onRegisterClick: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var email by rememberSaveable() { mutableStateOf("")}
    var password by rememberSaveable() {mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        email = "customer.test@bekal.com"
        password = "P@ssw0rd123!"
    }

    LaunchedEffect(uiState.status) {
        if(uiState.status == AuthStatus.AUTHENTICATED) {
            onLoginSuccess()
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
        // Top Navigation Bar
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

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Spacing.xl),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo Icon
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Logo",
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Header Text
                    Text(
                        text = "Masuk",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.deepCharcoal
                    )

                    Spacer(modifier = Modifier.height(Spacing.xxs))

                    Text(
                        text = "Lanjutkan ke akun Anda",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extendedColors.textMuted
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    // Email Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Email",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = {
                                Text(
                                    text = "nama@email.com",
                                    color = extendedColors.textMuted.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Radius.lg),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.2f),
                                focusedBorderColor = extendedColors.electricViolet,
                                unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    // Password Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Kata Sandi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = {
                                Text(
                                    text = "Masukkan kata sandi",
                                    color = extendedColors.textMuted.copy(alpha = 0.6f),
                                    fontSize = 14.sp
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = image,
                                        contentDescription = null,
                                        tint = extendedColors.textMuted
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Radius.lg),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.2f),
                                focusedBorderColor = extendedColors.electricViolet,
                                unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    // Remember Me & Forgot Password
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
                                    uncheckedColor = extendedColors.textMuted
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

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Login Button
                    Button(
                        onClick = { viewModel.login(email,password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extendedColors.deepCharcoal,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Masuk",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    // Divider "ATAU"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = extendedColors.textMuted.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "ATAU",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textMuted,
                            modifier = Modifier.padding(horizontal = Spacing.md)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = extendedColors.textMuted.copy(alpha = 0.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = onGoogleLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            extendedColors.textMuted.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = extendedColors.canvasBackground,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = extendedColors.electricViolet
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = "Lanjutkan dengan Google",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = extendedColors.deepCharcoal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    // Register Redirect
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Belum punya akun? ",
                            fontSize = 12.sp,
                            color = extendedColors.textMuted
                        )
                        Text(
                            text = "Daftar sekarang",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.electricViolet,
                            modifier = Modifier.clickable { onNavigateToRegister() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Footer Section
            Column(
                modifier = Modifier.padding(bottom = Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.8f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = extendedColors.electricViolet,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "Berizin & Diawasi oleh OJK • Terdaftar AFPI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "Terenkripsi standar perbankan ISO/IEC 27001",
                    fontSize = 10.sp,
                    color = extendedColors.textMuted
                )
            }
        }
    }
}