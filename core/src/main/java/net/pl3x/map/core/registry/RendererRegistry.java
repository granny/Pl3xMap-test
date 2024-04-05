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
package net.pl3x.map.core.registry;

import java.lang.reflect.InvocationTargetException;
import net.pl3x.map.core.renderer.BasicRenderer;
import net.pl3x.map.core.renderer.BiomeRenderer;
import net.pl3x.map.core.renderer.BlockInfoRenderer;
import net.pl3x.map.core.renderer.FlowerMapRenderer;
import net.pl3x.map.core.renderer.InhabitedRenderer;
import net.pl3x.map.core.renderer.NetherRoofRenderer;
import net.pl3x.map.core.renderer.NightRenderer;
import net.pl3x.map.core.renderer.Renderer;
import net.pl3x.map.core.renderer.VanillaRenderer;
import net.pl3x.map.core.renderer.VintageStoryRenderer;
import net.pl3x.map.core.renderer.task.RegionScanTask;
import org.jetbrains.annotations.NotNull;

public class RendererRegistry extends Registry<Renderer.@NotNull Builder> {
    public static final String BASIC = "basic";
    public static final String BIOMES = "biomes";
    public static final String BLOCKINFO = "blockinfo";
    public static final String FLOWERMAP = "flowermap";
    public static final String INHABITED = "inhabited";
    public static final String NIGHT = "night";
    public static final String VANILLA = "vanilla";
    public static final String VINTAGE_STORY = "vintage_story";
    public static final String NETHER_ROOF = "nether_roof";

    public void register() {
        register(BASIC, new Renderer.Builder(BASIC, "Basic", BasicRenderer.class));
        register(BIOMES, new Renderer.Builder(BIOMES, "Biomes", BiomeRenderer.class));
        register(BLOCKINFO, new Renderer.Builder(BLOCKINFO, "BlockInfo", BlockInfoRenderer.class));
        register(FLOWERMAP, new Renderer.Builder(FLOWERMAP, "FlowerMap", FlowerMapRenderer.class));
        register(INHABITED, new Renderer.Builder(INHABITED, "Inhabited", InhabitedRenderer.class));
        register(NIGHT, new Renderer.Builder(NIGHT, "Night", NightRenderer.class));
        register(VANILLA, new Renderer.Builder(VANILLA, "Vanilla", VanillaRenderer.class));
        register(VINTAGE_STORY, new Renderer.Builder(VINTAGE_STORY, "VintageStory", VintageStoryRenderer.class));
        register(NETHER_ROOF, new Renderer.Builder(NETHER_ROOF, "NetherRoof", NetherRoofRenderer.class));
    }

    public @NotNull Renderer createRenderer(@NotNull RegionScanTask task, Renderer.@NotNull Builder builder) {
        try {
            return builder.getClazz().getConstructor(RegionScanTask.class, Renderer.Builder.class).newInstance(task, builder);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
