// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtubemusic.core;

import java.util.function.BooleanSupplier;

import app.reseam.bundle.ReseamPatches;

/** Reads the settings declared by {@code object YouTubeMusicSettings} in the patches. */
public final class MusicSettings {
    private static final String PREFIX = "you_tube_music_settings.";

    private MusicSettings() {}

    public static BooleanSupplier toggle(String key, boolean defaultValue) {
        return () -> ReseamPatches.bundle().settings().getBoolean(PREFIX + key, defaultValue);
    }

    public static String getChoice(String key) {
        return ReseamPatches.bundle().settings().getChoice(PREFIX + key);
    }
}
