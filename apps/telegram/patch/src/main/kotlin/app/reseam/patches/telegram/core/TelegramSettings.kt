// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.telegram.core

import app.reseam.patch.settings.toggle
import app.reseam.patches.bundle.lucide

object TelegramSettings {
    val hideSponsoredAds by toggle("Hide sponsored messages", default = true, icon = lucide("megaphone-off"))
    val disableAutoUpdate by toggle("Disable auto-update", default = true, icon = lucide("refresh-cw-off"))
    val unlockPremium by toggle(
        "Unlock Premium",
        summary = "Server-checked features still need a real subscription.",
        default = true,
        icon = lucide("gem"),
    )
    val hideTyping by toggle("Hide typing indicator", default = true, icon = lucide("keyboard-off"))
    val boostDownloads by toggle("Boost download speed", default = true, icon = lucide("download"))
    val saveFromRestricted by toggle(
        "Save from restricted chats",
        summary = "Re-enables copy, save, and forward in chats with content protection on.",
        default = true,
        icon = lucide("lock-open"),
    )
    val removePlayStoreRestrictions by toggle(
        "Remove Play Store restrictions",
        summary = "Opens chats and media Telegram hides only in its Play Store app.",
        default = true,
        icon = lucide("store"),
    )
    val recoverDeleted by toggle(
        "Recover deleted messages",
        summary = "Keep messages others delete; they stay in the chat with a 🗑️ marker.",
        default = false,
        icon = lucide("message-square-x"),
    )
    val allowScreenshots by toggle(
        "Allow screenshots in secret viewers",
        summary = "Disable FLAG_SECURE so you can screenshot or record view-once / self-destruct media.",
        default = true,
        icon = lucide("camera"),
    )
}
