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
package net.pl3x.map.fabric.client.manager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.pl3x.map.core.network.Constants;
import net.pl3x.map.fabric.client.Pl3xMapFabricClient;
import net.pl3x.map.fabric.client.duck.MapInstance;
import org.jetbrains.annotations.NotNull;

public class NetworkManager {
    private final ResourceLocation channel = new ResourceLocation(Constants.MODID, Constants.MODID);
    private final Pl3xMapFabricClient mod;

    public NetworkManager(@NotNull Pl3xMapFabricClient mod) {
        this.mod = mod;
    }

    public static byte[] accessByteBufWithCorrectSize(FriendlyByteBuf buf) {
        int i = buf.writerIndex();
        byte[] bytes = new byte[i];
        buf.getBytes(0, bytes);
        return bytes;
    }

    public void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(this.channel, (client, handler, buf, sender) -> {
            ByteArrayDataInput packet = in(accessByteBufWithCorrectSize(buf));

            int protocol = packet.readInt();
            if (protocol != Constants.PROTOCOL) {
                this.mod.setEnabled(false);
                return;
            }

            int packetType = packet.readInt();
            switch (packetType) {
                case Constants.SERVER_DATA -> {
                    int response = packet.readInt();
                    if (response != Constants.RESPONSE_SUCCESS) {
                        this.mod.setEnabled(false);
                        return;
                    }
                    this.mod.getTileManager().initialize();
                    this.mod.setServerUrl(packet.readUTF());
                }
                case Constants.MAP_DATA -> {
                    int response = packet.readInt();
                    switch (response) {
                        case Constants.ERROR_NO_SUCH_MAP, Constants.ERROR_NO_SUCH_WORLD, Constants.ERROR_NOT_VANILLA_MAP -> {
                            MapInstance texture = (MapInstance) Minecraft.getInstance().gameRenderer.getMapRenderer().maps.get(packet.readInt());
                            if (texture != null) {
                                texture.skip();
                            }
                        }
                        case Constants.RESPONSE_SUCCESS -> {
                            MapInstance texture = (MapInstance) Minecraft.getInstance().gameRenderer.getMapRenderer().maps.get(packet.readInt());
                            if (texture != null) {
                                texture.setData(packet.readByte(), packet.readInt(), packet.readInt(), packet.readUTF());
                            }
                        }
                    }
                }
            }
        });
    }

    public void requestServerData() {
        ByteArrayDataOutput out = out();
        out.writeInt(Constants.PROTOCOL);
        out.writeInt(Constants.SERVER_DATA);
        sendPacket(out);
    }

    public void requestMapData(int id) {
        ByteArrayDataOutput out = out();
        out.writeInt(Constants.PROTOCOL);
        out.writeInt(Constants.MAP_DATA);
        out.writeInt(id);
        sendPacket(out);
    }

    private void sendPacket(@NotNull ByteArrayDataOutput packet) {
        if (Minecraft.getInstance().getConnection() == null) {
            // not in game yet; reschedule
            this.mod.getScheduler().addTask(0, () -> sendPacket(packet));
            return;
        }
        ClientPlayNetworking.send(this.channel, new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.toByteArray())));
    }

    @SuppressWarnings("UnstableApiUsage")
    private @NotNull ByteArrayDataOutput out() {
        return ByteStreams.newDataOutput();
    }

    @SuppressWarnings("UnstableApiUsage")
    private @NotNull ByteArrayDataInput in(byte[] bytes) {
        return ByteStreams.newDataInput(bytes);
    }
}
