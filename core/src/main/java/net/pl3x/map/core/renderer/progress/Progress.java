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
package net.pl3x.map.core.renderer.progress;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.configuration.Lang;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Progress implements Runnable {
    private final CPSTracker cpsTracker = new CPSTracker();

    private final AtomicLong processedChunks = new AtomicLong(0);
    private final AtomicLong processedRegions = new AtomicLong(0);

    private final Executor executor;
    private CompletableFuture<@NotNull Void> future;

    private World world;

    private long prevProcessedChunks = 0;
    private long totalChunks;
    private long totalRegions;
    private float percent;
    private double cps;
    private String eta = Lang.PROGRESS_ETA_UNKNOWN;

    public Progress() {
        this.executor = Pl3xMap.ThreadFactory.createService("Pl3xMap-Progress");
        start(1000L);
    }

    public void start(long delay) {
        this.future = CompletableFuture.runAsync(() -> {
            // wait...
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignore) {
            }

            run();

            // rinse and repeat
            start(1000L);
        }, this.executor);
    }

    public void stop() {
        if (this.future != null) {
            boolean result = this.future.cancel(true);
            Logger.debug("Stopped progress tracker: " + result);
        }
    }

    public void finish() {
        this.world = null;
        setTotalChunks(0);
        setProcessedChunks(0);
        setTotalRegions(0);
        setProcessedRegions(0);
        this.prevProcessedChunks = 0;
        this.percent = 0.0F;
        this.cps = 0.0D;
        this.eta = Lang.PROGRESS_ETA_UNKNOWN;
    }

    public @Nullable World getWorld() {
        return this.world;
    }

    public void setWorld(@Nullable World world) {
        this.world = world;
    }

    public long getTotalChunks() {
        return this.totalChunks;
    }

    public void setTotalChunks(long totalChunks) {
        this.totalChunks = totalChunks;
    }

    public long getTotalRegions() {
        return this.totalRegions;
    }

    public void setTotalRegions(long totalRegions) {
        this.totalRegions = totalRegions;
    }

    public void increment() {
        this.processedRegions.set(this.processedRegions.get() + 1);
        this.processedChunks.set(this.processedChunks.get() + 1024);
    }

    public float getPercent() {
        return this.percent;
    }

    public double getCPS() {
        return this.cps;
    }

    public @NotNull String getETA() {
        return this.eta;
    }

    public @NotNull AtomicLong getProcessedChunks() {
        return this.processedChunks;
    }

    public void setProcessedChunks(long processedChunks) {
        getProcessedChunks().set(processedChunks);
        this.prevProcessedChunks = processedChunks;
    }

    public @NotNull AtomicLong getProcessedRegions() {
        return this.processedRegions;
    }

    public void setProcessedRegions(long processedRegions) {
        getProcessedRegions().set(processedRegions);
    }

    @Override
    public void run() {
        try {
            runProgress();
        } catch (Throwable t) {
            Logger.severe("Failed to run progress", t);
        }
    }

    private void runProgress() {
        if (this.world == null || Pl3xMap.api().getRegionProcessor().isPaused()) {
            return;
        }

        long processedChunks = getProcessedChunks().get();
        this.cpsTracker.add(processedChunks - this.prevProcessedChunks);
        this.prevProcessedChunks = processedChunks;
        this.percent = ((float) processedChunks / (float) getTotalChunks()) * 100.0F;
        this.cps = this.cpsTracker.average();
        if (this.cps > 0.0D) {
            long timeLeft = (this.totalChunks - processedChunks) / (long) this.cps * 1000L;
            this.eta = formatMilliseconds(timeLeft);
        } else {
            this.eta = Lang.PROGRESS_ETA_UNKNOWN;
        }
    }

    public static @NotNull String formatMilliseconds(long time) {
        int hrs = (int) TimeUnit.MILLISECONDS.toHours(time);
        int min = (int) TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        int sec = (int) TimeUnit.MILLISECONDS.toSeconds(time) % 60;
        if (hrs > 0) {
            return String.format("%dh %dm %ds", hrs, min, sec);
        } else if (min > 0) {
            return String.format("%dm %ds", min, sec);
        } else {
            return String.format("%ds", sec);
        }
    }
}
