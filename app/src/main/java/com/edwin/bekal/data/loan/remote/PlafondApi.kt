package com.edwin.bekal.data.loan.remote

import com.edwin.bekal.data.dto.PlafondResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PlafondApi {
    @GET("api/v1/plafonds/customer/{customerId}/active")
    suspend fun getActivePlafond(@Path("customerId") customerId: String): PlafondResponse
}