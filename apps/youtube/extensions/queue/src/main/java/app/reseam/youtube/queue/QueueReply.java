// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.queue;

import android.net.Uri;
import android.util.Base64;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.MessageLite;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.reseam.youtube.core.Logger;
import app.reseam.youtube.core.Proto;

public final class QueueReply {
    private static final int REQUEST_QUEUE = 3;
    private static final int REQUEST_OPERATION = 1;
    private static final int REQUEST_TARGET = 2;
    private static final long OPERATION_PLAY_NEXT = 2;
    private static final int RESPONSE_COMMAND = 4;
    private static final int BROWSE_ENDPOINT = 48687626;
    private static final int WATCH_ENDPOINT = 48687757;
    static final int PLAYER_STATE_COMMAND = 469942096;
    private static final int QUEUE_INSERT = 465666947;
    private static final int WATCH_MINIMIZED = 339694216;
    private static final int QUEUE_TOKEN = 71;
    static final long POSITION_NEXT = 1;
    private static final long POSITION_LAST = 2;
    private static final String QUEUE_WATCH_PARAMS = "yAQB";

    private QueueReply() {}

    public static ListenableFuture<Object> wrap(ListenableFuture<Object> response, String params) {
        byte[] command = command(params);
        return command == null ? response : new Replaced(response, command);
    }

    public static Object parse(byte[] response) {
        return null;
    }

    private static byte[] command(String params) {
        if (params == null || params.isEmpty()) return null;
        try {
            byte[] queue = Proto.bytes(Base64.decode(Uri.decode(params), Base64.URL_SAFE), REQUEST_QUEUE);
            if (queue == null) return null;
            String videoId = Proto.string(Proto.bytes(Proto.bytes(queue, REQUEST_TARGET), 1), 1);
            long position = Proto.number(queue, REQUEST_OPERATION, 0) == OPERATION_PLAY_NEXT ? POSITION_NEXT : POSITION_LAST;
            return new Proto.Writer().bytes(PLAYER_STATE_COMMAND, playerStateCommand(videoId, position)).toByteArray();
        } catch (IllegalArgumentException | NullPointerException exception) {
            Logger.debug(() -> "Active-devices parameters are not a queue request");
            return null;
        }
    }

    static byte[] playerStateCommand(String videoId, long position) {
        return new Proto.Writer()
                .message(1, new Proto.Writer().message(QUEUE_INSERT, new Proto.Writer()
                        .string(1, token(videoId, position))))
                .message(2, new Proto.Writer().message(WATCH_MINIMIZED, new Proto.Writer()
                        .message(2, new Proto.Writer().message(WATCH_ENDPOINT, new Proto.Writer()
                                .string(1, videoId)
                                .string(5, QUEUE_WATCH_PARAMS)))))
                .toByteArray();
    }

    private static String token(String videoId, long position) {
        byte[] token = new Proto.Writer().message(QUEUE_TOKEN, new Proto.Writer()
                .message(1, new Proto.Writer().string(1, videoId).number(3, position))).toByteArray();
        return Uri.encode(Base64.encodeToString(token, Base64.URL_SAFE | Base64.NO_WRAP));
    }

    private static final class Replaced implements ListenableFuture<Object> {
        private final ListenableFuture<Object> response;
        private final byte[] command;
        private volatile Object replaced;

        Replaced(ListenableFuture<Object> response, byte[] command) {
            this.response = response;
            this.command = command;
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            response.addListener(listener, executor);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return response.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return response.isCancelled();
        }

        @Override
        public boolean isDone() {
            return response.isDone();
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return replace(response.get());
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return replace(response.get(timeout, unit));
        }

        private Object replace(Object message) {
            Object result = replaced;
            if (result != null) return result;
            byte[] bytes = ((MessageLite) message).toByteArray();
            byte[] current = Proto.bytes(bytes, RESPONSE_COMMAND);
            result = current != null && Proto.bytes(current, BROWSE_ENDPOINT) != null
                    ? parse(Proto.replace(bytes, Collections.singletonMap(RESPONSE_COMMAND, command)))
                    : message;
            replaced = result;
            return result;
        }
    }
}
