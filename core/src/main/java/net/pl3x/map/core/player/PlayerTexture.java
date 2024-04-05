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
package net.pl3x.map.core.player;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.util.Colors;
import net.pl3x.map.core.util.FileUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Task to fetch and save a players skin.
 */
public class PlayerTexture extends Thread {
    private static final Path SKINS_2D_DIR = FileUtil.getWebDir().resolve("images/skins/2D");
    private static final Path SKINS_3D_DIR = FileUtil.getWebDir().resolve("images/skins/3D");
    private static final URL STEVE_SKIN;

    static {
        try {
            Files.createDirectories(SKINS_2D_DIR);
            Files.createDirectories(SKINS_3D_DIR);
            STEVE_SKIN = FileUtil.getWebDir().resolve("images/skins/steve.png").toUri().toURL();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final UUID uuid;
    private final URL url;

    public PlayerTexture(@NotNull Player player) {
        this.uuid = player.getUUID();
        URL url = player.getSkin();
        if (url == null) {
            url = STEVE_SKIN;
        }
        this.url = url;
    }

    private static @NotNull BufferedImage get2DHead(@NotNull BufferedImage source) {
        return getPart(source, 8, 8);
    }

    private static @NotNull BufferedImage get3DHead(@NotNull BufferedImage source) {
        // get parts
        BufferedImage left = getPart(source, 8, 8);
        BufferedImage right = getPart(source, 16, 8);
        BufferedImage top = getPart(source, 8, 0);

        // scale parts up to 512
        left = scale(left, 16, 16 * 0.86062D);
        right = scale(right, 16, 16 * 0.86062D);
        top = scale(top, 16, 16 * 0.86062D);

        // shear parts
        left = shear(left);
        right = flip(shear(flip(right)));
        top = flip(shear(flip(top)));

        // rotate parts
        left = rotate(left, 0.523599);
        right = rotate(right, -0.523599);
        top = rotate(top, 0.523599);

        // combine parts (884x765)
        BufferedImage result = new BufferedImage(1024, 1024, left.getType());
        Graphics2D g2d = result.createGraphics();
        g2d.translate(-151, 257);
        g2d.drawImage(left, 0, 0, null);
        g2d.translate(442, 0);
        g2d.drawImage(right, 0, 0, null);
        g2d.translate(-222, -382);
        g2d.drawImage(top, 0, 0, null);
        g2d.dispose();

        // scale result down to 128
        result = scale(result, 0.125, 0.125);

        return result;
    }

    private static @NotNull BufferedImage getPart(@NotNull BufferedImage source, int x, int y) {
        BufferedImage head = source.getSubimage(x, y, 8, 8);
        BufferedImage helm = source.getSubimage(x + 32, y, 8, 8);
        BufferedImage result = new BufferedImage(32, 32, source.getType());
        for (int x1 = 0; x1 < 32; x1++) {
            for (int z1 = 0; z1 < 32; z1++) {
                int argb = Colors.blend(
                        helm.getRGB(x1 / 4, z1 / 4),
                        head.getRGB(x1 / 4, z1 / 4)
                );
                result.setRGB(x1, z1, argb);
            }
        }
        return result;
    }

    private static @NotNull BufferedImage flip(@NotNull BufferedImage src) {
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(-1, 1));
        at.concatenate(AffineTransform.getTranslateInstance(-src.getWidth(), 0));
        return transform(src, at);
    }

    private static @NotNull BufferedImage rotate(@NotNull BufferedImage src, double angle) {
        int w = src.getWidth();
        int h = src.getHeight();
        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));
        int newWidth = (int) (w * cos + h * sin);
        int newHeight = (int) (h * cos + w * sin);

        BufferedImage dest = new BufferedImage(newWidth, newHeight, src.getType());

        Graphics2D g2d = dest.createGraphics();
        g2d.translate((newWidth - w) / 2, (newHeight - h) / 2);
        g2d.rotate(angle, w / 2D, h / 2D);
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();

        return dest;
    }

    private static @NotNull BufferedImage scale(@NotNull BufferedImage src, double scaleX, double scaleY) {
        AffineTransform at = AffineTransform.getScaleInstance(scaleX, scaleY);
        return transform(src, at);
    }

    private static @NotNull BufferedImage shear(@NotNull BufferedImage src) {
        AffineTransform at = AffineTransform.getShearInstance(0.577375, 0);
        return transform(src, at);
    }

    private static @NotNull BufferedImage transform(@NotNull BufferedImage src, @NotNull AffineTransform at) {
        return new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC).filter(src, null);
    }

    @Override
    public void run() {
        if (this.url == null) {
            return;
        }

        InputStream inputStream = null;
        try {
            inputStream = this.url.openStream();
        } catch (IOException e) {
            Logger.warn("Failed to get texture of %s from %s: %s".formatted(this.uuid, this.url, e.getCause()), e);
            return;
        }

        try {
            BufferedImage textureSource = ImageIO.read(inputStream);

            BufferedImage head2D = get2DHead(textureSource);
            ImageIO.write(head2D, "png", SKINS_2D_DIR.resolve(this.uuid + ".png").toFile());

            BufferedImage head3D;
            try {
                head3D = get3DHead(textureSource);
            } catch (NoClassDefFoundError e) {
                // happens in headless environments (missing awt's GraphicsEnvironment)
                // just draw a 2d head and put it in the 3d directory for now
                head3D = head2D;
            }
            ImageIO.write(head3D, "png", SKINS_3D_DIR.resolve(this.uuid + ".png").toFile());
        } catch (Throwable t) {
            Logger.warn("Failed to process player texture with uuid of %s".formatted(this.uuid), t);
        }
    }
}
