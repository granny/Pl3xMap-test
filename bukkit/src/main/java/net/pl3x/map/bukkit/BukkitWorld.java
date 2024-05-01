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
package net.pl3x.map.bukkit;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.configuration.ColorsConfig;
import net.pl3x.map.core.event.world.WorldLoadedEvent;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.player.Player;
import net.pl3x.map.core.registry.BiomeRegistry;
import net.pl3x.map.core.util.Colors;
import net.pl3x.map.core.util.Mathf;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

public class BukkitWorld extends World {
    private final ServerLevel level;

    public BukkitWorld(@NotNull ServerLevel level, @NotNull String name) {
        super(
                name,
                level.getSeed(),
                Point.of(level.getLevelData().getSpawnPos().getX(), level.getLevelData().getSpawnPos().getZ()),
                Type.get(level.dimension().location().toString()),
                level.convertable.getDimensionPath(level.dimension()).resolve("region")
        );
        this.level = level;

        if (!isEnabled()) {
            return;
        }

        init();

        // register biomes
        Set<Map.Entry<ResourceKey<Biome>, Biome>> entries = level.registryAccess().registryOrThrow(Registries.BIOME).entrySet();
        for (Map.Entry<ResourceKey<Biome>, Biome> entry : entries) {
            if (getBiomeRegistry().size() > BiomeRegistry.MAX_INDEX) {
                Logger.debug(String.format("Cannot register any more biomes. Registered: %d Unregistered: %d", getBiomeRegistry().size(), entries.size() - getBiomeRegistry().size()));
                break;
            }

            String id = entry.getKey().location().toString();
            Biome biome = entry.getValue();
            float temperature = Mathf.clamp(0.0F, 1.0F, biome.getBaseTemperature());
            float humidity = Mathf.clamp(0.0F, 1.0F, biome.climateSettings.downfall());
            getBiomeRegistry().register(
                    id,
                    ColorsConfig.BIOME_COLORS.getOrDefault(id, 0),
                    ColorsConfig.BIOME_FOLIAGE.getOrDefault(id, biome.getSpecialEffects().getFoliageColorOverride().orElse(Colors.getDefaultFoliageColor(temperature, humidity))),
                    ColorsConfig.BIOME_GRASS.getOrDefault(id, biome.getSpecialEffects().getGrassColorOverride().orElse(Colors.getDefaultGrassColor(temperature, humidity))),
                    ColorsConfig.BIOME_WATER.getOrDefault(id, biome.getSpecialEffects().getWaterColor()),
                    (x, z, color) -> biome.getSpecialEffects().getGrassColorModifier().modifyColor(x, z, color)
            );
        }

        getBiomeRegistry().saveToDisk(this);

        Pl3xMap.api().getEventRegistry().callEvent(new WorldLoadedEvent(this));
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> @NotNull T getLevel() {
        return (@NotNull T) this.level;
    }

    @Override
    public long hashSeed(long seed) {
        return BiomeManager.obfuscateSeed(seed);
    }

    @Override
    public boolean hasCeiling() {
        return this.level.dimensionType().hasCeiling();
    }

    @Override
    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    @Override
    public int getMaxBuildHeight() {
        return this.level.getMaxBuildHeight();
    }

    @Override
    public int getLogicalHeight() {
        return this.level.getLogicalHeight();
    }

    @Override
    public double getBorderMinX() {
        return this.level.getWorldBorder().getMinX();
    }

    @Override
    public double getBorderMinZ() {
        return this.level.getWorldBorder().getMinZ();
    }

    @Override
    public double getBorderMaxX() {
        return this.level.getWorldBorder().getMaxX();
    }

    @Override
    public double getBorderMaxZ() {
        return this.level.getWorldBorder().getMaxZ();
    }

    @Override
    public @NotNull Collection<@NotNull Player> getPlayers() {
        return this.<ServerLevel>getLevel().players().stream()
                .map(player -> Pl3xMap.api().getPlayerRegistry().get(player.getUUID()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public @NotNull String toString() {
        return "BukkitWorld{"
                + "name=" + getName()
                + ",seed=" + getSeed()
                + ",spawn=" + getSpawn()
                + "}";
    }
}
