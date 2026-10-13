// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.discord.chat

import app.reseam.discord.settings.DiscordStrings
import app.reseam.patch.ExtJsModule
import app.reseam.patch.function
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import app.reseam.patches.discord.core.DISCORD
import app.reseam.discord.settings.DiscordSettings
import app.reseam.patches.discord.core.discordSettings
import app.reseam.patches.discord.core.draftStore
import app.reseam.patches.discord.core.messageActions
import app.reseam.patches.discord.core.react
import app.reseam.patches.discord.core.userStore

val draftPreview = patch("Draft preview") {
    description("Long press the send button to see your message in the chat before sending it.")
    compatibleWith(DISCORD)
    settings(discordSettings, section(DiscordStrings.sectionChat, DiscordSettings.draftPreview))

    execute {
        gate(DiscordSettings.draftPreview) {
            chatInputActionButton.wrap(DraftPreview.chatInputActionButton, react)
            sendButton.wrap(DraftPreview.sendButton, react, draftStore, userStore, messageActions)
            sendButtonItem.wrap(DraftPreview.sendButtonItem, react, draftStore, userStore, messageActions)
        }
    }
}

private object DraftPreview : ExtJsModule("discord-draft-preview") {
    val chatInputActionButton by export()
    val sendButton by export()
    val sendButtonItem by export()
}

/** A round button beside the message box, such as send or attach. */
private val chatInputActionButton = function {
    name("")
    strings("CHAT_INPUT_ACTION_BUTTON_SIZE", "CHAT_INPUT_BUTTON_MIN_TOUCH_TARGET_SIZE", "activeIconStyle")
    paramCount(2)
}

/** The send button while voice messages are unavailable. */
private val sendButton = function {
    name("")
    strings("SendMessageIcon", "canSendVoiceMessage", "TransitionGroup")
    paramCount(2)
}

/** The send button as one item of the send and voice message transition. */
private val sendButtonItem = function {
    name("")
    strings("SendMessageIcon", "BUTTON_SEND_VOICE_MESSAGE", "withBounce")
    paramCount(1)
}
