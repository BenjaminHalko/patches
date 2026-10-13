// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike.requests

import app.reseam.youtube.dislike.ReturnYouTubeDislike.Vote
import org.json.JSONException
import org.json.JSONObject

class RYDVoteData(json: JSONObject) {
    private val fetchedDislikeCount = json.getLong("dislikes")

    /** RYD omits its estimate for some videos; the raw counts' ratio stands in for it. */
    private val estimatedLikeCount =
        json.getLong("likes").let { likes ->
            val rawLikes = json.optLong("rawLikes", -1)
            val rawDislikes = json.optLong("rawDislikes", -1)
            if (likes != 0L || rawLikes < 0 || rawDislikes <= 0) likes
            else (fetchedDislikeCount.toFloat() / rawDislikes * rawLikes).toLong()
        }

    @Volatile
    var likeCount = estimatedLikeCount
        private set

    @Volatile
    var dislikeCount = fetchedDislikeCount
        private set

    @Volatile
    var dislikePercentage = 0f
        private set

    init {
        if (json.getLong("viewCount") < 0 || json.getLong("likes") < 0 || fetchedDislikeCount < 0)
            throw JSONException("Unexpected JSON values: $json")
        updateUsingVote(Vote.LIKE_REMOVE)
    }

    /** Adds at most one local like or dislike to the fetched counts. */
    fun updateUsingVote(vote: Vote) {
        likeCount = estimatedLikeCount + if (vote == Vote.LIKE) 1 else 0
        dislikeCount = fetchedDislikeCount + if (vote == Vote.DISLIKE) 1 else 0
        val total = likeCount + dislikeCount
        dislikePercentage = if (total == 0L) 0f else dislikeCount.toFloat() / total
    }

    override fun toString(): String = "likes=$likeCount dislikes=$dislikeCount"
}
