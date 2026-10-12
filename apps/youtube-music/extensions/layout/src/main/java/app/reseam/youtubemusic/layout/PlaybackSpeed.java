// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtubemusic.layout;

import app.reseam.youtubemusic.core.MusicSettings;

public final class PlaybackSpeed {
    private PlaybackSpeed() {}

    /** Injection point, for every rate the player is given. An empty choice keeps the app's rate. */
    public static float rate(float original) {
        String chosen = MusicSettings.getChoice("playback_speed");
        return chosen.isEmpty() ? original : Float.parseFloat(chosen);
    }
}
