// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike.requests

import app.reseam.bundle.ReseamPatches
import app.reseam.youtube.dislike.ReturnYouTubeDislike.Vote
import app.reseam.youtube.dislike.verifyOffMainThread
import app.reseam.youtube.settings.YouTubeStrings
import org.json.JSONObject

// The API identity is persisted feature data with no declaration in the settings DSL.
private const val USER_ID_KEY = "you_tube_settings.ryd_user_id"

internal fun sendVote(videoId: String, vote: Vote): Boolean =
    apiCall({ YouTubeStrings.rydSendVoteFailed }, { "Failed to send vote for $videoId: $vote" }) {
        val userId = userId() ?: return@apiCall false
        if (isRateLimited("sendVote")) return@apiCall false
        val body =
            JSONObject()
                .put("userId", userId)
                .put("videoId", videoId)
                .put("value", vote.value.toString())
        request("POST", "interact/vote", body.toString()).ifOk { response ->
            val confirmation =
                JSONObject()
                    .put("userId", userId)
                    .put("videoId", videoId)
                    .put("solution", solve(response.json()))
            confirm("interact/confirmVote", confirmation, YouTubeStrings.rydConfirmVoteFailed)
        }
    } == true

private fun userId(): String? {
    verifyOffMainThread()
    val settings = ReseamPatches.bundle().settings
    return settings.getString(USER_ID_KEY, "").ifEmpty { null }
        ?: register()?.also { settings.setString(USER_ID_KEY, it) }
}

private fun register(): String? =
    apiCall({ YouTubeStrings.rydRegistrationFailed }, { "Failed to register user" }) {
        if (isRateLimited("registerAsNewUser")) return@apiCall null
        val userId = randomString(36)
        request("GET", "puzzle/registration?userId=$userId").ifOk { response ->
            val solution = JSONObject().put("solution", solve(response.json()))
            userId.takeIf {
                confirm(
                    "puzzle/registration?userId=$userId",
                    solution,
                    YouTubeStrings.rydConfirmRegistrationFailed,
                )
            }
        }
    }

private fun confirm(path: String, body: JSONObject, ioError: String): Boolean =
    apiCall({ ioError }, { "Failed to confirm $path" }) {
        if (isRateLimited(path)) return@apiCall false
        request("POST", path, body.toString(), readBody = false).ifOk { true }
    } == true
