// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

@file:JvmName("ShortsAutoplay")

package app.reseam.youtube.shorts

import android.app.Activity
import app.reseam.runtime.Export
import app.reseam.youtube.settings.ShortsSettings
import java.lang.ref.WeakReference

private var mainActivity = WeakReference<Activity>(null)
private var advance: Enum<*>? = null
private var repeat: Enum<*>? = null

@Export
fun setMainActivity(activity: Activity) {
    mainActivity = WeakReference(activity)
}

@Export
fun changeRepeatBehavior(original: Enum<*>?): Enum<*>? {
    if (original == null) return null
    if (advance == null || repeat == null) {
        val behaviors = original.declaringJavaClass.enumConstants.associateBy { it.name }
        advance = behaviors["REEL_LOOP_BEHAVIOR_AUTO_ADVANCE"] ?: advance
        repeat = behaviors["REEL_LOOP_BEHAVIOR_REPEAT"] ?: repeat
    }
    val pip = mainActivity.get()?.isInPictureInPictureMode == true
    val autoplay =
        if (pip) ShortsSettings.shortsAutoplayBackground.value
        else ShortsSettings.shortsAutoplay.value
    return (if (autoplay) advance else repeat) ?: original
}
