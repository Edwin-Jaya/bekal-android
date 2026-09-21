package com.edwin.bekal.presentation.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
fun LoanDetailScreen(
    loanId: String,
    onBackClick: () -> Unit,
    onNavigateToPayment: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LoanDetailViewModel = hiltViewModel()
) {
    val extendedColors = BekalTheme.extendedColors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(loanId) {
        viewModel.loadLoanDetail(loanId)
    }

    Scaffold(
        modifier = modifier,
        containerColor = extendedColors.canvasBackground,
        topBar = {
            Surface(
                color = extendedColors.canvasBackground,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = extendedColors.deepCharcoal
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Rincian Pinjaman",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )
                }
            }
        },
        bottomBar = {
            val application = (uiState as? LoanDetailUiState.Success)?.loanDetail?.loanApplicationResponse
            if (application != null && (application.status.equals("APPROVED", ignoreCase = true) || application.status.equals("DISBURSED", ignoreCase = true))) {
                Surface(
                    color = extendedColors.canvasBackground,
                    shadowElevation = Elevation.none,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Box(modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.md)) {
                        Button(
                            onClick = { onNavigateToPayment(application.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(Radius.lg),
                            colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
                        ) {
                            Text(
                                text = "Bayar Angsuran",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is LoanDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = extendedColors.electricViolet)
                }
            }

            is LoanDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(text = state.message, color = extendedColors.deepCharcoal)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Button(
                            onClick = { viewModel.loadLoanDetail(loanId) },
                            colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }

            is LoanDetailUiState.Success -> {
                val application = state.loanDetail.loanApplicationResponse

                if (application != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = padding.calculateTopPadding() + Spacing.xs,
                            start = Spacing.xl,
                            end = Spacing.xl,
                            bottom = padding.calculateBottomPadding() + Spacing.lg
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        item { LoanHeroStatusCard(data = application) }
                        item { LoanProgressTimelineCard(status = application.status) }
                        item { LoanBentoGrid(data = application) }
                    }
                }
            }
        }
    }
}

// ==========================================
// HERO STATUS CARD & STATUS CHIP
// ==========================================
@Composable
private fun LoanHeroStatusCard(data: LoanApplicationResponse) {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = extendedColors.electricViolet),
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
                        text = "No. Aplikasi",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = data.applicationNumber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                StatusChip(status = data.status)
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Jumlah Pinjaman",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(Spacing.xxs))
            Text(
                text = data.amountRequested.toRupiahFormat(),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "in_review" -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "Diproses")
        "review_rejected" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Ditolak")
        "in_approval" -> Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), "Menunggu Persetujuan")
        "approval_rejected" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Ditolak")
        "in_disbursement" -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), "Sedang Dicairkan")
        "disbursed" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Dicairkan")
        "closed" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Lunas")
        "cancelled" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Dibatalkan")
        else -> Triple(Color.White.copy(alpha = 0.2f), Color.White, status)
    }

    Surface(
        color = bgColor,
        shape = CircleShape,
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// ==========================================
// TIMELINE / STEPPER PROGRESS COMPONENT
// ==========================================
private enum class StepState { COMPLETED, IN_PROGRESS, PENDING, FAILED }

private data class TimelineStep(
    val title: String,
    val description: String,
    val state: StepState
)

@Composable
private fun LoanProgressTimelineCard(status: String) {
    val extendedColors = BekalTheme.extendedColors
    val steps = getTimelineSteps(status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = Spacing.md)
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = extendedColors.electricViolet,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "Progres Pengajuan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.deepCharcoal
                )
            }

            steps.forEachIndexed { index, step ->
                TimelineStepItem(
                    step = step,
                    isLast = index == steps.lastIndex
                )
            }
        }
    }
}

@Composable
private fun TimelineStepItem(
    step: TimelineStep,
    isLast: Boolean
) {
    val extendedColors = BekalTheme.extendedColors

    val (circleBgColor, iconColor, icon) = when (step.state) {
        StepState.COMPLETED -> Triple(extendedColors.electricViolet, Color.White, Icons.Default.Check)
        StepState.IN_PROGRESS -> Triple(extendedColors.accentSoft, extendedColors.electricViolet, null)
        StepState.PENDING -> Triple(extendedColors.canvasBackground, extendedColors.textMuted.copy(alpha = 0.4f), null)
        StepState.FAILED -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.Close)
    }

    val titleColor = when (step.state) {
        StepState.COMPLETED, StepState.IN_PROGRESS -> extendedColors.deepCharcoal
        StepState.FAILED -> Color(0xFFC62828)
        StepState.PENDING -> extendedColors.textMuted
    }

    val lineColor = if (step.state == StepState.COMPLETED) extendedColors.electricViolet else extendedColors.textMuted.copy(alpha = 0.2f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(circleBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                } else if (step.state == StepState.IN_PROGRESS) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(extendedColors.electricViolet)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(lineColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        Column(
            modifier = Modifier
                .padding(bottom = if (isLast) 0.dp else Spacing.lg)
                .weight(1f)
        ) {
            Text(
                text = step.title,
                fontSize = 13.sp,
                fontWeight = if (step.state == StepState.IN_PROGRESS || step.state == StepState.COMPLETED) FontWeight.Bold else FontWeight.Medium,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = step.description,
                fontSize = 11.sp,
                color = extendedColors.textMuted,
                lineHeight = 15.sp
            )
        }
    }
}

private fun getTimelineSteps(status: String?): List<TimelineStep> {
    val normalizedStatus = status.orEmpty().trim().lowercase()

    return when (normalizedStatus) {
        "in_review" -> listOf(
            TimelineStep("Pengajuan Diterima", "Aplikasi Anda telah berhasil dikirim.", StepState.COMPLETED),
            TimelineStep("Verifikasi & Analisis", "Tim kami sedang memeriksa kelengkapan data Anda.", StepState.IN_PROGRESS),
            TimelineStep("Persetujuan Pinjaman", "Menunggu keputusan analisis kredit.", StepState.PENDING),
            TimelineStep("Pencairan Dana", "Dana akan ditransfer ke rekening Anda.", StepState.PENDING)
        )
        "review_rejected" -> listOf(
            TimelineStep("Pengajuan Diterima", "Aplikasi Anda telah berhasil dikirim.", StepState.COMPLETED),
            TimelineStep("Verifikasi & Analisis", "Pengajuan belum memenuhi kriteria persyaratan.", StepState.FAILED),
            TimelineStep("Persetujuan Pinjaman", "Pengajuan ditolak pada tahap verifikasi.", StepState.PENDING),
            TimelineStep("Pencairan Dana", "Proses dibatalkan.", StepState.PENDING)
        )
        "in_approval" -> listOf(
            TimelineStep("Pengajuan Diterima", "Aplikasi Anda telah berhasil dikirim.", StepState.COMPLETED),
            TimelineStep("Verifikasi & Analisis", "Verifikasi data selesai dan telah lolos uji.", StepState.COMPLETED),
            TimelineStep("Persetujuan Pinjaman", "Sedang menunggu keputusan persetujuan akhir.", StepState.IN_PROGRESS),
            TimelineStep("Pencairan Dana", "Dana akan ditransfer setelah disetujui.", StepState.PENDING)
        )
        "approval_rejected" -> listOf(
            TimelineStep("Pengajuan Diterima", "Aplikasi Anda telah berhasil dikirim.", StepState.COMPLETED),
            TimelineStep("Verifikasi & Analisis", "Verifikasi data selesai dan telah lolos uji.", StepState.COMPLETED),
            TimelineStep("Persetujuan Pinjaman", "Pengajuan ditolak pada tahap persetujuan akhir.", StepState.FAILED),
            TimelineStep("Pencairan Dana", "Proses dibatalkan.", StepState.PENDING)
        )
        "in_disbursement" -> listOf(
            TimelineStep("Pengajuan Diterima", "Aplikasi Anda telah berhasil dikirim.", StepState.COMPLETED),
            TimelineStep("Verifikasi & Analisis", "Verifikasi data selesai dan telah lolos uji.", StepState.COMPLETED),
            TimelineStep("Persetujuan Pinjaman", "Pengajuan pinjaman Anda telah disetujui.", StepState.COMPLETED),
            TimelineStep("Pencairan Dana", "Dana sedang dalam proses transfer.", StepState.IN_PROGRESS)
        )
        "disbursed" -> listOf(
            TimelineStep("Pengajuan Diterima", "Aplikasi Anda telah berhasil dikirim.", StepState.COMPLETED),
            TimelineStep("Verifikasi & Analisis", "Verifikasi data selesai dan telah lolos uji.", StepState.COMPLETED),
            TimelineStep("Persetujuan Pinjaman", "Pengajuan pinjaman Anda telah disetujui.", StepState.COMPLETED),
            TimelineStep("Pencairan Dana", "Dana telah berhasil ditransfer ke rekening Anda.", StepState.COMPLETED)
        )
        "closed" -> listOf(
            TimelineStep("Pengajuan Diterima", "Aplikasi Anda telah berhasil dikirim.", StepState.COMPLETED),
            TimelineStep("Verifikasi & Analisis", "Verifikasi data selesai dan telah lolos uji.", StepState.COMPLETED),
            TimelineStep("Persetujuan Pinjaman", "Pengajuan pinjaman Anda telah disetujui.", StepState.COMPLETED),
            TimelineStep("Pencairan Dana", "Pinjaman telah lunas dan selesai.", StepState.COMPLETED)
        )
        "cancelled" -> listOf(
            TimelineStep("Pengajuan Diterima", "Aplikasi Anda telah berhasil dikirim.", StepState.COMPLETED),
            TimelineStep("Verifikasi & Analisis", "Proses dibatalkan oleh pengguna atau sistem.", StepState.FAILED),
            TimelineStep("Persetujuan Pinjaman", "Proses dibatalkan.", StepState.PENDING),
            TimelineStep("Pencairan Dana", "Proses dibatalkan.", StepState.PENDING)
        )
        else -> listOf(
            TimelineStep("Pengajuan Diterima", "Aplikasi telah berhasil dikirim.", StepState.COMPLETED),
            TimelineStep("Verifikasi & Analisis", "Sedang dalam proses evaluasi.", StepState.IN_PROGRESS),
            TimelineStep("Persetujuan Pinjaman", "Menunggu konfirmasi.", StepState.PENDING),
            TimelineStep("Pencairan Dana", "Menunggu pencairan.", StepState.PENDING)
        )
    }
}

// ==========================================
// BENTO GRID LAYOUT
// ==========================================
@Composable
private fun LoanBentoGrid(data: LoanApplicationResponse) {
    val extendedColors = BekalTheme.extendedColors

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BentoIconBadge(icon = Icons.Default.CreditCard, isHighlight = true)
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "Angsuran per Bulan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = extendedColors.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = data.monthlyInstallment.toRupiahFormat(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.electricViolet
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            BentoCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Column {
                    BentoIconBadge(icon = Icons.Default.ReceiptLong)
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = "Tenor Pinjaman",
                        fontSize = 11.sp,
                        color = extendedColors.textMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = "${data.tenorMonths} Bulan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )
                }
            }

            BentoCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Column {
                    BentoIconBadge(icon = Icons.Default.Percent)
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = "Suku Bunga",
                        fontSize = 11.sp,
                        color = extendedColors.textMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = "${data.interestRate}% / bln",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )
                }
            }
        }

        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BentoIconBadge(icon = Icons.Default.CalendarToday)
                Spacer(modifier = Modifier.width(Spacing.sm))
                Column {
                    Text(
                        text = "Tanggal Pengajuan",
                        fontSize = 11.sp,
                        color = extendedColors.textMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = data.submittedAt.orEmpty().take(10),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = extendedColors.deepCharcoal
                    )
                }
            }
        }

        if (!data.purpose.isNullOrBlank()) {
            BentoCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BentoIconBadge(icon = Icons.Default.Info)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Column {
                        Text(
                            text = "Tujuan Pinjaman",
                            fontSize = 11.sp,
                            color = extendedColors.textMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(Spacing.xxs))
                        Text(
                            text = data.purpose,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extendedColors.deepCharcoal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Box(modifier = Modifier.padding(Spacing.lg)) {
            content()
        }
    }
}

@Composable
private fun BentoIconBadge(
    icon: ImageVector,
    isHighlight: Boolean = false
) {
    val extendedColors = BekalTheme.extendedColors
    Surface(
        shape = CircleShape,
        color = if (isHighlight) extendedColors.accentSoft else extendedColors.canvasBackground,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlight) extendedColors.electricViolet else extendedColors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun BigDecimal?.toRupiahFormat(): String {
    if (this == null) return "Rp 0"
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(this).replace("Rp", "Rp ").replace(",00", "")
}