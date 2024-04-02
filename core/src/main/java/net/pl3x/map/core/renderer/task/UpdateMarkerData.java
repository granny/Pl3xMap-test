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

import java.util.ArrayList;
import java.util.List;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.util.TickUtil;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

public class UpdateMarkerData extends AbstractDataTask {
    public UpdateMarkerData(@NotNull World world) {
        super(TickUtil.toTicks(1), true, world, "Pl3xMap-Markers");
    }

    @Override
    public void parse() {
        List<Object> layers = new ArrayList<>();

        this.world.getLayerRegistry().entrySet().forEach(entry -> {
            String key = entry.getKey();
            Layer layer = entry.getValue();
            try {
                layers.add(layer.toJson());

                long now = System.currentTimeMillis();
                long lastUpdated = this.lastUpdated.getOrDefault(key, 0L);

                if (now - lastUpdated > Math.max(TickUtil.toMilliseconds(layer.getUpdateInterval()), 1000)) {
                    List<Marker<?>> list = new ArrayList<>(layer.getMarkers());
                    FileUtil.writeJson(this.gson.toJson(list), this.world.getMarkersDirectory().resolve(key.replace(":", "-") + ".json"));
                    this.lastUpdated.put(key, now);
                }
            } catch (Throwable t) {
                Logger.severe("Failed to process layer " + key + "for world " + world.getName(), t);
            }
        });

        FileUtil.writeJson(this.gson.toJson(layers), this.world.getTilesDirectory().resolve("markers.json"));
    }
}
