package net.pl3x.map.fabric.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.pl3x.map.core.network.Constants;

public record ServerboundMapPayload(int protocol, int mapId) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ServerboundMapPayload> STREAM_CODEC = CustomPacketPayload.codec(ServerboundMapPayload::write, ServerboundMapPayload::new);
    public static final Type<ServerboundMapPayload> TYPE = new Type<>(new ResourceLocation(Constants.MODID, "server_map_data"));

    public ServerboundMapPayload(int mapId) {
        this(Constants.PROTOCOL, mapId);
    }

    public ServerboundMapPayload(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(protocol);
        friendlyByteBuf.writeInt(mapId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
