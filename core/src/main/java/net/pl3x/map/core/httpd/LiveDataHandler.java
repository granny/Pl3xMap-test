/*
 * MIT License
 *
 * Copyright (c) 2020-2023 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.pl3x.map.core.httpd;

import io.undertow.Handlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LiveDataHandler {
    private ServerSentEventHandler serverSentEventHandler;

    public LiveDataHandler() {
        this.serverSentEventHandler = Handlers.serverSentEvents();
    }

    /**
     *
     * @param event   The message event
     * @param data    The message data
     * @param success The callback that is called when a message is sucessfully sent.
     * @param failure The callback that is called when a message send fails.
     */
    public void send(String event, String data, SuccessCallback success, FailureCallback failure) {
        if (serverSentEventHandler == null) {
            return;
        }

        Callback callback = new Callback(success, failure);
        for (ServerSentEventConnection connection : this.serverSentEventHandler.getConnections()) {
            connection.send(data, event, null, callback);
        }
    }

    /**
     *
     * @param event   The message event
     * @param data    The message data
     * @param success The callback that is called when a message is sucessfully sent.
     */
    public void send(String event, String data, SuccessCallback success) {
        this.send(event, data, success, null);
    }

    /**
     *
     * @param event   The message event
     * @param data    The message data
     */
    public void send(String event, String data) {
        this.send(event, data, null, null);
    }

    /**
     *
     * @param data    The message data
     */
    public void send(String data) {
        this.send(null, data);
    }

    public void closeConnections() {
        for (ServerSentEventConnection connection : serverSentEventHandler.getConnections()) {
            connection.shutdown();
        }
    }

    public void handle(HttpServerExchange exchange) throws Exception {
        this.serverSentEventHandler.handleRequest(exchange);
    }

    public ServerSentEventHandler get() {
        return this.serverSentEventHandler;
    }

    /**
     * Notification that is called when a message is sucessfully sent
     */
    @FunctionalInterface
    public interface SuccessCallback {
        /**
         * @param connection The connection
         * @param data       The message data
         * @param event      The message event
         * @param id         The message id
         */
        void apply(@NotNull ServerSentEventConnection connection, @Nullable String data, @Nullable String event, @Nullable String id);
    }

    /**
     * Notification that is called when a message send fails.
     */
    @FunctionalInterface
    public interface FailureCallback {
        /**
         * @param connection The connection
         * @param data       The message data
         * @param event      The message event
         * @param id         The message id
         * @param exception  The exception
         */
        void apply(@NotNull ServerSentEventConnection connection, @Nullable String data, @Nullable String event, @Nullable String id, @NotNull IOException exception);
    }

    private class Callback implements ServerSentEventConnection.EventCallback {
        private SuccessCallback success;
        private FailureCallback failure;

        public Callback(SuccessCallback success, FailureCallback failure) {
            this.success = success;
            this.failure = failure;
        }

        @Override
        public void done(ServerSentEventConnection connection, String data, String event, String id) {
            if (success != null) {
                success.apply(connection, data, event, id);
            }
        }

        @Override
        public void failed(ServerSentEventConnection connection, String data, String event, String id, IOException e) {
            if (failure != null) {
                failure.apply(connection, data, event, id, e);
            }
        }
    }
}
