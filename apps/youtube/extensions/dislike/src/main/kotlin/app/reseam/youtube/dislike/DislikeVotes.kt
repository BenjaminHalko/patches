// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

import app.reseam.bundle.Logger
import app.reseam.youtube.core.debug
import app.reseam.youtube.core.showToast
import app.reseam.youtube.dislike.ReturnYouTubeDislike.Vote
import app.reseam.youtube.dislike.requests.sendVote
import app.reseam.youtube.player.PlayerType
import app.reseam.youtube.settings.YouTubeStrings
import java.util.concurrent.Executors

internal object DislikeVotes {
    private val executor = Executors.newSingleThreadExecutor()

    fun send(videoId: String, vote: Vote, isShort: () -> Boolean, applyVote: () -> Unit) {
        verifyOnMainThread()
        try {
            val type = PlayerType.current
            if (isShort() != type.isNoneHiddenOrMinimized) {
                Logger.debug {
                    "Cannot vote for video: $videoId as current player type does not match: $type"
                }
                showToast(YouTubeStrings.rydReopenBeforeVoting)
                return
            }
            applyVote()
            executor.execute {
                try {
                    sendVote(videoId, vote)
                } catch (ex: Exception) {
                    Logger.error(ex) { "Failed to send vote" }
                }
            }
        } catch (ex: Exception) {
            Logger.error(ex) { "Error trying to send vote" }
        }
    }
}
