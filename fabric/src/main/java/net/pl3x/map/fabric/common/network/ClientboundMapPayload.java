package net.pl3x.map.fabric.common.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.pl3x.map.core.network.Constants;
import net.pl3x.map.fabric.client.Pl3xMapFabricClient;
import net.pl3x.map.fabric.client.duck.MapInstance;

public record ClientboundMapPayload(int protocol, int response, int mapId, byte scale, int centerX, int centerZ, String worldName) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ClientboundMapPayload> STREAM_CODEC = CustomPacketPayload.codec(ClientboundMapPayload::write, ClientboundMapPayload::new);
    public static final Type<ClientboundMapPayload> TYPE = new Type<>(new ResourceLocation(Constants.MODID, "client_map_data"));

    public ClientboundMapPayload(int protocol, int response, int mapId) {
        this(protocol, response, mapId, (byte) 0, 0, 0, null);
    }

    public ClientboundMapPayload(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readByte(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readUtf());
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(protocol);
        friendlyByteBuf.writeInt(response);
        friendlyByteBuf.writeInt(mapId);
        friendlyByteBuf.writeByte(scale);
        friendlyByteBuf.writeInt(centerX);
        friendlyByteBuf.writeInt(centerZ);
        friendlyByteBuf.writeUtf(worldName);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundMapPayload payload, ClientPlayNetworking.Context context) {
        Pl3xMapFabricClient instance = Pl3xMapFabricClient.getInstance();
        if (payload.protocol != Constants.PROTOCOL) {
            instance.setEnabled(false);
            return;
        }

        switch (payload.response) {
            case Constants.ERROR_NO_SUCH_MAP, Constants.ERROR_NO_SUCH_WORLD, Constants.ERROR_NOT_VANILLA_MAP -> {
                MapInstance texture = (MapInstance) Minecraft.getInstance().gameRenderer.getMapRenderer().maps.get(payload.mapId);
                if (texture != null) {
                    texture.skip();
                }
            }
            case Constants.RESPONSE_SUCCESS -> {
                MapInstance texture = (MapInstance) Minecraft.getInstance().gameRenderer.getMapRenderer().maps.get(payload.mapId);
                if (texture != null) {
                    texture.setData(payload.scale, payload.centerX, payload.centerZ, payload.worldName);
                }
            }
        }
    }
}
