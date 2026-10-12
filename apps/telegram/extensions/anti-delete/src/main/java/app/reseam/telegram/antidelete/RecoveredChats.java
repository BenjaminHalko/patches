// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.telegram.antidelete;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import app.reseam.bundle.ReseamPatches;

/**
 * The chats Recover deleted messages covers: only the chats the user chose, or every chat but the
 * ones they excluded. Each mode keeps its own list, so switching modes keeps both.
 */
public final class RecoveredChats {
    // TelegramSettings.recoverDeletedChats in the patches.
    private static final String MODE_KEY = "telegram_settings.recover_deleted_chats";
    private static final String ALL = "all";

    private static final Set<Long> chosen = ConcurrentHashMap.newKeySet();
    private static final Set<Long> excluded = ConcurrentHashMap.newKeySet();
    private static volatile SQLiteDatabase store;

    private RecoveredChats() {}

    static void load(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS chats ("
                + "mode TEXT NOT NULL, uid INTEGER NOT NULL, PRIMARY KEY(mode, uid))");
        try (Cursor c = db.rawQuery("SELECT mode, uid FROM chats", null)) {
            while (c.moveToNext()) (ALL.equals(c.getString(0)) ? excluded : chosen).add(c.getLong(1));
        }
        store = db;
    }

    static boolean covers(long dialogId) {
        boolean all = ALL.equals(mode());
        return all != (all ? excluded : chosen).contains(dialogId);
    }

    public static String menuLabel(long dialogId) {
        return covers(dialogId) ? "Turn off deleted message recovery" : "Turn on deleted message recovery";
    }

    /** Flips the chat in the current mode's list and returns its new menu label. */
    public static String toggle(long dialogId) {
        String mode = mode();
        Set<Long> list = ALL.equals(mode) ? excluded : chosen;
        if (list.remove(dialogId)) {
            store.delete("chats", "mode = ? AND uid = ?", new String[]{mode, String.valueOf(dialogId)});
        } else {
            list.add(dialogId);
            ContentValues row = new ContentValues(2);
            row.put("mode", mode);
            row.put("uid", dialogId);
            store.insertWithOnConflict("chats", null, row, SQLiteDatabase.CONFLICT_IGNORE);
        }
        return menuLabel(dialogId);
    }

    private static String mode() {
        return ReseamPatches.bundle().settings().getChoice(MODE_KEY);
    }
}
