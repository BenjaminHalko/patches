// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.youtubemusic.core

import app.reseam.patch.settings.SettingsPage
import app.reseam.patches.bundle.lucide

internal object YouTubeMusicSettingsPages {
    val Playback = SettingsPage("playback", "Playback", order = 0, summary = "Audio, background play and speed", icon = lucide("play"))
    val Player = SettingsPage("player", "Player", order = 10, summary = "The player and miniplayer", icon = lucide("disc-3"))
    val Ads = SettingsPage("ads", "Ads", order = 20, summary = "Ads and Premium prompts", icon = lucide("megaphone-off"))
    val Interface = SettingsPage("interface", "Interface", order = 30, summary = "Navigation, buttons and start page", icon = lucide("layout-dashboard"))
    val Advanced = SettingsPage("advanced", "Advanced", order = 40, summary = "Streams, privacy and diagnostics", icon = lucide("sliders-horizontal"))

    val Menus = SettingsPage("interface.menus", "Menus", Interface, order = 0, summary = "Items in the app's menus", icon = lucide("menu"))
}
