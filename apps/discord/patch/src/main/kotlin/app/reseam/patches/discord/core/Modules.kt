// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.discord.core

import app.reseam.patch.MetroModule
import app.reseam.patch.function

/** GuildStore; its default export is the store. Named by the store's guild count. */
internal val guildStore: MetroModule = function {
    name("getGuildCount")
    strings("database")
    paramCount(0)
}.module

/** Discord's permission helpers, such as `computePermissions({ user, context })`. */
internal val permissionHelpers: MetroModule = function {
    name("computePermissions")
    strings("excludeGuildPermissions", "isViewingRoles")
    paramCount(1)
}.module

/** GuildMemberStore; its default export is the store. Named by the store's raw member table. */
internal val guildMemberStore: MetroModule = function {
    name("getMutableAllGuildsAndMembers")
    paramCount(0)
}.module

/** React, for building and cloning elements such as `cloneElement(element, props)`. */
internal val react: MetroModule = function {
    name("isValidElement")
    strings("\$\$typeof")
    paramCount(1)
}.module

/** Discord's message actions; its default export holds `jumpToMessage`, `receiveMessage` and the like. */
internal val messageActions: MetroModule = function {
    name("jumpToMessage")
    strings("trackJump", "pauseAllMediaPlayers")
    paramCount(1)
}.module

/** UserStore; its default export is the store. Named by the store's version counter. */
internal val userStore: MetroModule = function {
    name("getUserStoreVersion")
    paramCount(0)
}.module

/** DraftStore; its default export is the store. Named by its recent edits list. */
internal val draftStore: MetroModule = function {
    name("getRecentlyEditedDrafts")
    strings("pickBy", "sortBy")
    paramCount(1)
}.module

/** GuildRoleStore; its default export is the store. */
internal val guildRoleStore: MetroModule = function {
    name("getNumRoles")
    strings("partitionLength")
    paramCount(1)
}.module

/** React Native's components, such as `Text` and `View`. */
internal val reactNative: MetroModule = function {
    name("")
    strings("unstable_NativeText", "unstable_batchedUpdates")
    paramCount(7)
}.module

/** ChannelMemberStore; its default export is the store and `EVERYONE_CHANNEL_ID` the all-members list. */
internal val channelMemberStore: MetroModule = function {
    name("rebuildGroup")
    strings("findIndex", "OFFLINE", "ONLINE")
    paramCount(1)
}.module
