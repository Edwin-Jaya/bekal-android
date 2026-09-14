package com.edwin.bekal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.edwin.bekal.presentation.auth.LoginScreen
import com.edwin.bekal.presentation.auth.register.RegisterScreen
import com.edwin.bekal.presentation.loan.LoanApplicationScreen
import com.edwin.bekal.presentation.main.MainScreen
import java.math.BigDecimal

@Composable
fun RootNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: RootRoute = RootRoute.MainContainer
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 1. Container Utama dengan Bottom Navigation
        composable<RootRoute.MainContainer> {
            MainScreen(
                onNavigateToDetail = { loanId ->
                    navController.navigate(RootRoute.LoanDetail(loanId))
                },
                onNavigateToLogin = {
                    navController.navigate(RootRoute.Login)
                },
                onNavigateToLoanApplication = { plafondId, plafondLimit ->
                    navController.navigate(
                        RootRoute.LoanApplication(
                            plafondId = plafondId,
                            plafondLimit = plafondLimit.toPlainString()
                        )
                    )
                }
            )
        }

        // 2. Standalone Screen: Login
        composable<RootRoute.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(RootRoute.MainContainer) {
                        popUpTo<RootRoute.Login> { inclusive = true }
                    }
                },
                onForgotPasswordClick = { },
                onNavigateToRegister = {
                    navController.navigate(RootRoute.Register)
                },
                onGoogleLoginClick = { }
            )
        }

        // 3. Standalone Screen: Register (Form 4 Step Fullscreen)
        composable<RootRoute.Register> {
            RegisterScreen(
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Standalone Screen: Detail Pinjaman
        composable<RootRoute.LoanDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<RootRoute.LoanDetail>()
            // LoanDetailScreen(loanId = args.loanId)
        }

        // 5. Standalone Screen: Form Pengajuan Pinjaman (Fullscreen)
        composable<RootRoute.LoanApplication> { backStackEntry ->
            val args = backStackEntry.toRoute<RootRoute.LoanApplication>()
            LoanApplicationScreen(
                plafondId = args.plafondId,
                plafondLimit = BigDecimal(args.plafondLimit),
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}