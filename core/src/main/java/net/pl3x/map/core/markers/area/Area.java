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

import java.util.Map;
import net.pl3x.map.core.world.World;

public interface Area {
    static Area deserialize(World world, Map<String, Object> map) {
        return switch (String.valueOf(map.get("type"))) {
            case "circle" -> Circle.deserialize(map);
            case "rectangle" -> Rectangle.deserialize(map);
            case "world-border" -> Border.deserialize(world, map);
            default -> throw new IllegalArgumentException("Unknown area type");
        };
    }

    boolean containsBlock(int blockX, int blockZ);

    boolean containsChunk(int chunkX, int chunkZ);

    boolean containsRegion(int regionX, int regionZ);

    Map<String, Object> serialize();
}
