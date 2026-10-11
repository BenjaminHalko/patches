// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.spoof;

import java.util.LinkedHashMap;
import java.util.Map;

/** Tracks which client each installed response came from, for stats for nerds. */
final class InstalledStreams {
    private static final int MAX_RESPONSES = 50;
    private static final Map<String, Entry> CLIENTS = new LinkedHashMap<>();
    private static String currentVideoId;

    private record Entry(ClientType client, long expiresAt) {}

    private InstalledStreams() {}

    static synchronized void installed(String videoId, StreamingData data) {
        prune();
        CLIENTS.remove(videoId);
        CLIENTS.put(videoId, new Entry(data.client, data.expiresAt));
        if (CLIENTS.size() > MAX_RESPONSES) {
            CLIENTS.keySet().stream().filter(id -> !id.equals(currentVideoId)).findFirst().ifPresent(CLIENTS::remove);
        }
    }

    static synchronized void nativeInstalled(String videoId) {
        CLIENTS.remove(videoId);
    }

    static synchronized void videoChanged(String videoId) {
        currentVideoId = videoId;
    }

    static synchronized String clientName() {
        prune();
        Entry entry = CLIENTS.get(currentVideoId);
        return entry == null ? "Native" : entry.client.friendlyName;
    }

    private static void prune() {
        long now = System.currentTimeMillis();
        CLIENTS.values().removeIf(entry -> entry.expiresAt <= now);
    }
}
