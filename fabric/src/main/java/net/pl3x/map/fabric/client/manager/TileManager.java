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
package net.pl3x.map.fabric.client.manager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import net.pl3x.map.core.scheduler.Task;
import net.pl3x.map.core.util.Mathf;
import net.pl3x.map.core.util.TickUtil;
import net.pl3x.map.fabric.client.Pl3xMapFabricClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

public class TileManager {
    private static final BufferedImage EMPTY_IMAGE = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);

    private final Map<@NotNull String, @NotNull LoadingCache<@NotNull Long, @NotNull BufferedImage>> tiles = new ConcurrentHashMap<>();
    private final Pl3xMapFabricClient mod;

    private Task task;

    public TileManager(@NotNull Pl3xMapFabricClient mod) {
        this.mod = mod;
    }

    public void initialize() {
        // remove any old tasks (just in case)
        if (this.task != null) {
            this.task.cancel();
        }
        // update once next tick
        this.mod.getScheduler().addTask(0, this::update);
        // setup repeating task to update every 5 seconds
        this.task = new Task(TickUtil.toTicks(5), true) {
            @Override
            public void run() {
                update();
            }
        };
        // schedule task
        this.mod.getScheduler().addTask(this.task);
    }

    public @NotNull BufferedImage get(@NotNull String world, int regionX, int regionZ) {
        try {
            return this.tiles.computeIfAbsent(world, k -> Loader.create(this.mod, world)).get(Mathf.asLong(regionX, regionZ));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        this.tiles.forEach((world, cache) -> cache.invalidateAll());
        this.tiles.clear();
    }

    public void update() {
        if (RenderSystem.isOnRenderThread()) {
            this.mod.getExecutor().submit(this::update);
            return;
        }
        for (Map.Entry<String, LoadingCache<Long, BufferedImage>> entry : this.tiles.entrySet()) {
            LoadingCache<Long, BufferedImage> cache = entry.getValue();
            Collections.unmodifiableCollection(cache.asMap().keySet()).forEach(cache::refresh);
        }
        this.mod.updateAllMapTextures();
    }

    private static class Loader extends CacheLoader<@NotNull Long, @NotNull BufferedImage> {
        private static @NotNull LoadingCache<@NotNull Long, @NotNull BufferedImage> create(@NotNull Pl3xMapFabricClient mod, @NotNull String world) {
            return CacheBuilder.newBuilder().maximumSize(100).build(new Loader(mod, world));
        }

        private final Pl3xMapFabricClient mod;
        private final String world;

        private Loader(@NotNull Pl3xMapFabricClient mod, @NotNull String world) {
            this.mod = mod;
            this.world = world;
        }

        @Override
        public @NotNull BufferedImage load(@NotNull Long region) {
            if (this.mod.getServerUrl() == null) {
                return EMPTY_IMAGE;
            }

            String url = String.format("%s/tiles/%s/%d/%s/%d_%d.png",
                    this.mod.getServerUrl(),
                    this.world,
                    0,
                    "vintage_story",
                    Mathf.longToX(region),
                    Mathf.longToZ(region)
            );

            BufferedImage image = null;
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                try (CloseableHttpResponse response = httpclient.execute(new HttpGet(url));
                     InputStream stream = response.getEntity().getContent()) {
                    image = ImageIO.read(stream);
                }
            } catch (IOException ignore) {
            }

            return image == null ? EMPTY_IMAGE : image;
        }
    }
}
