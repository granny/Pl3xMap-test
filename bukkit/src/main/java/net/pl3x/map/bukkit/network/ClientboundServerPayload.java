package net.pl3x.map.bukkit.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.pl3x.map.core.network.Constants;

public record ClientboundServerPayload(int protocol, int response, String webAddress) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ClientboundServerPayload> STREAM_CODEC = CustomPacketPayload.codec(ClientboundServerPayload::write, ClientboundServerPayload::new);
    public static final Type<ClientboundServerPayload> TYPE = new Type<>(new ResourceLocation(Constants.MODID, "client_server_data"));

    public ClientboundServerPayload(int protocol, int response) {
        this(protocol, response, null);
    }

    public ClientboundServerPayload(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readUtf());
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(protocol);
        friendlyByteBuf.writeInt(response);
        friendlyByteBuf.writeUtf(webAddress);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
