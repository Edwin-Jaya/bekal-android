package com.edwin.bekal.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.edwin.bekal.BuildConfig
import com.edwin.bekal.presentation.account.edit.EditProfileScreen
import com.edwin.bekal.presentation.auth.AuthViewModel
import com.edwin.bekal.presentation.auth.LoginScreen
import com.edwin.bekal.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.edwin.bekal.presentation.auth.register.RegisterScreen
import com.edwin.bekal.presentation.auth.resetpassword.ResetPasswordScreen
import com.edwin.bekal.presentation.help.FaqScreen
import com.edwin.bekal.presentation.loan.LoanApplicationScreen
import com.edwin.bekal.presentation.loan.LoanDetailScreen
import com.edwin.bekal.presentation.loan.LoanRepaymentScreen
import com.edwin.bekal.presentation.main.MainScreen
import com.edwin.bekal.presentation.payment.CaraBayarScreen
import com.edwin.bekal.presentation.shared.sharedActivityViewModel
import com.edwin.bekal.presentation.splash.SplashScreen
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal

// ✅ String route constants untuk Google register flow
private const val REGISTER_WITH_EMAIL_ROUTE = "register?email={email}"
private fun registerWithEmailRoute(email: String) = "register?email=$email"

@Composable
fun RootNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: RootRoute = RootRoute.Splash
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val coroutineScope = rememberCoroutineScope()

    val credentialManager = remember(activity) {
        activity?.let { CredentialManager.create(it) }
    }

    val googleIdOption = remember {
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()
    }

    val credentialRequest = remember(googleIdOption) {
        GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable<RootRoute.Splash> {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(RootRoute.MainContainer) {
                        popUpTo<RootRoute.Splash> { inclusive = true }
                    }
                }
            )
        }

        composable<RootRoute.MainContainer> {
            MainScreen(
                onNavigateToDetail = { loanId ->
                    navController.navigate(RootRoute.LoanDetail(loanId))
                },
                onNavigateToLogin = {
                    navController.navigate(RootRoute.Login) {
                        launchSingleTop = true
                    }
                },
                onNavigateToLoanApplication = { plafondLimit ->
                    navController.navigate(
                        RootRoute.LoanApplication(plafondLimit = plafondLimit.toPlainString())
                    )
                },
                onNavigateToPayment = { loanId ->
                    navController.navigate(RootRoute.LoanRepayment(loanId))
                },
                onNavigateToEditProfile = { navController.navigate(RootRoute.EditProfile) },
                onNavigateToFaq = { navController.navigate(RootRoute.Faq) },
                onNavigateToPaymentGuide = { va ->
                    navController.navigate(RootRoute.CaraBayar(virtualAccountNumber = va))
                }
            )
        }

        composable<RootRoute.Login> {
            val authViewModel: AuthViewModel = sharedActivityViewModel()

            LoginScreen(
                viewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(RootRoute.MainContainer) {
                        popUpTo<RootRoute.Login> { inclusive = true }
                    }
                },
                // ✅ Cabang: email kosong = manual, ada email = dari Google
                onRegisterClick = { prefillEmail ->
                    if (prefillEmail.isBlank()) {
                        navController.navigate(RootRoute.Register)
                    } else {
                        navController.navigate(registerWithEmailRoute(prefillEmail))
                    }
                },
                onForgotPasswordClick = { navController.navigate("forgot_password") },
                onGoogleLoginClick = {
                    if (activity == null || credentialManager == null) {
                        Toast.makeText(
                            context,
                            "Gagal mendapatkan Activity",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@LoginScreen
                    }

                    handleGoogleSignIn(
                        coroutineScope = coroutineScope,
                        credentialManager = credentialManager,
                        credentialRequest = credentialRequest,
                        activity = activity,
                        context = context,
                        onSuccess = { idToken, email ->
                            authViewModel.loginWithGoogle(idToken = idToken, email = email)
                        }
                    )
                }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onOtpSent = { email -> navController.navigate("reset_password/$email") }
            )
        }

        composable(
            route = "reset_password/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) {
            ResetPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(RootRoute.Login) {
                        popUpTo<RootRoute.Login> { inclusive = true }
                    }
                }
            )
        }

        // ✅ Register manual — data object, tanpa prefillEmail
        composable<RootRoute.Register> {
            RegisterScreen(
                prefillEmail = "",
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }

        // ✅ Register dari Google — string route dengan email
        composable(
            route = REGISTER_WITH_EMAIL_ROUTE,
            arguments = listOf(navArgument("email") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            RegisterScreen(
                prefillEmail = backStackEntry.arguments?.getString("email") ?: "",
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }

        composable<RootRoute.LoanDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<RootRoute.LoanDetail>()
            LoanDetailScreen(
                loanId = args.loanId,
                onBackClick = { navController.popBackStack() },
                onNavigateToPayment = { loanId ->
                    navController.navigate(RootRoute.LoanRepayment(loanId))
                }
            )
        }

        composable<RootRoute.LoanApplication> { backStackEntry ->
            val args = backStackEntry.toRoute<RootRoute.LoanApplication>()
            LoanApplicationScreen(
                plafondLimit = BigDecimal(args.plafondLimit),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(RootRoute.Login) }
            )
        }

        composable<RootRoute.LoanRepayment> { backStackEntry ->
            val args = backStackEntry.toRoute<RootRoute.LoanRepayment>()
            LoanRepaymentScreen(
                loanId = args.loanId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<RootRoute.EditProfile> {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }

        composable<RootRoute.CaraBayar> { backStackEntry ->
            val args = backStackEntry.toRoute<RootRoute.CaraBayar>()
            CaraBayarScreen(
                virtualAccountNumber = args.virtualAccountNumber,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<RootRoute.Faq> {
            FaqScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

private fun handleGoogleSignIn(
    coroutineScope: CoroutineScope,
    credentialManager: CredentialManager,
    credentialRequest: GetCredentialRequest,
    activity: Activity,
    context: Context,
    onSuccess: (idToken: String, email: String) -> Unit
) {
    coroutineScope.launch {
        try {
            val result = credentialManager.getCredential(
                request = credentialRequest,
                context = activity
            )

            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                onSuccess(idToken, email)
            } else {
                Toast.makeText(
                    context,
                    "Tipe kredensial tidak didukung",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (e: GetCredentialCancellationException) {
            // Silent — user dismiss dialog

        } catch (e: NoCredentialException) {
            Toast.makeText(
                context,
                "Tidak ada akun Google ditemukan. Silakan tambahkan akun Google di pengaturan.",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: GetCredentialException) {
            Toast.makeText(
                context,
                "Google Sign-In gagal: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Terjadi kesalahan: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}