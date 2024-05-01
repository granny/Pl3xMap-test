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

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.registry.BiomeRegistry;
import net.pl3x.map.core.registry.BlockRegistry;
import net.pl3x.map.core.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Pl3xMapImpl extends Pl3xMap {
    @SuppressWarnings("deprecation")
    private final RandomSource randomSource = RandomSource.createThreadSafe();
    private final JavaPlugin plugin;

    private BukkitAudiences adventure;
    private Path jarPath;

    public Pl3xMapImpl(@NotNull JavaPlugin plugin) {
        super(true);
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        this.adventure = BukkitAudiences.create(this.plugin);
        super.enable();
    }

    @Override
    public void disable() {
        super.disable();
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @Override
    public @NotNull String getPlatform() {
        String name = Bukkit.getName();
        if ("CraftBukkit".equals(name)) {
            // Spigot erroneously reports itself as CraftBukkit,
            // so we have to check for it specifically :/
            try {
                Class.forName("org.spigotmc.SpigotConfig");
                return "spigot";
            } catch (Throwable ignore) {
            }
        }
        return name.toLowerCase(Locale.ROOT);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    @Override
    public boolean getOnlineMode() {
        return Bukkit.getOnlineMode();
    }

    @Override
    public String getServerVersion() {
        return Bukkit.getVersion();
    }

    @Override
    public @NotNull AudienceProvider adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure without a running server!");
        }
        return this.adventure;
    }

    @Override
    public @NotNull Path getMainDir() {
        return this.plugin.getDataFolder().toPath();
    }

    @Override
    public @NotNull Path getJarPath() {
        if (this.jarPath == null) {
            try {
                this.jarPath = Path.of(Pl3xMap.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return this.jarPath;
    }

    @Override
    public int getColorForPower(byte power) {
        return RedStoneWireBlock.getColorForPower(power);
    }

    @Override
    public net.pl3x.map.core.world.@Nullable Block getFlower(@NotNull World world, net.pl3x.map.core.world.@NotNull Biome biome, int blockX, int blockY, int blockZ) {
        // https://github.com/Draradech/FlowerMap (CC0-1.0 license)
        Biome nms = world.<ServerLevel>getLevel().registryAccess().registryOrThrow(Registries.BIOME).get(new ResourceLocation(biome.getKey()));
        if (nms == null) {
            return null;
        }
        List<ConfiguredFeature<?, ?>> flowers = nms.getGenerationSettings().getFlowerFeatures();
        if (flowers.isEmpty()) {
            return null;
        }
        RandomPatchConfiguration config = (RandomPatchConfiguration) flowers.get(0).config();
        SimpleBlockConfiguration flower = (SimpleBlockConfiguration) config.feature().value().feature().value().config();
        Block block = flower.toPlace().getState(this.randomSource, new BlockPos(blockX, blockY, blockZ)).getBlock();
        return getBlockRegistry().get(BuiltInRegistries.BLOCK.getKey(block).toString());
    }

    @Override
    protected void loadBlocks() {
        Set<Map.Entry<ResourceKey<Block>, Block>> entries = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BLOCK).entrySet();
        for (Map.Entry<ResourceKey<Block>, Block> entry : entries) {
            if (getBlockRegistry().size() > BlockRegistry.MAX_INDEX) {
                Logger.debug(String.format("Cannot register any more biomes. Registered: %d Unregistered: %d", getBlockRegistry().size(), entries.size() - getBlockRegistry().size()));
                break;
            }

            String id = entry.getKey().location().toString();
            int color = entry.getValue().defaultMapColor().col;
            getBlockRegistry().register(id, color);
        }
        getBlockRegistry().saveToDisk();
    }

    @Override
    protected void loadWorlds() {
        Bukkit.getWorlds().forEach(world -> getWorldRegistry().getOrDefault(world.getName(),
                () -> new BukkitWorld(((CraftWorld) world).getHandle(), world.getName())));
    }

    @Override
    protected void loadPlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            UUID uuid = player.getUniqueId();
            getPlayerRegistry().getOrDefault(uuid, () -> new BukkitPlayer(player));
        });
    }

    @Override
    public @NotNull World cloneWorld(@NotNull World world) {
        return new BukkitWorld(world.getLevel(), world.getName());
    }
}
