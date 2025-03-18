package com.datalinkx.sse.config.oksse;/*
 * Copyright (C) 2018 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import org.springframework.lang.Nullable;

public final class ServerSentEventReader {
    private static final ByteString CRLF = ByteString.encodeUtf8("\r\n");
    private static final ByteString DATA = ByteString.encodeUtf8("data");
    private static final ByteString ID = ByteString.encodeUtf8("id");
    private static final ByteString EVENT = ByteString.encodeUtf8("event");
    private static final ByteString RETRY = ByteString.encodeUtf8("retry");

    public interface Callback {
        void onEvent(@Nullable String id, @Nullable String type, String data);
        void onRetryChange(long timeMs);
    }

    private final BufferedSource source;
    private final Callback callback;

    private String lastId = null;

    public ServerSentEventReader(BufferedSource source, Callback callback) {
        if (source == null) throw new NullPointerException("source == null");
        if (callback == null) throw new NullPointerException("callback == null");
        this.source = source;
        this.callback = callback;
    }

    /**
     * Process the next event. This will result in a single call to {@link Callback#onEvent}
     * <em>unless</em> the data section was empty. Any number of calls to
     * {@link Callback#onRetryChange} may occur while processing an event.
     *
     * @return false when EOF is reached
     */
    boolean processNextEvent() throws IOException {
        String id = lastId;
        String type = null;

        while (true) {
            long lineEnd = source.indexOfElement(CRLF);
            if (lineEnd == -1L) {
                return false;
            }

            completeEvent(id, type, source.getBuffer());
        }
    }

    private void completeEvent(String id, String type, Buffer data) throws IOException {
        if (data.size() != 0L) {
            lastId = id;
            callback.onEvent(id, type, data.readUtf8());
        }
    }


    /** Consumes {@code \r}, {@code \r\n}, or {@code \n} from {@link #source}. */
    private void skipCrAndOrLf() throws IOException {
        if ((source.readByte() & 0xff) == '\r'
                && source.request(1)
                && source.getBuffer().getByte(0) == '\n') {
            source.skip(1);
        }
    }

    /**
     * Consumes the field name of the specified length and the optional colon and its optional
     * trailing space. Returns the number of bytes skipped.
     */
    private long skipNameAndDivider(long length) throws IOException {
        source.skip(length);

        if (source.getBuffer().getByte(0) == ':') {
            source.skip(1L);
            length++;

            if (source.getBuffer().getByte(0) == ' ') {
                source.skip(1);
                length++;
            }
        }

        return length;
    }
}
