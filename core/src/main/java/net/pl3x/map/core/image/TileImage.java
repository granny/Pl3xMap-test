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
package net.pl3x.map.core.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.pl3x.map.core.Keyed;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.image.io.IO;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.util.Colors;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileImage extends Keyed {
    private static final Map<@NotNull Path, @NotNull ReadWriteLock> FILE_LOCKS = new ConcurrentHashMap<>();

    public static final String DIR_PATH = "%d/%s/";
    public static final String FILE_PATH = "%d_%d.%s";

    private final World world;
    private final Point region;

    private final int[] pixels = new int[512 << 9];

    private final IO.Type io;

    private boolean written = false;

    public TileImage(@NotNull String key, @NotNull World world, @NotNull Point region) {
        super(key);
        this.world = world;
        this.region = region;

        this.io = IO.get(Config.WEB_TILE_FORMAT);
    }

    public int getIndex(int x, int z) {
        return ((z & 0x1FF) << 9) + (x & 0x1FF);
    }

    public int getPixel(int x, int z) {
        return this.pixels[getIndex(x, z)];
    }

    public void setPixel(int x, int z, int color) {
        this.pixels[getIndex(x, z)] = color;
        this.written = true;
    }

    public void saveToDisk() {
        if (!this.written) {
            return; // nothing written, nothing to save
        }
        for (int zoom = 0; zoom <= this.world.getConfig().ZOOM_MAX_OUT; zoom++) {
            Path dirPath = this.world.getTilesDirectory().resolve(String.format(DIR_PATH, zoom, getKey()));

            // create directories if they don't exist
            FileUtil.createDirs(dirPath);

            Path filePath = dirPath.resolve(String.format(FILE_PATH,
                    this.region.x() >> zoom,
                    this.region.z() >> zoom,
                    this.io.getKey()));

            ReadWriteLock lock = FILE_LOCKS.computeIfAbsent(filePath, k -> new ReentrantReadWriteLock(true));
            lock.writeLock().lock();

            // wrap all this to ensure we close the file lock even on fail
            try {
                // read existing image from disk
                BufferedImage buffer = getBuffer(filePath);

                // write new pixels
                writePixels(buffer, 512 >> zoom, zoom);

                // finally, save buffer to disk
                this.io.write(filePath, buffer);
            } catch (Throwable t) {
                Logger.severe("Failed to read/write tile at path %s".formatted(filePath), t);
            }

            lock.writeLock().unlock();
        }
    }

    private @NotNull BufferedImage getBuffer(@NotNull Path path) throws IOException {
        BufferedImage buffer = null;

        // try to read existing image
        if (Files.exists(path) && Files.size(path) > 0) {
            buffer = this.io.read(path);
        }

        // if not, create a new image
        if (buffer == null) {
            buffer = this.io.createBuffer();
        }

        return buffer;
    }

    private void writePixels(@NotNull BufferedImage buffer, int size, int zoom) {
        int step = 1 << zoom;
        int baseX = (this.region.x() * size) & 0x1FF;
        int baseZ = (this.region.z() * size) & 0x1FF;
        for (int x = 0; x < 512; x += step) {
            for (int z = 0; z < 512; z += step) {
                int argb = getPixel(x, z);
                if (argb == 0) {
                    // skipping 0 prevents overwrite existing
                    // parts of the buffer of existing images
                    continue;
                }
                if (step > 1) {
                    // merge pixel colors instead of skipping them
                    argb = downSample(x, z, argb, step);
                }
                buffer.setRGB(baseX + (x >> zoom), baseZ + (z >> zoom), this.io.color(argb));
            }
        }
    }

    private int downSample(int x, int z, int rgb, int step) {
        int a = 0, r = 0, g = 0, b = 0, c = 0;
        for (int i = 0; i < step; i++) {
            for (int j = 0; j < step; j++) {
                if (i != 0 && j != 0) {
                    rgb = getPixel(x + i, z + j);
                }
                a += Colors.alpha(rgb);
                r += Colors.red(rgb);
                g += Colors.green(rgb);
                b += Colors.blue(rgb);
                c++;
            }
        }
        return Colors.argb(a / c, r / c, g / c, b / c);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        TileImage other = (TileImage) o;
        return getKey().equals(other.getKey())
                && this.region.equals(other.region)
                && this.world.equals(other.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), this.region, this.world);
    }

    @Override
    public @NotNull String toString() {
        return "TileImage{"
                + "key=" + getKey()
                + ",region=" + this.region
                + ",world=" + this.world
                + "}";
    }
}
