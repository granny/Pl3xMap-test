/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.pl3x.map.core.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.util.MCAMath;
import net.pl3x.map.core.util.PackedIntArrayAccess;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.StringTag;
import net.querz.nbt.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChunkAnvil118 extends Chunk {
    private int sectionMin = Integer.MAX_VALUE;

    private Section[] sections = new Section[0];

    protected long[] worldSurfaceHeights = new long[0];

    private final boolean full;

    protected ChunkAnvil118(@NotNull World world, @NotNull Region region, @NotNull CompoundTag chunkTag, int index) {
        super(world, region, chunkTag, index);

        this.full = chunkTag.getString("Status").endsWith("full"); // 1.20 uses minecraft namespace here
        if (!this.full) {
            return;
        }

        if (chunkTag.containsKey("Heightmaps")) {
            CompoundTag heightmaps = chunkTag.getCompoundTag("Heightmaps");
            this.worldSurfaceHeights = heightmaps.getLongArray("WORLD_SURFACE");
        }

        if (chunkTag.containsKey("sections")) {
            ListTag<CompoundTag> sections = chunkTag.getListTag("sections").asCompoundTagList();
            List<Section> list = new ArrayList<>(sections.size());
            int sectionMax = Integer.MIN_VALUE;
            for (CompoundTag sectionTag : sections) {
                Section section = new Section(world, sectionTag);
                int y = section.sectionY;
                if (this.sectionMin > y) this.sectionMin = y;
                if (sectionMax < y) sectionMax = y;
                list.add(section);
            }
            this.sections = new Section[1 + sectionMax - this.sectionMin];
            for (Section section : list) {
                this.sections[section.sectionY - this.sectionMin] = section;
            }
        }
    }

    @Override
    public boolean isFull() {
        return this.full;
    }

    @Override
    public @NotNull BlockState getBlockState(int x, int y, int z) {
        int sectionY = y >> 4;
        Section section = getSection(sectionY);
        return section == null ? Blocks.AIR.getDefaultState() : section.getBlockState(x, y, z);
    }

    @Override
    public int getLight(int x, int y, int z) {
        int sectionY = y >> 4;
        Section section = getSection(sectionY);
        return section == null ? ((sectionY < this.sectionMin) ? 0 : getWorld().getSkylight()) : section.getLight(x, y, z);
    }

    @Override
    public @NotNull Biome getBiome(int x, int y, int z) {
        int sectionY = y >> 4;
        Section section = getSection(sectionY);
        return section == null ? Biome.DEFAULT : section.getBiome(x, y, z);
    }

    @Override
    public boolean noHeightmap() {
        return this.worldSurfaceHeights.length < 37;
    }

    @Override
    public int getWorldSurfaceY(int x, int z) {
        if (noHeightmap()) {
            return getWorld().getMinBuildHeight();
        }
        return heightmap(getWorld().getMaxBuildHeight(), this.worldSurfaceHeights).get(((z & 0xF) << 4) + (x & 0xF));
    }

    private @Nullable Section getSection(int y) {
        y -= this.sectionMin;
        return y < 0 || y >= this.sections.length ? null : this.sections[y];
    }

    private static PackedIntArrayAccess heightmap(int worldHeight, long[] data) {
        return new PackedIntArrayAccess(MCAMath.ceilLog2(worldHeight + 1), data);
    }

    protected static class Section {
        private final int sectionY;
        private byte[] blockLight;
        private long[] blocks;
        private long[] biomes = new long[0];
        private BlockState[] blockPalette = new BlockState[0];
        private Biome[] biomePalette = new Biome[0];
        private final int bitsPerBlock;
        private final int bitsPerBiome;

        public Section(@NotNull World world, @NotNull CompoundTag sectionData) {
            this.sectionY = sectionData.getNumber("Y").intValue();
            this.blockLight = sectionData.getByteArray("BlockLight");
            this.blocks = sectionData.getLongArray("BlockStates");

            CompoundTag blockStatesTag = sectionData.getCompoundTag("block_states");
            if (blockStatesTag != null) {
                this.blocks = blockStatesTag.getLongArray("data");
                if (blockStatesTag.containsKey("palette")) {
                    ListTag<CompoundTag> paletteTag = blockStatesTag.getListTag("palette").asCompoundTagList();
                    this.blockPalette = new BlockState[paletteTag.size()];
                    for (int i = 0; i < this.blockPalette.length; i++) {
                        CompoundTag entry = paletteTag.get(i);
                        String id = entry.getString("Name");
                        Block block = Pl3xMap.api().getBlockRegistry().getOrDefault(id, Blocks.AIR);
                        Map<String, String> properties = new HashMap<>();
                        CompoundTag propertiesTag = entry.getCompoundTag("Properties");
                        if (propertiesTag != null) {
                            for (Map.Entry<String, Tag<?>> property : propertiesTag) {
                                properties.put(property.getKey().toLowerCase(), ((StringTag) property.getValue()).getValue().toLowerCase());
                            }
                        }
                        this.blockPalette[i] = new BlockState(block, properties);
                    }
                }
            }

            CompoundTag biomesTag = sectionData.getCompoundTag("biomes");
            if (biomesTag != null) {
                this.biomes = biomesTag.getLongArray("data");
                if (biomesTag.containsKey("palette")) {
                    ListTag<StringTag> paletteTag = biomesTag.getListTag("palette").asStringTagList();
                    this.biomePalette = new Biome[paletteTag.size()];
                    for (int i = 0; i < this.biomePalette.length; i++) {
                        biomePalette[i] = world.getBiomeRegistry().getOrDefault(paletteTag.get(i).getValue(), Biome.DEFAULT);
                    }
                }
            }

            if (this.blocks.length < 256 && this.blocks.length > 0) {
                this.blocks = Arrays.copyOf(this.blocks, 256);
            }
            if (this.blockLight.length < 2048 && this.blockLight.length > 0) {
                this.blockLight = Arrays.copyOf(this.blockLight, 2048);
            }
            this.bitsPerBlock = this.blocks.length >> 6;
            this.bitsPerBiome = MCAMath.ceilLog2(this.biomePalette.length);
        }

        public @NotNull BlockState getBlockState(int x, int y, int z) {
            if (this.blockPalette.length == 1) {
                return this.blockPalette[0];
            }
            if (this.blocks.length == 0) {
                return Blocks.AIR.getDefaultState();
            }
            int blockIndex = ((y & 0xF) << 8) + ((z & 0xF) << 4) + (x & 0xF);
            long value = MCAMath.getValueFromLongArray(this.blocks, blockIndex, this.bitsPerBlock);
            if (value >= this.blockPalette.length) {
                return Blocks.AIR.getDefaultState();
            }
            return this.blockPalette[(int) value];
        }

        public int getLight(int x, int y, int z) {
            if (this.blockLight.length == 0) {
                return 0;
            }
            int blockByteIndex = ((y & 0xF) << 8) + ((z & 0xF) << 4) + (x & 0xF);
            int blockHalfByteIndex = blockByteIndex >> 1;
            boolean largeHalf = (blockByteIndex & 0x1) != 0;
            return MCAMath.getByteHalf(this.blockLight[blockHalfByteIndex], largeHalf);
        }

        public @NotNull Biome getBiome(int x, int y, int z) {
            if (this.biomePalette.length == 0) {
                return Biome.DEFAULT;
            }
            if (this.biomePalette.length == 1 || this.biomes.length == 0) {
                return this.biomePalette[0];
            }
            int biomeIndex = (((y & 0xF) >> 2) << 4) + (((z & 0xF) >> 2) << 2) + ((x & 0xF) >> 2);
            long value = MCAMath.getValueFromLongArray(this.biomes, biomeIndex, this.bitsPerBiome);
            if (value >= this.biomePalette.length) {
                return Biome.DEFAULT;
            }
            return this.biomePalette[(int) value];
        }
    }
}
