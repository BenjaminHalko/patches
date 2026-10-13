// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.patches.youtube.internal

import app.reseam.youtube.video.bindings.VideoQuality
import app.reseam.youtube.video.bindings.VideoInformation
import app.reseam.patch.ExtMethod
import app.reseam.patch.Type
import app.reseam.patch.after

internal val PLAYBACK_CONTROLLER = VideoInformation.PlaybackController.descriptor
internal val VIDEO_QUALITY_MENU_INTERFACE = VideoQuality.Menu.descriptor
internal val EXTENSION_VIDEO_QUALITY_INTERFACE = VideoQuality.Option.descriptor
internal val VIDEO_QUALITY_ARRAY = "[$EXTENSION_VIDEO_QUALITY_INTERFACE"

internal val onCreateHooks = mutableListOf<ExtMethod>()
internal val videoTimeHooks = mutableListOf<ExtMethod>()
internal val videoSpeedChangedHooks = mutableListOf<ExtMethod>()
internal val userSelectedPlaybackSpeedHooks = mutableListOf<ExtMethod>()

/** Register a callback after the player controller has been initialized. */
fun onCreateHook(hook: ExtMethod) {
    require(hook.isStatic && hook.proto == "($PLAYBACK_CONTROLLER)V") {
        "An onCreate hook must be static (PlaybackController) -> Unit: $hook"
    }
    onCreateHooks += hook
}

/** Register a callback for the regular player's periodic time update. */
fun videoTimeHook(hook: ExtMethod) {
    require(hook.isStatic && hook.proto == "(J)V") {
        "A video-time hook must be static (long) -> Unit: $hook"
    }
    videoTimeHooks += hook
}

/** Register a callback when YouTube changes playback speed outside a menu selection. */
fun videoSpeedChangedHook(hook: ExtMethod) {
    require(hook.isStatic && hook.proto == "(F)V") {
        "A video-speed hook must be static (float) -> Unit: $hook"
    }
    videoSpeedChangedHooks += hook
}

/** Register a callback when the user selects a playback speed. */
fun userSelectedPlaybackSpeedHook(hook: ExtMethod) {
    require(hook.isStatic && hook.proto == "(F)V") {
        "A user-selected-speed hook must be static (float) -> Unit: $hook"
    }
    userSelectedPlaybackSpeedHooks += hook
}
