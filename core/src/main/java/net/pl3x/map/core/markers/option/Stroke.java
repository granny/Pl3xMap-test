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
package net.pl3x.map.core.markers.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.Objects;
import net.pl3x.map.core.markers.JsonObjectWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stroke properties of a marker.
 */
public class Stroke extends Option<@NotNull Stroke> {
    private Boolean enabled;
    private Integer weight;
    private Integer color;
    private LineCapShape lineCapShape;
    private LineJoinShape lineJoinShape;
    private String dashPattern;
    private String dashOffset;

    /**
     * Create a stroke rule with default options.
     */
    public Stroke() {
    }

    /**
     * Create a stroke rule.
     */
    public Stroke(boolean enabled) {
        setEnabled(enabled);
    }

    /**
     * Create a stroke rule.
     *
     * @param weight stroke weight
     * @param color  argb color
     */
    public Stroke(int weight, int color) {
        setWeight(weight);
        setColor(color);
    }

    public static @NotNull Stroke fromJson(@NotNull JsonObject obj) {
        JsonElement el;
        Stroke stroke = new Stroke();
        if ((el = obj.get("enabled")) != null && !(el instanceof JsonNull)) stroke.setEnabled(el.getAsBoolean());
        if ((el = obj.get("weight")) != null && !(el instanceof JsonNull)) stroke.setWeight(el.getAsInt());
        if ((el = obj.get("color")) != null && !(el instanceof JsonNull)) stroke.setColor(el.getAsInt());
        if ((el = obj.get("lineCap")) != null && !(el instanceof JsonNull))
            stroke.setLineCapShape(LineCapShape.values()[el.getAsInt()]);
        if ((el = obj.get("lineJoin")) != null && !(el instanceof JsonNull))
            stroke.setLineJoinShape(LineJoinShape.values()[el.getAsInt()]);
        if ((el = obj.get("dashArray")) != null && !(el instanceof JsonNull)) stroke.setDashPattern(el.getAsString());
        if ((el = obj.get("dashOffset")) != null && !(el instanceof JsonNull)) stroke.setDashOffset(el.getAsString());
        return stroke;
    }

    /**
     * Whether to draw stroke along the path.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @return true if stroke is enabled
     */
    public @Nullable Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set whether to draw stroke along the path.
     * <p>
     * Setting to false will disable borders on polygons or circles.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @param enabled whether stroke is enabled
     * @return this stroke rule
     */
    public @NotNull Stroke setEnabled(@Nullable Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the weight of this stroke rule.
     * <p>
     * Defaults to '<code>3</code>' if null.
     *
     * @return stroke weight
     */
    public @Nullable Integer getWeight() {
        return this.weight;
    }

    /**
     * Set the weight for this stroke rule.
     * <p>
     * Defaults to '<code>3</code>' if null.
     *
     * @param weight new stroke weight
     * @return this stroke rule
     */
    public @NotNull Stroke setWeight(@Nullable Integer weight) {
        this.weight = weight;
        return this;
    }

    /**
     * Get the color of this stroke rule.
     * <p>
     * Defaults to '<code>#FF3388FF</code>' if null.
     *
     * @return argb color
     */
    public @Nullable Integer getColor() {
        return this.color;
    }

    /**
     * Set the color of this stroke rule.
     * <p>
     * Defaults to '<code>#FF3388FF</code>' if null.
     *
     * @param color argb color
     * @return this stroke rule
     */
    public @NotNull Stroke setColor(@Nullable Integer color) {
        this.color = color;
        return this;
    }

    /**
     * Get the shape to be used at the end of the stroke.
     * <p>
     * Defaults to '<code>{@link LineCapShape#ROUND}</code>' if null.
     *
     * @return line cap shape
     * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-linecap">MDN stroke-linecap</a>
     */
    public @Nullable LineCapShape getLineCapShape() {
        return this.lineCapShape;
    }

    /**
     * Set the shape to be used at the end of the stroke.
     * <p>
     * Defaults to '<code>{@link LineCapShape#ROUND}</code>' if null.
     *
     * @param lineCapShape line cap shape
     * @return this stroke rule
     * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-linecap">MDN stroke-linecap</a>
     */
    public @NotNull Stroke setLineCapShape(@Nullable LineCapShape lineCapShape) {
        this.lineCapShape = lineCapShape;
        return this;
    }

    /**
     * Get the shape to be used at the corners of the stroke.
     * <p>
     * Defaults to '<code>{@link LineJoinShape#ROUND}</code>' if null.
     *
     * @return line join shape
     * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-linejoin">MDN stroke-linejoin</a>
     */
    public @Nullable LineJoinShape getLineJoinShape() {
        return this.lineJoinShape;
    }

    /**
     * Set the shape to be used at the corners of the stroke.
     * <p>
     * Defaults to '<code>{@link LineJoinShape#ROUND}</code>' if null.
     *
     * @param lineJoinShape line join shape
     * @return this stroke rule
     * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-linejoin">MDN stroke-linejoin</a>
     */
    public @NotNull Stroke setLineJoinShape(@Nullable LineJoinShape lineJoinShape) {
        this.lineJoinShape = lineJoinShape;
        return this;
    }

    /**
     * Get the stroke dash pattern.
     * <p>
     * Note: Doesn't work in some old browsers.
     *
     * @return stroke dash pattern
     * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-dasharray">MDN stroke-dasharray</a>
     */
    public @Nullable String getDashPattern() {
        return this.dashPattern;
    }

    /**
     * Set the stroke dash pattern.
     * <p>
     * Note: Doesn't work in some old browsers.
     *
     * @param dashPattern dash pattern
     * @return this stroke rule
     * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-dasharray">MDN stroke-dasharray</a>
     */
    public @NotNull Stroke setDashPattern(@Nullable String dashPattern) {
        this.dashPattern = dashPattern;
        return this;
    }

    /**
     * Get the distance into the dash pattern to start the dash.
     * <p>
     * Note: Doesn't work in some old browsers.
     *
     * @return dash offset
     * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-dashoffset">MDN stroke-dashoffset</a>
     * @see <a href="https://developer.mozilla.org/docs/Web/API/CanvasRenderingContext2D/setLineDash#Browser_compatibility">Browser compatibility</a>
     */
    public @Nullable String getDashOffset() {
        return this.dashOffset;
    }

    /**
     * Set the distance into the dash pattern to start the dash.
     * <p>
     * Note: Doesn't work in some old browsers.
     *
     * @param dashOffset dash offset
     * @return this stroke rule
     * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-dashoffset">MDN stroke-dashoffset</a>
     * @see <a href="https://developer.mozilla.org/docs/Web/API/CanvasRenderingContext2D/setLineDash#Browser_compatibility">Browser compatibility</a>
     */
    public @NotNull Stroke setDashOffset(@Nullable String dashOffset) {
        this.dashOffset = dashOffset;
        return this;
    }

    @Override
    public boolean isDefault() {
        return (isEnabled() == null || Boolean.TRUE.equals(isEnabled())) &&
                (getWeight() == null || getWeight() == 3) &&
                (getColor() == null || getColor() == 0xFF3388FF) &&
                (getLineCapShape() == null || getLineCapShape() == LineCapShape.ROUND) &&
                (getLineJoinShape() == null || getLineJoinShape() == LineJoinShape.ROUND) &&
                getDashPattern() == null &&
                getDashOffset() == null;
    }

    @Override
    public @NotNull JsonObject toJson() {
        JsonObjectWrapper wrapper = new JsonObjectWrapper();
        wrapper.addProperty("enabled", isEnabled());
        wrapper.addProperty("weight", getWeight());
        wrapper.addProperty("color", getColor());
        wrapper.addProperty("lineCap", getLineCapShape());
        wrapper.addProperty("lineJoin", getLineJoinShape());
        wrapper.addProperty("dashArray", getDashPattern());
        wrapper.addProperty("dashOffset", getDashOffset());
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
        Stroke other = (Stroke) o;
        return Objects.equals(isEnabled(), other.isEnabled())
                && Objects.equals(getWeight(), other.getWeight())
                && Objects.equals(getColor(), other.getColor())
                && Objects.equals(getLineCapShape(), other.getLineCapShape())
                && Objects.equals(getLineJoinShape(), other.getLineJoinShape())
                && Objects.equals(getDashPattern(), other.getDashPattern())
                && Objects.equals(getDashOffset(), other.getDashOffset());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEnabled(), getWeight(), getColor(), getLineCapShape(), getLineJoinShape(), getDashPattern(), getDashOffset());
    }

    @Override
    public @NotNull String toString() {
        return "Stroke{"
                + "enabled=" + isEnabled()
                + ",weight=" + getWeight()
                + ",color=" + getColor()
                + ",lineCapShape=" + getLineCapShape()
                + ",lineJoinShape=" + getLineJoinShape()
                + ",dashPattern=" + getDashPattern()
                + ",dashOffset=" + getDashOffset()
                + "}";
    }

    /**
     * Represents the shape to be used at the corners of the stroke.
     *
     * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-linecap">MDN stroke-linecap</a>
     */
    public enum LineCapShape {
        /**
         * The butt value indicates that the stroke for each subpath does
         * not extend beyond its two endpoints. On a zero length subpath,
         * the path will not be rendered at all.
         */
        BUTT,
        /**
         * The round value indicates that at the end of each subpath the
         * stroke will be extended by a half circle with a diameter equal
         * to the stroke width. On a zero length subpath, the stroke
         * consists of a full circle centered at the subpath's point.
         */
        ROUND,
        /**
         * The square value indicates that at the end of each subpath the
         * stroke will be extended by a rectangle with a width equal to
         * half the width of the stroke and a height equal to the width
         * of the stroke. On a zero length subpath, the stroke consists
         * of a square with its width equal to the stroke width, centered
         * at the subpath's point.
         */
        SQUARE
    }

    /**
     * Represents the shape to be used at the corners of the stroke.
     *
     * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-linejoin">MDN stroke-linejoin</a>
     */
    public enum LineJoinShape {
        /**
         * The miter value indicates that a sharp corner is to be used
         * to join path segments. The corner is formed by extending the
         * outer edges of the stroke at the tangents of the path
         * segments until they intersect.
         */
        MITER,
        /**
         * The round value indicates that a round corner is to be used
         * to join path segments.
         */
        ROUND,
        /**
         * The bevel value indicates that a bevelled corner is to be
         * used to join path segments.
         */
        BEVEL
    }
}
