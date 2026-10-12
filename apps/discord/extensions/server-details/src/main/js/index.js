// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

const DISCORD_EPOCH = 1420070400000n;
const VERIFICATION_LEVELS = ["None", "Low", "Medium", "High", "Highest"];

// A snowflake's top bits are milliseconds since Discord's epoch.
function createdAt(snowflake) {
    return new Date(Number((BigInt(snowflake) >> 22n) + DISCORD_EPOCH));
}

function formatDate(date) {
    return date.toLocaleDateString(undefined, { year: "numeric", month: "long", day: "numeric" });
}

// Label and value pairs; a value the stores do not hold yet is left out.
function details(stores, guild) {
    const owner = stores.userStore.default.getUser(guild.ownerId);
    const self = stores.userStore.default.getCurrentUser();
    const joinedAt = stores.guildMemberStore.default.getMember(guild.id, self.id)?.joinedAt;
    const memberCount = stores.guildMemberCountStore.default.getMemberCount(guild.id);
    const channels = stores.guildChannelStore.default;
    return [
        ["Owner", owner == null ? guild.ownerId : `@${owner.username}`],
        ["Created", formatDate(createdAt(guild.id))],
        ["Joined", joinedAt == null ? null : formatDate(new Date(joinedAt))],
        ["Members", memberCount?.toLocaleString()],
        ["Roles", String(stores.guildRoleStore.default.getNumRoles(guild.id))],
        ["Channels", String(channels.getSelectableChannelIds(guild.id).length + channels.getVocalChannelIds(guild.id).length)],
        ["Boosts", `${guild.premiumSubscriberCount ?? 0} (level ${guild.premiumTier ?? 0})`],
        ["Verification", VERIFICATION_LEVELS[guild.verificationLevel]],
        ["Server ID", guild.id],
    ].filter(([, value]) => value != null);
}

exports.GuildActionSheetSecondaryActions = function (
    react, design, userStore, guildMemberStore, guildMemberCountStore, guildRoleStore, guildChannelStore,
    original, props,
) {
    const stores = { userStore, guildMemberStore, guildMemberCountStore, guildRoleStore, guildChannelStore };
    const h = react.createElement;
    const info = h(design.ActionSheetRow.Group, { key: "server-details" },
        ...details(stores, props.guild).map(([label, value]) =>
            h(design.ActionSheetRow, { key: label, label, trailing: h(design.TableRow.TrailingText, { text: value }) })));
    const actions = original(props);
    return actions == null ? info : [react.cloneElement(actions, { key: "actions" }), info];
};
