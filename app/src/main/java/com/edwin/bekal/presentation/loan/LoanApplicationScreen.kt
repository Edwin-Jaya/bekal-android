package com.edwin.bekal.presentation.loan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private fun BigDecimal.toRupiahFormat(): String {
    return try {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        formatter.format(this)
    } catch (e: Exception) {
        this.toPlainString()
    }
}

private fun String.formatInputToRupiah(): String {
    val cleanString = this.replace(".", "").replace(",", "").trim()
    val parsed = cleanString.toBigDecimalOrNull() ?: return this
    return parsed.toRupiahFormat()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LoanApplicationScreen(
    plafondLimit: BigDecimal,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToLoans: () -> Unit = onNavigateBack,
    viewModel: LoanApplicationViewModel = hiltViewModel()
) {
    val extendedColors = BekalTheme.extendedColors
    val uiState by viewModel.uiState.collectAsState()
    val branchState by viewModel.branchState.collectAsState()
    val plafondState by viewModel.plafondState.collectAsState()
    val amountRequested by viewModel.amountRequested.collectAsState()
    val tenorMonths by viewModel.tenorMonths.collectAsState()
    val purpose by viewModel.purpose.collectAsState()
    val selectedBranchId by viewModel.selectedBranchId.collectAsState()

    val whitelistedTenors = remember { listOf(6, 8, 12, 16, 20, 24) }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(Unit) {
        viewModel.resetFormState()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadActivePlafond()
                viewModel.loadBranches()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isFormValid = remember(amountRequested, tenorMonths, selectedBranchId) {
        val cleanAmount = amountRequested.replace(".", "")
        cleanAmount.isNotBlank() &&
                cleanAmount.toBigDecimalOrNull() != null &&
                tenorMonths.isNotBlank() &&
                tenorMonths.toIntOrNull() != null &&
                !selectedBranchId.isNullOrBlank()
    }

    val activePlafond = (plafondState as? PlafondUiState.Success)?.plafond
    val displayLimit = activePlafond?.availableAmount ?: plafondLimit

    // Pop-up Dialog saat Pengajuan Berhasil -> Mengarahkan ke Loan Screen
    if (uiState is LoanApplicationUiState.Success) {
        val successState = uiState as LoanApplicationUiState.Success
        AlertDialog(
            onDismissRequest = {
                viewModel.resetFormState()
                onNavigateToLoans()
            },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = extendedColors.successSoft,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = extendedColors.successMain,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Pengajuan Berhasil!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = extendedColors.deepCharcoal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = "Pengajuan pinjaman Anda telah berhasil disimpan dan sedang diproses.",
                        fontSize = 13.sp,
                        color = extendedColors.textMuted,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        shape = RoundedCornerShape(Radius.md),
                        color = extendedColors.canvasBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.xs)
                    ) {
                        Text(
                            text = "No. Aplikasi: ${successState.response.applicationNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(Spacing.sm)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetFormState()
                        onNavigateToLoans()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.electricViolet,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Lihat Pinjaman Saya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            containerColor = extendedColors.surfaceCard,
            shape = RoundedCornerShape(Radius.xl)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extendedColors.canvasBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Header
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
                text = "Pengajuan Pinjaman",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal,
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }

        if (plafondState is PlafondUiState.Unauthenticated) {
            UnauthenticatedView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                onNavigateToLogin = onNavigateToLogin
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Bento Card 1: Plafond Info (Hero Card)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.xl),
                    colors = CardDefaults.cardColors(containerColor = extendedColors.deepCharcoal),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Plafond Aktif Tersedia",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(extendedColors.electricViolet.copy(alpha = 0.25f))
                                    .padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                            ) {
                                Text(
                                    text = when (plafondState) {
                                        is PlafondUiState.Loading -> "Sinkron..."
                                        is PlafondUiState.Success -> "Aktif"
                                        else -> "Estimasi"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.accentSoft
                                )
                            }
                        }

                        Text(
                            text = "Rp ${displayLimit.toRupiahFormat()}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = when (plafondState) {
                                is PlafondUiState.Loading -> "Memuat batas limit plafond..."
                                is PlafondUiState.Success -> "Batas maksimum nominal yang dapat diajukan."
                                is PlafondUiState.NotAvailable -> "Plafond tidak tersedia saat ini"
                                is PlafondUiState.Error -> "Gagal memuat limit plafond backend"
                                else -> "Batas limit standar"
                            },
                            fontSize = 11.sp,
                            color = if (plafondState is PlafondUiState.Error) extendedColors.dangerSoft else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Bento Card 2: Financial Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.xl),
                    colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
                    border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Text(
                            text = "1. Detail Finansial",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )

                        BranchDropdownBento(
                            branchState = branchState,
                            selectedBranchId = selectedBranchId,
                            onBranchSelected = { viewModel.selectedBranchId.value = it }
                        )

                        Column {
                            Text(
                                text = "Jumlah Pengajuan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = extendedColors.deepCharcoal
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            OutlinedTextField(
                                value = amountRequested,
                                onValueChange = { input ->
                                    val clean = input.replace(".", "").filter { it.isDigit() }
                                    viewModel.amountRequested.value = clean.formatInputToRupiah()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = extendedColors.deepCharcoal) },
                                shape = RoundedCornerShape(Radius.lg),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.2f),
                                    focusedBorderColor = extendedColors.electricViolet,
                                    unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.3f),
                                    focusedContainerColor = Color.White
                                ),
                                supportingText = {
                                    val cleanAmount = amountRequested.replace(".", "").toBigDecimalOrNull()
                                    if (cleanAmount != null && cleanAmount > displayLimit) {
                                        Text(
                                            "Pengajuan melebihi plafond aktif!",
                                            color = extendedColors.dangerMain,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Bento Card 3: Tenor Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.xl),
                    colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
                    border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Text(
                            text = "2. Jangka Waktu (Tenor)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            whitelistedTenors.forEach { tenor ->
                                val isSelected = tenorMonths == tenor.toString()
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.tenorMonths.value = tenor.toString() },
                                    label = {
                                        Text(
                                            text = "$tenor Bulan",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    shape = RoundedCornerShape(Radius.pill),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = extendedColors.electricViolet,
                                        selectedLabelColor = Color.White,
                                        containerColor = extendedColors.canvasBackground,
                                        labelColor = extendedColors.deepCharcoal
                                    )
                                )
                            }
                        }
                    }
                }

                // Bento Card 4: Additional Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.xl),
                    colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
                    border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text(
                            text = "3. Informasi Tambahan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )

                        OutlinedTextField(
                            value = purpose,
                            onValueChange = { viewModel.purpose.value = it },
                            placeholder = { Text("Tujuan pinjaman (opsional)", color = extendedColors.textMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp),
                            minLines = 2,
                            shape = RoundedCornerShape(Radius.lg),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.2f),
                                focusedBorderColor = extendedColors.electricViolet,
                                unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.3f),
                                focusedContainerColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Submit Button
                Button(
                    onClick = {
                        val cleanAmount = amountRequested.replace(".", "").toBigDecimalOrNull()
                        val tenor = tenorMonths.toIntOrNull()
                        if (cleanAmount != null && tenor != null) {
                            viewModel.submitLoanApplication(
                                amountRequested = cleanAmount,
                                tenorMonths = tenor,
                                purpose = purpose.ifBlank { null }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.electricViolet,
                        contentColor = Color.White,
                        disabledContainerColor = extendedColors.textMuted.copy(alpha = 0.3f)
                    ),
                    enabled = isFormValid &&
                            uiState !is LoanApplicationUiState.Loading &&
                            plafondState is PlafondUiState.Success
                ) {
                    if (uiState is LoanApplicationUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Kirim Pengajuan Pinjaman",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Error Card Notification
                if (uiState is LoanApplicationUiState.Error) {
                    val errorState = uiState as LoanApplicationUiState.Error
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(containerColor = extendedColors.dangerSoft),
                        border = BorderStroke(1.dp, extendedColors.dangerMain.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = errorState.message,
                            modifier = Modifier.padding(Spacing.lg),
                            color = extendedColors.dangerMain,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))
            }
        }
    }
}

@Composable
private fun UnauthenticatedView(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = extendedColors.accentSoft,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = extendedColors.electricViolet
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = "Sesi Anda Berakhir",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = extendedColors.deepCharcoal
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = "Silakan login terlebih dahulu untuk mengajukan pinjaman.",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = extendedColors.textMuted
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = extendedColors.deepCharcoal)
        ) {
            Text("Login Sekarang", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BranchDropdownBento(
    branchState: BranchUiState,
    selectedBranchId: String?,
    onBranchSelected: (String) -> Unit
) {
    val extendedColors = BekalTheme.extendedColors
    var expanded by remember { mutableStateOf(false) }

    when (branchState) {
        is BranchUiState.Loading -> {
            OutlinedTextField(
                value = "Memuat daftar cabang...",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg)
            )
        }

        is BranchUiState.Error -> {
            Text(
                "Gagal memuat daftar cabang: ${branchState.message}",
                color = extendedColors.dangerMain,
                fontSize = 11.sp
            )
        }

        is BranchUiState.Success -> {
            val selectedBranch = branchState.branches.find { it.id == selectedBranchId }

            Column {
                Text(
                    text = "Cabang Operasional",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.deepCharcoal
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedBranch?.let {
                            if (it.branchCity != null) "${it.branchName} - ${it.branchCity}" else it.branchName
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Pilih lokasi cabang terdekat", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = extendedColors.electricViolet
                            )
                        },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.2f),
                            focusedBorderColor = extendedColors.electricViolet,
                            unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.3f),
                            focusedContainerColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        branchState.branches.forEach { branch ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (branch.branchCity != null) {
                                            "${branch.branchName} - ${branch.branchCity}"
                                        } else {
                                            branch.branchName
                                        },
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onBranchSelected(branch.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}