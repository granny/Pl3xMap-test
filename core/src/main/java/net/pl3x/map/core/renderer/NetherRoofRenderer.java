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
package net.pl3x.map.core.renderer;

import java.util.LinkedList;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.renderer.heightmap.Heightmap;
import net.pl3x.map.core.renderer.task.RegionScanTask;
import net.pl3x.map.core.util.Colors;
import net.pl3x.map.core.world.BlockState;
import net.pl3x.map.core.world.Chunk;
import net.pl3x.map.core.world.EmptyChunk;
import net.pl3x.map.core.world.Region;
import org.jetbrains.annotations.NotNull;

public class NetherRoofRenderer extends Renderer {
    private final Heightmap heightmap;

    public NetherRoofRenderer(@NotNull RegionScanTask task, @NotNull Builder builder) {
        super(task, builder);
        this.heightmap = Pl3xMap.api().getHeightmapRegistry().get("none");
    }

    @Override
    public @NotNull Heightmap getHeightmap() {
        return this.heightmap;
    }

    @Override
    public void scanData(@NotNull Region region) {
        int startX = region.getX() << 9;
        int startZ = region.getZ() << 9;

        LinkedList<Integer> glass = new LinkedList<>();

        for (int pixelX = 0; pixelX < 512; pixelX++) {
            int blockX = startX + pixelX;
            double lastBlockY = 0.0D;
            for (int pixelZ = -1; pixelZ < 512; pixelZ++) {
                int blockZ = startZ + pixelZ;

                if (!getWorld().visibleBlock(blockX, blockZ)) {
                    continue;
                }

                Pl3xMap.api().getRegionProcessor().checkPaused();

                Chunk chunk = region.getWorld().getChunk(region, blockX >> 4, blockZ >> 4);
                if (chunk instanceof EmptyChunk) {
                    continue;
                }

                int blockY = chunk.noHeightmap() ? getWorld().getMaxBuildHeight() : chunk.getWorldSurfaceY(blockX, blockZ) + 1;
                int fluidY = 0;
                BlockState blockstate;
                BlockState fluidstate = null;

                // iterate down until we find a renderable block
                do {
                    blockY -= 1;
                    blockstate = chunk.getBlockState(blockX, blockY, blockZ);
                    if (blockstate.getBlock().isFluid()) {
                        if (fluidstate == null) {
                            // get fluid information for the top fluid block
                            fluidY = blockY;
                            fluidstate = blockstate;
                        }
                        continue;
                    }

                    if (getWorld().getConfig().RENDER_TRANSLUCENT_GLASS && blockstate.getBlock().isGlass()) {
                        // translucent glass. store this color and keep iterating
                        glass.addFirst(Colors.setAlpha(0x99, blockstate.getBlock().color()));
                        continue;
                    }

                    // test if block is renderable. we ignore blocks with black color
                    if (blockstate.getBlock().color() > 0) {
                        break;
                    }
                } while (blockY > getWorld().getMinBuildHeight());

                Chunk.BlockData blockData = Chunk.BlockData.of(blockY, fluidY, blockstate, fluidstate, glass);

                int pixelColor = basicPixelColor(region, blockData, blockX, blockZ);
                getTileImage().setPixel(blockX, blockZ, pixelColor);

                if (blockstate.getBlock().isFlat()) {
                    blockY--;
                }

                glass.clear();

                lastBlockY = blockY;
            }
        }
    }

    @Override
    public void scanBlock(@NotNull Region region, @NotNull Chunk chunk, Chunk.@NotNull BlockData data, int blockX, int blockZ) {
    }
}
