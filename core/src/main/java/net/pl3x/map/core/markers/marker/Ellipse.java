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
import java.util.Objects;
import net.pl3x.map.core.markers.JsonObjectWrapper;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.Vector;
import net.pl3x.map.core.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an ellipse marker.
 */
@SuppressWarnings("UnusedReturnValue")
public class Ellipse extends Marker<@NotNull Ellipse> {
    private Point center;
    private Vector radius;
    private Double tilt;

    private Ellipse(@NotNull String key) {
        super("elli", key);
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param centerX center x location
     * @param centerZ center z location
     * @param radiusX x radius
     * @param radiusZ z radius
     */
    public Ellipse(@NotNull String key, double centerX, double centerZ, double radiusX, double radiusZ) {
        this(key, Point.of(centerX, centerZ), Vector.of(radiusX, radiusZ));
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param center  center location
     * @param radiusX x radius
     * @param radiusZ z radius
     */
    public Ellipse(@NotNull String key, @NotNull Point center, double radiusX, double radiusZ) {
        this(key, center, Vector.of(radiusX, radiusZ));
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param centerX center x location
     * @param centerZ center z location
     * @param radius  radius
     */
    public Ellipse(@NotNull String key, double centerX, double centerZ, @NotNull Vector radius) {
        this(key, Point.of(centerX, centerZ), radius);
    }

    /**
     * Create a new ellipse.
     *
     * @param key    identifying key
     * @param center center location
     * @param radius radius
     */
    public Ellipse(@NotNull String key, @NotNull Point center, @NotNull Vector radius) {
        this(key);
        setCenter(center);
        setRadius(radius);
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param centerX center x location
     * @param centerZ center z location
     * @param radiusX x radius
     * @param radiusZ z radius
     * @param tilt    tilt
     */
    public Ellipse(@NotNull String key, double centerX, double centerZ, double radiusX, double radiusZ, double tilt) {
        this(key, Point.of(centerX, centerZ), Vector.of(radiusX, radiusZ), tilt);
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param center  center location
     * @param radiusX x radius
     * @param radiusZ z radius
     * @param tilt    tilt
     */
    public Ellipse(@NotNull String key, @NotNull Point center, double radiusX, double radiusZ, double tilt) {
        this(key, center, Vector.of(radiusX, radiusZ), tilt);
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param centerX center x location
     * @param centerZ center z location
     * @param radius  radius
     * @param tilt    tilt
     */
    public Ellipse(@NotNull String key, double centerX, double centerZ, @NotNull Vector radius, double tilt) {
        this(key, Point.of(centerX, centerZ), radius, tilt);
    }

    /**
     * Create a new ellipse.
     *
     * @param key    identifying key
     * @param center center location
     * @param radius radius
     * @param tilt   tilt
     */
    public Ellipse(@NotNull String key, @NotNull Point center, @NotNull Vector radius, double tilt) {
        this(key);
        setCenter(center);
        setRadius(radius);
        setTilt(tilt);
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param centerX center x location
     * @param centerZ center z location
     * @param radiusX x radius
     * @param radiusZ z radius
     * @return a new ellipse
     */
    public static @NotNull Ellipse of(@NotNull String key, double centerX, double centerZ, double radiusX, double radiusZ) {
        return new Ellipse(key, centerX, centerZ, radiusX, radiusZ);
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param center  center location
     * @param radiusX x radius
     * @param radiusZ z radius
     * @return a new ellipse
     */
    public static @NotNull Ellipse of(@NotNull String key, @NotNull Point center, double radiusX, double radiusZ) {
        return new Ellipse(key, center, radiusX, radiusZ);
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param centerX center x location
     * @param centerZ center z location
     * @param radius  radius
     * @return a new ellipse
     */
    public static @NotNull Ellipse of(@NotNull String key, double centerX, double centerZ, @NotNull Vector radius) {
        return new Ellipse(key, centerX, centerZ, radius);
    }

    /**
     * Create a new ellipse.
     *
     * @param key    identifying key
     * @param center center location
     * @param radius radius
     * @return a new ellipse
     */
    public static @NotNull Ellipse of(@NotNull String key, @NotNull Point center, @NotNull Vector radius) {
        return new Ellipse(key, center, radius);
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param centerX center x location
     * @param centerZ center z location
     * @param radiusX x radius
     * @param radiusZ z radius
     * @param tilt    tilt
     * @return a new ellipse
     */
    public static @NotNull Ellipse of(@NotNull String key, double centerX, double centerZ, double radiusX, double radiusZ, double tilt) {
        return new Ellipse(key, centerX, centerZ, radiusX, radiusZ, tilt);
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param center  center location
     * @param radiusX x radius
     * @param radiusZ z radius
     * @param tilt    tilt
     * @return a new ellipse
     */
    public static @NotNull Ellipse of(@NotNull String key, @NotNull Point center, double radiusX, double radiusZ, double tilt) {
        return new Ellipse(key, center, radiusX, radiusZ, tilt);
    }

    /**
     * Create a new ellipse.
     *
     * @param key     identifying key
     * @param centerX center x location
     * @param centerZ center z location
     * @param radius  radius
     * @param tilt    tilt
     * @return a new ellipse
     */
    public static @NotNull Ellipse of(@NotNull String key, double centerX, double centerZ, @NotNull Vector radius, double tilt) {
        return new Ellipse(key, centerX, centerZ, radius, tilt);
    }

    /**
     * Create a new ellipse.
     *
     * @param key    identifying key
     * @param center center location
     * @param radius radius
     * @param tilt   tilt
     * @return a new ellipse
     */
    public static @NotNull Ellipse of(@NotNull String key, @NotNull Point center, @NotNull Vector radius, double tilt) {
        return new Ellipse(key, center, radius, tilt);
    }

    public static @NotNull Ellipse fromJson(@NotNull JsonObject obj) {
        JsonElement el;
        Ellipse ellipse = Ellipse.of(
                obj.get("key").getAsString(),
                Point.fromJson((JsonObject) obj.get("center")),
                Vector.fromJson((JsonObject) obj.get("radius"))
        );
        if ((el = obj.get("tilt")) != null && !(el instanceof JsonNull)) ellipse.setTilt(el.getAsDouble());
        if ((el = obj.get("pane")) != null && !(el instanceof JsonNull)) ellipse.setPane(el.getAsString());
        return ellipse;
    }

    /**
     * Get the center {@link Point} of this ellipse.
     *
     * @return center point
     */
    public @NotNull Point getCenter() {
        return this.center;
    }

    /**
     * Set a new center {@link Point} for this ellipse.
     *
     * @param center new center
     * @return this ellipse
     */
    public @NotNull Ellipse setCenter(@NotNull Point center) {
        this.center = Preconditions.checkNotNull(center, "Ellipse center is null");
        return this;
    }

    /**
     * Get the radius for this ellipse.
     *
     * @return radius
     */
    public @NotNull Vector getRadius() {
        return this.radius;
    }

    /**
     * Set a new radius for this ellipse.
     *
     * @param radius new radius
     * @return this ellipse
     */
    public @NotNull Ellipse setRadius(@NotNull Vector radius) {
        this.radius = Preconditions.checkNotNull(radius, "Ellipse radius is null");
        return this;
    }

    /**
     * Get the tilt of this ellipse, in degrees.
     * <p>
     * Defaults to '<code>0</code>' if null.
     *
     * @return tilt
     */
    public @Nullable Double getTilt() {
        return this.tilt;
    }

    /**
     * Set the tilt of this ellipse, in degrees.
     * <p>
     * Defaults to '<code>0</code>' if null.
     *
     * @param tilt new tilt
     * @return this ellipse
     */
    public @NotNull Ellipse setTilt(@Nullable Double tilt) {
        this.tilt = tilt;
        return this;
    }

    @Override
    public @NotNull JsonObject toJson() {
        JsonObjectWrapper wrapper = new JsonObjectWrapper();
        wrapper.addProperty("key", getKey());
        wrapper.addProperty("center", getCenter());
        wrapper.addProperty("radius", getRadius());
        wrapper.addProperty("tilt", getTilt());
        wrapper.addProperty("pane", getPane());
        return wrapper.getJsonObject();
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
        Ellipse other = (Ellipse) o;
        return getKey().equals(other.getKey())
                && Objects.equals(getCenter(), other.getCenter())
                && Objects.equals(getRadius(), other.getRadius())
                && Objects.equals(getTilt(), other.getTilt())
                && Objects.equals(getPane(), other.getPane())
                && Objects.equals(getOptions(), other.getOptions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getCenter(), getRadius(), getTilt(), getPane(), getOptions());
    }

    @Override
    public @NotNull String toString() {
        return "Ellipse{"
                + "key=" + getKey()
                + ",center=" + getCenter()
                + ",radius=" + getRadius()
                + ",tile=" + getTilt()
                + ",pane=" + getPane()
                + ",options=" + getOptions()
                + "}";
    }
}
