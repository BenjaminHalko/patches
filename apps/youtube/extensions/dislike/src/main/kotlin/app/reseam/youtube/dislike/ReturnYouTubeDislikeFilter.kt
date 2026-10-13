// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

import app.reseam.bundle.Logger
import app.reseam.runtime.Export
import app.reseam.youtube.core.debug
import app.reseam.youtube.litho.Filter
import app.reseam.youtube.litho.FilterGroup.ByteArrayFilterGroup
import app.reseam.youtube.litho.FilterGroup.StringFilterGroup
import app.reseam.youtube.litho.FilterGroupList.ByteArrayFilterGroupList
import app.reseam.youtube.litho.LithoComponent
import app.reseam.youtube.settings.VideoSettings

@Export
class ReturnYouTubeDislikeFilter : Filter() {
    private val videoIdFilterGroup =
        ByteArrayFilterGroupList().apply {
            addAll(
                ByteArrayFilterGroup(
                    null,
                    "id.reel_like_button",
                    "id.reel_dislike_button",
                    "ic_right_like",
                    "ic_right_dislike",
                )
            )
        }

    init {
        addPathCallbacks(StringFilterGroup(null, "|like_button.e"))
        addPathCallbacks(
            StringFilterGroup(
                null,
                "shorts_like_button.e",
                "reel_like_button.e",
                "reel_like_toggled_button.e",
                "shorts_dislike_button.e",
                "reel_dislike_button.e",
                "reel_dislike_toggled_button.e",
            )
        )
    }

    override fun isFiltered(component: LithoComponent, group: StringFilterGroup): Boolean {
        if (component.path.startsWith("compactify_video_action_bar.e")) {
            observeNativeLikes(component.path, component.accessibility)
            return false
        }
        if (!VideoSettings.rydEnabled.value || !VideoSettings.rydShorts.value) return false
        // No ID means an incognito Short, so stale data from the previous one is cleared.
        if (videoIdFilterGroup.check(component.buffer).isFiltered)
            onLithoShortVideo(findVideoId(component.buffer))
        return false
    }

    private fun findVideoId(buffer: ByteArray): String? =
        synchronized(lastVideoIds) {
            lastVideoIds.entries
                .firstOrNull { (id, matcher) -> id.isEmpty() || matcher.check(buffer).isFiltered }
                ?.key
        }

    companion object {
        private val lastVideoIds = linkedMapOf<String, ByteArrayFilterGroup>()

        @Export
        @JvmStatic
        fun newPlayerResponseVideoId(videoId: String, isShortAndOpeningOrPlaying: Boolean) {
            try {
                if (!isShortAndOpeningOrPlaying) return
                if (!VideoSettings.rydEnabled.value || !VideoSettings.rydShorts.value) return
                synchronized(lastVideoIds) {
                    if (videoId !in lastVideoIds) {
                        val bytes = ByteArray(videoId.length) { videoId[it].code.toByte() }
                        lastVideoIds[videoId] = ByteArrayFilterGroup(null, bytes)
                        Logger.debug { "New Short video ID: $videoId" }
                    }
                    // Retain insertion order; matching must prefer the oldest still-cached ID.
                    if (lastVideoIds.size > 5) lastVideoIds.remove(lastVideoIds.keys.first())
                }
            } catch (ex: Exception) {
                Logger.error(ex) { "newPlayerResponseVideoId failure" }
            }
        }
    }
}
