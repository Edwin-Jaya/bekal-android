package com.edwin.bekal.presentation.auth.register.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edwin.bekal.presentation.auth.register.RegisterUiState
import com.edwin.bekal.presentation.auth.register.RegisterViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StepJobContent(
    uiState: RegisterUiState,
    viewModel: RegisterViewModel,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

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
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- CARD 01: Status Pekerjaan & Perusahaan ---
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "STATUS PEKERJAAN & PERUSAHAAN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Text("Tipe Pekerjaan *", fontSize = 10.sp, color = Color.Gray)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    employmentOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = uiState.jobType == key,
                            onClick = { viewModel.onJobTypeChange(key) },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                uiState.jobTypeError?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                OutlinedTextField(
                    value = uiState.companyName,
                    onValueChange = viewModel::onCompanyNameChange,
                    label = { Text("Nama Perusahaan / Instansi *", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    isError = uiState.companyNameError != null,
                    supportingText = {
                        uiState.companyNameError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.industry,
                    onValueChange = viewModel::onIndustryChange,
                    label = { Text("Bidang Industri *", fontSize = 11.sp) },
                    placeholder = { Text("Contoh: Teknologi Informasi", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Domain, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    isError = uiState.industryError != null,
                    supportingText = {
                        uiState.industryError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.position,
                    onValueChange = viewModel::onPositionChange,
                    label = { Text("Jabatan / Posisi *", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    isError = uiState.positionError != null,
                    supportingText = {
                        uiState.positionError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uiState.employmentStartDate,
                        onValueChange = { },
                        label = { Text("Tanggal Mulai Bekerja *", fontSize = 11.sp) },
                        placeholder = { Text("YYYY-MM-DD", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal", modifier = Modifier.size(18.dp))
                            }
                        },
                        isError = uiState.employmentStartDateError != null,
                        supportingText = {
                            uiState.employmentStartDateError?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        shape = RoundedCornerShape(6.dp),
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

        // --- CARD 02: Pendapatan & Keuangan Bulanan ---
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "PENDAPATAN & KEUANGAN BULANAN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = uiState.monthlyIncome,
                    onValueChange = viewModel::onMonthlyIncomeChange,
                    label = { Text("Pendapatan Bersih Bulanan (THP) *", fontSize = 11.sp) },
                    prefix = { Text("Rp ", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.monthlyIncomeError != null,
                    supportingText = {
                        uiState.monthlyIncomeError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.otherIncome,
                    onValueChange = viewModel::onOtherIncomeChange,
                    label = { Text("Pendapatan Lainnya (Opsional)", fontSize = 11.sp) },
                    prefix = { Text("Rp ", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFAF5FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Estimasi Limit Kredit Tersedia hingga Rp 45.000.000 berdasarkan profil pendapatan Anda.",
                            fontSize = 10.sp,
                            color = Color(0xFF7E22CE)
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
                ) { Text("Pilih", fontSize = 12.sp) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal", fontSize = 12.sp) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}