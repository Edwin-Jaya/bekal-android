package com.edwin.bekal.data.home

import com.edwin.bekal.core.database.dao.HomeDashboardDao
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.core.network.map
import com.edwin.bekal.core.network.requirePayload
import com.edwin.bekal.core.network.runApiCatching
import com.edwin.bekal.data.dto.HomeDashboardData
import com.edwin.bekal.data.dto.toDomain
import com.edwin.bekal.data.mapper.toDomain
import com.edwin.bekal.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val homeApi: HomeApi,
    private val homeDashboardDao: HomeDashboardDao,
    private val json: Json
) {
    // 1. Observe data dari Room DB sebagai Flow untuk UI (Offline View)
    val homeDashboardStream: Flow<HomeDashboardData?> =
        homeDashboardDao.getHomeDashboard().map { entity ->
            entity?.toDomain()
        }

    // 2. Fetch data dari API & simpan ke Room DB jika berhasil
    suspend fun fetchDashboard(customerId: String? = null): AppResult<HomeDashboardData> {
        val result = runApiCatching(json) {
            homeApi.getDashboard(customerId)
                .requirePayload()
        }

        if (result is AppResult.Success) {
            // Simpan payload DTO terbaru ke Room Database
            homeDashboardDao.insertHomeDashboard(result.data.toEntity())
        }

        // Return data dalam bentuk domain
        return result.map { it.toDomain() }
    }

    // 3. Helper untuk menghapus cache saat user logout
    suspend fun clearCache() {
        homeDashboardDao.clearHomeDashboard()
    }
}