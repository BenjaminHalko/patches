// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.discord.chat

import app.reseam.patch.ExtJsModule
import app.reseam.patch.function
import app.reseam.patch.patch
import app.reseam.patch.wrap
import app.reseam.patches.discord.core.DISCORD
import app.reseam.patches.discord.core.commit
import app.reseam.patches.discord.core.createMessageContent
import app.reseam.patches.discord.core.getOrCreate

/** Tracks the messages this phone shows, for the deleted message and edit history patches. */
internal val messageHistory = patch {
    compatibleWith(DISCORD)

    execute {
        getOrCreate.wrap(MessageHistory.getOrCreate)
        commit.wrap(MessageHistory.commit)
        createMessageContent.wrap(MessageHistory.createMessageContent)
        dispatch.wrap(MessageHistory._dispatch)
    }
}

internal object MessageHistory : ExtJsModule("discord-message-history") {
    val getOrCreate by export()
    val commit by export()
    val createMessageContent by export()
    val _dispatch by export()
    val handleMessageDelete by export()
    val handleMessageDeleteBulk by export()
    val updateMessageRecord by export()
}

private val dispatch = function {
    name("_dispatch")
    strings("_interceptors", "getOrderedActionHandlers", "__subscriptions")
    paramCount(2)
}
