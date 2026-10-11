// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.queue;

import com.google.protobuf.MessageLite;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import app.reseam.youtube.core.Proto;
import app.reseam.youtube.video.VideoInformation;

public final class QueueContext {
    private static final int WATCH_ENDPOINT = 48687757;
    private static final int ENDPOINT_VIDEO = 1;
    private static final int ENDPOINT_PLAYLIST = 2;
    private static final int ENDPOINT_PLAYLIST_ITEM = 13;
    private static final int CONTEXT_TYPE = 1;
    private static final int CONTEXT_PLAYLIST = 5;
    private static final int CONTEXT_VIDEO = 6;
    private static final int CONTEXT_PLAYLIST_ITEM = 7;
    private static final int CONTEXT_UPDATED = 12;
    private static final long TYPE_QUEUE = 1;
    private static final long TYPE_SINGLE_VIDEO = 3;
    private static final String QUEUE_PLAYLIST_PREFIX = "QP";
    private static final int TRACKED_VIDEOS = 32;

    private record QueueItem(String playlistId, String itemId) {}

    private static final Map<String, QueueItem> QUEUE_ITEMS = Collections.synchronizedMap(
            new LinkedHashMap<>(TRACKED_VIDEOS, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, QueueItem> eldest) {
                    return size() > TRACKED_VIDEOS;
                }
            });

    private QueueContext() {}

    public static void watched(MessageLite currentVideo) {
        if (currentVideo == null) return;
        byte[] endpoint = Proto.bytes(currentVideo.toByteArray(), WATCH_ENDPOINT);
        String videoId = endpoint == null ? null : Proto.string(endpoint, ENDPOINT_VIDEO);
        if (videoId == null) return;
        String playlistId = Proto.string(endpoint, ENDPOINT_PLAYLIST);
        String itemId = Proto.string(endpoint, ENDPOINT_PLAYLIST_ITEM);
        if (itemId != null && playlistId != null && playlistId.startsWith(QUEUE_PLAYLIST_PREFIX)) {
            QUEUE_ITEMS.put(videoId, new QueueItem(playlistId, itemId));
        } else {
            QUEUE_ITEMS.remove(videoId);
        }
    }

    public static byte[] complete(byte[] current) {
        String videoId = VideoInformation.getVideoId();
        if (videoId.isEmpty() || current != null && Proto.bytes(current, CONTEXT_VIDEO) != null) return null;
        QueueItem item = QUEUE_ITEMS.get(videoId);
        if (item == null && current != null) return null;
        Proto.Writer context = new Proto.Writer().number(CONTEXT_TYPE, item == null ? TYPE_SINGLE_VIDEO : TYPE_QUEUE);
        if (item != null) context.string(CONTEXT_PLAYLIST, item.playlistId());
        context.string(CONTEXT_VIDEO, videoId);
        if (item != null) context.string(CONTEXT_PLAYLIST_ITEM, item.itemId());
        byte[] updated = current == null ? null : Proto.bytes(current, CONTEXT_UPDATED);
        if (updated != null) context.bytes(CONTEXT_UPDATED, updated);
        return context.toByteArray();
    }
}
