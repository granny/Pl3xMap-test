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
 * Fill properties of a marker.
 */
@SuppressWarnings("unused")
public class Fill extends Option<@NotNull Fill> {
    private Boolean enabled;
    private Type type;
    private Integer color;

    /**
     * Create a fill rule with default options.
     */
    public Fill() {
    }

    /**
     * Create a fill rule.
     */
    public Fill(boolean enabled) {
        setEnabled(enabled);
    }

    /**
     * Create a fill rule.
     *
     * @param color argb color
     */
    public Fill(int color) {
        setColor(color);
    }

    public static @NotNull Fill fromJson(@NotNull JsonObject obj) {
        JsonElement el;
        Fill fill = new Fill();
        if ((el = obj.get("enabled")) != null && !(el instanceof JsonNull)) fill.setEnabled(el.getAsBoolean());
        if ((el = obj.get("type")) != null && !(el instanceof JsonNull)) fill.setType(Type.values()[el.getAsInt()]);
        if ((el = obj.get("color")) != null && !(el instanceof JsonNull)) fill.setColor(el.getAsInt());
        return fill;
    }

    /**
     * Whether to fill the path with color.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @return true if fill is enabled
     */
    public @Nullable Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set whether to fill the path with color.
     * <p>
     * Setting to false will disable filling on polygons or circles.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @param enabled whether fill is enabled
     * @return this fill rule
     */
    public @NotNull Fill setEnabled(@Nullable Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the fill type of this fill rule.
     * <p>
     * Defaults to '<code>{@link Type#EVENODD}</code>' if null.
     *
     * @return fill type
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/fill-rule">MDN fill-rule</a>
     */
    public @Nullable Type getType() {
        return this.type;
    }

    /**
     * Set a new fill type for this fill rule.
     * <p>
     * Defaults to '<code>{@link Type#EVENODD}</code>' if null.
     *
     * @param type new fill type
     * @return this fill rule
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/fill-rule">MDN fill-rule</a>
     */
    public @NotNull Fill setType(@Nullable Type type) {
        this.type = type;
        return this;
    }

    /**
     * Get the fill color of this fill rule.
     * <p>
     * Defaults to '<code>{@link Stroke#getColor()}</code>' if null.
     *
     * @return argb color
     */
    public @Nullable Integer getColor() {
        return this.color;
    }

    /**
     * Set a new color for this fill rule.
     * <p>
     * Defaults to '<code>{@link Stroke#getColor()}</code>' if null.
     *
     * @param color new argb color
     * @return this fill rule
     */
    public @NotNull Fill setColor(@Nullable Integer color) {
        this.color = color;
        return this;
    }

    @Override
    public boolean isDefault() {
        return (isEnabled() == null || Boolean.TRUE.equals(isEnabled())) &&
                (getType() == null || getType() == Type.EVENODD) &&
                getColor() == null;
    }

    @Override
    public @NotNull JsonObject toJson() {
        JsonObjectWrapper wrapper = new JsonObjectWrapper();
        wrapper.addProperty("enabled", isEnabled());
        wrapper.addProperty("type", getType());
        wrapper.addProperty("color", getColor());
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
        Fill other = (Fill) o;
        return Objects.equals(isEnabled(), other.isEnabled())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getColor(), other.getColor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEnabled(), getType(), getColor());
    }

    @Override
    public @NotNull String toString() {
        return "Fill{enabled=" + isEnabled() + ",type=" + getType() + ",color=" + getColor() + "}";
    }

    /**
     * Fill type.
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/fill-rule">MDN fill-rule</a>
     */
    public enum Type {
        /**
         * The value nonzero determines the "insideness" of a point
         * in the shape by drawing a ray from that point to infinity
         * in any direction, and then examining the places where a
         * segment of the shape crosses the ray. Starting with a
         * count of zero, add one each time a path segment crosses
         * the ray from left to right and subtract one each time a
         * path segment crosses the ray from right to left. After
         * counting the crossings, if the result is zero then the
         * point is outside the path. Otherwise, it is inside.
         *
         * @see <a href="https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/fill-rule">MDN fill-rule</a>
         */
        NONZERO,
        /**
         * The value evenodd determines the "insideness" of a point
         * in the shape by drawing a ray from that point to infinity
         * in any direction and counting the number of path segments
         * from the given shape that the ray crosses. If this number
         * is odd, the point is inside; if even, the point is outside.
         *
         * @see <a href="https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/fill-rule">MDN fill-rule</a>
         */
        EVENODD
    }
}
