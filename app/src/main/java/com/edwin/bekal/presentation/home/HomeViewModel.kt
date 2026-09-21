package com.edwin.bekal.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwin.bekal.core.error.AppFailure
import com.edwin.bekal.core.network.onFailure
import com.edwin.bekal.core.network.onSuccess
import com.edwin.bekal.data.dto.HomeDashboardData
import com.edwin.bekal.data.home.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val data: HomeDashboardData,
        val isRefreshing: Boolean = false
    ) : HomeUiState
    data class Error(val failure: AppFailure) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Observe cache Room DB secara real-time
        observeLocalDashboard()
    }

    private fun observeLocalDashboard() {
        viewModelScope.launch {
            homeRepository.homeDashboardStream.collectLatest { cachedData ->
                if (cachedData != null) {
                    val currentState = _uiState.value
                    val isRefreshing = if (currentState is HomeUiState.Success) currentState.isRefreshing else false

                    _uiState.value = HomeUiState.Success(
                        data = cachedData,
                        isRefreshing = isRefreshing
                    )
                }
            }
        }
    }

    /**
     * Memuat data dashboard.
     * @param customerId ID Pelanggan (opsional)
     * @param isPullToRefresh True jika dipanggil dari gesture swipe-to-refresh
     * @param isSilentRefresh True jika dipanggil saat ON_RESUME (layar fokus kembali) agar tidak flicker loading
     */
    fun loadDashboard(
        customerId: String? = null,
        isPullToRefresh: Boolean = false,
        isSilentRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value

            // Atur status loading sesuai kondisi
            when {
                isPullToRefresh && currentState is HomeUiState.Success -> {
                    _uiState.value = currentState.copy(isRefreshing = true)
                }
                !isSilentRefresh && currentState !is HomeUiState.Success -> {
                    _uiState.value = HomeUiState.Loading
                }
                else -> {
                    // Silent refresh: biarkan UI menampilkan data lama selagi fetch data baru
                }
            }

            homeRepository.fetchDashboard(customerId)
                .onSuccess { newData ->
                    _uiState.value = HomeUiState.Success(
                        data = newData,
                        isRefreshing = false
                    )
                }
                .onFailure { failure ->
                    // Jika silent refresh/pull-to-refresh atau sudah ada data lokal (Success), pertahankan data lama
                    if (currentState is HomeUiState.Success && (isSilentRefresh || isPullToRefresh)) {
                        _uiState.value = currentState.copy(isRefreshing = false)
                    } else if (_uiState.value is HomeUiState.Success) {
                        // Jika fetch gagal (e.g. Offline) tapi DB lokal sudah merender data, tahan di Success state
                    } else {
                        _uiState.value = HomeUiState.Error(failure)
                    }
                }
        }
    }
}