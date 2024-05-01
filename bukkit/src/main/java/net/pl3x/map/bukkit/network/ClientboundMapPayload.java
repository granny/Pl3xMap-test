package net.pl3x.map.bukkit.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.pl3x.map.core.network.Constants;

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
}
