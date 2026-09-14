package com.edwin.bekal.presentation.loan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edwin.bekal.data.dto.LoanApplicationResponse
import java.math.BigDecimal

/**
 * Tab "Pinjaman" — riwayat pengajuan + tombol untuk mengajukan baru.
 * Plafond aktif diambil sendiri oleh screen ini (via ViewModel), caller tidak perlu passing apa-apa.
 */
@Composable
fun LoansScreen(
    onNavigateToApply: (plafondId: String, plafondLimit: BigDecimal) -> Unit,
    viewModel: LoanApplicationViewModel = hiltViewModel()
) {
    val historyState by viewModel.historyState.collectAsState()
    val plafondState by viewModel.plafondState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLoanHistory()
        viewModel.loadActivePlafond()
    }

    Scaffold(
        floatingActionButton = {
            val plafond = (plafondState as? PlafondUiState.Success)?.plafond
            if (plafond != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onNavigateToApply(plafond.id, plafond.availableAmount)
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Ajukan Pinjaman") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Riwayat Pengajuan",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (val state = historyState) {
                is LoanHistoryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LoanHistoryUiState.Success -> {
                    if (state.applications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada pengajuan pinjaman")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp) // space for FAB
                        ) {
                            items(state.applications) { app ->
                                LoanApplicationCard(app)
                            }
                        }
                    }
                }

                is LoanHistoryUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Gagal memuat: ${state.message}")
                    }
                }
            }
        }
    }
}

@Composable
fun LoanApplicationCard(application: LoanApplicationResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "No. Aplikasi: ${application.applicationNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        application.submittedAt.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(application.status)
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Jumlah", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "Rp ${application.amountRequested.toPlainString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Tenor", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${application.tenorMonths} bulan",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Cicilan/Bulan", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "Rp ${application.monthlyInstallment.toPlainString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!application.purpose.isNullOrBlank()) {
                Column {
                    Text("Tujuan", style = MaterialTheme.typography.labelSmall)
                    Text(application.purpose, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val colors = when (status.lowercase()) {
        "submitted" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        "approved" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        "rejected" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        "disbursed" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(color = colors.first, shape = MaterialTheme.shapes.small) {
        Text(
            status.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colors.second,
            fontWeight = FontWeight.Bold
        )
    }
}