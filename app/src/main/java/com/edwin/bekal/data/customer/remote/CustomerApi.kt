package com.edwin.bekal.data.customer.remote

import com.edwin.bekal.core.network.ApiEnvelope
import com.edwin.bekal.data.dto.CustomerProfileDto
import retrofit2.http.GET

interface CustomerApi {

    @GET("api/v1/customers/me")
    suspend fun getProfile(): ApiEnvelope<CustomerProfileDto>

}