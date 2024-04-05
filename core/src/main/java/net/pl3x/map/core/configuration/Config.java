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

import java.nio.file.Path;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.util.FileUtil;
import org.simpleyaml.configuration.file.YamlFile;

@SuppressWarnings("CanBeFinal")
public final class Config extends AbstractConfig {
    @Key("settings.debug-mode")
    @Comment("""
            Extra logger/console output. (can be spammy)""")
    public static boolean DEBUG_MODE = false;

    @Key("settings.language-file")
    @Comment("""
            The language file to use from the locale folder.""")
    public static String LANGUAGE_FILE = "lang-en.yml";

    @Key("settings.web-address")
    @Comment("""
            Set the web address players use to connect to your map. This
            is only used for the client mod to know where to connect.""")
    public static String WEB_ADDRESS = "http://localhost:8080";

    @Key("settings.web-directory.path")
    @Comment("""
            The directory that houses the website and world tiles.
            This is a relative path from Pl3xMap's plugin directory,
            unless it starts with / in which case it will be treated
            as an absolute path.""")
    public static String WEB_DIR = "web/";
    @Key("settings.web-directory.read-only")
    @Comment("""
            Set to true if you don't want Pl3xMap to overwrite
            the website files on startup. (Good for servers that
            customize these files)
            Note: Be sure this is false when upgrading.""")
    public static boolean WEB_DIR_READONLY = false;
    @Key("settings.web-directory.tile-format")
    @Comment("""
            The image format for tile images.
            Built in types: bmp, gif, jpg, jpeg, png""")
    public static String WEB_TILE_FORMAT = "png";
    @Key("settings.web-directory.tile-quality")
    @Comment("""
            The quality for image tiles (0.0 - 1.0)
            0.0 is low quality, high compression, small file size
            1.0 is high quality, no compression, large file size
            Note: Not all image formats honor this setting.""")
    public static double WEB_TILE_QUALITY = 0.0D;

    @Key("settings.map.zoom.snap")
    @Comment("""
            Forces the map's zoom level to always be a multiple of this.
            By default, the zoom level snaps to the nearest integer; lower
            values (e.g. 0.5 or 0.1) allow for greater granularity. A
            value of 0 means the zoom level will not be snapped.""")
    public static double MAP_ZOOM_SNAP = 0.25D;
    @Key("settings.map.zoom.delta")
    @Comment("""
            Controls how much the map's zoom level will change after a zoom in,
            zoom out, pressing + or - on the keyboard, or using the zoom controls.
            Values smaller than 1 (e.g. 0.5) allow for greater granularity.""")
    public static double MAP_ZOOM_DELTA = 0.25D;
    @Key("settings.map.zoom.wheel")
    @Comment("""
            How many scroll pixels (as reported by L.DomEvent.getWheelDelta) mean
            a change of one full zoom level. Smaller values will make wheel-zooming
            faster (and vice versa).""")
    public static int MAP_ZOOM_WHEEL = 120;

    @Key("settings.internal-webserver.enabled")
    @Comment("""
            Enable the built-in web server.
            Disable this if you want to use a standalone web server
            such as apache or nginx.""")
    public static boolean HTTPD_ENABLED = true;
    @Key("settings.internal-webserver.bind")
    @Comment("""
            The interface the built-in web server should bind to.
            This is NOT always the same as your public facing IP.
            If you don't understand what this is,
            leave it set to 0.0.0.0""")
    public static String HTTPD_BIND = "0.0.0.0";
    @Key("settings.internal-webserver.port")
    @Comment("""
            The port the built-in web server listens to.
            Make sure the port is allocated if using Pterodactyl.""")
    public static int HTTPD_PORT = 8080;
    @Key("settings.internal-webserver.follow-symlinks")
    @Comment("""
            Allows the built-in web server to follow symlinks.
            It is generally advised against enabling this,
            for security reasons. But you do you, boo boo.""")
    public static boolean HTTPD_FOLLOW_SYMLINKS = false;

    @Key("settings.performance.live-update-threads")
    @Comment("""
            The number of process-threads to use for real-time marker updates on the map.
            Value of -1 will use 50% of the available cpu-threads. (recommended)""")
    public static int LIVE_UPDATE_THREADS = -1;

    @Key("settings.performance.render-threads")
    @Comment("""
            The number of process-threads to use for loading and scanning chunks.
            Value of -1 will use 50% of the available cpu-threads. (recommended)""")
    public static int RENDER_THREADS = -1;

    @Key("settings.performance.gc.when-finished")
    @Comment("""
            Runs the JVM GC after a render job stops to free up memory immediately.""")
    public static boolean GC_WHEN_FINISHED = true;

    @Key("settings.performance.gc.when-running")
    @Comment("""
            Runs the JVM GC aggressively while a render is running
            CAUTION: this _will_ slow down your renders!""")
    public static boolean GC_WHEN_RUNNING = false;

    private static final Config CONFIG = new Config();

    public static void reload() {
        Path mainDir = Pl3xMap.api().getMainDir();

        // extract default config from jar
        FileUtil.extractFile(Config.class, "config.yml", mainDir, false);

        CONFIG.reload(mainDir.resolve("config.yml"), Config.class);

        if (!Pl3xMap.api().isBukkit()) {
            tryRenamePath("world-settings.world", "world-settings.minecraft:overworld");
            tryRenamePath("world-settings.world_nether", "world-settings.minecraft:the_nether");
            tryRenamePath("world-settings.world_the_end", "world-settings.minecraft:the_end");
        }
    }

    private static void tryRenamePath(String oldPath, String newPath) {
        YamlFile config = CONFIG.getConfig();
        Object oldValue = config.get(oldPath);
        if (oldValue == null) {
            return; // old default doesn't exist; do nothing
        }
        if (config.get(newPath) != null) {
            return; // new default already set; do nothing
        }
        config.set(newPath, oldValue);
        config.set(oldPath, null);
        CONFIG.save();
    }
}
