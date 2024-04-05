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
package net.pl3x.map.core.configuration;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.option.Stroke;

@SuppressWarnings("CanBeFinal")
public final class WorldBorderLayerConfig extends AbstractConfig {
    private static final WorldBorderLayerConfig CONFIG = new WorldBorderLayerConfig();
    @Key("settings.enabled")
    @Comment("""
            Shows vanilla world border on the map.""")
    public static boolean ENABLED = true;
    @Key("settings.layer.update-interval")
    @Comment("""
            How often (in seconds) to update the marker.
            Setting to 0 is the same as setting it to 1.""")
    public static int UPDATE_INTERVAL = 30;
    @Key("settings.layer.show-controls")
    @Comment("""
            Whether the vanilla world border layer control shows up in the layers list or not.""")
    public static boolean SHOW_CONTROLS = true;
    @Key("settings.layer.default-hidden")
    @Comment("""
            Whether the vanilla world border layer should be hidden (toggled off) by default.""")
    public static boolean DEFAULT_HIDDEN = false;
    @Key("settings.layer.priority")
    @Comment("""
            Priority order vanilla world border layer shows up in the layers list.
            (lower values = higher in the list)""")
    public static int PRIORITY = 30;
    @Key("settings.layer.z-index")
    @Comment("""
            Z-Index order vanilla world border layer shows up in the map.
            (higher values are drawn on top of lower values)""")
    public static int Z_INDEX = 500;
    @Key("settings.style.stroke.color")
    @Comment("""
            Stroke color (#AARRGGBB)""")
    public static String STROKE_COLOR = "#FFFF0000";
    @Key("settings.style.stroke.weight")
    @Comment("""
            Stroke weight (thickness)""")
    public static int STROKE_WEIGHT = 3;
    @Key("settings.style.stroke.dash-offset")
    @Comment("""
            The offset on the rendering of the associated dash pattern.""")
    // https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-dashoffset
    public static String STROKE_DASH_OFFSET = null;
    @Key("settings.style.stroke.dash-pattern")
    @Comment("""
            The pattern of dashes and gaps used to paint the outline of the shape.""")
    // https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-dasharray
    public static String STROKE_DASH_PATTERN = null;
    @Key("settings.style.stroke.line-cap-shape")
    @Comment("""
            The shape to be used at the end of open sub-paths when they are stroked.""")
    // https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-linecap
    public static Stroke.LineCapShape STROKE_LINE_CAP_SHAPE = null;
    @Key("settings.style.stroke.line-join-shape")
    @Comment("""
            The shape to be used at the corners of paths when they are stroked.""")
    // https://developer.mozilla.org/docs/Web/SVG/Attribute/stroke-linejoin
    public static Stroke.LineJoinShape STROKE_LINE_JOIN_SHAPE = null;

    public static void reload() {
        CONFIG.reload(Pl3xMap.api().getMainDir().resolve("layers/worldborder.yml"), WorldBorderLayerConfig.class);
    }
}
