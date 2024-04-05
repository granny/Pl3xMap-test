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
package net.pl3x.map.core.markers.layer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a custom layer of markers and other metadata.
 */
public class CustomLayer extends WorldLayer {
    /**
     * Create a new custom layer.
     *
     * @param key key for layer
     */
    public CustomLayer(@NotNull String key, @NotNull World world, @NotNull Supplier<@NotNull String> labelSupplier) {
        super(key, world, labelSupplier);
    }

    public static void load(World world, Path path) {
        CustomLayer layer;
        try {
            JsonObject obj = (JsonObject) JsonParser.parseReader(new FileReader(path.toFile()));
            layer = fromJson(world, obj);
            if (obj.get("markers") instanceof JsonArray markers) {
                markers.forEach(el -> layer.addMarker(Marker.fromJson(el.getAsJsonObject())));
            }
        } catch (Throwable t) {
            Logger.severe("Error reading custom marker file: " + path.toAbsolutePath(), t);
            return;
        }
        world.getLayerRegistry().register(layer.getKey(), layer);
    }

    public static @NotNull CustomLayer fromJson(@NotNull World world, @NotNull JsonObject obj) {
        JsonElement el;
        CustomLayer layer = new CustomLayer(obj.get("key").getAsString(), world, () -> obj.get("label").getAsString());
        if ((el = obj.get("updateInterval")) != null && !(el instanceof JsonNull))
            layer.setUpdateInterval(el.getAsInt());
        if ((el = obj.get("showControls")) != null && !(el instanceof JsonNull))
            layer.setShowControls(el.getAsBoolean());
        if ((el = obj.get("defaultHidden")) != null && !(el instanceof JsonNull))
            layer.setDefaultHidden(el.getAsBoolean());
        if ((el = obj.get("priority")) != null && !(el instanceof JsonNull)) layer.setPriority(el.getAsInt());
        if ((el = obj.get("zIndex")) != null && !(el instanceof JsonNull)) layer.setZIndex(el.getAsInt());
        if ((el = obj.get("pane")) != null && !(el instanceof JsonNull)) layer.setPane(el.getAsString());
        if ((el = obj.get("css")) != null && !(el instanceof JsonNull)) layer.setCss(el.getAsString());
        return layer;
    }
}
