// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.shorts

import app.reseam.runtime.Export
import app.reseam.youtube.litho.Filter
import app.reseam.youtube.settings.ShortsSettings

@Export
class ShortsFilter : Filter() {
    private val groups = ShortsFilterGroups()

    init {
        addIdentifierCallbacks(*groups.identifierGroups)
        addPathCallbacks(*groups.pathGroups)
    }

    companion object {
        private const val COMPONENT_TYPE = "ComponentType"
        private val actionBarPaths = listOf("reel_action_bar.", "reels_player_overlay_layout.")
        private val actionSettings =
            mapOf(
                4 to ShortsSettings.hideShortsRemixButton,
                3 to ShortsSettings.hideShortsShareButton,
            )

        @Export
        @JvmStatic
        fun hideActionButtons(pathBuilder: StringBuilder?, results: MutableList<Any?>?) {
            if (pathBuilder.isNullOrEmpty() || results == null || results.size < 4) return
            val path = pathBuilder.toString()
            if (actionBarPaths.none { it in path } || results[0]?.toString() != COMPONENT_TYPE)
                return
            // Higher indices go first: the app fixes action positions before filtering.
            for ((index, setting) in actionSettings) if (
                index < results.size && results[index] != null && setting.value
            )
                results.removeAt(index)
        }
    }
}
