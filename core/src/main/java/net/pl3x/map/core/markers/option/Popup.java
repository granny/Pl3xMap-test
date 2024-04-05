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
import net.pl3x.map.core.markers.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Popup properties of a marker.
 */
public class Popup extends Option<@NotNull Popup> {
    public static final Point DEFAULT_OFFSET = Point.of(0, 7);
    public static final Point DEFAULT_AUTO_PAN_PADDING = Point.of(5, 5);

    private String content;
    private String pane;
    private Point offset;
    private Integer maxWidth;
    private Integer minWidth;
    private Integer maxHeight;
    private Boolean autoPan;
    private Point autoPanPaddingTopLeft;
    private Point autoPanPaddingBottomRight;
    private Point autoPanPadding;
    private Boolean keepInView;
    private Boolean closeButton;
    private Boolean autoClose;
    private Boolean closeOnEscapeKey;
    private Boolean closeOnClick;

    /**
     * Create a popup rule with default options.
     */
    public Popup() {
    }

    /**
     * Create a popup rule.
     *
     * @param content popup content
     */
    public Popup(@Nullable String content) {
        setContent(content);
    }

    public static @NotNull Popup fromJson(@NotNull JsonObject obj) {
        JsonElement el;
        Popup popup = new Popup();
        if ((el = obj.get("content")) != null && !(el instanceof JsonNull)) popup.setContent(el.getAsString());
        if ((el = obj.get("pane")) != null && !(el instanceof JsonNull)) popup.setPane(el.getAsString());
        if ((el = obj.get("offset")) != null && !(el instanceof JsonNull))
            popup.setOffset(Point.fromJson((JsonObject) el));
        fromJsonSize(obj, popup);
        fromJsonPanning(obj, popup);
        fromJsonClosing(obj, popup);
        return popup;
    }

    private static void fromJsonSize(@NotNull JsonObject obj, Popup popup) {
        JsonElement el;
        if ((el = obj.get("maxWidth")) != null && !(el instanceof JsonNull)) popup.setMaxWidth(el.getAsInt());
        if ((el = obj.get("minWidth")) != null && !(el instanceof JsonNull)) popup.setMinWidth(el.getAsInt());
        if ((el = obj.get("maxHeight")) != null && !(el instanceof JsonNull)) popup.setMaxHeight(el.getAsInt());
    }

    private static void fromJsonPanning(@NotNull JsonObject obj, Popup popup) {
        JsonElement el;
        if ((el = obj.get("autoPan")) != null && !(el instanceof JsonNull)) popup.setShouldAutoPan(el.getAsBoolean());
        if ((el = obj.get("autoPanPaddingTopLeft")) != null && !(el instanceof JsonNull))
            popup.setAutoPanPaddingTopLeft(Point.fromJson((JsonObject) el));
        if ((el = obj.get("autoPanPaddingBottomRight")) != null && !(el instanceof JsonNull))
            popup.setAutoPanPaddingBottomRight(Point.fromJson((JsonObject) el));
        if ((el = obj.get("autoPanPadding")) != null && !(el instanceof JsonNull))
            popup.setAutoPanPadding(Point.fromJson((JsonObject) el));
        if ((el = obj.get("keepInView")) != null && !(el instanceof JsonNull))
            popup.setShouldKeepInView(el.getAsBoolean());
    }

    private static void fromJsonClosing(@NotNull JsonObject obj, Popup popup) {
        JsonElement el;
        if ((el = obj.get("closeButton")) != null && !(el instanceof JsonNull)) popup.setCloseButton(el.getAsBoolean());
        if ((el = obj.get("autoClose")) != null && !(el instanceof JsonNull))
            popup.setShouldAutoClose(el.getAsBoolean());
        if ((el = obj.get("closeOnEscapeKey")) != null && !(el instanceof JsonNull))
            popup.setShouldCloseOnEscapeKey(el.getAsBoolean());
        if ((el = obj.get("closeOnClick")) != null && !(el instanceof JsonNull))
            popup.setShouldCloseOnClick(el.getAsBoolean());
    }

    /**
     * Get the content of this popup rule.
     * <p>
     * If null, the popup rule is effectively disabled.
     *
     * @return popup content
     */
    public @Nullable String getContent() {
        return this.content;
    }

    /**
     * Set the content for this popup rule.
     * <p>
     * HTML is valid here.
     * <p>
     * If null, the popup rule is effectively disabled.
     *
     * @param content popup content
     * @return this popup rule
     */
    public @NotNull Popup setContent(@Nullable String content) {
        this.content = content;
        return this;
    }

    /**
     * Get the map pane where the popup will be added.
     * <p>
     * Defaults to '<code>popupPane</code>' if null.
     *
     * @return map pane
     */
    public @Nullable String getPane() {
        return this.pane;
    }

    /**
     * Set the map pane where the popup will be added.
     * <p>
     * If the pane does not exist, it will be created the first time it is used.
     * <p>
     * Defaults to '<code>popupPane</code>' if null.
     *
     * @param pane map pane
     * @return this popup rule
     */
    public @NotNull Popup setPane(@Nullable String pane) {
        this.pane = pane;
        return this;
    }

    /**
     * Get offset of this popup rule.
     * <p>
     * Defaults to '<code>new {@link Point}(0, 7)</code>' if null.
     *
     * @return popup offset from marker point
     */
    public @Nullable Point getOffset() {
        return this.offset;
    }

    /**
     * Set offset of this popup rule from marker point
     * <p>
     * Defaults to '<code>new {@link Point}(0, 7)</code>' if null.
     *
     * @param offset popup offset
     * @return this popup rule
     */
    public @NotNull Popup setOffset(@Nullable Point offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Get the max width of the popup.
     * <p>
     * Defaults to '<code>300</code>' if null.
     *
     * @return max width
     */
    public @Nullable Integer getMaxWidth() {
        return this.maxWidth;
    }

    /**
     * Set the max width of the popup.
     * <p>
     * Defaults to '<code>300</code>' if null.
     *
     * @param maxWidth max width
     * @return this popup rule
     */
    public @NotNull Popup setMaxWidth(@Nullable Integer maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    /**
     * Get the min width of the popup.
     * <p>
     * Defaults to '<code>50</code>' if null.
     *
     * @return min width
     */
    public @Nullable Integer getMinWidth() {
        return this.minWidth;
    }

    /**
     * Set the min width of the popup.
     * <p>
     * Defaults to '<code>50</code>' if null.
     *
     * @param minWidth min width
     * @return this popup rule
     */
    public @NotNull Popup setMinWidth(@Nullable Integer minWidth) {
        this.minWidth = minWidth;
        return this;
    }

    /**
     * Get the max height of the popup.
     * <p>
     * If set, creates a scrollable container of the given
     * height inside a popup if its content exceeds it.
     *
     * @return max height
     */
    public @Nullable Integer getMaxHeight() {
        return this.maxHeight;
    }

    /**
     * Set the max height of the popup.
     * <p>
     * If set, creates a scrollable container of the given
     * height inside a popup if its content exceeds it.
     *
     * @param maxHeight max height
     * @return this popup rule
     */
    public @NotNull Popup setMaxHeight(@Nullable Integer maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    /**
     * Get whether the map should automatically pan to fit the opened popup.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @return true to auto pan
     */
    public @Nullable Boolean shouldAutoPan() {
        return this.autoPan;
    }

    /**
     * Set whether the map should automatically pan to fit the opened popup.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @param autoPan true to auto pan
     * @return this popup rule
     */
    public @NotNull Popup setShouldAutoPan(@Nullable Boolean autoPan) {
        this.autoPan = autoPan;
        return this;
    }

    /**
     * Get the margin between the popup and the top left corner of the map view
     * after auto panning was performed.
     * <p>
     * If set, overrides the top left values of {@link #getAutoPanPadding()}.
     *
     * @return top left corner padding margins
     */
    public @Nullable Point getAutoPanPaddingTopLeft() {
        return this.autoPanPaddingTopLeft;
    }

    /**
     * Set the margin between the popup and the top left corner of the map view
     * after auto panning was performed.
     * <p>
     * If set, overrides the top left values of {@link #getAutoPanPadding()}.
     *
     * @param autoPanPaddingTopLeft top left corner padding margins
     * @return this popup rule
     */
    public @NotNull Popup setAutoPanPaddingTopLeft(@Nullable Point autoPanPaddingTopLeft) {
        this.autoPanPaddingTopLeft = autoPanPaddingTopLeft;
        return this;
    }

    /**
     * Get the margin between the popup and the bottom right corner of the map view
     * after auto panning was performed.
     * <p>
     * If set, overrides the bottom right values of {@link #getAutoPanPadding()}.
     *
     * @return bottom right corner padding margins
     */
    public @Nullable Point getAutoPanPaddingBottomRight() {
        return this.autoPanPaddingBottomRight;
    }

    /**
     * Set the margin between the popup and the bottom right corner of the map view
     * after auto panning was performed.
     * <p>
     * If set, overrides the bottom right values of {@link #getAutoPanPadding()}.
     *
     * @param autoPanPaddingBottomRight bottom right corner padding margins
     * @return this popup rule
     */
    public @NotNull Popup setAutoPanPaddingBottomRight(@Nullable Point autoPanPaddingBottomRight) {
        this.autoPanPaddingBottomRight = autoPanPaddingBottomRight;
        return this;
    }

    /**
     * Get the margin between the popup and the map view after auto panning was performed.
     * <p>
     * This is the equivalent of the same values in both {@link #getAutoPanPaddingTopLeft()}
     * and {@link #getAutoPanPaddingBottomRight()}
     * <p>
     * Defaults to '<code>new {@link Point}(5, 5)</code>' if null.
     *
     * @return padding margins
     */
    public @Nullable Point getAutoPanPadding() {
        return this.autoPanPadding;
    }

    /**
     * Set the margin between the popup and the map view after auto panning was performed.
     * <p>
     * This is the equivalent of the same values in both {@link #getAutoPanPaddingTopLeft()}
     * and {@link #getAutoPanPaddingBottomRight()}
     * <p>
     * Defaults to '<code>new {@link Point}(5, 5)</code>' if null.
     *
     * @param autoPanPadding padding margins
     * @return this popup rule
     */
    public @NotNull Popup setAutoPanPadding(@Nullable Point autoPanPadding) {
        this.autoPanPadding = autoPanPadding;
        return this;
    }

    /**
     * Get whether the popup should stay in view.
     * <p>
     * If set to true it will prevent users from panning the popup off of the screen while it is open.
     * <p>
     * Defaults to '<code>false</code>' if null.
     *
     * @return true to keep popup in view
     */
    public @Nullable Boolean shouldKeepInView() {
        return this.keepInView;
    }

    /**
     * Set whether the popup should stay in view.
     * <p>
     * If set to true it will prevent users from panning the popup off of the screen while it is open.
     * <p>
     * Defaults to '<code>false</code>' if null.
     *
     * @param keepInView true to keep popup in view
     * @return this popup rule
     */
    public @NotNull Popup setShouldKeepInView(@Nullable Boolean keepInView) {
        this.keepInView = keepInView;
        return this;
    }

    /**
     * Get whether the popup has a close button.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @return true if popup has close button
     */
    public @Nullable Boolean hasCloseButton() {
        return this.closeButton;
    }

    /**
     * Set whether the popup has a close button.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @param closeButton true if popup has close button
     * @return this popup rule
     */
    public @NotNull Popup setCloseButton(@Nullable Boolean closeButton) {
        this.closeButton = closeButton;
        return this;
    }

    /**
     * Get whether the popup automatically closes when another popup is opened.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @return true if popup auto closes
     */
    public @Nullable Boolean shouldAutoClose() {
        return this.autoClose;
    }

    /**
     * Set whether the popup automatically closes when another popup is opened.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @param autoClose true if popup auto closes
     * @return this popup rule
     */
    public @NotNull Popup setShouldAutoClose(@Nullable Boolean autoClose) {
        this.autoClose = autoClose;
        return this;
    }

    /**
     * Get whether the popup closes with the escape key.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @return true to close with escape
     */
    public @Nullable Boolean shouldCloseOnEscapeKey() {
        return this.closeOnEscapeKey;
    }

    /**
     * Set whether the popup closes with the escape key.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @param closeOnEscapeKey true to close with escape
     * @return this popup rule
     */
    public @NotNull Popup setShouldCloseOnEscapeKey(@Nullable Boolean closeOnEscapeKey) {
        this.closeOnEscapeKey = closeOnEscapeKey;
        return this;
    }

    /**
     * Get whether the popup closes when the map is clicked.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @return true to close on map click
     */
    public @Nullable Boolean shouldCloseOnClick() {
        return this.closeOnClick;
    }

    /**
     * Set whether the popup closes when the map is clicked.
     * <p>
     * Defaults to '<code>true</code>' if null.
     *
     * @param closeOnClick true to close on map click
     * @return this popup rule
     */
    public @NotNull Popup setShouldCloseOnClick(@Nullable Boolean closeOnClick) {
        this.closeOnClick = closeOnClick;
        return this;
    }

    @Override
    public boolean isDefault() {
        return getContent() == null
                && (getPane() == null || getPane().equals("popupPane"))
                && (getOffset() == null || getOffset().equals(DEFAULT_OFFSET))
                && isSizeDefault()
                && isPanningDefault()
                && isClosingDefault();
    }

    private boolean isSizeDefault() {
        return (getMaxWidth() == null || getMaxWidth() == 300)
                && (getMinWidth() == null || getMinWidth() == 50)
                && getMaxHeight() == null;
    }

    private boolean isPanningDefault() {
        return (shouldAutoPan() == null || Boolean.TRUE.equals(shouldAutoPan()))
                && getAutoPanPaddingTopLeft() == null
                && getAutoPanPaddingBottomRight() == null
                && (getAutoPanPadding() == null || getAutoPanPadding().equals(DEFAULT_AUTO_PAN_PADDING))
                && (shouldKeepInView() == null || Boolean.FALSE.equals(shouldKeepInView()));
    }

    private boolean isClosingDefault() {
        return (hasCloseButton() == null || Boolean.TRUE.equals(hasCloseButton()))
                && (shouldAutoClose() == null || Boolean.TRUE.equals(shouldAutoClose()))
                && (shouldCloseOnEscapeKey() == null || Boolean.TRUE.equals(shouldCloseOnEscapeKey()))
                && (shouldCloseOnClick() == null || Boolean.TRUE.equals(shouldCloseOnClick()));
    }

    @Override
    public @NotNull JsonObject toJson() {
        JsonObjectWrapper wrapper = new JsonObjectWrapper();
        wrapper.addProperty("content", getContent());
        wrapper.addProperty("pane", getPane());
        wrapper.addProperty("offset", getOffset());
        wrapper.addProperty("maxWidth", getMaxWidth());
        wrapper.addProperty("minWidth", getMinWidth());
        wrapper.addProperty("maxHeight", getMaxHeight());
        wrapper.addProperty("autoPan", shouldAutoPan());
        wrapper.addProperty("autoPanPaddingTopLeft", getAutoPanPaddingTopLeft());
        wrapper.addProperty("autoPanPaddingBottomRight", getAutoPanPaddingBottomRight());
        wrapper.addProperty("autoPanPadding", getAutoPanPadding());
        wrapper.addProperty("keepInView", shouldKeepInView());
        wrapper.addProperty("closeButton", hasCloseButton());
        wrapper.addProperty("autoClose", shouldAutoClose());
        wrapper.addProperty("closeOnEscapeKey", shouldCloseOnEscapeKey());
        wrapper.addProperty("closeOnClick", shouldCloseOnClick());
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
        Popup other = (Popup) o;
        return Objects.equals(getContent(), other.getContent())
                && Objects.equals(getPane(), other.getPane())
                && Objects.equals(getOffset(), other.getOffset())
                && isSizeEqual(other)
                && isPanningEqual(other)
                && isClosingEqual(other);
    }

    private boolean isSizeEqual(@NotNull Popup other) {
        return Objects.equals(getMaxWidth(), other.getMaxWidth())
                && Objects.equals(getMinWidth(), other.getMinWidth())
                && Objects.equals(getMaxHeight(), other.getMaxHeight());
    }

    private boolean isPanningEqual(@NotNull Popup other) {
        return Objects.equals(shouldAutoPan(), other.shouldAutoPan())
                && Objects.equals(getAutoPanPaddingTopLeft(), other.getAutoPanPaddingTopLeft())
                && Objects.equals(getAutoPanPaddingBottomRight(), other.getAutoPanPaddingBottomRight())
                && Objects.equals(getAutoPanPadding(), other.getAutoPanPadding())
                && Objects.equals(shouldKeepInView(), other.shouldKeepInView());
    }

    private boolean isClosingEqual(@NotNull Popup other) {
        return Objects.equals(hasCloseButton(), other.hasCloseButton())
                && Objects.equals(shouldAutoClose(), other.shouldAutoClose())
                && Objects.equals(shouldCloseOnEscapeKey(), other.shouldCloseOnEscapeKey())
                && Objects.equals(shouldCloseOnClick(), other.shouldCloseOnClick());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContent(), getPane(), getOffset(), getMaxWidth(), getMinWidth(), getMaxHeight(),
                shouldAutoPan(), getAutoPanPaddingTopLeft(), getAutoPanPaddingBottomRight(), getAutoPanPadding(),
                shouldKeepInView(), hasCloseButton(), shouldAutoClose(), shouldCloseOnEscapeKey(), shouldCloseOnClick());
    }

    @Override
    public @NotNull String toString() {
        return "Popup{"
                + ",content=" + getContent()
                + ",pane=" + getPane()
                + ",offset=" + getOffset()
                + ",maxWidth=" + getMaxWidth()
                + ",minWidth=" + getMinWidth()
                + ",maxHeight=" + getMaxHeight()
                + ",autoPan=" + shouldAutoPan()
                + ",autoPanPaddingTopLeft=" + getAutoPanPaddingTopLeft()
                + ",autoPanPaddingBottomRight=" + getAutoPanPaddingBottomRight()
                + ",autoPanPadding=" + getAutoPanPadding()
                + ",keepInView=" + shouldKeepInView()
                + ",closeButton=" + hasCloseButton()
                + ",autoClose=" + shouldAutoClose()
                + ",closeOnEscapeKey=" + shouldCloseOnEscapeKey()
                + ",closeOnClick=" + shouldCloseOnClick()
                + "}";
    }
}
