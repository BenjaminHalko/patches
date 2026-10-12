// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

const MAX_THEME_FILE_SIZE = 1024 * 1024;

const sharedThemes = new Map();
const SHARED_THEME_SHEET = "ReseamSharedTheme";
let useSheetStyles = null;
let readMessages = null;
let commitMessages = null;
let messages = null;

function themeAttachment(message) {
    return message.attachments?.find(attachment =>
        attachment.filename?.endsWith(THEME_FILE_SUFFIX) && attachment.size <= MAX_THEME_FILE_SIZE) ?? null;
}

function fetchSharedTheme(message, attachment) {
    fetch(attachment.url)
        .then(response => {
            if (!response.ok) throw new Error(`The theme file could not be downloaded (${response.status})`);
            return response.json();
        })
        .then(document => {
            sharedThemes.set(attachment.id, { entry: { id: `shared-${attachment.id}`, revision: 0, builtIn: false, theme: parseTheme(document) } });
            if (readMessages === null || commitMessages === null) return;
            const channel = readMessages(message.channel_id);
            if (!channel.has(message.id)) return;
            // A commit outside a dispatch reports no change, so the store emits one for the redraw.
            commitMessages(channel.update(message.id, stored => withThemeCard(stored, attachment)));
            messages.emitChange();
        })
        .catch(error => sharedThemes.set(attachment.id, { error }));
}

function withThemeCard(message, attachment) {
    const shared = sharedThemes.get(attachment.id)?.entry;
    if (shared === undefined) return message;
    // Discord's native card adds the "#" itself.
    const colors = themeSwatch(shared.theme).map(color => color.slice(1));
    return message.set("sharedClientTheme", { colors, gradient_angle: shared.theme.background?.angle ?? 0 });
}

exports.getOrCreate = function (original, channelId) {
    readMessages = original;
    return original(channelId);
};

exports.commit = function (original, messages) {
    commitMessages = original;
    return original(messages);
};

exports.createMessageContent = function (messageStore, original, args) {
    messages = messageStore.default;
    const attachment = themeAttachment(args.message);
    if (attachment === null) return original(args);
    if (!sharedThemes.has(attachment.id)) {
        sharedThemes.set(attachment.id, {});
        fetchSharedTheme(args.message, attachment);
    }
    return original({ ...args, message: withThemeCard(args.message, attachment) });
};

exports.formatSharedClientThemeData = function (original, message, avatarSource, nick) {
    const data = original(message, avatarSource, nick);
    const attachment = themeAttachment(message);
    const shared = attachment === null ? undefined : sharedThemes.get(attachment.id)?.entry;
    return shared === undefined ? data : { ...data, previewHeading: shared.theme.name };
};

// The app shows the theme while the sheet is open; closing it any way ends the preview.
function SharedThemeSheet({ react, design, colorTokens, actionSheets, shared }) {
    const h = react.createElement;
    const space = colorTokens.default.space;
    useSheetStyles ??= design.createStyles({ actions: { paddingHorizontal: space.PX_16, paddingBottom: space.PX_16 } });
    const styles = useSheetStyles();
    // Like Discord's own theme preview, the sheet leaves the app it previews uncovered.
    react.useEffect(() => {
        setPreview(shared);
        return () => setPreview(null);
    }, []);
    const saved = () => findSameTheme(shared.theme) ?? addTheme(shared.theme);
    const close = then => () => {
        then();
        actionSheets.hideActionSheet(SHARED_THEME_SHEET);
    };
    return h(design.BottomSheet, { backdropOpacity: 0 },
        h(design.BottomSheetTitleHeader, { title: shared.theme.name, subtitle: shared.theme.author ?? undefined }),
        h(design.Stack, { direction: "horizontal", spacing: 8, style: styles.actions },
            h(design.Button, { variant: "secondary", size: "md", grow: true, text: "Add to my themes", onPress: close(saved) }),
            h(design.Button, { variant: "primary", size: "md", grow: true, text: "Apply", onPress: close(() => applyTheme(saved().id)) })));
}

exports.handleTapPreviewSharedClientTheme = function (actions, design, react, colorTokens, actionSheets, original, props) {
    setThemeActions(actions);
    // Discord can store the message again without the card's data.
    const attachment = themeAttachment(props.message);
    const shared = attachment === null ? undefined : sharedThemes.get(attachment.id)?.entry;
    if (shared === undefined) return original(props);
    const sheet = () => SharedThemeSheet({ react, design, colorTokens, actionSheets: actionSheets.default, shared });
    actionSheets.default.openLazy(Promise.resolve({ default: sheet }), SHARED_THEME_SHEET, { backdropKind: "none" });
};

function shareTheme(theme) {
    themesModule().share(themeFileName(theme), JSON.stringify(serializeTheme(theme), null, 2), "Share theme");
}
