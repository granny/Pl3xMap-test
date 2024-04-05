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
public final class PlayersLayerConfig extends AbstractConfig {
    private static final PlayersLayerConfig CONFIG = new PlayersLayerConfig();
    @Key("settings.enabled")
    @Comment("""
            Show online players on the map and sidebar.""")
    public static boolean ENABLED = true;
    @Key("settings.hide.invisible")
    @Comment("""
            Should invisible players be hidden from the map.""")
    public static boolean HIDE_INVISIBLE = true;
    @Key("settings.hide.spectators")
    @Comment("""
            Should spectators be hidden from the map.""")
    public static boolean HIDE_SPECTATORS = true;
    @Key("settings.layer.update-interval")
    @Comment("""
            How often (in seconds) to update the marker.
            Setting to 0 is the same as setting it to 1.""")
    public static int UPDATE_INTERVAL = 0;
    @Key("settings.layer.show-controls")
    @Comment("""
            Whether the players layer control shows up in the layers list or not.""")
    public static boolean SHOW_CONTROLS = true;
    @Key("settings.layer.default-hidden")
    @Comment("""
            Whether the players layer should be hidden (toggled off) by default.""")
    public static boolean DEFAULT_HIDDEN = false;
    @Key("settings.layer.priority")
    @Comment("""
            Priority order players layer shows up in the layers list.
            (lower values = higher in the list)""")
    public static int PRIORITY = 20;
    @Key("settings.layer.z-index")
    @Comment("""
            Z-Index order players layer shows up in the map.
            (higher values are drawn on top of lower values)""")
    public static int Z_INDEX = 999;
    @Key("settings.icon")
    @Comment("""
            The player icon.
            Icon must be in the web/images/icon/ directory.""")
    public static String ICON = "players";
    @Key("settings.tooltip")
    @Comment("""
            Tooltip for player markers.
            Variables: uuid, name, decoratedName, health, armor""")
    public static String TOOLTIP = """
            <ul>
              <li><img src='images/skins/2D/<uuid>.png' class='head' alt='<name>' /></li>
              <li>
                <name>
                <img src='images/clear.png' class='health' style='background-position:0 calc(-<health>px * 9);' alt='Health <health>' />
                <img src='images/clear.png' class='armor' style='background-position:0 calc(-<armor>px * 9);' alt='Armor <armor>' />
              </li>
            </ul>""";
    @Key("settings.pane")
    @Comment("""
            The custom pane layer for the player tracker.
            This is used to make custom css styled tooltips.
            (see css setting below)""")
    public static String PANE = "nameplates";
    @Key("settings.css")
    @Comment("""
            Custom css for players marker layer.
            Class names use the pane name set above.""")
    public static String CSS = """
            div.leaflet-nameplates-pane div img.head {
              image-rendering: pixelated;
              image-rendering: -moz-crisp-edges;
              -ms-interpolation-mode: nearest-neighbor;
            }
            div.leaflet-nameplates-pane div {
              margin: 0;
              padding: 0;
              color: #ffffff;
              font-weight: 700;
              line-height: 1rem;
              background: rgba(0, 0, 0, 0.5);
              border-color: rgba(0, 0, 0, 0.75);
            }
            div.leaflet-nameplates-pane div ul {
              padding: 3px;
            }
            div.leaflet-nameplates-pane div:before {
              border-color: transparent;
            }
            div.leaflet-nameplates-pane div img.head {
              vertical-align: middle;
              width: 32px;
              height: 32px;
              border-radius: 5px;
              border: 1px solid black;
            }
            div.leaflet-nameplates-pane div img.health {
              margin-top: 3px;
            }
            div.leaflet-nameplates-pane div img.armor,
            div.leaflet-nameplates-pane div img.health {
              display: block;
              width: 81px;
              height: 9px;
              background-position: 0 0;
            }
            div.leaflet-nameplates-pane div img.armor {
              background: url('images/armor.png') no-repeat;
            }
            div.leaflet-nameplates-pane div img.health {
              background: url('images/health.png') no-repeat;
            }
            div.leaflet-nameplates-pane div,
            div.leaflet-marker-pane img {
              transition: all 0.25s;
            }""";

    public static void reload() {
        CONFIG.reload(Pl3xMap.api().getMainDir().resolve("layers/players.yml"), PlayersLayerConfig.class);
    }
}
