// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.downloads;

import android.content.Context;

import java.util.Collections;

import app.reseam.youtube.core.Proto;
import app.reseam.youtube.core.YouTubeContext;

public final class QualityPicker {
    private static final int RESPONSE_COMMAND = 2;
    private static final int GET_DOWNLOAD_ACTION = 382320942;
    private static final int UPSELL_DIALOG_MODEL = 39181032;
    private static final int OFFLINE_VIDEO_ENDPOINT = 73080600;
    private static final int OFFLINEABILITY = 60572968;
    private static final long ACTION_ADD = 1;
    private static final long AVAILABILITY_FREE = 1;

    private record Quality(long value, String label) {}

    private static final Quality[] QUALITIES = {
            new Quality(7, "offline_video_quality_1080p"),
            new Quality(2, "offline_video_quality_720p"),
            new Quality(1, "offline_video_quality_360p"),
            new Quality(4, "offline_video_quality_144p"),
    };

    private static volatile String requestedVideoId;

    private QualityPicker() {}

    public static void requested(byte[] command) {
        byte[] action = Proto.bytes(command, GET_DOWNLOAD_ACTION);
        String videoId = action == null ? null : Proto.string(action, 1);
        if (videoId != null) requestedVideoId = videoId;
    }

    static byte[] replaceUpsell(byte[] response) {
        byte[] command = Proto.bytes(response, RESPONSE_COMMAND);
        String videoId = requestedVideoId;
        if (command == null || videoId == null || !contains(command, UPSELL_DIALOG_MODEL)) return null;
        return Proto.replace(response, Collections.singletonMap(RESPONSE_COMMAND, addCommand(videoId)));
    }

    private static byte[] addCommand(String videoId) {
        Proto.Writer offlineability = new Proto.Writer().number(1, 1);
        Context context = YouTubeContext.get();
        for (Quality quality : QUALITIES) {
            int label = context.getResources().getIdentifier(quality.label(), "string", context.getPackageName());
            offlineability.message(3, new Proto.Writer()
                    .message(1, new Proto.Writer().string(4, context.getString(label)))
                    .number(3, quality.value())
                    .number(4, AVAILABILITY_FREE));
        }
        return new Proto.Writer().message(OFFLINE_VIDEO_ENDPOINT, new Proto.Writer()
                .string(1, videoId)
                .number(2, ACTION_ADD)
                .message(4, new Proto.Writer().message(OFFLINEABILITY, offlineability))).toByteArray();
    }

    private static boolean contains(byte[] message, int number) {
        try {
            for (Proto.Field field : Proto.fields(message)) {
                if (field.wireType == Proto.WIRE_LENGTH && (field.number == number || contains(field.bytes, number))) return true;
            }
            return false;
        } catch (IllegalArgumentException notMessage) {
            return false;
        }
    }
}
