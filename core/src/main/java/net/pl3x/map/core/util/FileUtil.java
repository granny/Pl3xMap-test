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
package net.pl3x.map.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileUtil {
    public static @NotNull Path getTilesDir() {
        return getWebDir().resolve("tiles");
    }

    public static @NotNull Path getWebDir() {
        return Config.WEB_DIR.startsWith("/") ? Path.of(Config.WEB_DIR) : Pl3xMap.api().getMainDir().resolve(Config.WEB_DIR);
    }

    public static void extractFile(@NotNull Class<?> clazz, @NotNull String filename, @NotNull Path outDir, boolean replace) {
        try (InputStream in = clazz.getResourceAsStream("/" + filename)) {
            if (in == null) {
                throw new RuntimeException("Could not read file from jar! (" + filename + ")");
            }
            Path path = outDir.resolve(filename);
            if (!Files.exists(path) || replace) {
                Files.createDirectories(path.getParent());
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void extractDir(@NotNull String sourceDir, @NotNull Path outDir, boolean replace) {
        try (JarFile jarFile = new JarFile(Pl3xMap.api().getJarPath().toFile())) {
            Logger.debug("Extracting " + sourceDir + " directory from jar...");
            String path = sourceDir.substring(1);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(path)) {
                    continue;
                }
                Path file = outDir.resolve(name.substring(path.length()));
                boolean exists = Files.exists(file);
                if (!replace && exists) {
                    Logger.debug("  <yellow>exists</yellow>   " + name);
                    continue;
                }
                if (entry.isDirectory()) {
                    if (!exists) {
                        try {
                            Files.createDirectories(file);
                            Logger.debug("  <green>creating</green> " + name);
                        } catch (IOException e) {
                            Logger.debug("  <red><bold>failed</bold></red>   " + name);
                        }
                    } else {
                        Logger.debug("  <yellow>exists</yellow>   " + name);
                    }
                    continue;
                }
                try (
                        InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
                        OutputStream out = new BufferedOutputStream(new FileOutputStream(file.toFile()))
                ) {
                    byte[] buffer = new byte[4096];
                    int readCount;
                    while ((readCount = in.read(buffer)) > 0) {
                        out.write(buffer, 0, readCount);
                    }
                    out.flush();
                    Logger.debug("  <green>writing</green>  " + name);
                } catch (IOException e) {
                    Logger.debug("  <red><bold>failed</bold></red>   " + name);
                    Logger.severe("Failed to extract file (" + name + ") from jar!", e);
                }
            }
        } catch (IOException e) {
            Logger.severe("Failed to extract " + sourceDir + " directory from jar", e);
        }
    }

    public static void writeJson(@NotNull String str, @NotNull Path file) {
        Path tmp = tmp(file);
        try (
                OutputStream fileOut = Files.newOutputStream(mkDirs(tmp));
                Writer writer = new OutputStreamWriter(fileOut)
        ) {
            writer.write(str);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            atomicMove(tmp, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveGzip(@NotNull String json, @NotNull Path file) throws IOException {
        Path tmp = tmp(file);
        try (
                OutputStream fileOut = Files.newOutputStream(mkDirs(tmp));
                GZIPOutputStream gzipOut = new GZIPOutputStream(fileOut);
                Writer writer = new OutputStreamWriter(gzipOut)
        ) {
            writer.write(json);
            writer.flush();
        }
        atomicMove(tmp, file);
    }

    public static void saveGzip(byte[] bytes, @NotNull Path file) throws IOException {
        Path tmp = tmp(file);
        try (
                OutputStream fileOut = Files.newOutputStream(mkDirs(tmp));
                GZIPOutputStream gzipOut = new GZIPOutputStream(fileOut)
        ) {
            gzipOut.write(bytes);
            gzipOut.flush();
        }
        atomicMove(tmp, file);
    }

    public static void readGzip(@NotNull Path file, @NotNull ByteBuffer buffer) throws IOException {
        try (
                InputStream fileIn = Files.newInputStream(file);
                GZIPInputStream gzipIn = new GZIPInputStream(fileIn)
        ) {
            // try reading all bytes and closing stream _before_ putting into buffer
            byte[] bytes = gzipIn.readAllBytes();
            gzipIn.close();
            buffer.put(bytes);
        }
    }

    public static String readGzip(@NotNull Path file) throws IOException {
        try (
                InputStream fileIn = Files.newInputStream(file);
                GZIPInputStream gzipIn = new GZIPInputStream(fileIn);
                Reader reader = new InputStreamReader(gzipIn, StandardCharsets.UTF_8);
                Writer writer = new StringWriter()
        ) {
            char[] buffer = new char[4096];
            for (int length; (length = reader.read(buffer)) > 0; ) {
                writer.write(buffer, 0, length);
            }
            return writer.toString();
        }
    }

    public static Path tmp(Path file) {
        return file.resolveSibling("." + file.getFileName().toString() + ".tmp");
    }

    public static void atomicMove(Path source, Path target) throws IOException {
        try {
            atomicMove(source, target, 0);
        } catch (AccessDeniedException | NoSuchFileException ignore) {
        }
    }

    private static void atomicMove(Path source, Path target, int attempt) throws IOException {
        try {
            com.google.common.io.Files.move(source.toFile(), target.toFile());
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (AccessDeniedException e) {
            if (attempt < 5) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignore) {
                }
                atomicMove(source, target, ++attempt);
            } else if (source.getFileName().toString().endsWith(".tmp")) {
                try {
                    Files.delete(source);
                } catch (Throwable ignore) {
                }
            }
        }
    }

    public static @NotNull Path mkDirs(@NotNull Path file) throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
        }
        return file;
    }

    public static void createDirs(@NotNull Path dirPath) {
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteDirectory(@NotNull Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    public static @NotNull Collection<@NotNull Point> regionPathsToPoints(@NotNull World world, @Nullable Collection<@NotNull Path> paths, boolean ignoreTimestamp) {
        if (paths == null || paths.isEmpty()) {
            return Collections.emptyList();
        }
        List<Point> regions = new ArrayList<>();
        for (Path file : paths) {
            if (file.toFile().length() <= 0) {
                Logger.debug("Skipping zero length region file: " + file.getFileName());
                continue;
            }
            try {
                String[] split = file.getFileName().toString().split("\\.");
                int rX = Integer.parseInt(split[1]);
                int rZ = Integer.parseInt(split[2]);
                if (!world.visibleRegion(rX, rZ)) {
                    Logger.debug("Skipping region outside of visible areas: " + file.getFileName());
                    continue;
                }
                if (ignoreTimestamp) {
                    regions.add(Point.of(rX, rZ));
                    continue;
                }
                long storedModifiedTime = world.getRegionModifiedState().get(Mathf.asLong(rX, rZ));
                long actualModifiedTime = Files.getLastModifiedTime(file).toMillis();
                if (actualModifiedTime > storedModifiedTime) {
                    Logger.debug("Found modified region file: " + file.getFileName());
                    regions.add(Point.of(rX, rZ));
                } else {
                    //Logger.debug("Skipping unmodified region file: " + file.getFileName() + " " + actualModifiedTime + " <= " + storedModifiedTime);
                }
            } catch (NumberFormatException ignore) {
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return regions;
    }
}
