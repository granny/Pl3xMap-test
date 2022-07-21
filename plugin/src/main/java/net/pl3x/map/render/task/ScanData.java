package net.pl3x.map.render.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.pl3x.map.render.image.Image;
import net.pl3x.map.render.job.iterator.coordinate.BlockCoordinate;
import net.pl3x.map.util.Colors;
import net.pl3x.map.world.MapWorld;

public class ScanData {
    private final ScanTask scanTask;
    private final ChunkAccess chunk;
    private final BlockCoordinate coordinate;

    private Holder<Biome> biome;

    private final BlockPos.MutableBlockPos blockPos;
    private BlockState blockState;

    private BlockPos.MutableBlockPos fluidPos = null;
    private BlockState fluidState;

    private final List<Integer> glass = new ArrayList<>();

    public ScanData(ScanTask scanTask, ChunkAccess chunk, BlockCoordinate coordinate) {
        this.scanTask = scanTask;
        this.chunk = chunk;
        this.coordinate = coordinate;

        int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, coordinate.getBlockX(), coordinate.getBlockZ()) + 1;
        this.blockPos = new BlockPos.MutableBlockPos(coordinate.getBlockX(), y, coordinate.getBlockZ());

        scan();
    }

    public ScanTask getScanTask() {
        return this.scanTask;
    }

    public MapWorld getWorld() {
        return this.scanTask.getWorld();
    }

    public ChunkAccess getChunk() {
        return this.chunk;
    }

    public BlockCoordinate getCoordinate() {
        return this.coordinate;
    }

    public Holder<Biome> getBiome() {
        return this.biome;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public BlockPos getFluidPos() {
        return this.fluidPos;
    }

    public BlockState getFluidState() {
        return this.fluidState;
    }

    public List<Integer> getGlassColors() {
        return this.glass;
    }

    private void scan() {
        // if world has ceiling iterate down until we find air
        if (getWorld().getLevel().dimensionType().hasCeiling()) {
            do {
                this.blockPos.move(Direction.DOWN);
                this.blockState = this.chunk.getBlockState(this.blockPos);
            } while (this.blockPos.getY() > getWorld().getLevel().getMinBuildHeight() && !this.blockState.isAir());
        }

        // iterate down until we find a renderable block
        do {
            this.blockPos.move(Direction.DOWN);
            this.blockState = this.chunk.getBlockState(this.blockPos);
            if (!this.blockState.getFluidState().isEmpty()) {
                if (this.fluidPos == null) {
                    this.fluidPos = this.blockPos.mutable();
                    this.fluidState = this.chunk.getBlockState(this.fluidPos);
                }
                continue;
            }
            // just get a quick color for now
            int blockColor = Colors.getRawBlockColor(this.blockState);
            if (getWorld().getConfig().RENDER_TRANSLUCENT_GLASS && isGlass(this.blockState)) {
                this.glass.add(blockColor);
                continue;
            }
            if (blockColor > 0) {
                break;
            }
        } while (this.blockPos.getY() > getWorld().getLevel().getMinBuildHeight());

        // determine the biome
        this.biome = this.scanTask.getChunkHelper().getBiome(getWorld(), this.chunk, this.blockPos);
    }

    private boolean isGlass(BlockState state) {
        return state.is(Blocks.GLASS) || state.is(Blocks.GLASS_PANE) ||
                state.getBlock() instanceof StainedGlassBlock ||
                state.getBlock() instanceof StainedGlassPaneBlock;
    }

    public static ScanData get(ScanData[] scanData, BlockCoordinate coordinate) {
        final int size = size();
        final int x = coordinate.getBlockX() & size - 1;
        final int z = coordinate.getBlockZ() & size - 1;
        return scanData[z * size + x];
    }

    public static void put(ScanData[] scanData, BlockCoordinate coordinate, ScanData data) {
        final int size = size();
        final int x = coordinate.getBlockX() & size - 1;
        final int z = coordinate.getBlockZ() & size - 1;
        scanData[z * size + x] = data;
    }

    public static int size() {
        return Image.SIZE + 16 * 2;
    }

    public static class Data {
        private final Map<BlockCoordinate, ScanData> scanData = new HashMap<>(Image.SIZE * Image.SIZE, 1.0F);
        private final Map<BlockCoordinate, ScanData> edgeData = new HashMap<>((Image.SIZE + 2) * 4, 1.0F);

        public ScanData get(BlockCoordinate coordinate) {
            ScanData data = this.scanData.get(coordinate);
            if (data == null) {
                data = this.edgeData.get(coordinate);
            }
            return data;
        }

        public void put(BlockCoordinate coordinate, ScanData data) {
            this.scanData.put(coordinate, data);
        }

        public void edge(BlockCoordinate coordinate, ScanData data) {
            this.edgeData.put(coordinate, data);
        }

        public Collection<ScanData> values() {
            return this.scanData.values();
        }
    }
}
