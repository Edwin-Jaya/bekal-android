package com.edwin.bekal.presentation.loan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanApplicationScreen(
    plafondId: String,
    plafondLimit: BigDecimal,
    onNavigateBack: () -> Unit,
    viewModel: LoanApplicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val branchState by viewModel.branchState.collectAsState()
    val amountRequested by viewModel.amountRequested.collectAsState()
    val tenorMonths by viewModel.tenorMonths.collectAsState()
    val purpose by viewModel.purpose.collectAsState()
    val selectedBranchId by viewModel.selectedBranchId.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetFormState()
        viewModel.loadBranches()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengajuan Pinjaman") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Branch dropdown
            BranchDropdown(
                branchState = branchState,
                selectedBranchId = selectedBranchId,
                onBranchSelected = { viewModel.selectedBranchId.value = it }
            )

            OutlinedTextField(
                value = amountRequested,
                onValueChange = { viewModel.amountRequested.value = it },
                label = { Text("Jumlah Pengajuan") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                prefix = { Text("Rp") }
            )

            Text(
                "Plafon tersedia: Rp ${plafondLimit.toPlainString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = tenorMonths,
                onValueChange = { viewModel.tenorMonths.value = it },
                label = { Text("Tenor (Bulan)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            OutlinedTextField(
                value = purpose,
                onValueChange = { viewModel.purpose.value = it },
                label = { Text("Tujuan Pinjaman") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (amountRequested.isNotBlank() && tenorMonths.isNotBlank()) {
                        viewModel.submitLoanApplication(
                            plafondId = plafondId,
                            amountRequested = BigDecimal(amountRequested),
                            tenorMonths = tenorMonths.toInt(),
                            purpose = purpose.ifBlank { null }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = uiState !is LoanApplicationUiState.Loading
            ) {
                if (uiState is LoanApplicationUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Ajukan Pinjaman")
                }
            }

            when (val state = uiState) {
                is LoanApplicationUiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✓ Pengajuan Berhasil", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "No. Aplikasi: ${state.response.applicationNumber}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(
                                onClick = onNavigateBack,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Selesai")
                            }
                        }
                    }
                }

                is LoanApplicationUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            "❌ ${state.message}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BranchDropdown(
    branchState: BranchUiState,
    selectedBranchId: String?,
    onBranchSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    when (branchState) {
        is BranchUiState.Loading -> {
            OutlinedTextField(
                value = "Memuat cabang...",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Cabang") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        is BranchUiState.Error -> {
            Text(
                "Gagal memuat daftar cabang: ${branchState.message}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        is BranchUiState.Success -> {
            val selectedBranch = branchState.branches.find { it.id == selectedBranchId }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedBranch?.branchName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cabang") },
                    placeholder = { Text("Pilih cabang") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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
                                    }
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