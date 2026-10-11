// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.spoof;

import android.net.Uri;
import android.os.SystemClock;

import java.util.Collections;

import app.reseam.youtube.core.Proto;

/** A validated replacement and its provenance, kept together through the native installation hook. */
public final class StreamingData {
    final byte[] response;
    final ClientType client;
    final long expiresAt;
    final SabrData sabr;
    /** The client's whole player response, kept only for downloads. */
    final byte[] playerResponse;

    StreamingData(byte[] streamingData, ClientType client, byte[] playerResponse) {
        // Direct responses must not advertise a competing SABR transport.
        response = PlayerResponse.withStreamingData(Proto.replace(streamingData,
                Collections.singletonMap(15, null)));
        this.client = client;
        this.playerResponse = playerResponse;
        sabr = null;
        expiresAt = PlayerResponse.streamUrls(streamingData).stream()
                .mapToLong(url -> StreamUrl.expiresAt(Uri.parse(url.url()))).min().orElse(0);
    }

    StreamingData(SabrData sabr, byte[] playerResponse) {
        this.sabr = sabr;
        this.playerResponse = playerResponse;
        response = PlayerResponse.withStreamingData(sabr.streams());
        client = sabr.client();
        expiresAt = StreamUrl.expiresAt(sabr.uri());
    }

    /** Leaves time to start playback before the earliest media URL expires. */
    boolean isFresh() {
        return expiresAt > System.currentTimeMillis() + 60_000
                && (sabr == null || sabr.attestation().expiresAt() > SystemClock.elapsedRealtime() + 30_000);
    }
}
