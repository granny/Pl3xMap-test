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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.util.FileUtil;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("CanBeFinal")
public final class Lang extends AbstractConfig {
    @Key("prefix.command")
    public static String PREFIX_COMMAND = "<grey>[<gradient:#C028FF:#5B00FF>Pl3xMap</gradient>]</grey> ";
    @Key("command.base")
    public static String COMMAND_BASE = "View the map at '<grey><click:open_url:'<web-address>'><web-address></click></grey>'";

    @Key("command.event.click-for-help")
    public static String CLICK_FOR_HELP = "Click for help";
    @Key("command.event.click-to-confirm")
    public static String CLICK_TO_CONFIRM = "Click to confirm";

    @Key("httpd.started.success")
    public static String HTTPD_STARTED = "<green>Internal webserver running on <yellow><bind></yellow>:<yellow><port></yellow>";
    @Key("httpd.started.error")
    public static String HTTPD_START_ERROR = "<red>Internal webserver could not start";
    @Key("httpd.stopped.success")
    public static String HTTPD_STOPPED = "<green>Internal webserver stopped";
    @Key("httpd.stopped.error")
    public static String HTTPD_STOP_ERROR = "<red>An error occurred with the internal webserver";
    @Key("httpd.disabled")
    public static String HTTPD_DISABLED = "<green>Internal webserver is disabled";

    @Key("progress.eta.unknown")
    public static String PROGRESS_ETA_UNKNOWN = "Unknown";

    @Key("command.argument.optional-player")
    public static String COMMAND_ARGUMENT_OPTIONAL_PLAYER_DESCRIPTION = "Defaults to the executing player if unspecified (console must specify a player)";
    @Key("command.argument.optional-center")
    public static String COMMAND_ARGUMENT_OPTIONAL_CENTER_DESCRIPTION = "Defaults to (<white>0<gray>,</gray> 0</white>) if unspecified";
    @Key("command.argument.optional-zoom")
    public static String COMMAND_ARGUMENT_OPTIONAL_ZOOM_DESCRIPTION = "Map zoom level";
    @Key("command.argument.required-renderer")
    public static String COMMAND_ARGUMENT_REQUIRED_RENDERER_DESCRIPTION = "Renderer is required";
    @Key("command.argument.required-world")
    public static String COMMAND_ARGUMENT_REQUIRED_WORLD_DESCRIPTION = "World is required";

    @Key("command.confirm.description")
    public static String COMMAND_CONFIRM_DESCRIPTION = "Confirm a pending command";
    @Key("command.confirm.not-rendering")
    public static String COMMAND_CONFIRM_CONFIRMATION_REQUIRED_MESSAGE = "<red>Confirmation required. Confirm using <grey>/map confirm";
    @Key("command.confirm.success")
    public static String COMMAND_CONFIRM_NO_PENDING_MESSAGE = "<red>You don't have any pending confirmations";

    @Key("command.fullrender.description")
    public static String COMMAND_FULLRENDER_DESCRIPTION = "Fully render a world";
    @Key("command.fullrender.starting")
    public static String COMMAND_FULLRENDER_STARTING = "<green>Full render starting. Check <grey>/map status</grey> for more info";

    @Key("command.help.description")
    public static String COMMAND_HELP_DESCRIPTION = "Get help for Pl3xmap commands";
    @Key("command.help.argument.query")
    public static String COMMAND_HELP_ARGUMENT_QUERY_DESCRIPTION = "Help Query";

    @Key("command.hide.description")
    public static String COMMAND_HIDE_DESCRIPTION = "Hide a player from the map";
    @Key("command.hide.already-hidden")
    public static String COMMAND_HIDE_ALREADY_HIDDEN = "<grey><player> <red>is already hidden from the map";
    @Key("command.hide.success")
    public static String COMMAND_HIDE_SUCCESS = "<grey><player> <green>is now hidden from the map";

    @Key("command.pause.description")
    public static String COMMAND_PAUSE_DESCRIPTION = "Toggle the pause state of renderers";
    @Key("command.pause.paused")
    public static String COMMAND_PAUSE_PAUSED = "<green>Renderers are now paused";
    @Key("command.pause.unpaused")
    public static String COMMAND_PAUSE_UNPAUSED = "<green>Renderers are now unpaused";

    @Key("command.radiusrender.description")
    public static String COMMAND_RADIUSRENDER_DESCRIPTION = "Render a section of a world";
    @Key("command.radiusrender.starting")
    public static String COMMAND_RADIUSRENDER_STARTING = "<green>Radius render starting. Check <grey>/map status</grey> for more info";

    @Key("command.reload.description")
    public static String COMMAND_RELOAD_DESCRIPTION = "Reloads the plugin";
    @Key("command.reload.success")
    public static String COMMAND_RELOAD_SUCCESS = "<green>Pl3xMap <grey>v<version></grey> reloaded";

    @Key("command.resetmap.description")
    public static String COMMAND_RESETMAP_DESCRIPTION = "Cancel active render of a world";
    @Key("command.resetmap.begin")
    public static String COMMAND_RESETMAP_BEGIN = "<green>Map reset for <grey><world></grey> has begun";
    @Key("command.resetmap.success")
    public static String COMMAND_RESETMAP_SUCCESS = "<green>Successfully reset map for <grey><world>";
    @Key("command.resetmap.failed")
    public static String COMMAND_RESETMAP_FAILED = "<red>Could not reset map for <grey><world>";

    @Key("command.show.description")
    public static String COMMAND_SHOW_DESCRIPTION = "Show a player on the map";
    @Key("command.show.not-hidden")
    public static String COMMAND_SHOW_NOT_HIDDEN = "<grey><player> <red>is not hidden from the map";
    @Key("command.show.success")
    public static String COMMAND_SHOW_SUCCESS = "<grey><player> <green>is no longer hidden from the map";

    @Key("command.status.description")
    public static String COMMAND_STATUS_DESCRIPTION = "View the render status";

    @Key("command.stitch.description")
    public static String COMMAND_STITCH_DESCRIPTION = "Stitches tiles into one image";
    @Key("command.stitch.missing-directory")
    public static String COMMAND_STITCH_MISSING_DIRECTORY = "<red>Unable to find tiles directory.";
    @Key("command.stitch.error-reading-directory")
    public static String COMMAND_STITCH_ERROR_READING_DIRECTORY = "<red>There was a problem reading the tiles directory.";
    @Key("command.stitch.empty-directory")
    public static String COMMAND_STITCH_EMPTY_DIRECTORY = "<red>There are no tiles to stitch.";
    @Key("command.stitch.starting")
    public static String COMMAND_STITCH_STARTING = "<green>Started stitching <count> tiles..\n<green><italic>(min: <min-x>,<min-z> max: <max-x>,<max-z> size: <size-x>,<size-z>)";
    @Key("command.stitch.finished")
    public static String COMMAND_STITCH_FINISHED = "<green>Finished stitching <count> tiles!\n<green>You can find it at <grey>/tiles/<world>/stitched/<filename>";

    @Key("command.version.description")
    public static String COMMAND_VERSION_DESCRIPTION = "Get version information";
    @Key("command.version.please-wait")
    public static String COMMAND_VERSION_PLEASE_WAIT = "<italic>Checking version, please wait...";
    @Key("command.version.still-checking")
    public static String COMMAND_VERSION_STILL_CHECKING = "<italic>Still checking...";
    @Key("command.version.error.not-array")
    public static String COMMAND_VERSION_ERROR_NOT_ARRAY = "<red>Error: response not an array";
    @Key("command.version.error.corrupt-json")
    public static String COMMAND_VERSION_ERROR_CORRUPT_JSON = "<red>Error: response is corrupt json";
    @Key("command.version.error.unknown-version")
    public static String COMMAND_VERSION_ERROR_UNKNOWN_VERSION = "<red>Error: response has unknown version";
    @Key("command.version.error.unable-to-determine")
    public static String COMMAND_VERSION_ERROR_UNABLE_TO_DETERMINE = "<red>Error: Unable to determine latest build";
    @Key("command.version.success")
    public static String COMMAND_VERSION_SUCCESS = "Pl3xMap v3 <version> (<italic><platform></italic>) git-<commit>";
    @Key("command.version.snapshot")
    public static String COMMAND_VERSION_SNAPSHOT = "<yellow><italic>You are running a snapshot";
    @Key("command.version.latest-build-is")
    public static String COMMAND_VERSION_LATEST_BUILD_IS = "<yellow><italic>Latest build is <build>";
    @Key("command.version.running-latest-build")
    public static String COMMAND_VERSION_RUNNING_LATEST_BUILD = "<green><italic>You are running the latest build.";
    @Key("command.version.builds-behind")
    public static String COMMAND_VERSION_BUILDS_BEHIND = "<yellow><italic>You are <number> builds behind.";
    @Key("command.version.download")
    public static String COMMAND_VERSION_DOWNLOAD = "<yellow><italic>Download new build at: <gold><link>";
    @Key("command.version.time-traveler")
    public static String COMMAND_VERSION_TIME_TRAVELER = "<yellow><italic>Are you a time traveler?";

    @Key("error.must-specify-player")
    public static String ERROR_MUST_SPECIFY_PLAYER = "<red>You must specify the player";
    @Key("error.no-such-player")
    public static String ERROR_NO_SUCH_PLAYER = "<red>No such player <grey><player>";
    @Key("error.must-specify-renderer")
    public static String ERROR_MUST_SPECIFY_RENDERER = "<red>You must specify the renderer";
    @Key("error.no-such-renderer")
    public static String ERROR_NO_SUCH_RENDERER = "<red>No such renderer <grey><renderer>";
    @Key("error.must-specify-world")
    public static String ERROR_MUST_SPECIFY_WORLD = "<red>You must specify the world";
    @Key("error.no-such-world")
    public static String ERROR_NO_SUCH_WORLD = "<red>No such world <grey><world>";
    @Key("error.world-disabled")
    public static String ERROR_WORLD_DISABLED = "<red>Pl3xMap is disabled for world <grey><world>";
    @Key("error.not-valid-zoom-level")
    public static String ERROR_NOT_VALID_ZOOM_LEVEL = "<red>Not a valid zoom level";
    @Key("error.point-invalid-format")
    public static String ERROR_POINT_INVALID_FORMAT = "'<point>' is not a valid location. Required format is '<x> <z>'";

    @Key("ui.layer.players")
    public static String UI_LAYER_PLAYERS = "Players";
    @Key("ui.layer.spawn")
    public static String UI_LAYER_SPAWN = "Spawn";
    @Key("ui.layer.worldborder")
    public static String UI_LAYER_WORLDBORDER = "World Border";

    @Key("ui.title")
    public static String UI_TITLE = "Pl3xMap";
    @Key("ui.block-and-biome-lang-file")
    public static String UI_BLOCK_AND_BIOME_LANG_FILE = "en_us.json";
    @Key("ui.blockinfo.label")
    public static String UI_BLOCKINFO_LABEL = "BlockInfo";
    @Key("ui.blockinfo.value")
    public static String UI_BLOCKINFO_VALUE = "<block><br /><biome>";
    @Key("ui.blockinfo.unknown.block")
    public static String UI_BLOCKINFO_UNKNOWN_BLOCK = "Unknown block";
    @Key("ui.blockinfo.unknown.biome")
    public static String UI_BLOCKINFO_UNKNOWN_BIOME = "Unknown biome";
    @Key("ui.coords.label")
    public static String UI_COORDS_LABEL = "Coordinates";
    @Key("ui.coords.value")
    public static String UI_COORDS_VALUE = "<x>, <y>, <z>";
    @Key("ui.link.label")
    public static String UI_LINK_LABEL = "Sharable Link";
    @Key("ui.link.value")
    public static String UI_LINK_VALUE = "";
    @Key("ui.markers.label")
    public static String UI_MARKERS_LABEL = "Markers";
    @Key("ui.markers.value")
    public static String UI_MARKERS_VALUE = "No markers have been configured";
    @Key("ui.players.label")
    public static String UI_PLAYERS_LABEL = "Players (<online>/<max>)";
    @Key("ui.players.value")
    public static String UI_PLAYERS_VALUE = "No players are currently online";
    @Key("ui.worlds.label")
    public static String UI_WORLDS_LABEL = "Worlds";
    @Key("ui.worlds.value")
    public static String UI_WORLDS_VALUE = "No worlds have been configured";
    @Key("ui.layers.label")
    public static String UI_LAYERS_LABEL = "Layers";
    @Key("ui.layers.value")
    public static String UI_LAYERS_VALUE = "No layers have been configured";

    private static final Lang CONFIG = new Lang();

    public static void reload() {
        Path localeDir = Pl3xMap.api().getMainDir().resolve("locale");

        // extract locale dir from jar
        FileUtil.extractDir("/locale/", localeDir, false);

        CONFIG.reload(localeDir.resolve(Config.LANGUAGE_FILE), Lang.class);
    }

    public static @NotNull Component parse(@NotNull String msg, @NotNull TagResolver.@NotNull Single... placeholders) {
        return MiniMessage.miniMessage().deserialize(msg, placeholders);
    }

    public static @NotNull String strip(@NotNull String msg) {
        return MiniMessage.miniMessage().stripTags(msg);
    }
}
