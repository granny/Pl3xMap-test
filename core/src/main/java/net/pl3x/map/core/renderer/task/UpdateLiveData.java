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
package net.pl3x.map.core.renderer.task;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

public class UpdateLiveData extends AbstractDataTask {
    private final Cache<@NotNull String, Integer> markerCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();
    private Map<String, CompletableFuture<Void>> liveUpdateFutures;

    public UpdateLiveData(@NotNull World world, int threads) {
        super(1, true, world, "Pl3xMap-LiveData", threads);
        this.liveUpdateFutures = new HashMap<>();
    }

    @Override
    public void cancel() {
        super.cancel();
        this.liveUpdateFutures.forEach((key, future) -> future.cancel(true));
        this.liveUpdateFutures.clear();
    }

    @Override
    public void parse() {
        this.world.getLayerRegistry().entrySet().forEach(entry -> {
            String key = entry.getKey();
            Layer layer = entry.getValue();

            if (!layer.isLiveUpdate()) {
                return;
            }

            CompletableFuture<Void> future = liveUpdateFutures.get(key);
            if (future != null && !future.isDone()) {
                return;
            }

            this.liveUpdateFutures.put(key, CompletableFuture.runAsync(() -> {
                try {
                    List<Marker<?>> list = new ArrayList<>(layer.getMarkers());
                    Integer markerCacheIfPresent = markerCache.getIfPresent(key);
                    int markerHashCode = list.hashCode();
                    if (markerCacheIfPresent == null || !markerCacheIfPresent.equals(markerHashCode)) {
                        Logger.debug("[%s/%s] sending through sse %d".formatted(this.world.getName(), key, (System.currentTimeMillis())));
                        world.getServerSentEventHandler().send("markers", String.format("{\"key\": \"%s\", \"markers\": %s}", key, this.gson.toJson(list)));
                        markerCache.put(key, markerHashCode);
                    }
                } catch (Throwable t) {
                    Logger.debug("[%s/%s] failed".formatted(this.world.getName(), key));
                    t.printStackTrace();
                } finally {
                    this.liveUpdateFutures.remove(key);
                }
            }, this.executor));
        });
    }
}
