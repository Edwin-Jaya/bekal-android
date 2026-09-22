package com.edwin.bekal.presentation.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwin.bekal.R
import com.edwin.bekal.core.error.AppFailure
import com.edwin.bekal.core.error.CommonFailure
import com.edwin.bekal.data.dto.HomeDashboardData
import com.edwin.bekal.presentation.auth.AuthViewModel
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HomeLoanState {
    PRE_APPLICATION,
    IN_REVIEW,
    ACTIVE_REPAYMENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSimulation: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPaymentGuide: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToPayment: (String) -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val customerId = authUiState.user?.id

    val isAuthenticated = authUiState.user != null
    val userName = authUiState.user?.name ?: "User"

    if (!isAuthenticated) {
        GuestHomeScreenContent(
            onLoginClick = onNavigateToLogin,
            onNavigateToSimulation = onNavigateToSimulation,
            modifier = modifier
        )
    } else {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    homeViewModel.loadDashboard(customerId = customerId, isSilentRefresh = true)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        when (val state = homeUiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BekalTheme.extendedColors.electricViolet)
                }
            }

            is HomeUiState.Error -> {
                ErrorSection(
                    failure = state.failure,
                    onRetry = { homeViewModel.loadDashboard() },
                    modifier = modifier
                )
            }

            is HomeUiState.Success -> {
                val dashboard = state.data

                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { homeViewModel.loadDashboard(customerId = customerId, isPullToRefresh = true) },
                    modifier = modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                FluidWaveHeaderBackground()

                                Column {
                                    HeaderSection(
                                        userName = userName,
                                        onProfileClick = onNavigateToEditProfile,
                                        modifier = Modifier.padding(
                                            top = Spacing.xl,
                                            start = Spacing.xl,
                                            end = Spacing.xl,
                                            bottom = Spacing.xxl
                                        )
                                    )

                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = Spacing.xl)
                                            .offset(y = (-Spacing.xs))
                                    ) {
                                        DynamicHeroCard(
                                            dashboardData = dashboard,
                                            onApplyClick = onNavigateToHistory,
                                            onCheckDetailClick = {
                                                onNavigateToHistory()
                                            },
                                            onPayClick = { onNavigateToPayment(dashboard.activeLoanId ?: "") },
                                            onInstallmentDetailClick = { onNavigateToDetail("RINCIAN-CICILAN") }
                                        )
                                    }
                                }
                            }
                        }

                        if (dashboard.loanState == HomeLoanState.IN_REVIEW) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                                    ApplicationLockBanner()
                                }
                            }
                        }

                        item {
                            Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                                QuickMenuGrid(
                                    onSimulationClick = onNavigateToSimulation,
                                    onHistoryClick = onNavigateToHistory,
                                    onPaymentGuideClick = onNavigateToPaymentGuide,
                                    onHelpClick = onNavigateToHelp
                                )
                            }
                        }

                        if (dashboard.loanState == HomeLoanState.IN_REVIEW) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                                    HelpAndEscalationCard(onContactCsClick = onNavigateToHelp)
                                }
                            }
                        }

                        if (dashboard.loanState != HomeLoanState.IN_REVIEW) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                                    GamificationProgressTracker(
                                        currentTier = dashboard.tierNumber,
                                        targetLimit = dashboard.maxLimit.toRupiahFormat(),
                                        remainingLoansCount = dashboard.requiredForNextTier
                                    )
                                }
                            }
                        }

                        item {
                            Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                                EducationalCarousel(isInReview = dashboard.loanState == HomeLoanState.IN_REVIEW)
                            }
                        }

                        item {
                            Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                                OjkComplianceFooter()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FluidWaveHeaderBackground(modifier: Modifier = Modifier) {
    val extendedColors = BekalTheme.extendedColors

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
    ) {
        val width = size.width
        val height = size.height

        val primaryPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height * 0.45f)
            cubicTo(
                width * 0.75f, height * 0.85f,
                width * 0.35f, height * 0.25f,
                0f, height * 0.70f
            )
            close()
        }
        drawPath(
            path = primaryPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    extendedColors.electricViolet,
                    extendedColors.electricViolet.copy(alpha = 0.85f)
                )
            )
        )

        val accentPath = Path().apply {
            moveTo(width * 0.40f, 0f)
            lineTo(width, 0f)
            lineTo(width, height * 0.55f)
            cubicTo(
                width * 0.80f, height * 0.90f,
                width * 0.60f, height * 0.40f,
                width * 0.40f, 0f
            )
            close()
        }
        drawPath(
            path = accentPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    extendedColors.warningMain.copy(alpha = 0.9f),
                    extendedColors.warningMain.copy(alpha = 0.3f)
                )
            )
        )
    }
}

@Composable
private fun GuestHomeScreenContent(
    onLoginClick: () -> Unit,
    onNavigateToSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xl)
            ) {
                FluidWaveHeaderBackground()

                Column {
                    GuestHeaderSection(
                        onLoginClick = onLoginClick,
                        modifier = Modifier.padding(
                            top = Spacing.xl,
                            start = Spacing.xl,
                            end = Spacing.xl,
                            bottom = Spacing.xxl
                        )
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = Spacing.xl)
                            .offset(y = (-Spacing.xs))
                    ) {
                        GuestHeroCard(onCheckLimitClick = onLoginClick)
                    }
                }
            }
        }

        item {
            Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                GuestAutoSlidingBanner()
            }
        }
        item {
            Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                GuestBentoGridSection(
                    onNavigateToSimulation = onNavigateToSimulation,
                    onLoginClick = onLoginClick
                )
            }
        }
        item {
            Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                ThreeStepsFlowSection()
            }
        }
        item {
            Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                EducationalCarousel()
            }
        }
        item {
            Box(modifier = Modifier.padding(horizontal = Spacing.xl)) {
                GuestTrustAndSecuritySection()
            }
        }
    }
}

@Composable
private fun GuestHeaderSection(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "BEKAL",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = extendedColors.deepCharcoal
            )
            Text(
                text = "setiap langkah, ada bekal",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = extendedColors.deepCharcoal
            )
        }

        OutlinedButton(
            onClick = onLoginClick,
            shape = RoundedCornerShape(Radius.lg),
            border = BorderStroke(1.5.dp, extendedColors.deepCharcoal),
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs)
        ) {
            Text(
                text = "Masuk / Daftar",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal
            )
        }
    }
}

@Composable
private fun GuestHeroCard(onCheckLimitClick: () -> Unit) {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            extendedColors.accentSoft.copy(alpha = 0.6f),
                            Color.White
                        )
                    )
                )
                .padding(Spacing.xl)
        ) {
            Column {
                Surface(
                    shape = CircleShape,
                    color = extendedColors.electricViolet
                ) {
                    Text(
                        text = "Spesial Pengguna Baru",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = "Dapatkan Limit Siap Cair s/d",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.textMuted
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Rp 20.000.000",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = extendedColors.deepCharcoal
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "Tanpa agunan • Langsung cair ke rekening pribadi",
                    fontSize = 11.sp,
                    color = extendedColors.textMuted,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                Button(
                    onClick = onCheckLimitClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
                ) {
                    Text(
                        text = "Cek Limit Saya",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun GuestAutoSlidingBanner() {
    val extendedColors = BekalTheme.extendedColors

    val banners = remember {
        listOf(
            PromoBannerItem(
                imageRes = R.drawable.banner_promo_1,
                contentDescription = "Promo Bunga Rendah"
            ),
            PromoBannerItem(
                imageRes = R.drawable.banner_promo_2,
                contentDescription = "Dampak Bekal Bagi Ekonomi"
            ),
            PromoBannerItem(
                imageRes = R.drawable.banner_promo_3,
                contentDescription = "Daftar Sekarang"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(3500L)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = Spacing.md
        ) { page ->
            val banner = banners[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
            ) {
                Image(
                    painter = painterResource(id = banner.imageRes),
                    contentDescription = banner.contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width = if (isSelected) 16.dp else 6.dp
                val color = if (isSelected) extendedColors.electricViolet else extendedColors.textMuted.copy(alpha = 0.25f)

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .height(6.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

private data class PromoBannerItem(
    @DrawableRes val imageRes: Int,
    val contentDescription: String
)

@Composable
private fun GuestBentoGridSection(
    onNavigateToSimulation: () -> Unit,
    onLoginClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(
            text = "Layanan Unggulan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = extendedColors.deepCharcoal
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToSimulation() },
            shape = RoundedCornerShape(Radius.xl),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(Radius.lg),
                        color = extendedColors.accentSoft,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(Spacing.md))

                    Column {
                        Text(
                            text = "Hitung Estimasi Pinjaman",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Simulasi cicilan transparan & bunga 0.1%",
                            fontSize = 10.5.sp,
                            color = extendedColors.textMuted
                        )
                    }
                }

                Button(
                    onClick = onNavigateToSimulation,
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet),
                    contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs)
                ) {
                    Text(
                        text = "Hitung Sekarang",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .clickable { onLoginClick() },
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Persetujuan Instan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                        Text(
                            text = "Verifikasi KTP Praktis",
                            fontSize = 9.5.sp,
                            color = extendedColors.textMuted
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .clickable { onLoginClick() },
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.successSoft,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = extendedColors.successMain,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Bunga Transparan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                        Text(
                            text = "Tanpa Biaya Tersembunyi",
                            fontSize = 9.5.sp,
                            color = extendedColors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreeStepsFlowSection() {
    val extendedColors = BekalTheme.extendedColors

    val steps = listOf(
        Triple("1. Isi Data", "Siapkan KTP & info singkat", Icons.Default.PersonAdd),
        Triple("2. Verifikasi", "Pemeriksaan otomatis", Icons.Default.Speed),
        Triple("3. Dana Cair", "Langsung masuk rekening", Icons.Default.AccountBalanceWallet)
    )

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = "Cara Mudah Dapatkan Dana",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = extendedColors.deepCharcoal
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            steps.forEach { (title, desc, icon) ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(115.dp),
                    shape = RoundedCornerShape(Radius.xl),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.md),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = extendedColors.accentSoft,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = extendedColors.electricViolet,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.deepCharcoal,
                                lineHeight = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontSize = 9.sp,
                                color = extendedColors.textMuted,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onProfileClick() }) {
            Surface(
                shape = CircleShape,
                color = extendedColors.deepCharcoal,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column {
                Text(
                    text = "Halo, $userName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.deepCharcoal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = CircleShape,
                    color = extendedColors.successSoft
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = extendedColors.successMain,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Terverifikasi",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.successMain
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicHeroCard(
    dashboardData: HomeDashboardData,
    onApplyClick: () -> Unit,
    onCheckDetailClick: () -> Unit,
    onPayClick: () -> Unit,
    onInstallmentDetailClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    val cardModifier = if (dashboardData.loanState == HomeLoanState.IN_REVIEW) {
        Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = extendedColors.warningMain.copy(alpha = alphaAnim),
                shape = RoundedCornerShape(Radius.xl)
            )
    } else {
        Modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            when (dashboardData.loanState) {
                HomeLoanState.PRE_APPLICATION -> ConditionACard(
                    availableLimit = dashboardData.availableLimit,
                    tierNumber = dashboardData.tierNumber,
                    onApplyClick = onApplyClick
                )

                HomeLoanState.IN_REVIEW -> {
                    if (dashboardData.rawStatus.uppercase().contains("APPROVED") ||
                        dashboardData.rawStatus.uppercase().contains("TRANSFER")) {
                        ConditionBApprovedVariantCard(
                            amount = dashboardData.availableLimit,
                            bankInfo = "BCA •••• 5678",
                            onCheckDetailClick = onCheckDetailClick
                        )
                    } else {
                        ConditionBCard(
                            amountRequested = dashboardData.availableLimit,
                            tenorMonths = 12,
                            onCheckDetailClick = onCheckDetailClick
                        )
                    }
                }

                HomeLoanState.ACTIVE_REPAYMENT -> ConditionCCard(
                    activeBillAmount = dashboardData.activeBillAmount,
                    dueDate = dashboardData.dueDate,
                    onPayClick = onPayClick,
                    onInstallmentDetailClick = onInstallmentDetailClick
                )
            }
        }
    }
}

@Composable
private fun ConditionBCard(
    amountRequested: BigDecimal,
    tenorMonths: Int,
    onCheckDetailClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Pengajuan Sedang Diproses",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = extendedColors.deepCharcoal
        )
        Surface(
            shape = CircleShape,
            color = extendedColors.warningSoft
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    strokeWidth = 1.5.dp,
                    color = extendedColors.warningMain
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Verifikasi",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.warningMain
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(Spacing.sm))

    Text(
        text = amountRequested.toRupiahFormat(),
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = extendedColors.deepCharcoal
    )
    Text(
        text = "Tenor $tenorMonths Bulan",
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = extendedColors.textMuted
    )

    Spacer(modifier = Modifier.height(Spacing.md))

    VisualStepperSection(currentStep = 2)

    Spacer(modifier = Modifier.height(Spacing.md))

    Button(
        onClick = onCheckDetailClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(Radius.lg),
        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
    ) {
        Text(
            text = "Cek Detail Progress",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ConditionBApprovedVariantCard(
    amount: BigDecimal,
    bankInfo: String,
    onCheckDetailClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Pengajuan Disetujui",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = extendedColors.deepCharcoal
        )
        Surface(
            shape = CircleShape,
            color = extendedColors.successSoft
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = extendedColors.successMain,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Proses Transfer",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.successMain
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(Spacing.xs))

    Text(
        text = amount.toRupiahFormat(),
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = extendedColors.deepCharcoal
    )

    Spacer(modifier = Modifier.height(2.dp))

    Text(
        text = "Dana sedang dikirimkan ke $bankInfo via BI-FAST.",
        fontSize = 11.sp,
        color = extendedColors.textMuted
    )

    Spacer(modifier = Modifier.height(Spacing.md))

    Button(
        onClick = onCheckDetailClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(Radius.lg),
        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
    ) {
        Text(
            text = "Lihat Detail Rekening",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun VisualStepperSection(currentStep: Int) {
    val extendedColors = BekalTheme.extendedColors
    val steps = listOf("Dikirim", "Analisis", "Pencairan")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isCompleted = stepNumber < currentStep
            val isActive = stepNumber == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (index == 0) Color.Transparent
                                else if (isCompleted || isActive) extendedColors.successMain
                                else extendedColors.textMuted.copy(alpha = 0.2f)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = when {
                                    isCompleted -> extendedColors.successMain
                                    isActive -> extendedColors.electricViolet
                                    else -> extendedColors.textMuted.copy(alpha = 0.2f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        } else if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color.White, CircleShape)
                            )
                        } else {
                            Text(
                                text = "$stepNumber",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.textMuted
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (index == steps.lastIndex) Color.Transparent
                                else if (isCompleted) extendedColors.successMain
                                else extendedColors.textMuted.copy(alpha = 0.2f)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xxs))

                Text(
                    text = title,
                    fontSize = 9.5.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) extendedColors.deepCharcoal else extendedColors.textMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

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
private fun HelpAndEscalationCard(onContactCsClick: () -> Unit) {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = extendedColors.accentSoft,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = null,
                            tint = extendedColors.electricViolet,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.sm))

                Text(
                    text = "Butuh bantuan verifikasi?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.deepCharcoal
                )
            }

            OutlinedButton(
                onClick = onContactCsClick,
                shape = RoundedCornerShape(Radius.md),
                border = BorderStroke(1.dp, extendedColors.electricViolet),
                contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = Spacing.xxs)
            ) {
                Text(
                    text = "Hubungi CS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.electricViolet
                )
            }
        }
    }
}

@Composable
private fun ConditionACard(
    availableLimit: BigDecimal,
    tierNumber: Int,
    onApplyClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Limit Siap Cair",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = extendedColors.textMuted
        )
        Surface(
            shape = CircleShape,
            color = extendedColors.accentSoft
        ) {
            Text(
                text = "Tier $tierNumber",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.electricViolet,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.sm))

    Text(
        text = availableLimit.toRupiahFormat(),
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        color = extendedColors.deepCharcoal
    )

    Spacer(modifier = Modifier.height(Spacing.lg))

    Button(
        onClick = onApplyClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(Radius.lg),
        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
    ) {
        Text(
            text = "Cairkan Ke Rekening",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    Spacer(modifier = Modifier.height(Spacing.md))

    Surface(
        shape = RoundedCornerShape(Radius.md),
        color = extendedColors.canvasBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = extendedColors.electricViolet,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = "Bayar tepat waktu untuk membuka kenaikan limit ke Tier berikutnya!",
                fontSize = 11.sp,
                color = extendedColors.deepCharcoal,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ConditionCCard(
    activeBillAmount: BigDecimal,
    dueDate: String?,
    onPayClick: () -> Unit,
    onInstallmentDetailClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tagihan Bulan Ini",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = extendedColors.textMuted
        )
        Surface(
            shape = CircleShape,
            color = extendedColors.accentSoft
        ) {
            Text(
                text = "Pinjaman Aktif",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.electricViolet,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.xs))

    Text(
        text = activeBillAmount.toRupiahFormat(),
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        color = extendedColors.deepCharcoal
    )

    if (!dueDate.isNullOrEmpty()) {
        Surface(
            shape = RoundedCornerShape(Radius.sm),
            color = extendedColors.dangerSoft,
            modifier = Modifier.padding(top = Spacing.xs)
        ) {
            Text(
                text = "Jatuh Tempo: ${dueDate.toIndonesianDateFormat()}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.dangerMain,
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.lg))

    Button(
        onClick = onPayClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(Radius.lg),
        colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
    ) {
        Text(
            text = "Bayar Sekarang",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }

    Spacer(modifier = Modifier.height(Spacing.md))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInstallmentDetailClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Text(
//            text = "Lihat Rincian Cicilan",
//            fontSize = 12.sp,
//            fontWeight = FontWeight.SemiBold,
//            color = extendedColors.electricViolet
//        )
//        Icon(
//            imageVector = Icons.Default.ChevronRight,
//            contentDescription = null,
//            tint = extendedColors.electricViolet,
//            modifier = Modifier.size(16.dp)
//        )
    }
}

@Composable
private fun QuickMenuGrid(
    onSimulationClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onPaymentGuideClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    val items = listOf(
        QuickMenuItem("Simulasi", Icons.Default.Calculate, onSimulationClick),
        QuickMenuItem("Riwayat", Icons.Default.History, onHistoryClick),
        QuickMenuItem("Cara Bayar", Icons.Default.CreditCard, onPaymentGuideClick),
        QuickMenuItem("Bantuan", Icons.Default.HelpOutline, onHelpClick)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.md))
                    .clickable { item.onClick() }
                    .padding(Spacing.xs)
            ) {
                Surface(
                    shape = RoundedCornerShape(Radius.lg),
                    color = Color.White,
                    border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f)),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = extendedColors.electricViolet,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = item.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.deepCharcoal
                )
            }
        }
    }
}

private data class QuickMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun GamificationProgressTracker(
    currentTier: Int,
    targetLimit: String,
    remainingLoansCount: Int
) {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress Level Tiering",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.deepCharcoal
                )
                Text(
                    text = "Tier $currentTier dari 3",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.electricViolet
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TierNode(label = "Tier 1", isReached = currentTier >= 1)
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (currentTier >= 2) extendedColors.electricViolet else extendedColors.accentSoft)
                )
                TierNode(label = "Tier 2", isReached = currentTier >= 2)
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (currentTier >= 3) extendedColors.electricViolet else extendedColors.accentSoft)
                )
                TierNode(label = "Tier 3", isReached = currentTier >= 3)
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Surface(
                shape = RoundedCornerShape(Radius.md),
                color = extendedColors.accentSoft,
                modifier = Modifier.fillMaxWidth()
            ) {
                val statusText = if (currentTier < 3) {
                    "Selesaikan $remainingLoansCount pinjaman lagi untuk naik ke Tier ${currentTier + 1} ($targetLimit)."
                } else {
                    "Selamat! Anda telah mencapai Tier tertinggi dengan limit maksimal $targetLimit."
                }

                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = extendedColors.electricViolet,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
                )
            }
        }
    }
}

@Composable
private fun TierNode(label: String, isReached: Boolean) {
    val extendedColors = BekalTheme.extendedColors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (isReached) extendedColors.electricViolet else extendedColors.accentSoft,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isReached) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isReached) FontWeight.Bold else FontWeight.Normal,
            color = if (isReached) extendedColors.deepCharcoal else extendedColors.textMuted
        )
    }
}

@Composable
private fun EducationalCarousel(isInReview: Boolean = false) {
    val extendedColors = BekalTheme.extendedColors

    val banners = remember(isInReview) {
        if (isInReview) {
            listOf(
                EducationalBanner(
                    title = "Cara Kerja Verifikasi Otomatis",
                    description = "Engine memproses DSR dan skor kredit Anda secara otomatis.",
                    icon = Icons.Default.Speed,
                    bgColor = extendedColors.accentSoft,
                    tintColor = extendedColors.electricViolet
                ),
                EducationalBanner(
                    title = "Keamanan Data Terjamin",
                    description = "Aplikasi terdaftar dan diawasi resmi oleh OJK.",
                    icon = Icons.Default.Shield,
                    bgColor = extendedColors.successSoft,
                    tintColor = extendedColors.successMain
                )
            )
        } else {
            listOf(
                EducationalBanner(
                    title = "Tips Cepat Disetujui",
                    description = "Pastikan KTP & rekening bank atas nama pribadi match 100%.",
                    icon = Icons.Default.Lightbulb,
                    bgColor = extendedColors.accentSoft,
                    tintColor = extendedColors.electricViolet
                ),
                EducationalBanner(
                    title = "Keamanan Data Terjamin",
                    description = "Aplikasi terdaftar dan diawasi resmi oleh OJK.",
                    icon = Icons.Default.Shield,
                    bgColor = extendedColors.successSoft,
                    tintColor = extendedColors.successMain
                )
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = "Edukasi & Informasi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = extendedColors.deepCharcoal
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = PaddingValues(end = Spacing.sm)
        ) {
            items(banners) { banner ->
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .height(100.dp),
                    shape = RoundedCornerShape(Radius.xl),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(Radius.md),
                            color = banner.bgColor,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = banner.icon,
                                    contentDescription = null,
                                    tint = banner.tintColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(Spacing.md))

                        Column {
                            Text(
                                text = banner.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.deepCharcoal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = banner.description,
                                fontSize = 10.sp,
                                color = extendedColors.textMuted,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class EducationalBanner(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val bgColor: Color,
    val tintColor: Color
)

@Composable
private fun GuestTrustAndSecuritySection() {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = extendedColors.electricViolet,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "Keamanan & Regulasi Resmi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.deepCharcoal
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComplianceBadge(text = "OJK", subText = "Berizin & Diawasi")
                ComplianceBadge(text = "AFPI", subText = "Anggota Resmi")
                ComplianceBadge(text = "ISO 27001", subText = "Keamanan Data")
            }

            Spacer(modifier = Modifier.height(Spacing.md))
            HorizontalDivider(color = extendedColors.textMuted.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = extendedColors.successMain,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Data Anda dilindungi dengan enkripsi AES 256-bit kelas perbankan.",
                    fontSize = 9.5.sp,
                    color = extendedColors.textMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ComplianceBadge(text: String, subText: String) {
    val extendedColors = BekalTheme.extendedColors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(Radius.sm),
            color = extendedColors.canvasBackground,
            border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f))
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = extendedColors.deepCharcoal
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subText,
            fontSize = 8.5.sp,
            color = extendedColors.textMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorSection(
    failure: AppFailure,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors

    val message = when (failure) {
        is CommonFailure.Network -> "Koneksi internet terputus. Periksa jaringan Anda."
        is CommonFailure.ApiError -> failure.details.firstOrNull() ?: "Terjadi kesalahan pada server."
        is CommonFailure.Unauthorized -> "Sesi berakhir. Silakan login kembali."
        else -> "Terjadi kesalahan tidak terduga."
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(Spacing.xl)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = extendedColors.dangerMain,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = message,
                fontSize = 14.sp,
                color = extendedColors.deepCharcoal
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = extendedColors.electricViolet)
            ) {
                Text("Coba Lagi", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OjkComplianceFooter() {
    val extendedColors = BekalTheme.extendedColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = extendedColors.electricViolet,
                modifier = Modifier.size(Spacing.lg)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Berizin & Diawasi oleh OJK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xxs))
        Text(
            text = "Terdaftar Asosiasi Fintech Pendanaan Bersama Indonesia (AFPI)",
            fontSize = 10.sp,
            color = extendedColors.textMuted
        )
    }
}

fun BigDecimal.toRupiahFormat(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(this).replace("Rp", "Rp ").replace(",00", "")
}

fun String.toIndonesianDateFormat(): String {
    return try {
        val parsedDate = LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        parsedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID")))
    } catch (e: Exception) {
        this
    }
}