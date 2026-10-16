// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.navigation

import app.reseam.bundle.Logger
import app.reseam.runtime.Export
import app.reseam.youtube.core.Endpoints
import app.reseam.youtube.core.Proto
import java.lang.ref.WeakReference

/** Runs navigation commands through the app's command router, as a tap inside YouTube does. */
object Commands {
    @Volatile private var router = WeakReference<Any>(null)

    @Export
    @JvmStatic
    fun setRouter(router: Any) {
        this.router = WeakReference(router)
    }

    /** Plays a video in the regular player. Call on the main thread. */
    fun openVideo(videoId: String): Boolean =
        route(
            Proto.Writer()
                .message(Endpoints.WATCH, Proto.Writer().string(Endpoints.WATCH_VIDEO_ID, videoId))
        )

    private fun route(endpoint: Proto.Writer): Boolean {
        val current = router.get() ?: return false
        return try {
            dispatch(current, endpoint.toByteArray())
            true
        } catch (e: RuntimeException) {
            Logger.error(e) { "Could not route a navigation command" }
            false
        }
    }

    // The router and the endpoint message are obfuscated, so the command router patch writes the body.
    @Export
    @JvmStatic
    fun dispatch(router: Any, endpoint: ByteArray): Unit = error("Replaced by the command router patch")
}
