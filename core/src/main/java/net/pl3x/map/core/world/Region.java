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
package net.pl3x.map.core.world;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Objects;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.log.Logger;
import net.querz.mca.CompressionType;
import net.querz.nbt.io.NBTInputStream;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Region {
    private final World world;
    private final int regionX;
    private final int regionZ;
    private final File regionFile;

    private final Chunk[] chunks = new Chunk[32 << 5];

    private final int hash;

    public Region(@NotNull World world, int regionX, int regionZ, @NotNull Path regionFile) {
        this.world = world;
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.regionFile = regionFile.toFile();

        this.hash = Objects.hash(world, regionX, regionZ);
    }

    public @NotNull World getWorld() {
        return this.world;
    }

    public int getX() {
        return this.regionX;
    }

    public int getZ() {
        return this.regionZ;
    }

    public @NotNull File getRegionFile() {
        return this.regionFile;
    }

    private int getChunkIndex(int chunkX, int chunkZ) {
        return (chunkX & 0x1F) + ((chunkZ & 0x1F) << 5);
    }

    public @NotNull Chunk getChunk(int chunkX, int chunkZ) {
        int index = getChunkIndex(chunkX, chunkZ);
        Chunk chunk = this.chunks[index];
        if (chunk == null) {
            try (RandomAccessFile raf = new RandomAccessFile(getRegionFile(), "r")) {
                chunk = loadChunk(raf, index);
            } catch (EOFException | FileNotFoundException ignore) {
            } catch (IOException e) {
                Logger.severe("Failed to load chunk at region [%d, %d]".formatted(chunkX, chunkZ), e);
            }
            if (chunk == null) {
                return this.chunks[index] = new EmptyChunk(getWorld(), this);
            }
        }
        return chunk;
    }

    public void loadChunks() throws IOException {
        if (!getRegionFile().exists() || getRegionFile().length() <= 0) {
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(getRegionFile(), "r")) {
            for (int index = 0; index < this.chunks.length; index++) {
                Pl3xMap.api().getRegionProcessor().checkPaused();
                loadChunk(raf, index);
            }
        } catch (EOFException ignore) {
        }
    }

    public @NotNull Chunk loadChunk(@NotNull RandomAccessFile raf, int index) throws IOException {
        raf.seek(index * 4L);
        int offset = raf.read() << 16;
        offset |= (raf.read() & 0xFF) << 8;
        offset |= raf.read() & 0xFF;
        if (raf.readByte() == 0) {
            return this.chunks[index] = new EmptyChunk(getWorld(), this);
        }
        raf.seek(4096L * offset + 4); // +4 skip chunk size

        byte compressionTypeByte = raf.readByte();
        CompressionType compressionType = CompressionType.getFromID(compressionTypeByte);
        if (compressionType == null) {
            throw new IOException("Invalid compression type " + compressionTypeByte);
        }

        DataInputStream dis = new DataInputStream(new BufferedInputStream(compressionType.decompress(new FileInputStream(raf.getFD()))));
        NamedTag tag = new NBTInputStream(dis).readTag(Tag.DEFAULT_MAX_DEPTH);
        if (tag != null && tag.getTag() instanceof CompoundTag compoundTag) {
            return this.chunks[index] = Chunk.create(getWorld(), this, compoundTag, index).populate();
        } else {
            throw new IOException("Invalid data tag: " + (tag == null ? "null" : tag.getName()));
        }
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
        Region other = (Region) o;
        return getWorld().equals(other.getWorld())
                && getX() == other.getX()
                && getZ() == other.getZ();
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public @NotNull String toString() {
        return "Region{"
                + "world=" + getWorld()
                + ",x=" + getX()
                + ",z=" + getZ()
                + "}";
    }
}
