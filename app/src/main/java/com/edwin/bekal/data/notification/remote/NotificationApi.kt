package com.edwin.bekal.data.notification.remote

import com.edwin.bekal.data.dto.RegisterTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApi {
    @POST("api/v1/notifications/register-token")
    suspend fun registerDeviceToken(
        @Body request: RegisterTokenRequest
    ): Response<Unit>
}