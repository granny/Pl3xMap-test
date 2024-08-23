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

import com.google.common.io.ByteArrayDataOutput;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.network.Constants;
import net.pl3x.map.core.network.Network;
import net.pl3x.map.neoforge.common.network.ClientboundMapPayload;
import net.pl3x.map.neoforge.common.network.ClientboundServerPayload;
import net.pl3x.map.neoforge.common.network.ServerboundMapPayload;
import net.pl3x.map.neoforge.common.network.ServerboundServerPayload;

public class NeoForgeNetwork extends Network {
    public static IPayloadHandler<CustomPacketPayload> NOOP_HANDLER = (payload, context) -> {};

    private final Pl3xMapNeoForge mod;

    public NeoForgeNetwork(Pl3xMapNeoForge mod) {
        this.mod = mod;
    }

    @Override
    public void register() {
        new PayloadRegistrar(Constants.MODID)
            .playToServer(ServerboundServerPayload.TYPE, ServerboundServerPayload.STREAM_CODEC, (payload, context) -> {
                if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                    return; // not a server player
                }

                PacketDistributor.sendToPlayer(serverPlayer, new ClientboundServerPayload(Constants.PROTOCOL, Constants.RESPONSE_SUCCESS, Config.WEB_ADDRESS));
            })
            .playToClient(ClientboundServerPayload.TYPE, ClientboundServerPayload.STREAM_CODEC, NeoForgeNetwork.NOOP_HANDLER::handle)
            .playToServer(ServerboundMapPayload.TYPE, ServerboundMapPayload.STREAM_CODEC, (payload, context) -> {

            })
            .playToClient(ClientboundMapPayload.TYPE, ClientboundMapPayload.STREAM_CODEC, NeoForgeNetwork.NOOP_HANDLER::handle);
    }

    @Override
    public void unregister() {
        // do nothing ig
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
            PacketDistributor.sendToPlayer(player, new ClientboundMapPayload(Constants.PROTOCOL, Constants.ERROR_NO_SUCH_MAP, id));
            return;
        }

        ServerLevel level = this.mod.getServer().getLevel(map.dimension);
        if (level == null) {
            PacketDistributor.sendToPlayer(player, new ClientboundMapPayload(Constants.PROTOCOL, Constants.ERROR_NO_SUCH_WORLD, id));
            return;
        }

        PacketDistributor.sendToPlayer(player, new ClientboundMapPayload(
                Constants.PROTOCOL, Constants.RESPONSE_SUCCESS, id,
                map.scale, map.centerX, map.centerZ, level.dimension().location().toString().replace(":", "-")
        ));
    }
}
