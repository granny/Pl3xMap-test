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
package net.pl3x.map.neoforge;

import com.mojang.serialization.Codec;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforgespi.language.IModInfo;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.server.ServerLoadedEvent;
import net.pl3x.map.core.network.Constants;
import net.pl3x.map.core.player.Player;
import net.pl3x.map.core.player.PlayerListener;
import net.pl3x.map.core.player.PlayerRegistry;
import net.pl3x.map.core.world.World;
import net.pl3x.map.neoforge.command.NeoForgeCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod("pl3xmap")
public class Pl3xMapNeoForge extends Pl3xMap {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MODID);

    public static final Supplier<AttachmentType<Boolean>> HIDDEN = Pl3xMapNeoForge.ATTACHMENT_TYPES.register(
            "hidden",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .copyOnDeath()
                    .build()
    );

    private final PlayerListener playerListener = new PlayerListener();

    @SuppressWarnings("deprecation")
    private final RandomSource randomSource = RandomSource.createThreadSafe();

    private MinecraftServer server;
    private IModInfo modInfo;

    private MinecraftServerAudiences adventure;

    private int tick;

    private final NeoForgeNetwork network;

    @SuppressWarnings("InstantiationOfUtilityClass")
    public Pl3xMapNeoForge(IEventBus eventBus) {
        super(false);

        NeoForge.EVENT_BUS.register(this);
        ATTACHMENT_TYPES.register(eventBus);

        try {
            new NeoForgeCommandManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.network = new NeoForgeNetwork(this);
        eventBus.register(this.network);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.@NotNull Post event) {
        if (this.tick++ >= 20) {
            this.tick = 0;
            getScheduler().tick();
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        PlayerRegistry registry = Pl3xMap.api().getPlayerRegistry();
        UUID uuid = event.getEntity().getUUID();
        Player forgePlayer = registry.getOrDefault(uuid, () -> new NeoForgePlayer((ServerPlayer) event.getEntity()));
        this.playerListener.onJoin(forgePlayer);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.@NotNull PlayerLoggedOutEvent event) {
        PlayerRegistry registry = Pl3xMap.api().getPlayerRegistry();
        UUID uuid = event.getEntity().getUUID();
        Player forgePlayer = registry.unregister(uuid);
        if (forgePlayer != null) {
            this.playerListener.onQuit(forgePlayer);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        if (!isEnabled()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        String name = level.dimension().location().toString();
        Pl3xMap.api().getWorldRegistry().getOrDefault(name, () -> new NeoForgeWorld(level, name));
    }

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        String name = level.dimension().location().toString();
        Pl3xMap.api().getWorldRegistry().unregister(name);
    }

    @SubscribeEvent
    public void onServerStarted(@NotNull ServerStartedEvent event) {
        this.server = event.getServer();
        this.adventure = MinecraftServerAudiences.of(this.server);

        enable();

        this.network.register();
    }

    @SubscribeEvent
    public void onServerStopping(@NotNull ServerStoppingEvent event) {
        this.network.unregister();

        disable();

        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    @SubscribeEvent
    public void onServerLoad(ServerStartedEvent event) {
        Pl3xMap.api().getEventRegistry().callEvent(new ServerLoadedEvent());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer) {
            getPlayerRegistry().getOrDefault(newPlayer.getUUID(), () -> new NeoForgePlayer(newPlayer)).setPlayer(newPlayer);
        }
    }

    public @NotNull IModInfo getModInfo() {
        if (this.modInfo == null) {
            this.modInfo = ModList.get().getModContainerById("pl3xmap").orElseThrow().getModInfo();
        }
        return this.modInfo;
    }

    @Override
    public @NotNull String getPlatform() {
        return this.server.getServerModName().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String getVersion() {
        return getModInfo().getVersion().toString();
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
        return FMLPaths.GAMEDIR.get().resolve("config").resolve("pl3xmap");
    }

    @Override
    public @NotNull Path getJarPath() {
        return getModInfo().getOwningFile().getFile().getFilePath();
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
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return key == null ? null : getBlockRegistry().get(key.toString());
    }

    @Override
    protected void loadBlocks() {
        for (Map.Entry<ResourceKey<Block>, Block> entry : this.server.registryAccess().registryOrThrow(Registries.BLOCK).entrySet()) {
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
            Pl3xMap.api().getWorldRegistry().getOrDefault(name, () -> new NeoForgeWorld(level, name));
        });
    }

    @Override
    protected void loadPlayers() {
        this.server.getPlayerList().getPlayers().forEach(player -> {
            UUID uuid = player.getUUID();
            getPlayerRegistry().getOrDefault(uuid, () -> new NeoForgePlayer(player));
        });
    }

    @Override
    public @NotNull World cloneWorld(@NotNull World world) {
        return new NeoForgeWorld(world.getLevel(), world.getName());
    }

    public @Nullable MinecraftServer getServer() {
        return this.server;
    }
}
