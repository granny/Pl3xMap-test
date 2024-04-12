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
package net.pl3x.map.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import net.kyori.adventure.platform.AudienceProvider;
import net.pl3x.map.core.configuration.ColorsConfig;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.configuration.Lang;
import net.pl3x.map.core.configuration.PlayersLayerConfig;
import net.pl3x.map.core.configuration.SpawnLayerConfig;
import net.pl3x.map.core.configuration.WorldBorderLayerConfig;
import net.pl3x.map.core.event.EventRegistry;
import net.pl3x.map.core.event.server.Pl3xMapDisabledEvent;
import net.pl3x.map.core.event.server.Pl3xMapEnabledEvent;
import net.pl3x.map.core.httpd.HttpdServer;
import net.pl3x.map.core.image.io.IO;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.metrics.Metrics;
import net.pl3x.map.core.player.PlayerRegistry;
import net.pl3x.map.core.registry.BlockRegistry;
import net.pl3x.map.core.registry.IconRegistry;
import net.pl3x.map.core.registry.RendererRegistry;
import net.pl3x.map.core.registry.WorldRegistry;
import net.pl3x.map.core.renderer.heightmap.HeightmapRegistry;
import net.pl3x.map.core.renderer.task.RegionDoubleChecker;
import net.pl3x.map.core.renderer.task.RegionProcessor;
import net.pl3x.map.core.renderer.task.UpdateSettingsData;
import net.pl3x.map.core.scheduler.Scheduler;
import net.pl3x.map.core.util.Mathf;

import net.pl3x.map.core.world.Biome;
import net.pl3x.map.core.world.Block;
import net.pl3x.map.core.world.Blocks;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Pl3xMap {
    public static @NotNull Pl3xMap api() {
        return Provider.api();
    }

    private final boolean isBukkit;

    private final Attributes manifestAttributes;
    private final HttpdServer httpdServer;
    private final RegionProcessor regionProcessor;
    private final RegionDoubleChecker regionDoubleChecker;
    private final Scheduler scheduler;

    private final BlockRegistry blockRegistry;
    private final EventRegistry eventRegistry;
    private final HeightmapRegistry heightmapRegistry;
    private final IconRegistry iconRegistry;
    private final PlayerRegistry playerRegistry;
    private final RendererRegistry rendererRegistry;
    private final WorldRegistry worldRegistry;

    private ExecutorService renderExecutor;

    private String commit;
    private Metrics metrics;
    private boolean enabled;

    public Pl3xMap(boolean isBukkit) {
        this.isBukkit = isBukkit;

        try {
            Field api = Provider.class.getDeclaredField("api");
            api.setAccessible(true);
            api.set(null, this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Manifest manifest;
        try (JarFile jarFile = new JarFile(getJarPath().toFile())) {
            JarEntry entry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
            try (InputStream in = new BufferedInputStream(jarFile.getInputStream(entry))) {
                manifest = new Manifest(in);
            }
        } catch (Exception e) {
            manifest = new Manifest();
            Logger.severe("Failed to obtain Manifest from META-INF", e);
        }
        this.manifestAttributes = manifest.getMainAttributes();

        // setup internal server
        this.httpdServer = new HttpdServer();

        // setup tasks
        this.regionProcessor = new RegionProcessor();
        this.regionDoubleChecker = new RegionDoubleChecker();
        this.scheduler = new Scheduler();

        // setup registries
        this.blockRegistry = new BlockRegistry();
        this.eventRegistry = new EventRegistry();
        this.heightmapRegistry = new HeightmapRegistry();
        this.iconRegistry = new IconRegistry();
        this.playerRegistry = new PlayerRegistry();
        this.rendererRegistry = new RendererRegistry();
        this.worldRegistry = new WorldRegistry();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public @NotNull HttpdServer getHttpdServer() {
        return this.httpdServer;
    }

    public @NotNull RegionProcessor getRegionProcessor() {
        return this.regionProcessor;
    }

    public @NotNull RegionDoubleChecker getRegionDoubleChecker() {
        return this.regionDoubleChecker;
    }

    public @NotNull BlockRegistry getBlockRegistry() {
        return this.blockRegistry;
    }

    public @NotNull EventRegistry getEventRegistry() {
        return this.eventRegistry;
    }

    public @NotNull HeightmapRegistry getHeightmapRegistry() {
        return this.heightmapRegistry;
    }

    public @NotNull IconRegistry getIconRegistry() {
        return this.iconRegistry;
    }

    public @NotNull PlayerRegistry getPlayerRegistry() {
        return this.playerRegistry;
    }

    public @NotNull RendererRegistry getRendererRegistry() {
        return this.rendererRegistry;
    }

    public @NotNull WorldRegistry getWorldRegistry() {
        return this.worldRegistry;
    }

    public @NotNull ExecutorService getRenderExecutor() {
        return this.renderExecutor;
    }

    public @NotNull Scheduler getScheduler() {
        return this.scheduler;
    }

    public void enable() {
        // load up configs
        Logger.debug("Loading configs");
        Config.reload();
        Lang.reload();
        ColorsConfig.reload();
        PlayersLayerConfig.reload();
        SpawnLayerConfig.reload();
        WorldBorderLayerConfig.reload();

        // initialize block registry
        getBlockRegistry().init();

        // initialize icons
        getIconRegistry().init();

        // load blocks _after_ we loaded colors
        Logger.debug("Registering blocks");
        Blocks.registerDefaults();
        loadBlocks();

        // create the executor service
        Logger.debug("Creating services");
        this.renderExecutor = ThreadFactory.createService("Pl3xMap-Renderer", Config.RENDER_THREADS);

        // register built in tile image types
        Logger.debug("Registering tile image types");
        IO.register();

        // register built-in heightmaps
        Logger.debug("Registering heightmaps");
        getHeightmapRegistry().register();

        // register built-in renderers
        Logger.debug("Registering renderers");
        getRendererRegistry().register();

        this.enabled = true;

        new Pl3xMapEnabledEvent().callEvent();

        // load up already loaded worlds
        Logger.debug("Registering worlds");
        loadWorlds();

        // load up players already connected to the server
        Logger.debug("Registering players");
        loadPlayers();

        // start integrated server
        Logger.debug("Starting internal server");
        getHttpdServer().startServer();

        // start tasks
        Logger.debug("Starting region processor");
        getRegionProcessor().start(10000L);
        getRegionDoubleChecker().start(250000L);

        Logger.debug("Starting update settings data task");
        getScheduler().addTask(new UpdateSettingsData());

        Logger.info("Platform: " + getPlatform());
        Logger.info("Version: " + getVersion());

        try {
            this.metrics = new Metrics(this);
        } catch (IOException e) {
            Logger.severe("Failed to generate or load default bStats config", e);
        }

        Pl3xMap.api().getRegionProcessor().setPaused(false);
    }

    public void disable() {
        Pl3xMap.api().getRegionProcessor().setPaused(true);

        if (this.metrics != null) {
            this.metrics.shutdown();
            this.metrics = null;
        }

        // stop tasks
        Logger.debug("Stopping tasks");
        getScheduler().cancelAll();
        getRegionDoubleChecker().stop();
        getRegionProcessor().stop();
        if (this.renderExecutor != null) {
            this.renderExecutor.shutdownNow();
        }

        // stop integrated server
        Logger.debug("Stopping internal server");
        getHttpdServer().stopServer();

        this.enabled = false;

        new Pl3xMapDisabledEvent().callEvent();

        // unload all players
        Logger.debug("Unregistering players");
        getPlayerRegistry().unregister();

        // unload all map worlds
        Logger.debug("Unregistering worlds");
        getWorldRegistry().unregister();

        // unregister renderers
        Logger.debug("Unregistering renderers");
        getRendererRegistry().unregister();

        // unregister icons
        Logger.debug("Unregistering icons");
        getIconRegistry().unregister();

        // unregister heightmaps
        Logger.debug("Unregistering heightmaps");
        getHeightmapRegistry().unregister();

        // unregister tile image types
        Logger.debug("Unregistering tile image types");
        IO.unregister();

        // unregister blocks
        Logger.debug("Unregistering blocks");
        getBlockRegistry().unregister();

        System.gc();
    }

    public boolean isBukkit() {
        return this.isBukkit;
    }

    public abstract @NotNull String getPlatform();

    public abstract @NotNull String getVersion();

    public @NotNull String getVersionCommit() {
        if (this.commit == null) {
            this.commit = this.manifestAttributes.getValue("Git-Commit");
            if (this.commit == null) {
                this.commit = "unknown";
            }
        }
        return this.commit;
    }

    public abstract int getMaxPlayers();

    public abstract boolean getOnlineMode();

    public abstract String getServerVersion();

    public abstract @NotNull AudienceProvider adventure();

    public abstract @NotNull Path getMainDir();

    public abstract @NotNull Path getJarPath();

    public abstract int getColorForPower(byte power);

    public abstract @Nullable Block getFlower(@NotNull World world, @NotNull Biome biome, int blockX, int blockY, int blockZ);

    protected abstract void loadBlocks();

    protected abstract void loadWorlds();

    protected abstract void loadPlayers();

    public abstract @NotNull World cloneWorld(@NotNull World world);

    protected static final class Provider {
        static Pl3xMap api;

        static @NotNull Pl3xMap api() {
            return Provider.api;
        }
    }

    public static final class ThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        private final String name;
        private final int threads;

        private final AtomicInteger id = new AtomicInteger();

        public ThreadFactory(@NotNull String name, int threads) {
            this.name = name;
            this.threads = threads;
        }

        public static @NotNull ExecutorService createService(@NotNull String name) {
            return createService(new ThreadFactory(name, 1));
        }

        public static @NotNull ExecutorService createService(@NotNull String name, int threads) {
            int max = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
            int parallelism = Mathf.clamp(1, max, threads < 1 ? max : threads);
            return createService(new ThreadFactory(name, parallelism));
        }

        private static @NotNull ExecutorService createService(@NotNull ThreadFactory factory) {
            return new ForkJoinPool(factory.threads, factory, null, false);
        }

        @Override
        public @NotNull ForkJoinWorkerThread newThread(@NotNull ForkJoinPool pool) {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            // use current classloader, this fixes ClassLoading issues with forge
            thread.setContextClassLoader(Pl3xMap.class.getClassLoader());
            thread.setName(this.threads > 1 ? String.format("%s-%d", this.name, this.id.getAndIncrement()) : this.name);
            return thread;
        }
    }
}
