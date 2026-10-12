// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.discord.chat

import app.reseam.patch.ExtJsModule
import app.reseam.patch.function
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import app.reseam.patches.discord.core.DISCORD
import app.reseam.patches.discord.core.DiscordSettings
import app.reseam.patches.discord.core.discordSettings
import app.reseam.patches.discord.core.messageActions
import app.reseam.patches.discord.core.react

val jumpToFirstMessage = patch("Jump to first message") {
    description("Add a button above Jump to present that jumps to the first message.")
    compatibleWith(DISCORD)
    settings(discordSettings, section("Chat", DiscordSettings.jumpToFirstMessage))

    execute {
        gate(DiscordSettings.jumpToFirstMessage) {
            jumpToPresentButton.wrap(FirstMessage.JumpToPresentButton, react, messageActions)
        }
    }
}

private object FirstMessage : ExtJsModule("discord-first-message") {
    val JumpToPresentButton by export()
}

/** The arrow shown at the bottom of a chat scrolled away from the newest messages. */
private val jumpToPresentButton = function {
    name("JumpToPresentButton")
    strings("onJumpToPresent", "MemoedVoicePanelDismissChatButton")
    paramCount(1)
}
