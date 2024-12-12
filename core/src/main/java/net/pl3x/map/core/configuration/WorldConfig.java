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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.area.Area;
import net.pl3x.map.core.markers.area.Border;
import net.pl3x.map.core.util.Mathf;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("CanBeFinal")
public final class WorldConfig extends AbstractConfig {
    @Key("enabled")
    @Comment("""
            Enables this world to be rendered on the map.""")
    public boolean ENABLED = true;

    @Key("render.renderers")
    @Comment("""
            Renderers to use. Each renderer will render a different type of map. The value is the icon
            that the renderer should use in the Web UI. These are any "*.png" files under "web/images/icon/"
            
            The built-in renderers include (key):
            vintage_story, basic, biomes, flowermap, inhabited, night, nether_roof, and vanilla
            
            The built-in icons include (value):
            overworld_basic, overworld_biomes, overworld_night,
            nether_basic, nether_biomes, nether_night, nether_roof,
            the_end_basic, the_end_biomes, the_end_night,
            flowermap, inhabited, vanilla""")
    public Map<@NotNull String, @NotNull String> RENDER_RENDERERS = new LinkedHashMap<>() {{
        put("vintage_story", "overworld_basic");
    }};

    @Key("render.biome-blend")
    @Comment("""
            Enables blending of biome grass/foliage/water colors similar to
            the client's biome blending option.
            Values are clamped to 0-7""")
    public int RENDER_BIOME_BLEND = 3;

    @Key("render.skylight")
    @Comment("""
            World skylight value. This is used for the day/night cycle
            map (not yet implemented) .Values are clamped to 0-15
            with 0 being darkest and 15 being full bright.""")
    public int RENDER_SKYLIGHT = 15;

    @Key("render.translucent-fluids")
    @Comment("""
            Enable translucent fluids.
            This will make the fluids look fancier and translucent,
            so you can see the blocks below in shallow fluids.""")
    public boolean RENDER_TRANSLUCENT_FLUIDS = true;

    @Key("render.translucent-glass")
    @Comment("""
            Enable translucent glass.
            This will make the glass look fancier and translucent,
            so you can see the blocks below.""")
    public boolean RENDER_TRANSLUCENT_GLASS = true;

    @Key("render.heightmap-type")
    @Comment("""
            Type of heightmap to render.
            NONE has no heightmap drawn.
            EVEN_ODD makes every other Y layer darker, like Dynmap.
            HIGH_CONTRAST same as MODERN, but darker.
            LOW_CONTRAST same as MODERN, but lighter.
            MODERN is a clearer, more detailed view.
            OLD_SCHOOL is the old type from v1.
            VANILLA matches the in-game vanilla maps.
            EVEN_ODD_HIGH_CONTRAST mix of EVEN_ODD and HIGH_CONTRAST.
            EVEN_ODD_LOW_CONTRAST mix of EVEN_ODD and LOW_CONTRAST.
            EVEN_ODD_MODERN mix of EVEN_ODD and MODERN.
            EVEN_ODD_OLD_SCHOOL mix of EVEN_ODD and OLD_SCHOOL.
            EVEN_ODD_VANILLA mix of EVEN_ODD and VANILLA.""")
    public String RENDER_HEIGHTMAP_TYPE = "MODERN";

    @Key("ui.display-name")
    @Comment("""
            The display name of the world in the world list.
            Use <world> to use the official world name.""")
    public String DISPLAY_NAME = "<world>";

    @Key("ui.order")
    @Comment("""
            The order of the world in the world list""")
    public int ORDER = 0;

    @Key("ui.attribution")
    @Comment("""
            Shows the footer attributes""")
    public boolean UI_ATTRIBUTION = true;

    @Key("ui.blockinfo")
    @Comment("""
            The display position for the blockinfo box""")
    public String UI_BLOCKINFO = "bottomleft";

    @Key("ui.coords")
    @Comment("""
            The display position for the coordinates box""")
    public String UI_COORDS = "bottomcenter";

    @Key("ui.link")
    @Comment("""
            The display position for the link box""")
    public String UI_LINK = "bottomright";

    @Key("center.x")
    @Comment("""
            The x coordinate for the map to load at.
            A value of -1 will default to world spawn.""")
    public int CENTER_X = -1;

    @Key("center.z")
    @Comment("""
            The z coordinate for the map to load at.
            A value of -1 will default to world spawn.""")
    public int CENTER_Z = -1;


    @Key("zoom.default")
    @Comment("""
            The default zoom when loading the map in browser.
            Normal sized tiles (1 pixel = 1 block) are
            always at zoom level 0.""")
    public int ZOOM_DEFAULT = 0;
    @Key("zoom.max-out")
    @Comment("""
            The maximum zoom out you can do on the map.
            Each additional level requires a new set of tiles
            to be rendered, so don't go too wild here.""")
    public int ZOOM_MAX_OUT = 3;
    @Key("zoom.max-in")
    @Comment("""
            Extra zoom in layers will stretch the original
            tile images so you can zoom in further without
            the extra cost of rendering more tiles.""")
    public int ZOOM_MAX_IN = 2;

    @Key("render.visible-areas")
    @Comment("""
            Visible areas of the world.""")
    public List<Area> VISIBLE_AREAS = new ArrayList<>(); // defaults added in ctor

    private final World world;

    public WorldConfig(@NotNull World world) {
        this.world = world;

        // we need an instance of the world to get the border
        VISIBLE_AREAS.add(new Border(world));

        reload();
    }

    public void reload() {
        reload(Pl3xMap.api().getMainDir().resolve("config.yml"), WorldConfig.class);

        RENDER_BIOME_BLEND = Mathf.clamp(0, 7, RENDER_BIOME_BLEND);
        RENDER_SKYLIGHT = Mathf.clamp(0, 15, RENDER_SKYLIGHT);
    }

    @Override
    protected @NotNull Object getClassObject() {
        return this;
    }

    @Override
    protected @Nullable Object getValue(@NotNull String path, @Nullable Object def) {
        if (getConfig().get("world-settings.default." + path) == null) {
            set("world-settings.default." + path, def);
        }
        return get("world-settings." + this.world.getName() + "." + path,
                get("world-settings.default." + path, def));
    }

    @Override
    protected void setComment(@NotNull String path, @Nullable String comment) {
        getConfig().setComment("world-settings.default." + path, comment);
    }

    @Override
    protected @Nullable Object get(@NotNull String path) {
        if (!path.contains("render.visible-areas")) {
            return super.get(path);
        }
        Object value = getConfig().get(path);
        if (!(value instanceof List<?>)) {
            return value;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) value;
        List<Area> areas = new ArrayList<>();
        for (Map<String, Object> map : list) {
            areas.add(Area.deserialize(this.world, map));
        }
        return areas;
    }

    @Override
    protected void set(@NotNull String path, @Nullable Object value) {
        if (value != null && path.contains("render.visible-areas")) {
            @SuppressWarnings("unchecked")
            List<Area> list = (List<Area>) value;
            value = list.stream().map(Area::serialize).toList();
        }
        getConfig().set(path, value);
    }
}
