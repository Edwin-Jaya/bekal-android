package com.edwin.bekal.presentation.loan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanRepaymentScreen(
    loanId: String,
    onNavigateBack: () -> Unit,
    viewModel: LoanRepaymentViewModel = hiltViewModel()
) {
    val extendedColors = BekalTheme.extendedColors
    val balanceState by viewModel.balanceState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val paymentInput by viewModel.paymentInput.collectAsState()

    LaunchedEffect(loanId) {
        viewModel.loadLoanBalance(loanId)
    }

    Scaffold(
        containerColor = extendedColors.canvasBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pembayaran Angsuran",
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = extendedColors.deepCharcoal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = extendedColors.canvasBackground)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = balanceState) {
                is BalanceUiState.Loading -> {
                    CircularProgressIndicator(
                        color = extendedColors.electricViolet,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is BalanceUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = state.message,
                            color = extendedColors.deepCharcoal,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                is BalanceUiState.Success -> {
                    val balance = state.balance
                    val isSettled = balance.isFullyPaid || balance.status == "CLOSED"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Spacing.xl, vertical = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // ── HERO CARD: Sisa Tagihan ──
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Radius.xl),
                            colors = CardDefaults.cardColors(containerColor = extendedColors.deepCharcoal),
                            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
                        ) {
                            Column(modifier = Modifier.padding(Spacing.lg)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = balance.applicationNumber,
                                        fontSize = 12.sp,
                                        color = extendedColors.textMuted
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(Radius.sm),
                                        color = extendedColors.electricViolet.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = balance.status,
                                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = extendedColors.electricViolet
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(Spacing.sm))
                                Text("Sisa Tagihan", fontSize = 13.sp, color = extendedColors.textMuted)
                                Text(
                                    text = "Rp ${balance.remainingBalance.toPlainString()}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = androidx.compose.ui.graphics.Color.White
                                )

                                Spacer(modifier = Modifier.height(Spacing.md))
                                LinearProgressIndicator(
                                    progress = {
                                        if (balance.totalRepayment > BigDecimal.ZERO) {
                                            (balance.totalPaidSoFar.toFloat() / balance.totalRepayment.toFloat())
                                                .coerceIn(0f, 1f)
                                        } else 0f
                                    },
                                    color = extendedColors.electricViolet,
                                    trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(Radius.sm))
                                )
                            }
                        }

                        // ── BENTO GRID: Ringkasan Pinjaman ──
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BentoStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.ReceiptLong,
                                label = "Total Tagihan",
                                value = "Rp ${balance.totalRepayment.toPlainString()}"
                            )
                            BentoStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Savings,
                                label = "Total Terbayar",
                                value = "Rp ${balance.totalPaidSoFar.toPlainString()}"
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BentoStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.CreditCard,
                                label = "Angsuran / Bulan",
                                value = "Rp ${balance.monthlyInstallment.toPlainString()}"
                            )
                            BentoStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.CalendarMonth,
                                label = "Tenor",
                                value = "${balance.tenorMonths} Bulan"
                            )
                        }

                        if (isSettled) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.xl),
                                colors = CardDefaults.cardColors(
                                    containerColor = extendedColors.electricViolet.copy(alpha = 0.08f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
                            ) {
                                Column(
                                    modifier = Modifier.padding(Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = extendedColors.electricViolet
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.xs))
                                        Text(
                                            "Pinjaman Sudah Lunas!",
                                            fontWeight = FontWeight.Bold,
                                            color = extendedColors.deepCharcoal
                                        )
                                    }
                                    Text(
                                        "Selamat! Batas CreditTier Anda telah ditingkatkan secara otomatis untuk pengajuan berikutnya.",
                                        fontSize = 13.sp,
                                        color = extendedColors.textMuted
                                    )
                                }
                            }
                        } else {
                            // ── FORM PEMBAYARAN ──
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.xl),
                                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
                            ) {
                                Column(
                                    modifier = Modifier.padding(Spacing.lg),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                                ) {
                                    Text(
                                        "Jumlah Pembayaran",
                                        fontWeight = FontWeight.Bold,
                                        color = extendedColors.deepCharcoal
                                    )

                                    OutlinedTextField(
                                        value = paymentInput,
                                        onValueChange = { viewModel.paymentInput.value = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        prefix = { Text("Rp ") },
                                        shape = RoundedCornerShape(Radius.sm),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = extendedColors.electricViolet
                                        )
                                    )

                                    OutlinedButton(
                                        onClick = { viewModel.paymentInput.value = balance.monthlyInstallment.toPlainString() },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(Radius.sm),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            extendedColors.electricViolet.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Text("Isi 1x Angsuran Bulanan", color = extendedColors.electricViolet)
                                    }

                                    Button(
                                        onClick = {
                                            val amount = paymentInput.toBigDecimalOrNull()
                                            if (amount != null && amount > BigDecimal.ZERO) {
                                                viewModel.executeRepayment(loanId, amount)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(Radius.md),
                                        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet),
                                        enabled = submitState !is RepaymentSubmitState.Loading && paymentInput.isNotBlank()
                                    ) {
                                        if (submitState is RepaymentSubmitState.Loading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(22.dp),
                                                color = androidx.compose.ui.graphics.Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text("Bayar Sekarang", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (submitState is RepaymentSubmitState.Error) {
                                        Text(
                                            "❌ ${(submitState as RepaymentSubmitState.Error).message}",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Surface(
                shape = RoundedCornerShape(Radius.sm),
                color = extendedColors.electricViolet.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = extendedColors.electricViolet,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(label, fontSize = 11.sp, color = extendedColors.textMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal
            )
        }
    }
}