package com.edwin.bekal.data.home

import com.edwin.bekal.core.network.ApiEnvelope
import com.edwin.bekal.data.dto.HomeDashboardResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApi {
    @GET("api/v1/home/dashboard")
    suspend fun getDashboard(
        @Query("customerId") customerId: String? = null
    ): ApiEnvelope<HomeDashboardResponseDto>

}