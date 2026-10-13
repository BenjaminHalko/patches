// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

@file:JvmName("CustomBranding")

package app.reseam.youtube.branding

import android.app.Notification
import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.Color
import app.reseam.bundle.Logger
import app.reseam.bundle.ReseamPatches
import app.reseam.bundle.identifier
import app.reseam.runtime.Export
import app.reseam.youtube.core.YouTubeContext
import app.reseam.youtube.core.debug

@Export
fun setBranding(nameKey: String, iconKey: String) {
    runCatching {
        val context = YouTubeContext.applicationContext
        val manager = context.packageManager
        val packageName = context.packageName
        val settings = ReseamPatches.bundle().settings
        val name = settings.getChoice(nameKey)
        val icon = settings.getChoice(iconKey)
        val aliasPrefix = "$packageName.reseam_"
        val selectedAlias = "${aliasPrefix}${icon}_$name"
        // Enable the destination first so switching presets always leaves a launcher entry.
        manager.setComponentState(
            ComponentName(packageName, selectedAlias),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        )
        val activities =
            manager
                .getPackageInfo(
                    packageName,
                    PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS,
                )
                .activities
                .orEmpty()
        activities
            .filter { it.name.startsWith(aliasPrefix) && it.name != selectedAlias }
            .forEach {
                manager.setComponentState(
                    ComponentName(packageName, it.name),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                )
            }
        Logger.debug { "Custom branding: name=$name icon=$icon" }
    }
        .onFailure { ex ->
            Logger.error(ex) { "Custom branding failed" }
        }
}

private fun PackageManager.setComponentState(component: ComponentName, state: Int) {
    if (getComponentEnabledSetting(component) != state) {
        setComponentEnabledSetting(component, state, PackageManager.DONT_KILL_APP)
    }
}

/**
 * A custom icon keeps YouTube's notification icon; the Reseam glyph only matches the Reseam icon.
 */
@Export
fun setNotificationIcon(builder: Notification.Builder, iconKey: String) {
    runCatching {
        if (ReseamPatches.bundle().settings.getChoice(iconKey) != "reseam") return
        val context = YouTubeContext.applicationContext
        val id = context.identifier("reseam_notification_icon", "drawable")
        if (id != 0) builder.setSmallIcon(id).setColor(Color.TRANSPARENT)
    }
        .onFailure { ex ->
            Logger.error(ex) { "Notification icon failed" }
        }
}
