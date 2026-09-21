package com.edwin.bekal.presentation.loan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwin.bekal.data.dto.LoanApplicationResponse
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoansScreen(
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToPayment: (String) -> Unit = {},
    onNavigateToLoanApplication: (BigDecimal) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LoanApplicationViewModel = hiltViewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = true)
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()
    val plafondState by viewModel.plafondState.collectAsStateWithLifecycle()
    val extendedColors = BekalTheme.extendedColors

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            viewModel.loadLoanHistory()
            viewModel.loadActivePlafond()
        }
    }

    val currentLimit = (plafondState as? PlafondUiState.Success)?.plafond?.availableAmount
        ?: BigDecimal.ZERO
    val currentTier = (plafondState as? PlafondUiState.Success)?.plafond?.creditTier
        ?: "TIER_1"

    // Cek apakah ada pengajuan yang sedang diproses/diajukan
    val hasPendingApplication = (historyState as? LoanHistoryUiState.Success)
        ?.applications
        ?.any { application ->
            application.status.lowercase() in listOf("in_review", "in_approval","in_disbursement","pending", "under_review", "diproses", "disbursed")
        } == true

    Scaffold(
        modifier = modifier,
        containerColor = extendedColors.canvasBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + Spacing.xs,
                start = Spacing.xl,
                end = Spacing.xl,
                bottom = padding.calculateBottomPadding() + 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // --- Minimalist Text Header ---
            item {
                Column(
                    modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.xs)
                ) {
                    Text(
                        text = "Pinjaman Saya",
                        style = MaterialTheme.typography.headlineMedium,
                        color = extendedColors.deepCharcoal
                    )
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = "Atur pinjaman aktif & lihat riwayat transaksi Anda",
                        style = MaterialTheme.typography.labelLarge,
                        color = extendedColors.textMuted
                    )
                }
            }

            // PENGONDISIAN: Jika User Belum Login
            if (!isLoggedIn) {
                item {
                    LoginRequiredCard(onLoginClick = onNavigateToLogin)
                }
            } else {
                // 1. HERO BENTO CARD: TIER & APPLY PROMO
                item {
                    ApplyLoanHeroCard(
                        limit = currentLimit,
                        tier = currentTier,
                        isLoading = plafondState is PlafondUiState.Loading,
                        hasPendingApplication = hasPendingApplication,
                        onApplyClick = { onNavigateToLoanApplication(currentLimit) }
                    )
                }

                // Banner Penguncian Pengajuan (sama seperti Home)
                if (hasPendingApplication) {
                    item {
                        ApplicationLockBanner()
                    }
                }

                // 2. JUDUL SEKSI RIWAYAT
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = Spacing.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = extendedColors.deepCharcoal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "Riwayat Pengajuan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                    }
                }

                // 3. KONTEN RIWAYAT (LOADING, EMPTY, ERROR, SUCCESS)
                when (val state = historyState) {
                    is LoanHistoryUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = extendedColors.electricViolet)
                            }
                        }
                    }

                    is LoanHistoryUiState.Success -> {
                        if (state.applications.isEmpty()) {
                            item {
                                EmptyLoanHistoryCard(
                                    onApplyClick = { onNavigateToLoanApplication(currentLimit) }
                                )
                            }
                        } else {
                            items(state.applications) { application ->
                                LoanApplicationCard(
                                    application = application,
                                    onClick = { onNavigateToDetail(application.id) }
                                )
                            }
                        }
                    }

                    is LoanHistoryUiState.Unauthorized -> {
                        item {
                            LoginRequiredCard(onLoginClick = onNavigateToLogin)
                        }
                    }

                    is LoanHistoryUiState.Error -> {
                        item {
                            ErrorLoanCard(
                                message = state.message,
                                onRetry = { viewModel.loadLoanHistory() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// LOGIN REQUIRED CARD (GUEST STATE)
// ==========================================
@Composable
private fun LoginRequiredCard(
    onLoginClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = extendedColors.accentSoft,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = extendedColors.electricViolet,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = "Silakan Masuk Terlebih Dahulu",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Masuk ke akun Anda untuk melihat limit pinjaman aktif dan riwayat pengajuan.",
                fontSize = 12.sp,
                color = extendedColors.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(Radius.lg),
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
            ) {
                Text(
                    text = "Masuk Sekarang",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ==========================================
// HERO BENTO CARD (PROMO / LIMIT AKTIF)
// ==========================================
@Composable
private fun ApplyLoanHeroCard(
    limit: BigDecimal,
    tier: String,
    isLoading: Boolean,
    hasPendingApplication: Boolean = false,
    onApplyClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors
    val tierLabel = tier.replace("TIER_", "Tier ")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Limit Potensial Anda",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.textMuted
                )
                Surface(
                    shape = CircleShape,
                    color = extendedColors.accentSoft
                ) {
                    Text(
                        text = tierLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.electricViolet,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = extendedColors.electricViolet,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = limit.toRupiahFormat(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = extendedColors.deepCharcoal
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Button(
                onClick = onApplyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                enabled = !isLoading && limit > BigDecimal.ZERO && !hasPendingApplication,
                shape = RoundedCornerShape(Radius.lg),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extendedColors.electricViolet,
                    disabledContainerColor = extendedColors.textMuted.copy(alpha = 0.2f),
                    disabledContentColor = extendedColors.textMuted
                )
            ) {
                Text(
                    text = if (hasPendingApplication) "Pengajuan Sedang Diproses" else "Ajukan Pinjaman Baru",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasPendingApplication) extendedColors.textMuted else Color.White
                )
            }
        }
    }
}

// ==========================================
// APPLICATION LOCK BANNER
// ==========================================
@Composable
private fun ApplicationLockBanner() {
    val extendedColors = BekalTheme.extendedColors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        color = extendedColors.warningSoft,
        border = BorderStroke(1.dp, extendedColors.warningMain.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = extendedColors.warningMain,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            Text(
                text = "Pengajuan baru dikunci hingga verifikasi aktif selesai.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = extendedColors.warningMain
            )
        }
    }
}

// ==========================================
// ITEM KARTU RIWAYAT PENGAJUAN (BENTO STYLE)
// ==========================================
@Composable
fun LoanApplicationCard(
    application: LoanApplicationResponse,
    onClick: () -> Unit = {}
) {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.xl))
            .clickable { onClick() },
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "No. Aplikasi",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = extendedColors.textMuted
                    )
                    Text(
                        text = application.applicationNumber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )
                    Text(
                        text = application.submittedAt.take(10),
                        fontSize = 10.sp,
                        color = extendedColors.textMuted
                    )
                }
                StatusChip(status = application.status)
            }

            HorizontalDivider(color = extendedColors.textMuted.copy(alpha = 0.1f))

            Surface(
                shape = RoundedCornerShape(Radius.lg),
                color = extendedColors.canvasBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailColumn(
                        label = "Jumlah",
                        value = application.amountRequested.toRupiahFormat()
                    )
                    DetailColumn(
                        label = "Tenor",
                        value = "${application.tenorMonths} Bln"
                    )
                    DetailColumn(
                        label = "Cicilan / Bln",
                        value = application.monthlyInstallment.toRupiahFormat()
                    )
                }
            }

            if (!application.purpose.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = Spacing.xxs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = extendedColors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Tujuan: ${application.purpose}",
                        fontSize = 11.sp,
                        color = extendedColors.textMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailColumn(label: String, value: String) {
    val extendedColors = BekalTheme.extendedColors
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = extendedColors.textMuted,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = extendedColors.deepCharcoal
        )
    }
}

// ==========================================
// STATUS CHIP
// ==========================================
@Composable
fun StatusChip(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "submitted" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "approved", "disbursed" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "rejected" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> BekalTheme.extendedColors.accentSoft to BekalTheme.extendedColors.electricViolet
    }

    Surface(
        color = bgColor,
        shape = CircleShape
    ) {
        Text(
            text = status.replace("_", " ").uppercase(),
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==========================================
// EMPTY & ERROR STATES
// ==========================================
@Composable
private fun EmptyLoanHistoryCard(onApplyClick: () -> Unit) {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = extendedColors.accentSoft,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = extendedColors.electricViolet,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = "Belum Ada Pengajuan Pinjaman",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Nikmati kemudahan akses pendanaan dengan proses cepat dan aman.",
                fontSize = 11.sp,
                color = extendedColors.textMuted,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            OutlinedButton(
                onClick = onApplyClick,
                shape = RoundedCornerShape(Radius.lg),
                border = BorderStroke(1.dp, extendedColors.electricViolet)
            ) {
                Text(
                    text = "Ajukan Sekarang",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.electricViolet
                )
            }
        }
    }
}

@Composable
private fun ErrorLoanCard(
    message: String,
    onRetry: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFC62828),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Gagal memuat riwayat: $message",
                fontSize = 12.sp,
                color = extendedColors.deepCharcoal
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(Radius.md),
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
            ) {
                Text("Coba Lagi", fontSize = 12.sp)
            }
        }
    }
}

// ==========================================
// UTILITY FORMATTER
// ==========================================
private fun BigDecimal.toRupiahFormat(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(this).replace("Rp", "Rp ").replace(",00", "")
}