// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

import android.text.Spanned
import app.reseam.bundle.Logger
import app.reseam.youtube.core.debug
import app.reseam.youtube.dislike.requests.RYDVoteData
import app.reseam.youtube.dislike.requests.fetchVotes
import app.reseam.youtube.player.Event
import app.reseam.youtube.settings.VideoSettings

internal val dislikeChanges = Event<String>()

internal fun getFetchForVideoId(videoId: String): ReturnYouTubeDislike =
    getCachedDislikes(videoId) { ReturnYouTubeDislike(videoId) }

class ReturnYouTubeDislike internal constructor(val videoId: String) {
    @Volatile private var fetchedVotes: RYDVoteData? = null
    private var nativeLikeCount = -1L
    private val timeFetched = System.currentTimeMillis()
    @Volatile private var isShort = false
    private var userVote: Vote? = null
    private val spanCache = DislikeSpanCache()

    init {
        dislikeFetchExecutor.execute {
            fetchedVotes = fetchVotes(videoId)
            dislikeChanges.fire(videoId)
        }
    }

    val nativeLikeCountText: String?
        get() =
            synchronized(this) {
                if (nativeLikeCount < 0) null else formatCount(nativeLikeCount)
            }

    fun setNativeLikeCount(count: Long) {
        synchronized(this) {
            if (nativeLikeCount == count) return
            nativeLikeCount = count
        }
        dislikeChanges.fire(videoId)
    }

    val dislikeCountText: String?
        get() =
            synchronized(this) {
                fetchedVotes?.let { votes ->
                    userVote?.let(votes::updateUsingVote)
                    if (VideoSettings.rydDislikePercentage.value)
                        formatPercentage(votes.dislikePercentage)
                    else formatCount(votes.dislikeCount)
                }
            }

    internal fun isExpired(now: Long): Boolean {
        val elapsed = now - timeFetched
        return when {
            elapsed < CACHE_TIMEOUT_FAILURE_MILLISECONDS -> false
            elapsed > CACHE_TIMEOUT_SUCCESS_MILLISECONDS -> true
            else -> fetchedVotes == null
        }
    }

    @Synchronized
    fun setVideoIdIsShort(isShort: Boolean) {
        this.isShort = isShort
    }

    fun getDislikesSpanForRegularVideo(
        original: Spanned,
        isSegmentedButton: Boolean,
        isRollingNumber: Boolean,
    ): Spanned =
        updateReplacementSpan(original, regularSpanLayout(isSegmentedButton, isRollingNumber))

    fun getLikeSpanForShort(original: Spanned): Spanned =
        updateShortSpan(original, ::createLikesSpan)

    fun getDislikeSpanForShort(original: Spanned): Spanned =
        updateShortSpan(original) { source, votes ->
            spanCache.update(source, SpanLayout.DISLIKES, votes)
        }

    private fun updateReplacementSpan(original: Spanned, layout: SpanLayout): Spanned {
        if (layout.segmented && isPreviouslyCreatedSegmentedSpan(original.toString()))
            return original
        return updateSpan(original) { votes ->
            if (isShort) {
                Logger.debug {
                    "Ignoring regular video dislike span, as data loaded was previously used for a Short: $videoId"
                }
                original
            } else {
                userVote?.let(votes::updateUsingVote)
                spanCache.update(original, layout, votes)
            }
        }
    }

    private fun updateShortSpan(
        original: Spanned,
        create: (Spanned, RYDVoteData) -> Spanned,
    ): Spanned =
        updateSpan(original) { votes ->
            isShort = true
            userVote?.let(votes::updateUsingVote)
            create(original, votes)
        }

    private inline fun updateSpan(original: Spanned, create: (RYDVoteData) -> Spanned): Spanned =
        try {
            val votes = fetchedVotes
            if (votes == null) original
            else
                synchronized(this) {
                    create(votes)
                }
        } catch (ex: Exception) {
            Logger.error(ex) { "updateReplacementSpan failure" }
            original
        }

    fun sendVote(vote: Vote) = DislikeVotes.send(videoId, vote, { isShort }) { setUserVote(vote) }

    fun setUserVote(vote: Vote) {
        try {
            synchronized(this) {
                userVote = vote
                spanCache.clear()
                fetchedVotes?.updateUsingVote(vote)
            }
            dislikeChanges.fire(videoId)
        } catch (ex: Exception) {
            Logger.error(ex) { "setUserVote failure" }
        }
    }

    enum class Vote(val value: Int) {
        LIKE(1),
        DISLIKE(-1),
        LIKE_REMOVE(0),
    }
}
