package com.edwin.bekal.core.network

import com.edwin.bekal.core.error.CommonFailure
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403

/** Menjalankan panggilan API dan menerjemahkan exception jaringan menjadi [CommonFailure]. */
suspend fun <T> runApiCatching(
    json: Json,
    block: suspend () -> AppResult<T>,
): AppResult<T> = try {
    block()
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (http: HttpException) {
    AppResult.failure(http.toFailure(json))
} catch (io: IOException) {
    AppResult.failure(CommonFailure.Network(io))
} catch (throwable: Throwable) {
    AppResult.failure(CommonFailure.Unexpected(throwable))
}

/** Mengambil payload envelope, atau melaporkan error bila server mengirimkan success = false. */
fun <T> ApiEnvelope<T>.requirePayload(): AppResult<T> {
    if (!success) {
        return AppResult.failure(
            CommonFailure.ApiError(
                code = status.toString(),
                details = listOf(message)
            )
        )
    }

    val payload = data
        ?: return AppResult.failure(
            CommonFailure.ApiError(
                code = status.toString(),
                details = listOf(message)
            )
        )

    return AppResult.success(payload)
}

private fun HttpException.toFailure(json: Json): CommonFailure {
    if (code() == HTTP_UNAUTHORIZED || code() == HTTP_FORBIDDEN) return CommonFailure.Unauthorized

    val body = runCatching { response()?.errorBody()?.string() }.getOrNull()
        ?: return CommonFailure.Unexpected(this)

    val parsed = runCatching {
        json.decodeFromString(ApiEnvelope.serializer(Unit.serializer()), body)
    }.getOrNull()

    if (parsed != null) {
        return CommonFailure.ApiError(
            code = parsed.status.toString(),
            details = listOf(parsed.message)
        )
    }

    return CommonFailure.Unexpected(this)
}