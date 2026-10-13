// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

@file:JvmName("DislikeVideoHooks")

package app.reseam.youtube.dislike

import app.reseam.bundle.Logger
import app.reseam.runtime.Export
import app.reseam.youtube.core.debug
import app.reseam.youtube.core.isNetworkConnected
import app.reseam.youtube.dislike.ReturnYouTubeDislike.Vote
import app.reseam.youtube.player.PlayerType
import app.reseam.youtube.settings.VideoSettings
import app.reseam.youtube.video.VideoInformation

@Export
fun preloadVideoId(videoId: String, isShortAndOpeningOrPlaying: Boolean) {
    try {
        if (!VideoSettings.rydEnabled.value) return
        if (videoId == lastPrefetchedVideoId) return
        if (!isNetworkConnected()) {
            Logger.debug { "Cannot pre-fetch RYD, network is not connected" }
            lastPrefetchedVideoId = null
            return
        }
        val skipShort =
            VideoInformation.lastPlayerResponseIsShort &&
                (!isShortAndOpeningOrPlaying || !VideoSettings.rydShorts.value)
        if (skipShort) return
        getFetchForVideoId(videoId)
        lastPrefetchedVideoId = videoId
    } catch (ex: Exception) {
        Logger.error(ex) { "preloadVideoId failure" }
    }
}

@Export
fun newVideoLoaded(videoId: String) {
    try {
        nativeDislikeLabels.newVideoLoaded(videoId)
        if (!VideoSettings.rydEnabled.value) return
        val type = PlayerType.current
        val minimized = type.isNoneHiddenOrSlidingMinimized
        if (minimized && !VideoSettings.rydShorts.value) {
            clearData()
            return
        }
        if (currentVideoData?.videoId == videoId) return
        Logger.debug { "New video ID: $videoId playerType: $type" }
        if (!isNetworkConnected()) {
            Logger.debug { "Cannot fetch RYD, network is not connected" }
            currentVideoData = null
            return
        }
        val data = getFetchForVideoId(videoId)
        if (minimized) data.setVideoIdIsShort(true)
        currentVideoData = data
    } catch (ex: Exception) {
        Logger.error(ex) { "newVideoLoaded failure" }
    }
}

internal fun onLithoShortVideo(videoId: String?) {
    if (lastLithoShortsVideoData?.videoId == videoId) return
    if (videoId == null) {
        Logger.debug { "Litho filter did not find any video IDs" }
        clearData()
        return
    }
    Logger.debug { "New litho Shorts video ID: $videoId" }
    val data = getFetchForVideoId(videoId)
    data.setVideoIdIsShort(true)
    lastLithoShortsVideoData = data
    useLithoShortsVideoTwice()
}

@Export
fun sendVote(vote: Int) {
    try {
        if (!VideoSettings.rydEnabled.value) return
        if (PlayerType.current.isNoneHiddenOrMinimized && !VideoSettings.rydShorts.value) return
        val data = currentVideoData
        if (data == null) {
            Logger.debug { "Cannot send vote, as current video data is null" }
            return
        }
        val choice =
            Vote.entries.firstOrNull { it.value == vote }
                ?: return Logger.error { "Unknown vote type: $vote" }
        data.sendVote(choice)
    } catch (ex: Exception) {
        Logger.error(ex) { "sendVote failure" }
    }
}
