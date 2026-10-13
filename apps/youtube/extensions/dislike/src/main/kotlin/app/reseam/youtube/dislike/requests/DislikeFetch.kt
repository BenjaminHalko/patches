// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike.requests

import app.reseam.bundle.Logger
import app.reseam.youtube.core.debug
import app.reseam.youtube.settings.YouTubeStrings

/** Null on failure or backoff. */
internal fun fetchVotes(videoId: String): RYDVoteData? {
    if (isRateLimited("fetchVotes")) return null
    var rateLimited = false
    val votes =
        apiCall({ YouTubeStrings.rydFailure(it.message.toString()) }, { "fetchVotes failure" }) {
            val response =
                request(
                    "GET",
                    "votes?videoId=$videoId",
                    connectTimeout = 3_000,
                    readTimeout = 7_000,
                )
            rateLimited = response.status == 429
            response.ifOk { RYDVoteData(it.json()) }
        }
    if (votes == null) return null.also { backOff(rateLimited) }
    markSuccess()
    Logger.debug { "Voting data fetched for $videoId: $votes" }
    return votes
}
