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

@SuppressWarnings("CanBeFinal")
public final class SpawnLayerConfig extends AbstractConfig {
    private static final SpawnLayerConfig CONFIG = new SpawnLayerConfig();
    @Key("settings.enabled")
    @Comment("""
            Show spawn icon on the map.""")
    public static boolean ENABLED = true;
    @Key("settings.layer.update-interval")
    @Comment("""
            How often (in seconds) to update the marker.
            Setting to 0 is the same as setting it to 1.""")
    public static int UPDATE_INTERVAL = 30;
    @Key("settings.layer.show-controls")
    @Comment("""
            Whether the spawn layer control shows up in the layers list or not.""")
    public static boolean SHOW_CONTROLS = true;
    @Key("settings.layer.default-hidden")
    @Comment("""
            Whether the spawn layer should be hidden (toggled off) by default.""")
    public static boolean DEFAULT_HIDDEN = false;
    @Key("settings.layer.priority")
    @Comment("""
            Priority order spawn layer shows up in the layers list.
            (lower values = higher in the list)""")
    public static int PRIORITY = 10;
    @Key("settings.layer.z-index")
    @Comment("""
            Z-Index order spawn layer shows up in the map.
            (higher values are drawn on top of lower values)""")
    public static int Z_INDEX = 500;
    @Key("settings.icon")
    @Comment("""
            The spawn icon.
            Icon must be in the web/images/icon/ directory.""")
    public static String ICON = "spawn";

    public static void reload() {
        CONFIG.reload(Pl3xMap.api().getMainDir().resolve("layers/spawn.yml"), SpawnLayerConfig.class);
    }
}
