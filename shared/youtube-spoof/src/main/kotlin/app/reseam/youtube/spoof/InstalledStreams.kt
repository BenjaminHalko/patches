// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.spoof

import app.reseam.youtubespoof.settings.SpoofStrings

internal object InstalledStreams {
    private const val MAX_RESPONSES = 50

    private data class Entry(val client: ClientType, val expiresAt: Long)

    private val clients = linkedMapOf<String, Entry>()
    private var currentVideoId: String? = null

    @Synchronized
    fun installed(videoId: String, data: StreamingData) {
        prune()
        clients.remove(videoId)
        clients[videoId] = Entry(data.client, data.expiresAt)
        if (clients.size > MAX_RESPONSES) {
            val oldest = clients.keys.firstOrNull { it != currentVideoId }
            oldest?.let(clients::remove)
        }
    }

    @Synchronized
    fun nativeInstalled(videoId: String?) {
        clients.remove(videoId)
    }

    @Synchronized
    fun videoChanged(videoId: String?) {
        currentVideoId = videoId
    }

    @Synchronized
    fun clientName(): String {
        prune()
        return clients[currentVideoId]?.client?.displayName ?: SpoofStrings.spoofClientNative
    }

    private fun prune() {
        val now = System.currentTimeMillis()
        clients.values.removeIf { it.expiresAt <= now }
    }
}
