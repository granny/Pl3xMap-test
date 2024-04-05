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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.scheduler.Scheduler;
import net.pl3x.map.fabric.client.duck.MapInstance;
import net.pl3x.map.fabric.client.manager.NetworkManager;
import net.pl3x.map.fabric.client.manager.TileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class Pl3xMapFabricClient implements ClientModInitializer {
    private static Pl3xMapFabricClient instance;

    private final NetworkManager networkManager;
    private final Scheduler scheduler;
    private final TileManager tileManager;
    private final ExecutorService executor = Pl3xMap.ThreadFactory.createService("Pl3xMap-Update");
    private KeyMapping keyBinding;
    private boolean isEnabled;
    private boolean isOnServer;
    private String serverUrl;
    private int tick;
    public Pl3xMapFabricClient() {
        instance = this;
        this.networkManager = new NetworkManager(this);
        this.scheduler = new Scheduler();
        this.tileManager = new TileManager(this);
    }

    public static Pl3xMapFabricClient getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        this.keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "pl3xmap.keymap.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "pl3xmap.title"
        ));

        getNetworkManager().initialize();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.isSingleplayer()) {
                return;
            }
            setEnabled(true);
            setIsOnServer(true);
            getScheduler().addTask(0, getNetworkManager()::requestServerData);
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
            if (Minecraft.getInstance().player == null) {
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

    public @NotNull NetworkManager getNetworkManager() {
        return this.networkManager;
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
