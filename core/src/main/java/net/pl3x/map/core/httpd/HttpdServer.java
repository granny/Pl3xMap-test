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
import io.undertow.Undertow;
import io.undertow.UndertowLogger;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.ETag;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.configuration.Lang;
import net.pl3x.map.core.log.LogFilter;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.registry.WorldRegistry;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.world.World;

public class HttpdServer {
    private HttpString X_ACCEL_BUFFERING = new HttpString("X-Accel-Buffering");
    private Undertow server;
    private LiveDataHandler liveDataHandler = new LiveDataHandler();

    public LiveDataHandler getLiveDataHandler() {
        return liveDataHandler;
    }

    public void startServer() {
        if (!Config.HTTPD_ENABLED) {
            Logger.info(Lang.HTTPD_DISABLED);
            return;
        }

        try {
            ResourceManager resourceManager = PathResourceManager.builder()
                    .setBase(Paths.get(FileUtil.getWebDir().toFile().getAbsolutePath()))
                    .setFollowLinks(Config.HTTPD_FOLLOW_SYMLINKS)
                    .setETagFunction((path) -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                            long time = attr.lastModifiedTime().toMillis();
                            return new ETag(false, Long.toString(time));
                        } catch (IOException e) {
                            Logger.severe("Failed to read basic file attributes from file %s".formatted(path.toAbsolutePath()), e);
                            return null;
                        }
                    })
                    .build();
            ResourceHandler resourceHandler = new ResourceHandler(resourceManager, exchange -> {
                String url = exchange.getRelativePath();
                if (url.startsWith("/tiles") && (url.endsWith(".png") || url.endsWith(".gz"))) {
                    exchange.setStatusCode(200);
                    return;
                }
                exchange.setStatusCode(404);
                if (UndertowLogger.PREDICATE_LOGGER.isDebugEnabled()) {
                    UndertowLogger.PREDICATE_LOGGER.debugf("Response code set to [%s] for %s.", 404, exchange);
                }
            });

            LogFilter.HIDE_UNDERTOW_LOGS = true;
            this.server = Undertow.builder()
                    .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                    .addHttpListener(Config.HTTPD_PORT, Config.HTTPD_BIND)
                    .setHandler(
                        Handlers.path(exchange -> {
                            if (exchange.getRelativePath().startsWith("/tiles")) {
                                exchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "max-age=0, must-revalidate, no-cache");
                            }
                            if (exchange.getRelativePath().endsWith(".gz")) {
                                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                                exchange.getResponseHeaders().put(Headers.CONTENT_ENCODING, "gzip");
                            }
                            resourceHandler.handleRequest(exchange);
                        })
                        .addPrefixPath("/sse",
                            Handlers.pathTemplate()
                                .add("{world}", exchange -> {
                                    String worldName = exchange.getQueryParameters().get("world").peek();
                                    if (worldName == null || worldName.isEmpty()) {
                                        exchange.getResponseHeaders().put(X_ACCEL_BUFFERING, "no");
                                        liveDataHandler.handle(exchange);
                                        return;
                                    }

                                    WorldRegistry worldRegistry = Pl3xMap.api().getWorldRegistry();
                                    World world = worldRegistry.get(worldName);
                                    if (world == null || !world.isEnabled()) {
                                        String listOfValidWorlds = worldRegistry.values().stream()
                                                .filter(World::isEnabled)
                                                .map(World::getName).collect(Collectors.joining(", "));
                                        handleError(exchange, "Could not find world named '%s'. Available worlds: %s"
                                                .formatted(worldName, listOfValidWorlds));
                                        exchange.endExchange();
                                        return;
                                    }

                                    if (exchange.isInIoThread()) {
                                        exchange.dispatch(world.getServerSentEventHandler().get());
                                    } else {
                                        exchange.getResponseHeaders().put(X_ACCEL_BUFFERING, "no");
                                        world.getServerSentEventHandler().handle(exchange);
                                    }
                                })
                        )
                    )
                    .build();
            this.server.start();
            LogFilter.HIDE_UNDERTOW_LOGS = false;

            Logger.info(Lang.HTTPD_STARTED
                    .replace("<bind>", Config.HTTPD_BIND)
                    .replace("<port>", Integer.toString(Config.HTTPD_PORT))
            );
        } catch (Exception e) {
            this.server = null;
            Logger.severe(Lang.HTTPD_START_ERROR, e);
        }
    }

    private void handleError(HttpServerExchange exchange, String errorMessage) {
        exchange.setStatusCode(StatusCodes.NOT_FOUND);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send("{\"error\": \"" + errorMessage + "\"}");
    }

    public void stopServer() {
        if (!Config.HTTPD_ENABLED) {
            return;
        }

        if (this.server == null) {
            Logger.warn(Lang.HTTPD_STOP_ERROR);
            return;
        }

        LogFilter.HIDE_UNDERTOW_LOGS = true;
        this.liveDataHandler.closeConnections();
        Pl3xMap.api().getWorldRegistry().forEach(world -> {
            world.getServerSentEventHandler().closeConnections();
        });
        this.server.stop();
        LogFilter.HIDE_UNDERTOW_LOGS = false;

        this.server = null;
        Logger.info(Lang.HTTPD_STOPPED);
    }
}
