// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import app.reseam.youtube.core.Logger;
import app.reseam.youtube.core.Proto;

public final class QueueMenu {
    private static final int[] MENU_ITEMS = {98150882, 1, 66439850, 1, 77258115};
    private static final int ITEM_ICON = 2;
    private static final int ITEM_COMMAND = 3;
    private static final int ITEM_BADGE = 12;
    private static final int ICON_TYPE = 1;
    private static final long ICON_QUEUE_PLAY_NEXT = 251;
    private static final int BROWSE_ENDPOINT = 48687626;
    private static final int CAST_PLAY_NEXT = 122330696;

    private QueueMenu() {}

    public static byte[] unlock(byte[] command) {
        try {
            String videoId = castVideoId(command);
            if (videoId == null) return null;
            byte[] unlocked = rewrite(command, 0, item -> isUpsell(item) ? queueItem(item, videoId) : item);
            return unlocked == command ? null : unlocked;
        } catch (IllegalArgumentException exception) {
            Logger.debug(() -> "Menu is not a queue menu");
            return null;
        }
    }

    private static String castVideoId(byte[] command) {
        List<byte[]> items = new ArrayList<>();
        collect(command, 0, items);
        for (byte[] item : items) {
            byte[] itemCommand = Proto.bytes(item, ITEM_COMMAND);
            byte[] playNext = itemCommand == null ? null : Proto.bytes(itemCommand, CAST_PLAY_NEXT);
            if (playNext != null) return Proto.string(playNext, 1);
        }
        return null;
    }

    private static boolean isUpsell(byte[] item) {
        byte[] icon = Proto.bytes(item, ITEM_ICON);
        byte[] command = Proto.bytes(item, ITEM_COMMAND);
        return icon != null && command != null
                && Proto.number(icon, ICON_TYPE, 0) == ICON_QUEUE_PLAY_NEXT
                && Proto.bytes(command, BROWSE_ENDPOINT) != null;
    }

    private static byte[] queueItem(byte[] item, String videoId) {
        Map<Integer, byte[]> command = new HashMap<>();
        command.put(BROWSE_ENDPOINT, null);
        command.put(QueueReply.PLAYER_STATE_COMMAND, QueueReply.playerStateCommand(videoId, QueueReply.POSITION_NEXT));
        Map<Integer, byte[]> fields = new HashMap<>();
        fields.put(ITEM_COMMAND, Proto.replace(Proto.bytes(item, ITEM_COMMAND), command));
        fields.put(ITEM_BADGE, null);
        return Proto.replace(item, fields);
    }

    private static void collect(byte[] message, int depth, List<byte[]> found) {
        if (depth == MENU_ITEMS.length) {
            found.add(message);
            return;
        }
        for (Proto.Field field : Proto.fields(message)) {
            if (field.number == MENU_ITEMS[depth] && field.wireType == Proto.WIRE_LENGTH) collect(field.bytes, depth + 1, found);
        }
    }

    private static byte[] rewrite(byte[] message, int depth, UnaryOperator<byte[]> item) {
        if (depth == MENU_ITEMS.length) return item.apply(message);
        return Proto.rewriteLength(message, (number, value) ->
                number == MENU_ITEMS[depth] ? rewrite(value, depth + 1, item) : value);
    }
}
