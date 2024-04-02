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
package net.pl3x.map.core.markers.marker;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import javax.imageio.ImageIO;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.JsonObjectWrapper;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.Vector;
import net.pl3x.map.core.registry.IconRegistry;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an icon marker.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Icon extends Marker<@NotNull Icon> {
    private Point point;
    private String image;
    private String retina;
    private Vector size;
    private Vector anchor;
    private Double rotationAngle;
    private String rotationOrigin;
    private String shadow;
    private String shadowRetina;
    private Vector shadowSize;
    private Vector shadowAnchor;

    private Icon(@NotNull String key) {
        super("icon", key);
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param x     icon x location on map
     * @param z     icon z location on map
     * @param image image key
     */
    public Icon(@NotNull String key, double x, double z, @NotNull String image) {
        this(key);
        setPoint(Point.of(x, z));
        setImage(image);
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param point icon location on map
     * @param image image key
     */
    public Icon(@NotNull String key, @NotNull Point point, @NotNull String image) {
        this(key);
        setPoint(point);
        setImage(image);
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param x     icon x location on map
     * @param z     icon z location on map
     * @param image image key
     * @param size  size of image
     */
    public Icon(@NotNull String key, double x, double z, @NotNull String image, double size) {
        this(key);
        setPoint(Point.of(x, z));
        setImage(image);
        setSize(Vector.of(size, size));
    }

    /**
     * Create a new icon.
     *
     * @param key    identifying key
     * @param x      icon x location on map
     * @param z      icon z location on map
     * @param image  image key
     * @param width  width of image
     * @param height height of image
     */
    public Icon(@NotNull String key, double x, double z, @NotNull String image, double width, double height) {
        this(key);
        setPoint(Point.of(x, z));
        setImage(image);
        setSize(Vector.of(width, height));
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param point icon location on map
     * @param image image key
     * @param size  size of image
     */
    public Icon(@NotNull String key, @NotNull Point point, @NotNull String image, double size) {
        this(key);
        setPoint(point);
        setImage(image);
        setSize(Vector.of(size, size));
    }

    /**
     * Create a new icon.
     *
     * @param key    identifying key
     * @param point  icon location on map
     * @param image  image key
     * @param width  width of image
     * @param height height of image
     */
    public Icon(@NotNull String key, @NotNull Point point, @NotNull String image, double width, double height) {
        this(key);
        setPoint(point);
        setImage(image);
        setSize(Vector.of(width, height));
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param point icon location on map
     * @param image image key
     * @param size  size of image
     */
    public Icon(@NotNull String key, @NotNull Point point, @NotNull String image, @Nullable Vector size) {
        this(key);
        setPoint(point);
        setImage(image);
        setSize(size);
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param x     icon x location on map
     * @param z     icon z location on map
     * @param image image key
     * @return a new icon
     */
    public static @NotNull Icon of(@NotNull String key, double x, double z, @NotNull String image) {
        return new Icon(key, x, z, image);
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param point icon location on map
     * @param image image key
     * @return a new icon
     */
    public static @NotNull Icon of(@NotNull String key, @NotNull Point point, @NotNull String image) {
        return new Icon(key, point, image);
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param x     icon x location on map
     * @param z     icon z location on map
     * @param image image key
     * @param size  size of image
     * @return a new icon
     */
    public static @NotNull Icon of(@NotNull String key, double x, double z, @NotNull String image, double size) {
        return new Icon(key, x, z, image, size);
    }

    /**
     * Create a new icon.
     *
     * @param key    identifying key
     * @param x      icon x location on map
     * @param z      icon z location on map
     * @param image  image key
     * @param width  width of image
     * @param height height of image
     * @return a new icon
     */
    public static @NotNull Icon of(@NotNull String key, double x, double z, @NotNull String image, double width, double height) {
        return new Icon(key, x, z, image, width, height);
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param point icon location on map
     * @param image image key
     * @param size  size of image
     * @return a new icon
     */
    public static @NotNull Icon of(@NotNull String key, @NotNull Point point, @NotNull String image, double size) {
        return new Icon(key, point, image, size);
    }

    /**
     * Create a new icon.
     *
     * @param key    identifying key
     * @param point  icon location on map
     * @param image  image key
     * @param width  width of image
     * @param height height of image
     * @return a new icon
     */
    public static @NotNull Icon of(@NotNull String key, @NotNull Point point, @NotNull String image, double width, double height) {
        return new Icon(key, point, image, width, height);
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param point icon location on map
     * @param image image key
     * @param size  size of image
     * @return a new icon
     */
    public static @NotNull Icon of(@NotNull String key, @NotNull Point point, @NotNull String image, @Nullable Vector size) {
        return new Icon(key, point, image, size);
    }

    /**
     * Get the {@link Point} on the map for this icon.
     *
     * @return point on map
     */
    public @NotNull Point getPoint() {
        return this.point;
    }

    /**
     * Set a new {@link Point} on the map for this icon.
     *
     * @param point new point on map
     * @return this icon
     */
    public @NotNull Icon setPoint(@NotNull Point point) {
        this.point = Preconditions.checkNotNull(point, "Icon point is null");
        return this;
    }

    /**
     * Get the image to use for this icon.
     *
     * @return image
     * @see IconRegistry
     */
    public @NotNull String getImage() {
        return this.image;
    }

    /**
     * Set the image to use for this icon.
     * <p>
     * Key must be registered with the icon registry.
     *
     * @param image new image
     * @return this icon
     * @see IconRegistry
     */
    public @NotNull Icon setImage(@NotNull String image) {
        Preconditions.checkNotNull(image, "Icon key is null");
        Preconditions.checkNotNull(Pl3xMap.api().getIconRegistry().get(image), String.format("Icon not in registry (%s)", image));
        this.image = image;
        return this;
    }

    /**
     * Get the retina sized image to use for this icon.
     * <p>
     * This image will be used on retina devices.
     * <p>
     * Defaults to '<code>{@link #getImage()}</code>' if null.
     *
     * @return retina image
     * @see IconRegistry
     */
    public @Nullable String getRetina() {
        return this.retina;
    }

    /**
     * Set the retina sized image to use for this icon.
     * <p>
     * This image will be used on retina devices.
     * <p>
     * Key must be registered with the icon registry.
     * <p>
     * Defaults to '<code>{@link #getImage()}</code>' if null.
     *
     * @param retina new retina image
     * @return this icon
     * @see IconRegistry
     */
    public @NotNull Icon setRetina(@Nullable String retina) {
        Preconditions.checkArgument(retina == null || Pl3xMap.api().getIconRegistry().has(retina), String.format("Icon not in registry (%s)", retina));
        this.retina = retina;
        return this;
    }

    /**
     * Get the size of the image, in pixels.
     * <p>
     * Used for auto centering the image on '<code>{@link #getPoint()}</code>' if set.
     *
     * @return image size
     */
    public @Nullable Vector getSize() {
        return this.size;
    }

    /**
     * Set the size of the image, in pixels.
     * <p>
     * Used for auto centering the image on '<code>{@link #getPoint()}</code>' if set.
     *
     * @param size new image size
     * @return this icon
     */
    public @NotNull Icon setSize(@Nullable Vector size) {
        this.size = size;
        return this;
    }

    /**
     * Get the coordinates of the "tip" of the icon (relative to its top left corner).
     * <p>
     * The icon will be aligned so that this point is at {@link #getPoint()}.
     * <p>
     * Centered by default if '<code>{@link #getSize()}</code>' is also set.
     *
     * @return icon anchor
     */
    public @Nullable Vector getAnchor() {
        return this.anchor;
    }

    /**
     * Set the coordinates of the "tip" of the icon (relative to its top left corner).
     * <p>
     * The icon will be aligned so that this point is at {@link #getPoint()}.
     * <p>
     * Centered by default if '<code>{@link #getSize()}</code>' is also set.
     *
     * @param anchor new anchor
     * @return this icon
     */
    public @NotNull Icon setAnchor(@Nullable Vector anchor) {
        this.anchor = anchor;
        return this;
    }

    /**
     * Get the rotation angle, in degrees, clockwise.
     * <p>
     * Defaults to '<code>0</code>' if null.
     *
     * @return angle of rotation
     */
    public @Nullable Double getRotationAngle() {
        return this.rotationAngle;
    }

    /**
     * Set the rotation angle, in degrees, clockwise.
     * <p>
     * Defaults to '<code>0</code>' if null.
     *
     * @param rotationAngle angle of rotation
     * @return this icon
     */
    public @NotNull Icon setRotationAngle(@Nullable Double rotationAngle) {
        this.rotationAngle = rotationAngle;
        return this;
    }

    /**
     * Get the rotation origin, as a transform-origin CSS rule.
     * <p>
     * Defaults to '<code>bottom center</code>' if null.
     *
     * @return origin of rotation
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/transform-origin">MDN transform-origin</a>
     */
    public @Nullable String getRotationOrigin() {
        return this.rotationOrigin;
    }

    /**
     * Set the rotation origin, as a transform-origin CSS rule.
     * <p>
     * Defaults to '<code>bottom center</code>' if null.
     *
     * @param rotationOrigin origin of rotation
     * @return this icon
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/transform-origin">MDN transform-origin</a>
     */
    public @NotNull Icon setRotationOrigin(@Nullable String rotationOrigin) {
        this.rotationOrigin = rotationOrigin;
        return this;
    }

    /**
     * Get shadow image of this icon.
     * <p>
     * No shadow image will be shown, if null.
     *
     * @return shadow image
     */
    public @Nullable String getShadow() {
        return this.shadow;
    }

    /**
     * Set the shadow image to use for this icon.
     * <p>
     * Key must be registered with the icon registry.
     * <p>
     * No shadow image will be shown, if null.
     *
     * @param shadow new shadow image
     * @return this icon
     * @see IconRegistry
     */
    public @NotNull Icon setShadow(@Nullable String shadow) {
        Preconditions.checkArgument(shadow == null || Pl3xMap.api().getIconRegistry().has(shadow), String.format("Icon not in registry (%s)", shadow));
        this.shadow = shadow;
        return this;
    }

    /**
     * Get the retina sized shadow image to use for this icon.
     * <p>
     * This shadow image will be used on retina devices.
     * <p>
     * Key must be registered with the icon registry.
     * <p>
     * Defaults to '<code>{@link #getShadow()}</code>' if null.
     *
     * @return retina shadow image
     * @see IconRegistry
     */
    public @Nullable String getShadowRetina() {
        return this.shadowRetina;
    }

    /**
     * Set the retina sized shadow image to use for this icon.
     * <p>
     * This shadow image will be used on retina devices.
     * <p>
     * Key must be registered with the icon registry.
     * <p>
     * Defaults to '<code>{@link #getShadow()}</code>' if null.
     *
     * @param shadowRetina new retina shadow image
     * @return this icon
     * @see IconRegistry
     */
    public @NotNull Icon setShadowRetina(@Nullable String shadowRetina) {
        Preconditions.checkArgument(shadowRetina == null || Pl3xMap.api().getIconRegistry().has(shadowRetina), String.format("Icon not in registry (%s)", shadowRetina));
        this.shadowRetina = shadowRetina;
        return this;
    }

    /**
     * Get the size of the shadow image in pixels.
     * <p>
     * Used for auto centering the shadow image on '<code>{@link #getPoint()}</code>' if set.
     *
     * @return shadow image size
     */
    public @Nullable Vector getShadowSize() {
        return this.shadowSize;
    }

    /**
     * Set the size of the shadow image in pixels.
     * <p>
     * Used for auto centering the shadow image on '<code>{@link #getPoint()}</code>' if set.
     *
     * @param shadowSize new shadow image size
     * @return this icon
     */
    public @NotNull Icon setShadowSize(@Nullable Vector shadowSize) {
        this.shadowSize = shadowSize;
        return this;
    }

    /**
     * Get the coordinates of the "tip" of the shadow image (relative to its top left corner).
     * <p>
     * The icon will be aligned so that this point is at {@link #getPoint()}.
     * <p>
     * The same as {@link #getAnchor()} if null.
     *
     * @return icon anchor
     */
    public @Nullable Vector getShadowAnchor() {
        return this.shadowAnchor;
    }

    /**
     * Set the coordinates of the "tip" of the shadow image (relative to its top left corner).
     * <p>
     * The shadow image will be aligned so that this point is at {@link #getPoint()}.
     * <p>
     * The same as {@link #getAnchor()} if null.
     *
     * @param shadowAnchor new anchor
     * @return this icon
     */
    public @NotNull Icon setShadowAnchor(@Nullable Vector shadowAnchor) {
        this.shadowAnchor = shadowAnchor;
        return this;
    }

    @Override
    public @NotNull JsonObject toJson() {
        JsonObjectWrapper wrapper = new JsonObjectWrapper();
        wrapper.addProperty("key", getKey());
        wrapper.addProperty("point", getPoint());
        wrapper.addProperty("image", getImage());
        wrapper.addProperty("retina", getRetina());
        wrapper.addProperty("size", getSize());
        wrapper.addProperty("anchor", getAnchor());
        wrapper.addProperty("shadow", getShadow());
        wrapper.addProperty("shadowRetina", getShadowRetina());
        wrapper.addProperty("shadowSize", getShadowSize());
        wrapper.addProperty("shadowAnchor", getShadowAnchor());
        wrapper.addProperty("rotationAngle", getRotationAngle());
        wrapper.addProperty("rotationOrigin", getRotationOrigin());
        wrapper.addProperty("pane", getPane());
        return wrapper.getJsonObject();
    }

    public static @NotNull Icon fromJson(@NotNull JsonObject obj) {
        JsonElement el;
        Icon icon = Icon.of(
                obj.get("key").getAsString(),
                Point.fromJson((JsonObject) obj.get("point")),
                Objects.requireNonNull(registerIconImage("image", obj))
        );
        icon.setRetina(registerIconImage("retina", obj));
        if ((el = obj.get("size")) != null && !(el instanceof JsonNull)) icon.setSize(Vector.fromJson((JsonObject) obj.get("size")));
        if ((el = obj.get("anchor")) != null && !(el instanceof JsonNull)) icon.setAnchor(Vector.fromJson((JsonObject) obj.get("anchor")));
        icon.setShadow(registerIconImage("shadow", obj));
        icon.setShadowRetina(registerIconImage("shadowRetina", obj));
        if ((el = obj.get("shadowSize")) != null && !(el instanceof JsonNull)) icon.setShadowSize(Vector.fromJson((JsonObject) obj.get("shadowSize")));
        if ((el = obj.get("shadowAnchor")) != null && !(el instanceof JsonNull)) icon.setShadowAnchor(Vector.fromJson((JsonObject) obj.get("shadowAnchor")));
        if ((el = obj.get("rotationAngle")) != null && !(el instanceof JsonNull)) icon.setRotationAngle(el.getAsDouble());
        if ((el = obj.get("rotationOrigin")) != null && !(el instanceof JsonNull)) icon.setRotationOrigin(el.getAsString());
        if ((el = obj.get("pane")) != null && !(el instanceof JsonNull)) icon.setPane(el.getAsString());
        return icon;
    }

    private static @Nullable String registerIconImage(@NotNull String key, @NotNull JsonObject obj) {
        JsonElement el;
        String image = null;
        if ((el = obj.get(key)) != null && !(el instanceof JsonNull)) {
            try {
                Path path = FileUtil.getWebDir().resolve("images/icon/" + (image = el.getAsString()) + ".png");
                IconImage icon = new IconImage(image, ImageIO.read(path.toFile()), "png");
                Pl3xMap.api().getIconRegistry().register(icon);
            } catch (IOException e) {
                throw new RuntimeException("Can't read file from path: %s".formatted("images/icon/" + (image = el.getAsString()) + ".png"), e);
            }
        }
        return image;
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
        Icon other = (Icon) o;
        return getKey().equals(other.getKey())
                && getPoint().equals(other.getPoint())
                && isImageEquals(other)
                && isPositionEquals(other)
                && isShadowEquals(other)
                && Objects.equals(getPane(), other.getPane())
                && Objects.equals(getOptions(), other.getOptions());
    }

    private boolean isImageEquals(@Nullable Icon other) {
        return getImage().equals(other.getImage())
                && Objects.equals(getRetina(), other.getRetina())
                && Objects.equals(getSize(), other.getSize());
    }

    private boolean isPositionEquals(@Nullable Icon other) {
        return Objects.equals(getAnchor(), other.getAnchor())
                && Objects.equals(getRotationAngle(), other.getRotationAngle())
                && Objects.equals(getRotationOrigin(), other.getRotationOrigin());
    }

    private boolean isShadowEquals(@Nullable Icon other) {
        return Objects.equals(getShadow(), other.getShadow())
                && Objects.equals(getShadowRetina(), other.getShadowRetina())
                && Objects.equals(getShadowSize(), other.getShadowSize())
                && Objects.equals(getShadowAnchor(), other.getShadowAnchor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getPoint(), getImage(), getRetina(), getSize(), getAnchor(), getRotationAngle(),
                getRotationOrigin(), getShadow(), getShadowRetina(), getShadowSize(), getShadowAnchor(), getPane(), getOptions());
    }

    @Override
    public @NotNull String toString() {
        return "Icon{"
                + "key=" + getKey()
                + ",point=" + getPoint()
                + ",image=" + getImage()
                + ",retina=" + getRetina()
                + ",size=" + getSize()
                + ",anchor=" + getAnchor()
                + ",rotationAngle=" + getRotationAngle()
                + ",rotationOrigin=" + getRotationOrigin()
                + ",shadow=" + getShadow()
                + ",shadowRetina=" + getShadowRetina()
                + ",shadowSize=" + getShadowSize()
                + ",shadowAnchor=" + getShadowAnchor()
                + ",pane=" + getPane()
                + ",options=" + getOptions()
                + "}";
    }
}
