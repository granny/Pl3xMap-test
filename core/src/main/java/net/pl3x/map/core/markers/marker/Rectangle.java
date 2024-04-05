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
import net.pl3x.map.core.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a rectangle marker.
 */
@SuppressWarnings("UnusedReturnValue")
public class Rectangle extends Marker<@NotNull Rectangle> {
    private Point point1;
    private Point point2;

    private Rectangle(@NotNull String key) {
        super("rect", key);
    }

    /**
     * Create a new rectangle.
     *
     * @param key identifying key
     * @param x1  first x point
     * @param z1  first z point
     * @param x2  second x point
     * @param z2  second z point
     */
    public Rectangle(@NotNull String key, double x1, double z1, double x2, double z2) {
        this(key);
        setPoint1(Point.of(x1, z1));
        setPoint2(Point.of(x2, z2));
    }

    /**
     * Create a new rectangle.
     *
     * @param key    identifying key
     * @param point1 first point
     * @param point2 second point
     */
    public Rectangle(@NotNull String key, @NotNull Point point1, @NotNull Point point2) {
        this(key);
        setPoint1(point1);
        setPoint2(point2);
    }

    /**
     * Create a new rectangle.
     *
     * @param key identifying key
     * @param x1  first x point
     * @param z1  first z point
     * @param x2  second x point
     * @param z2  second z point
     * @return a new rectangle
     */
    public static @NotNull Rectangle of(@NotNull String key, double x1, double z1, double x2, double z2) {
        return new Rectangle(key, x1, z1, x2, z2);
    }

    /**
     * Create a new rectangle.
     *
     * @param key    identifying key
     * @param point1 first point
     * @param point2 second point
     * @return a new rectangle
     */
    public static @NotNull Rectangle of(@NotNull String key, @NotNull Point point1, @NotNull Point point2) {
        return new Rectangle(key, point1, point2);
    }

    public static @NotNull Rectangle fromJson(@NotNull JsonObject obj) {
        JsonElement el;
        Rectangle rectangle = Rectangle.of(
                obj.get("key").getAsString(),
                Point.fromJson((JsonObject) obj.get("point1")),
                Point.fromJson((JsonObject) obj.get("point2"))
        );
        if ((el = obj.get("pane")) != null && !(el instanceof JsonNull)) rectangle.setPane(el.getAsString());
        return rectangle;
    }

    /**
     * Get the first {@link Point} of this rectangle.
     *
     * @return first point
     */
    public @NotNull Point getPoint1() {
        return this.point1;
    }

    /**
     * Set the first {@link Point} of this rectangle.
     *
     * @param point1 first point
     * @return this rectangle
     */
    public @NotNull Rectangle setPoint1(@NotNull Point point1) {
        this.point1 = Preconditions.checkNotNull(point1, "Rectangle point1 is null");
        return this;
    }

    /**
     * Get the second {@link Point} of this rectangle.
     *
     * @return second point
     */
    public @NotNull Point getPoint2() {
        return this.point2;
    }

    /**
     * Set the second {@link Point} of this rectangle.
     *
     * @param point2 second point
     * @return this rectangle
     */
    public @NotNull Rectangle setPoint2(@NotNull Point point2) {
        this.point2 = Preconditions.checkNotNull(point2, "Rectangle point2 is null");
        return this;
    }

    @Override
    public @NotNull JsonObject toJson() {
        JsonObjectWrapper wrapper = new JsonObjectWrapper();
        wrapper.addProperty("key", getKey());
        wrapper.addProperty("point1", getPoint1());
        wrapper.addProperty("point2", getPoint2());
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
        Rectangle other = (Rectangle) o;
        return getKey().equals(other.getKey())
                && getPoint1().equals(other.getPoint1())
                && getPoint2().equals(other.getPoint2())
                && Objects.equals(getPane(), other.getPane())
                && Objects.equals(getOptions(), other.getOptions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getPoint1(), getPoint2(), getPane(), getOptions());
    }

    @Override
    public @NotNull String toString() {
        return "Rectangle{"
                + "key=" + getKey()
                + ",point1=" + getPoint1()
                + ",point2=" + getPoint2()
                + ",pane=" + getPane()
                + ",options=" + getOptions()
                + "}";
    }
}
