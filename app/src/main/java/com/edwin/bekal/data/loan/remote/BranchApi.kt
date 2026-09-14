package com.edwin.bekal.data.loan.remote

import com.edwin.bekal.data.dto.BranchResponse
import retrofit2.http.GET

interface BranchApi {
    @GET("api/v1/branches/active")
    suspend fun getActiveBranches(): List<BranchResponse>
}