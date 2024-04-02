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
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

public class RegionFileWatcher implements Runnable {
    private final World world;

    private Timer timer;
    private TimerTask task;
    private Thread thread;

    private boolean stopped;

    public RegionFileWatcher(@NotNull World world) {
        this.world = world;
    }

    public void start() {
        start(true);
    }

    public void start(boolean verbose) {
        if (!Pl3xMap.api().isEnabled()) {
            return;
        }
        if (verbose) {
            Logger.debug("Starting region file watcher for " + this.world.getName());
        }
        stop(false);
        this.timer = new Timer();
        this.task = new TimerTask() {
            @Override
            public void run() {
                //RegionFileWatcher rfw = RegionFileWatcher.this;
                //rfw.thread = Thread.currentThread();
                //rfw.thread.setName(String.format("Pl3xMap-FileWatcher-%s", rfw.world.getName()));
                //rfw.stopped = false;
                //rfw.run();
            }
        };

        this.timer.schedule(task, 10000L);
    }

    public void stop() {
        stop(true);
    }

    public void stop(boolean verbose) {
        if (verbose) {
            Logger.debug("Stopping region file watcher for " + this.world.getName());
        }
        this.stopped = true;
        if (this.task != null) {
            this.task.cancel();
        }
        if (this.timer != null) {
            this.timer.cancel();
        }
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    @Override
    public void run() {
        Path dir = this.world.getRegionDirectory();

        try (WatchService watcher = dir.getFileSystem().newWatchService()) {

            dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            Logger.debug("Region file watcher started for " + dir);

            WatchKey key;
            while ((key = watcher.take()) != null) {
                Collection<Path> modifiedFiles = new HashSet<>();

                Logger.debug("Region file watcher got a key!");
                for (WatchEvent<?> event : key.pollEvents()) {
                    Logger.debug("Region file watcher detected event: " + event.kind().name());
                    if (event.kind() != StandardWatchEventKinds.OVERFLOW) {
                        Path file = (Path) event.context();
                        Logger.debug("Detected file change: " + file.getFileName());
                        modifiedFiles.add(dir.resolve(file));
                    }
                }
                key.reset();

                Collection<Point> points = FileUtil.regionPathsToPoints(this.world, modifiedFiles, true);
                Pl3xMap.api().getRegionProcessor().addRegions(this.world, points);
            }

        } catch (ClosedWatchServiceException | InterruptedException ignore) {
        } catch (IOException e) {
            Logger.severe("Failed to create a new file watcher for directory %s".formatted(dir.toAbsolutePath()), e);
        }

        if (this.stopped) {
            Logger.debug("Region file watcher stopped!");
        } else {
            Logger.debug("Region file watcher stopped! Trying to start again..");
            start(false);
        }
    }
}
