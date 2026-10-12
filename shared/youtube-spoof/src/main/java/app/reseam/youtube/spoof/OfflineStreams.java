// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.spoof;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import app.reseam.youtube.core.Logger;
import app.reseam.youtube.core.Proto;

public final class OfflineStreams {
    private static final int STREAMING_DATA = 4;
    private static final int VIDEO_DETAILS = 11;
    private static final int OFFLINE_STATE = 14;
    private static final int VIDEO_ID = 1;
    private static final int ADAPTIVE_FORMATS = 3;
    private static final int FORMAT_MIME_TYPE = 5;
    private static final int FORMAT_OFFLINE_ELIGIBLE = 58;
    private static final String OPUS_AUDIO = "audio/webm";
    private static final int STATE_TOKEN = 1;
    private static final int STATE_VALIDITY = 3;
    private static final int STATE_ACTION = 5;
    private static final long ACTION_OK = 1;
    private static final long VALIDITY_SECONDS = TimeUnit.DAYS.toSeconds(30);
    private static final long RETRY_MILLISECONDS = TimeUnit.SECONDS.toMillis(30);
    private static final long EXPIRY_MARGIN_MILLISECONDS = TimeUnit.MINUTES.toMillis(30);
    private static final int REMEMBERED_VIDEOS = 32;

    private record Answer(byte[] response, long validUntil) {}

    private static final Map<String, Answer> ANSWERS = Collections.synchronizedMap(
            new LinkedHashMap<>(REMEMBERED_VIDEOS, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Answer> eldest) {
                    return size() > REMEMBERED_VIDEOS;
                }
            });

    private OfflineStreams() {}

    public static byte[] replaceRefused(byte[] playerResponse) {
        byte[] details = Proto.bytes(playerResponse, VIDEO_DETAILS);
        String videoId = details == null ? null : Proto.string(details, VIDEO_ID);
        byte[] refusal = Proto.bytes(playerResponse, OFFLINE_STATE);
        if (videoId == null || refusal == null) return null;
        Answer answer = ANSWERS.get(videoId);
        if (answer == null || answer.validUntil <= System.currentTimeMillis()) {
            answer = answer(videoId, refusal);
            ANSWERS.put(videoId, answer);
        }
        return answer.response;
    }

    private static Answer answer(String videoId, byte[] refusal) {
        StreamingData streams = StreamingDataRequest.fetchOffline(videoId);
        if (streams == null || streams.playerResponse == null || streams.sabr != null) {
            Logger.info(() -> "No direct offline streams for " + videoId);
            return new Answer(null, System.currentTimeMillis() + RETRY_MILLISECONDS);
        }
        Logger.info(() -> "Offline streams for " + videoId + " from " + streams.client.friendlyName);
        byte[] response = Proto.replace(streams.playerResponse, Map.of(
                STREAMING_DATA, offlineEligible(Proto.bytes(streams.response, STREAMING_DATA)),
                OFFLINE_STATE, allowed(refusal)));
        return new Answer(response, streams.expiresAt - EXPIRY_MARGIN_MILLISECONDS);
    }

    private static byte[] allowed(byte[] refusal) {
        Proto.Writer state = new Proto.Writer();
        byte[] token = Proto.bytes(refusal, STATE_TOKEN);
        if (token != null) state.bytes(STATE_TOKEN, token);
        return state.number(STATE_VALIDITY, VALIDITY_SECONDS).number(STATE_ACTION, ACTION_OK).toByteArray();
    }

    private static byte[] offlineEligible(byte[] streamingData) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(streamingData.length + 64);
        for (Proto.Field field : Proto.fields(streamingData)) {
            String mime = field.number == ADAPTIVE_FORMATS && field.wireType == Proto.WIRE_LENGTH
                    ? Proto.string(field.bytes, FORMAT_MIME_TYPE) : null;
            if (mime == null || mime.startsWith(OPUS_AUDIO)) {
                output.write(streamingData, field.start, field.end - field.start);
                continue;
            }
            ByteArrayOutputStream format = new ByteArrayOutputStream(field.bytes.length + 3);
            format.write(field.bytes, 0, field.bytes.length);
            Proto.writeNumber(format, FORMAT_OFFLINE_ELIGIBLE, 1);
            Proto.writeBytes(output, ADAPTIVE_FORMATS, format.toByteArray());
        }
        return output.toByteArray();
    }
}
