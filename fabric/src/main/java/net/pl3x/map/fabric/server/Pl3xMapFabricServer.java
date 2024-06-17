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
package net.pl3x.map.fabric.server;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.server.ServerLoadedEvent;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.player.Player;
import net.pl3x.map.core.player.PlayerListener;
import net.pl3x.map.core.registry.BlockRegistry;
import net.pl3x.map.core.world.World;
import net.pl3x.map.fabric.server.command.FabricCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Pl3xMapFabricServer extends Pl3xMap implements DedicatedServerModInitializer {
    @SuppressWarnings("deprecation")
    private final RandomSource randomSource = RandomSource.createThreadSafe();
    private final PlayerListener playerListener = new PlayerListener();

    private MinecraftServer server;
    private ModContainer modContainer;
    private FabricServerAudiences adventure;

    private boolean firstTick = true;

    private FabricNetwork network;

    public Pl3xMapFabricServer() {
        super(false);
    }

    @Override
    public void onInitializeServer() {
        try {
            new FabricCommandManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (this.firstTick) {
                Pl3xMap.api().getEventRegistry().callEvent(new ServerLoadedEvent());
                this.firstTick = false;
            }
            getScheduler().tick();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            Player fabricPlayer = getPlayerRegistry().getOrDefault(player.getUUID(), () -> new FabricPlayer(player));
            this.playerListener.onJoin(fabricPlayer);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            Player fabricPlayer = getPlayerRegistry().unregister(player.getUUID());
            if (fabricPlayer != null) {
                this.playerListener.onQuit(fabricPlayer);
            }
        });

        ServerWorldEvents.LOAD.register((server, level) -> {
            if (isEnabled()) {
                String name = level.dimension().location().toString();
                Pl3xMap.api().getWorldRegistry().getOrDefault(name, () -> new FabricWorld(level, name));
            }
        });

        ServerWorldEvents.UNLOAD.register((server, level) -> {
            String name = level.dimension().location().toString();
            Pl3xMap.api().getWorldRegistry().unregister(name);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            this.adventure = FabricServerAudiences.of(this.server);

            enable();

            this.network = new FabricNetwork(this);
            this.network.register();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (this.network != null) {
                this.network.unregister();
                this.network = null;
            }

            disable();

            if (this.adventure != null) {
                this.adventure.close();
                this.adventure = null;
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                getPlayerRegistry().getOrDefault(newPlayer.getUUID(), () -> new FabricPlayer(newPlayer)).setPlayer(newPlayer)
        );
    }

    public @NotNull ModContainer getModContainer() {
        if (this.modContainer == null) {
            this.modContainer = FabricLoader.getInstance().getModContainer("pl3xmap").orElseThrow();
        }
        return this.modContainer;
    }

    @Override
    public @NotNull String getPlatform() {
        return this.server.getServerModName().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String getVersion() {
        return getModContainer().getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public int getMaxPlayers() {
        return this.server.getMaxPlayers();
    }

    @Override
    public boolean getOnlineMode() {
        return this.server.usesAuthentication();
    }

    @Override
    public String getServerVersion() {
        return SharedConstants.getCurrentVersion().getName();
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
        return FabricLoader.getInstance().getGameDir().resolve("config").resolve("pl3xmap");
    }

    @Override
    public @NotNull Path getJarPath() {
        return getModContainer().getOrigin().getPaths().get(0);
    }

    @Override
    public int getColorForPower(byte power) {
        return RedStoneWireBlock.getColorForPower(power);
    }

    @Override
    public net.pl3x.map.core.world.@Nullable Block getFlower(@NotNull World world, net.pl3x.map.core.world.@NotNull Biome biome, int blockX, int blockY, int blockZ) {
        // https://github.com/Draradech/FlowerMap (CC0-1.0 license)
        Biome nms = world.<ServerLevel>getLevel().registryAccess().registryOrThrow(Registries.BIOME).get(ResourceLocation.parse(biome.getKey()));
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
        Set<Map.Entry<ResourceKey<Block>, Block>> entries = this.server.registryAccess().registryOrThrow(Registries.BLOCK).entrySet();
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
        this.server.getAllLevels().forEach(level -> {
            String name = level.dimension().location().toString();
            Pl3xMap.api().getWorldRegistry().getOrDefault(name, () -> new FabricWorld(level, name));
        });
    }

    @Override
    protected void loadPlayers() {
        this.server.getPlayerList().getPlayers().forEach(player -> {
            UUID uuid = player.getUUID();
            getPlayerRegistry().getOrDefault(uuid, () -> new FabricPlayer(player));
        });
    }

    @Override
    public @NotNull World cloneWorld(@NotNull World world) {
        return new FabricWorld(world.getLevel(), world.getName());
    }

    public @Nullable MinecraftServer getServer() {
        return this.server;
    }
}
