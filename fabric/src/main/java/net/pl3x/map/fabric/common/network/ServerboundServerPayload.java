package net.pl3x.map.fabric.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.pl3x.map.core.network.Constants;

public record ServerboundServerPayload(int protocol) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ServerboundServerPayload> STREAM_CODEC = CustomPacketPayload.codec(ServerboundServerPayload::write, ServerboundServerPayload::new);
    public static final Type<ServerboundServerPayload> TYPE = new Type<>(new ResourceLocation(Constants.MODID, "server_server_data"));

    public ServerboundServerPayload(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readInt());
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(protocol);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
