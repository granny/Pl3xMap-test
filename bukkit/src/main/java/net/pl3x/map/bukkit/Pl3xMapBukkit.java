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
package net.pl3x.map.bukkit;

import java.util.UUID;
import net.pl3x.map.bukkit.command.BukkitCommandManager;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.server.ServerLoadedEvent;
import net.pl3x.map.core.network.Network;
import net.pl3x.map.core.player.Player;
import net.pl3x.map.core.player.PlayerListener;
import net.pl3x.map.core.player.PlayerRegistry;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Pl3xMapBukkit extends JavaPlugin implements Listener {
    private final Pl3xMapImpl pl3xmap;
    private final PlayerListener playerListener = new PlayerListener();

    private Network network;

    public Pl3xMapBukkit() {
        super();
        this.pl3xmap = new Pl3xMapImpl(this);
    }

    @Override
    public void onEnable() {
        try {
            io.papermc.paper.chunk.system.scheduling.ChunkFullTask.class.getDeclaredField("chunkLoads");
            io.papermc.paper.chunk.system.scheduling.ChunkFullTask.class.getDeclaredField("chunkGenerates");
            getLogger().severe("Pl3xMap does not support Folia");
            getLogger().severe("Pl3xMap will now disable itself");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } catch (Throwable ignore) {
        }

        this.pl3xmap.enable();

        getServer().getPluginManager().registerEvents(this, this);

        this.network = new BukkitNetwork(this);
        this.network.register();

        try {
            new BukkitCommandManager(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        getServer().getScheduler().runTaskTimer(this, () ->
                this.pl3xmap.getScheduler().tick(), 20, 1);
    }

    @Override
    public void onDisable() {
        if (this.network != null) {
            this.network.unregister();
            this.network = null;
        }

        this.pl3xmap.disable();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        PlayerRegistry registry = Pl3xMap.api().getPlayerRegistry();
        UUID uuid = event.getPlayer().getUniqueId();
        Player bukkitPlayer = registry.getOrDefault(uuid, () -> new BukkitPlayer(event.getPlayer()));
        this.playerListener.onJoin(bukkitPlayer);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        PlayerRegistry registry = Pl3xMap.api().getPlayerRegistry();
        UUID uuid = event.getPlayer().getUniqueId();
        Player bukkitPlayer = registry.unregister(uuid);
        if (bukkitPlayer != null) {
            this.playerListener.onQuit(bukkitPlayer);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        Pl3xMap.api().getWorldRegistry().getOrDefault(world.getName(),
                () -> new BukkitWorld(((CraftWorld) world).getHandle(), world.getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        Pl3xMap.api().getWorldRegistry().unregister(event.getWorld().getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerLoaded(ServerLoadEvent event) {
        Pl3xMap.api().getEventRegistry().callEvent(new ServerLoadedEvent());
    }
}
