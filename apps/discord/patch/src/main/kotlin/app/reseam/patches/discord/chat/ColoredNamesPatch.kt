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
import app.reseam.patches.discord.core.channelMemberStore
import app.reseam.patches.discord.core.guildMemberStore
import app.reseam.patches.discord.core.guildRoleStore
import app.reseam.patches.discord.core.reactNative
import app.reseam.patches.discord.core.react
import app.reseam.patches.discord.core.userStore

val coloredNames = patch("Colored names") {
    description("Color members' names with their role color in mentions, the typing indicator, voice channels and member list headers.")
    compatibleWith(DISCORD)
    settings(discordSettings, section("Chat", DiscordSettings.coloredNames))

    execute {
        gate(DiscordSettings.coloredNames) {
            hydrateUserMention.wrap(ColoredNames.hydrateUserMention, guildMemberStore)
            useTypingText.wrap(ColoredNames.useTypingText, react, userStore, guildMemberStore)
            voiceUserNameItem.wrap(ColoredNames.VoiceUserNameItem, react)
            guildChannelUserList.wrap(ColoredNames.GuildChannelUserList, react, reactNative, channelMemberStore, guildRoleStore)
        }
    }
}

private object ColoredNames : ExtJsModule("discord-colored-names") {
    val hydrateUserMention by export()
    val useTypingText by export()
    val VoiceUserNameItem by export()
    val GuildChannelUserList by export()
}

/** Turns a parsed `<@id>` into the mention node chat and replies render. */
private val hydrateUserMention = function {
    name("hydrateUserMention")
    strings("unknownUserMentionPlaceholder", "everyoneOrHere", "viewingChannelId")
    paramCount(2)
}

/** The "is typing" sentence under the message box. */
private val useTypingText = function {
    name("useTypingText")
    strings("typingUserIds", "useStateFromStoresArray")
    paramCount(1)
}

/** A channel's member list, grouped into sections by hoisted role, then online and offline. */
private val guildChannelUserList = function {
    name("GuildChannelUserList")
    strings("guild-channel-user-list")
    paramCount(1)
}

/** A participant's name under a voice channel. */
private val voiceUserNameItem = function {
    name("VoiceUserNameItem")
    strings("measuringTag", "isGuest")
    paramCount(1)
}
