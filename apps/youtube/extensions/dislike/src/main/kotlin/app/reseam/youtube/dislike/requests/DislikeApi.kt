// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike.requests

import android.widget.Toast
import app.reseam.bundle.Logger
import app.reseam.bundle.openHttp
import app.reseam.bundle.send
import app.reseam.youtube.core.TextResponse
import app.reseam.youtube.core.debug
import app.reseam.youtube.core.readResponseText
import app.reseam.youtube.core.showToast
import app.reseam.youtube.dislike.verifyOffMainThread
import app.reseam.youtube.settings.VideoSettings
import app.reseam.youtube.settings.YouTubeStrings
import java.io.IOException
import java.net.SocketTimeoutException

private const val API_URL = "https://returnyoutubedislikeapi.com/"
private const val RATE_LIMIT_BACKOFF_MILLISECONDS = 10 * 60 * 1000L
private const val CONNECTION_ERROR_BACKOFF_MILLISECONDS = 2 * 60 * 1000L

@Volatile private var resumeApiCallsAt = 0L
@Volatile private var lastCallFailed = false

internal fun isRateLimited(endpoint: String): Boolean {
    if (System.currentTimeMillis() >= resumeApiCallsAt) return false
    Logger.debug { "Ignoring api call $endpoint as rate limit is in effect" }
    return true
}

internal fun backOff(rateLimited: Boolean) {
    if (rateLimited && !lastCallFailed && VideoSettings.rydToastOnConnectionError.value)
        showToast(YouTubeStrings.rydRateLimited)
    val backoff =
        if (rateLimited) RATE_LIMIT_BACKOFF_MILLISECONDS else CONNECTION_ERROR_BACKOFF_MILLISECONDS
    resumeApiCallsAt = System.currentTimeMillis() + backoff
    lastCallFailed = true
}

internal fun markSuccess() {
    lastCallFailed = false
}

internal fun request(
    method: String,
    path: String,
    body: String? = null,
    readBody: Boolean = true,
    connectTimeout: Int = 60_000,
    readTimeout: Int = 60_000,
): TextResponse =
    openHttp(API_URL + path) {
        requestMethod = method
        this.connectTimeout = connectTimeout
        this.readTimeout = readTimeout
        useCaches = false
        setRequestProperty("User-Agent", "Reseam/YouTube")
        setRequestProperty("Accept", "application/json")
        setRequestProperty("Pragma", "no-cache")
        setRequestProperty("Cache-Control", "no-cache")
        if (body != null) {
            setRequestProperty("Content-Type", "application/json")
            send(body.encodeToByteArray())
        }
        val status = responseCode
        TextResponse(
            status,
            if (status == 200 && readBody) readResponseText(1_048_576, "RYD response is too large")
            else null,
        )
    }

/** Runs [onOk] for HTTP 200; a rate limit yields null, other statuses are reported. */
internal inline fun <T> TextResponse.ifOk(onOk: (TextResponse) -> T?): T? =
    when (status) {
        200 -> onOk(this)
        429 -> null
        else -> {
            reportConnectionError(
                YouTubeStrings.rydHttpError(status.toString()),
                status,
                null,
                Toast.LENGTH_LONG,
            )
            null
        }
    }

internal inline fun <T> apiCall(
    ioError: (IOException) -> String,
    crossinline failure: () -> String,
    block: () -> T,
): T? {
    verifyOffMainThread()
    return try {
        block()
    } catch (ex: SocketTimeoutException) {
        reportConnectionError(YouTubeStrings.rydConnectionTimeout, null, ex, Toast.LENGTH_SHORT)
        null
    } catch (ex: IOException) {
        reportConnectionError(ioError(ex), null, ex, Toast.LENGTH_LONG)
        null
    } catch (ex: Exception) {
        Logger.error(ex) { failure() }
        null
    }
}

internal fun reportConnectionError(
    message: String,
    status: Int?,
    ex: Exception?,
    toastDuration: Int,
) {
    // 401 is an API authorization error the user cannot act on.
    if (!lastCallFailed && status != 401 && VideoSettings.rydToastOnConnectionError.value)
        showToast(message, toastDuration)
    lastCallFailed = true
    Logger.info(ex) { message }
}
