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
import net.pl3x.map.core.markers.JsonSerializable;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.marker.Marker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link Marker}'s options.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Options implements JsonSerializable {
    private Stroke stroke;
    private Fill fill;
    private Tooltip tooltip;
    private Popup popup;

    /**
     * Create empty marker options.
     */
    public Options() {
    }

    /**
     * Create marker options.
     *
     * @param stroke  stroke rules
     * @param fill    fill rules
     * @param tooltip tooltip rules
     */
    public Options(@Nullable Stroke stroke, @Nullable Fill fill, @Nullable Tooltip tooltip, @Nullable Popup popup) {
        setStroke(stroke);
        setFill(fill);
        setTooltip(tooltip);
        setPopup(popup);
    }

    /**
     * Create a new {@link Builder}.
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    static <T extends @NotNull Option<@NotNull T>> @Nullable T parse(@Nullable T option) {
        return option == null || option.isDefault() ? null : option;
    }

    public static @NotNull Options fromJson(@NotNull JsonObject obj) {
        JsonElement el;
        Options options = new Options();
        if ((el = obj.get("stroke")) != null && !(el instanceof JsonNull))
            options.setStroke(Stroke.fromJson((JsonObject) el));
        if ((el = obj.get("fill")) != null && !(el instanceof JsonNull))
            options.setFill(Fill.fromJson((JsonObject) el));
        if ((el = obj.get("tooltip")) != null && !(el instanceof JsonNull))
            options.setTooltip(Tooltip.fromJson((JsonObject) el));
        if ((el = obj.get("popup")) != null && !(el instanceof JsonNull))
            options.setPopup(Popup.fromJson((JsonObject) el));
        return options;
    }

    /**
     * Get stroke rules.
     *
     * @return stroke rules
     */
    public @Nullable Stroke getStroke() {
        return this.stroke;
    }

    /**
     * Set new stroke rules.
     *
     * @param stroke new stroke rules.
     * @return this marker options
     */
    public @NotNull Options setStroke(@Nullable Stroke stroke) {
        this.stroke = parse(stroke);
        return this;
    }

    /**
     * Get fill rules.
     *
     * @return fill rules
     */
    public @Nullable Fill getFill() {
        return this.fill;
    }

    /**
     * Set new fill rules.
     *
     * @param fill new fill rules
     * @return this marker options
     */
    public @NotNull Options setFill(@Nullable Fill fill) {
        this.fill = parse(fill);
        return this;
    }

    /**
     * Get tooltip rules.
     *
     * @return tooltip rules
     */
    public @Nullable Tooltip getTooltip() {
        return this.tooltip;
    }

    /**
     * Set new tooltip rules.
     *
     * @param tooltip new tooltip rules
     * @return this marker options
     */
    public @NotNull Options setTooltip(@Nullable Tooltip tooltip) {
        this.tooltip = parse(tooltip);
        return this;
    }

    /**
     * Get popup rules.
     *
     * @return popup rules
     */
    public @Nullable Popup getPopup() {
        return this.popup;
    }

    /**
     * Set new popup rules.
     *
     * @param popup new popup rules
     * @return this marker options
     */
    public @NotNull Options setPopup(@Nullable Popup popup) {
        this.popup = parse(popup);
        return this;
    }

    /**
     * Create a new {@link Builder} from this {@link Options} instance
     *
     * @return new builder
     */
    public @NotNull Builder asBuilder() {
        Options.Builder builder = new Builder();
        if (getStroke() != null) builder.stroke(Stroke.fromJson(getStroke().toJson()));
        if (getFill() != null) builder.fill(Fill.fromJson(getFill().toJson()));
        if (getTooltip() != null) builder.tooltip(Tooltip.fromJson(getTooltip().toJson()));
        if (getPopup() != null) builder.popup(Popup.fromJson(getPopup().toJson()));
        return builder;
    }

    @Override
    public @NotNull JsonObject toJson() {
        JsonObjectWrapper wrapper = new JsonObjectWrapper();
        wrapper.addProperty("stroke", getStroke());
        wrapper.addProperty("fill", getFill());
        wrapper.addProperty("tooltip", getTooltip());
        wrapper.addProperty("popup", getPopup());
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
        Options other = (Options) o;
        return Objects.equals(getStroke(), other.getStroke())
                && Objects.equals(getFill(), other.getFill())
                && Objects.equals(getTooltip(), other.getTooltip())
                && Objects.equals(getPopup(), other.getPopup());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStroke(), getFill(), getTooltip(), getPopup());
    }

    @Override
    public @NotNull String toString() {
        return "Options{fill=" + getFill() + ",stroke=" + getStroke() + ",tooltip=" + getTooltip() + ",popup=" + getPopup() + "}";
    }

    /**
     * Builder for {@link Options}.
     */
    public static class Builder {
        private Stroke stroke = null;
        private Fill fill = null;
        private Tooltip tooltip = null;
        private Popup popup = null;

        /**
         * Create a new builder for {@link Options}.
         */
        public Builder() {
        }

        /**
         * Set the stroke properties.
         *
         * @param stroke stroke properties
         * @return this options builder
         */
        public @NotNull Builder stroke(@Nullable Stroke stroke) {
            this.stroke = parse(stroke);
            return this;
        }

        /**
         * Set whether to draw stroke along the path.
         * <p>
         * Setting to false will disable borders on polygons or circles.
         * <p>
         * Defaults to '<code>true</code>' if null.
         *
         * @param enabled whether stroke is enabled
         * @return this options builder
         */
        public @NotNull Builder stroke(@Nullable Boolean enabled) {
            if (this.stroke == null) {
                this.stroke = new Stroke();
            }
            if (this.stroke.setEnabled(enabled).isDefault()) {
                this.stroke = null;
            }
            return this;
        }

        /**
         * Set the weight for this stroke rule.
         * <p>
         * Defaults to '<code>3</code>' if null.
         *
         * @param weight new stroke weight
         * @return this options builder
         */
        public @NotNull Builder strokeWeight(@Nullable Integer weight) {
            if (this.stroke == null) {
                this.stroke = new Stroke();
            }
            if (this.stroke.setWeight(weight).isDefault()) {
                this.stroke = null;
            }
            return this;
        }

        /**
         * Set the color of this stroke rule.
         * <p>
         * Defaults to '<code>#FF3388FF</code>' if null.
         *
         * @param color argb color
         * @return this options builder
         */
        public @NotNull Builder strokeColor(@Nullable Integer color) {
            if (this.stroke == null) {
                this.stroke = new Stroke();
            }
            if (this.stroke.setColor(color).isDefault()) {
                this.stroke = null;
            }
            return this;
        }

        /**
         * Set the shape to be used at the end of the stroke.
         * <p>
         * Defaults to '<code>{@link Stroke.LineCapShape#ROUND}</code>' if null.
         *
         * @param lineCapShape line cap shape
         * @return this options builder
         * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-linecap">MDN stroke-linecap</a>
         */
        public @NotNull Builder strokeLineCapShape(Stroke.@Nullable LineCapShape lineCapShape) {
            if (this.stroke == null) {
                this.stroke = new Stroke();
            }
            if (this.stroke.setLineCapShape(lineCapShape).isDefault()) {
                this.stroke = null;
            }
            return this;
        }

        /**
         * Set the shape to be used at the corners of the stroke.
         * <p>
         * Defaults to '<code>{@link Stroke.LineJoinShape#ROUND}</code>' if null.
         *
         * @param lineJoinShape line join shape
         * @return this options builder
         * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-linejoin">MDN stroke-linejoin</a>
         */
        public @NotNull Builder strokeLineJoinShape(Stroke.@Nullable LineJoinShape lineJoinShape) {
            if (this.stroke == null) {
                this.stroke = new Stroke();
            }
            if (this.stroke.setLineJoinShape(lineJoinShape).isDefault()) {
                this.stroke = null;
            }
            return this;
        }

        /**
         * Set the stroke dash pattern.
         * <p>
         * Note: Doesn't work in some old browsers.
         *
         * @param dashPattern dash pattern
         * @return this options builder
         * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-dasharray">MDN stroke-dasharray</a>
         */
        public @NotNull Builder strokeDashPattern(@Nullable String dashPattern) {
            if (this.stroke == null) {
                this.stroke = new Stroke();
            }
            if (this.stroke.setDashPattern(dashPattern).isDefault()) {
                this.stroke = null;
            }
            return this;
        }

        /**
         * Set the distance into the dash pattern to start the dash.
         * <p>
         * Note: Doesn't work in some old browsers.
         *
         * @param dashOffset dash offset
         * @return this options builder
         * @see <a href="https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-dashoffset">MDN stroke-dashoffset</a>
         * @see <a href="https://developer.mozilla.org/docs/Web/API/CanvasRenderingContext2D/setLineDash#Browser_compatibility">Browser compatibility</a>
         */
        public @NotNull Builder strokeDashOffset(@Nullable String dashOffset) {
            if (this.stroke == null) {
                this.stroke = new Stroke();
            }
            if (this.stroke.setDashOffset(dashOffset).isDefault()) {
                this.stroke = null;
            }
            return this;
        }

        /**
         * Set fill properties.
         *
         * @param fill fill properties
         * @return this options builder
         */
        public @NotNull Builder fill(@Nullable Fill fill) {
            this.fill = parse(fill);
            return this;
        }

        /**
         * Set whether to fill the path with color.
         * <p>
         * Setting to false will disable filling on polygons or circles.
         * <p>
         * Defaults to '<code>true</code>' if null.
         *
         * @param enabled whether fill is enabled
         * @return this options builder
         */
        public @NotNull Builder fill(@Nullable Boolean enabled) {
            if (this.fill == null) {
                this.fill = new Fill();
            }
            if (this.fill.setEnabled(enabled).isDefault()) {
                this.fill = null;
            }
            return this;
        }

        /**
         * Set a new fill type for this fill rule.
         * <p>
         * Defaults to '<code>{@link Fill.Type#EVENODD}</code>' if null.
         *
         * @param type new fill type
         * @return this options builder
         * @see <a href="https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/fill-rule">MDN fill-rule</a>
         */
        public @NotNull Builder fillType(Fill.@Nullable Type type) {
            if (this.fill == null) {
                this.fill = new Fill();
            }
            if (this.fill.setType(type).isDefault()) {
                this.fill = null;
            }
            return this;
        }

        /**
         * Set a new color for this fill rule.
         * <p>
         * Defaults to '<code>{@link Stroke#getColor()}</code>' if null.
         *
         * @param color new argb color
         * @return this options builder
         */
        public @NotNull Builder fillColor(@Nullable Integer color) {
            if (this.fill == null) {
                this.fill = new Fill();
            }
            if (this.fill.setColor(color).isDefault()) {
                this.fill = null;
            }
            return this;
        }

        /**
         * Set tooltip properties.
         *
         * @param tooltip tooltip properties
         * @return this options builder
         */
        public @NotNull Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = parse(tooltip);
            return this;
        }

        /**
         * Set the content for this tooltip rule.
         * <p>
         * HTML is valid here.
         * <p>
         * If null, the tooltip rule is effectively disabled.
         *
         * @param content tooltip content
         * @return this options builder
         */
        public @NotNull Builder tooltipContent(@Nullable String content) {
            if (this.tooltip == null) {
                this.tooltip = new Tooltip();
            }
            if (this.tooltip.setContent(content).isDefault()) {
                this.tooltip = null;
            }
            return this;
        }

        /**
         * Set the map pane where the tooltip will be added.
         * <p>
         * Defaults to '<code>tooltipPane</code>' if null.
         *
         * @param pane map pane
         * @return this options builder
         */
        public @NotNull Builder tooltipPane(@Nullable String pane) {
            if (this.tooltip == null) {
                this.tooltip = new Tooltip();
            }
            if (this.tooltip.setPane(pane).isDefault()) {
                this.tooltip = null;
            }
            return this;
        }

        /**
         * Set offset of this tooltip rule from marker point.
         * <p>
         * Defaults to '<code>{@link Point#ZERO}</code>' if null.
         *
         * @param offset tooltip offset
         * @return this options builder
         */
        public @NotNull Builder tooltipOffset(@Nullable Point offset) {
            if (this.tooltip == null) {
                this.tooltip = new Tooltip();
            }
            if (this.tooltip.setOffset(offset).isDefault()) {
                this.tooltip = null;
            }
            return this;
        }

        /**
         * Set the direction where to open the tooltip.
         * <p>
         * Defaults to '<code>{@link Tooltip.Direction#AUTO}</code>' if null.
         *
         * @param direction opening direction
         * @return this options builder
         */
        public @NotNull Builder tooltipDirection(Tooltip.@Nullable Direction direction) {
            if (this.tooltip == null) {
                this.tooltip = new Tooltip();
            }
            if (this.tooltip.setDirection(direction).isDefault()) {
                this.tooltip = null;
            }
            return this;
        }

        /**
         * Set whether to open the tooltip permanently or only on mouseover
         * <p>
         * Defaults to '<code>false</code>' if null.
         *
         * @param permanent opened permanently
         * @return this options builder
         */
        public @NotNull Builder tooltipPermanent(@Nullable Boolean permanent) {
            if (this.tooltip == null) {
                this.tooltip = new Tooltip();
            }
            if (this.tooltip.setPermanent(permanent).isDefault()) {
                this.tooltip = null;
            }
            return this;
        }

        /**
         * Set whether the tooltip is sticky or not.
         * <p>
         * A sticky tooltip will stick to and follow the mouse instead of the anchor.
         * <p>
         * Defaults to '<code>false</code>' if null.
         *
         * @param sticky sticky state
         * @return this options builder
         */
        public @NotNull Builder tooltipSticky(@Nullable Boolean sticky) {
            if (this.tooltip == null) {
                this.tooltip = new Tooltip();
            }
            if (this.tooltip.setSticky(sticky).isDefault()) {
                this.tooltip = null;
            }
            return this;
        }

        /**
         * Set the tooltip opacity percent.
         * <p>
         * Defaults to '<code>0.9D</code>' if null.
         *
         * @param opacity tooltip opacity
         * @return this options builder
         */
        public @NotNull Builder tooltipOpacity(@Nullable Double opacity) {
            if (this.tooltip == null) {
                this.tooltip = new Tooltip();
            }
            if (this.tooltip.setOpacity(opacity).isDefault()) {
                this.tooltip = null;
            }
            return this;
        }

        /**
         * Set popup properties.
         *
         * @param popup popup properties
         * @return this options builder
         */
        public @NotNull Builder popup(@Nullable Popup popup) {
            this.popup = parse(popup);
            return this;
        }

        /**
         * Set the content for this popup rule.
         * <p>
         * HTML is valid here.
         * <p>
         * If null, the popup rule is effectively disabled.
         *
         * @param content popup content
         * @return this options builder
         */
        public @NotNull Builder popupContent(@Nullable String content) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setContent(content).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set the map pane where the popup will be added.
         * <p>
         * Defaults to '<code>popupPane</code>' if null.
         *
         * @param pane map pane
         * @return this options builder
         */
        public @NotNull Builder popupPane(@Nullable String pane) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setPane(pane).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set offset of this popup rule from marker point
         * <p>
         * Defaults to '<code>new {@link Point}(0, 7)</code>' if null.
         *
         * @param offset popup offset
         * @return this options builder
         */
        public @NotNull Builder popupOffset(@Nullable Point offset) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setOffset(offset).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set the max width of the popup.
         * <p>
         * Defaults to '<code>300</code>' if null.
         *
         * @param maxWidth max width
         * @return this options builder
         */
        public @NotNull Builder popupMaxWidth(@Nullable Integer maxWidth) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setMaxWidth(maxWidth).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set the min width of the popup.
         * <p>
         * Defaults to '<code>50</code>' if null.
         *
         * @param minWidth min width
         * @return this options builder
         */
        public @NotNull Builder popupMinWidth(@Nullable Integer minWidth) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setMinWidth(minWidth).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set the max height of the popup.
         * <p>
         * If set, creates a scrollable container of the given
         * height inside a popup if its content exceeds it.
         *
         * @param maxHeight max height
         * @return this options builder
         */
        public @NotNull Builder popupMaxHeight(@Nullable Integer maxHeight) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setMaxHeight(maxHeight).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set whether the map should automatically pan to fit the opened popup.
         * <p>
         * Defaults to '<code>true</code>' if null.
         *
         * @param autoPan true to auto pan
         * @return this options builder
         */
        public @NotNull Builder popupShouldAutoPan(@Nullable Boolean autoPan) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setShouldAutoPan(autoPan).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set the margin between the popup and the top left corner of the map view
         * after auto panning was performed.
         * <p>
         * If set, overrides the top left values of {@link Popup#getAutoPanPadding()}.
         *
         * @param autoPanPaddingTopLeft top left corner padding margins
         * @return this options builder
         */
        public @NotNull Builder popupAutoPanPaddingTopLeft(@Nullable Point autoPanPaddingTopLeft) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setAutoPanPaddingTopLeft(autoPanPaddingTopLeft).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set the margin between the popup and the bottom right corner of the map view
         * after auto panning was performed.
         * <p>
         * If set, overrides the bottom right values of {@link Popup#getAutoPanPadding()}.
         *
         * @param autoPanPaddingBottomRight bottom right corner padding margins
         * @return this options builder
         */
        public @NotNull Builder popupAutoPanPaddingBottomRight(@Nullable Point autoPanPaddingBottomRight) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setAutoPanPaddingBottomRight(autoPanPaddingBottomRight).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set the margin between the popup and the map view after auto panning was performed.
         * <p>
         * This is the equivalent of the same values in both {@link Popup#getAutoPanPaddingTopLeft()}
         * and {@link Popup#getAutoPanPaddingBottomRight()}
         * <p>
         * Defaults to '<code>new {@link Point}(5, 5)</code>' if null.
         *
         * @param autoPanPadding padding margins
         * @return this options builder
         */
        public @NotNull Builder popupAutoPanPadding(@Nullable Point autoPanPadding) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setAutoPanPadding(autoPanPadding).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set whether the popup should stay in view.
         * <p>
         * If set to true it will prevent users from panning the popup off of the screen while it is open.
         * <p>
         * Defaults to '<code>false</code>' if null.
         *
         * @param keepInView true to keep popup in view
         * @return this options builder
         */
        public @NotNull Builder popupShouldKeepInView(@Nullable Boolean keepInView) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setShouldKeepInView(keepInView).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set whether the popup has a close button.
         * <p>
         * Defaults to '<code>true</code>' if null.
         *
         * @param closeButton true if popup has close button
         * @return this options builder
         */
        public @NotNull Builder popupCloseButton(@Nullable Boolean closeButton) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setCloseButton(closeButton).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set whether the popup automatically closes when another popup is opened.
         * <p>
         * Defaults to '<code>true</code>' if null.
         *
         * @param autoClose true if popup auto closes
         * @return this options builder
         */
        public @NotNull Builder popupShouldAutoClose(@Nullable Boolean autoClose) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setShouldAutoClose(autoClose).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set whether the popup closes with the escape key.
         * <p>
         * Defaults to '<code>true</code>' if null.
         *
         * @param closeOnEscapeKey true to close with escape
         * @return this options builder
         */
        public @NotNull Builder popupShouldCloseOnEscapeKey(@Nullable Boolean closeOnEscapeKey) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setShouldCloseOnEscapeKey(closeOnEscapeKey).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Set whether the popup closes when the map is clicked.
         * <p>
         * Defaults to '<code>true</code>' if null.
         *
         * @param closeOnClick true to close on map click
         * @return this options builder
         */
        public @NotNull Builder popupShouldCloseOnClick(@Nullable Boolean closeOnClick) {
            if (this.popup == null) {
                this.popup = new Popup();
            }
            if (this.popup.setShouldCloseOnClick(closeOnClick).isDefault()) {
                this.popup = null;
            }
            return this;
        }

        /**
         * Create a new {@link Options} instance from the current state of this {@link Builder}.
         *
         * @return new options
         */
        public @NotNull Options build() {
            return new Options(this.stroke, this.fill, this.tooltip, this.popup);
        }
    }
}
