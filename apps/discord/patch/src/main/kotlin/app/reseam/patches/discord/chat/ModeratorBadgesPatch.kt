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
import app.reseam.patches.discord.core.guildStore
import app.reseam.patches.discord.core.permissionHelpers

val moderatorBadges = patch("Moderator badges") {
    description("Tag the server owner, admins, managers and moderators next to their names in chat.")
    compatibleWith(DISCORD)
    settings(discordSettings, section("Chat", DiscordSettings.moderatorBadges))

    execute {
        gate(DiscordSettings.moderatorBadges) {
            getTagProperties.wrap(ModeratorBadges.getTagProperties, guildStore, permissionHelpers)
        }
    }
}

private object ModeratorBadges : ExtJsModule("discord-moderator-badges") {
    val getTagProperties by export()
}

/** The tag shown after an author's name in chat, such as the bot tag. */
private val getTagProperties = function {
    name("getTagProperties")
    strings("isVerifiedBot", "BOT_TAG_SERVER_TYPE", "opTagBackgroundColor")
    paramCount(1)
}
