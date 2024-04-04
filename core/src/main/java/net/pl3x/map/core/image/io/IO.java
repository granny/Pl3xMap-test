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
package net.pl3x.map.core.image.io;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import net.pl3x.map.core.Keyed;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.registry.Registry;
import net.pl3x.map.core.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IO {
    private static final Registry<@NotNull Type> TYPES = new Registry<>();

    public static void register() {
        IO.register("bmp", new Bmp());
        IO.register("gif", new Gif());
        IO.register("jpg", new Jpg());
        IO.register("jpeg", get("jpg"));
        IO.register("png", new Png());
    }

    public static void register(@NotNull String name, @NotNull Type type) {
        if (TYPES.has(name)) {
            throw new IllegalStateException(String.format("IO type %s already registered", name));
        }
        TYPES.register(name, type);
    }

    public static void unregister() {
        TYPES.unregister();
    }

    public static void unregister(@NotNull String name) {
        TYPES.unregister(name);
    }

    public static @NotNull Type get(@NotNull String format) {
        Type type = TYPES.get(format.toLowerCase(Locale.ROOT));
        if (type == null) {
            throw new IllegalStateException("Unknown or unsupported image format");
        }
        return type;
    }

    public abstract static class Type extends Keyed {
        public Type(@NotNull String key) {
            super(key);
        }

        public @NotNull BufferedImage createBuffer() {
            return new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        }

        public int color(int argb) {
            return argb;
        }

        public @Nullable BufferedImage read(@NotNull Path path) {
            BufferedImage buffer = null;
            ImageReader reader = null;
            try (ImageInputStream in = ImageIO.createImageInputStream(Files.newInputStream(path))) {
                reader = ImageIO.getImageReadersBySuffix(getKey()).next();
                reader.setInput(in, false, true);
                buffer = reader.read(0);
                in.flush();
            } catch (IOException e) {
                Logger.warn("Could not read tile image: " + path, e);
            } finally {
                if (reader != null) {
                    reader.dispose();
                }
            }
            return buffer;
        }

        public void write(@NotNull Path path, @NotNull BufferedImage buffer) {
            Path tmp = FileUtil.tmp(path);
            ImageWriter writer = null;
            try (ImageOutputStream out = ImageIO.createImageOutputStream(tmp.toFile())) {
                writer = ImageIO.getImageWritersBySuffix(getKey()).next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    if (param.getCompressionType() == null) {
                        param.setCompressionType(param.getCompressionTypes()[0]);
                    }
                    param.setCompressionQuality((float) Config.WEB_TILE_QUALITY);
                }
                writer.setOutput(out);
                writer.write(null, new IIOImage(buffer, null, null), param);
                out.flush();
            } catch (IOException e) {
                Logger.warn("Could not write tile image: " + tmp, e);
            } finally {
                if (writer != null) {
                    writer.dispose();
                }
            }
            try {
                FileUtil.atomicMove(tmp, path);
            } catch (IOException e) {
                Logger.warn("Could not write tile image: " + path, e);
            }
        }
    }
}
