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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.JsonObjectWrapper;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.scheduler.Task;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDataTask extends Task {
    protected final Gson gson = new GsonBuilder()
            //.setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .setLenient()
            .registerTypeHierarchyAdapter(Marker.class, new Adapter())
            .create();

    protected final World world;
    protected final Map<@NotNull String, @NotNull Long> lastUpdated = new HashMap<>();
    protected final ExecutorService executor;
    protected final String executorName;

    protected CompletableFuture<Void> future;
    protected boolean running;

    public AbstractDataTask(int delay, boolean repeat, World world, String serviceName, int threads) {
        super(delay, repeat);
        this.world = world;
        this.executor = Pl3xMap.ThreadFactory.createService(serviceName, threads);
        this.executorName = serviceName;
    }

    public AbstractDataTask(int delay, boolean repeat, World world, String serviceName) {
        super(delay, repeat);
        this.world = world;
        this.executor = Pl3xMap.ThreadFactory.createService(serviceName);
        this.executorName = serviceName;
    }

    @Override
    public void run() {
        if (this.running) {
            return;
        }
        this.running = true;
        this.future = CompletableFuture.runAsync(() -> {
            try {
                parse();
            } catch (Throwable t) {
                Logger.severe("Failed to parse task %s for world %s".formatted(executorName, world.getName()), t);
            }
            this.running = false;
        }, this.executor);
    }

    @Override
    public void cancel() {
        super.cancel();
        if (this.future != null) {
            this.future.cancel(true);
        }
    }

    public abstract void parse();

    protected static class Adapter implements JsonSerializer<@NotNull Marker<?>> {
        @Override
        public @NotNull JsonElement serialize(@NotNull Marker<?> marker, @NotNull Type type, @NotNull JsonSerializationContext context) {
            JsonObjectWrapper wrapper = new JsonObjectWrapper();
            wrapper.addProperty("type", marker.getType());
            wrapper.addProperty("data", marker);
            wrapper.addProperty("options", marker.getOptions());
            return wrapper.getJsonObject();
        }
    }
}
