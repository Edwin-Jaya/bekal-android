package com.edwin.bekal.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.edwin.bekal.presentation.loan.LoansScreen
import com.edwin.bekal.presentation.simulation.SimulationScreen
import com.edwin.bekal.ui.theme.BekalTheme
import java.math.BigDecimal

@Composable
fun MainScreen(
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToLoanApplication: (plafondId: String, plafondLimit: BigDecimal) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()

    // 1. Observe real state from AuthViewModel
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn = authUiState.status == AuthStatus.AUTHENTICATED

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
                onTabSelected = { targetTab ->
                    // 2. Prevent navigation if unauthenticated
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
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomTabRoute.Home,
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {
            composable<BottomTabRoute.Home> {
                HomeScreen(
                    onNavigateToSimulation = {
                        bottomNavController.navigate(BottomTabRoute.Simulation) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToDetail = onNavigateToDetail
                )
            }

            composable<BottomTabRoute.Simulation> {
                SimulationScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onConfirmLoan = onNavigateToDetail
                )
            }

            composable<BottomTabRoute.Loans> {
                LoansScreen(
                    onNavigateToApply = { plafondId, plafondLimit ->
                        onNavigateToLoanApplication(plafondId, plafondLimit)
                    }
                )
            }

            // 3. Clean composable without LaunchedEffect redirect loops
            composable<BottomTabRoute.Account> {
                AccountScreen(
                    onLogoutClick = {
                        authViewModel.logout()
                        onNavigateToLogin()
                    }
                )
            }
        }
    }
}