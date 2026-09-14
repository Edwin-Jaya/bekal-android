package com.edwin.bekal.navigation

import kotlinx.serialization.Serializable

// Rute Tingkat Atas (Root Navigation)
sealed interface RootRoute {
    @Serializable data object Login : RootRoute

    @Serializable
    data object Register : RootRoute
    @Serializable data object MainContainer : RootRoute
    @Serializable data class LoanDetail(val loanId: String) : RootRoute

    // Form pengajuan pinjaman (fullscreen, seperti Register)
    @Serializable data class LoanApplication(val plafondId: String, val plafondLimit: String) : RootRoute
}

// Rute Internal Bottom Navigation
sealed interface BottomTabRoute {
    @Serializable data object Home : BottomTabRoute
    @Serializable data object Simulation : BottomTabRoute
    @Serializable data object Loans : BottomTabRoute
    @Serializable data object Account : BottomTabRoute
}