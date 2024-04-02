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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.registry.RendererRegistry;
import net.pl3x.map.core.renderer.Renderer;
import net.pl3x.map.core.util.Mathf;
import net.pl3x.map.core.world.Region;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegionScanTask implements Runnable {
    private final World world;
    private final Point regionPos;

    private final Map<@NotNull String, @NotNull Renderer> renderers = new LinkedHashMap<>();

    public RegionScanTask(@NotNull World world, @NotNull Point regionPos) {
        this.world = world;
        this.regionPos = regionPos;

        RendererRegistry registry = Pl3xMap.api().getRendererRegistry();
        List<Renderer.Builder> rendererBuilders = new ArrayList<>(this.world.getRenderers().values());

        String blockInfo = this.world.getConfig().UI_BLOCKINFO;
        if (blockInfo != null && !blockInfo.isEmpty()) {
            rendererBuilders.add(registry.get(RendererRegistry.BLOCKINFO));
        }

        rendererBuilders.forEach(builder -> {
            Renderer renderer = registry.createRenderer(this, builder);
            this.renderers.put(renderer.getKey(), renderer);
        });
    }

    public @NotNull World getWorld() {
        return this.world;
    }

    public @Nullable Renderer getRenderer(@NotNull String id) {
        return this.renderers.get(id);
    }

    public void cleanup() {
        this.renderers.clear();
    }

    @Override
    public void run() {
        try {
            Logger.debug("[" + this.world.getName() + "] Scanning " + regionPos + " -- " + Thread.currentThread().getName());

            Pl3xMap.api().getRegionProcessor().checkPaused();

            allocateImages();

            Pl3xMap.api().getRegionProcessor().checkPaused();

            scanRegion(loadRegion());

            Pl3xMap.api().getRegionProcessor().checkPaused();

            saveImages();
        } catch (Throwable t) {
            Logger.severe("Failed to scan world %s at region %s".formatted(world.getName(), regionPos), t);
        }
    }

    private void allocateImages() {
        for (Renderer renderer : this.renderers.values()) {
            Pl3xMap.api().getRegionProcessor().checkPaused();
            renderer.allocateData(this.regionPos);
        }
    }

    private @NotNull Region loadRegion() {
        Region region = this.world.getRegion(null, this.regionPos.x(), this.regionPos.z());
        try {
            region.loadChunks();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return region;
    }

    private void scanRegion(@NotNull Region region) {
        for (Renderer renderer : this.renderers.values()) {
            Pl3xMap.api().getRegionProcessor().checkPaused();
            renderer.scanData(region);
        }
        Pl3xMap.api().getRegionProcessor().getProgress().increment();
    }

    private void saveImages() {
        for (Renderer renderer : this.renderers.values()) {
            Pl3xMap.api().getRegionProcessor().checkPaused();
            renderer.saveData(this.regionPos);
        }
        // set region modified time
        world.getRegionModifiedState().set(Mathf.asLong(this.regionPos), System.currentTimeMillis());
    }
}
