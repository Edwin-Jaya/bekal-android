package com.edwin.bekal.presentation.auth.register.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edwin.bekal.presentation.auth.register.RegisterUiState
import com.edwin.bekal.presentation.auth.register.RegisterViewModel
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing
import com.edwin.bekal.utils.RupiahVisualTransformation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StepJobContent(
    uiState: RegisterUiState,
    viewModel: RegisterViewModel,
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.15f),
        focusedBorderColor = extendedColors.electricViolet,
        unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.5f),
        focusedContainerColor = Color.White
    )

    val employmentOptions = remember {
        listOf(
            "karyawan_tetap" to "Karyawan Tetap",
            "karyawan_kontrak" to "Karyawan Kontrak",
            "wiraswasta" to "Wiraswasta",
            "profesional" to "Profesional",
            "lainnya" to "Lainnya"
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // --- BENTO CARD 01: Status Pekerjaan ---
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
                Text("Status & Informasi Pekerjaan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = extendedColors.deepCharcoal)

                Text("Tipe Pekerjaan *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = extendedColors.deepCharcoal)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    employmentOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = uiState.jobType == key,
                            onClick = { viewModel.onJobTypeChange(key) },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = extendedColors.electricViolet,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                uiState.jobTypeError?.let { Text(it, color = Color.Red, fontSize = 11.sp) }

                OutlinedTextField(
                    value = uiState.companyName,
                    onValueChange = viewModel::onCompanyNameChange,
                    label = { Text("Nama Perusahaan / Perusahaan *", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = extendedColors.textMuted) },
                    isError = uiState.companyNameError != null,
                    supportingText = uiState.companyNameError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.industry,
                    onValueChange = viewModel::onIndustryChange,
                    label = { Text("Bidang Industri *", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: Teknologi Informasi", fontSize = 13.sp, color = extendedColors.textMuted.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Domain, contentDescription = null, tint = extendedColors.textMuted) },
                    isError = uiState.industryError != null,
                    supportingText = uiState.industryError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.position,
                    onValueChange = viewModel::onPositionChange,
                    label = { Text("Jabatan / Posisi *", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = extendedColors.textMuted) },
                    isError = uiState.positionError != null,
                    supportingText = uiState.positionError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.employmentStartDate,
                        onValueChange = { },
                        label = { Text("Mulai Bekerja *", fontSize = 12.sp) },
                        placeholder = { Text("YYYY-MM-DD", fontSize = 13.sp, color = extendedColors.textMuted.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = extendedColors.textMuted) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = extendedColors.textMuted)
                            }
                        },
                        isError = uiState.employmentStartDateError != null,
                        supportingText = uiState.employmentStartDateError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
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
            }
        }

        // --- BENTO CARD 02: Pendapatan ---
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
                Text("Pendapatan & Keuangan Bulanan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = extendedColors.deepCharcoal)

                OutlinedTextField(
                    value = uiState.monthlyIncome,
                    onValueChange = viewModel::onMonthlyIncomeChange,
                    label = { Text("Gaji Bersih Bulanan (THP) *", fontSize = 12.sp) },
                    prefix = { Text("Rp ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = extendedColors.deepCharcoal) },
                    visualTransformation = RupiahVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.monthlyIncomeError != null,
                    supportingText = uiState.monthlyIncomeError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.otherIncome,
                    onValueChange = viewModel::onOtherIncomeChange,
                    label = { Text("Pendapatan Lainnya (Opsional)", fontSize = 12.sp) },
                    prefix = { Text("Rp ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = extendedColors.deepCharcoal) },
                    visualTransformation = RupiahVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )

                Surface(
                    shape = RoundedCornerShape(Radius.lg),
                    color = extendedColors.accentSoft,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = extendedColors.electricViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "Estimasi limit kredit tersedia hingga Rp 45.000.000 berdasarkan profil penghasilan Anda.",
                            fontSize = 11.sp,
                            color = extendedColors.deepCharcoal
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            viewModel.onEmploymentStartDateChange(formatter.format(Date(millis)))
                        }
                        showDatePicker = false
                    }
                ) { Text("Pilih", color = extendedColors.electricViolet, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}