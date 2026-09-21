package com.edwin.bekal.presentation.auth.register.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.edwin.bekal.presentation.auth.register.RegisterUiState
import com.edwin.bekal.presentation.auth.register.RegisterViewModel
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepAccountContent(
    uiState: RegisterUiState,
    viewModel: RegisterViewModel,
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors
    var passwordVisible by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val genderOptions = listOf("Laki-laki", "Perempuan")
    val scrollState = rememberScrollState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.15f),
        focusedBorderColor = extendedColors.electricViolet,
        unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.5f),
        focusedContainerColor = Color.White
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // --- BENTO CARD 01: Kredensial Akun ---
        Card(
            shape = RoundedCornerShape(Radius.xl),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Kredensial Akun",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                        Text(
                            text = "Data autentikasi & nama sesuai identitas",
                            fontSize = 12.sp,
                            color = extendedColors.textMuted
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = { Text("Nama Lengkap (Sesuai KTP) *", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = extendedColors.textMuted) },
                    trailingIcon = {
                        if (uiState.fullName.isNotBlank()) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = extendedColors.electricViolet)
                        }
                    },
                    isError = uiState.fullNameError != null,
                    supportingText = uiState.fullNameError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { if (!uiState.isEmailFromGoogle) viewModel.onEmailChange(it) },
                    label = { Text("Alamat Email Aktif *", fontSize = 12.sp) },
                    placeholder = { Text("contoh@gmail.com", fontSize = 13.sp, color = extendedColors.textMuted.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = extendedColors.textMuted) },
                    trailingIcon = {
                        if (uiState.isEmailFromGoogle) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Email dari Google",
                                tint = extendedColors.electricViolet
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    readOnly = uiState.isEmailFromGoogle,
                    isError = uiState.emailError != null,
                    supportingText = if (uiState.isEmailFromGoogle) {
                        { Text("Email diisi otomatis dari akun Google", fontSize = 11.sp, color = extendedColors.electricViolet) }
                    } else {
                        uiState.emailError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = if (uiState.isEmailFromGoogle) {
                        textFieldColors.copy(
                            focusedContainerColor = extendedColors.accentSoft.copy(alpha = 0.3f),
                            unfocusedContainerColor = extendedColors.accentSoft.copy(alpha = 0.3f)
                        )
                    } else {
                        textFieldColors
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Kata Sandi *", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = extendedColors.textMuted) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = extendedColors.textMuted
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )
            }
        }

        // --- BENTO CARD 02: Kontak & Domisili ---
        Card(
            shape = RoundedCornerShape(Radius.xl),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Informasi Kontak & Domisili",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                        Text(
                            text = "Kontak WhatsApp & alamat tempat tinggal saat ini",
                            fontSize = 12.sp,
                            color = extendedColors.textMuted
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) viewModel.onPhoneNumberChange(input)
                    },
                    label = { Text("Nomor Handphone (WhatsApp) *", fontSize = 12.sp) },
                    prefix = { Text("+62 ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = extendedColors.deepCharcoal) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.phoneNumberError != null,
                    supportingText = uiState.phoneNumberError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )

                // Bento Grid Row: Tanggal Lahir & Jenis Kelamin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = uiState.dateOfBirth,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tgl Lahir *", fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = extendedColors.textMuted, modifier = Modifier.size(18.dp))
                                }
                            },
                            isError = uiState.dateOfBirthError != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = CircleShape,
                            colors = textFieldColors,
                            singleLine = true
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showDatePicker = true }
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender *", fontSize = 11.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            isError = uiState.genderError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = CircleShape,
                            colors = textFieldColors,
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.onGenderChange(option)
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = viewModel::onAddressChange,
                    label = { Text("Alamat Lengkap Domisili *", fontSize = 12.sp) },
                    isError = uiState.addressError != null,
                    supportingText = uiState.addressError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = textFieldColors
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            viewModel.onDateOfBirthChange(formatter.format(Date(millis)))
                        }
                        showDatePicker = false
                    }
                ) { Text("OK", color = extendedColors.electricViolet, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}