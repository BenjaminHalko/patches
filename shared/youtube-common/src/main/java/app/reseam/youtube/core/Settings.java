// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.core;

import app.reseam.bundle.ReseamPatches;

/**
 * Reads and stores the settings declared by {@code object YouTubeSettings} in the patches. Extension code that
 * branches at runtime goes through here; hooks emitted by the settings DSL call the app runtime directly.
 */
public final class Settings {
    /** The bundle derives a key from the settings object and property name: {@code YouTubeSettings.debugLogging}. */
    private static final String PREFIX = "you_tube_settings.";

    private Settings() {}

    public static boolean getBoolean(String key, boolean defaultValue) {
        return ReseamPatches.bundle().settings().getBoolean(PREFIX + key, defaultValue);
    }

    public static String getString(String key, String defaultValue) {
        return ReseamPatches.bundle().settings().getString(PREFIX + key, defaultValue);
    }

    public static String getChoice(String key) {
        return ReseamPatches.bundle().settings().getChoice(PREFIX + key);
    }

    public static void setBoolean(String key, boolean value) {
        ReseamPatches.bundle().settings().setBoolean(PREFIX + key, value);
    }

    public static void setString(String key, String value) {
        ReseamPatches.bundle().settings().setString(PREFIX + key, value);
    }
}
