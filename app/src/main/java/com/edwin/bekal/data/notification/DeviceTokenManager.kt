package com.edwin.bekal.data.notification

import android.os.Build
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.dto.RegisterTokenRequest
import com.edwin.bekal.data.notification.remote.NotificationApi
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTokenManager @Inject constructor(
    private val notificationApi: NotificationApi,
    private val authRepository: AuthRepository
) {
    suspend fun syncFcmToken(token: String? = null) {
        runCatching {
            // 1. Ambil FCM token (jika null, ambil dari Firebase instance)
            val currentToken = token ?: FirebaseMessaging.getInstance().token.await()

            // 2. Ambil ID Customer dari Session
            val customerId = authRepository.observeSession().first()?.user?.id ?: return@runCatching

            // 3. Ambil Info Perangkat (contoh: Samsung SM-G998B - Android 14)
            val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"

            // 4. Kirim ke Backend
            val request = RegisterTokenRequest(
                customerId = customerId,
                fcmToken = currentToken,
                deviceInfo = deviceInfo
            )
            notificationApi.registerDeviceToken(request)
        }
    }
}