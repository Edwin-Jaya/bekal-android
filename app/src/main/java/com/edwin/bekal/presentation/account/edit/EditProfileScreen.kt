package com.edwin.bekal.presentation.account.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val extendedColors = BekalTheme.extendedColors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()

    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val phoneNumber by viewModel.phoneNumber.collectAsStateWithLifecycle()
    val address by viewModel.address.collectAsStateWithLifecycle()
    val gender by viewModel.gender.collectAsStateWithLifecycle()

    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("MALE" to "Laki-laki", "FEMALE" to "Perempuan")

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(saveState) {
        if (saveState is SaveUiState.Success) {
            onNavigateBack()
        }
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
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = extendedColors.deepCharcoal
                )
            }
            Text(
                text = "Edit Profil",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal,
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }

        when (val state = uiState) {
            is EditProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = extendedColors.electricViolet)
                }
            }

            is EditProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = extendedColors.dangerMain,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            "Gagal memuat profil: ${state.message}",
                            color = extendedColors.deepCharcoal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            is EditProfileUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.xl),
                        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
                        border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = extendedColors.accentSoft,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = extendedColors.electricViolet,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(Spacing.md))
                            Column {
                                Text(
                                    text = fullName.ifBlank { "Pengguna Bekal" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.deepCharcoal
                                )
                                Text(
                                    text = "Perbarui informasi akun Anda secara berkala",
                                    fontSize = 11.sp,
                                    color = extendedColors.textMuted
                                )
                            }
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
                                .padding(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Text(
                                text = "DATA DIRI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = extendedColors.textMuted
                            )

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { viewModel.fullName.value = it },
                                label = { Text("Nama Lengkap", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.lg),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.2f),
                                    focusedBorderColor = extendedColors.electricViolet
                                )
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { viewModel.phoneNumber.value = it },
                                label = { Text("Nomor Handphone", fontSize = 12.sp) },
                                prefix = { Text("+62 ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.lg),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.2f),
                                    focusedBorderColor = extendedColors.electricViolet
                                )
                            )

                            ExposedDropdownMenuBox(
                                expanded = genderExpanded,
                                onExpandedChange = { genderExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = genderOptions.find { it.first == gender }?.second ?: gender,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Jenis Kelamin", fontSize = 12.sp) },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(Radius.lg),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.2f),
                                        focusedBorderColor = extendedColors.electricViolet
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = genderExpanded,
                                    onDismissRequest = { genderExpanded = false }
                                ) {
                                    genderOptions.forEach { (value, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label, fontSize = 12.sp) },
                                            onClick = {
                                                viewModel.gender.value = value
                                                genderExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = address,
                                onValueChange = { viewModel.address.value = it },
                                label = { Text("Alamat Tempat Tinggal", fontSize = 12.sp) },
                                minLines = 2,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.lg),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.2f),
                                    focusedBorderColor = extendedColors.electricViolet
                                )
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToForgotPassword() },
                        shape = RoundedCornerShape(Radius.xl),
                        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
                        border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.lg),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(Radius.md),
                                    color = extendedColors.accentSoft,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = extendedColors.electricViolet,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(Spacing.md))
                                Column {
                                    Text(
                                        text = "Ubah Kata Sandi",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = extendedColors.deepCharcoal
                                    )
                                    Text(
                                        text = "Atur ulang kata sandi melalui verifikasi OTP",
                                        fontSize = 10.sp,
                                        color = extendedColors.textMuted
                                    )
                                }
                            }

                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Navigasi Reset Password",
                                tint = extendedColors.textMuted
                            )
                        }
                    }

                    if (saveState is SaveUiState.Error) {
                        Text(
                            text = "Gagal menyimpan: ${(saveState as SaveUiState.Error).message}",
                            color = extendedColors.dangerMain,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.saveProfile() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = saveState !is SaveUiState.Loading,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
                    ) {
                        if (saveState is SaveUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Simpan Perubahan",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))
                }
            }
        }
    }
}