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
import java.util.List;
import net.pl3x.map.core.Keyed;
import net.pl3x.map.core.markers.JsonSerializable;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.Vector;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Represents a map marker.
 */
public abstract class Marker<T extends Marker<@NotNull T>> extends Keyed implements JsonSerializable {
    private final String type;
    private String pane;
    private Options options;

    /**
     * Create a new marker.
     *
     * @param type type of marker
     * @param key  identifying key
     */
    public Marker(@NotNull String type, @NotNull String key) {
        super(key);
        this.type = Preconditions.checkNotNull(type, "Marker type is null");
    }

    /**
     * Create a new circle.
     *
     * @param key     identifying key
     * @param centerX center x location
     * @param centerZ center z location
     * @param radius  circle radius
     * @return a new circle
     */
    public static @NotNull Circle circle(@NotNull String key, double centerX, double centerZ, double radius) {
        return Circle.of(key, centerX, centerZ, radius);
    }

    /**
     * Create a new circle.
     *
     * @param key    identifying key
     * @param center center location
     * @param radius circle radius
     * @return a new circle
     */
    public static @NotNull Circle circle(@NotNull String key, @NotNull Point center, double radius) {
        return Circle.of(key, center, radius);
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
    public static @NotNull Ellipse ellipse(@NotNull String key, double centerX, double centerZ, double radiusX, double radiusZ) {
        return Ellipse.of(key, centerX, centerZ, radiusX, radiusZ);
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
    public static @NotNull Ellipse ellipse(@NotNull String key, @NotNull Point center, double radiusX, double radiusZ) {
        return Ellipse.of(key, center, radiusX, radiusZ);
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
    public static @NotNull Ellipse ellipse(@NotNull String key, double centerX, double centerZ, @NotNull Vector radius) {
        return Ellipse.of(key, centerX, centerZ, radius);
    }

    /**
     * Create a new ellipse.
     *
     * @param key    identifying key
     * @param center center location
     * @param radius radius
     * @return a new ellipse
     */
    public static @NotNull Ellipse ellipse(@NotNull String key, @NotNull Point center, @NotNull Vector radius) {
        return Ellipse.of(key, center, radius);
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
    public static @NotNull Ellipse ellipse(@NotNull String key, double centerX, double centerZ, double radiusX, double radiusZ, double tilt) {
        return Ellipse.of(key, centerX, centerZ, radiusX, radiusZ, tilt);
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
    public static @NotNull Ellipse ellipse(@NotNull String key, @NotNull Point center, double radiusX, double radiusZ, double tilt) {
        return Ellipse.of(key, center, radiusX, radiusZ, tilt);
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
    public static @NotNull Ellipse ellipse(@NotNull String key, double centerX, double centerZ, @NotNull Vector radius, double tilt) {
        return Ellipse.of(key, centerX, centerZ, radius, tilt);
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
    public static @NotNull Ellipse ellipse(@NotNull String key, @NotNull Point center, @NotNull Vector radius, double tilt) {
        return Ellipse.of(key, center, radius, tilt);
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
    public static @NotNull Icon icon(@NotNull String key, double x, double z, @NotNull String image) {
        return Icon.of(key, x, z, image);
    }

    /**
     * Create a new icon.
     *
     * @param key   identifying key
     * @param point icon location on map
     * @param image image key
     * @return a new icon
     */
    public static @NotNull Icon icon(@NotNull String key, @NotNull Point point, @NotNull String image) {
        return Icon.of(key, point, image);
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
    public static @NotNull Icon icon(@NotNull String key, double x, double z, @NotNull String image, double size) {
        return Icon.of(key, x, z, image, size, size);
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
    public static @NotNull Icon icon(@NotNull String key, double x, double z, @NotNull String image, double width, double height) {
        return Icon.of(key, x, z, image, width, height);
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
    public static @NotNull Icon icon(@NotNull String key, @NotNull Point point, @NotNull String image, double size) {
        return Icon.of(key, point, image, size, size);
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
    public static @NotNull Icon icon(@NotNull String key, @NotNull Point point, @NotNull String image, double width, double height) {
        return Icon.of(key, point, image, width, height);
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
    public static @NotNull Icon icon(@NotNull String key, @NotNull Point point, @NotNull String image, @Nullable Vector size) {
        return Icon.of(key, point, image, size);
    }

    /**
     * Create a new multi-polygon.
     *
     * @param key     identifying key
     * @param polygon polygon to add
     * @return a new multi-polygon
     */
    public static @NotNull MultiPolygon multiPolygon(@NotNull String key, @NotNull Polygon polygon) {
        return MultiPolygon.of(key, polygon);
    }

    /**
     * Create a new multi-polygon.
     *
     * @param key      identifying key
     * @param polygons polygons to add
     * @return a new multi-polygon
     */
    public static @NotNull MultiPolygon multiPolygon(@NotNull String key, @NotNull Polygon @NotNull ... polygons) {
        return MultiPolygon.of(key, polygons);
    }

    /**
     * Create a new multi-polygon.
     *
     * @param key      identifying key
     * @param polygons polygons to add
     * @return a new multi-polygon
     */
    public static @NotNull MultiPolygon multiPolygon(@NotNull String key, @NotNull List<@NotNull Polygon> polygons) {
        return MultiPolygon.of(key, polygons);
    }

    /**
     * Create a new multi-polyline.
     *
     * @param key      identifying key
     * @param polyline polyline to add
     * @return a new multi-polyline
     */
    public static @NotNull MultiPolyline multiPolyline(@NotNull String key, @NotNull Polyline polyline) {
        return MultiPolyline.of(key, polyline);
    }

    /**
     * Create a new multi-polyline.
     *
     * @param key       identifying key
     * @param polylines polylines to add
     * @return a new multi-polyline
     */
    public static @NotNull MultiPolyline multiPolyline(@NotNull String key, @NotNull Polyline @NotNull ... polylines) {
        return MultiPolyline.of(key, polylines);
    }

    /**
     * Create a new multi-polyline.
     *
     * @param key       identifying key
     * @param polylines polylines to add
     * @return a new multi-polyline
     */
    public static @NotNull MultiPolyline multiPolyline(@NotNull String key, @NotNull List<@NotNull Polyline> polylines) {
        return MultiPolyline.of(key, polylines);
    }

    /**
     * Create a new polygon.
     *
     * @param key      identifying key
     * @param polyline polyline to add
     * @return a new polygon
     */
    public static @NotNull Polygon polygon(@NotNull String key, @NotNull Polyline polyline) {
        return Polygon.of(key, polyline);
    }

    /**
     * Create a new polygon.
     *
     * @param key       identifying key
     * @param polylines polylines to add
     * @return a new polygon
     */
    public static @NotNull Polygon polygon(@NotNull String key, @NotNull Polyline @NotNull ... polylines) {
        return Polygon.of(key, polylines);
    }

    /**
     * Create a new polygon.
     *
     * @param key       identifying key
     * @param polylines polylines to add
     * @return a new polygon
     */
    public static @NotNull Polygon polygon(@NotNull String key, @NotNull List<@NotNull Polyline> polylines) {
        return Polygon.of(key, polylines);
    }

    /**
     * Create a new polyline.
     *
     * @param key   identifying key
     * @param point point to add
     * @return a new polyline
     */
    public static @NotNull Polyline polyline(@NotNull String key, @NotNull Point point) {
        return Polyline.of(key, point);
    }

    /**
     * Create a new polyline.
     *
     * @param key    identifying key
     * @param points points to add
     * @return a new polyline
     */
    public static @NotNull Polyline polyline(@NotNull String key, @NotNull Point @NotNull ... points) {
        return Polyline.of(key, points);
    }

    /**
     * Create a new polyline.
     *
     * @param key    identifying key
     * @param points points to add
     * @return a new polyline
     */
    public static @NotNull Polyline polyline(@NotNull String key, @NotNull List<@NotNull Point> points) {
        return Polyline.of(key, points);
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
    public static @NotNull Rectangle rectangle(@NotNull String key, double x1, double z1, double x2, double z2) {
        return Rectangle.of(key, x1, z1, x2, z2);
    }

    /**
     * Create a new rectangle.
     *
     * @param key    identifying key
     * @param point1 first point
     * @param point2 second point
     * @return a new rectangle
     */
    public static @NotNull Rectangle rectangle(@NotNull String key, @NotNull Point point1, @NotNull Point point2) {
        return Rectangle.of(key, point1, point2);
    }

    public static @NotNull Marker<?> fromJson(@NotNull JsonObject obj) {
        JsonElement el;
        String type = obj.get("type").getAsString();
        JsonObject data = obj.get("data").getAsJsonObject();
        Marker<?> marker = switch (type) {
            case "circ" -> Circle.fromJson(data);
            case "elli" -> Ellipse.fromJson(data);
            case "icon" -> Icon.fromJson(data);
            case "multipoly" -> MultiPolygon.fromJson(data);
            case "multiline" -> MultiPolyline.fromJson(data);
            case "poly" -> Polygon.fromJson(data);
            case "line" -> Polyline.fromJson(data);
            case "rect" -> Rectangle.fromJson(data);
            default -> throw new IllegalStateException("Marker type not found: " + type);
        };
        if ((el = obj.get("options")) != null && !(el instanceof JsonNull))
            marker.setOptions(Options.fromJson((JsonObject) el));
        return marker;
    }

    /**
     * Get the type identifier of this marker.
     * <p>
     * Used in the serialized json for the frontend.
     *
     * @return marker type
     */
    public @NotNull String getType() {
        return this.type;
    }

    /**
     * Get the map pane where the marker will be added.
     * <p>
     * Defaults to the overlay pane if null.
     *
     * @return map pane
     */
    public @Nullable String getPane() {
        return this.pane;
    }

    /**
     * Set the map pane where the marker will be added.
     * <p>
     * If the pane does not exist, it will be created the first time it is used.
     * <p>
     * Defaults to the overlay pane if null.
     *
     * @param pane map pane
     * @return this marker
     */
    @SuppressWarnings("unchecked")
    public @NotNull T setPane(@Nullable String pane) {
        this.pane = pane;
        return (@NotNull T) this;
    }

    /**
     * Get the options of this marker.
     * <p>
     * Null options represents "default" values. See wiki about defaults.
     *
     * @return marker options
     */
    public @Nullable Options getOptions() {
        return this.options;
    }

    /**
     * Set new options for this marker.
     * <p>
     * Null options represents "default" values. See wiki about defaults.
     *
     * @param options new options or null
     * @return this marker
     */
    @SuppressWarnings("unchecked")
    public @NotNull T setOptions(@Nullable Options options) {
        this.options = options;
        return (@NotNull T) this;
    }

    /**
     * Set new options for this marker.
     * <p>
     * Null options represents "default" values. See wiki about defaults.
     *
     * @param builder new options builder or null
     * @return this marker
     */
    public @NotNull T setOptions(Options.@Nullable Builder builder) {
        return setOptions(builder == null ? null : builder.build());
    }
}
