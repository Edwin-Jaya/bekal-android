package com.edwin.bekal.presentation.simulation

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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
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

    // Auth State Check
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)

    // Interactive State
    var amount by remember { mutableFloatStateOf(25_000_000f) }
    var selectedTenor by remember { mutableIntStateOf(12) }

    // Calculations
    val dailyInterestRate = 0.001f // 0.1% per hari
    val totalInterest = amount * dailyInterestRate * 30 * selectedTenor
    val totalRepayment = amount + totalInterest
    val monthlyInstallment = totalRepayment / selectedTenor

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

        // Card 1: Jumlah Pengajuan
        item {
            JumlahPengajuanCard(
                amount = amount,
                onAmountChange = { amount = it }
            )
        }

        // Card 2: Pilih Tenor Pinjaman
        item {
            PilihTenorCard(
                selectedTenor = selectedTenor,
                onTenorSelected = { selectedTenor = it }
            )
        }

        // Card 3: Estimasi & Breakdown Detail
        item {
            EstimasiDetailCard(
                amount = amount.toLong(),
                monthlyInstallment = monthlyInstallment.toLong()
            )
        }

        // CTA Button & Info Persetujuan
        item {
            ActionCtaSection(
                onSubmit = {
                    if (isLoggedIn) {
                        val selectedPlafond = amount.toLong().toBigDecimal()
                        onNavigateToLoanApplication(selectedPlafond)
                        onConfirmLoan("LOAN-${System.currentTimeMillis()}")
                    } else {
                        onNavigateToLogin()
                    }
                }
            )
        }
    }
}

@Composable
private fun JumlahPengajuanCard(
    amount: Float,
    onAmountChange: (Float) -> Unit
) {
    val extendedColors = BekalTheme.extendedColors
    val quickAmounts = listOf(10_000_000f, 25_000_000f, 35_000_000f, 50_000_000f)

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
                        text = "Jumlah Pengajuan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )
                    Text(
                        text = "Tentukan plafon yang dibutuhkan",
                        fontSize = 12.sp,
                        color = extendedColors.textMuted
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
                                .clickable {
                                    if (amount > 1_000_000f) onAmountChange(amount - 1_000_000f)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Kurang",
                                    tint = extendedColors.deepCharcoal,
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
                            color = extendedColors.deepCharcoal,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (amount < 50_000_000f) onAmountChange(amount + 1_000_000f)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Slider(
                value = amount,
                onValueChange = { onAmountChange(it) },
                valueRange = 1_000_000f..50_000_000f,
                steps = 48,
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
                    text = "Min Rp 1.000.000",
                    fontSize = 11.sp,
                    color = extendedColors.textMuted
                )
                Text(
                    text = "Maks Rp 50.000.000",
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
                    val label = "${(value / 1_000_000).toInt()} Juta"

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable { onAmountChange(value) },
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
    onTenorSelected: (Int) -> Unit
) {
    val extendedColors = BekalTheme.extendedColors
    val tenors = listOf(6, 8, 12, 16, 20, 24)

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

            val chunkedTenors = tenors.chunked(3)
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
    monthlyInstallment: Long
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
                            text = "Sudah mencakup pokok & bunga harian",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            DetailRow(label = "Pokok Pinjaman", value = "Rp ${formatRupiah(amount)}")

            DetailRowWithBadge(
                label = "Suku Bunga Harian",
                value = "0.1% / hari",
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
    onSubmit: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = extendedColors.deepCharcoal,
                contentColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Lanjutkan Pengajuan Pinjaman",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = "Persetujuan instan dalam 15 menit melalui e-KTP valid",
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