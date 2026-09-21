package com.edwin.bekal.data.customer.remote

import com.edwin.bekal.core.network.ApiEnvelope
import com.edwin.bekal.data.dto.CustomerProfileDto
import com.edwin.bekal.data.dto.UpdateCustomerRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface CustomerApi {

    @GET("api/v1/customers/me")
    suspend fun getProfile(): ApiEnvelope<CustomerProfileDto>

    @PATCH("api/v1/customers/{id}")
    suspend fun updateProfile(
        @Path("id") id: String,
        @Body request: UpdateCustomerRequestDto
    ): ApiEnvelope<CustomerProfileDto>

}