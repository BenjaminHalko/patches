// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.discord.themes;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Kept on disk, so views drawn before JavaScript starts use them too.
public final class ThemeColors {
    private static volatile Palette palette = Palette.NONE;

    private ThemeColors() {}

    public static void init(Context context) throws IOException, JSONException {
        palette = Palette.parse(ThemeFiles.read(ThemeFiles.nativeColors(context)));
    }

    public static int color(String theme, String token, int color) {
        Palette current = palette;
        if (!theme.equals(current.base)) return color;
        Integer themed = current.colors.get(token);
        return themed == null ? color : themed;
    }

    static void apply(Context context, String json) throws IOException, JSONException {
        Palette next = Palette.parse(json);
        File file = ThemeFiles.nativeColors(context);
        ThemeFiles.write(file, json);
        palette = next;
    }

    private static final class Palette {
        static final Palette NONE = new Palette("", Collections.emptyMap());

        final String base;
        final Map<String, Integer> colors;

        Palette(String base, Map<String, Integer> colors) {
            this.base = base;
            this.colors = colors;
        }

        static Palette parse(String json) throws JSONException {
            if (json.isEmpty()) return NONE;
            JSONObject root = new JSONObject(json);
            JSONObject entries = root.getJSONObject("colors");
            Map<String, Integer> colors = new HashMap<>(entries.length() * 2);
            for (Iterator<String> it = entries.keys(); it.hasNext(); ) {
                String token = it.next();
                colors.put(token, (int) entries.getLong(token));
            }
            return new Palette(root.getString("base"), Collections.unmodifiableMap(colors));
        }
    }
}
