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
package net.pl3x.map.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.concurrent.ExecutorService;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.network.Constants;
import net.pl3x.map.core.scheduler.Scheduler;
import net.pl3x.map.fabric.client.duck.MapInstance;
import net.pl3x.map.fabric.client.manager.TileManager;
import net.pl3x.map.fabric.common.network.ClientboundMapPayload;
import net.pl3x.map.fabric.common.network.ClientboundServerPayload;
import net.pl3x.map.fabric.common.network.ServerboundMapPayload;
import net.pl3x.map.fabric.common.network.ServerboundServerPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pl3xMapFabricClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Pl3xMapFabricClient.class);

    private static Pl3xMapFabricClient instance;

    private final Scheduler scheduler;
    private final TileManager tileManager;
    private final ExecutorService executor = Pl3xMap.ThreadFactory.createService("Pl3xMap-Update");

    public static Pl3xMapFabricClient getInstance() {
        return instance;
    }

    private KeyMapping keyBinding;
    private boolean isEnabled;
    private boolean isOnServer;
    private String serverUrl;
    private int tick;

    public Pl3xMapFabricClient() {
        instance = this;
        this.scheduler = new ClientScheduler();
        this.tileManager = new TileManager(this);
    }

    @Override
    public void onInitializeClient() {
        this.keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "pl3xmap.keymap.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "pl3xmap.title"
        ));

        PayloadTypeRegistry.playC2S().register(ServerboundServerPayload.TYPE, ServerboundServerPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundServerPayload.TYPE, ClientboundServerPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ServerboundMapPayload.TYPE, ServerboundMapPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundMapPayload.TYPE, ClientboundMapPayload.STREAM_CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ClientboundServerPayload.TYPE, ClientboundServerPayload::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundMapPayload.TYPE, ClientboundMapPayload::handle);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.isSingleplayer()) {
                return;
            }
            setEnabled(true);
            setIsOnServer(true);
            getScheduler().addTask(0, () -> ClientPlayNetworking.send(new ServerboundServerPayload(Constants.PROTOCOL)));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            getScheduler().cancelAll();
            setEnabled(false);
            setIsOnServer(false);
            setServerUrl(null);
            getTileManager().clear();
            updateAllMapTextures();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            while (this.keyBinding.consumeClick()) {
                this.isEnabled = !this.isEnabled;
                MutableComponent onOff = Component.translatable("pl3xmap.toggled." + (this.isEnabled ? "on" : "off"));
                MutableComponent component = Component.translatable("pl3xmap.toggled.response", onOff);
                Minecraft.getInstance().player.displayClientMessage(component, true);
            }
            if (this.tick++ >= 20) {
                this.tick = 0;
                getScheduler().tick();
            }
        });
    }

    public @NotNull Scheduler getScheduler() {
        return this.scheduler;
    }

    public @NotNull TileManager getTileManager() {
        return this.tileManager;
    }

    public @NotNull ExecutorService getExecutor() {
        return this.executor;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isOnServer() {
        return this.isOnServer;
    }

    public void setIsOnServer(boolean isOnServer) {
        this.isOnServer = isOnServer;
    }

    public @Nullable String getServerUrl() {
        return this.serverUrl;
    }

    public void setServerUrl(String url) {
        this.serverUrl = url;
    }

    public void updateAllMapTextures() {
        Minecraft.getInstance().gameRenderer.getMapRenderer().maps.values().forEach(tex -> ((MapInstance) tex).updateImage());
    }
}
