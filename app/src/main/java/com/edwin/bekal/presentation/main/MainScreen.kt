package com.edwin.bekal.presentation.main

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.edwin.bekal.navigation.BottomTabRoute
import com.edwin.bekal.presentation.account.AccountScreen
import com.edwin.bekal.presentation.auth.AuthStatus
import com.edwin.bekal.presentation.auth.AuthViewModel
import com.edwin.bekal.presentation.components.BekalBottomNavigation
import com.edwin.bekal.presentation.home.HomeScreen
import com.edwin.bekal.presentation.loan.LoanHistoryUiState
import com.edwin.bekal.presentation.loan.LoanApplicationViewModel
import com.edwin.bekal.presentation.loan.LoansScreen
import com.edwin.bekal.presentation.loan.PlafondUiState
import com.edwin.bekal.presentation.simulation.SimulationScreen
import com.edwin.bekal.ui.theme.BekalTheme
import java.math.BigDecimal

@Composable
fun MainScreen(
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToLoanApplication: (plafondLimit: BigDecimal) -> Unit = { _ -> },
    onNavigateToPayment: (String) -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToPaymentGuide: (virtualAccountNumber: String) -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    modifier: Modifier = Modifier,
    loanApplicationViewModel: LoanApplicationViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()

    // 1. Observe state login
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn = authUiState.status == AuthStatus.AUTHENTICATED

    val plafondState by loanApplicationViewModel.plafondState.collectAsStateWithLifecycle()
    val historyState by loanApplicationViewModel.historyState.collectAsStateWithLifecycle()

    // Evaluasi status pengajuan aktif sesuai referensi dari LoansScreen
    val hasActiveApplication = (historyState as? LoanHistoryUiState.Success)
        ?.applications
        ?.any { application ->
            application.status.lowercase() in listOf(
                "in_review", "in_approval", "in_disbursement", "pending", "under_review", "diproses", "disbursed"
            )
        } == true

    // 2. Request Notification Permission & Muat Data saat Login
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted -> }

        LaunchedEffect(isLoggedIn) {
            if (isLoggedIn) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                loanApplicationViewModel.loadActivePlafond()
                loanApplicationViewModel.loadLoanHistory()
            }
        }
    } else {
        LaunchedEffect(isLoggedIn) {
            if (isLoggedIn) {
                loanApplicationViewModel.loadActivePlafond()
                loanApplicationViewModel.loadLoanHistory()
            }
        }
    }

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentTab: BottomTabRoute = when {
        currentDestination?.hasRoute<BottomTabRoute.Simulation>() == true -> BottomTabRoute.Simulation
        currentDestination?.hasRoute<BottomTabRoute.Loans>() == true -> BottomTabRoute.Loans
        currentDestination?.hasRoute<BottomTabRoute.Account>() == true -> BottomTabRoute.Account
        else -> BottomTabRoute.Home
    }

    Scaffold(
        modifier = modifier,
        containerColor = BekalTheme.extendedColors.canvasBackground,
        bottomBar = {
            BekalBottomNavigation(
                currentScreen = currentTab,
                hasActiveApplication = hasActiveApplication,
                onTabSelected = { targetTab ->
                    if (targetTab == BottomTabRoute.Account && !isLoggedIn) {
                        onNavigateToLogin()
                        return@BekalBottomNavigation
                    }

                    bottomNavController.navigate(targetTab) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onApplyClick = {
                    if (!isLoggedIn) {
                        onNavigateToLogin()
                    } else if (hasActiveApplication) {
                        bottomNavController.navigate(BottomTabRoute.Loans) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        val currentLimit = (plafondState as? PlafondUiState.Success)?.plafond?.availableAmount
                            ?: BigDecimal.ZERO
                        onNavigateToLoanApplication(currentLimit)
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomTabRoute.Home,
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {
            // Tab 1: Home
            composable<BottomTabRoute.Home> {
                HomeScreen(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToSimulation = {
                        bottomNavController.navigate(BottomTabRoute.Simulation) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToHistory = {
                        bottomNavController.navigate(BottomTabRoute.Loans) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToPaymentGuide = {
                        onNavigateToPaymentGuide("8808012345678901")
                    },
                    onNavigateToHelp = onNavigateToFaq,
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToPayment = onNavigateToPayment,
                    onNavigateToEditProfile = onNavigateToEditProfile
                )
            }

            // Tab 2: Simulation
            composable<BottomTabRoute.Simulation> {
                SimulationScreen(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToLoanApplication = onNavigateToLoanApplication
                )
            }

            // Tab 3: Loans
            composable<BottomTabRoute.Loans> {
                LoansScreen(
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToPayment = onNavigateToPayment,
                    onNavigateToLoanApplication = onNavigateToLoanApplication,
                    onNavigateToLogin = onNavigateToLogin
                )
            }

            // Tab 4: Account
            composable<BottomTabRoute.Account> {
                AccountScreen(
                    onLogoutClick = {
                        bottomNavController.navigate(BottomTabRoute.Home) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToFaq = onNavigateToFaq
                )
            }
        }
    }
}