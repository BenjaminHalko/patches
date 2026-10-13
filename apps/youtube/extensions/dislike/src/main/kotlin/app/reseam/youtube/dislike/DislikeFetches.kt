// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

import app.reseam.bundle.Logger
import app.reseam.youtube.core.debug

internal const val CACHE_TIMEOUT_SUCCESS_MILLISECONDS = 7 * 60 * 1000L
internal const val CACHE_TIMEOUT_FAILURE_MILLISECONDS = 3 * 60 * 1000L
private val fetchCache = hashMapOf<String, ReturnYouTubeDislike>()

internal fun getCachedDislikes(
    videoId: String,
    create: () -> ReturnYouTubeDislike,
): ReturnYouTubeDislike =
    synchronized(fetchCache) {
        val now = System.currentTimeMillis()
        fetchCache.values.removeIf { data ->
            val expired = data.isExpired(now)
            if (expired) Logger.debug { "Removing expired fetch: ${data.videoId}" }
            expired
        }
        fetchCache.getOrPut(videoId, create)
    }
