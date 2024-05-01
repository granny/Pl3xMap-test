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

import com.google.common.io.ByteArrayDataOutput;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.network.Constants;
import net.pl3x.map.core.network.Network;
import net.pl3x.map.fabric.common.network.ClientboundMapPayload;
import net.pl3x.map.fabric.common.network.ClientboundServerPayload;
import net.pl3x.map.fabric.common.network.ServerboundMapPayload;
import net.pl3x.map.fabric.common.network.ServerboundServerPayload;

public class FabricNetwork extends Network {
    private final Pl3xMapFabricServer mod;

    public FabricNetwork(Pl3xMapFabricServer mod) {
        this.mod = mod;
    }

    @Override
    public void register() {
        PayloadTypeRegistry.playC2S().register(ServerboundServerPayload.TYPE, ServerboundServerPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundServerPayload.TYPE, ClientboundServerPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ServerboundMapPayload.TYPE, ServerboundMapPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundMapPayload.TYPE, ClientboundMapPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerboundServerPayload.TYPE, (payload, context) -> {
            ServerPlayNetworking.send(context.player(), new ClientboundServerPayload(Constants.PROTOCOL, Constants.RESPONSE_SUCCESS, Config.WEB_ADDRESS));
        });

        ServerPlayNetworking.registerGlobalReceiver(ServerboundMapPayload.TYPE, (payload, context) -> {
            sendMapData(context.player(), payload.mapId());
        });
    }

    @Override
    public void unregister() {
        ServerPlayNetworking.unregisterGlobalReceiver(ServerboundServerPayload.TYPE.id());
        ServerPlayNetworking.unregisterGlobalReceiver(ServerboundMapPayload.TYPE.id());
    }

    @Override
    protected <T> void sendServerData(T player) {

    }

    @Override
    protected <T> void sendMapData(T player, int id) {

    }

    @Override
    protected <T> void send(T player, ByteArrayDataOutput out) {

    }

    protected <T> void sendMapData(ServerPlayer player, int id) {
        MinecraftServer server = this.mod.getServer();
        if (server == null) {
            return;
        }

        @SuppressWarnings("DataFlowIssue")
        MapItemSavedData map = MapItem.getSavedData(new MapId(id), server.getLevel(Level.OVERWORLD));
        if (map == null) {
            ServerPlayNetworking.send(player, new ClientboundMapPayload(Constants.PROTOCOL, Constants.ERROR_NO_SUCH_MAP, id));
            return;
        }

        ServerLevel level = this.mod.getServer().getLevel(map.dimension);
        if (level == null) {
            ServerPlayNetworking.send(player, new ClientboundMapPayload(Constants.PROTOCOL, Constants.ERROR_NO_SUCH_WORLD, id));
            return;
        }

        ServerPlayNetworking.send(player, new ClientboundMapPayload(
                Constants.PROTOCOL, Constants.RESPONSE_SUCCESS, id,
                map.scale, map.centerX, map.centerZ, level.dimension().location().toString().replace(":", "-")
        ));
    }
}
