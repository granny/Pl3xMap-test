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
package net.pl3x.map.core.scheduler;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.util.TickUtil;
import org.jetbrains.annotations.NotNull;

public class Scheduler {
    private final Queue<@NotNull Task> tasks = new ConcurrentLinkedQueue<>();

    private boolean ticking;

    /**
     * Tick this scheduler once every tick.
     */
    public void tick() {
        if (this.ticking) {
            return;
        }
        this.ticking = true;
        try {
            Iterator<Task> iter = this.tasks.iterator();
            while (iter.hasNext()) {
                Task task = iter.next();
                if (task.tick++ < task.delay) {
                    continue;
                }
                if (task.cancelled()) {
                    iter.remove();
                    continue;
                }
                task.run();
                if (task.repeat) {
                    task.tick = 0;
                    continue;
                }
                iter.remove();
            }
        } catch (Throwable t) {
            error("Failed to tick a task", t);
        }
        this.ticking = false;
    }

    // TODO: make logger work in fabric client
    protected void error(String message, Throwable t) {
        Logger.severe(message, t);
    }

    /**
     * Cancel all scheduled tasks.
     */
    public void cancelAll() {
        Iterator<Task> iter = this.tasks.iterator();
        while (iter.hasNext()) {
            iter.next().cancel();
            iter.remove();
        }
    }

    /**
     * Add task to the scheduler.
     *
     * @param task Task to add
     */
    public void addTask(@NotNull Task task) {
        this.tasks.add(task);
    }

    /**
     * Add task to the scheduler.
     *
     * @param delay    Delay (in seconds) before task starts
     * @param runnable Task to add
     */
    public void addTask(int delay, @NotNull Runnable runnable) {
        addTask(delay, false, runnable);
    }

    /**
     * Add task to the scheduler.
     *
     * @param delay    Delay (in seconds or ticks) before task starts
     * @param runnable Task to add
     * @param ticks    Set to true to pass the delay as ticks instead of seconds
     */
    public void addTask(int delay, @NotNull Runnable runnable, boolean ticks) {
        addTask(delay, false, runnable, ticks);
    }

    /**
     * Add task to the scheduler.
     *
     * @param delay    Delay (in seconds) before task starts
     * @param repeat   Whether this task should repeat
     * @param runnable Task to add
     */
    public void addTask(int delay, boolean repeat, @NotNull Runnable runnable) {
        addTask(delay, repeat, runnable, false);
    }

    /**
     * Add task to the scheduler.
     *
     * @param delay    Delay (in seconds or ticks) before task starts
     * @param repeat   Whether this task should repeat
     * @param runnable Task to add
     * @param ticks    Set to true to pass the delay as ticks instead of seconds
     */
    public void addTask(int delay, boolean repeat, @NotNull Runnable runnable, boolean ticks) {
        addTask(new Task(ticks ? delay : TickUtil.toTicks(delay), repeat) {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }
}
