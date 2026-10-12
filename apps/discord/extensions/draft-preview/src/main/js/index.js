// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

// Discord's API constants: the snowflake epoch, the ephemeral message flag and the draft type of a
// channel's message box.
const DISCORD_EPOCH = 1420070400000n;
const EPHEMERAL = 1 << 6;
const CHANNEL_MESSAGE_DRAFT = 0;

// A snowflake for now, so the preview sorts after the messages already loaded.
function snowflake() {
    return ((BigInt(Date.now()) - DISCORD_EPOCH) << 22n).toString();
}

// Shows the draft as an ephemeral message from the current user, which only this phone has and
// Discord lets the user dismiss.
function showPreview({ draftStore, userStore, messageActions }, channelId) {
    const content = draftStore.default.getDraft(channelId, CHANNEL_MESSAGE_DRAFT);
    if (content.trim() === "") return;
    const user = userStore.default.getCurrentUser();
    messageActions.default.receiveMessage(channelId, {
        id: snowflake(),
        type: 0,
        flags: EPHEMERAL,
        content,
        channel_id: channelId,
        author: {
            id: user.id,
            username: user.username,
            global_name: user.globalName,
            discriminator: user.discriminator,
            avatar: user.avatar,
        },
        attachments: [],
        embeds: [],
        mentions: [],
        mention_roles: [],
        mention_channels: [],
        mention_everyone: false,
        pinned: false,
        tts: false,
        timestamp: new Date().toISOString(),
        state: "SENT",
    });
}

// The send button is the action button its renderer gives `onSendMessage` as `onPress`.
function withPreview(react, stores, sendButton, onSendMessage, channelId) {
    const button = sendButton?.props.children;
    if (button?.props.onPress !== onSendMessage) return sendButton;
    return react.cloneElement(sendButton, {}, react.cloneElement(button, {
        onLongPress: () => showPreview(stores, channelId),
    }));
}

exports.chatInputActionButton = function (react, original, props, ref) {
    const pressable = original(props, ref);
    if (props.onLongPress == null || pressable == null) return pressable;
    return react.cloneElement(pressable, { onLongPress: props.onLongPress });
};

exports.sendButton = function (react, draftStore, userStore, messageActions, original, props, ref) {
    const stores = { draftStore, userStore, messageActions };
    return withPreview(react, stores, original(props, ref), props.onSendMessage, props.channel.id);
};

exports.sendButtonItem = function (react, draftStore, userStore, messageActions, original, props) {
    const stores = { draftStore, userStore, messageActions };
    return withPreview(react, stores, original(props), props.onSendMessage, props.channelId);
};
