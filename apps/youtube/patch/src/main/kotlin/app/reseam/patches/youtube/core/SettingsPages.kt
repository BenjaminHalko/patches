// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.youtube.core

import app.reseam.patch.settings.SettingsPage
import app.reseam.patches.bundle.lucide

/** YouTube's menu structure; the SDK only knows generic pages and sections. */
internal object YouTubeSettingsPages {
    val Video = SettingsPage("video", "Video", order = 0, summary = "Quality, speed, SponsorBlock and dislikes", icon = lucide("clapperboard"))
    val Player = SettingsPage("player", "Player", order = 10, summary = "Controls, overlay, miniplayer and casting", icon = lucide("tv-minimal-play"))
    val Shorts = SettingsPage("shorts", "Shorts", order = 20, summary = "Autoplay, overlay and hidden buttons", icon = lucide("film"))
    val Ads = SettingsPage("ads", "Ads", order = 30, summary = "Video ads and sponsored cards", icon = lucide("megaphone-off"))
    val Appearance = SettingsPage("appearance", "Appearance", order = 40, summary = "Feed, comments, thumbnails and layout", icon = lucide("palette"))
    val Downloads = SettingsPage("downloads", "Downloads", order = 50, summary = "Offline videos in any quality", icon = lucide("download"))
    val Advanced = SettingsPage("advanced", "Advanced", order = 60, summary = "Streams, privacy and diagnostics", icon = lucide("sliders-horizontal"))

    val Controls = SettingsPage("player.controls", "Controls and gestures", Player, order = 0, summary = "Buttons, seekbar, double tap and swipes", icon = lucide("hand"))
    val Overlay = SettingsPage("player.overlay", "Overlay and menus", Player, order = 10, summary = "The player overlay and end screens", icon = lucide("layers"))
    val Feed = SettingsPage("appearance.feed", "Feed", Appearance, order = 0, summary = "Shelves and cards in your feeds", icon = lucide("layout-list"))
    val Comments = SettingsPage("appearance.comments", "Comments", Appearance, order = 10, summary = "Summaries and sections in comments", icon = lucide("message-square"))
    val Description = SettingsPage("appearance.description", "Video description", Appearance, order = 20, summary = "Sections below the video", icon = lucide("text"))
    val Channel = SettingsPage("appearance.channel", "Channel pages", Appearance, order = 30, summary = "Tabs and buttons on channels", icon = lucide("user-round"))
    val Filters = SettingsPage("appearance.filters", "Content filters", Appearance, order = 40, summary = "Hide videos by keyword or custom rule", icon = lucide("funnel"))
}
