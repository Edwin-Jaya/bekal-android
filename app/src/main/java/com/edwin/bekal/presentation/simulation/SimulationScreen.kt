package com.edwin.bekal.presentation.simulation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwin.bekal.presentation.loan.LoanApplicationViewModel
import com.edwin.bekal.presentation.loan.LoanHistoryUiState
import com.edwin.bekal.presentation.loan.PlafondUiState
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SimulationScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToLoanApplication: (BigDecimal) -> Unit = {},
    onConfirmLoan: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LoanApplicationViewModel = hiltViewModel()
) {
    val extendedColors = BekalTheme.extendedColors

    // Auth & Plafond State Check (selaras dengan LoanScreen.kt)
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val plafondState by viewModel.plafondState.collectAsStateWithLifecycle()
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            viewModel.loadActivePlafond()
            viewModel.loadLoanHistory()
        }
    }

    val activePlafond = (plafondState as? PlafondUiState.Success)?.plafond
    val userTier = activePlafond?.creditTier

    // Plafond limit bounds
    val isPlafondZero = isLoggedIn && activePlafond != null && activePlafond.availableAmount <= BigDecimal.ZERO
    val minLimit = 1_000_000f
    val maxLimit = if (isLoggedIn && activePlafond != null) {
        maxOf(minLimit, activePlafond.availableAmount.toFloat())
    } else {
        50_000_000f
    }

    // Pending application check (sama persis dengan LoanScreen.kt)
    val hasPendingApplication = (historyState as? LoanHistoryUiState.Success)
        ?.applications
        ?.any { application ->
            application.status.lowercase() in listOf("in_review", "in_approval", "in_disbursement", "pending", "under_review", "diproses", "disbursed")
        } == true

    // Quick amounts calculation disesuaikan dengan plafon maksimal
    val quickAmounts = remember(maxLimit) {
        if (maxLimit <= 10_000_000f) {
            val step = maxLimit / 4f
            listOf(step, step * 2, step * 3, maxLimit).map {
                (it / 500_000).toInt() * 500_000f
            }.distinct().filter { it >= 1_000_000f }.let { list ->
                if (list.isEmpty()) listOf(maxLimit) else list
            }
        } else if (maxLimit == 50_000_000f) {
            listOf(10_000_000f, 25_000_000f, 35_000_000f, 50_000_000f)
        } else {
            listOf(
                maxLimit * 0.25f,
                maxLimit * 0.5f,
                maxLimit * 0.75f,
                maxLimit
            ).map { (it / 1_000_000).toInt() * 1_000_000f }.distinct().filter { it >= 1_000_000f }.let { list ->
                if (list.isEmpty()) listOf(maxLimit) else list
            }
        }
    }

    // Interactive State
    var amount by remember { mutableFloatStateOf(25_000_000f) }
    var selectedTenor by remember { mutableIntStateOf(12) }

    // Sinkronkan nominal pinjaman saat batas limit plafon berubah
    LaunchedEffect(maxLimit) {
        if (amount > maxLimit) {
            amount = maxLimit
        } else if (amount < minLimit) {
            amount = minLimit
        }
    }

    // Filter tenor yang tersedia berdasarkan batas maxTenorMonths pada plafon
    val maxTenor = activePlafond?.maxTenorMonths ?: 24
    val availableTenors = remember(maxTenor) {
        listOf(6, 8, 12, 16, 20, 24).filter { it <= maxTenor }
    }
    LaunchedEffect(availableTenors) {
        if (selectedTenor !in availableTenors && availableTenors.isNotEmpty()) {
            selectedTenor = availableTenors.last()
        }
    }

    // Suku bunga & Perhitungan cicilan bulanan (1% per bulan flat, selaras 100% dengan LoanApplicationServiceImpl.java)
    val interestRateBd = activePlafond?.interestRate ?: BigDecimal("1.0")
    val amountBd = amount.toLong().toBigDecimal()
    val tenorBd = selectedTenor.toBigDecimal()

    val totalInterest = amountBd
        .multiply(interestRateBd)
        .divide(BigDecimal.valueOf(100), 10, java.math.RoundingMode.HALF_UP)
        .multiply(tenorBd)

    val totalRepayment = amountBd.add(totalInterest).setScale(2, java.math.RoundingMode.HALF_UP)
    val monthlyInstallment = totalRepayment.divide(tenorBd, 2, java.math.RoundingMode.HALF_UP)

    val interestRateFormatted = "${interestRateBd.stripTrailingZeros().toPlainString()}% / bulan"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.canvasBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            top = Spacing.xs,
            start = Spacing.xl,
            end = Spacing.xl,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // --- Minimalist Text Header ---
        item {
            Column(
                modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.xs)
            ) {
                Text(
                    text = "Simulasi Pinjaman",
                    style = MaterialTheme.typography.headlineMedium,
                    color = extendedColors.deepCharcoal
                )
                Spacer(modifier = Modifier.height(Spacing.xxs))
                Text(
                    text = "Hitung estimasi cicilan & plafon sesuai kebutuhan Anda",
                    style = MaterialTheme.typography.labelLarge,
                    color = extendedColors.textMuted
                )
            }
        }

        // Banner Penguncian Pengajuan (sama seperti LoanScreen)
        if (hasPendingApplication) {
            item {
                ApplicationLockBanner()
            }
        }

        // Card 1: Jumlah Pengajuan
        item {
            JumlahPengajuanCard(
                amount = amount,
                minAmount = minLimit,
                maxAmount = maxLimit,
                quickAmounts = quickAmounts,
                isPlafondZero = isPlafondZero,
                userTier = userTier,
                onAmountChange = { amount = it }
            )
        }

        // Card 2: Pilih Tenor Pinjaman
        item {
            PilihTenorCard(
                selectedTenor = selectedTenor,
                availableTenors = availableTenors,
                onTenorSelected = { selectedTenor = it }
            )
        }

        // Card 3: Estimasi & Breakdown Detail
        item {
            EstimasiDetailCard(
                amount = amount.toLong(),
                monthlyInstallment = monthlyInstallment.toLong(),
                rateText = interestRateFormatted
            )
        }

        // CTA Button & Info Persetujuan
        item {
            ActionCtaSection(
                onSubmit = {
                    if (isLoggedIn) {
                        val limitToPass = activePlafond?.availableAmount ?: amount.toLong().toBigDecimal()
                        onNavigateToLoanApplication(limitToPass)
                        onConfirmLoan("LOAN-${System.currentTimeMillis()}")
                    } else {
                        onNavigateToLogin()
                    }
                },
                hasPendingApplication = hasPendingApplication,
                isPlafondZero = isPlafondZero,
                isLoggedIn = isLoggedIn
            )
        }
    }
}

// ==========================================
// APPLICATION LOCK BANNER (SAMA PERSIS DENGAN LOAN SCREEN)
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

@Composable
private fun JumlahPengajuanCard(
    amount: Float,
    minAmount: Float,
    maxAmount: Float,
    quickAmounts: List<Float>,
    isPlafondZero: Boolean,
    userTier: String?,
    onAmountChange: (Float) -> Unit
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
                .padding(Spacing.xl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Jumlah Pengajuan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                        if (userTier != null) {
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Surface(
                                shape = CircleShape,
                                color = extendedColors.accentSoft
                            ) {
                                Text(
                                    text = userTier.replace("TIER_", "Tier "),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.electricViolet,
                                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isPlafondZero) "Plafond aktif telah terpakai seluruhnya" else "Tentukan nominal dalam batas plafon aktif",
                        fontSize = 12.sp,
                        color = if (isPlafondZero) extendedColors.dangerMain else extendedColors.textMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(Radius.md),
                    color = extendedColors.accentSoft,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Wallet,
                            contentDescription = null,
                            tint = extendedColors.electricViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.xl),
                color = extendedColors.canvasBackground.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.lg, horizontal = Spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NOMINAL PINJAMAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.electricViolet,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = extendedColors.surfaceCard,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(enabled = !isPlafondZero && amount > minAmount) {
                                    onAmountChange((amount - 1_000_000f).coerceAtLeast(minAmount))
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Kurang",
                                    tint = if (!isPlafondZero && amount > minAmount) extendedColors.deepCharcoal else extendedColors.textMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Rp",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.deepCharcoal
                            )
                            Text(
                                text = formatRupiah(amount.toLong()),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = extendedColors.deepCharcoal
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (!isPlafondZero && amount < maxAmount) extendedColors.deepCharcoal else extendedColors.surfaceCard,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(enabled = !isPlafondZero && amount < maxAmount) {
                                    onAmountChange((amount + 1_000_000f).coerceAtMost(maxAmount))
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah",
                                    tint = if (!isPlafondZero && amount < maxAmount) Color.White else extendedColors.textMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            val stepsCount = ((maxAmount - minAmount) / 1_000_000f).toInt().coerceAtLeast(0) - 1
            Slider(
                value = amount.coerceIn(minAmount, maxAmount),
                onValueChange = { onAmountChange(it) },
                valueRange = minAmount..maxAmount,
                steps = if (stepsCount > 0) stepsCount else 0,
                enabled = !isPlafondZero && maxAmount > minAmount,
                colors = SliderDefaults.colors(
                    thumbColor = extendedColors.electricViolet,
                    activeTrackColor = extendedColors.electricViolet,
                    inactiveTrackColor = extendedColors.accentSoft
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Min Rp ${formatRupiah(minAmount.toLong())}",
                    fontSize = 11.sp,
                    color = extendedColors.textMuted
                )
                Text(
                    text = "Maks Rp ${formatRupiah(maxAmount.toLong())}",
                    fontSize = 11.sp,
                    color = extendedColors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                quickAmounts.forEach { value ->
                    val isSelected = amount == value
                    val label = if (value >= 1_000_000f) {
                        val millions = value / 1_000_000f
                        if (millions % 1f == 0f) "${millions.toInt()} Juta" else String.format(Locale.US, "%.1f Jt", millions)
                    } else {
                        "${(value / 1_000f).toInt()} Rb"
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable(enabled = !isPlafondZero) { onAmountChange(value) },
                        shape = CircleShape,
                        color = if (isSelected) extendedColors.electricViolet else extendedColors.canvasBackground
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = Spacing.sm + Spacing.xxs)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else extendedColors.deepCharcoal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PilihTenorCard(
    selectedTenor: Int,
    availableTenors: List<Int>,
    onTenorSelected: (Int) -> Unit
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
                .padding(Spacing.xl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pilih Tenor Pinjaman",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )
                    Text(
                        text = "Fleksibilitas pembayaran bulanan",
                        fontSize = 12.sp,
                        color = extendedColors.textMuted
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = extendedColors.accentSoft
                ) {
                    Text(
                        text = "Cicilan Tetap",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.electricViolet,
                        modifier = Modifier.padding(horizontal = Spacing.sm + Spacing.xxs, vertical = Spacing.xxs)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            val chunkedTenors = availableTenors.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xxs)) {
                chunkedTenors.forEach { rowTenors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xxs)
                    ) {
                        rowTenors.forEach { tenor ->
                            val isSelected = tenor == selectedTenor

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .clickable { onTenorSelected(tenor) },
                                shape = CircleShape,
                                color = if (isSelected) extendedColors.deepCharcoal else extendedColors.canvasBackground
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = Spacing.md)
                                ) {
                                    Text(
                                        text = "$tenor Bulan",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else extendedColors.deepCharcoal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstimasiDetailCard(
    amount: Long,
    monthlyInstallment: Long,
    rateText: String
) {
    val extendedColors = BekalTheme.extendedColors
    val todayFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID")))
    }

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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    extendedColors.electricViolet,
                                    extendedColors.electricViolet.copy(alpha = 0.75f)
                                )
                            )
                        )
                        .padding(Spacing.lg + Spacing.xxs)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimasi Cicilan Bulanan",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Transparan",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(
                                        horizontal = Spacing.sm + Spacing.xxs,
                                        vertical = Spacing.xxs
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "Rp ${formatRupiah(monthlyInstallment)}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = " /bulan",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = Spacing.xxs)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.xxs))

                        Text(
                            text = "Sudah mencakup pokok & bunga bulanan",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            DetailRow(label = "Pokok Pinjaman", value = "Rp ${formatRupiah(amount)}")

            DetailRowWithBadge(
                label = "Suku Bunga",
                value = rateText,
                icon = Icons.Default.CheckCircle
            )

            DetailRowBadge(
                label = "Biaya Provisi / Admin",
                badgeText = "Bebas Biaya (Rp 0)"
            )

            DetailRow(label = "Jatuh Tempo Pertama", value = todayFormatted)

            Spacer(modifier = Modifier.height(Spacing.lg))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                color = extendedColors.canvasBackground.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md + Spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(Spacing.sm))

                    Column {
                        Text(
                            text = "Berizin & Diawasi oleh OJK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                        Text(
                            text = "Terdaftar AFPI • Standar Keamanan ISO 27001",
                            fontSize = 10.sp,
                            color = extendedColors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCtaSection(
    onSubmit: () -> Unit,
    hasPendingApplication: Boolean,
    isPlafondZero: Boolean,
    isLoggedIn: Boolean
) {
    val extendedColors = BekalTheme.extendedColors
    val isEnabled = !hasPendingApplication && (!isLoggedIn || !isPlafondZero)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onSubmit,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = extendedColors.deepCharcoal,
                contentColor = Color.White,
                disabledContainerColor = extendedColors.textMuted.copy(alpha = 0.2f),
                disabledContentColor = extendedColors.textMuted
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when {
                        hasPendingApplication -> "Pengajuan Sedang Diproses"
                        isPlafondZero -> "Limit Plafon Habis"
                        !isLoggedIn -> "Masuk untuk Mengajukan Pinjaman"
                        else -> "Lanjutkan Pengajuan Pinjaman"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isEnabled) {
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = if (hasPendingApplication) {
                "Selesaikan verifikasi pengajuan aktif sebelum mengajukan kembali"
            } else {
                "Persetujuan instan dalam 15 menit melalui e-KTP valid"
            },
            fontSize = 11.sp,
            color = extendedColors.textMuted
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, isBold: Boolean = false) {
    val extendedColors = BekalTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs + Spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = extendedColors.textMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = extendedColors.deepCharcoal
        )
    }
}

@Composable
private fun DetailRowWithBadge(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val extendedColors = BekalTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs + Spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = extendedColors.textMuted
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = extendedColors.deepCharcoal
            )
            Spacer(modifier = Modifier.width(Spacing.xxs))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = extendedColors.electricViolet,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun DetailRowBadge(label: String, badgeText: String) {
    val extendedColors = BekalTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs + Spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = extendedColors.textMuted
        )
        Surface(
            shape = CircleShape,
            color = extendedColors.accentSoft
        ) {
            Text(
                text = badgeText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.electricViolet,
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
            )
        }
    }
}

private fun formatRupiah(number: Long): String {
    val formatter = NumberFormat.getInstance(Locale("id", "ID"))
    return formatter.format(number)
}