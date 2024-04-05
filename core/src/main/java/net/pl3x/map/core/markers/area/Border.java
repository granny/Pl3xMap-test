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
package net.pl3x.map.core.markers.area;

import java.util.LinkedHashMap;
import java.util.Map;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

public class Border implements Area {
    private final World world;

    public Border(World world) {
        this.world = world;
    }

    public static @NotNull Border deserialize(World world, @SuppressWarnings("unused") Map<String, Object> map) {
        return new Border(world);
    }

    public int getMinX() {
        return (int) this.world.getBorderMinX();
    }

    public int getMinZ() {
        return (int) this.world.getBorderMinZ();
    }

    public int getMaxX() {
        return (int) this.world.getBorderMaxX();
    }

    public int getMaxZ() {
        return (int) this.world.getBorderMaxZ();
    }

    @Override
    public boolean containsBlock(int blockX, int blockZ) {
        return blockX >= getMinX() && blockX <= getMaxX() && blockZ >= getMinZ() && blockZ <= getMaxZ();
    }

    @Override
    public boolean containsChunk(int chunkX, int chunkZ) {
        return chunkX >= (getMinX() >> 4) && chunkX <= (getMaxX() >> 4) && chunkZ >= (getMinZ() >> 4) && chunkZ <= (getMaxZ() >> 4);
    }

    @Override
    public boolean containsRegion(int regionX, int regionZ) {
        return regionX >= (getMinX() >> 9) && regionX <= (getMaxX() >> 9) && regionZ >= (getMinZ() >> 9) && regionZ <= (getMaxZ() >> 9);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "world-border");
        return map;
    }

    @Override
    public @NotNull String toString() {
        return "Border{"
                + "minX=" + getMinX()
                + ",minZ=" + getMinZ()
                + ",maxX=" + getMaxX()
                + ",maxZ=" + getMaxZ()
                + "}";
    }
}
