// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

const BUTTON_GAP = 8;

// A channel's ID is older than every message in it, so jumping to it loads the first messages.
function jumpToFirstMessage(messageActions, channelId) {
    messageActions.default.jumpToMessage({ channelId, messageId: channelId, flash: false });
}

exports.JumpToPresentButton = function (react, messageActions, original, props) {
    const container = original(props);
    const jumpToPresent = container?.props.children;
    // Voice channel chats show a dismiss button here instead.
    if (jumpToPresent?.props.onPress !== props.onJumpToPresent) return container;
    const firstMessageButton = react.cloneElement(jumpToPresent, {
        accessibilityLabel: "Jump to first message",
        onPress: () => jumpToFirstMessage(messageActions, props.channelId),
    });
    // The same down arrow, turned to point up.
    const turned = react.createElement(
        container.type,
        { key: "first", style: { marginBottom: BUTTON_GAP, transform: [{ rotate: "180deg" }] } },
        firstMessageButton,
    );
    return react.cloneElement(container, {}, turned, react.cloneElement(jumpToPresent, { key: "present" }));
};
