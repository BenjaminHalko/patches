// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

@file:JvmName("InstagramSettingsEntry")

package app.reseam.instagram.settings

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import app.reseam.bundle.ReseamPatches
import app.reseam.bundle.identifier
import app.reseam.runtime.Export
import java.util.Locale

private const val RESEAM_VIEW_TAG = 0x7f5151cc
private const val RESEAM_DECOR_TAG = 0x7f5151cd
private const val RESEAM_REPOST_VISIBILITY_TAG = 0x7f5151ce
private val repostViewNames =
    arrayOf(
        "clips_repost_button",
        "repost_button",
        "repost_button_container",
        "row_feed_button_repost",
        "tisu_ufi_repost_button",
    )
private val menuDescriptions = listOf("menu", "options", "more", "hamburger")
private var hookInstalled = false
private var repostViewIds = intArrayOf()

@Export
fun init(ctx: Context?) {
    val app = ctx?.applicationContext as? Application ?: return
    repostViewIds = repostViewNames.map { app.identifier(it, "id") }.toIntArray()
    if (hookInstalled) return
    hookInstalled = true
    app.registerActivityLifecycleCallbacks(
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, state: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                attachOnceLaidOut(activity)
            }

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        }
    )
}

private fun attachOnceLaidOut(activity: Activity) {
    val decor = activity.window?.decorView ?: return
    if (decor.getTag(RESEAM_DECOR_TAG) != null) return
    decor.setTag(RESEAM_DECOR_TAG, true)
    decor.viewTreeObserver.addOnGlobalLayoutListener {
        walk(decor, AppearanceSettings.hideRepostButtons.value)
    }
    walk(decor, AppearanceSettings.hideRepostButtons.value)
}

private fun walk(view: View?, hideRepost: Boolean) {
    if (view == null) return
    val id = view.id
    if (id != View.NO_ID && repostViewIds.any { it != 0 && it == id }) {
        view.updateRepostVisibility(hideRepost)
    }
    val description = view.contentDescription?.toString()?.lowercase(Locale.getDefault())
    if (
        description != null &&
            menuDescriptions.any { it in description } &&
            view.getTag(RESEAM_VIEW_TAG) == null
    ) {
        view.setTag(RESEAM_VIEW_TAG, true)
        view.isLongClickable = true
        view.setOnLongClickListener {
            ReseamPatches.bundle().openSettings()
            true
        }
    }
    if (view is ViewGroup) {
        for (index in 0 until view.childCount) walk(view.getChildAt(index), hideRepost)
    }
}

private fun View.updateRepostVisibility(hide: Boolean) {
    val original = getTag(RESEAM_REPOST_VISIBILITY_TAG)
    when {
        hide -> {
            if (original == null) setTag(RESEAM_REPOST_VISIBILITY_TAG, visibility)
            if (visibility != View.GONE) visibility = View.GONE
        }
        original is Int -> {
            setTag(RESEAM_REPOST_VISIBILITY_TAG, null)
            visibility = original
        }
    }
}
