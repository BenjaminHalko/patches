// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.spoof;

import android.net.Uri;

import app.reseam.youtube.web.WebPlayer;

/** YouTube clients whose player responses can replace the app's own streams. */
enum ClientType {
    // Signed in with the app's account; its SABR URL needs the web player's solved challenges, but no PoToken.
    // Its version and device fields come from TvProfile.
    TV("tv", "TVHTML5", 7, null, null, null, null, null, TvProfile.USER_AGENT, "TV (SABR)", true, true),
    WEB("web", "WEB", 1, null, null, null, null, null,
            WebPlayer.USER_AGENT, "Web (SABR)", true, true),
    // Needs neither, but "made for kids" videos are unavailable.
    VISIONOS("visionos", "VISIONOS", 101, "1.02", "Apple", "RealityDevice17,1", "visionOS", "26.5.23O471",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15",
            "visionOS", false, false),
    // A web client: its stream URLs need the web player's solved challenges and a BotGuard PoToken.
    TV_SIMPLY("tv_simply", "TVHTML5_SIMPLY", 75, "1.0", null, null, null, null,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36",
            "TV Simply", true, false);

    final String settingValue;
    final String clientName;
    final int clientId;
    final String clientVersion;
    final String deviceMake;
    final String deviceModel;
    final String osName;
    final String osVersion;
    final String userAgent;
    /** Shown in stats for nerds. */
    final String friendlyName;
    /** Whether its stream URLs or attestation need {@link WebPlayer}. */
    final boolean usesWebPlayer;
    /** Whether it streams through one SABR URL instead of a direct URL per format. */
    final boolean sabr;

    ClientType(String settingValue, String clientName, int clientId, String clientVersion, String deviceMake,
               String deviceModel, String osName, String osVersion, String userAgent,
               String friendlyName, boolean usesWebPlayer, boolean sabr) {
        this.settingValue = settingValue;
        this.clientName = clientName;
        this.clientId = clientId;
        this.clientVersion = clientVersion;
        this.deviceMake = deviceMake;
        this.deviceModel = deviceModel;
        this.osName = osName;
        this.osVersion = osVersion;
        this.userAgent = userAgent;
        this.friendlyName = friendlyName;
        this.usesWebPlayer = usesWebPlayer;
        this.sabr = sabr;
    }

    /** A media URL issued to one of these clients for direct, per-format GET requests. */
    static boolean issuedDirectUrl(Uri uri) {
        if (!"/videoplayback".equals(uri.getPath())) return false;
        String host = uri.getHost();
        String name = uri.getQueryParameter("c");
        if (host == null || !host.endsWith(".googlevideo.com") || name == null) return false;
        for (ClientType client : values()) {
            if (!client.sabr && client.clientName.equals(name)) return true;
        }
        return false;
    }

    static ClientType fromSetting(String value) {
        for (ClientType client : values()) {
            if (client.settingValue.equals(value)) return client;
        }
        return WEB;
    }
}
