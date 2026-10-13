// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

@Volatile internal var currentVideoData: ReturnYouTubeDislike? = null
@Volatile internal var lastLithoShortsVideoData: ReturnYouTubeDislike? = null
private val shortsVideoMonitor = Any()
private var useLithoShortsVideoDataCount = 0
@Volatile internal var lastPrefetchedVideoId: String? = null

internal fun clearData() {
    currentVideoData = null
    lastLithoShortsVideoData = null
    synchronized(shortsVideoMonitor) { useLithoShortsVideoDataCount = 0 }
}

internal fun useLithoShortsVideoTwice() {
    synchronized(shortsVideoMonitor) { useLithoShortsVideoDataCount = 2 }
}

internal fun decrementUseLithoDataIfNeeded(): Boolean =
    synchronized(shortsVideoMonitor) {
        if (useLithoShortsVideoDataCount > 0) {
            useLithoShortsVideoDataCount--
            true
        } else false
    }

@Volatile internal var rollingNumberSpan: CharSequence? = null
