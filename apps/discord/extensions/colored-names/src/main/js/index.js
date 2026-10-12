// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

// A user mention takes the member's role color, in the fields role mentions carry.
exports.hydrateUserMention = function (guildMemberStore, original, node, state) {
    const mention = original(node, state);
    if (mention.userId == null || mention.guildId == null) return mention;
    const colorString = guildMemberStore.default.getMember(mention.guildId, mention.userId)?.colorString;
    if (colorString == null) return mention;
    const color = parseInt(colorString.slice(1), 16);
    return { ...mention, roleColor: color, color, colorString };
};


// "Name is typing...": Discord renders one element per typing user it knows, in the order of
// `typingUserIds`; four or more typing users become one plain sentence.
exports.useTypingText = function (react, userStore, guildMemberStore, original, props) {
    const text = original(props);
    if (!Array.isArray(text) || props.guildId == null) return text;
    const users = props.typingUserIds.filter(id => userStore.default.getUser(id) != null);
    let user = 0;
    return text.map(part => {
        if (!react.isValidElement(part)) return part;
        const color = guildMemberStore.default.getMember(props.guildId, users[user++])?.colorString;
        return color == null ? part : react.cloneElement(part, { style: [part.props.style, { color }] });
    });
};

// A voice participant row: its first child is the name, followed by an optional server tag.
exports.VoiceUserNameItem = function (react, original, props) {
    const row = original(props);
    const color = props.member?.colorString;
    const [name, ...rest] = row.props.children;
    if (color == null || name == null) return row;
    return react.cloneElement(row, {}, react.cloneElement(name, { style: [name.props.style, { color }] }), ...rest);
};

// Member list headers: section `index` shows group `index` as "title — count"; a hoisted role's
// group takes the role's color. The search results header shares index 0, hence the title check.
exports.GuildChannelUserList = function (react, reactNative, channelMemberStore, guildRoleStore, original, props) {
    const list = original(props);
    const children = list?.props.children;
    if (!Array.isArray(children)) return list;
    const { guildId, channelId } = props;
    const listChannelId = channelId === channelMemberStore.EVERYONE_CHANNEL_ID ? null : channelId;
    const colored = getSectionProps => index => {
        const section = getSectionProps(index);
        const group = channelMemberStore.default.getProps(guildId, listChannelId).groups[index];
        if (section?.type !== "section" || group == null || section.props.title !== `${group.title} \u2014 ${group.count}`) {
            return section;
        }
        const color = guildRoleStore.default.getRole(guildId, group.id)?.colorString;
        if (color == null) return section;
        return { ...section, props: { ...section.props, title: react.createElement(reactNative.Text, { style: { color } }, section.props.title) } };
    };
    return react.cloneElement(list, {}, ...children.map(child => child?.props?.getSectionProps == null
        ? child
        : react.cloneElement(child, { getSectionProps: colored(child.props.getSectionProps) })));
};
