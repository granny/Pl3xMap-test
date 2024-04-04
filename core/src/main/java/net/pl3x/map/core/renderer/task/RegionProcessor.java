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
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.renderer.progress.Progress;
import net.pl3x.map.core.util.Mathf;
import net.pl3x.map.core.util.SpiralIterator;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

public class RegionProcessor {
    private final Map<@NotNull World, @NotNull Collection<@NotNull Point>> regionsToScan = new ConcurrentHashMap<>();
    private final Deque<@NotNull Ticket> ticketsToScan = new ConcurrentLinkedDeque<>();

    private final Executor executor;
    private final Progress progress;

    private CompletableFuture<@NotNull Void> future;

    private boolean paused;

    private long timeStarted;
    private boolean running;

    public RegionProcessor() {
        this.executor = Pl3xMap.ThreadFactory.createService("Pl3xMap-Processor");
        this.progress = new Progress();
    }

    @SuppressWarnings("BusyWait")
    public void checkPaused() {
        while (isPaused()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignore) {
            }
        }
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public @NotNull Progress getProgress() {
        return this.progress;
    }

    public Set<World> getQueuedWorlds() {
        return this.regionsToScan.keySet();
    }

    public void start(long delay) {
        this.future = CompletableFuture.runAsync(() -> {
            // wait...
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignore) {
            }

            // run the task
            if (!isPaused()) {
                run();
            }

            // rinse and repeat
            start(5000L);
        }, this.executor);
    }

    public void stop() {
        this.progress.stop();
        if (this.future != null) {
            boolean result = this.future.cancel(true);
            Logger.debug("Stopped region processor: " + result);
        }
    }

    public void addRegions(@NotNull World world, @NotNull Collection<@NotNull Point> regions) {
        for (Point region : regions) {
            Ticket ticket = new Ticket(world, region);
            if (!this.ticketsToScan.contains(ticket)) {
                this.ticketsToScan.add(ticket);
            }
        }
    }

    private void run() {
        if (this.running) {
            // this task is already running
            Logger.debug("Region processor already running!");
            return;
        }

        // consider task as running
        this.running = true;
        this.timeStarted = System.currentTimeMillis();

        Logger.debug("Region processor started queuing at " + this.timeStarted);

        try {
            while (!this.ticketsToScan.isEmpty()) {
                Ticket ticket = this.ticketsToScan.poll();
                if (ticket == null) continue;

                Collection<Point> set = this.regionsToScan.getOrDefault(ticket.world, new HashSet<>());
                set.add(ticket.region);
                this.regionsToScan.put(ticket.world, set);
            }

            Iterator<Map.Entry<World, Collection<Point>>> iter = this.regionsToScan.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<World, Collection<Point>> entry = iter.next();
                iter.remove();
                World world = entry.getKey();
                Collection<Point> regions = entry.getValue();
                process(world, regions);

            }
        } catch (Throwable t) {
            Logger.severe("Region processor failed to process tickets", t);
        }

        this.running = false;
        Logger.debug("Region processor finished queuing at " + System.currentTimeMillis());
    }

    private void process(@NotNull World world, @NotNull Collection<@NotNull Point> regionPositions) {
        Logger.debug(world.getName() + " Region processor started processing at " + System.currentTimeMillis());

        // create spiral iterator to order region scanning
        Point spawn = world.getSpawn();
        SpiralIterator spiralIterator = new SpiralIterator(spawn.x() >> 9, spawn.z() >> 9);

        // order preserved map of regions with boolean to signify if it was already scanned
        List<Point> orderedRegionsToScan = new ArrayList<>();

        // iterate the spiral
        int totalRegions = regionPositions.size();
        int numberOfFoundRegions = 0;
        int numberOfSkippedRegions = 0;
        while (numberOfFoundRegions < totalRegions) {
            // let us not get stuck in an endless loop
            if (numberOfSkippedRegions > 1000000) {
                Logger.debug("Failsafe triggered.");
                // we scanned over a million non-existent regions straight
                // quit the spiral and add the remaining regions to the end
                orderedRegionsToScan.addAll(regionPositions);
                break;
            }

            // get region from spiral and ensure a region file exists for it
            Point regionPos = spiralIterator.next();
            if (regionPositions.remove(regionPos)) {
                // file exists, add region to scan
                orderedRegionsToScan.add(regionPos);
                numberOfFoundRegions++;
                numberOfSkippedRegions = 0;
            } else {
                numberOfSkippedRegions++;
            }
        }

        // create and send tasks to executor to run
        schedule(world, orderedRegionsToScan);

        Logger.debug(world.getName() + " Region processor finished processing at " + System.currentTimeMillis());
    }

    private void schedule(@NotNull World world, @NotNull List<@NotNull Point> orderedRegionsToScan) {
        getProgress().setWorld(world);
        getProgress().setTotalRegions(orderedRegionsToScan.size());
        getProgress().setTotalChunks(getProgress().getTotalRegions() * 1024L);

        CompletableFuture.allOf(orderedRegionsToScan.stream()
                .map(pos -> CompletableFuture.runAsync(new RegionScanTask(world, pos), Pl3xMap.api().getRenderExecutor())
                        .whenComplete((result, throwable) -> {
                            if (throwable != null) {
                                Logger.severe("Failed to run region scan task for %s".formatted(world.getName(), pos), throwable);
                            }

                            // set region modified time
                            world.getRegionModifiedState().set(Mathf.asLong(pos), this.timeStarted);

                            // run the garbage collector
                            if (Config.GC_WHEN_RUNNING) {
                                System.gc();
                            }
                        })
                ).toArray(CompletableFuture[]::new)
        ).whenComplete((result, throwable) -> {
            if (throwable != null) {
                Logger.severe("Failed to run region scan tasks for world %s".formatted(world.getName()), throwable);
            }

            // stop the progress tracker
            getProgress().finish();

            // free up some memory
            world.cleanup();

            // run the garbage collector
            if (Config.GC_WHEN_FINISHED) {
                System.gc();
            }

            // consider task as no longer running
            this.running = false;

            Logger.debug(world.getName() + " Region processor finished task at " + System.currentTimeMillis());
        }).join();
    }

    private record Ticket(@NotNull World world, @NotNull Point region) {
    }
}
